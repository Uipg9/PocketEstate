package com.pocketestate.farm;

import net.minecraft.nbt.CompoundTag;

/**
 * Represents a single crop plot in the virtual garden
 */
public class CropPlot {
    private CropType cropType;
    private int growthStage;
    private int harvestCount;
    private boolean isPlanted;
    
    public CropPlot() {
        this.cropType = null;
        this.growthStage = 0;
        this.harvestCount = 0;
        this.isPlanted = false;
    }
    
    // Getters
    public CropType getCropType() { return cropType; }
    public int getGrowthStage() { return growthStage; }
    public int getHarvestCount() { return harvestCount; }
    public boolean isPlanted() { return isPlanted; }
    
    /**
     * Check if the crop is fully grown
     */
    public boolean isFullyGrown() {
        return isPlanted && cropType != null && growthStage >= cropType.getMaxGrowthStage();
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
        this.growthStage = 0;
        this.isPlanted = true;
    }
    
    /**
     * Advance growth by one stage
     * @return true if growth occurred
     */
    public boolean grow() {
        if (!isPlanted || cropType == null) return false;
        if (growthStage >= cropType.getMaxGrowthStage()) return false;
        
        growthStage++;
        return true;
    }
    
    /**
     * Harvest the crop
     * @param replant whether to automatically replant
     * @return the yield amount, or 0 if not ready
     */
    public int harvest(boolean replant) {
        if (!isFullyGrown()) return 0;
        
        int yield = cropType.getBaseYield();
        harvestCount++;
        
        if (replant) {
            growthStage = 0;
        } else {
            cropType = null;
            isPlanted = false;
            growthStage = 0;
        }
        
        return yield;
    }
    
    /**
     * Clear the plot completely
     */
    public void clear() {
        this.cropType = null;
        this.growthStage = 0;
        this.isPlanted = false;
    }
    
    /**
     * Get growth progress as a percentage (0.0 to 1.0)
     */
    public float getGrowthProgress() {
        if (!isPlanted || cropType == null) return 0f;
        return (float) growthStage / cropType.getMaxGrowthStage();
    }
    
    // NBT serialization
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("planted", isPlanted);
        tag.putInt("growthStage", growthStage);
        tag.putInt("harvestCount", harvestCount);
        if (cropType != null) {
            tag.putString("cropType", cropType.name());
        }
        return tag;
    }
    
    public static CropPlot fromNBT(CompoundTag tag) {
        CropPlot plot = new CropPlot();
        plot.isPlanted = tag.getBoolean("planted").orElse(false);
        plot.growthStage = tag.getInt("growthStage").orElse(0);
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
