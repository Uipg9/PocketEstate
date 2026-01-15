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
    
    // Costs for unlocking crop slots (54 slots = 6 pages of 9)
    // Prices are cheaper and scale gradually
    private static final long[] CROP_SLOT_COSTS = {
        // Page 1 (slots 1-9) - Very cheap to get started
        0,      // Slot 1 - free (starting)
        100,    // Slot 2
        200,    // Slot 3
        300,    // Slot 4
        400,    // Slot 5
        500,    // Slot 6
        600,    // Slot 7
        700,    // Slot 8
        800,    // Slot 9
        // Page 2 (slots 10-18)
        1000,   1200,   1400,   1600,   1800,   2000,   2200,   2400,   2600,
        // Page 3 (slots 19-27)
        3000,   3500,   4000,   4500,   5000,   5500,   6000,   6500,   7000,
        // Page 4 (slots 28-36)
        8000,   9000,   10000,  11000,  12000,  13000,  14000,  15000,  16000,
        // Page 5 (slots 37-45)
        18000,  20000,  22000,  24000,  26000,  28000,  30000,  32000,  34000,
        // Page 6 (slots 46-54)
        36000,  38000,  40000,  42000,  44000,  46000,  48000,  50000,  55000
    };
    
    /**
     * Get the cost to unlock the next crop slot
     */
    public static long getNextCropSlotCost(UUID playerId) {
        if (PocketEstate.dataManager == null) return -1;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        int currentSlots = data.getUnlockedCropSlots();
        
        if (currentSlots >= PlayerData.MAX_CROP_PLOTS) return -1; // Already maxed
        
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
        
        if (currentSlots >= PlayerData.MAX_CROP_PLOTS) return false; // Already maxed
        
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
