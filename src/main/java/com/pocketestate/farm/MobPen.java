package com.pocketestate.farm;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;

/**
 * Represents a mob pen in the virtual menagerie
 */
public class MobPen {
    private final PenType penType;
    private ItemStack toolSlot;
    private int fodderAmount;
    private int productionProgress; // 0-100
    private int totalProduced;
    
    // For Iron Golem foundry - requires initial iron block investment
    private boolean isConstructed;
    private int ironBlocksInvested;
    private static final int IRON_BLOCKS_REQUIRED = 4;
    
    private static final Random random = new Random();
    
    public MobPen(PenType type) {
        this.penType = type;
        this.toolSlot = ItemStack.EMPTY;
        this.fodderAmount = 0;
        this.productionProgress = 0;
        this.totalProduced = 0;
        this.isConstructed = type != PenType.IRON_GOLEM; // Only Iron Golem needs construction
        this.ironBlocksInvested = 0;
    }
    
    // Getters
    public PenType getPenType() { return penType; }
    public ItemStack getToolSlot() { return toolSlot; }
    public int getFodderAmount() { return fodderAmount; }
    public int getProductionProgress() { return productionProgress; }
    public int getTotalProduced() { return totalProduced; }
    public boolean isConstructed() { return isConstructed; }
    public int getIronBlocksInvested() { return ironBlocksInvested; }
    public int getIronBlocksRequired() { return IRON_BLOCKS_REQUIRED; }
    
    /**
     * Set the tool in the tool slot
     */
    public void setTool(ItemStack tool) {
        this.toolSlot = tool.copy();
    }
    
    /**
     * Remove and return the tool from the slot
     */
    public ItemStack removeTool() {
        ItemStack tool = this.toolSlot;
        this.toolSlot = ItemStack.EMPTY;
        return tool;
    }
    
    /**
     * Add fodder to the pen
     * @return amount actually added
     */
    public int addFodder(int amount) {
        int maxFodder = 640; // 10 stacks worth
        int toAdd = Math.min(amount, maxFodder - fodderAmount);
        fodderAmount += toAdd;
        return toAdd;
    }
    
    /**
     * Invest iron blocks into the foundry
     * @return true if fully constructed
     */
    public boolean investIronBlock() {
        if (penType != PenType.IRON_GOLEM) return false;
        if (isConstructed) return true;
        
        ironBlocksInvested++;
        if (ironBlocksInvested >= IRON_BLOCKS_REQUIRED) {
            isConstructed = true;
        }
        return isConstructed;
    }
    
    /**
     * Check if the pen can produce this cycle
     */
    public boolean canProduce() {
        if (!isConstructed) return false;
        
        // Iron Golem doesn't need fodder
        if (penType == PenType.IRON_GOLEM) return true;
        
        // Check fodder
        if (penType.getFodderItem() != null && fodderAmount < penType.getFodderPerCycle()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if a tool is required for any loot
     */
    public boolean needsToolForLoot() {
        return penType.getLootTable().stream()
            .anyMatch(loot -> loot.requiredTool() != null);
    }
    
    /**
     * Process one production cycle
     * @return the items produced (can be empty)
     */
    public java.util.List<ItemStack> produce() {
        java.util.List<ItemStack> produced = new java.util.ArrayList<>();
        
        if (!canProduce()) return produced;
        
        // Consume fodder
        if (penType.getFodderItem() != null) {
            fodderAmount = Math.max(0, fodderAmount - penType.getFodderPerCycle());
        }
        
        // Process each loot entry
        for (PenType.LootEntry loot : penType.getLootTable()) {
            // Check if tool is required
            if (loot.requiredTool() != null) {
                if (toolSlot.isEmpty() || !isValidTool(toolSlot, loot.requiredTool())) {
                    continue; // Skip this loot - no valid tool
                }
                
                // Consume tool durability
                if (loot.consumesTool() && toolSlot.isDamageableItem()) {
                    // Damage the tool
                    int newDamage = toolSlot.getDamageValue() + 1;
                    toolSlot.setDamageValue(newDamage);
                    
                    // Check if tool broke
                    if (newDamage >= toolSlot.getMaxDamage()) {
                        toolSlot = ItemStack.EMPTY;
                    }
                }
            }
            
            // Calculate yield
            int count = loot.minCount();
            if (loot.maxCount() > loot.minCount()) {
                count += random.nextInt(loot.maxCount() - loot.minCount() + 1);
            }
            
            if (count > 0) {
                produced.add(new ItemStack(loot.item(), count));
                totalProduced += count;
            }
        }
        
        return produced;
    }
    
    /**
     * Check if an item is a valid tool for the required type
     */
    private boolean isValidTool(ItemStack stack, net.minecraft.world.item.Item required) {
        if (required == Items.SHEARS) {
            return stack.is(Items.SHEARS);
        }
        if (required == Items.IRON_SWORD) {
            // Accept any sword
            return stack.is(Items.WOODEN_SWORD) || 
                   stack.is(Items.STONE_SWORD) ||
                   stack.is(Items.IRON_SWORD) ||
                   stack.is(Items.GOLDEN_SWORD) ||
                   stack.is(Items.DIAMOND_SWORD) ||
                   stack.is(Items.NETHERITE_SWORD);
        }
        return stack.is(required);
    }
    
    // NBT serialization
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("penType", penType.name());
        tag.putInt("fodder", fodderAmount);
        tag.putInt("progress", productionProgress);
        tag.putInt("totalProduced", totalProduced);
        tag.putBoolean("constructed", isConstructed);
        tag.putInt("ironBlocks", ironBlocksInvested);
        
        if (!toolSlot.isEmpty()) {
            CompoundTag toolTag = new CompoundTag();
            toolTag.putString("id", BuiltInRegistries.ITEM.getKey(toolSlot.getItem()).toString());
            toolTag.putInt("count", toolSlot.getCount());
            if (toolSlot.getDamageValue() > 0) {
                toolTag.putInt("damage", toolSlot.getDamageValue());
            }
            tag.put("tool", toolTag);
        }
        
        return tag;
    }
    
    public static MobPen fromNBT(CompoundTag tag, PenType type) {
        MobPen pen = new MobPen(type);
        pen.fodderAmount = tag.getInt("fodder").orElse(0);
        pen.productionProgress = tag.getInt("progress").orElse(0);
        pen.totalProduced = tag.getInt("totalProduced").orElse(0);
        pen.isConstructed = tag.getBoolean("constructed").orElse(type != PenType.IRON_GOLEM);
        pen.ironBlocksInvested = tag.getInt("ironBlocks").orElse(0);
        
        // Tool loading would need registry access - handled at use time
        
        return pen;
    }
}
