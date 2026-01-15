package com.pocketestate.data;

import com.pocketestate.farm.CropPlot;
import com.pocketestate.farm.MobPen;
import com.pocketestate.farm.CropType;
import com.pocketestate.farm.PenType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Stores all data for a single player's Pocket Estate
 * Updated with idle-game mechanics: compost, auto-harvest, XP/money rewards
 */
public class PlayerData {
    private long balance = 1000; // Starting balance

    // Crop fields (indexed 0-179 for 180 plots across 20 pages)
    private final List<CropPlot> cropPlots = new ArrayList<>();
    private int unlockedCropSlots = 3; // Start with 3 unlocked

    // Mob pens (by type)
    private final Map<PenType, MobPen> mobPens = new EnumMap<>(PenType.class);

    // Output buffers for collecting resources
    private final List<ItemStack> outputBuffer = new ArrayList<>(); // Mob pen output
    private final List<ItemStack> cropOutput = new ArrayList<>(); // Crop harvest output
    
    // Compost Bin
    private int compostProgress = 0; // 0-100, produces bonemeal at 100
    private int compostResources = 0; // Adds speed to composting
    private int storedBonemeal = 0;
    
    // Auto-harvest settings
    private boolean autoHarvestEnabled = false;
    private boolean autoFeedPensEnabled = false; // Pens can take from farms
    
    // Stats
    private long totalCropsHarvested = 0;
    private long totalXpEarned = 0;
    private long totalMoneyEarned = 0;

    // Maximum number of crop plots (20 pages of 9)
    public static final int MAX_CROP_PLOTS = 180;
    public static final int PLOTS_PER_PAGE = 9;

    // Item ID lookup map for NBT serialization
    private static final Map<String, Item> ITEM_LOOKUP = new HashMap<>();
    private static final Map<Item, String> ITEM_REVERSE = new IdentityHashMap<>();
    
    static {
        // Common items used in estate
        registerItem("wheat", Items.WHEAT);
        registerItem("wheat_seeds", Items.WHEAT_SEEDS);
        registerItem("carrot", Items.CARROT);
        registerItem("potato", Items.POTATO);
        registerItem("beetroot", Items.BEETROOT);
        registerItem("beetroot_seeds", Items.BEETROOT_SEEDS);
        registerItem("melon_slice", Items.MELON_SLICE);
        registerItem("melon_seeds", Items.MELON_SEEDS);
        registerItem("pumpkin", Items.PUMPKIN);
        registerItem("pumpkin_seeds", Items.PUMPKIN_SEEDS);
        registerItem("nether_wart", Items.NETHER_WART);
        registerItem("bone_meal", Items.BONE_MEAL);
        // Mob drops
        registerItem("white_wool", Items.WHITE_WOOL);
        registerItem("mutton", Items.MUTTON);
        registerItem("leather", Items.LEATHER);
        registerItem("beef", Items.BEEF);
        registerItem("feather", Items.FEATHER);
        registerItem("egg", Items.EGG);
        registerItem("chicken", Items.CHICKEN);
        registerItem("string", Items.STRING);
        registerItem("spider_eye", Items.SPIDER_EYE);
        registerItem("rotten_flesh", Items.ROTTEN_FLESH);
        registerItem("iron_ingot", Items.IRON_INGOT);
        registerItem("bone", Items.BONE);
        registerItem("arrow", Items.ARROW);
        registerItem("poppy", Items.POPPY);
    }
    
    private static void registerItem(String id, Item item) {
        ITEM_LOOKUP.put(id, item);
        ITEM_REVERSE.put(item, id);
    }
    
    private static String getItemId(Item item) {
        return ITEM_REVERSE.getOrDefault(item, "unknown");
    }
    
    private static Item getItemById(String id) {
        return ITEM_LOOKUP.getOrDefault(id, Items.AIR);
    }

    public PlayerData() {
        // Initialize 180 empty crop plots (20 pages of 9)
        for (int i = 0; i < MAX_CROP_PLOTS; i++) {
            cropPlots.add(new CropPlot());
        }
    }

