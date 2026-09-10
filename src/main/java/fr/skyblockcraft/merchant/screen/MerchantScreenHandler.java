package fr.skyblockcraft.merchant.screen;

import fr.skyblockcraft.merchant.MerchantOfferSet;
import fr.skyblockcraft.merchant.MerchantType;
import fr.skyblockcraft.merchant.SkyMerchantManager;
import fr.skyblockcraft.merchant.SkyMerchantOffer;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * A 6-row chest-like GUI for the Sky Merchant.
 *
 * Layout:
 *   Row 0 (slots 0-8):  BUY tab (slot 0), fillers (1-7), SELL tab (slot 8)
 *   Rows 1-5 (9-53):    up to 45 offer/buyback slots depending on the active tab
 *
 * All shop slots are non-interactive (canInsert/canTake return false). All
 * "interaction" happens through onSlotClick, which routes to buy/sell/tab logic.
 * The player inventory (54-89) works normally so the player can see what they own.
 */
public class MerchantScreenHandler extends ScreenHandler {

    public static final int SHOP_ROWS = 6;
    public static final int SHOP_COLS = 9;
    public static final int SHOP_SLOTS = SHOP_ROWS * SHOP_COLS;   // 54

    public static final int BUY_TAB_SLOT = 0;
    public static final int SELL_TAB_SLOT = 8;
    public static final int FIRST_OFFER_SLOT = 9;

    private final SimpleInventory shopInventory;
    private MerchantType merchantType;
    private Mode mode = Mode.BUY;
    /** Maps a slot index (in the shop grid) to the offer id it currently represents. */
    private final String[] slotOfferIds = new String[SHOP_SLOTS];

    public enum Mode { BUY, SELL }

