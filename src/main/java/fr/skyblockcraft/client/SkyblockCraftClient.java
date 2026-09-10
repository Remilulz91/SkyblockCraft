package fr.skyblockcraft.client;

import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.client.screen.MerchantScreen;
import fr.skyblockcraft.merchant.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * Client-side entry point.
 * Registers the client-side rendering for the mod's custom ScreenHandlers.
 */
public class SkyblockCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SkyblockCraft.LOGGER.info("[SkyblockCraft] Client initializing...");

        // Register the client-side Screen for the merchant ScreenHandler.
        HandledScreens.register(ModScreenHandlers.MERCHANT, MerchantScreen::new);

        SkyblockCraft.LOGGER.info("[SkyblockCraft] Client initialized.");
    }
}