    // Balance methods
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = Math.max(0, balance); }
    public void addBalance(long amount) { this.balance += amount; }

    // Crop plot methods
    public List<CropPlot> getCropPlots() { return cropPlots; }
    public CropPlot getCropPlot(int index) {
        if (index >= 0 && index < cropPlots.size()) {
            return cropPlots.get(index);
        }
        return null;
    }

    public int getUnlockedCropSlots() { return unlockedCropSlots; }
    public void setUnlockedCropSlots(int slots) { this.unlockedCropSlots = Math.min(MAX_CROP_PLOTS, slots); }

    public boolean isCropSlotUnlocked(int index) {
        return index < unlockedCropSlots;
    }
    
    // Compost methods
    public int getCompostProgress() { return compostProgress; }
    public int getStoredBonemeal() { return storedBonemeal; }
    public int getCompostResources() { return compostResources; }
    
    public void addStoredBonemeal(int amount) {
        this.storedBonemeal += amount;
    }
    
    public void addCompostResource(int amount) {
        this.compostResources += amount;
    }
    
    public void tickCompost() {
        // Base rate: 1 progress per tick, +1 per resource (max 10)
        int rate = 1 + Math.min(compostResources, 10);
        compostProgress += rate;
        
        if (compostProgress >= 100) {
            storedBonemeal++;
            compostProgress = 0;
            if (compostResources > 0) compostResources--;
        }
    }
    
    public boolean useBonemeal(int amount) {
        if (storedBonemeal >= amount) {
            storedBonemeal -= amount;
            return true;
        }
        return false;
    }
    
    // Auto-harvest
    public boolean isAutoHarvestEnabled() { return autoHarvestEnabled; }
    public void setAutoHarvestEnabled(boolean enabled) { this.autoHarvestEnabled = enabled; }
    public boolean isAutoFeedPensEnabled() { return autoFeedPensEnabled; }
    public void setAutoFeedPensEnabled(boolean enabled) { this.autoFeedPensEnabled = enabled; }
    
    // Stats
    public long getTotalCropsHarvested() { return totalCropsHarvested; }
    public long getTotalXpEarned() { return totalXpEarned; }
    public long getTotalMoneyEarned() { return totalMoneyEarned; }
    public void addCropsHarvested(int amount) { this.totalCropsHarvested += amount; }
    public void addXpEarned(int amount) { this.totalXpEarned += amount; }
    public void addMoneyEarned(long amount) { this.totalMoneyEarned += amount; }

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

    // Output buffer methods (for mob pens)
    public List<ItemStack> getOutputBuffer() { return outputBuffer; }

    public void addToOutput(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            // Try to stack with existing items
            for (ItemStack existing : outputBuffer) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    int toAdd = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) return;
                }
            }
            // Add remainder as new stack
            if (!stack.isEmpty()) {
                outputBuffer.add(stack.copy());
            }
        }
    }

    public void clearOutput() {
        outputBuffer.clear();
    }
    
    // Crop output buffer methods
    public List<ItemStack> getCropOutput() { return cropOutput; }
    
    public void addToCropOutput(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            // Try to stack with existing items
            for (ItemStack existing : cropOutput) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    int toAdd = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) return;
                }
            }
            // Add remainder as new stack
            if (!stack.isEmpty()) {
                cropOutput.add(stack.copy());
            }
        }
    }
    
    public void clearCropOutput() {
        cropOutput.clear();
    }
    
    public int getCropOutputCount() {
        return cropOutput.stream().mapToInt(ItemStack::getCount).sum();
    }

    // NBT serialization
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("balance", balance);
        tag.putInt("unlockedCropSlots", unlockedCropSlots);
        
        // Compost data
        tag.putInt("compostProgress", compostProgress);
        tag.putInt("compostResources", compostResources);
        tag.putInt("storedBonemeal", storedBonemeal);
        
        // Auto settings
        tag.putBoolean("autoHarvest", autoHarvestEnabled);
        tag.putBoolean("autoFeedPens", autoFeedPensEnabled);
        
        // Stats
        tag.putLong("totalCropsHarvested", totalCropsHarvested);
        tag.putLong("totalXpEarned", totalXpEarned);
        tag.putLong("totalMoneyEarned", totalMoneyEarned);

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
                CompoundTag itemTag = new CompoundTag();
                String itemId = getItemId(stack.getItem());
                itemTag.putString("id", itemId);
                itemTag.putInt("count", stack.getCount());
                outputTag.add(itemTag);
            }
        }
        tag.put("outputBuffer", outputTag);
        
        // Save crop output buffer
        ListTag cropOutputTag = new ListTag();
        for (ItemStack stack : cropOutput) {
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                String itemId = getItemId(stack.getItem());
                itemTag.putString("id", itemId);
                itemTag.putInt("count", stack.getCount());
                cropOutputTag.add(itemTag);
            }
        }
        tag.put("cropOutputBuffer", cropOutputTag);

        return tag;
    }

    public static PlayerData fromNBT(CompoundTag tag) {
        PlayerData data = new PlayerData();

        data.balance = tag.getLong("balance").orElse(1000L);
        data.unlockedCropSlots = tag.getInt("unlockedCropSlots").orElse(3);
        
        // Compost data
        data.compostProgress = tag.getInt("compostProgress").orElse(0);
        data.compostResources = tag.getInt("compostResources").orElse(0);
        data.storedBonemeal = tag.getInt("storedBonemeal").orElse(0);
        
        // Auto settings
        data.autoHarvestEnabled = tag.getBoolean("autoHarvest").orElse(false);
        data.autoFeedPensEnabled = tag.getBoolean("autoFeedPens").orElse(false);
        
        // Stats
        data.totalCropsHarvested = tag.getLong("totalCropsHarvested").orElse(0L);
        data.totalXpEarned = tag.getLong("totalXpEarned").orElse(0L);
        data.totalMoneyEarned = tag.getLong("totalMoneyEarned").orElse(0L);

        // Load crop plots
        if (tag.contains("cropPlots")) {
            ListTag cropsTag = tag.getList("cropPlots").orElse(new ListTag());
            for (int i = 0; i < Math.min(cropsTag.size(), MAX_CROP_PLOTS); i++) {
                final int slotIndex = i;
                cropsTag.getCompound(i).ifPresent(plotTag -> {
                    if (slotIndex < data.cropPlots.size()) {
                        data.cropPlots.set(slotIndex, CropPlot.fromNBT(plotTag));
                    }
                });
            }
        }

        // Load mob pens
        if (tag.contains("mobPens")) {
            tag.getCompound("mobPens").ifPresent(pensTag -> {
                for (PenType type : PenType.values()) {
                    if (pensTag.contains(type.name())) {
                        pensTag.getCompound(type.name()).ifPresent(penTag -> {
                            data.mobPens.put(type, MobPen.fromNBT(penTag, type));
                        });
                    }
                }
            });
        }

        // Load output buffer  
        if (tag.contains("outputBuffer")) {
            ListTag outputTag = tag.getList("outputBuffer").orElse(new ListTag());
            for (int i = 0; i < outputTag.size(); i++) {
                outputTag.getCompound(i).ifPresent(itemTag -> {
                    String id = itemTag.getString("id").orElse("");
                    int count = itemTag.getInt("count").orElse(1);
                    if (!id.isEmpty()) {
                        Item item = getItemById(id);
                        if (item != Items.AIR) {
                            data.outputBuffer.add(new ItemStack(item, count));
                        }
                    }
                });
            }
        }
        
        // Load crop output buffer  
        if (tag.contains("cropOutputBuffer")) {
            ListTag cropOutputTag = tag.getList("cropOutputBuffer").orElse(new ListTag());
            for (int i = 0; i < cropOutputTag.size(); i++) {
                cropOutputTag.getCompound(i).ifPresent(itemTag -> {
                    String id = itemTag.getString("id").orElse("");
                    int count = itemTag.getInt("count").orElse(1);
                    if (!id.isEmpty()) {
                        Item item = getItemById(id);
                        if (item != Items.AIR) {
                            data.cropOutput.add(new ItemStack(item, count));
                        }
                    }
                });
            }
        }

        return data;
    }
}
