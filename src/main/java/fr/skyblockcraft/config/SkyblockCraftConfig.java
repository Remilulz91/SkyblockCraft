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
 *
 * Values can be modified via:
 * - The in-game config screen (Mod Menu + Cloth Config)
 * - The command /skyblock config (OP)
 * - Directly in the config/skyblockcraft.json file
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

    /** Display name shown above a Sky Merchant villager. */
    public String merchantDisplayName = "Sky Merchant";

    /**
     * Merchant offers. Key is the offer ID, value is "ITEM:COUNT:PRICE_IN_COINS".
     * Example: "diamond" -> "minecraft:diamond:1:50"
     *
     * On config load, validated and parsed into {@link fr.skyblockcraft.merchant.SkyMerchantOffer}.
     */
    public Map<String, String> merchantOffers = defaultOffers();

    private static Map<String, String> defaultOffers() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("dirt_stack", "minecraft:dirt:64:5");
        map.put("oak_log_stack", "minecraft:oak_log:16:25");
        map.put("cobblestone_stack", "minecraft:cobblestone:64:10");
        map.put("iron_ingot", "minecraft:iron_ingot:1:30");
        map.put("gold_ingot", "minecraft:gold_ingot:1:60");
        map.put("diamond", "minecraft:diamond:1:150");
        map.put("emerald", "minecraft:emerald:1:200");
        map.put("water_bucket", "minecraft:water_bucket:1:80");
        map.put("lava_bucket", "minecraft:lava_bucket:1:80");
        return map;
    }

    // === UI ===

    /** Display a balance reminder in chat when a player joins. */
    public boolean showBalanceOnJoin = true;

    // === DEBUG (disabled by default in public builds, enabled in debug builds) ===

    /**
     * [DEBUG] Enables /skyblock debug ... commands (givecoins, setbalance, teleport, etc.).
     * DISABLE for production servers — otherwise any OP could cheat with debug commands.
     * Default depends on build type (public = false, debug = true).
     */
    public boolean enableDebugCommands = SkyblockCraft.isDebugBuild();

    /**
     * [DEBUG] If true, regenerating an existing island via /island create is allowed
     * without confirmation. Useful for testing the generator.
     */
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
                    // Guard against null map after deserialization
                    if (INSTANCE.merchantOffers == null) {
                        INSTANCE.merchantOffers = defaultOffers();
                    }
                }
                SkyblockCraft.LOGGER.info("[Config] Configuration loaded from {}", CONFIG_PATH);
            } else {
                save();
                SkyblockCraft.LOGGER.info("[Config] Default configuration created");
            }
        } catch (IOException e) {
            SkyblockCraft.LOGGER.error("[Config] Loading error: {}", e.getMessage());
        }

        // SECURITY: in PUBLIC builds, debug flags are FORCED to false at runtime
        // regardless of what the config file contains. This prevents anyone from
        // bypassing the public/debug build distinction by editing the JSON.
        // To use debug features, the DEBUG build must be installed instead.
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

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            SkyblockCraft.LOGGER.error("[Config] Save error: {}", e.getMessage());
        }
    }
}
