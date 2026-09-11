package fr.skyblockcraft.generator;

import fr.skyblockcraft.SkyblockCraft;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * Registers the mod's blocks. Each coin-generator tier is its own block +
 * BlockItem so they have distinct textures, names, and creative-tab entries.
 */
public class ModBlocks {

    // Generators are "utility" blocks that players place and remove often —
    // so hardness stays low (~stone) and NO requiresTool() so they always drop
    // the item regardless of what the player uses to break them. Blast
    // resistance stays moderate so they survive a stray creeper.
    public static final CoinGeneratorBlock COIN_GENERATOR_BASIC = register(
            "coin_generator_basic",
            new CoinGeneratorBlock(CoinGeneratorTier.BASIC,
                    AbstractBlock.Settings.create()
                            .strength(1.5f, 6.0f)
                            .sounds(BlockSoundGroup.STONE))
    );

    public static final CoinGeneratorBlock COIN_GENERATOR_ADVANCED = register(
            "coin_generator_advanced",
            new CoinGeneratorBlock(CoinGeneratorTier.ADVANCED,
                    AbstractBlock.Settings.create()
                            .strength(2.0f, 8.0f)
                            .sounds(BlockSoundGroup.METAL))
    );

    public static final CoinGeneratorBlock COIN_GENERATOR_ELITE = register(
            "coin_generator_elite",
            new CoinGeneratorBlock(CoinGeneratorTier.ELITE,
                    AbstractBlock.Settings.create()
                            .strength(2.5f, 12.0f)
                            .sounds(BlockSoundGroup.METAL))
    );

    private static <T extends Block> T register(String name, T block) {
        Identifier id = SkyblockCraft.id(name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return block;
    }

    /** Called from SkyblockCraft.onInitialize() to trigger the static registrations. */
    public static void register() {
        SkyblockCraft.LOGGER.info("[ModBlocks] Registered 3 coin generator blocks");
    }
}
