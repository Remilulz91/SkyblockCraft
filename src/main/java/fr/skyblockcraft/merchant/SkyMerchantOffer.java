package fr.skyblockcraft.merchant;

import fr.skyblockcraft.SkyblockCraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * A single Sky Merchant offer: a quantity of an item for a coin price.
 *
 * Defined in config as "ITEM_ID:COUNT:PRICE", e.g. "minecraft:diamond:1:150".
 */
public record SkyMerchantOffer(String id, Item item, int count, long priceCoins) {

    /** Parses a config string. Returns null on parse error (logged). */
    public static SkyMerchantOffer parse(String offerId, String spec) {
        if (spec == null) return null;
        String[] parts = spec.split(":");
        if (parts.length != 4) {
            SkyblockCraft.LOGGER.warn("[Merchant] Invalid offer spec '{}' for id '{}' (expected NAMESPACE:PATH:COUNT:PRICE)", spec, offerId);
            return null;
        }
        String itemIdStr = parts[0] + ":" + parts[1];
        Identifier itemId;
        try {
            itemId = Identifier.of(itemIdStr);
        } catch (Exception e) {
            SkyblockCraft.LOGGER.warn("[Merchant] Invalid item id '{}' in offer '{}'", itemIdStr, offerId);
            return null;
        }
        Item item = Registries.ITEM.get(itemId);
        if (item == null) {
            SkyblockCraft.LOGGER.warn("[Merchant] Unknown item '{}' in offer '{}'", itemIdStr, offerId);
            return null;
        }
        int count;
        long price;
        try {
            count = Integer.parseInt(parts[2]);
            price = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            SkyblockCraft.LOGGER.warn("[Merchant] Invalid count/price in offer '{}': {}", offerId, spec);
            return null;
        }
        if (count <= 0 || price < 0) {
            SkyblockCraft.LOGGER.warn("[Merchant] Negative or zero count/price in offer '{}'", offerId);
            return null;
        }
        return new SkyMerchantOffer(offerId, item, count, price);
    }

    public ItemStack newStack() {
        return new ItemStack(item, count);
    }
}
