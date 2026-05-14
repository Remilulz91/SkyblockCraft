package fr.skyblockcraft;

import fr.skyblockcraft.commands.EconomyCommand;
import fr.skyblockcraft.commands.IslandCommand;
import fr.skyblockcraft.commands.SkyblockCommand;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import fr.skyblockcraft.economy.EconomyManager;
import fr.skyblockcraft.island.IslandManager;
import fr.skyblockcraft.merchant.SkyMerchantManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main entry point of SkyblockCraft.
 * Initializes all server-side and common-side systems.
 */
public class SkyblockCraft implements ModInitializer {

    public static final String MOD_ID = "skyblockcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger("SkyblockCraft");

    // === Build type detection (set by Gradle's processResources) ===
    private static final boolean IS_DEBUG_BUILD;
    static {
        boolean debug = false;
        try (InputStream is = SkyblockCraft.class.getResourceAsStream("/skyblockcraft.build.properties")) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                debug = "debug".equalsIgnoreCase(p.getProperty("build.type", "public").trim());
            }
        } catch (IOException ignored) { }
        IS_DEBUG_BUILD = debug;
    }

    /** Returns true if this JAR was built as the debug variant. */
    public static boolean isDebugBuild() {
        return IS_DEBUG_BUILD;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("==============================================");
        LOGGER.info("    SkyblockCraft - Starting up ({} build)",
                IS_DEBUG_BUILD ? "DEBUG" : "PUBLIC");
        LOGGER.info("==============================================");

        // 1. Load configuration
        SkyblockCraftConfig.load();
        LOGGER.info("[SkyblockCraft] Configuration loaded");

        // 2. Load merchant offers from config
        SkyMerchantManager.loadOffers();
        LOGGER.info("[SkyblockCraft] Merchant offers loaded ({} offers)",
                SkyMerchantManager.getOffers().size());

        // 3. Register commands (/island, /balance, /pay, /skyblock, /skyblock debug)
        CommandRegistrationCallback.EVENT.register(IslandCommand::register);
        CommandRegistrationCallback.EVENT.register(EconomyCommand::register);
        CommandRegistrationCallback.EVENT.register(SkyblockCommand::register);
        LOGGER.info("[SkyblockCraft] Commands registered");

        // 4. Register merchant interaction listener (right-click on Sky Merchants)
        SkyMerchantManager.registerInteractionHandler();
        LOGGER.info("[SkyblockCraft] Merchant interaction handler registered");

        // 5. Server lifecycle: ensure persistent state is loaded for each world
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Force-load economy and island state so they are available immediately
            EconomyManager.get(server);
            IslandManager.get(server);
            LOGGER.info("[SkyblockCraft] Server state initialized");
        });

        LOGGER.info("[SkyblockCraft] Mod loaded successfully!");
    }

    /**
     * Creates an Identifier in the mod's namespace.
     */
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
