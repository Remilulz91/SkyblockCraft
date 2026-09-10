# Changelog

All notable changes to SkyblockCraft will be documented in this file.

## [0.2.0-alpha.1] — 2026-09-10

Merchant overhaul: real GUI, sell system, and multiple merchant types.

### Added
- **Real GUI for the Sky Merchant** (`MerchantScreenHandler` + `MerchantScreen`): a 6-row chest-style window with a **BUY** tab (green, top-left) and a **SELL** tab (gold, top-right). Each offer slot shows the item, price in lore, and clicks to buy/sell.
- **Sell system**: merchants now have buybacks in addition to offers. In SELL mode, clicking an item icon removes the required stack from your inventory and credits the coins. Message and localization keys for `sold` / `not_enough_items` / `unknown_buyback`.
- **Four merchant types**: `farmer` (seeds, crops, farm produce), `miner` (stone, ores, ingots), `adventurer` (torches, food, mob loot), `general` (everything else + building blocks). Each type has its own offers + buybacks in the config.
- **Type-specific spawn command**: `/skyblock spawn merchant <type>` (with tab completion of the four types). The old `/skyblock spawn merchant` without a type argument no longer works — pick a type.

### Changed
- **Config structure**: `merchantOffers` (flat map) → `merchants: { <type>: { displayName, offers, buybacks } }`. The legacy flat `merchantOffers` is auto-migrated into `merchants.general.offers` on the first load.
- **Merchants track their type**: each spawned merchant now has a `skyblockcraft_type_<id>` command tag in addition to the base `skyblockcraft_sky_merchant` tag. The right-click interaction opens the shop with the correct offers.
- **`/skyblock reload` output** now shows both offers and buybacks counts.

### Removed
- **`/skyblock buy <offerId>`**: obsolete now that clicks happen inside the GUI. Removed from the command tree.
- **Chat-based shop menu**: replaced by the GUI.

## [0.1.0-alpha.2] — 2026-09-10

### Fixed
- **Minecraft version compatibility**: the mod was declared compatible with all 1.21.x versions (`~1.21.1` in `fabric.mod.json`), but it only actually works on 1.21.1. Fabric Loader will now refuse to load the mod on 1.21.2 and later, preventing silent breakage on incompatible versions. Users on 1.21.2+ must downgrade to 1.21.1 or wait for a version bump of the mod.

### Added
- **GitHub Releases + Modrinth auto-publish**: pushing a tag matching `v*` (e.g. `v0.1.0-alpha.2`) now builds the JAR, creates a GitHub Release, and publishes to Modrinth via the `mod-publish-plugin` (requires the `MODRINTH_TOKEN` GitHub secret).

### Changed
- Committed `gradle-wrapper.jar` (was excluded by mistake), fixed `gradlew` executable bit for CI, upgraded to `setup-gradle@v4`.
- Documentation cleanup: shorter user-facing README, dev-focused SETUP.

## [0.1.0-alpha.1] — 2026-05-14

Initial prototype.

### Added
- **Island system**: `/island create` generates the classic Skyblock starter
  island (dirt platform, oak tree, sand + cactus, starter chest with seeds and
  starter items). `/island home` teleports the player to their island.
  `/island visit <player>` teleports to another player's island (configurable).
  Islands are arranged on a spaced grid centered at a configurable origin.
- **Coin economy**: persistent per-player coin balance stored at the world
  level via `PersistentState`. Commands `/balance` (alias `/bal`),
  `/balance <player>` (OP), `/pay <player> <amount>`.
- **Sky Merchant villagers**: spawn one with `/skyblock spawn merchant` (OP),
  right-click to open a chat-based shop with clickable `[BUY]` buttons. Offers
  are defined in `config/skyblockcraft.json` and reloadable via
  `/skyblock reload`. Default offers cover common Skyblock-needed items: dirt,
  oak logs, cobblestone, iron, gold, diamond, emerald, water bucket,
  lava bucket.
- **Public/Debug dual build**: like MurderCraft, two JARs are produced. The
  public build forces debug flags to `false` at runtime even if the JSON
  config tries to enable them.
- **Mod Menu + Cloth Config** integration for an in-game config screen with
  categories: Islands, Economy, Merchant, UI, Debug (debug only).
- **Localization**: full French and English translations.
- **GitHub Actions** workflow producing both JARs on push.

### Known limitations
- The shop uses chat messages with clickable `[BUY]` buttons rather than a
  custom GUI. Functional but minimalistic — a real `ScreenHandler` is planned.
- Islands are generated in the overworld. A dedicated dimension is planned.
- No protection against other players placing/breaking blocks on your island.
- No island leveling / upgrades.
- The Sky Merchant entity is a vanilla villager with a tag and a display name
  — not a fully custom entity yet.
