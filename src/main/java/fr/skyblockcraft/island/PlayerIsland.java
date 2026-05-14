package fr.skyblockcraft.island;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Per-player island record: position on the grid and creation timestamp.
 */
public class PlayerIsland {

    public final UUID owner;
    public final int gridX;     // grid column index, not block coords
    public final int gridZ;     // grid row index
    public final BlockPos spawn; // block coords where the player spawns when teleporting "home"
    public final long createdAt; // epoch millis

    public PlayerIsland(UUID owner, int gridX, int gridZ, BlockPos spawn, long createdAt) {
        this.owner = owner;
        this.gridX = gridX;
        this.gridZ = gridZ;
        this.spawn = spawn;
        this.createdAt = createdAt;
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
        return nbt;
    }

    public static PlayerIsland fromNbt(NbtCompound nbt) {
        return new PlayerIsland(
                nbt.getUuid("owner"),
                nbt.getInt("gridX"),
                nbt.getInt("gridZ"),
                new BlockPos(nbt.getInt("spawnX"), nbt.getInt("spawnY"), nbt.getInt("spawnZ")),
                nbt.getLong("createdAt")
        );
    }
}
