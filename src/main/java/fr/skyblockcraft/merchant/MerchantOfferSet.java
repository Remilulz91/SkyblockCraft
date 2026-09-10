package fr.skyblockcraft.merchant;

import fr.skyblockcraft.SkyblockCraft;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The set of offers (items the merchant sells to the player) and buybacks
 * (items the merchant buys from the player) for one {@link MerchantType}.
 *
 * Offers/buybacks are keyed by their offer id (a short string), so they can be
 * referenced individually (e.g. by /skyblock reload feedback, debug commands).
 */
public class MerchantOfferSet {

    private final MerchantType type;
    private final Map<String, SkyMerchantOffer> offers = new LinkedHashMap<>();
    private final Map<String, SkyMerchantOffer> buybacks = new LinkedHashMap<>();

    public MerchantOfferSet(MerchantType type) {
        this.type = type;
    }

    public MerchantType getType() {
        return type;
    }

    public Map<String, SkyMerchantOffer> getOffers() {
        return Collections.unmodifiableMap(offers);
    }

    public Map<String, SkyMerchantOffer> getBuybacks() {
        return Collections.unmodifiableMap(buybacks);
    }

    public SkyMerchantOffer getOffer(String id) {
        return offers.get(id);
    }

    public SkyMerchantOffer getBuyback(String id) {
        return buybacks.get(id);
    }

    /**
     * Loads offers from a config map (spec strings). Existing entries are cleared.
     * Invalid entries are logged and skipped.
     */
    public void loadOffers(Map<String, String> configOffers) {
        offers.clear();
        if (configOffers == null) return;
        for (Map.Entry<String, String> e : configOffers.entrySet()) {
            SkyMerchantOffer offer = SkyMerchantOffer.parse(e.getKey(), e.getValue());
            if (offer != null) {
                offers.put(offer.id(), offer);
            }
        }
    }

    public void loadBuybacks(Map<String, String> configBuybacks) {
        buybacks.clear();
        if (configBuybacks == null) return;
        for (Map.Entry<String, String> e : configBuybacks.entrySet()) {
            SkyMerchantOffer offer = SkyMerchantOffer.parse(e.getKey(), e.getValue());
            if (offer != null) {
                buybacks.put(offer.id(), offer);
            }
        }
        SkyblockCraft.LOGGER.debug("[Merchant] {} buybacks loaded for type {}", buybacks.size(), type.getId());
    }

    public boolean isEmpty() {
        return offers.isEmpty() && buybacks.isEmpty();
    }
}
