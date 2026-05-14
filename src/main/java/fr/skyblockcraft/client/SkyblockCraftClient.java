package fr.skyblockcraft.client;

import fr.skyblockcraft.SkyblockCraft;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side entry point.
 * For the v0.1 prototype, client-side does not need much: the merchant uses
 * vanilla chat + clickable text components, the economy uses chat messages,
 * and the island generation happens server-side.
 *
 * This class exists so we can register a Mod Menu config screen and grow client
 * features later (custom HUD, custom GUI, particle effects, etc.).
 */
public class SkyblockCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SkyblockCraft.LOGGER.info("[SkyblockCraft] Client initializing...");
        // Future: register HUD, key bindings, client-only events
        SkyblockCraft.LOGGER.info("[SkyblockCraft] Client initialized.");
    }
}
