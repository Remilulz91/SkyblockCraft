package fr.skyblockcraft.island;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Per-player island record: position on the grid, trusted co-op players,
 * placed-block counter (drives island level), and creation timestamp.
 *
 * NBT is backward compatible: v0.1/v0.2 islands without trustedPlayers or
 * placedBlocks fields load with empty set / 0 counter.
 */
public class PlayerIsland {

    public final UUID owner;
    public final int gridX;      // grid column index, not block coords
    public final int gridZ;      // grid row index
    public final BlockPos spawn; // block coords where the player spawns when teleporting "home"
    public final long createdAt; // epoch millis

    /** Co-op players allowed to build/break on this island. Mutable. */
    public final Set<UUID> trustedPlayers = new HashSet<>();

    /**
     * Net number of blocks placed on this island (increment on place, decrement
     * on break). Drives the island level.
     */
    public long placedBlocks = 0L;

    public PlayerIsland(UUID owner, int gridX, int gridZ, BlockPos spawn, long createdAt) {
        this.owner = owner;
        this.gridX = gridX;
        this.gridZ = gridZ;
        this.spawn = spawn;
        this.createdAt = createdAt;
    }

    public int getLevel(int blocksPerLevel) {
        if (blocksPerLevel <= 0) return 0;
        return (int) (placedBlocks / blocksPerLevel);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("owner", owner);
        nbt.putInt("gridX", gridX);
        nbt.putInt("gridZ", gridZ);
        nbt.putInt("spawnX", spawn.getX());
        nbt.putInt("spawnY", spawn.getY());
        nbt.putInt("spawnZ", spawn.getZ());
        nbt.putLong("createdAt", createdAt);
        nbt.putLong("placedBlocks", placedBlocks);

        NbtList trustList = new NbtList();
        for (UUID u : trustedPlayers) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("uuid", u);
            trustList.add(entry);
        }
        nbt.put("trustedPlayers", trustList);

        return nbt;
    }

    public static PlayerIsland fromNbt(NbtCompound nbt) {
        PlayerIsland island = new PlayerIsland(
                nbt.getUuid("owner"),
                nbt.getInt("gridX"),
                nbt.getInt("gridZ"),
                new BlockPos(nbt.getInt("spawnX"), nbt.getInt("spawnY"), nbt.getInt("spawnZ")),
                nbt.getLong("createdAt")
        );
        // Backward compat: these fields don't exist in v0.1/v0.2 saves
        if (nbt.contains("placedBlocks")) {
            island.placedBlocks = nbt.getLong("placedBlocks");
        }
        if (nbt.contains("trustedPlayers", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("trustedPlayers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                island.trustedPlayers.add(list.getCompound(i).getUuid("uuid"));
            }
        }
        return island;
    }
}
