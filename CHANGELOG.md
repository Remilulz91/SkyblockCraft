# Changelog

All notable changes to SkyblockCraft will be documented in this file.

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
