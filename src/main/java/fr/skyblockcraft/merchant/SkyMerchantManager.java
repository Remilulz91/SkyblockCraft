package fr.skyblockcraft.merchant;

import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.config.SkyblockCraftConfig.MerchantTypeConfig;
import fr.skyblockcraft.economy.EconomyManager;
import fr.skyblockcraft.merchant.screen.MerchantScreenHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central registry for the mod's Sky Merchants. Handles:
 *   - Loading per-type {@link MerchantOfferSet} from the config
 *   - Tagging entities as merchants of a given {@link MerchantType}
 *   - Opening the {@link MerchantScreenHandler} GUI when a merchant is clicked
 *   - Buy and sell logic backed by {@link EconomyManager}
 */
public class SkyMerchantManager {

    /** Marker tag: any entity with this tag is a Sky Merchant. */
    public static final String SKY_MERCHANT_TAG = "skyblockcraft_sky_merchant";

    private static final Map<MerchantType, MerchantOfferSet> SETS = new EnumMap<>(MerchantType.class);

    public static void loadOffers() {
        SETS.clear();
        Map<String, MerchantTypeConfig> configMerchants = SkyblockCraftConfig.get().merchants;
        if (configMerchants == null) {
            SkyblockCraft.LOGGER.warn("[Merchant] merchants config is null, no offers loaded");
            return;
        }

        int totalOffers = 0;
        int totalBuybacks = 0;
        for (MerchantType type : MerchantType.values()) {
            MerchantOfferSet set = new MerchantOfferSet(type);
            MerchantTypeConfig cfg = configMerchants.get(type.getId());
            if (cfg != null) {
                set.loadOffers(cfg.offers);
                set.loadBuybacks(cfg.buybacks);
                totalOffers += set.getOffers().size();
                totalBuybacks += set.getBuybacks().size();
            }
            SETS.put(type, set);
        }
        SkyblockCraft.LOGGER.info("[Merchant] Loaded {} offers and {} buybacks across {} types",
                totalOffers, totalBuybacks, MerchantType.values().length);
    }

    public static MerchantOfferSet getOfferSet(MerchantType type) {
        return SETS.computeIfAbsent(type, MerchantOfferSet::new);
    }

    // ---- Marking an entity as a merchant of a given type ----

    /**
     * Marks the entity with the base merchant tag AND the type-specific tag,
     * and applies a friendly display name derived from the type's config.
     */
    public static void markAsMerchant(Entity entity, MerchantType type) {
        entity.addCommandTag(SKY_MERCHANT_TAG);
        // Remove any existing type tag before setting the new one
        for (MerchantType t : MerchantType.values()) {
            entity.removeCommandTag(t.getEntityTag());
        }
        entity.addCommandTag(type.getEntityTag());

        String displayName = getDisplayName(type);
        entity.setCustomName(Text.literal(displayName)
                .formatted(Formatting.YELLOW, Formatting.BOLD));
        entity.setCustomNameVisible(true);
    }

    public static boolean isMerchant(Entity entity) {
        return entity.getCommandTags().contains(SKY_MERCHANT_TAG);
    }

    /** Returns the merchant type of the entity, or null if not a merchant / no type. */
    public static MerchantType getMerchantType(Entity entity) {
        for (MerchantType t : MerchantType.values()) {
            if (entity.getCommandTags().contains(t.getEntityTag())) return t;
        }
        return null;
    }

    private static String getDisplayName(MerchantType type) {
        Map<String, MerchantTypeConfig> merchants = SkyblockCraftConfig.get().merchants;
        if (merchants != null) {
            MerchantTypeConfig cfg = merchants.get(type.getId());
            if (cfg != null && cfg.displayName != null && !cfg.displayName.isEmpty()) {
                return cfg.displayName;
            }
        }
        return type.getDefaultDisplayName();
    }

    // ---- Interaction: right-click a merchant opens the GUI ----

    public static void registerInteractionHandler() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            if (!isMerchant(entity)) return ActionResult.PASS;

