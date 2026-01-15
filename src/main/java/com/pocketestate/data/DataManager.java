package com.pocketestate.data;

import com.pocketestate.PocketEstate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all persistent data for Pocket Estate
 * Handles saving/loading player data to NBT files
 */
public class DataManager {
    private static final String DATA_FILE = "pocketestate_data.dat";
    
    private final MinecraftServer server;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    
    public DataManager(MinecraftServer server) {
        this.server = server;
    }
    
    /**
     * Get or create player data
     */
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData());
    }
    
    /**
     * Get player balance
     */
    public long getBalance(UUID playerId) {
        return getPlayerData(playerId).getBalance();
    }
    
    /**
     * Add/subtract from balance
     */
    public void addBalance(UUID playerId, long amount) {
        PlayerData data = getPlayerData(playerId);
        data.setBalance(Math.max(0, data.getBalance() + amount));
    }
    
    /**
     * Save all data to disk
     */
    public void save() {
        try {
            File dataFile = server.getWorldPath(LevelResource.ROOT)
                .resolve(DATA_FILE)
                .toFile();
            
            CompoundTag rootTag = new CompoundTag();
            
            // Save player data
            CompoundTag playersTag = new CompoundTag();
            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
                playersTag.put(entry.getKey().toString(), entry.getValue().toNBT());
            }
            rootTag.put("players", playersTag);
            
            NbtIo.writeCompressed(rootTag, dataFile.toPath());
            PocketEstate.LOGGER.debug("Pocket Estate data saved successfully");
        } catch (IOException e) {
            PocketEstate.LOGGER.error("Failed to save Pocket Estate data: " + e.getMessage());
        }
    }
    
    /**
     * Load all data from disk
     */
    public void load() {
        try {
            File dataFile = server.getWorldPath(LevelResource.ROOT)
                .resolve(DATA_FILE)
                .toFile();
            
            if (dataFile.exists()) {
                CompoundTag rootTag = NbtIo.readCompressed(
                    dataFile.toPath(),
                    net.minecraft.nbt.NbtAccounter.unlimitedHeap()
                );
                
                // Load player data
                if (rootTag.contains("players")) {
                    CompoundTag playersTag = rootTag.getCompound("players").orElse(new CompoundTag());
                    for (String key : playersTag.keySet()) {
                        UUID playerId = UUID.fromString(key);
                        CompoundTag playerTag = playersTag.getCompound(key).orElse(new CompoundTag());
                        playerDataMap.put(playerId, PlayerData.fromNBT(playerTag));
                    }
                }
                
                PocketEstate.LOGGER.info("Loaded data for " + playerDataMap.size() + " players");
            }
        } catch (IOException e) {
            PocketEstate.LOGGER.error("Failed to load Pocket Estate data: " + e.getMessage());
        }
    }
}
