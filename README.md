# SkyblockCraft

A classic Skyblock mod for Minecraft 1.21.1 (Fabric) with a virtual coin economy
and four types of Sky Merchant villagers (Farmer, Miner, Adventurer, General),
each with their own buy and sell offers in a real chest-style GUI.

## Features

- **`/island create`** — generate a classic Skyblock starter island (dirt platform, oak tree, sand + cactus, starter chest with seeds and starter items) and teleport there.
- **`/island home`** and **`/island visit <player>`** — travel between islands.
- **Virtual coin economy** — per-player balance persisted with the world. `/balance` to check, `/pay <player> <amount>` to transfer.
- **Sky Merchant villagers** with four types (`farmer`, `miner`, `adventurer`, `general`), each with its own offers and buybacks. Spawn with `/skyblock spawn merchant <type>` (OP), right-click for a 6-row GUI with BUY / SELL tabs.
- **Real chest-style GUI** — click an item to buy or sell it. Tab-switch between buy and sell modes.
- **Configurable offers and buybacks** per type in `config/skyblockcraft.json`, reloadable via `/skyblock reload`.
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
2. As OP: **`/skyblock spawn merchant farmer`** (or `miner`, `adventurer`, `general`) to place a merchant.
3. Right-click the merchant → the shop GUI opens. Click any item slot to buy (BUY tab) or sell (SELL tab).
4. **`/balance`** to check funds, **`/pay <player> <amount>`** to send coins.

## Commands

| Command | OP | Description |
|---|---|---|
| `/island create` | no | Generate your starter island |
| `/island home` | no | Teleport to your island |
| `/island visit <player>` | no | Visit another player's island |
| `/balance` (or `/bal`) | no | Show your coin balance |
| `/pay <player> <amount>` | no | Send coins to another player |
| `/skyblock spawn merchant <type>` | yes | Spawn a merchant of the given type (farmer, miner, adventurer, general) |
| `/skyblock remove merchant` | yes | Remove the nearest merchant within 20 blocks (they're invulnerable) |
| `/skyblock reload` | yes | Reload `skyblockcraft.json` |
| `/skyblock version` | no | Show mod version + build type |

## Configuration

Key sections in `config/skyblockcraft.json`:

- `islandSpacing` — blocks between two player islands (default 512)
- `startingCoins` — new player balance (default 100)
- `merchants` — per-type merchant config. Each type has `displayName`, `offers` (what the merchant sells), and `buybacks` (what the merchant buys from the player). Both use the format `"offer_id": "namespace:path:count:price"`.

Run `/skyblock reload` after editing.

## License

MIT — see [LICENSE](LICENSE).
