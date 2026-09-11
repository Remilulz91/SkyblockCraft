# Changelog

All notable changes to SkyblockCraft will be documented in this file.

## [0.4.0-alpha.1] — 2026-09-11

Coin generators — passive income blocks.

### Added
- **Three tiers of coin generator blocks**:
  - **Basic** (emerald-textured) — 1 coin/min, sold for 500 coins
  - **Advanced** (gold-textured) — 5 coins/min, sold for 3000 coins
  - **Elite** (diamond-textured) — 15 coins/min, sold for 15000 coins
- Each generator is a proper custom block with a block entity. When placed, it remembers the placer as its owner. Every production cycle, if the owner is online, it credits the owner's balance with the tier's coin amount. Offline owners' generators still tick but skip the payout (no infinite AFK farming).
- **Placement restrictions**: generators can only be placed on your own island or on an island you're trusted on (enforced by the existing island protection). A per-island cap (default 5, configurable via `maxGeneratorsPerIsland`) prevents runaway inflation — extra placement attempts are rejected with an action-bar message.
- **Merchant integration**: all three generators are added to the **General** merchant's default offers. Right-click any general merchant → BUY tab → click a generator icon.
- Broken generators drop the block item (standard loot table), so relocation works normally.

### Configuration
- New field: `maxGeneratorsPerIsland` (default 5). Editable in Mod Menu.

### Backward compatibility
- Existing worlds keep working: no data structure changes to `IslandManager` or `EconomyManager`. Generator block entities appear only where players place them.

## [0.3.0-alpha.2] — 2026-09-10

### Fixed
- **`/island visit <self>` is now rejected** with a message pointing to `/island home` instead of silently teleporting to your own island.

## [0.3.0-alpha.1] — 2026-09-10

Complete island system: protection, co-op trust, leveling, and leaderboard.

### Added
- **Island protection**: within a configurable protection radius (default 24 blocks around the island center — a 49×49 zone), only the owner and trusted players may break or place blocks. Other players see an action bar message and their action is cancelled. Interactions (chests, doors) remain unrestricted.
- **Co-op trust**: `/island trust <player>`, `/island untrust <player>`, `/island trusted` (list). Trusted players have the same build/break rights as the owner on that island. Both players get chat feedback when a trust changes.
- **Island level**: each block placed by an authorized player increments the island's `placedBlocks` counter; each block broken decrements it (clamped at 0). Level = `placedBlocks / blocksPerLevel` (default 100 blocks per level). Shown via `/island level`.
- **Leaderboard**: `/island top` (or `/is top`) lists the top 10 islands by placed-block count with owner name (resolved via UserCache for offline players) and level.
- **OP bypass**: config `opBypassIslandProtection` (default true) — permission-level-2+ players can build/break anywhere for admin convenience. Toggleable in Mod Menu.

### Configuration
- New fields: `protectionRadius` (default 24), `blocksPerLevel` (default 100), `opBypassIslandProtection` (default true). All editable in Mod Menu.

### Backward compatibility
- v0.1/v0.2 island records without `trustedPlayers` or `placedBlocks` fields load fine: trusted list starts empty, block counter starts at 0.

## [0.2.0-alpha.2] — 2026-09-10

### Fixed
- **Sky Merchants are now truly invulnerable**. The previous `setInvulnerable(true)` on the villager entity was bypassed by creative-mode players and by damage sources tagged as bypassing invulnerability. Now handled via `ServerLivingEntityEvents.ALLOW_DAMAGE` which cancels all damage on any merchant entity, regardless of source.

### Added
- **`/skyblock remove merchant`** (OP): removes the closest Sky Merchant within a 20-block radius of the player. Needed because merchants can no longer be killed by attacking them.

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
