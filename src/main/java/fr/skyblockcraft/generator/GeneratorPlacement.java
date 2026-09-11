package fr.skyblockcraft.generator;

import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.island.IslandManager;
import fr.skyblockcraft.island.PlayerIsland;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Enforces the per-island maximum number of coin generators.
 *
 * Restrictions on WHICH island the player can place a generator on are already
 * handled by {@link fr.skyblockcraft.island.IslandProtection} (they can only
 * build on their own island or on islands they've been trusted on). This class
 * only adds the "max per island" check.
 */
public class GeneratorPlacement {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (player.getServer() == null) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!(stack.getItem() instanceof BlockItem blockItem)) return ActionResult.PASS;
            if (!(blockItem.getBlock() instanceof CoinGeneratorBlock)) return ActionResult.PASS;

            BlockPos placedPos = hitResult.getBlockPos().offset(hitResult.getSide());

            IslandManager mgr = IslandManager.get(player.getServer());
            PlayerIsland island = mgr.findIslandAt(placedPos);
            if (island == null) return ActionResult.PASS; // outside any island, let normal handling proceed

            int max = SkyblockCraftConfig.get().maxGeneratorsPerIsland;
            int current = countGeneratorsOnIsland(world, island);
            if (current >= max) {
                player.sendMessage(
                        Text.translatable("skyblockcraft.generator.limit_reached", current, max)
                                .formatted(Formatting.RED),
                        true // action bar
                );
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        SkyblockCraft.LOGGER.info("[GeneratorPlacement] Registered max-per-island guard");
    }

    /**
     * Counts coin generators within the island's protection zone.
     * Uses chunk-level iteration for efficiency (only visits block entities in
     * chunks that overlap the zone, not every block position).
     */
    private static int countGeneratorsOnIsland(World world, PlayerIsland island) {
        if (!(world instanceof ServerWorld sw)) return 0;
        int radius = SkyblockCraftConfig.get().protectionRadius;
        BlockPos center = island.spawn;

        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius) >> 4;

        int count = 0;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                Chunk chunk = sw.getChunk(cx, cz);
                if (!(chunk instanceof WorldChunk wc)) continue;
                for (BlockEntity be : wc.getBlockEntities().values()) {
                    if (!(be instanceof CoinGeneratorBlockEntity)) continue;
                    BlockPos p = be.getPos();
                    if (Math.abs(p.getX() - center.getX()) <= radius
                            && Math.abs(p.getZ() - center.getZ()) <= radius) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
