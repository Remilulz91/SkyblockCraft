package fr.skyblockcraft.generator;

import fr.skyblockcraft.economy.EconomyManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * The block entity for a {@link CoinGeneratorBlock}.
 *
 * Ticks server-side. Every {@code tier.getTicksPerCycle()} ticks, if the owner
 * is online, credits them {@code tier.getCoinsPerCycle()} coins.
 * If the owner is offline, the cycle counter still advances (so the payout on
 * their next login isn't delayed by a full cycle) but the credit is skipped.
 */
public class CoinGeneratorBlockEntity extends BlockEntity {

    private UUID ownerUuid;
    private CoinGeneratorTier tier;
    private int tickCounter = 0;

    public CoinGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COIN_GENERATOR, pos, state);
        // Determine tier from the block's class at construction time
        if (state.getBlock() instanceof CoinGeneratorBlock cgb) {
            this.tier = cgb.tier;
        } else {
            this.tier = CoinGeneratorTier.BASIC;
        }
    }

    public UUID getOwner() { return ownerUuid; }
    public CoinGeneratorTier getTier() { return tier; }

    public void setOwner(UUID uuid) {
        this.ownerUuid = uuid;
        markDirty();
    }

    public void setTier(CoinGeneratorTier tier) {
        this.tier = tier;
        markDirty();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CoinGeneratorBlockEntity be) {
        if (be.tier == null) return;
        be.tickCounter++;
        if (be.tickCounter < be.tier.getTicksPerCycle()) return;
        be.tickCounter = 0;

        if (be.ownerUuid == null) return; // no owner yet (freshly placed via /setblock etc.)

        MinecraftServer server = world.getServer();
        if (server == null) return;
        ServerPlayerEntity ownerPlayer = server.getPlayerManager().getPlayer(be.ownerUuid);
        if (ownerPlayer == null) return; // owner offline — skip payout

        EconomyManager.get(server).add(be.ownerUuid, be.tier.getCoinsPerCycle());
    }

    // ---- Persistence ----

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (ownerUuid != null) nbt.putUuid("owner", ownerUuid);
        if (tier != null) nbt.putString("tier", tier.getId());
        nbt.putInt("tickCounter", tickCounter);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("owner")) ownerUuid = nbt.getUuid("owner");
        if (nbt.contains("tier")) {
            CoinGeneratorTier t = CoinGeneratorTier.fromId(nbt.getString("tier"));
            if (t != null) tier = t;
        }
        if (nbt.contains("tickCounter")) tickCounter = nbt.getInt("tickCounter");
    }
}
