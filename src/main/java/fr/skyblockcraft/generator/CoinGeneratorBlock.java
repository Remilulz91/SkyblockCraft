package fr.skyblockcraft.generator;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A coin generator block: while a placer is online, it periodically credits
 * the placer with coins according to its {@link CoinGeneratorTier}.
 *
 * There are 3 instances of this block registered (Basic/Advanced/Elite), each
 * with its own tier.
 */
public class CoinGeneratorBlock extends Block implements BlockEntityProvider {

    public final CoinGeneratorTier tier;

    public CoinGeneratorBlock(CoinGeneratorTier tier, AbstractBlock.Settings settings) {
        super(settings);
        this.tier = tier;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CoinGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        // Since this class doesn't extend BlockWithEntity, we can't use the
        // protected validateTicker helper — do the type check manually.
        if (type != ModBlockEntities.COIN_GENERATOR) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<CoinGeneratorBlockEntity>) CoinGeneratorBlockEntity::serverTick;
    }

    /**
     * Called after the block is placed. Records the placer's UUID and the tier
     * into the newly created block entity so it knows who to pay and how much.
     */
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient) return;
        if (!(placer instanceof PlayerEntity player)) return;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof CoinGeneratorBlockEntity gen) {
            gen.setOwner(player.getUuid());
            gen.setTier(this.tier);
            gen.markDirty();
        }
    }
}
