# 💾 Data Persistence Guide

Complete guide to saving and loading data in Fabric 1.21.11.

---

## Overview

Data persistence options:
1. **In-Memory** - Lost on restart (simplest)
2. **NBT Files** - Saved to world folder (recommended)
3. **Config Files** - JSON/TOML for settings

---

## In-Memory Storage (Simple)

### Basic Pattern

```java
package com.yourname.modname.managers;

import java.util.*;

public class BalanceManager {
    // Store data in memory - LOST ON RESTART
    private static final Map<UUID, Integer> balances = new HashMap<>();
    private static final int DEFAULT_BALANCE = 1000;
    
    public static int getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, DEFAULT_BALANCE);
    }
    
    public static void setBalance(UUID playerId, int amount) {
        balances.put(playerId, Math.max(0, amount));
    }
    
    public static void add(UUID playerId, int amount) {
        setBalance(playerId, getBalance(playerId) + amount);
    }
    
    public static boolean withdraw(UUID playerId, int amount) {
        if (getBalance(playerId) < amount) return false;
        setBalance(playerId, getBalance(playerId) - amount);
        return true;
    }
}
```

### With Player Data Class

```java
public class PlayerDataManager {
    private static final Map<UUID, PlayerData> playerData = new HashMap<>();
    
    public static PlayerData getData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerData());
    }
    
    public static class PlayerData {
        public int balance = 1000;
        public int level = 1;
        public long lastLogin = 0;
        public boolean tutorialComplete = false;
    }
}

// Usage
PlayerData data = PlayerDataManager.getData(player.getUUID());
data.balance += 100;
data.level++;
```

---

## NBT Persistence (Recommended)

### Central DataManager Pattern

```java
package com.yourname.modname.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class DataManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("modname");
    private static final String DATA_FILE = "modname_data.dat";
    
    /**
     * Save all mod data to NBT file
     */
    public static void saveData(MinecraftServer server) {
        try {
            Path savePath = getDataPath(server);
            CompoundTag root = new CompoundTag();
            
            // Collect data from all managers
            root.put("balances", BalanceManager.saveToNBT());
            root.put("players", PlayerDataManager.saveToNBT());
            root.put("config", ConfigManager.saveToNBT());
            // Add more managers as needed
            
            // Write to file
            NbtIo.writeCompressed(root, savePath);
            LOGGER.info("Saved mod data successfully");
            
        } catch (IOException e) {
            LOGGER.error("Failed to save mod data: " + e.getMessage());
        }
    }
    
    /**
     * Load all mod data from NBT file
     */
    public static void loadData(MinecraftServer server) {
        try {
            Path savePath = getDataPath(server);
            File saveFile = savePath.toFile();
            
            if (!saveFile.exists()) {
                LOGGER.info("No save file found, using defaults");
                return;
            }
            
            // Read with NbtAccounter (required in 1.21.11)
            CompoundTag root = NbtIo.readCompressed(
                savePath, 
                NbtAccounter.unlimitedHeap()
            );
            
            if (root == null) return;
            
            // Distribute data to managers
            if (root.contains("balances")) {
                root.getCompound("balances").ifPresent(BalanceManager::loadFromNBT);
            }
            if (root.contains("players")) {
                root.getCompound("players").ifPresent(PlayerDataManager::loadFromNBT);
            }
            if (root.contains("config")) {
                root.getCompound("config").ifPresent(ConfigManager::loadFromNBT);
            }
            
            LOGGER.info("Loaded mod data successfully");
            
        } catch (IOException e) {
            LOGGER.error("Failed to load mod data: " + e.getMessage());
        }
    }
    
    /**
     * Get path to data file in world folder
     */
    private static Path getDataPath(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        return new File(worldDir, DATA_FILE).toPath();
    }
}
```

### Manager with NBT Support

```java
public class BalanceManager {
    private static final Map<UUID, Integer> balances = new HashMap<>();
    private static final int DEFAULT_BALANCE = 1000;
    
    // ... getBalance, setBalance, add, withdraw methods ...
    
    /**
     * Save balances to NBT
     */
    public static CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        
        balances.forEach((uuid, balance) -> {
            tag.putInt(uuid.toString(), balance);
        });
        
        return tag;
    }
    
    /**
     * Load balances from NBT (1.21.11 style)
     */
    public static void loadFromNBT(CompoundTag tag) {
        balances.clear();
        
        for (String key : tag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                // 1.21.11: getInt returns Optional
                tag.getInt(key).ifPresent(balance -> {
                    balances.put(uuid, balance);
                });
            } catch (IllegalArgumentException e) {
                // Skip invalid UUIDs
            }
        }
    }
}
```

### Complex Player Data with NBT

