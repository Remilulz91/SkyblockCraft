package fr.skyblockcraft.island;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Generates the classic Skyblock starter island.
 *
 * Layout (relative to island center on the configured Y):
 *   - Dirt platform: ~3x3 with grass on top
 *   - Sand block under a cactus
 *   - Lava bucket on top of an obsidian/stone block (in the starter chest, actually)
 *   - Oak tree (sapling already grown into a small tree)
 *   - Wooden chest with starter items: 1 lava bucket, 1 ice, 1 melon seed, 1 pumpkin seed,
 *     1 sugar cane, 1 cactus, a few seeds and saplings, 1 bone meal
 */
public class IslandGenerator {

    /**
     * Generates the island in the given world at the island's logical position.
     * Safe to call multiple times — will overwrite existing blocks in the zone.
     */
    public static void generate(ServerWorld world, PlayerIsland island) {
        BlockPos center = new BlockPos(
                island.spawn.getX(),
                island.spawn.getY() - 2, // platform level (spawn is 2 above platform)
                island.spawn.getZ()
        );

        // 1. Dirt platform 5x1x5, grass on top
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.DIRT.getDefaultState());
                world.setBlockState(center.add(dx, 0, dz), Blocks.GRASS_BLOCK.getDefaultState());
            }
        }

        // 2. Plant a small oak tree at (-1, +1, -1) — manually place trunk + leaves
        BlockPos trunkBase = center.add(-1, 1, -1);
        for (int i = 0; i < 4; i++) {
            world.setBlockState(trunkBase.up(i), Blocks.OAK_LOG.getDefaultState());
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 2; dy <= 4; dy++) {
                    // Hollow-ish leaves around the top of the trunk
                    BlockPos leafPos = trunkBase.add(dx, dy, dz);
                    if (Math.abs(dx) + Math.abs(dz) <= 3 && world.isAir(leafPos)) {
                        world.setBlockState(leafPos, Blocks.OAK_LEAVES.getDefaultState());
                    }
                }
            }
        }

        // 3. A single sand block at (+1, +1, +1) with a cactus on top
        BlockPos sandPos = center.add(1, 1, 1);
        world.setBlockState(sandPos, Blocks.SAND.getDefaultState());
        world.setBlockState(sandPos.up(), Blocks.CACTUS.getDefaultState());

        // 4. Starter chest at (+1, +1, -1)
        BlockPos chestPos = center.add(1, 1, -1);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState());
        fillStarterChest(world, chestPos);
    }

    private static void fillStarterChest(ServerWorld world, BlockPos chestPos) {
        BlockEntity be = world.getBlockEntity(chestPos);
        if (!(be instanceof ChestBlockEntity chest)) return;

        // ChestBlockEntity implements Inventory — we can call setStack() directly.
        int slot = 0;
        chest.setStack(slot++, new ItemStack(Items.LAVA_BUCKET));
        chest.setStack(slot++, new ItemStack(Items.ICE));
        chest.setStack(slot++, new ItemStack(Items.MELON_SEEDS));
        chest.setStack(slot++, new ItemStack(Items.PUMPKIN_SEEDS));
        chest.setStack(slot++, new ItemStack(Items.SUGAR_CANE));
        chest.setStack(slot++, new ItemStack(Items.RED_MUSHROOM));
        chest.setStack(slot++, new ItemStack(Items.BROWN_MUSHROOM));
        chest.setStack(slot++, new ItemStack(Items.BONE_MEAL, 3));
        chest.setStack(slot++, new ItemStack(Items.WHEAT_SEEDS, 3));
        chest.setStack(slot, new ItemStack(Items.OAK_SAPLING, 2));
        chest.markDirty();
    }
}