    /**
     * Client-side constructor used by {@link ScreenHandlerType} — the client
     * doesn't know which merchant type this is, but that's OK: the server syncs
     * slot contents automatically. Defaults to GENERAL for safety.
     */
    public MerchantScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, MerchantType.GENERAL);
    }

    /** Server-side constructor with the actual merchant type. */
    public MerchantScreenHandler(int syncId, PlayerInventory playerInv, MerchantType type) {
        super(ModScreenHandlers.MERCHANT, syncId);
        this.merchantType = type;
        this.shopInventory = new SimpleInventory(SHOP_SLOTS);

        // Shop slots (0..53) — non-interactive display slots
        for (int row = 0; row < SHOP_ROWS; row++) {
            for (int col = 0; col < SHOP_COLS; col++) {
                int i = col + row * SHOP_COLS;
                addSlot(new Slot(shopInventory, i, 8 + col * 18, 18 + row * 18) {
                    @Override public boolean canInsert(ItemStack stack) { return false; }
                    @Override public boolean canTakeItems(PlayerEntity player) { return false; }
                });
            }
        }

        // Player inventory (27 slots) at y=140
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        // Player hotbar (9 slots) at y=198
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }

        // Populate initial display on server side only
        if (playerInv.player instanceof ServerPlayerEntity) {
            refreshDisplay();
        }
    }

    public MerchantType getMerchantType() { return merchantType; }
    public Mode getMode() { return mode; }

    /** Rebuilds the shop slots according to the current mode + merchant type. */
    public void refreshDisplay() {
        // Clear all slots
        for (int i = 0; i < SHOP_SLOTS; i++) {
            shopInventory.setStack(i, ItemStack.EMPTY);
            slotOfferIds[i] = null;
        }

        // Top row: tabs
        shopInventory.setStack(BUY_TAB_SLOT, makeTabItem(true));
        shopInventory.setStack(SELL_TAB_SLOT, makeTabItem(false));
        // Fillers between tabs
        for (int i = 1; i < 8; i++) {
            shopInventory.setStack(i, makeFiller());
        }

        // Offer slots (rows 1-5, 45 max)
        MerchantOfferSet set = SkyMerchantManager.getOfferSet(merchantType);
        var map = mode == Mode.BUY ? set.getOffers() : set.getBuybacks();

        int slot = FIRST_OFFER_SLOT;
        for (SkyMerchantOffer offer : map.values()) {
            if (slot >= SHOP_SLOTS) break;
            shopInventory.setStack(slot, makeOfferDisplay(offer, mode));
            slotOfferIds[slot] = offer.id();
            slot++;
        }

        sendContentUpdates();
    }

    // ---- Slot click handling ----

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity clicker) {
        // Shop area click — never move items, route to logic
        if (slotIndex >= 0 && slotIndex < SHOP_SLOTS) {
            if (clicker instanceof ServerPlayerEntity sp) {
                handleShopClick(slotIndex, sp);
            }
            return;
        }
        // Player inventory click — normal behavior EXCEPT shift-click which we
        // may intercept for a quick sell.
        super.onSlotClick(slotIndex, button, actionType, clicker);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        // Prevent shift-clicking from shop area (nothing to move) and from
        // player inventory into shop area (would just fail silently).
        // For SELL mode a future improvement: shift-click sells the item.
        return ItemStack.EMPTY;
    }

    private void handleShopClick(int slotIndex, ServerPlayerEntity player) {
        if (slotIndex == BUY_TAB_SLOT) {
            if (mode != Mode.BUY) { mode = Mode.BUY; refreshDisplay(); }
            return;
        }
        if (slotIndex == SELL_TAB_SLOT) {
            if (mode != Mode.SELL) { mode = Mode.SELL; refreshDisplay(); }
            return;
        }
        if (slotIndex < FIRST_OFFER_SLOT) return; // filler

        String offerId = slotOfferIds[slotIndex];
        if (offerId == null) return; // empty slot

        if (mode == Mode.BUY) {
            SkyMerchantManager.buy(player, merchantType, offerId);
        } else {
            SkyMerchantManager.sell(player, merchantType, offerId);
        }
        // Refresh to reflect any state changes (e.g. balance in tooltip)
        refreshDisplay();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    // ---- Display helpers ----

    private ItemStack makeTabItem(boolean forBuyTab) {
        ItemStack stack;
        Text name;
        List<Text> lore = new ArrayList<>();
        boolean active = (forBuyTab && mode == Mode.BUY) || (!forBuyTab && mode == Mode.SELL);
        if (forBuyTab) {
            stack = new ItemStack(active ? Items.EMERALD_BLOCK : Items.EMERALD);
            name = Text.translatable("skyblockcraft.merchant.tab.buy")
                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true).withItalic(false));
            lore.add(Text.translatable(active ? "skyblockcraft.merchant.tab.active"
                    : "skyblockcraft.merchant.tab.switch").formatted(Formatting.GRAY).styled(s -> s.withItalic(false)));
        } else {
            stack = new ItemStack(active ? Items.GOLD_BLOCK : Items.GOLD_INGOT);
            name = Text.translatable("skyblockcraft.merchant.tab.sell")
                    .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true).withItalic(false));
            lore.add(Text.translatable(active ? "skyblockcraft.merchant.tab.active"
                    : "skyblockcraft.merchant.tab.switch").formatted(Formatting.GRAY).styled(s -> s.withItalic(false)));
        }
        stack.set(DataComponentTypes.CUSTOM_NAME, name);
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return stack;
    }

    private ItemStack makeFiller() {
        ItemStack stack = new ItemStack(Blocks.GRAY_STAINED_GLASS_PANE);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(" "));
        return stack;
    }

    private ItemStack makeOfferDisplay(SkyMerchantOffer offer, Mode currentMode) {
        ItemStack stack = new ItemStack(offer.item(), offer.count());
        // Custom name = original item name, no italic
        Text name = offer.item().getName().copy()
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false));
        stack.set(DataComponentTypes.CUSTOM_NAME, name);

        List<Text> lore = new ArrayList<>();
        String priceKey = currentMode == Mode.BUY
                ? "skyblockcraft.merchant.lore.price_buy"
                : "skyblockcraft.merchant.lore.price_sell";
        lore.add(Text.translatable(priceKey, offer.priceCoins())
                .formatted(Formatting.GOLD).styled(s -> s.withItalic(false)));
        lore.add(Text.literal(""));
        String actionKey = currentMode == Mode.BUY
                ? "skyblockcraft.merchant.lore.click_buy"
                : "skyblockcraft.merchant.lore.click_sell";
        lore.add(Text.translatable(actionKey)
                .formatted(Formatting.YELLOW).styled(s -> s.withItalic(false)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return stack;
    }
}
