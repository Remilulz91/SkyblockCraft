package fr.skyblockcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.economy.EconomyManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Player-facing economy commands:
 *   /balance               - show your balance
 *   /balance <player>      - show another player's balance (OP)
 *   /pay <player> <amount> - transfer coins to another player
 */
public class EconomyCommand {

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            net.minecraft.command.CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        // /balance
        dispatcher.register(CommandManager.literal("balance")
                .executes(EconomyCommand::onBalanceSelf)
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(EconomyCommand::onBalanceOther))
        );

        // /bal (alias)
        dispatcher.register(CommandManager.literal("bal")
                .executes(EconomyCommand::onBalanceSelf)
        );

        // /pay <player> <amount>
        dispatcher.register(CommandManager.literal("pay")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(CommandManager.argument("amount", LongArgumentType.longArg(1L))
                                .executes(EconomyCommand::onPay)))
        );
    }

    private static int onBalanceSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        long balance = EconomyManager.get(p.getServer()).getBalance(p.getUuid());
        p.sendMessage(
                Text.translatable("skyblockcraft.economy.balance_self", balance).formatted(Formatting.GOLD),
                false
        );
        return 1;
    }

    private static int onBalanceOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        long balance = EconomyManager.get(ctx.getSource().getServer()).getBalance(target.getUuid());
        ctx.getSource().sendFeedback(
                () -> Text.translatable("skyblockcraft.economy.balance_other",
                        target.getName().getString(), balance).formatted(Formatting.GOLD),
                false
        );
        return 1;
    }

    private static int onPay(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity recipient = EntityArgumentType.getPlayer(ctx, "target");
        long amount = LongArgumentType.getLong(ctx, "amount");

        SkyblockCraftConfig cfg = SkyblockCraftConfig.get();

        if (!cfg.allowPlayerPay) {
            sender.sendMessage(Text.translatable("skyblockcraft.economy.pay_disabled").formatted(Formatting.RED), false);
            return 0;
        }
        if (amount < cfg.minPayAmount) {
            sender.sendMessage(Text.translatable("skyblockcraft.economy.pay_too_low", cfg.minPayAmount).formatted(Formatting.RED), false);
            return 0;
        }
        if (sender.getUuid().equals(recipient.getUuid())) {
            sender.sendMessage(Text.translatable("skyblockcraft.economy.pay_self").formatted(Formatting.RED), false);
            return 0;
        }

        EconomyManager eco = EconomyManager.get(sender.getServer());
        boolean ok = eco.transfer(sender.getUuid(), recipient.getUuid(), amount);
        if (!ok) {
            sender.sendMessage(Text.translatable("skyblockcraft.economy.pay_not_enough").formatted(Formatting.RED), false);
            return 0;
        }

        sender.sendMessage(
                Text.translatable("skyblockcraft.economy.pay_sent", amount, recipient.getName().getString()).formatted(Formatting.GREEN),
                false
        );
        recipient.sendMessage(
                Text.translatable("skyblockcraft.economy.pay_received", amount, sender.getName().getString()).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }
}
