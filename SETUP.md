# SkyblockCraft — Developer Setup

## Prerequisites

- **Java 21** (Temurin recommended)
- A Java IDE (IntelliJ IDEA has first-class Fabric Loom support)

## Build

```bash
./gradlew build                          # PUBLIC JAR
./gradlew clean build -PbuildType=debug  # DEBUG JAR
```

Output in `build/libs/skyblockcraft-<version>[-debug].jar`.

## Run in dev

```bash
./gradlew runClient   # client with the mod loaded
./gradlew runServer   # dedicated server with the mod loaded
```

Only resource changes hot-reload; restart Minecraft for Java changes.

## Project conventions

- Server-side data uses `PersistentState` attached to the overworld (see `EconomyManager`, `IslandManager`).
- Debug features are gated on `SkyblockCraft.isDebugBuild()` — always guard behind this.
- Localization keys live in `assets/skyblockcraft/lang/{en_us,fr_fr}.json` — keep both in sync.

## Build variants (Public vs Debug)

| | Public | Debug |
|---|---|---|
| `/skyblock debug ...` available | ❌ | ✓ |
| Allows regenerating island | ❌ | ✓ |
| Debug flags editable via JSON | forced to `false` | editable |

In PUBLIC builds, debug flags in `skyblockcraft.json` are ignored at runtime — a warning is logged. Install the DEBUG JAR to get debug features.

## Project layout

```
src/main/
├── java/fr/skyblockcraft/
│   ├── SkyblockCraft.java           (main entry, build detection)
│   ├── config/SkyblockCraftConfig.java
│   ├── commands/                    (IslandCommand, EconomyCommand, SkyblockCommand, DebugCommand)
│   ├── economy/EconomyManager.java  (PersistentState — coin balances)
│   ├── island/                      (IslandManager, IslandGenerator, PlayerIsland)
│   ├── merchant/                    (SkyMerchantManager, SkyMerchantOffer)
│   └── client/                      (SkyblockCraftClient, ModMenuIntegration)
└── resources/
    ├── fabric.mod.json
    ├── skyblockcraft.build.properties
    └── assets/skyblockcraft/lang/   (en_us.json, fr_fr.json)
```

## Releasing a new version

1. Bump `mod_version` in `gradle.properties`.
2. Add a `CHANGELOG.md` entry.
3. Commit + push. GitHub Actions produces both JARs as artifacts.
4. Download the PUBLIC JAR and upload it to Modrinth.

## Troubleshooting

**`Could not resolve net.fabricmc:yarn...`** — if you changed `minecraft_version`, also update `yarn_mappings` to a matching version from https://fabricmc.net/develop.

**`/skyblock debug` unavailable** — you're running the PUBLIC build. Install the DEBUG JAR (produced by `-PbuildType=debug`) to get debug commands.

**DEBUG commands (in DEBUG build only)**

| Command | Description |
|---|---|
| `/skyblock debug givecoins <player> <amount>` | Give coins to a player |
| `/skyblock debug setbalance <player> <amount>` | Set a player's balance |
| `/skyblock debug resetisland <player>` | Remove a player's island record |
| `/skyblock debug info` | Print runtime debug info |
