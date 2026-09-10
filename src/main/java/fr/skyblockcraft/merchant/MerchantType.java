package fr.skyblockcraft.merchant;

/**
 * The categories of Sky Merchants.
 *
 * Each type has its own {@link MerchantOfferSet} (buys + sells) configured in
 * {@code skyblockcraft.json}. An entity is marked as a merchant of a given type
 * via a command tag {@code skyblockcraft_type_<id>} in addition to the base
 * {@code skyblockcraft_sky_merchant} marker tag.
 */
public enum MerchantType {
    FARMER("farmer", "Sky Farmer"),
    MINER("miner", "Sky Miner"),
    ADVENTURER("adventurer", "Sky Adventurer"),
    GENERAL("general", "Sky Merchant");

    private final String id;
    private final String defaultDisplayName;

    MerchantType(String id, String defaultDisplayName) {
        this.id = id;
        this.defaultDisplayName = defaultDisplayName;
    }

    public String getId() {
        return id;
    }

    public String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    /** Localization key for this type's name (e.g. "skyblockcraft.merchant.type.farmer"). */
    public String getTranslationKey() {
        return "skyblockcraft.merchant.type." + id;
    }

    /** Command tag used to identify a merchant entity's type. */
    public String getEntityTag() {
        return "skyblockcraft_type_" + id;
    }

    public static MerchantType fromId(String id) {
        if (id == null) return null;
        String normalized = id.trim().toLowerCase();
        for (MerchantType t : values()) {
            if (t.id.equals(normalized)) return t;
        }
        return null;
    }
}
