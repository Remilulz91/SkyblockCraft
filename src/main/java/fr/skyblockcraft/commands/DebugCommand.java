package fr.skyblockcraft.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.economy.EconomyManager;
import fr.skyblockcraft.island.IslandManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * /skyblock debug subcommands. Only registered when the build is DEBUG
 * AND the config flag {@code enableDebugCommands} is true.
 *
 * Available in DEBUG build only:
 *   /skyblock debug givecoins <player> <amount>
 *   /skyblock debug setbalance <player> <amount>
 *   /skyblock debug resetisland <player>
 *   /skyblock debug info
 */
public class DebugCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("debug")
                .requires(src -> SkyblockCraft.isDebugBuild()
                        && SkyblockCraftConfig.get().enableDebugCommands
                        && src.hasPermissionLevel(2))
                .then(CommandManager.literal("givecoins")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1L))
                                        .executes(DebugCommand::onGiveCoins))))
                .then(CommandManager.literal("setbalance")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(0L))
                                        .executes(DebugCommand::onSetBalance))))
                .then(CommandManager.literal("resetisland")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(DebugCommand::onResetIsland)))
                .then(CommandManager.literal("info")
                        .executes(DebugCommand::onInfo));
    }

    private static int onGiveCoins(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        long amount = LongArgumentType.getLong(ctx, "amount");
        long newBalance = EconomyManager.get(ctx.getSource().getServer()).add(target.getUuid(), amount);
        ctx.getSource().sendFeedback(
                () -> Text.literal("[DEBUG] Gave " + amount + " coins to " + target.getName().getString()
                        + " (new balance: " + newBalance + ")").formatted(Formatting.LIGHT_PURPLE),
                true
        );
        return 1;
    }

    private static int onSetBalance(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        long amount = LongArgumentType.getLong(ctx, "amount");
        EconomyManager.get(ctx.getSource().getServer()).setBalance(target.getUuid(), amount);
        ctx.getSource().sendFeedback(
                () -> Text.literal("[DEBUG] Set " + target.getName().getString() + " balance to " + amount)
                        .formatted(Formatting.LIGHT_PURPLE),
                true
        );
        return 1;
    }

    private static int onResetIsland(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        IslandManager.get(ctx.getSource().getServer()).removeIsland(target.getUuid());
        ctx.getSource().sendFeedback(
                () -> Text.literal("[DEBUG] Removed island record for " + target.getName().getString())
                        .formatted(Formatting.LIGHT_PURPLE),
                true
        );
        return 1;
    }

    private static int onInfo(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        src.sendFeedback(() -> Text.literal("═══ SkyblockCraft DEBUG INFO ═══").formatted(Formatting.LIGHT_PURPLE), false);
        src.sendFeedback(() -> Text.literal("Build type: DEBUG").formatted(Formatting.GRAY), false);
        src.sendFeedback(() -> Text.literal("enableDebugCommands: " + SkyblockCraftConfig.get().enableDebugCommands).formatted(Formatting.GRAY), false);
        src.sendFeedback(() -> Text.literal("debugAllowIslandOverride: " + SkyblockCraftConfig.get().debugAllowIslandOverride).formatted(Formatting.GRAY), false);
        int totalOffers = 0, totalBuybacks = 0;
        for (fr.skyblockcraft.merchant.MerchantType t : fr.skyblockcraft.merchant.MerchantType.values()) {
            totalOffers += fr.skyblockcraft.merchant.SkyMerchantManager.getOfferSet(t).getOffers().size();
            totalBuybacks += fr.skyblockcraft.merchant.SkyMerchantManager.getOfferSet(t).getBuybacks().size();
        }
        final int fo = totalOffers, fb = totalBuybacks;
        src.sendFeedback(() -> Text.literal("merchants: " + fo + " offers, " + fb + " buybacks").formatted(Formatting.GRAY), false);
        return 1;
    }
}