```java
public class PlayerDataManager {
    private static final Map<UUID, PlayerData> playerData = new HashMap<>();
    
    public static class PlayerData {
        public int balance = 1000;
        public int level = 1;
        public long lastLogin = 0;
        public boolean tutorialComplete = false;
        public List<String> unlockedFeatures = new ArrayList<>();
        
        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("balance", balance);
            tag.putInt("level", level);
            tag.putLong("lastLogin", lastLogin);
            tag.putBoolean("tutorialComplete", tutorialComplete);
            
            // Save list as comma-separated string
            tag.putString("unlockedFeatures", String.join(",", unlockedFeatures));
            
            return tag;
        }
        
        public static PlayerData fromNBT(CompoundTag tag) {
            PlayerData data = new PlayerData();
            
            // 1.21.11: All getters return Optional
            data.balance = tag.getInt("balance").orElse(1000);
            data.level = tag.getInt("level").orElse(1);
            data.lastLogin = tag.getLong("lastLogin").orElse(0L);
            data.tutorialComplete = tag.getBoolean("tutorialComplete").orElse(false);
            
            tag.getString("unlockedFeatures").ifPresent(features -> {
                if (!features.isEmpty()) {
                    data.unlockedFeatures = new ArrayList<>(Arrays.asList(features.split(",")));
                }
            });
            
            return data;
        }
    }
    
    public static PlayerData getData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerData());
    }
    
    public static CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        playerData.forEach((uuid, data) -> {
            tag.put(uuid.toString(), data.toNBT());
        });
        return tag;
    }
    
    public static void loadFromNBT(CompoundTag tag) {
        playerData.clear();
        for (String key : tag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                tag.getCompound(key).ifPresent(dataTag -> {
                    playerData.put(uuid, PlayerData.fromNBT(dataTag));
                });
            } catch (IllegalArgumentException e) {
                // Skip invalid entries
            }
        }
    }
}
```

---

## Saving ItemStacks to NBT

### Single ItemStack

```java
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

// Save ItemStack
public static CompoundTag itemStackToNBT(ItemStack stack) {
    CompoundTag tag = new CompoundTag();
    
    if (stack.isEmpty()) {
        tag.putString("id", "minecraft:air");
        tag.putInt("count", 0);
        return tag;
    }
    
    // Get item registry ID
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    tag.putString("id", itemId.toString());
    tag.putInt("count", stack.getCount());
    
    // Save damage if applicable
    if (stack.getDamageValue() > 0) {
        tag.putInt("damage", stack.getDamageValue());
    }
    
    // Save custom NBT if present
    if (stack.hasTag()) {
        tag.put("tag", stack.getTag().copy());
    }
    
    return tag;
}

// Load ItemStack
public static ItemStack itemStackFromNBT(CompoundTag tag) {
    String itemId = tag.getString("id").orElse("minecraft:air");
    ResourceLocation loc = ResourceLocation.tryParse(itemId);
    
    if (loc == null) return ItemStack.EMPTY;
    
    Item item = BuiltInRegistries.ITEM.getValue(loc);
    if (item == null || item == Items.AIR) return ItemStack.EMPTY;
    
    int count = tag.getInt("count").orElse(1);
    ItemStack stack = new ItemStack(item, count);
    
    // Restore damage
    tag.getInt("damage").ifPresent(stack::setDamageValue);
    
    // Restore custom NBT
    tag.getCompound("tag").ifPresent(customTag -> {
        stack.setTag(customTag.copy());
    });
    
    return stack;
}
```

### List of ItemStacks (Output Buffer Pattern)

```java
public class OutputBuffer {
    private final List<ItemStack> items = new ArrayList<>();
    
    public void add(ItemStack stack) {
        if (stack.isEmpty()) return;
        
        // Try to merge with existing stacks
        for (ItemStack existing : items) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int canAdd = existing.getMaxStackSize() - existing.getCount();
                int toAdd = Math.min(canAdd, stack.getCount());
                existing.grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return;
            }
        }
        
        // Add remaining as new stack
        if (!stack.isEmpty()) {
            items.add(stack.copy());
        }
    }
    
    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }
    
    public void clear() {
        items.clear();
    }
    
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        
        for (ItemStack stack : items) {
            list.add(itemStackToNBT(stack));
        }
        
        tag.put("items", list);
        return tag;
    }
    
    public static OutputBuffer fromNBT(CompoundTag tag) {
        OutputBuffer buffer = new OutputBuffer();
        
        tag.getList("items").ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                final int index = i;
                list.getCompound(index).ifPresent(itemTag -> {
                    ItemStack stack = itemStackFromNBT(itemTag);
                    if (!stack.isEmpty()) {
                        buffer.items.add(stack);
                    }
                });
            }
        });
        
        return buffer;
    }
}
```

---

## Server Lifecycle Hooks

### Register Save/Load Events

