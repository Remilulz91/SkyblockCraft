package fr.skyblockcraft.merchant.screen;

import fr.skyblockcraft.SkyblockCraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

/**
 * Central registry for the mod's custom ScreenHandlers.
 * Called from {@link SkyblockCraft#onInitialize()} so the registration
 * happens once at startup on both server and client.
 */
public class ModScreenHandlers {

    public static final ScreenHandlerType<MerchantScreenHandler> MERCHANT = Registry.register(
            Registries.SCREEN_HANDLER,
            SkyblockCraft.id("merchant"),
            new ScreenHandlerType<>(MerchantScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );

    /** Called from the mod's onInitialize to force class-loading of the static fields. */
    public static void register() {
        SkyblockCraft.LOGGER.info("[ScreenHandlers] Registered {} handlers", 1);
    }
}
