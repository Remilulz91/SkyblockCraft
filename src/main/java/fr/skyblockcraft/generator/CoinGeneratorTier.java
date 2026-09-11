package fr.skyblockcraft.generator;

/**
 * The tiers of coin generators. Each tier defines how many coins it produces
 * per production cycle and how long that cycle is (in ticks, 20 ticks = 1 second).
 *
 * A short cycle with a small payout feels more "alive" than a long cycle with
 * a big payout, even if the coins/minute is the same.
 */
public enum CoinGeneratorTier {

    /** 1 coin every 60 seconds = 1 coin/min. */
    BASIC("basic", 1, 20 * 60, "Basic Coin Generator"),
    /** 1 coin every 12 seconds = 5 coins/min. */
    ADVANCED("advanced", 1, 20 * 12, "Advanced Coin Generator"),
    /** 3 coins every 12 seconds = 15 coins/min. */
    ELITE("elite", 3, 20 * 12, "Elite Coin Generator");

    private final String id;
    private final int coinsPerCycle;
    private final int ticksPerCycle;
    private final String defaultDisplayName;

    CoinGeneratorTier(String id, int coinsPerCycle, int ticksPerCycle, String defaultDisplayName) {
        this.id = id;
        this.coinsPerCycle = coinsPerCycle;
        this.ticksPerCycle = ticksPerCycle;
        this.defaultDisplayName = defaultDisplayName;
    }

    public String getId() { return id; }
    public int getCoinsPerCycle() { return coinsPerCycle; }
    public int getTicksPerCycle() { return ticksPerCycle; }
    public String getDefaultDisplayName() { return defaultDisplayName; }
    public String getTranslationKey() { return "block.skyblockcraft.coin_generator_" + id; }

    /** Approximate coins per real-time minute (for UI/tooltip). */
    public int coinsPerMinute() {
        return coinsPerCycle * 20 * 60 / ticksPerCycle;
    }

    public static CoinGeneratorTier fromId(String id) {
        for (CoinGeneratorTier t : values()) if (t.id.equals(id)) return t;
        return null;
    }
}
