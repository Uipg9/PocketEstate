package com.pocketestate.farm;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Types of crops that can be grown in the virtual garden
 */
public enum CropType {
    WHEAT("Wheat", Items.WHEAT_SEEDS, Items.WHEAT, 8, 5, 100),
    CARROT("Carrot", Items.CARROT, Items.CARROT, 8, 3, 150),
    POTATO("Potato", Items.POTATO, Items.POTATO, 8, 4, 150),
    BEETROOT("Beetroot", Items.BEETROOT_SEEDS, Items.BEETROOT, 4, 2, 200),
    MELON("Melon", Items.MELON_SEEDS, Items.MELON_SLICE, 8, 4, 300),
    PUMPKIN("Pumpkin", Items.PUMPKIN_SEEDS, Items.PUMPKIN, 1, 1, 300),
    NETHER_WART("Nether Wart", Items.NETHER_WART, Items.NETHER_WART, 4, 3, 500);
    
    private final String displayName;
    private final Item seedItem;
    private final Item harvestItem;
    private final int maxGrowthStage;
    private final int baseYield;
    private final long unlockCost;
    
    CropType(String displayName, Item seedItem, Item harvestItem, int maxGrowthStage, int baseYield, long unlockCost) {
        this.displayName = displayName;
        this.seedItem = seedItem;
        this.harvestItem = harvestItem;
        this.maxGrowthStage = maxGrowthStage;
        this.baseYield = baseYield;
        this.unlockCost = unlockCost;
    }
    
    public String getDisplayName() { return displayName; }
    public Item getSeedItem() { return seedItem; }
    public Item getHarvestItem() { return harvestItem; }
    public int getMaxGrowthStage() { return maxGrowthStage; }
    public int getBaseYield() { return baseYield; }
    public long getUnlockCost() { return unlockCost; }
    
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
}
