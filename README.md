# SkyblockCraft

A classic Skyblock mod for Minecraft 1.21.1 (Fabric) with a virtual coin economy
and custom Sky Merchant villagers. Generate your starter island, earn coins,
and trade with merchants.

## Features

- **`/island create`** — generate a classic Skyblock starter island (dirt platform, oak tree, sand + cactus, starter chest with seeds and starter items) and teleport there.
- **`/island home`** and **`/island visit <player>`** — travel between islands.
- **Virtual coin economy** — per-player balance persisted with the world. `/balance` to check, `/pay <player> <amount>` to transfer.
- **Sky Merchant villagers** — spawn with `/skyblock spawn merchant` (OP), right-click for a chat-based shop with clickable `[BUY]` buttons.
- **Configurable offers** — define shop items and prices in `config/skyblockcraft.json`, reloadable via `/skyblock reload`.
- **Mod Menu + Cloth Config** integration for an in-game config screen.
- **Public + Debug dual build** — the public JAR strips out debug commands and refuses to re-enable them from the config file.

## Requirements

- Minecraft **1.21.1** with Fabric Loader
- **Fabric API** (required)
- **Cloth Config** and **Mod Menu** (optional, for the config GUI)
- Java 21

## Installation

1. Download `skyblockcraft-<version>.jar` from [Modrinth](https://modrinth.com/mod/skyblockcraft) or the [Releases](https://github.com/Remilulz91/SkyblockCraft/releases) page.
2. Drop it in your `mods/` folder along with Fabric API.
3. Launch Minecraft. `config/skyblockcraft.json` is created on first launch with sensible defaults.

## Quick start

1. **`/island create`** — get your starter island.
2. As OP: **`/skyblock spawn merchant`** to place a merchant near you.
3. Right-click the merchant → click a `[BUY]` link to purchase.
4. **`/balance`** to check funds, **`/pay <player> <amount>`** to send coins.

## Commands

| Command | OP | Description |
|---|---|---|
| `/island create` | no | Generate your starter island |
| `/island home` | no | Teleport to your island |
| `/island visit <player>` | no | Visit another player's island |
| `/balance` (or `/bal`) | no | Show your coin balance |
| `/pay <player> <amount>` | no | Send coins to another player |
| `/skyblock spawn merchant` | yes | Spawn a Sky Merchant |
| `/skyblock reload` | yes | Reload `skyblockcraft.json` |
| `/skyblock version` | no | Show mod version + build type |

## Configuration

Key fields in `config/skyblockcraft.json`:

- `islandSpacing` — blocks between two player islands (default 512)
- `startingCoins` — new player balance (default 100)
- `merchantOffers` — map of `offer_id` → `"namespace:path:count:price"`
- `merchantDisplayName` — name above a Sky Merchant (default `Sky Merchant`)

Run `/skyblock reload` after editing.

## License

MIT — see [LICENSE](LICENSE).
