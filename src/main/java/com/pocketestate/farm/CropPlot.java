package com.pocketestate.farm;

import net.minecraft.nbt.CompoundTag;

/**
 * Represents a single crop plot in the virtual garden
 * Updated with tick-based growth and bonemeal boost support
 */
public class CropPlot {
    private CropType cropType;
    private int growthProgress; // Ticks of growth completed
    private int harvestCount;
    private boolean isPlanted;

    public CropPlot() {
        this.cropType = null;
        this.growthProgress = 0;
        this.harvestCount = 0;
        this.isPlanted = false;
    }

    // Getters
    public CropType getCropType() { return cropType; }
    public int getGrowthProgress() { return growthProgress; }
    public int getHarvestCount() { return harvestCount; }
    public boolean isPlanted() { return isPlanted; }

    /**
     * Check if the crop is fully grown (alias for isFullyGrown)
     */
    public boolean isReady() {
        return isPlanted && cropType != null && growthProgress >= cropType.getGrowthTime();
    }

    /**
     * Check if the crop is fully grown
     */
    public boolean isFullyGrown() {
        return isReady();
    }

    /**
     * Check if this plot is empty (no crop planted)
     */
    public boolean isEmpty() {
        return !isPlanted || cropType == null;
    }

    /**
     * Plant a crop in this plot
     */
    public void plant(CropType type) {
        this.cropType = type;
        this.growthProgress = 0;
        this.isPlanted = true;
    }

    /**
     * Tick-based growth - called every game tick
     */
    public void tick() {
        if (isPlanted && cropType != null && !isReady()) {
            growthProgress++;
        }
    }

    /**
     * Advance growth by one stage (legacy)
     * @return true if growth occurred
     */
    public boolean grow() {
        if (!isPlanted || cropType == null) return false;
        if (isReady()) return false;

        growthProgress += 20; // Add roughly 1 second worth of growth
        return true;
    }
    
    /**
     * Get remaining growth time in ticks
     */
    public int getGrowthTimeRemaining() {
        if (cropType == null || isReady()) return 0;
        return Math.max(0, cropType.getGrowthTime() - growthProgress);
    }
    
    /**
     * Boost growth by a number of ticks
     */
    public void boostGrowth(int ticks) {
        if (isPlanted && cropType != null && !isReady()) {
            growthProgress += ticks;
            if (growthProgress > cropType.getGrowthTime()) {
                growthProgress = cropType.getGrowthTime();
            }
        }
    }
    
    /**
     * Get growth stage (0-7) for display purposes
     */
    public int getGrowthStage() {
        if (cropType == null) return 0;
        float ratio = (float) growthProgress / cropType.getGrowthTime();
        return Math.min(7, (int) (ratio * 8));
    }
    
    /**
     * Get growth progress as a percentage (0.0 to 1.0)
     */
    public float getGrowthPercent() {
        if (!isPlanted || cropType == null) return 0f;
        return Math.min(1.0f, (float) growthProgress / cropType.getGrowthTime());
    }

    /**
     * Harvest the crop (no replant, clears plot)
     * @return the yield amount, or 0 if not ready
     */
    public int harvest() {
        return harvest(false);
    }

    /**
     * Harvest the crop
     * @param replant whether to automatically replant
     * @return the yield amount, or 0 if not ready
     */
    public int harvest(boolean replant) {
        if (!isReady()) return 0;

        int yield = cropType.getBaseYield();
        harvestCount++;

        if (replant) {
            growthProgress = 0;
        } else {
            cropType = null;
            isPlanted = false;
            growthProgress = 0;
        }

        return yield;
    }

    /**
     * Clear the plot completely
     */
    public void clear() {
        this.cropType = null;
        this.growthProgress = 0;
        this.isPlanted = false;
    }

    // NBT serialization
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("planted", isPlanted);
        tag.putInt("growthProgress", growthProgress);
        tag.putInt("harvestCount", harvestCount);
        if (cropType != null) {
            tag.putString("cropType", cropType.name());
        }
        return tag;
    }

    public static CropPlot fromNBT(CompoundTag tag) {
        CropPlot plot = new CropPlot();
        plot.isPlanted = tag.getBoolean("planted").orElse(false);
        plot.growthProgress = tag.getInt("growthProgress").orElse(0);
        plot.harvestCount = tag.getInt("harvestCount").orElse(0);

        if (tag.contains("cropType")) {
            try {
                String typeName = tag.getString("cropType").orElse("");
                if (!typeName.isEmpty()) {
                    plot.cropType = CropType.valueOf(typeName);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return plot;
    }
}
