package fr.skyblockcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.skyblockcraft.SkyblockCraft;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central configuration for SkyblockCraft.
 * Saved to config/skyblockcraft.json.
 */
public class SkyblockCraftConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("skyblockcraft.json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static SkyblockCraftConfig INSTANCE = new SkyblockCraftConfig();

    // === Island settings ===

    /** Spacing (in blocks) between two player islands on the grid. */
    public int islandSpacing = 512;

    /** Y coordinate where the starter island platform is generated. */
    public int islandY = 64;

    /** Origin X coordinate of the islands grid. */
    public int islandsGridOriginX = 0;

    /** Origin Z coordinate of the islands grid. */
    public int islandsGridOriginZ = 0;

    /** Name of the dimension where islands are generated (overworld by default). */
    public String islandDimension = "minecraft:overworld";

    /** Allow players to visit other players' islands with /island visit. */
    public boolean allowIslandVisits = true;

    /**
     * Half-side of the square protection zone around each island's grid center.
     * A radius of 24 gives a 49×49 protected zone (from -24 to +24 in x/z).
     * Blocks inside this zone can only be modified by the owner or trusted players.
     */
    public int protectionRadius = 24;

    /** Number of net placed blocks required to gain one island level. */
    public int blocksPerLevel = 100;

    /** If true, OPs (permission level 2+) can build/break on any island. */
    public boolean opBypassIslandProtection = true;

    // === Economy settings ===

    /** Starting coin balance for a new player. */
    public long startingCoins = 100;

    /** Maximum coins a player can hold (0 = unlimited). */
    public long maxCoins = 0;

    /** Allow players to send coins to others with /pay. */
    public boolean allowPlayerPay = true;

    /** Minimum amount per /pay transaction. */
    public long minPayAmount = 1;

    // === Merchant settings ===

    /**
     * DEPRECATED (v0.1 legacy): flat offer list for the generic merchant.
     * Kept for backward compatibility. On config load, if this is populated but
     * merchants.general.offers is empty, entries are migrated automatically.
     */
    @Deprecated
    public Map<String, String> merchantOffers = null;

    /**
     * Per-type merchant configuration. Key is the type id (farmer/miner/adventurer/general).
     * Each type has a display name, its own offers (what it sells) and its own
     * buybacks (what it buys back from the player).
     */
    public Map<String, MerchantTypeConfig> merchants = defaultMerchants();

    /** Nested config object for one merchant type. */
    public static class MerchantTypeConfig {
        public String displayName = "";
        public Map<String, String> offers = new LinkedHashMap<>();
        public Map<String, String> buybacks = new LinkedHashMap<>();
    }

    private static Map<String, MerchantTypeConfig> defaultMerchants() {
        Map<String, MerchantTypeConfig> map = new LinkedHashMap<>();

        // Farmer — seeds, crops, farm produce
        MerchantTypeConfig farmer = new MerchantTypeConfig();
        farmer.displayName = "Sky Farmer";
        farmer.offers.put("wheat_seeds", "minecraft:wheat_seeds:8:5");
        farmer.offers.put("melon_seeds", "minecraft:melon_seeds:4:10");
        farmer.offers.put("pumpkin_seeds", "minecraft:pumpkin_seeds:4:10");
        farmer.offers.put("beetroot_seeds", "minecraft:beetroot_seeds:8:10");
        farmer.offers.put("carrot", "minecraft:carrot:8:12");
        farmer.offers.put("potato", "minecraft:potato:8:12");
        farmer.offers.put("bone_meal", "minecraft:bone_meal:8:20");
        farmer.offers.put("cocoa_beans", "minecraft:cocoa_beans:4:25");
        farmer.offers.put("hay_block", "minecraft:hay_block:4:60");
        farmer.buybacks.put("wheat", "minecraft:wheat:16:8");
        farmer.buybacks.put("carrot", "minecraft:carrot:16:8");
        farmer.buybacks.put("potato", "minecraft:potato:16:8");
        farmer.buybacks.put("melon_slice", "minecraft:melon_slice:16:10");
        farmer.buybacks.put("pumpkin", "minecraft:pumpkin:4:15");
        farmer.buybacks.put("beetroot", "minecraft:beetroot:16:10");
        farmer.buybacks.put("apple", "minecraft:apple:4:20");
        map.put("farmer", farmer);

        // Miner — stone, ores, ingots
        MerchantTypeConfig miner = new MerchantTypeConfig();
        miner.displayName = "Sky Miner";
        miner.offers.put("cobblestone_stack", "minecraft:cobblestone:64:10");
        miner.offers.put("stone_stack", "minecraft:stone:64:12");
        miner.offers.put("coal", "minecraft:coal:16:30");
        miner.offers.put("iron_ingot", "minecraft:iron_ingot:1:30");
        miner.offers.put("gold_ingot", "minecraft:gold_ingot:1:60");
        miner.offers.put("redstone", "minecraft:redstone:8:20");
        miner.offers.put("lapis_lazuli", "minecraft:lapis_lazuli:8:25");
        miner.offers.put("diamond", "minecraft:diamond:1:150");
        miner.offers.put("emerald", "minecraft:emerald:1:200");
        miner.offers.put("obsidian", "minecraft:obsidian:1:40");
        miner.buybacks.put("cobblestone", "minecraft:cobblestone:64:5");
        miner.buybacks.put("coal", "minecraft:coal:16:15");
        miner.buybacks.put("iron_ingot", "minecraft:iron_ingot:1:15");
        miner.buybacks.put("gold_ingot", "minecraft:gold_ingot:1:30");
        miner.buybacks.put("diamond", "minecraft:diamond:1:75");
        miner.buybacks.put("emerald", "minecraft:emerald:1:100");
        map.put("miner", miner);

        // Adventurer — combat, exploration, mob loot
        MerchantTypeConfig adventurer = new MerchantTypeConfig();
        adventurer.displayName = "Sky Adventurer";
        adventurer.offers.put("torch", "minecraft:torch:16:15");
        adventurer.offers.put("bread", "minecraft:bread:8:20");
        adventurer.offers.put("water_bucket", "minecraft:water_bucket:1:80");
        adventurer.offers.put("lava_bucket", "minecraft:lava_bucket:1:80");
        adventurer.offers.put("ender_pearl", "minecraft:ender_pearl:1:100");
        adventurer.offers.put("blaze_rod", "minecraft:blaze_rod:1:120");
        adventurer.offers.put("string", "minecraft:string:8:20");
        adventurer.offers.put("gunpowder", "minecraft:gunpowder:4:25");
        adventurer.buybacks.put("rotten_flesh", "minecraft:rotten_flesh:16:5");
        adventurer.buybacks.put("bone", "minecraft:bone:8:10");
        adventurer.buybacks.put("string", "minecraft:string:8:12");
        adventurer.buybacks.put("gunpowder", "minecraft:gunpowder:4:15");
        adventurer.buybacks.put("blaze_rod", "minecraft:blaze_rod:1:60");
        adventurer.buybacks.put("ender_pearl", "minecraft:ender_pearl:1:50");
        adventurer.buybacks.put("spider_eye", "minecraft:spider_eye:8:12");
        map.put("adventurer", adventurer);

        // General — everything else + basic building blocks
        MerchantTypeConfig general = new MerchantTypeConfig();
        general.displayName = "Sky Merchant";
        general.offers.put("dirt_stack", "minecraft:dirt:64:5");
        general.offers.put("oak_log_stack", "minecraft:oak_log:16:25");
        general.offers.put("sand_stack", "minecraft:sand:32:15");
        general.offers.put("bookshelf", "minecraft:bookshelf:1:30");
        general.offers.put("book", "minecraft:book:4:20");
        general.offers.put("chest", "minecraft:chest:1:15");
        general.buybacks.put("oak_log", "minecraft:oak_log:16:12");
        general.buybacks.put("dirt", "minecraft:dirt:64:2");
        general.buybacks.put("sand", "minecraft:sand:32:8");
        map.put("general", general);

        return map;
    }

    // === UI ===

    /** Display a balance reminder in chat when a player joins. */
    public boolean showBalanceOnJoin = true;

    // === DEBUG (disabled by default in public builds, enabled in debug builds) ===

    public boolean enableDebugCommands = SkyblockCraft.isDebugBuild();
    public boolean debugAllowIslandOverride = SkyblockCraft.isDebugBuild();

    // === Methods ===

    public static SkyblockCraftConfig get() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                SkyblockCraftConfig loaded = GSON.fromJson(json, SkyblockCraftConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    // Guard against nulls after deserialization
                    if (INSTANCE.merchants == null) {
                        INSTANCE.merchants = defaultMerchants();
                    }
                    // Migrate legacy flat merchantOffers into merchants.general.offers
                    migrateLegacyOffers(INSTANCE);
                }
                SkyblockCraft.LOGGER.info("[Config] Configuration loaded from {}", CONFIG_PATH);
            } else {
                save();
                SkyblockCraft.LOGGER.info("[Config] Default configuration created");
            }
        } catch (IOException e) {
            SkyblockCraft.LOGGER.error("[Config] Loading error: {}", e.getMessage());
        }

        // SECURITY: PUBLIC builds force debug flags to false regardless of config file.
        if (!SkyblockCraft.isDebugBuild()) {
            boolean wasModified = false;
            if (INSTANCE.enableDebugCommands) {
                INSTANCE.enableDebugCommands = false;
                wasModified = true;
            }
            if (INSTANCE.debugAllowIslandOverride) {
                INSTANCE.debugAllowIslandOverride = false;
                wasModified = true;
            }
            if (wasModified) {
                SkyblockCraft.LOGGER.warn("[Config] ⚠ Debug flags found in config file but this is a PUBLIC build —");
                SkyblockCraft.LOGGER.warn("[Config] ⚠ they are IGNORED. To use debug features, install the DEBUG build.");
            }
        }
    }

    /**
     * Migrates the v0.1 legacy `merchantOffers` (flat map) into the new
     * `merchants.general.offers` structure. Runs on load, only if the legacy map
     * is populated and the general merchant has no offers yet.
     */
    private static void migrateLegacyOffers(SkyblockCraftConfig cfg) {
        if (cfg.merchantOffers == null || cfg.merchantOffers.isEmpty()) return;
        MerchantTypeConfig general = cfg.merchants.computeIfAbsent("general", k -> {
            MerchantTypeConfig m = new MerchantTypeConfig();
            m.displayName = "Sky Merchant";
            return m;
        });
        if (general.offers != null && !general.offers.isEmpty()) {
            SkyblockCraft.LOGGER.info("[Config] Legacy merchantOffers found but merchants.general.offers already populated — skipping migration");
            return;
        }
        general.offers = new LinkedHashMap<>(cfg.merchantOffers);
        SkyblockCraft.LOGGER.info("[Config] Migrated {} legacy merchantOffers into merchants.general.offers", cfg.merchantOffers.size());
        cfg.merchantOffers = null; // wipe legacy field so it won't be re-serialized
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            SkyblockCraft.LOGGER.error("[Config] Save error: {}", e.getMessage());
        }
    }
}
