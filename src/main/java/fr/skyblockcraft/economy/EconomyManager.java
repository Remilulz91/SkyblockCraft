package fr.skyblockcraft.economy;

import fr.skyblockcraft.SkyblockCraft;
import fr.skyblockcraft.config.SkyblockCraftConfig;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persists per-player coin balances at the world (overworld) level.
 *
 * Uses Minecraft's {@link PersistentState} infrastructure, so data is saved
 * with the world and reloaded automatically.
 *
 * Access the singleton via {@link #get(MinecraftServer)}.
 */
public class EconomyManager extends PersistentState {

    private static final String STATE_KEY = SkyblockCraft.MOD_ID + "_economy";

    private final Map<UUID, Long> balances = new HashMap<>();

    public EconomyManager() {}

    // ---- Public API ----

    /**
     * Returns the player's current balance.
     * If the player has no entry yet, initializes it to the configured starting balance.
     */
    public long getBalance(UUID playerId) {
        Long b = balances.get(playerId);
        if (b == null) {
            long starting = SkyblockCraftConfig.get().startingCoins;
            balances.put(playerId, starting);
            markDirty();
            return starting;
        }
        return b;
    }

    /** Returns true if the player has a balance entry (i.e. has joined at least once). */
    public boolean hasAccount(UUID playerId) {
        return balances.containsKey(playerId);
    }

    /** Sets the balance to a specific value, clamping to [0, maxCoins] if a max is set. */
    public void setBalance(UUID playerId, long amount) {
        long clamped = clamp(amount);
        balances.put(playerId, clamped);
        markDirty();
    }

    /** Adds coins. Returns the new balance (capped). */
    public long add(UUID playerId, long amount) {
        if (amount <= 0) return getBalance(playerId);
        long newBalance = clamp(getBalance(playerId) + amount);
        balances.put(playerId, newBalance);
        markDirty();
        return newBalance;
    }

    /**
     * Removes coins if the player has enough.
     * Returns true on success, false if not enough funds.
     */
    public boolean tryRemove(UUID playerId, long amount) {
        if (amount <= 0) return true;
        long current = getBalance(playerId);
        if (current < amount) return false;
        balances.put(playerId, current - amount);
        markDirty();
        return true;
    }

    /**
     * Transfers coins from sender to recipient.
     * Returns true on success.
     */
    public boolean transfer(UUID sender, UUID recipient, long amount) {
        if (amount <= 0) return false;
        if (sender.equals(recipient)) return false;
        long senderBalance = getBalance(sender);
        if (senderBalance < amount) return false;
        long recipientNew = clamp(getBalance(recipient) + amount);
        long actuallyTransferred = recipientNew - getBalance(recipient);
        if (actuallyTransferred <= 0) return false;
        balances.put(sender, senderBalance - actuallyTransferred);
        balances.put(recipient, recipientNew);
        markDirty();
        return true;
    }

    private static long clamp(long amount) {
        if (amount < 0) return 0;
        long max = SkyblockCraftConfig.get().maxCoins;
        if (max > 0 && amount > max) return max;
        return amount;
    }

    // ---- Persistence ----

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, Long> e : balances.entrySet()) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("uuid", e.getKey());
            entry.putLong("balance", e.getValue());
            list.add(entry);
        }
        nbt.put("balances", list);
        return nbt;
    }

    public static EconomyManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        EconomyManager mgr = new EconomyManager();
        if (nbt.contains("balances")) {
            NbtList list = nbt.getList("balances", NbtCompound.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound entry = list.getCompound(i);
                mgr.balances.put(entry.getUuid("uuid"), entry.getLong("balance"));
            }
        }
        return mgr;
    }

    private static final PersistentState.Type<EconomyManager> TYPE = new PersistentState.Type<>(
            EconomyManager::new,
            EconomyManager::fromNbt,
            DataFixTypes.LEVEL // closest existing type; harmless for our custom data
    );

    /**
     * Returns the singleton EconomyManager attached to the overworld.
     * Loads or creates it on first access.
     */
    public static EconomyManager get(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld is not loaded yet");
        }
        PersistentStateManager psm = overworld.getPersistentStateManager();
        return psm.getOrCreate(TYPE, STATE_KEY);
    }
}
