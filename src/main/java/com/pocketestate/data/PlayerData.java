package com.pocketestate.data;

import com.pocketestate.farm.CropPlot;
import com.pocketestate.farm.MobPen;
import com.pocketestate.farm.CropType;
import com.pocketestate.farm.PenType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Stores all data for a single player's Pocket Estate
 */
public class PlayerData {
    private long balance = 1000; // Starting balance
    
    // Crop fields (indexed 0-8 for 9 plots)
    private final List<CropPlot> cropPlots = new ArrayList<>();
    private int unlockedCropSlots = 1; // Start with 1 unlocked
    
    // Mob pens (by type)
    private final Map<PenType, MobPen> mobPens = new EnumMap<>(PenType.class);
    
    // Output buffers for collecting resources
    private final List<ItemStack> outputBuffer = new ArrayList<>();
    
    public PlayerData() {
        // Initialize 9 empty crop plots
        for (int i = 0; i < 9; i++) {
            cropPlots.add(new CropPlot());
        }
    }
    
    // Balance methods
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = Math.max(0, balance); }
    
    // Crop plot methods
    public List<CropPlot> getCropPlots() { return cropPlots; }
    public CropPlot getCropPlot(int index) {
        if (index >= 0 && index < cropPlots.size()) {
            return cropPlots.get(index);
        }
        return null;
    }
    
    public int getUnlockedCropSlots() { return unlockedCropSlots; }
    public void setUnlockedCropSlots(int slots) { this.unlockedCropSlots = Math.min(9, slots); }
    
    public boolean isCropSlotUnlocked(int index) {
        return index < unlockedCropSlots;
    }
    
    // Mob pen methods
    public Map<PenType, MobPen> getMobPens() { return mobPens; }
    
    public MobPen getMobPen(PenType type) {
        return mobPens.get(type);
    }
    
    public boolean hasMobPen(PenType type) {
        return mobPens.containsKey(type);
    }
    
    public void unlockMobPen(PenType type) {
        if (!mobPens.containsKey(type)) {
            mobPens.put(type, new MobPen(type));
        }
    }
    
    // Output buffer methods
    public List<ItemStack> getOutputBuffer() { return outputBuffer; }
    
    public void addToOutput(ItemStack stack) {
        // Try to merge with existing stacks first
        for (ItemStack existing : outputBuffer) {
            if (ItemStack.isSameItemSameComponents(existing, stack) && 
                existing.getCount() < existing.getMaxStackSize()) {
                int toAdd = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                existing.grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return;
            }
        }
        // Add remaining as new stack
        if (!stack.isEmpty()) {
            outputBuffer.add(stack.copy());
        }
    }
    
    public void clearOutput() {
        outputBuffer.clear();
    }
    
    // NBT serialization
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("balance", balance);
        tag.putInt("unlockedCropSlots", unlockedCropSlots);
        
        // Save crop plots
        ListTag cropsTag = new ListTag();
        for (CropPlot plot : cropPlots) {
            cropsTag.add(plot.toNBT());
        }
        tag.put("cropPlots", cropsTag);
        
        // Save mob pens
        CompoundTag pensTag = new CompoundTag();
        for (Map.Entry<PenType, MobPen> entry : mobPens.entrySet()) {
            pensTag.put(entry.getKey().name(), entry.getValue().toNBT());
        }
        tag.put("mobPens", pensTag);
        
        // Save output buffer
        ListTag outputTag = new ListTag();
        for (ItemStack stack : outputBuffer) {
            if (!stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                // Get the item's registry key through BuiltInRegistries
                stackTag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                stackTag.putInt("count", stack.getCount());
                outputTag.add(stackTag);
            }
        }
        tag.put("outputBuffer", outputTag);
        
        return tag;
    }
    
    public static PlayerData fromNBT(CompoundTag tag) {
        PlayerData data = new PlayerData();
        
        data.balance = tag.getLong("balance").orElse(1000L);
        data.unlockedCropSlots = tag.getInt("unlockedCropSlots").orElse(1);
        
        // Load crop plots
        if (tag.contains("cropPlots")) {
            ListTag cropsTag = tag.getList("cropPlots").orElse(new ListTag());
            for (int i = 0; i < Math.min(cropsTag.size(), 9); i++) {
                cropsTag.getCompound(i).ifPresent(plotTag -> {
                    // Add to first available slot
                    for (int j = 0; j < 9; j++) {
                        if (data.cropPlots.get(j) == null) {
                            data.cropPlots.set(j, CropPlot.fromNBT(plotTag));
                            break;
                        }
                    }
                });
            }
        }
        
        // Load mob pens
        if (tag.contains("mobPens")) {
            CompoundTag pensTag = tag.getCompound("mobPens").orElse(new CompoundTag());
            for (String key : pensTag.keySet()) {
                try {
                    PenType type = PenType.valueOf(key);
                    pensTag.getCompound(key).ifPresent(penTag -> {
                        data.mobPens.put(type, MobPen.fromNBT(penTag, type));
                    });
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        // Load output buffer - deferred until we have registry access
        // Items will be loaded on first access
        
        return data;
    }
}
