# SkyblockCraft — Developer Setup

This document explains how to set up your local dev environment, build the
mod, and create a new release.

## Prerequisites

- **Java 21** (Temurin recommended)
- **Git**
- A Java IDE (IntelliJ IDEA preferred — first-class Fabric Loom support)

## Cloning

```bash
git clone https://github.com/Remilulz91/SkyblockCraft.git
cd SkyblockCraft
```

## First-time build

The first build downloads Minecraft, Yarn mappings, Fabric Loader, the Fabric
API, Cloth Config and Mod Menu. It can take a few minutes.

```bash
./gradlew build
```

The output JAR is in `build/libs/skyblockcraft-<version>.jar`.

## Building the DEBUG variant

```bash
./gradlew clean build -PbuildType=debug
```

This produces `skyblockcraft-<version>-debug.jar`. The debug build has
`/skyblock debug` commands enabled by default and allows regenerating an
existing island.

## Running in dev (with hot reload)

Fabric Loom generates run configurations for both client and server:

```bash
./gradlew runClient   # launches a Minecraft client with the mod loaded
./gradlew runServer   # launches a dedicated server with the mod loaded
```

These commands open Minecraft in dev mode pointing at the mod source. Save a
Java file and rebuild to see your changes (you'll usually need to restart
Minecraft for code changes — only resource changes hot-reload).

## Importing into IntelliJ IDEA

1. **File → Open** → select the project root (where `build.gradle` lives).
2. Wait for the Gradle import to finish.
3. Open the Gradle tool window → expand `Tasks → fabric` → double-click
   `genSources` (optional, generates Minecraft source attachments).
4. Run configurations `Minecraft Client` and `Minecraft Server` appear under
   the run dropdown.

## Project conventions

- **Server-side data** lives in `PersistentState` subclasses attached to the
  overworld (see `EconomyManager`, `IslandManager`). They auto-save when
  `markDirty()` is called.
- **Commands** are split per area (`IslandCommand`, `EconomyCommand`, etc.) and
  registered from `SkyblockCraft.onInitialize()`.
- **Localization keys** live in `assets/skyblockcraft/lang/en_us.json` and
  `fr_fr.json`. Keep them in sync.
- **Build type** is checked via `SkyblockCraft.isDebugBuild()`. Use this guard
  before exposing any debug feature.

## Releasing a new version

1. Bump `mod_version` in `gradle.properties`.
2. Add an entry to `CHANGELOG.md` with the changes.
3. Commit: `git commit -am "vX.Y.Z: <summary>"`.
4. Push: `git push origin main`.
5. GitHub Actions builds both PUBLIC and DEBUG JARs and uploads them as
   artifacts under the `Actions` tab.
6. Download the ZIP, extract, and create a GitHub Release with the public JAR.

## Troubleshooting

**Build fails: "Could not resolve net.fabricmc:yarn..."**
Yarn mappings versions are tied to a specific Minecraft version. If you
update `minecraft_version` in `gradle.properties`, also update
`yarn_mappings` to a matching version from https://fabricmc.net/develop.

**Build fails: "gradle-wrapper.jar not found"**
Run `gradle wrapper` once locally (with Gradle 8.10 installed) to generate
the wrapper jar. The CI handles this automatically.

**Mod loads but commands are missing**
Check the log line `[SkyblockCraft] Commands registered`. If absent, an
exception happened during init — scroll up in `logs/latest.log`.

**`/skyblock debug` not available**
Either you're running the PUBLIC build, or `enableDebugCommands` is `false`.
In the DEBUG build, set it to `true` in `config/skyblockcraft.json` and
reload with `/skyblock reload`.
