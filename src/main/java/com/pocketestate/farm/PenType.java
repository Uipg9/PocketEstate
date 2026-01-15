package com.pocketestate.farm;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Types of mob pens available in the virtual menagerie
 */
public enum PenType {
    // Passive mobs - The Pasture (tools no longer required - included in price!)
    SHEEP("Sheep Pasture", Category.PASTURE, EntityType.SHEEP, 
          5000, Items.WHEAT, 10,
          List.of(new LootEntry(Items.WHITE_WOOL, 1, 3, null, false),
                  new LootEntry(Items.MUTTON, 1, 2, null, false))),
    
    COW("Cattle Ranch", Category.PASTURE, EntityType.COW,
        7500, Items.WHEAT, 10,
        List.of(new LootEntry(Items.LEATHER, 1, 2, null, false),
                new LootEntry(Items.BEEF, 1, 3, null, false))),
    
    CHICKEN("Chicken Coop", Category.PASTURE, EntityType.CHICKEN,
            3000, Items.WHEAT_SEEDS, 5,
            List.of(new LootEntry(Items.FEATHER, 0, 2, null, false),
                    new LootEntry(Items.CHICKEN, 1, 1, null, false),
                    new LootEntry(Items.EGG, 1, 1, null, false))),
    
    // Hostile mobs - The Dungeon (tools no longer required - included in price!)
    SPIDER("Spider Dungeon", Category.DUNGEON, EntityType.SPIDER,
           10000, Items.ROTTEN_FLESH, 20,
           List.of(new LootEntry(Items.STRING, 0, 2, null, false),
                   new LootEntry(Items.SPIDER_EYE, 0, 1, null, false))),
    
    ZOMBIE("Zombie Dungeon", Category.DUNGEON, EntityType.ZOMBIE,
           10000, Items.ROTTEN_FLESH, 20,
           List.of(new LootEntry(Items.ROTTEN_FLESH, 0, 2, null, false),
                   new LootEntry(Items.IRON_INGOT, 0, 1, null, false))),
    
    SKELETON("Skeleton Dungeon", Category.DUNGEON, EntityType.SKELETON,
             12000, Items.BONE, 25,
             List.of(new LootEntry(Items.BONE, 0, 2, null, false),
                     new LootEntry(Items.ARROW, 0, 2, null, false))),
    
    // Special - The Foundry
    IRON_GOLEM("Iron Foundry", Category.FOUNDRY, EntityType.IRON_GOLEM,
               100000, null, 0, // No fodder needed, but requires iron blocks to build
               List.of(new LootEntry(Items.IRON_INGOT, 3, 5, null, false),
                       new LootEntry(Items.POPPY, 0, 2, null, false)));
    
    public enum Category {
        PASTURE("§aThe Pasture", "§7Peaceful mob farms"),
        DUNGEON("§cThe Dungeon", "§7Hostile mob farms"),
        FOUNDRY("§6The Foundry", "§7Industrial production");
        
        private final String displayName;
        private final String description;
        
        Category(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Represents a possible loot drop from a mob pen
     */
    public record LootEntry(
        Item item,
        int minCount,
        int maxCount,
        Item requiredTool,  // null = no tool required
        boolean consumesTool // Does this consume tool durability?
    ) {}
    
    private final String displayName;
    private final Category category;
    private final EntityType<?> entityType;
    private final long unlockCost;
    private final Item fodderItem;        // What to feed to produce resources
    private final int fodderPerCycle;     // How much fodder per production cycle
    private final List<LootEntry> lootTable;
    
    PenType(String displayName, Category category, EntityType<?> entityType,
            long unlockCost, Item fodderItem, int fodderPerCycle,
            List<LootEntry> lootTable) {
        this.displayName = displayName;
        this.category = category;
        this.entityType = entityType;
        this.unlockCost = unlockCost;
        this.fodderItem = fodderItem;
        this.fodderPerCycle = fodderPerCycle;
        this.lootTable = lootTable;
    }
    
    public String getDisplayName() { return displayName; }
    public Category getCategory() { return category; }
    public EntityType<?> getEntityType() { return entityType; }
    public long getUnlockCost() { return unlockCost; }
    public Item getFodderItem() { return fodderItem; }
    public int getFodderPerCycle() { return fodderPerCycle; }
    public List<LootEntry> getLootTable() { return lootTable; }
    
    /**
     * Check if this pen requires a specific tool for any of its loot
     */
    public boolean requiresTool() {
        return lootTable.stream().anyMatch(l -> l.requiredTool() != null);
    }
    
    /**
     * Get the display item for the pen (spawn egg style)
     */
    public Item getDisplayItem() {
        return switch (this) {
            case SHEEP -> Items.WHITE_WOOL;
            case COW -> Items.LEATHER;
            case CHICKEN -> Items.EGG;
            case SPIDER -> Items.STRING;
            case ZOMBIE -> Items.ROTTEN_FLESH;
            case SKELETON -> Items.BONE;
            case IRON_GOLEM -> Items.IRON_BLOCK;
        };
    }
}
