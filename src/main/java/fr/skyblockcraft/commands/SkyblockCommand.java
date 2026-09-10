package fr.skyblockcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.merchant.MerchantType;
import fr.skyblockcraft.merchant.SkyMerchantManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * /skyblock administrative + utility commands:
 *   /skyblock spawn merchant <type>  - OP, spawn a Sky Merchant of the given type
 *   /skyblock reload                 - OP, reload config from disk
 *   /skyblock version                - print mod version + build type
 *   /skyblock debug ...              - DEBUG build only
 */
public class SkyblockCommand {

    private static final SuggestionProvider<ServerCommandSource> MERCHANT_TYPE_SUGGESTIONS =
            (ctx, builder) -> {
                for (MerchantType t : MerchantType.values()) {
                    builder.suggest(t.getId());
                }
                return builder.buildFuture();
            };

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            net.minecraft.command.CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(CommandManager.literal("skyblock")
                .then(CommandManager.literal("spawn")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("merchant")
                                .then(CommandManager.argument("type", StringArgumentType.word())
                                        .suggests(MERCHANT_TYPE_SUGGESTIONS)
                                        .executes(SkyblockCommand::onSpawnMerchant))))
                .then(CommandManager.literal("remove")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("merchant")
                                .executes(SkyblockCommand::onRemoveMerchant)))
                .then(CommandManager.literal("reload")
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(SkyblockCommand::onReload))
                .then(CommandManager.literal("version")
                        .executes(SkyblockCommand::onVersion))
                .then(DebugCommand.build())
        );
    }

    private static int onSpawnMerchant(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String typeArg = StringArgumentType.getString(ctx, "type");
        MerchantType type = MerchantType.fromId(typeArg);
        if (type == null) {
            ctx.getSource().sendError(
                    Text.translatable("skyblockcraft.command.unknown_merchant_type", typeArg)
            );
            return 0;
        }

        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d pos = player.getPos();
        BlockPos blockPos = BlockPos.ofFloored(pos);

        VillagerEntity villager = EntityType.VILLAGER.create(world);
        if (villager == null) {
            ctx.getSource().sendError(Text.literal("Failed to create villager entity"));
            return 0;
        }
        villager.refreshPositionAndAngles(pos.x, pos.y, pos.z, player.getYaw(), 0f);
        villager.setAiDisabled(true);
        villager.setInvulnerable(true);
        villager.setPersistent();
        world.spawnEntity(villager);

        SkyMerchantManager.markAsMerchant(villager, type);

        final MerchantType finalType = type;
        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.command.merchant_spawned",
                        Text.translatable(finalType.getTranslationKey()),
                        blockPos.getX(), blockPos.getY(), blockPos.getZ())
                        .formatted(Formatting.GREEN),
                true
        );
        return 1;
    }

    /**
     * Removes the nearest Sky Merchant within 20 blocks of the player.
     * Needed because merchants are now truly invulnerable — they can't be
     * killed by attacking them. This is the intended cleanup path.
     */
    private static int onRemoveMerchant(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d pos = player.getPos();
        Box searchBox = new Box(pos, pos).expand(20.0);

        List<Entity> nearby = world.getOtherEntities(null, searchBox, SkyMerchantManager::isMerchant);
        if (nearby.isEmpty()) {
            ctx.getSource().sendError(Text.translatable("skyblockcraft.command.no_merchant_nearby"));
            return 0;
        }

        // Find the closest one
        Entity closest = nearby.get(0);
        double closestDist = closest.squaredDistanceTo(player);
        for (Entity e : nearby) {
            double d = e.squaredDistanceTo(player);
            if (d < closestDist) {
                closest = e;
                closestDist = d;
            }
        }

        BlockPos removedPos = closest.getBlockPos();
        closest.discard();
        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.command.merchant_removed",
                        removedPos.getX(), removedPos.getY(), removedPos.getZ())
                        .formatted(Formatting.YELLOW),
                true
        );
        return 1;
    }

    private static int onReload(CommandContext<ServerCommandSource> ctx) {
        SkyblockCraftConfig.load();
        SkyMerchantManager.loadOffers();
        int totalOffers = 0;
        int totalBuybacks = 0;
        for (MerchantType t : MerchantType.values()) {
            totalOffers += SkyMerchantManager.getOfferSet(t).getOffers().size();
            totalBuybacks += SkyMerchantManager.getOfferSet(t).getBuybacks().size();
        }
        final int fo = totalOffers, fb = totalBuybacks;
        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.command.reloaded", fo, fb).formatted(Formatting.GREEN),
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
        int totalOffers = 0;
        int totalBuybacks = 0;
        for (MerchantType t : MerchantType.values()) {
            totalOffers += SkyMerchantManager.getOfferSet(t).getOffers().size();
            totalBuybacks += SkyMerchantManager.getOfferSet(t).getBuybacks().size();
        }
        final int fo = totalOffers, fb = totalBuybacks;
        ctx.getSource().sendFeedback(
                () -> Text.literal("  " + fo + " offer(s), " + fb + " buyback(s) loaded across "
                        + MerchantType.values().length + " types").formatted(Formatting.GRAY),
                false
        );
        return 1;
    }
}
