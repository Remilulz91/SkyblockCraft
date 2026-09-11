# SkyblockCraft

A classic Skyblock mod for Minecraft 1.21.1 (Fabric) with a virtual coin economy
and four types of Sky Merchant villagers (Farmer, Miner, Adventurer, General),
each with their own buy and sell offers in a real chest-style GUI.

## Features

- **`/island create`** — generate a classic Skyblock starter island (dirt platform, oak tree, sand + cactus, starter chest with seeds and starter items) and teleport there.
- **`/island home`** and **`/island visit <player>`** — travel between islands.
- **Island protection** — within a configurable radius around each island's center (default 24 blocks), only the owner and trusted co-op players can build or break. Interactions (chests, doors) remain free so visitors can look around.
- **Co-op trust** — `/island trust <player>` gives another player build rights on your island. `/island untrust` revokes. `/island trusted` lists.
- **Island level + leaderboard** — every block you place on your island counts toward your level (default 100 blocks per level). `/island level` shows your progress. `/island top` shows the top 10 islands.
- **Virtual coin economy** — per-player balance persisted with the world. `/balance` to check, `/pay <player> <amount>` to transfer.
- **Sky Merchant villagers** with four types (`farmer`, `miner`, `adventurer`, `general`), each with its own offers and buybacks. Spawn with `/skyblock spawn merchant <type>` (OP), right-click for a 6-row GUI with BUY / SELL tabs. Merchants are truly invulnerable — use `/skyblock remove merchant` to remove one.
- **Coin generators** — three tiers of custom blocks (Basic / Advanced / Elite) sold by the General merchant. Placed on your island, they passively generate coins in your balance while you're online. Limited to 5 per island by default.
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
| `/island level` | no | Show your island level and placed-block count |
| `/island top` | no | List the top 10 islands by level |
| `/island trust <player>` | no | Allow another player to build on your island |
| `/island untrust <player>` | no | Revoke build rights |
| `/island trusted` | no | List players trusted on your island |
| `/balance` (or `/bal`) | no | Show your coin balance |
| `/pay <player> <amount>` | no | Send coins to another player |
| `/skyblock spawn merchant <type>` | yes | Spawn a merchant of the given type (farmer, miner, adventurer, general) |
| `/skyblock remove merchant` | yes | Remove the nearest merchant within 20 blocks (they're invulnerable) |
| `/skyblock reload` | yes | Reload `skyblockcraft.json` |
| `/skyblock version` | no | Show mod version + build type |

## Configuration

Key sections in `config/skyblockcraft.json`:

- `islandSpacing` — blocks between two player islands (default 512)
- `protectionRadius` — half-side of the protected zone around each island (default 24 → 49×49)
- `blocksPerLevel` — blocks needed to gain one level (default 100)
- `opBypassIslandProtection` — if true, OPs can build/break on any island (default true)
- `maxGeneratorsPerIsland` — cap on coin generators per island (default 5)
- `startingCoins` — new player balance (default 100)
- `merchants` — per-type merchant config. Each type has `displayName`, `offers` (what the merchant sells), and `buybacks` (what the merchant buys from the player). Both use the format `"offer_id": "namespace:path:count:price"`.

Run `/skyblock reload` after editing.

## License

MIT — see [LICENSE](LICENSE).
