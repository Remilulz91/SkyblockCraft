package fr.skyblockcraft.island;

import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Enforces island protection: only the owner and trusted co-op players may
 * break or place blocks in an island's protection zone.
 * <p>
 * Also tracks the {@code placedBlocks} counter on each island (drives level):
 * increments on allowed placement, decrements on allowed break (clamped at 0).
 */
public class IslandProtection {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient) return true;
            if (player.getServer() == null) return true;

            IslandManager mgr = IslandManager.get(player.getServer());
            PlayerIsland island = mgr.findIslandAt(pos);
            if (island == null) return true;

            if (canModify(island, player)) {
                if (island.placedBlocks > 0) {
                    island.placedBlocks--;
                    mgr.markDirtyPublic();
                }
                return true;
            }
            player.sendMessage(
                    Text.translatable("skyblockcraft.island.cant_break_here").formatted(Formatting.RED),
                    true // action bar
            );
            return false;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (player.getServer() == null) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!(stack.getItem() instanceof BlockItem)) return ActionResult.PASS;

            // The block will be placed on the face adjacent to the clicked one
            BlockPos placedPos = hitResult.getBlockPos().offset(hitResult.getSide());

            IslandManager mgr = IslandManager.get(player.getServer());
            PlayerIsland island = mgr.findIslandAt(placedPos);
            if (island == null) return ActionResult.PASS;

            if (canModify(island, player)) {
                // Optimistic increment: this fires before placement, but for MVP
                // the level being off by 1-2 blocks in edge cases is acceptable
                island.placedBlocks++;
                mgr.markDirtyPublic();
                return ActionResult.PASS;
            }
            player.sendMessage(
                    Text.translatable("skyblockcraft.island.cant_build_here").formatted(Formatting.RED),
                    true
            );
            return ActionResult.FAIL;
        });

        SkyblockCraft.LOGGER.info("[IslandProtection] Registered break/place handlers");
    }

    /** Whether the given player may build/break on the given island. */
    private static boolean canModify(PlayerIsland island, PlayerEntity player) {
        UUID id = player.getUuid();
        if (island.owner.equals(id)) return true;
        if (island.trustedPlayers.contains(id)) return true;
        if (SkyblockCraftConfig.get().opBypassIslandProtection && player.hasPermissionLevel(2)) return true;
        return false;
    }
}
