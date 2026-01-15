package com.pocketestate.farm;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Types of crops that can be grown in the virtual garden
 * Updated with tick-based growth times
 */
public enum CropType {
    // name, seed, yield, maxStages, baseYield, unlockCost, growthTime (ticks)
    WHEAT("Wheat", Items.WHEAT_SEEDS, Items.WHEAT, 8, 5, 100, 600),           // 30 seconds
    CARROT("Carrot", Items.CARROT, Items.CARROT, 8, 3, 150, 800),             // 40 seconds
    POTATO("Potato", Items.POTATO, Items.POTATO, 8, 4, 150, 800),             // 40 seconds
    BEETROOT("Beetroot", Items.BEETROOT_SEEDS, Items.BEETROOT, 4, 2, 200, 1000), // 50 seconds
    MELON("Melon", Items.MELON_SEEDS, Items.MELON_SLICE, 8, 4, 300, 1200),    // 60 seconds
    PUMPKIN("Pumpkin", Items.PUMPKIN_SEEDS, Items.PUMPKIN, 1, 1, 300, 1200),  // 60 seconds
    NETHER_WART("Nether Wart", Items.NETHER_WART, Items.NETHER_WART, 4, 3, 500, 1500); // 75 seconds

    private final String displayName;
    private final Item seedItem;
    private final Item harvestItem;
    private final int maxGrowthStage;
    private final int baseYield;
    private final long unlockCost;
    private final int growthTime; // Growth time in ticks

    CropType(String displayName, Item seedItem, Item harvestItem, int maxGrowthStage, int baseYield, long unlockCost, int growthTime) {
        this.displayName = displayName;
        this.seedItem = seedItem;
        this.harvestItem = harvestItem;
        this.maxGrowthStage = maxGrowthStage;
        this.baseYield = baseYield;
        this.unlockCost = unlockCost;
        this.growthTime = growthTime;
    }

    public String getDisplayName() { return displayName; }
    public Item getSeedItem() { return seedItem; }
    public Item getHarvestItem() { return harvestItem; }
    public Item getYieldItem() { return harvestItem; } // Alias for compatibility
    public int getMaxGrowthStage() { return maxGrowthStage; }
    public int getBaseYield() { return baseYield; }
    public long getUnlockCost() { return unlockCost; }
    public int getGrowthTime() { return growthTime; }
    
    /**
     * Get growth time in seconds
     */
    public int getGrowthTimeSeconds() {
        return growthTime / 20;
    }

    /**
     * Get the vanilla crop block texture path for this growth stage
     */
    public String getTextureForStage(int stage) {
        return switch (this) {
            case WHEAT -> "textures/block/wheat_stage" + Math.min(stage, 7) + ".png";
            case CARROT -> "textures/block/carrots_stage" + Math.min(stage / 2, 3) + ".png";
            case POTATO -> "textures/block/potatoes_stage" + Math.min(stage / 2, 3) + ".png";
            case BEETROOT -> "textures/block/beetroots_stage" + Math.min(stage, 3) + ".png";
            case MELON -> stage >= maxGrowthStage ? "textures/block/melon_side.png" : "textures/block/melon_stem.png";
            case PUMPKIN -> stage >= maxGrowthStage ? "textures/block/pumpkin_side.png" : "textures/block/pumpkin_stem.png";
            case NETHER_WART -> "textures/block/nether_wart_stage" + Math.min(stage, 2) + ".png";
        };
    }

    /**
     * Get display item for the current growth stage
     */
    public Item getDisplayItem(int stage) {
        if (stage >= maxGrowthStage) {
            return harvestItem;
        }
        return seedItem;
    }
    
    /**
     * Find crop type by seed item
     */
    public static CropType fromSeed(Item seed) {
        for (CropType type : values()) {
            if (type.seedItem == seed) {
                return type;
            }
        }
        return null;
    }
}
