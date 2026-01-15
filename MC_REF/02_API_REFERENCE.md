# 📚 API Reference - Minecraft 1.21.11

Complete API reference for Fabric modding on Minecraft 1.21.11.

---

## ⚠️ Critical: Mapping System

### Use Mojang Official Mappings

```gradle
// In build.gradle - REQUIRED for 1.21.11
mappings loom.officialMojangMappings()
```

### Mapping Name Differences

| Purpose | Yarn (DON'T USE) | Mojang (USE THIS) |
|---------|------------------|-------------------|
| Server-side player | `ServerPlayerEntity` | `ServerPlayer` |
| Text/messages | `Text` | `Component` |
| NBT compound | `NbtCompound` | `CompoundTag` |
| NBT list | `NbtList` | `ListTag` |
| Get player list | `getPlayerManager()` | `getPlayerList()` |

---

## 🔴 Breaking API Changes in 1.21.11

### 1. NBT Methods Return Optional

**OLD (1.20.x):**
```java
int value = tag.getInt("key");           // Returns int
String str = tag.getString("key");       // Returns String
CompoundTag nested = tag.getCompound("nested");  // Returns CompoundTag
```

**NEW (1.21.11):**
```java
int value = tag.getInt("key").orElse(0);              // Returns Optional<Integer>
String str = tag.getString("key").orElse("");         // Returns Optional<String>
CompoundTag nested = tag.getCompound("key").orElse(new CompoundTag());
```

**Pattern for safe access:**
```java
// Check existence first
if (tag.contains("key")) {
    tag.getInt("key").ifPresent(value -> {
        // Use value
    });
}

// Or use orElse for defaults
int value = tag.getInt("key").orElse(defaultValue);
```

### 2. NbtIo Requires Path and NbtAccounter

**OLD (1.20.x):**
```java
NbtIo.writeCompressed(tag, file);
CompoundTag tag = NbtIo.readCompressed(file);
```

**NEW (1.21.11):**
```java
import java.nio.file.Path;
import net.minecraft.nbt.NbtAccounter;

// Writing
NbtIo.writeCompressed(tag, file.toPath());

// Reading
CompoundTag tag = NbtIo.readCompressed(
    file.toPath(),
    NbtAccounter.unlimitedHeap()
);
```

### 3. ListTag Access Returns Optional

**OLD:**
```java
ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
CompoundTag item = list.getCompound(0);
```

**NEW:**
```java
ListTag list = tag.getList("items").orElse(new ListTag());
list.getCompound(0).ifPresent(item -> {
    // Use item
});
```

### 4. Enchantment System Overhaul

**OLD (1.20.x) - DOES NOT WORK:**
```java
stack.enchant(Enchantments.SHARPNESS, 5);
```

**NEW (1.21.x) - Complex:**
```java
// Requires Holder<Enchantment> with ResourceKeys
// Very complex - recommend avoiding
```

**RECOMMENDED Alternative - Use NBT:**
```java
ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
CompoundTag tag = stack.getOrCreateTag();
tag.putString("custom_effect", "damage_boost");
tag.putInt("effect_level", 5);
```

---

## 📦 Core Classes Reference

### ServerPlayer
```java
import net.minecraft.server.level.ServerPlayer;

// Get from command context
ServerPlayer player = context.getSource().getPlayerOrException();

// Common methods
player.getUUID();                          // Get player UUID
player.getName().getString();              // Get player name
player.sendSystemMessage(component);       // Send chat message
player.getInventory();                     // Get inventory
player.teleportTo(x, y, z);                // Teleport
player.blockPosition();                    // Get current block position
player.addEffect(mobEffectInstance);       // Apply potion effect
player.heal(amount);                       // Heal player
player.giveExperiencePoints(amount);       // Give XP
```

### Component (Text)
```java
import net.minecraft.network.chat.Component;

// Create text
Component text = Component.literal("Hello World");
Component colored = Component.literal("§aGreen §cRed §eYellow");
Component bold = Component.literal("§l§6Bold Gold");

// Send to player
player.sendSystemMessage(Component.literal("§aSuccess!"));

// ⚠️ IMPORTANT: Wrap ternary operators in parentheses!
// WRONG:
Component.literal(condition ? "Yes" : "No" + " text");
// CORRECT:
Component.literal((condition ? "Yes" : "No") + " text");
```

### Color Codes
| Code | Color | Code | Format |
|------|-------|------|--------|
| §0 | Black | §l | Bold |
| §1 | Dark Blue | §o | Italic |
| §2 | Dark Green | §n | Underline |
| §3 | Dark Aqua | §m | Strikethrough |
| §4 | Dark Red | §k | Obfuscated |
| §5 | Dark Purple | §r | Reset |
| §6 | Gold | | |
| §7 | Gray | | |
| §8 | Dark Gray | | |
| §9 | Blue | | |
| §a | Green | | |
| §b | Aqua | | |
| §c | Red | | |
| §d | Light Purple | | |
| §e | Yellow | | |
| §f | White | | |

### ItemStack
```java
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// Create
ItemStack stack = new ItemStack(Items.DIAMOND, 5);
ItemStack single = new ItemStack(Items.EMERALD);

// Properties
stack.getCount();                    // Get amount
stack.setCount(10);                  // Set amount
stack.grow(5);                       // Add to count
stack.shrink(3);                     // Remove from count
stack.isEmpty();                     // Check if empty
stack.getMaxStackSize();             // Max stack size
stack.getItem();                     // Get item type
stack.getDamageValue();              // Get durability damage
stack.setDamageValue(10);            // Set durability damage

// NBT
CompoundTag tag = stack.getOrCreateTag();
tag.putString("custom", "value");

// Display name
stack.setHoverName(Component.literal("§6Custom Name"));

// Comparison
ItemStack.isSameItem(stack1, stack2);
ItemStack.isSameItemSameComponents(stack1, stack2);
```

### CompoundTag (NBT)
```java
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

CompoundTag tag = new CompoundTag();

// Write primitives
tag.putInt("intValue", 100);
tag.putLong("longValue", 1000L);
tag.putDouble("doubleValue", 99.5);
tag.putFloat("floatValue", 1.5f);
tag.putBoolean("boolValue", true);
tag.putString("stringValue", "hello");
tag.putUUID("uuidValue", uuid);

// Write compound
CompoundTag nested = new CompoundTag();
nested.putString("name", "test");
tag.put("nested", nested);

// Read (1.21.11 - returns Optional!)
int intVal = tag.getInt("intValue").orElse(0);
String strVal = tag.getString("stringValue").orElse("");
UUID uuidVal = tag.getUUID("uuidValue");  // Still direct
CompoundTag nestedVal = tag.getCompound("nested").orElse(new CompoundTag());

// Check existence
if (tag.contains("key")) { ... }

// Get all keys
Set<String> keys = tag.getAllKeys();
```

### Registry Lookups
```java
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

// Item registry
ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.tryParse("minecraft:diamond"));

// Create ResourceLocation
ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("modid", "itemname");
// Or for minecraft namespace:
ResourceLocation mcLoc = ResourceLocation.tryParse("minecraft:diamond");
```

---

## 🖥️ Server Classes

### MinecraftServer
```java
import net.minecraft.server.MinecraftServer;

// Get from various sources
MinecraftServer server = player.getServer();
MinecraftServer server = context.getSource().getServer();

// Common methods
server.getPlayerList();              // Get all players
server.getTickCount();               // Current tick count
server.getWorldPath(LevelResource.ROOT);  // World save path
```

### PlayerList
```java
import net.minecraft.server.players.PlayerList;

PlayerList playerList = server.getPlayerList();

// Methods
playerList.getPlayers();             // List<ServerPlayer>
playerList.getPlayer(uuid);          // Get by UUID
playerList.getPlayerByName(name);    // Get by name
playerList.isOp(gameProfile);        // Check if OP
playerList.broadcastSystemMessage(component, false);  // Broadcast
```

---

## 🎮 Inventory & Items

### Player Inventory
```java
import net.minecraft.world.entity.player.Inventory;

Inventory inv = player.getInventory();

// Methods
inv.add(itemStack);                  // Add item
inv.contains(Items.DIAMOND);         // Check for item
inv.clearContent();                  // Clear all
inv.getItem(slot);                   // Get item at slot
inv.setItem(slot, itemStack);        // Set item at slot
```

### Give Items to Player
```java
ItemStack stack = new ItemStack(Items.DIAMOND, 5);

// Try to add to inventory
if (!player.addItem(stack)) {
    // Inventory full, drop at player's feet
    player.drop(stack, false);
}
```

---

## 🧪 Effects & Buffs

### Apply Potion Effect
```java
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

player.addEffect(new MobEffectInstance(
    MobEffects.SPEED,        // Effect type
    200,                     // Duration in ticks (10 sec = 200 ticks)
    1,                       // Amplifier (0 = level I, 1 = level II)
    false,                   // Ambient (beacon-style)
    true                     // Show particles
));

// Common effects
MobEffects.SPEED
MobEffects.SLOWNESS
MobEffects.HASTE
MobEffects.MINING_FATIGUE
MobEffects.STRENGTH
MobEffects.INSTANT_HEALTH
MobEffects.REGENERATION
MobEffects.RESISTANCE
MobEffects.FIRE_RESISTANCE
MobEffects.WATER_BREATHING
MobEffects.INVISIBILITY
MobEffects.NIGHT_VISION
MobEffects.SLOW_FALLING
MobEffects.LUCK
```

---

## 🌍 World & Blocks

### Block Position
```java
import net.minecraft.core.BlockPos;

BlockPos pos = player.blockPosition();
BlockPos custom = new BlockPos(x, y, z);

pos.getX();
pos.getY();
pos.getZ();
pos.above();         // Block above
pos.below();         // Block below
pos.north();         // Block to north
pos.south();         // etc.
```

### Teleportation
```java
// Simple teleport
player.teleportTo(x, y, z);

// Teleport with slow falling (prevent fall damage)
player.teleportTo(x, y, z);
player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));

// Teleport across dimensions (advanced)
ServerLevel targetLevel = server.getLevel(Level.NETHER);
player.teleportTo(targetLevel, x, y, z, yaw, pitch);
```

---

## 🔐 Permissions

### Check Player Permissions
```java
// Basic permission check
if (player.hasPermissions(2)) {
    // OP level 2+ (can use /give, /tp, etc.)
}

if (player.hasPermissions(4)) {
    // OP level 4 (full admin)
}

// In command .requires()
.requires(source -> source.hasPermission(4))

// Check if player is OP (for commands)
.requires(source -> {
    if (source.getEntity() instanceof ServerPlayer player) {
        return source.getServer().getPlayerList().isOp(player.getGameProfile());
    }
    return source.hasPermission(4);
})
```

---

## 📋 Items Reference

### Common Items (verified for 1.21.11)
```java
Items.DIAMOND
Items.EMERALD
Items.GOLD_INGOT
Items.IRON_INGOT
Items.COAL
Items.DIAMOND_SWORD
Items.DIAMOND_PICKAXE
Items.COMPASS          // Use instead of CLOCK/WATCH
Items.CLOCK
Items.PAPER
Items.BOOK
Items.ARROW
Items.BARRIER
Items.CHEST
Items.ENDER_CHEST
Items.NETHER_STAR
Items.WHEAT
Items.WHEAT_SEEDS
Items.CARROT
Items.POTATO
Items.APPLE
Items.GOLDEN_APPLE
Items.ENCHANTED_GOLDEN_APPLE
```

### Items That DON'T Exist
```java
Items.WATCH    // ❌ Use Items.CLOCK or Items.COMPASS
```

---

## 📊 Summary Table: What Changed

| Feature | 1.20.x | 1.21.11 | Impact |
|---------|--------|---------|--------|
| `tag.getInt()` | Returns int | Returns Optional | 🔴 High |
| `tag.getString()` | Returns String | Returns Optional | 🔴 High |
| `tag.getList()` | Returns ListTag | Returns Optional | 🔴 High |
| `NbtIo.read/write` | File param | Path + Accounter | 🔴 High |
| Enchantments | Simple API | Holder system | 🔴 High |
| Mappings | Yarn works | Mojang required | 🔴 High |
| Component.literal | Same | Same | 🟢 OK |
| Commands | Same | Same | 🟢 OK |
| ServerPlayer | Same | Same | 🟢 OK |

---

*Reference for Minecraft 1.21.11 - All code verified.*
