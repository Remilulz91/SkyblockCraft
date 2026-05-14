package fr.skyblockcraft.island;

import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player island data: assignment on a spaced grid, persistence,
 * and lookup.
 *
 * Islands are placed on a fixed grid centered at (islandsGridOriginX, islandsGridOriginZ)
 * with a configurable spacing. New players are assigned the next free slot in a
 * spiral around the origin.
 */
public class IslandManager extends PersistentState {

    private static final String STATE_KEY = SkyblockCraft.MOD_ID + "_islands";

    private final Map<UUID, PlayerIsland> islands = new HashMap<>();
    private int nextSlotIndex = 0;

    public IslandManager() {}

    // ---- Public API ----

    /** Returns the player's island, or null if they don't have one yet. */
    public PlayerIsland getIsland(UUID playerId) {
        return islands.get(playerId);
    }

    public boolean hasIsland(UUID playerId) {
        return islands.containsKey(playerId);
    }

    /**
     * Allocates a new island slot for the player, computes its block-space spawn,
     * and stores the record. Does NOT generate blocks — that's
     * {@link IslandGenerator#generate(ServerWorld, PlayerIsland)}'s job.
     *
     * Returns the new island record.
     */
    public PlayerIsland createIsland(UUID playerId) {
        // Compute next grid coords using a simple spiral around origin
        int[] coords = nextSpiralCoords(nextSlotIndex);
        nextSlotIndex++;

        SkyblockCraftConfig cfg = SkyblockCraftConfig.get();
        int blockX = cfg.islandsGridOriginX + coords[0] * cfg.islandSpacing;
        int blockZ = cfg.islandsGridOriginZ + coords[1] * cfg.islandSpacing;
        BlockPos spawn = new BlockPos(blockX, cfg.islandY + 2, blockZ);

        PlayerIsland island = new PlayerIsland(
                playerId,
                coords[0],
                coords[1],
                spawn,
                System.currentTimeMillis()
        );

        islands.put(playerId, island);
        markDirty();
        return island;
    }

    /** Removes a player's island record (used for /island reset or admin actions). */
    public void removeIsland(UUID playerId) {
        if (islands.remove(playerId) != null) {
            markDirty();
        }
    }

    /**
     * Spiral coordinates around origin: 0=(0,0), 1=(1,0), 2=(1,1), 3=(0,1), 4=(-1,1)...
     * Simple clockwise spiral that visits each integer cell exactly once.
     */
    private static int[] nextSpiralCoords(int index) {
        if (index == 0) return new int[]{0, 0};
        int x = 0, z = 0;
        int dx = 0, dz = -1;
        for (int i = 0; i < index; i++) {
            // Change direction at appropriate steps to spiral outward
            if ((x == z) || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int tmp = dx;
                dx = -dz;
                dz = tmp;
            }
            x += dx;
            z += dz;
        }
        return new int[]{x, z};
    }

    // ---- Persistence ----

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (PlayerIsland island : islands.values()) {
            list.add(island.toNbt());
        }
        nbt.put("islands", list);
        nbt.putInt("nextSlotIndex", nextSlotIndex);
        return nbt;
    }

    public static IslandManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        IslandManager mgr = new IslandManager();
        if (nbt.contains("islands")) {
            NbtList list = nbt.getList("islands", NbtCompound.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                PlayerIsland island = PlayerIsland.fromNbt(list.getCompound(i));
                mgr.islands.put(island.owner, island);
            }
        }
        if (nbt.contains("nextSlotIndex")) {
            mgr.nextSlotIndex = nbt.getInt("nextSlotIndex");
        }
        return mgr;
    }

    private static final PersistentState.Type<IslandManager> TYPE = new PersistentState.Type<>(
            IslandManager::new,
            IslandManager::fromNbt,
            DataFixTypes.LEVEL
    );

    public static IslandManager get(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld is not loaded yet");
        }
        PersistentStateManager psm = overworld.getPersistentStateManager();
        return psm.getOrCreate(TYPE, STATE_KEY);
    }
}
