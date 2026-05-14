package fr.skyblockcraft.merchant;

import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.economy.EconomyManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages Sky Merchant offers (loaded from config), the right-click interaction
 * that opens the chat-based shop, and the purchase logic.
 *
 * For the v0.1 prototype, the shop is rendered as a chat menu with clickable
 * text components. Right-clicking a villager tagged "SkyMerchant" sends the menu
 * to the player. Clicking a "[BUY]" link runs /skyblock buy <offerId>.
 */
public class SkyMerchantManager {

    /** Vanilla "command tag" we attach to an entity to mark it as a Sky Merchant. */
    public static final String SKY_MERCHANT_TAG = "skyblockcraft_sky_merchant";

    private static final Map<String, SkyMerchantOffer> OFFERS = new LinkedHashMap<>();

    public static void loadOffers() {
        OFFERS.clear();
        Map<String, String> configOffers = SkyblockCraftConfig.get().merchantOffers;
        if (configOffers == null) return;

        for (Map.Entry<String, String> e : configOffers.entrySet()) {
            SkyMerchantOffer offer = SkyMerchantOffer.parse(e.getKey(), e.getValue());
            if (offer != null) {
                OFFERS.put(offer.id(), offer);
            }
        }
        SkyblockCraft.LOGGER.info("[Merchant] Loaded {} valid offers", OFFERS.size());
    }

    public static Map<String, SkyMerchantOffer> getOffers() {
        return OFFERS;
    }

    public static SkyMerchantOffer getOffer(String id) {
        return OFFERS.get(id);
    }

    // ---- Marking an entity as a Sky Merchant ----

    /**
     * Marks the given entity as a Sky Merchant (will respond to right-click with the shop).
     * Uses vanilla scoreboard tags via {@link Entity#addCommandTag(String)} so the
     * marker persists with the entity and survives chunk reloads.
     */
    public static void markAsMerchant(Entity entity) {
        entity.addCommandTag(SKY_MERCHANT_TAG);
        // Apply a friendly display name and make it visible
        entity.setCustomName(Text.literal(SkyblockCraftConfig.get().merchantDisplayName)
                .formatted(Formatting.YELLOW, Formatting.BOLD));
        entity.setCustomNameVisible(true);
    }

    public static boolean isMerchant(Entity entity) {
        return entity.getCommandTags().contains(SKY_MERCHANT_TAG);
    }

    // ---- Interaction handler ----

    public static void registerInteractionHandler() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            if (!isMerchant(entity)) return ActionResult.PASS;

            // Show the shop menu, swallow the vanilla interaction
            sendShopMenu(sp);
            return ActionResult.SUCCESS;
        });
    }

    public static void sendShopMenu(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        long balance = EconomyManager.get(server).getBalance(player.getUuid());

        player.sendMessage(
                Text.literal("═══════ ").formatted(Formatting.GOLD)
                        .append(Text.literal(SkyblockCraftConfig.get().merchantDisplayName).formatted(Formatting.YELLOW, Formatting.BOLD))
                        .append(Text.literal(" ═══════").formatted(Formatting.GOLD)),
                false
        );
        player.sendMessage(
                Text.translatable("skyblockcraft.merchant.your_balance", balance).formatted(Formatting.AQUA),
                false
        );
        player.sendMessage(Text.literal("─────────────────────────").formatted(Formatting.DARK_GRAY), false);

        if (OFFERS.isEmpty()) {
            player.sendMessage(Text.translatable("skyblockcraft.merchant.no_offers").formatted(Formatting.RED), false);
            return;
        }

        for (SkyMerchantOffer offer : OFFERS.values()) {
            MutableText line = Text.literal(" • ").formatted(Formatting.GRAY)
                    .append(Text.literal(offer.count() + "× ").formatted(Formatting.WHITE))
                    .append(offer.item().getName().copy().formatted(Formatting.GREEN))
                    .append(Text.literal(" — ").formatted(Formatting.GRAY))
                    .append(Text.literal(offer.priceCoins() + " coins").formatted(Formatting.GOLD))
                    .append(Text.literal(" "))
                    .append(Text.literal("[BUY]")
                            .setStyle(Style.EMPTY
                                    .withColor(Formatting.AQUA)
                                    .withBold(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblock buy " + offer.id()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.translatable("skyblockcraft.merchant.hover_buy", offer.id())))));
            player.sendMessage(line, false);
        }
        player.sendMessage(Text.literal("─────────────────────────").formatted(Formatting.DARK_GRAY), false);
    }

    // ---- Purchase logic ----

    /**
     * Attempts to buy the offer for the given player.
     * Returns a result code:
     *   1 = success
     *   0 = generic fail
     *  -1 = unknown offer
     *  -2 = not enough coins
     *  -3 = inventory full
     */
    public static int buy(ServerPlayerEntity player, String offerId) {
        SkyMerchantOffer offer = OFFERS.get(offerId);
        if (offer == null) {
            player.sendMessage(Text.translatable("skyblockcraft.merchant.unknown_offer", offerId).formatted(Formatting.RED), false);
            return -1;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return 0;
        EconomyManager eco = EconomyManager.get(server);

        long balance = eco.getBalance(player.getUuid());
        if (balance < offer.priceCoins()) {
            player.sendMessage(
                    Text.translatable("skyblockcraft.merchant.not_enough", offer.priceCoins(), balance).formatted(Formatting.RED),
                    false
            );
            return -2;
        }

        // Try to give the item. insertStack mutates the stack (consumes count as it inserts).
        ItemStack stack = offer.newStack();
        player.getInventory().insertStack(stack);
        if (!stack.isEmpty()) {
            // Inventory was full — drop the remainder at the player's feet
            player.dropItem(stack, false);
        }

        // Charge coins
        eco.tryRemove(player.getUuid(), offer.priceCoins());

        long newBalance = eco.getBalance(player.getUuid());
        player.sendMessage(
                Text.translatable("skyblockcraft.merchant.bought",
                        offer.count(), offer.item().getName().copy(), offer.priceCoins(), newBalance).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    // ---- List for debugging / config ----

    public static List<String> listOfferIds() {
        return new ArrayList<>(OFFERS.keySet());
    }
}