```java
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

@Override
public void onInitialize() {
    // Load when server starts
    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        DataManager.loadData(server);
        LOGGER.info("Loaded mod data");
    });
    
    // Save when server stops
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
        DataManager.saveData(server);
        LOGGER.info("Saved mod data");
    });
    
    // Auto-save every 5 minutes
    final int[] tickCounter = {0};
    ServerTickEvents.END_SERVER_TICK.register(server -> {
        tickCounter[0]++;
        if (tickCounter[0] >= 6000) {  // 5 minutes
            DataManager.saveData(server);
            tickCounter[0] = 0;
        }
    });
}
```

---

## NBT API Quick Reference (1.21.11)

### Writing to CompoundTag

```java
CompoundTag tag = new CompoundTag();

// Primitives
tag.putInt("intVal", 100);
tag.putLong("longVal", 1000L);
tag.putDouble("doubleVal", 99.5);
tag.putFloat("floatVal", 1.5f);
tag.putBoolean("boolVal", true);
tag.putString("stringVal", "hello");
tag.putByte("byteVal", (byte) 1);
tag.putShort("shortVal", (short) 100);

// UUID (special - uses 2 longs internally)
tag.putUUID("uuid", player.getUUID());

// Nested compound
CompoundTag nested = new CompoundTag();
nested.putString("name", "test");
tag.put("nested", nested);

// List
ListTag list = new ListTag();
list.add(IntTag.valueOf(1));
list.add(IntTag.valueOf(2));
tag.put("intList", list);
```

### Reading from CompoundTag (1.21.11 - Optional Returns!)

```java
// All numeric/string/boolean getters return Optional now!
int intVal = tag.getInt("intVal").orElse(0);
long longVal = tag.getLong("longVal").orElse(0L);
double doubleVal = tag.getDouble("doubleVal").orElse(0.0);
float floatVal = tag.getFloat("floatVal").orElse(0.0f);
boolean boolVal = tag.getBoolean("boolVal").orElse(false);
String stringVal = tag.getString("stringVal").orElse("");

// UUID (still direct access)
UUID uuid = tag.getUUID("uuid");

// Nested compound
CompoundTag nested = tag.getCompound("nested").orElse(new CompoundTag());

// List
ListTag list = tag.getList("intList").orElse(new ListTag());

// Check before access
if (tag.contains("key")) {
    tag.getInt("key").ifPresent(value -> {
        // Use value
    });
}
```

### ListTag Iteration

```java
ListTag list = tag.getList("items").orElse(new ListTag());

for (int i = 0; i < list.size(); i++) {
    final int index = i;
    
    // For compound entries
    list.getCompound(index).ifPresent(itemTag -> {
        // Process itemTag
    });
    
    // For string entries
    // String str = list.getString(i);  // Direct in some cases
}
```

---

## Performance Tips

### 1. Batch Saves
```java
// DON'T save after every small change
public void addMoney(int amount) {
    balance += amount;
    saveData();  // ❌ Too frequent!
}

// DO batch saves at intervals
// Use ServerTickEvents for periodic saves
```

### 2. Use EnumMap for Enum Keys
```java
// Instead of HashMap for enums
private final Map<MyEnum, Data> data = new HashMap<>();

// Use EnumMap (faster)
private final Map<MyEnum, Data> data = new EnumMap<>(MyEnum.class);
```

### 3. Lazy Initialization
```java
public static PlayerData getData(UUID id) {
    return playerData.computeIfAbsent(id, k -> new PlayerData());
}
```

### 4. Only Save Changed Data
```java
public class PlayerData {
    private boolean dirty = false;
    
    public void setBalance(int balance) {
        this.balance = balance;
        this.dirty = true;
    }
    
    public boolean isDirty() { return dirty; }
    public void markClean() { dirty = false; }
}

// In save logic
playerData.forEach((uuid, data) -> {
    if (data.isDirty()) {
        tag.put(uuid.toString(), data.toNBT());
        data.markClean();
    }
});
```

---

## Common Patterns

### Default Values for New Players

```java
public static PlayerData getData(UUID playerId) {
    return playerData.computeIfAbsent(playerId, k -> {
        PlayerData data = new PlayerData();
        // Set defaults
        data.balance = 1000;
        data.level = 1;
        data.tutorialComplete = false;
        return data;
    });
}
```

### Reset Player Data

```java
public static void resetPlayer(UUID playerId) {
    playerData.remove(playerId);
    // Next getData() call will create fresh defaults
}
```

### Backup Before Reset

```java
public static CompoundTag backupPlayer(UUID playerId) {
    PlayerData data = playerData.get(playerId);
    return data != null ? data.toNBT() : null;
}

public static void restorePlayer(UUID playerId, CompoundTag backup) {
    if (backup != null) {
        playerData.put(playerId, PlayerData.fromNBT(backup));
    }
}
```

---

*Data persistence is critical - test save/load thoroughly before release!*
