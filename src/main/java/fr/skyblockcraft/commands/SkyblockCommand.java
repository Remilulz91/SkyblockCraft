package fr.skyblockcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.merchant.SkyMerchantManager;
import fr.skyblockcraft.merchant.SkyMerchantOffer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * /skyblock administrative + utility commands:
 *   /skyblock spawn merchant     - OP, spawns a Sky Merchant at the player's location
 *   /skyblock buy <offerId>      - used by the [BUY] click in the chat shop
 *   /skyblock reload             - OP, reload config from disk
 *   /skyblock version            - print mod version + build type
 *   /skyblock debug ...          - DEBUG build only (see DebugCommand)
 */
public class SkyblockCommand {

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            net.minecraft.command.CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        SuggestionProvider<ServerCommandSource> offerIdSuggestions = (ctx, builder) -> {
            for (String id : SkyMerchantManager.listOfferIds()) {
                builder.suggest(id);
            }
            return builder.buildFuture();
        };

        dispatcher.register(CommandManager.literal("skyblock")
                .then(CommandManager.literal("spawn")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("merchant")
                                .executes(SkyblockCommand::onSpawnMerchant)))
                .then(CommandManager.literal("buy")
                        .then(CommandManager.argument("offerId", StringArgumentType.word())
                                .suggests(offerIdSuggestions)
                                .executes(SkyblockCommand::onBuy)))
                .then(CommandManager.literal("reload")
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(SkyblockCommand::onReload))
                .then(CommandManager.literal("version")
                        .executes(SkyblockCommand::onVersion))
                .then(DebugCommand.build())
        );
    }

    private static int onSpawnMerchant(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d pos = player.getPos();
        BlockPos blockPos = BlockPos.ofFloored(pos);

        VillagerEntity villager = EntityType.VILLAGER.create(world, SpawnReason.COMMAND);
        if (villager == null) {
            ctx.getSource().sendError(Text.literal("Failed to create villager entity"));
            return 0;
        }
        villager.refreshPositionAndAngles(pos.x, pos.y, pos.z, player.getYaw(), 0f);
        // Stop the villager from being attacked / wandering: make it persistent (no despawn)
        villager.setAiDisabled(true);
        villager.setInvulnerable(true);
        villager.setPersistent();
        world.spawnEntity(villager);

        SkyMerchantManager.markAsMerchant(villager);

        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.command.merchant_spawned",
                        blockPos.getX(), blockPos.getY(), blockPos.getZ()).formatted(Formatting.GREEN),
                true
        );
        return 1;
    }

    private static int onBuy(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        String offerId = StringArgumentType.getString(ctx, "offerId");
        SkyMerchantManager.buy(player, offerId);
        return 1;
    }

    private static int onReload(CommandContext<ServerCommandSource> ctx) {
        SkyblockCraftConfig.load();
        SkyMerchantManager.loadOffers();
        int count = SkyMerchantManager.getOffers().size();
        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.command.reloaded", count).formatted(Formatting.GREEN),
                true
        );
        return 1;
    }

    private static int onVersion(CommandContext<ServerCommandSource> ctx) {
        String buildType = SkyblockCraft.isDebugBuild() ? "DEBUG" : "PUBLIC";
        ctx.getSource().sendFeedback(
                () -> Text.literal("SkyblockCraft ").formatted(Formatting.GOLD)
                        .append(Text.literal("(" + buildType + " build)").formatted(Formatting.GRAY)),
                false
        );
        // Show offer count
        int count = SkyMerchantManager.getOffers().size();
        ctx.getSource().sendFeedback(
                () -> Text.literal("  " + count + " merchant offer(s) loaded").formatted(Formatting.GRAY),
                false
        );
        return 1;
    }
}
