package fr.skyblockcraft.commands;

import com.mojang.authlib.GameProfile;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * /island commands.
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
                .then(CommandManager.literal("level").executes(IslandCommand::onLevel))
                .then(CommandManager.literal("top").executes(IslandCommand::onTop))
                .then(CommandManager.literal("trust")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(IslandCommand::onTrust)))
                .then(CommandManager.literal("untrust")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(IslandCommand::onUntrust)))
                .then(CommandManager.literal("trusted").executes(IslandCommand::onTrustedList))
                .then(CommandManager.literal("delete")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(IslandCommand::onDelete)))
        );

        // /is (alias)
        dispatcher.register(CommandManager.literal("is")
                .then(CommandManager.literal("create").executes(IslandCommand::onCreate))
                .then(CommandManager.literal("home").executes(IslandCommand::onHome))
                .then(CommandManager.literal("level").executes(IslandCommand::onLevel))
                .then(CommandManager.literal("top").executes(IslandCommand::onTop))
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
        if (mgr.hasIsland(player.getUuid())) {
            mgr.removeIsland(player.getUuid());
        }

        PlayerIsland island = mgr.createIsland(player.getUuid());
        ServerWorld world = server.getOverworld();
        IslandGenerator.generate(world, island);
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

    // ---- v0.3: level, top, trust, untrust, trusted ----

    private static int onLevel(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        IslandManager mgr = IslandManager.get(player.getServer());
        PlayerIsland island = mgr.getIsland(player.getUuid());
        if (island == null) {
            player.sendMessage(Text.translatable("skyblockcraft.island.no_island").formatted(Formatting.RED), false);
            return 0;
        }
        int blocksPerLevel = SkyblockCraftConfig.get().blocksPerLevel;
        int level = island.getLevel(blocksPerLevel);
        long blocks = island.placedBlocks;
        long nextThreshold = (long) (level + 1) * blocksPerLevel;
        player.sendMessage(
                Text.translatable("skyblockcraft.island.level_info", level, blocks, nextThreshold - blocks)
                        .formatted(Formatting.GOLD),
                false
        );
        return 1;
    }

    private static int onTop(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        MinecraftServer server = ctx.getSource().getServer();
        IslandManager mgr = IslandManager.get(server);
        List<PlayerIsland> top = mgr.getTopIslands(10);
        if (top.isEmpty()) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("skyblockcraft.island.top_empty").formatted(Formatting.GRAY),
                    false
            );
            return 0;
        }
        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.island.top_header").formatted(Formatting.GOLD),
                false
        );
        int blocksPerLevel = SkyblockCraftConfig.get().blocksPerLevel;
        int rank = 1;
        for (PlayerIsland island : top) {
            String name = resolvePlayerName(server, island.owner);
            int level = island.getLevel(blocksPerLevel);
            final int r = rank;
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("skyblockcraft.island.top_line", r, name, level, island.placedBlocks)
                            .formatted(Formatting.WHITE),
                    false
            );
            rank++;
        }
        return 1;
    }

    private static int onTrust(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        if (target.getUuid().equals(player.getUuid())) {
            player.sendMessage(Text.translatable("skyblockcraft.island.trust_self").formatted(Formatting.RED), false);
            return 0;
        }
        IslandManager mgr = IslandManager.get(player.getServer());
        PlayerIsland island = mgr.getIsland(player.getUuid());
        if (island == null) {
            player.sendMessage(Text.translatable("skyblockcraft.island.no_island").formatted(Formatting.RED), false);
            return 0;
        }
        if (!island.trustedPlayers.add(target.getUuid())) {
            player.sendMessage(
                    Text.translatable("skyblockcraft.island.already_trusted", target.getName().getString()).formatted(Formatting.YELLOW),
                    false
            );
            return 0;
        }
        mgr.markDirtyPublic();
        player.sendMessage(
                Text.translatable("skyblockcraft.island.trusted", target.getName().getString()).formatted(Formatting.GREEN),
                false
        );
        target.sendMessage(
                Text.translatable("skyblockcraft.island.you_are_trusted_by", player.getName().getString()).formatted(Formatting.AQUA),
                false
        );
        return 1;
    }

    private static int onUntrust(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        IslandManager mgr = IslandManager.get(player.getServer());
        PlayerIsland island = mgr.getIsland(player.getUuid());
        if (island == null) {
            player.sendMessage(Text.translatable("skyblockcraft.island.no_island").formatted(Formatting.RED), false);
            return 0;
        }
        if (!island.trustedPlayers.remove(target.getUuid())) {
            player.sendMessage(
                    Text.translatable("skyblockcraft.island.not_trusted", target.getName().getString()).formatted(Formatting.YELLOW),
                    false
            );
            return 0;
        }
        mgr.markDirtyPublic();
        player.sendMessage(
                Text.translatable("skyblockcraft.island.untrusted", target.getName().getString()).formatted(Formatting.YELLOW),
                false
        );
        return 1;
    }

    private static int onTrustedList(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        IslandManager mgr = IslandManager.get(player.getServer());
        PlayerIsland island = mgr.getIsland(player.getUuid());
        if (island == null) {
            player.sendMessage(Text.translatable("skyblockcraft.island.no_island").formatted(Formatting.RED), false);
            return 0;
        }
        if (island.trustedPlayers.isEmpty()) {
            player.sendMessage(Text.translatable("skyblockcraft.island.trusted_empty").formatted(Formatting.GRAY), false);
            return 0;
        }
        player.sendMessage(Text.translatable("skyblockcraft.island.trusted_header").formatted(Formatting.GOLD), false);
        MinecraftServer server = player.getServer();
        for (UUID uuid : island.trustedPlayers) {
            String name = resolvePlayerName(server, uuid);
            player.sendMessage(Text.literal(" • " + name).formatted(Formatting.WHITE), false);
        }
        return 1;
    }

    /** Resolves a player name from UUID: try online player first, then UserCache, else short UUID. */
    private static String resolvePlayerName(MinecraftServer server, UUID uuid) {
        ServerPlayerEntity online = server.getPlayerManager().getPlayer(uuid);
        if (online != null) return online.getName().getString();
        if (server.getUserCache() != null) {
            Optional<GameProfile> profile = server.getUserCache().getByUuid(uuid);
            if (profile.isPresent()) return profile.get().getName();
        }
        return uuid.toString().substring(0, 8);
    }
}
