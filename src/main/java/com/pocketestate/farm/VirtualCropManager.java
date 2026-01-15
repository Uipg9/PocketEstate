package com.pocketestate.farm;

import com.pocketestate.PocketEstate;
import com.pocketestate.data.PlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Manages virtual crop growth for all players
 */
public class VirtualCropManager {
    
    /**
     * Process crop growth for all players
     * Called periodically from the main tick handler
     */
    public static void processCropGrowth(MinecraftServer server) {
        if (PocketEstate.dataManager == null) return;
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            processPlayerCrops(player.getUUID());
        }
    }
    
    /**
     * Process crop growth for a specific player
     */
    public static void processPlayerCrops(UUID playerId) {
        if (PocketEstate.dataManager == null) return;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.isPlanted() && !plot.isFullyGrown()) {
                plot.grow();
            }
        }
    }
    
    /**
     * Plant a crop in a specific plot
     * @return true if successful
     */
    public static boolean plantCrop(UUID playerId, int plotIndex, CropType cropType) {
        if (PocketEstate.dataManager == null) return false;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        
        // Check if slot is unlocked
        if (!data.isCropSlotUnlocked(plotIndex)) return false;
        
        CropPlot plot = data.getCropPlot(plotIndex);
        if (plot == null || !plot.isEmpty()) return false;
        
        plot.plant(cropType);
        return true;
    }
    
    /**
     * Harvest a crop from a specific plot
     * @param replant whether to automatically replant
     * @return the harvested items, or null if not ready
     */
    public static net.minecraft.world.item.ItemStack harvestCrop(UUID playerId, int plotIndex, boolean replant) {
        if (PocketEstate.dataManager == null) return null;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        CropPlot plot = data.getCropPlot(plotIndex);
        
        if (plot == null || !plot.isFullyGrown()) return null;
        
        CropType type = plot.getCropType();
        int yield = plot.harvest(replant);
        
        if (yield > 0 && type != null) {
            return new net.minecraft.world.item.ItemStack(type.getHarvestItem(), yield);
        }
        
        return null;
    }
    
    /**
     * Harvest all ready crops
     * @return list of harvested items
     */
    public static java.util.List<net.minecraft.world.item.ItemStack> harvestAll(UUID playerId, boolean replant) {
        java.util.List<net.minecraft.world.item.ItemStack> harvested = new java.util.ArrayList<>();
        
        if (PocketEstate.dataManager == null) return harvested;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            net.minecraft.world.item.ItemStack result = harvestCrop(playerId, i, replant);
            if (result != null && !result.isEmpty()) {
                harvested.add(result);
            }
        }
        
        return harvested;
    }
    
    /**
     * Get the total number of ready crops
     */
    public static int getReadyCropCount(UUID playerId) {
        if (PocketEstate.dataManager == null) return 0;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        int count = 0;
        
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.isFullyGrown()) {
                count++;
            }
        }
        
        return count;
    }
}
