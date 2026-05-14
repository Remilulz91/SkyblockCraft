package fr.skyblockcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.island.IslandGenerator;
import fr.skyblockcraft.island.IslandManager;
import fr.skyblockcraft.island.PlayerIsland;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * /island commands:
 *   /island create           - generate your starter island and teleport you there
 *   /island home             - teleport to your island
 *   /island visit <player>   - teleport to another player's island (if allowed)
 *   /island delete           - delete your island record (admin shortcut available too)
 */
public class IslandCommand {

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            net.minecraft.command.CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(CommandManager.literal("island")
                .then(CommandManager.literal("create").executes(IslandCommand::onCreate))
                .then(CommandManager.literal("home").executes(IslandCommand::onHome))
                .then(CommandManager.literal("visit")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(IslandCommand::onVisit)))
                .then(CommandManager.literal("delete")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(IslandCommand::onDelete)))
        );

        // /is (alias)
        dispatcher.register(CommandManager.literal("is")
                .then(CommandManager.literal("create").executes(IslandCommand::onCreate))
                .then(CommandManager.literal("home").executes(IslandCommand::onHome))
        );
    }

    private static int onCreate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        MinecraftServer server = player.getServer();
        if (server == null) return 0;

        IslandManager mgr = IslandManager.get(server);
        if (mgr.hasIsland(player.getUuid()) && !SkyblockCraftConfig.get().debugAllowIslandOverride) {
            player.sendMessage(Text.translatable("skyblockcraft.island.already_have").formatted(Formatting.RED), false);
            player.sendMessage(Text.translatable("skyblockcraft.island.use_home").formatted(Formatting.GRAY), false);
            return 0;
        }

        // If override is allowed and they already have one, remove the old record first
        if (mgr.hasIsland(player.getUuid())) {
            mgr.removeIsland(player.getUuid());
        }

        PlayerIsland island = mgr.createIsland(player.getUuid());
        ServerWorld world = server.getOverworld();
        IslandGenerator.generate(world, island);

        // Teleport to spawn
        player.teleport(world, island.spawn.getX() + 0.5, island.spawn.getY(), island.spawn.getZ() + 0.5,
                java.util.Set.of(), 0f, 0f);

        player.sendMessage(
                Text.translatable("skyblockcraft.island.created",
                        island.spawn.getX(), island.spawn.getY(), island.spawn.getZ()).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    private static int onHome(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        MinecraftServer server = player.getServer();
        if (server == null) return 0;

        IslandManager mgr = IslandManager.get(server);
        PlayerIsland island = mgr.getIsland(player.getUuid());
        if (island == null) {
            player.sendMessage(Text.translatable("skyblockcraft.island.no_island").formatted(Formatting.RED), false);
            player.sendMessage(Text.translatable("skyblockcraft.island.use_create").formatted(Formatting.GRAY), false);
            return 0;
        }

        ServerWorld world = server.getOverworld();
        player.teleport(world, island.spawn.getX() + 0.5, island.spawn.getY(), island.spawn.getZ() + 0.5,
                java.util.Set.of(), 0f, 0f);
        player.sendMessage(Text.translatable("skyblockcraft.island.teleported_home").formatted(Formatting.AQUA), false);
        return 1;
    }

    private static int onVisit(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        if (!SkyblockCraftConfig.get().allowIslandVisits) {
            ctx.getSource().sendError(Text.translatable("skyblockcraft.island.visits_disabled"));
            return 0;
        }
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        MinecraftServer server = player.getServer();
        if (server == null) return 0;

        IslandManager mgr = IslandManager.get(server);
        PlayerIsland island = mgr.getIsland(target.getUuid());
        if (island == null) {
            player.sendMessage(Text.translatable("skyblockcraft.island.target_no_island", target.getName().getString()).formatted(Formatting.RED), false);
            return 0;
        }

        ServerWorld world = server.getOverworld();
        player.teleport(world, island.spawn.getX() + 0.5, island.spawn.getY(), island.spawn.getZ() + 0.5,
                java.util.Set.of(), 0f, 0f);
        player.sendMessage(
                Text.translatable("skyblockcraft.island.visiting", target.getName().getString()).formatted(Formatting.AQUA),
                false
        );
        return 1;
    }

    private static int onDelete(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        IslandManager mgr = IslandManager.get(ctx.getSource().getServer());
        if (!mgr.hasIsland(target.getUuid())) {
            ctx.getSource().sendError(Text.translatable("skyblockcraft.island.target_no_island", target.getName().getString()));
            return 0;
        }
        mgr.removeIsland(target.getUuid());
        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.island.deleted_admin", target.getName().getString()).formatted(Formatting.YELLOW),
                true
        );
        return 1;
    }
}
