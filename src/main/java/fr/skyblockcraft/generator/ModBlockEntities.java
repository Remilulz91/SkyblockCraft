package fr.skyblockcraft.generator;

import fr.skyblockcraft.SkyblockCraft;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Registers the mod's BlockEntity types. Must be registered AFTER ModBlocks
 * because the type references the block instances.
 */
public class ModBlockEntities {

    public static final BlockEntityType<CoinGeneratorBlockEntity> COIN_GENERATOR = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            SkyblockCraft.id("coin_generator"),
            BlockEntityType.Builder.create(
                    CoinGeneratorBlockEntity::new,
                    ModBlocks.COIN_GENERATOR_BASIC,
                    ModBlocks.COIN_GENERATOR_ADVANCED,
                    ModBlocks.COIN_GENERATOR_ELITE
            ).build()
    );

    public static void register() {
        SkyblockCraft.LOGGER.info("[ModBlockEntities] Registered coin_generator");
    }
}
