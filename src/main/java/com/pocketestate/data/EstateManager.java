package com.pocketestate.data;

import com.pocketestate.PocketEstate;
import com.pocketestate.currency.CurrencyManager;
import com.pocketestate.farm.PenType;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Manager for player estate operations
 * Handles unlocking, upgrading, and accessing estate features
 */
public class EstateManager {
    
    // Costs for unlocking crop slots
    private static final long[] CROP_SLOT_COSTS = {
        0,       // Slot 1 - free (starting)
        500,     // Slot 2
        1000,    // Slot 3
        2500,    // Slot 4
        5000,    // Slot 5
        10000,   // Slot 6
        25000,   // Slot 7
        50000,   // Slot 8
        100000   // Slot 9
    };
    
    /**
     * Get the cost to unlock the next crop slot
     */
    public static long getNextCropSlotCost(UUID playerId) {
        if (PocketEstate.dataManager == null) return -1;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        int currentSlots = data.getUnlockedCropSlots();
        
        if (currentSlots >= 9) return -1; // Already maxed
        
        return CROP_SLOT_COSTS[currentSlots];
    }
    
    /**
     * Attempt to unlock the next crop slot
     * @return true if successful
     */
    public static boolean unlockNextCropSlot(ServerPlayer player) {
        if (PocketEstate.dataManager == null) return false;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        int currentSlots = data.getUnlockedCropSlots();
        
        if (currentSlots >= 9) return false; // Already maxed
        
        long cost = CROP_SLOT_COSTS[currentSlots];
        
        if (!CurrencyManager.canAfford(player, cost)) {
            CurrencyManager.sendInsufficientFundsMessage(player, cost);
            return false;
        }
        
        CurrencyManager.removeMoney(player, cost);
        data.setUnlockedCropSlots(currentSlots + 1);
        CurrencyManager.sendMoneySpentMessage(player, cost, "Unlocked Field Slot " + (currentSlots + 1));
        
        return true;
    }
    
    /**
     * Attempt to unlock a mob pen
     * @return true if successful
     */
    public static boolean unlockMobPen(ServerPlayer player, PenType penType) {
        if (PocketEstate.dataManager == null) return false;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        
        if (data.hasMobPen(penType)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYou already own this pen!"));
            return false;
        }
        
        long cost = penType.getUnlockCost();
        
        if (!CurrencyManager.canAfford(player, cost)) {
            CurrencyManager.sendInsufficientFundsMessage(player, cost);
            return false;
        }
        
        CurrencyManager.removeMoney(player, cost);
        data.unlockMobPen(penType);
        CurrencyManager.sendMoneySpentMessage(player, cost, "Unlocked " + penType.getDisplayName());
        
        return true;
    }
    
    /**
     * Get estate overview statistics
     */
    public static EstateStats getEstateStats(UUID playerId) {
        if (PocketEstate.dataManager == null) return new EstateStats();
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        
        EstateStats stats = new EstateStats();
        stats.balance = data.getBalance();
        stats.unlockedCropSlots = data.getUnlockedCropSlots();
        stats.unlockedPens = data.getMobPens().size();
        stats.pendingOutput = data.getOutputBuffer().stream()
            .mapToInt(s -> s.getCount())
            .sum();
        
        // Count planted and ready crops
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            var plot = data.getCropPlot(i);
            if (plot != null) {
                if (plot.isPlanted()) {
                    stats.plantedCrops++;
                    if (plot.isFullyGrown()) {
                        stats.readyCrops++;
                    }
                }
            }
        }
        
        return stats;
    }
    
    /**
     * Container for estate statistics
     */
    public static class EstateStats {
        public long balance = 0;
        public int unlockedCropSlots = 0;
        public int unlockedPens = 0;
        public int plantedCrops = 0;
        public int readyCrops = 0;
        public int pendingOutput = 0;
    }
}
