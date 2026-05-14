package fr.skyblockcraft.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

/**
 * Mod Menu integration: adds a config button to the mod list.
 * Generates a full configuration GUI screen via Cloth Config.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            SkyblockCraftConfig cfg = SkyblockCraftConfig.get();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.skyblockcraft.title"))
                    .setSavingRunnable(SkyblockCraftConfig::save);

            ConfigEntryBuilder entry = builder.entryBuilder();

            // === Islands category ===
            ConfigCategory islands = builder.getOrCreateCategory(Text.translatable("config.skyblockcraft.category.islands"));

            islands.addEntry(entry.startIntField(Text.translatable("config.skyblockcraft.islandSpacing"), cfg.islandSpacing)
                    .setMin(128).setMax(8192).setDefaultValue(512)
                    .setTooltip(Text.translatable("config.skyblockcraft.islandSpacing.tooltip"))
                    .setSaveConsumer(v -> cfg.islandSpacing = v).build());

            islands.addEntry(entry.startIntField(Text.translatable("config.skyblockcraft.islandY"), cfg.islandY)
                    .setMin(-64).setMax(320).setDefaultValue(64)
                    .setSaveConsumer(v -> cfg.islandY = v).build());

            islands.addEntry(entry.startIntField(Text.translatable("config.skyblockcraft.islandsGridOriginX"), cfg.islandsGridOriginX)
                    .setDefaultValue(0)
                    .setSaveConsumer(v -> cfg.islandsGridOriginX = v).build());

            islands.addEntry(entry.startIntField(Text.translatable("config.skyblockcraft.islandsGridOriginZ"), cfg.islandsGridOriginZ)
                    .setDefaultValue(0)
                    .setSaveConsumer(v -> cfg.islandsGridOriginZ = v).build());

            islands.addEntry(entry.startBooleanToggle(Text.translatable("config.skyblockcraft.allowIslandVisits"), cfg.allowIslandVisits)
                    .setDefaultValue(true)
                    .setSaveConsumer(v -> cfg.allowIslandVisits = v).build());

            // === Economy category ===
            ConfigCategory eco = builder.getOrCreateCategory(Text.translatable("config.skyblockcraft.category.economy"));

            eco.addEntry(entry.startLongField(Text.translatable("config.skyblockcraft.startingCoins"), cfg.startingCoins)
                    .setMin(0L).setMax(1_000_000L).setDefaultValue(100L)
                    .setSaveConsumer(v -> cfg.startingCoins = v).build());

            eco.addEntry(entry.startLongField(Text.translatable("config.skyblockcraft.maxCoins"), cfg.maxCoins)
                    .setMin(0L).setDefaultValue(0L)
                    .setTooltip(Text.translatable("config.skyblockcraft.maxCoins.tooltip"))
                    .setSaveConsumer(v -> cfg.maxCoins = v).build());

            eco.addEntry(entry.startBooleanToggle(Text.translatable("config.skyblockcraft.allowPlayerPay"), cfg.allowPlayerPay)
                    .setDefaultValue(true)
                    .setSaveConsumer(v -> cfg.allowPlayerPay = v).build());

            eco.addEntry(entry.startLongField(Text.translatable("config.skyblockcraft.minPayAmount"), cfg.minPayAmount)
                    .setMin(1L).setDefaultValue(1L)
                    .setSaveConsumer(v -> cfg.minPayAmount = v).build());

            // === Merchant category ===
            ConfigCategory merchant = builder.getOrCreateCategory(Text.translatable("config.skyblockcraft.category.merchant"));

            merchant.addEntry(entry.startStrField(Text.translatable("config.skyblockcraft.merchantDisplayName"), cfg.merchantDisplayName)
                    .setDefaultValue("Sky Merchant")
                    .setSaveConsumer(v -> cfg.merchantDisplayName = v).build());

            // === UI category ===
            ConfigCategory ui = builder.getOrCreateCategory(Text.translatable("config.skyblockcraft.category.ui"));

            ui.addEntry(entry.startBooleanToggle(Text.translatable("config.skyblockcraft.showBalanceOnJoin"), cfg.showBalanceOnJoin)
                    .setDefaultValue(true)
                    .setSaveConsumer(v -> cfg.showBalanceOnJoin = v).build());

            // === Debug category — only visible in DEBUG build ===
            if (SkyblockCraft.isDebugBuild()) {
                ConfigCategory debug = builder.getOrCreateCategory(Text.translatable("config.skyblockcraft.category.debug"));

                debug.addEntry(entry.startBooleanToggle(Text.translatable("config.skyblockcraft.enableDebugCommands"), cfg.enableDebugCommands)
                        .setDefaultValue(true)
                        .setTooltip(Text.translatable("config.skyblockcraft.enableDebugCommands.tooltip"))
                        .setSaveConsumer(v -> cfg.enableDebugCommands = v).build());

                debug.addEntry(entry.startBooleanToggle(Text.translatable("config.skyblockcraft.debugAllowIslandOverride"), cfg.debugAllowIslandOverride)
                        .setDefaultValue(true)
                        .setTooltip(Text.translatable("config.skyblockcraft.debugAllowIslandOverride.tooltip"))
                        .setSaveConsumer(v -> cfg.debugAllowIslandOverride = v).build());
            }

            return builder.build();
        };
    }
}
