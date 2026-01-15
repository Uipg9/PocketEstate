package com.pocketestate.farm;

import com.pocketestate.PocketEstate;
import com.pocketestate.data.PlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles virtual crop growth and harvesting
 * Updated with idle-game mechanics: XP/money rewards, plant all, bonemeal boost
 */
public class VirtualCropManager {
    
    // XP and money rewards per harvest
    public static final int XP_PER_HARVEST = 2;
    public static final int MONEY_PER_HARVEST = 5;
    
    // Track for notification
    private static boolean allCropsReadyNotified = false;
    
    /**
     * Called every game tick from server - processes all online players
     */
    public static void processCropGrowth(MinecraftServer server) {
        if (PocketEstate.dataManager == null) return;
        
        // Process all online players
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
            processCropGrowthForPlayer(data, player);
        }
    }
    
    /**
     * Process crop growth for a single player's data
     */
    public static void processCropGrowthForPlayer(PlayerData data, ServerPlayer player) {
        boolean anyGrowing = false;
        int readyCount = 0;
        int totalPlanted = 0;
        
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.getCropType() != null) {
                totalPlanted++;
                if (!plot.isReady()) {
                    plot.tick();
                    anyGrowing = true;
                } else {
                    readyCount++;
                }
            }
        }
        
        // Tick compost bin
        data.tickCompost();
        
        // Auto-harvest if enabled
        if (data.isAutoHarvestEnabled() && readyCount > 0) {
            harvestAllWithRewards(data, player);
        }
        
        // Reset notification flag if any crops are still growing
        if (anyGrowing) {
            allCropsReadyNotified = false;
        }
        
        // Track if all planted crops are now ready (for notification)
        if (totalPlanted > 0 && readyCount == totalPlanted && !allCropsReadyNotified) {
            allCropsReadyNotified = true;
        }
    }

    /**
     * Plant a specific crop type in a plot
     */
    public static boolean plantCrop(PlayerData data, int slotIndex, CropType cropType) {
        if (!data.isCropSlotUnlocked(slotIndex)) return false;

        CropPlot plot = data.getCropPlot(slotIndex);
        if (plot == null || plot.getCropType() != null) return false;

        plot.plant(cropType);
        return true;
    }
    
    /**
     * Plant the same crop type in all empty unlocked plots
     * @return Number of plots planted
     */
    public static int plantAll(PlayerData data, CropType cropType, int maxSeeds) {
        int planted = 0;
        for (int i = 0; i < data.getUnlockedCropSlots() && planted < maxSeeds; i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.getCropType() == null) {
                plot.plant(cropType);
                planted++;
            }
        }
        return planted;
    }

    /**
     * Harvest a crop from a plot
     * @return ItemStack of harvested crop, or empty if not ready
     */
    public static ItemStack harvestCrop(PlayerData data, int slotIndex) {
        if (!data.isCropSlotUnlocked(slotIndex)) return ItemStack.EMPTY;

        CropPlot plot = data.getCropPlot(slotIndex);
        if (plot == null || !plot.isReady()) return ItemStack.EMPTY;

        CropType type = plot.getCropType();
        if (type == null) return ItemStack.EMPTY;

        // Get yield and clear plot
        int yield = plot.harvest();
        return new ItemStack(type.getYieldItem(), yield);
    }
    
    /**
     * Harvest a crop with XP and money rewards for the player
     * @return ItemStack of harvested crop, or empty if not ready
     */
    public static ItemStack harvestCropWithRewards(PlayerData data, int slotIndex, ServerPlayer player) {
        ItemStack result = harvestCrop(data, slotIndex);
        
        if (!result.isEmpty() && player != null) {
            // Give XP
            player.giveExperiencePoints(XP_PER_HARVEST);
            data.addXpEarned(XP_PER_HARVEST);
            
            // Give money
            data.addBalance(MONEY_PER_HARVEST);
            data.addMoneyEarned(MONEY_PER_HARVEST);
            
            // Track stats
            data.addCropsHarvested(1);
        }
        
        return result;
    }

    /**
     * Harvest all ready crops (no rewards, for internal use)
     * @return Total number of items harvested
     */
    public static int harvestAll(PlayerData data) {
        List<ItemStack> harvested = new ArrayList<>();
        int totalHarvested = 0;

        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.isReady()) {
                ItemStack result = harvestCrop(data, i);
                if (!result.isEmpty()) {
                    harvested.add(result);
                    totalHarvested += result.getCount();
                }
            }
        }

        // Add all harvested to crop output buffer
        for (ItemStack stack : harvested) {
            data.addToCropOutput(stack);
        }
        
        return totalHarvested;
    }
    
    /**
     * Harvest all ready crops with XP and money rewards
     * @return Total number of items harvested
     */
    public static int harvestAllWithRewards(PlayerData data, ServerPlayer player) {
        List<ItemStack> harvested = new ArrayList<>();
        int totalHarvested = 0;
        int plotsHarvested = 0;

        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.isReady()) {
                ItemStack result = harvestCrop(data, i);
                if (!result.isEmpty()) {
                    harvested.add(result);
                    totalHarvested += result.getCount();
                    plotsHarvested++;
                }
            }
        }

        // Add all harvested to crop output buffer
        for (ItemStack stack : harvested) {
            data.addToCropOutput(stack);
        }
        
        // Award XP and money based on plots harvested
        if (plotsHarvested > 0 && player != null) {
            int totalXp = plotsHarvested * XP_PER_HARVEST;
            long totalMoney = (long) plotsHarvested * MONEY_PER_HARVEST;
            
            player.giveExperiencePoints(totalXp);
            data.addXpEarned(totalXp);
            data.addBalance(totalMoney);
            data.addMoneyEarned(totalMoney);
            data.addCropsHarvested(plotsHarvested);
            
            // Play harvest sound
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.7f, 1.0f);
        }
        
        return totalHarvested;
    }

    /**
     * Get the number of ready crops
     */
    public static int getReadyCropCount(PlayerData data) {
        int count = 0;
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.isReady()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get the number of empty (unlocked) plots
     */
    public static int getEmptyPlotCount(PlayerData data) {
        int count = 0;
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.getCropType() == null) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get the number of growing crops
     */
    public static int getGrowingCropCount(PlayerData data) {
        int count = 0;
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.getCropType() != null && !plot.isReady()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Apply bonemeal boost to all growing crops
     * @return Number of crops affected
     */
    public static int applyBonemealBoost(PlayerData data) {
        int affected = 0;
        
        for (int i = 0; i < data.getUnlockedCropSlots(); i++) {
            CropPlot plot = data.getCropPlot(i);
            if (plot != null && plot.getCropType() != null && !plot.isReady()) {
                // Boost growth by 25% of remaining time
                int remaining = plot.getGrowthTimeRemaining();
                int boost = Math.max(1, remaining / 4);
                plot.boostGrowth(boost);
                affected++;
            }
        }
        
        return affected;
    }
    
    /**
     * Check if all crops are ready (for notification purposes)
     */
    public static boolean checkAllCropsReady(PlayerData data) {
        return allCropsReadyNotified;
    }
    
    /**
     * Reset the notification flag
     */
    public static void resetNotification() {
        allCropsReadyNotified = false;
    }
}
