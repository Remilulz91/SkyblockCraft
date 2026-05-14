# SkyblockCraft

A classic Skyblock mod for Minecraft 1.21.1 (Fabric) with a virtual coin economy
and custom Sky Merchant villagers.

This is the **v0.1 prototype** — feature scope is intentionally minimal:
generate your starter island, earn and trade coins, and buy items from
spawnable Sky Merchant villagers.

---

## Features

- **`/island create`** — generate a classic Skyblock starter island (dirt platform,
  oak tree, sand + cactus, starter chest with lava bucket, ice, seeds, etc.) and
  teleport you to it.
- **`/island home`** — teleport back to your island.
- **`/island visit <player>`** — visit another player's island (if enabled in config).
- **Virtual coin economy** — each player has a coin balance persisted at the
  world level. Use `/balance` to check, `/pay <player> <amount>` to transfer.
- **Sky Merchant villagers** — spawn one with `/skyblock spawn merchant` (OP).
  Right-click the merchant to open a chat-based shop with clickable `[BUY]`
  buttons. Buying deducts coins from your balance and gives you the item.
- **Configurable offers** — define merchant offers in `config/skyblockcraft.json`
  as `"offer_id": "minecraft:item:count:price_in_coins"`. Use `/skyblock reload`
  to apply changes without restarting.
- **Mod Menu + Cloth Config** integration for an in-game config screen.
- **Public + Debug dual build** — the public JAR has all debug commands stripped
  out and refuses to enable debug flags from the config file.

## Requirements

- Minecraft **1.21.1** with Fabric Loader
- **Fabric API** (required)
- **Cloth Config** (optional — enables the in-game config GUI)
- **Mod Menu** (optional — exposes the config GUI in the mod list)
- Java 21

## Installation

1. Download `skyblockcraft-<version>.jar` from the
   [Releases](https://github.com/Remilulz91/SkyblockCraft/releases) page.
2. Drop the JAR in your `mods/` folder along with Fabric API.
3. (Optional) Add Cloth Config and Mod Menu to get the in-game config screen.
4. Launch Minecraft. On first launch, a `config/skyblockcraft.json` file is
   created with sensible defaults.

## Quick start

1. Create your island: **`/island create`**
2. Spawn a merchant near your island (as OP): **`/skyblock spawn merchant`**
3. Right-click the merchant — a list of offers appears in chat. Click `[BUY]`
   on any offer to buy it (if you have enough coins).
4. Check your balance anytime: **`/balance`** (alias `/bal`)
5. Send coins to a friend: **`/pay Steve 50`**

## Commands

| Command | OP | Description |
|---|---|---|
| `/island create` | no | Generate your starter island |
| `/island home` | no | Teleport to your island |
| `/island visit <player>` | no | Visit another player's island |
| `/island delete <player>` | yes | Delete a player's island record (admin) |
| `/balance` (or `/bal`) | no | Show your coin balance |
| `/balance <player>` | yes | Show another player's balance |
| `/pay <player> <amount>` | no | Send coins to another player |
| `/skyblock spawn merchant` | yes | Spawn a Sky Merchant at your location |
| `/skyblock buy <offerId>` | no | (used by [BUY] links; usually not typed manually) |
| `/skyblock reload` | yes | Reload `skyblockcraft.json` from disk |
| `/skyblock version` | no | Show mod version + build type |

DEBUG build only:

| Command | Description |
|---|---|
| `/skyblock debug givecoins <player> <amount>` | Give coins to a player |
| `/skyblock debug setbalance <player> <amount>` | Set a player's balance |
| `/skyblock debug resetisland <player>` | Remove a player's island record |
| `/skyblock debug info` | Print runtime debug info |

## Build variants (Public vs Debug)

Like MurderCraft, SkyblockCraft ships two builds:

| | Public | Debug |
|---|---|---|
| `enableDebugCommands` default | `false` | `true` |
| `/skyblock debug ...` available | ❌ | ✓ |
| Tab completion suggests `debug` | ❌ | ✓ |
| Allows regenerating island | ❌ | ✓ |

**Important:** in PUBLIC builds, debug flags in `skyblockcraft.json` are
**ignored** at runtime. You cannot enable debug commands by editing the JSON —
you must install the DEBUG JAR. This prevents accidental cheat surface on
shared servers.

## Configuration

`config/skyblockcraft.json` is created on first launch. Notable fields:

- `islandSpacing` — distance between two player islands (default 512)
- `islandY` — Y level where islands are generated (default 64)
- `startingCoins` — new player balance (default 100)
- `merchantOffers` — map of `offer_id` → `"namespace:path:count:price"`
- `merchantDisplayName` — name shown above a Sky Merchant (default `Sky Merchant`)

Run `/skyblock reload` after editing to apply changes without restarting.

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

## License

MIT — see [LICENSE](LICENSE).

## Author

Made by **Remilulz_91**. Issues and PRs welcome on
[GitHub](https://github.com/Remilulz91/SkyblockCraft).