            MerchantType type = getMerchantType(entity);
            if (type == null) type = MerchantType.GENERAL; // legacy merchants without type tag
            openShop(sp, type);
            return ActionResult.SUCCESS;
        });
    }

    /**
     * Registers a damage handler that cancels ALL damage to Sky Merchants.
     * This is stronger than Entity.setInvulnerable(true) which is bypassed by
     * creative-mode players and certain damage types. To remove a merchant, use
     * /skyblock remove merchant.
     */
    public static void registerDamageHandler() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (isMerchant(entity)) {
                return false; // cancel damage entirely
            }
            return true;
        });
    }

    public static void openShop(ServerPlayerEntity player, MerchantType type) {
        final MerchantType finalType = type;
        String displayName = getDisplayName(type);
        NamedScreenHandlerFactory factory = new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, playerEntity) -> new MerchantScreenHandler(syncId, playerInv, finalType),
                Text.literal(displayName)
        );
        player.openHandledScreen(factory);
    }

    // ---- Buy ----

    public static int buy(ServerPlayerEntity player, MerchantType type, String offerId) {
        SkyMerchantOffer offer = getOfferSet(type).getOffer(offerId);
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

        // Give the item (insertStack mutates the stack)
        ItemStack stack = offer.newStack();
        player.getInventory().insertStack(stack);
        if (!stack.isEmpty()) {
            player.dropItem(stack, false);
        }
        eco.tryRemove(player.getUuid(), offer.priceCoins());

        long newBalance = eco.getBalance(player.getUuid());
        player.sendMessage(
                Text.translatable("skyblockcraft.merchant.bought",
                        offer.count(), offer.item().getName().copy(), offer.priceCoins(), newBalance)
                        .formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    // ---- Sell ----

    /**
     * Attempts to sell {@code offer.count()} of the buyback's item from the
     * player's inventory to the merchant, crediting them the price in coins.
     */
    public static int sell(ServerPlayerEntity player, MerchantType type, String offerId) {
        SkyMerchantOffer buyback = getOfferSet(type).getBuyback(offerId);
        if (buyback == null) {
            player.sendMessage(Text.translatable("skyblockcraft.merchant.unknown_buyback", offerId).formatted(Formatting.RED), false);
            return -1;
        }
        MinecraftServer server = player.getServer();
        if (server == null) return 0;

        int required = buyback.count();
        int available = countItems(player.getInventory(), buyback.item().getDefaultStack());
        if (available < required) {
            player.sendMessage(
                    Text.translatable("skyblockcraft.merchant.not_enough_items",
                            required, buyback.item().getName().copy(), available).formatted(Formatting.RED),
                    false
            );
            return -2;
        }

        // Remove the items
        removeItems(player.getInventory(), buyback.item().getDefaultStack(), required);
        // Credit coins
        EconomyManager eco = EconomyManager.get(server);
        long newBalance = eco.add(player.getUuid(), buyback.priceCoins());

        player.sendMessage(
                Text.translatable("skyblockcraft.merchant.sold",
                        buyback.count(), buyback.item().getName().copy(), buyback.priceCoins(), newBalance)
                        .formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    /** Counts how many items of the same type as {@code sample} are in the inventory. */
    private static int countItems(Inventory inv, ItemStack sample) {
        int total = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && ItemStack.areItemsEqual(s, sample)) {
                total += s.getCount();
            }
        }
        return total;
    }

    /** Removes up to {@code amount} items of type {@code sample} from the inventory. */
    private static void removeItems(Inventory inv, ItemStack sample, int amount) {
        int remaining = amount;
        for (int i = 0; i < inv.size() && remaining > 0; i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && ItemStack.areItemsEqual(s, sample)) {
                int take = Math.min(s.getCount(), remaining);
                s.decrement(take);
                remaining -= take;
                if (s.isEmpty()) inv.setStack(i, ItemStack.EMPTY);
            }
        }
        if (inv instanceof PlayerInventory pi) pi.markDirty();
    }
}
