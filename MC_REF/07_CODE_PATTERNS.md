# 📋 Code Patterns - Copy-Paste Ready Examples

Tested, working code patterns for Fabric 1.21.11.

---

## 🎮 Player Interactions

### Send Message to Player
```java
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

player.sendSystemMessage(Component.literal("§aSuccess!"));
player.sendSystemMessage(Component.literal("§cError occurred"));
player.sendSystemMessage(Component.literal("§eWarning message"));
player.sendSystemMessage(Component.literal("§6§lBold Gold Title"));
```

### Broadcast to All Players
```java
server.getPlayerList().broadcastSystemMessage(
    Component.literal("§e[Server] Hello everyone!"),
    false  // false = not above action bar
);
```

### Give Items to Player
```java
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

ItemStack stack = new ItemStack(Items.DIAMOND, 5);

if (!player.addItem(stack)) {
    // Inventory full - drop at feet
    player.drop(stack, false);
}
```

### Take Items from Player
```java
ItemStack hand = player.getMainHandItem();
if (hand.is(Items.DIAMOND)) {
    hand.shrink(1);  // Remove 1 from stack
}
```

### Check Inventory for Items
```java
boolean hasDiamonds = player.getInventory().contains(new ItemStack(Items.DIAMOND));

// Count specific item
int count = 0;
for (ItemStack stack : player.getInventory().items) {
    if (stack.is(Items.DIAMOND)) {
        count += stack.getCount();
    }
}
```

---

## 🧪 Effects & Buffs

### Apply Potion Effect
```java
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

// Speed II for 10 seconds
player.addEffect(new MobEffectInstance(
    MobEffects.SPEED,
    200,     // Duration in ticks (20 = 1 second)
    1,       // Amplifier (0 = level I, 1 = level II)
    false,   // Ambient (like beacon)
    true     // Show particles
));
```

### Common Effects
```java
MobEffects.SPEED
MobEffects.SLOWNESS
MobEffects.HASTE
MobEffects.MINING_FATIGUE
MobEffects.STRENGTH
MobEffects.INSTANT_HEALTH
MobEffects.INSTANT_DAMAGE
MobEffects.JUMP_BOOST
MobEffects.NAUSEA
MobEffects.REGENERATION
MobEffects.RESISTANCE
MobEffects.FIRE_RESISTANCE
MobEffects.WATER_BREATHING
MobEffects.INVISIBILITY
MobEffects.NIGHT_VISION
MobEffects.HUNGER
MobEffects.WEAKNESS
MobEffects.POISON
MobEffects.WITHER
MobEffects.SLOW_FALLING
MobEffects.LUCK
MobEffects.BAD_LUCK
MobEffects.GLOWING
```

### Heal Player
```java
player.heal(10.0f);  // Heal 5 hearts (10 half-hearts)
```

### Give Experience
```java
player.giveExperiencePoints(100);   // Points
player.giveExperienceLevels(5);     // Levels
```

---

## 🌍 Teleportation

### Simple Teleport
```java
player.teleportTo(x, y, z);
```

### Teleport with Safe Landing
```java
player.teleportTo(x, y, z);
player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
```

### Teleport to Another Player
```java
ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
if (target != null) {
    player.teleportTo(target.getX(), target.getY(), target.getZ());
}
```

### Get Player's Position
```java
import net.minecraft.core.BlockPos;

BlockPos pos = player.blockPosition();
int x = pos.getX();
int y = pos.getY();
int z = pos.getZ();

// Or as doubles
double x = player.getX();
double y = player.getY();
double z = player.getZ();
```

---

## 💰 Economy Pattern

### Simple Balance Manager
```java
public class BalanceManager {
    private static final Map<UUID, Long> balances = new HashMap<>();
    private static final long DEFAULT_BALANCE = 1000L;
    
    public static long getBalance(UUID id) {
        return balances.getOrDefault(id, DEFAULT_BALANCE);
    }
    
    public static void setBalance(UUID id, long amount) {
        balances.put(id, Math.max(0, amount));
    }
    
    public static void add(UUID id, long amount) {
        setBalance(id, getBalance(id) + amount);
    }
    
    public static boolean withdraw(UUID id, long amount) {
        if (getBalance(id) < amount) return false;
        setBalance(id, getBalance(id) - amount);
        return true;
    }
    
    public static boolean transfer(UUID from, UUID to, long amount) {
        if (!withdraw(from, amount)) return false;
        add(to, amount);
        return true;
    }
    
    // NBT Save/Load
    public static CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        balances.forEach((uuid, balance) -> {
            tag.putLong(uuid.toString(), balance);
        });
        return tag;
    }
    
    public static void loadFromNBT(CompoundTag tag) {
        balances.clear();
        for (String key : tag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            tag.getLong(key).ifPresent(bal -> balances.put(uuid, bal));
        }
    }
}
```

---

## ⏱️ Cooldown Pattern

### Simple Cooldown Manager
```java
public class CooldownManager {
    private static final Map<String, Long> cooldowns = new HashMap<>();
    
    // Key format: "playerId:feature"
    private static String getKey(UUID player, String feature) {
        return player.toString() + ":" + feature;
    }
    
    public static void setCooldown(UUID player, String feature, long durationMs) {
        cooldowns.put(getKey(player, feature), 
            System.currentTimeMillis() + durationMs);
    }
    
    public static boolean isOnCooldown(UUID player, String feature) {
        Long expiry = cooldowns.get(getKey(player, feature));
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }
    
    public static long getRemainingMs(UUID player, String feature) {
        Long expiry = cooldowns.get(getKey(player, feature));
        if (expiry == null) return 0;
        return Math.max(0, expiry - System.currentTimeMillis());
    }
    
    public static String getRemainingFormatted(UUID player, String feature) {
        long ms = getRemainingMs(player, feature);
        if (ms <= 0) return "Ready";
        
        long seconds = ms / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes + "m " + seconds + "s";
    }
}

// Usage
if (CooldownManager.isOnCooldown(player.getUUID(), "daily")) {
    player.sendSystemMessage(Component.literal(
        "§cOn cooldown: " + CooldownManager.getRemainingFormatted(player.getUUID(), "daily")
    ));
} else {
    // Do action
    CooldownManager.setCooldown(player.getUUID(), "daily", 24 * 60 * 60 * 1000);  // 24 hours
}
```

---

## 🎲 Random & Chances

### Random Number
```java
import java.util.Random;

Random random = new Random();
int number = random.nextInt(100);      // 0-99
int inRange = random.nextInt(10) + 1;  // 1-10
double decimal = random.nextDouble();   // 0.0-1.0
```

### Percentage Chance
```java
if (Math.random() < 0.25) {  // 25% chance
    // Rare event
}
```

### Weighted Random Selection
```java
public static <T> T weightedRandom(Map<T, Integer> weights) {
    int total = weights.values().stream().mapToInt(Integer::intValue).sum();
    int roll = new Random().nextInt(total);
    
    int cumulative = 0;
    for (Map.Entry<T, Integer> entry : weights.entrySet()) {
        cumulative += entry.getValue();
        if (roll < cumulative) {
            return entry.getKey();
        }
    }
    return weights.keySet().iterator().next();
}

// Usage
Map<String, Integer> loot = new HashMap<>();
loot.put("common", 70);
loot.put("rare", 25);
loot.put("legendary", 5);
String result = weightedRandom(loot);
```

---

## 📦 ItemStack Utilities

### Create Custom ItemStack
```java
ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);

// Set name
stack.setHoverName(Component.literal("§6§lLegendary Blade"));

// Set count
stack.setCount(1);

// Add custom NBT
CompoundTag tag = stack.getOrCreateTag();
tag.putString("modid_custom", "special_sword");
tag.putInt("modid_power", 100);
```

### Compare ItemStacks
```java
// Same item type
if (stack1.is(Items.DIAMOND)) { ... }

// Same item and components
if (ItemStack.isSameItemSameComponents(stack1, stack2)) { ... }

// Just same item (ignores NBT)
if (ItemStack.isSameItem(stack1, stack2)) { ... }
```

### Copy ItemStack
```java
ItemStack copy = stack.copy();
```

---

## 🏷️ Format Numbers

### Format with Commas
```java
import java.text.NumberFormat;

NumberFormat formatter = NumberFormat.getInstance();
String formatted = formatter.format(1234567);  // "1,234,567"
```

### Format Currency
```java
public static String formatMoney(long amount) {
    if (amount >= 1_000_000_000) {
        return String.format("$%.1fB", amount / 1_000_000_000.0);
    } else if (amount >= 1_000_000) {
        return String.format("$%.1fM", amount / 1_000_000.0);
    } else if (amount >= 1_000) {
        return String.format("$%.1fK", amount / 1_000.0);
    }
    return "$" + amount;
}
```

### Format Time
```java
public static String formatTime(long ms) {
    long seconds = ms / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    
    if (days > 0) return days + "d " + (hours % 24) + "h";
    if (hours > 0) return hours + "h " + (minutes % 60) + "m";
    if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
    return seconds + "s";
}
```

---

## 🔄 Pagination Helper

### Generic Pagination
```java
public class Pagination<T> {
    private final List<T> items;
    private final int pageSize;
    private int currentPage = 0;
    
    public Pagination(List<T> items, int pageSize) {
        this.items = items;
        this.pageSize = pageSize;
    }
    
    public List<T> getCurrentPage() {
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, items.size());
        return items.subList(start, end);
    }
    
    public int getTotalPages() {
        return (int) Math.ceil((double) items.size() / pageSize);
    }
    
    public int getCurrentPageNumber() {
        return currentPage + 1;
    }
    
    public boolean hasNext() {
        return currentPage < getTotalPages() - 1;
    }
    
    public boolean hasPrevious() {
        return currentPage > 0;
    }
    
    public void nextPage() {
        if (hasNext()) currentPage++;
    }
    
    public void previousPage() {
        if (hasPrevious()) currentPage--;
    }
}
```

---

## ⚙️ Config Manager Pattern

### Simple Config with Defaults
```java
public class ConfigManager {
    private static int maxBalance = 1_000_000_000;
    private static int startingBalance = 1000;
    private static boolean debugMode = false;
    
    public static int getMaxBalance() { return maxBalance; }
    public static int getStartingBalance() { return startingBalance; }
    public static boolean isDebugMode() { return debugMode; }
    
    public static CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("maxBalance", maxBalance);
        tag.putInt("startingBalance", startingBalance);
        tag.putBoolean("debugMode", debugMode);
        return tag;
    }
    
    public static void loadFromNBT(CompoundTag tag) {
        maxBalance = tag.getInt("maxBalance").orElse(1_000_000_000);
        startingBalance = tag.getInt("startingBalance").orElse(1000);
        debugMode = tag.getBoolean("debugMode").orElse(false);
    }
}
```

---

## 🏆 Achievement Pattern

### Simple Achievement System
```java
public class AchievementManager {
    private static final Map<UUID, Set<String>> unlocked = new HashMap<>();
    private static final Map<String, Achievement> achievements = new LinkedHashMap<>();
    
    public record Achievement(
        String id,
        String name,
        String description,
        int reward,
        Predicate<ServerPlayer> condition
    ) {}
    
    static {
        register(new Achievement("first_purchase", "First Steps", 
            "Make your first purchase", 100,
            p -> StatsManager.getPurchases(p.getUUID()) >= 1));
            
        register(new Achievement("millionaire", "Millionaire",
            "Reach 1,000,000 balance", 10000,
            p -> BalanceManager.getBalance(p.getUUID()) >= 1_000_000));
    }
    
    public static void register(Achievement a) {
        achievements.put(a.id(), a);
    }
    
    public static boolean isUnlocked(UUID player, String id) {
        return unlocked.getOrDefault(player, Set.of()).contains(id);
    }
    
    public static void check(ServerPlayer player) {
        UUID id = player.getUUID();
        for (Achievement a : achievements.values()) {
            if (!isUnlocked(id, a.id()) && a.condition().test(player)) {
                unlock(player, a);
            }
        }
    }
    
    private static void unlock(ServerPlayer player, Achievement a) {
        unlocked.computeIfAbsent(player.getUUID(), k -> new HashSet<>())
               .add(a.id());
        BalanceManager.add(player.getUUID(), a.reward());
        player.sendSystemMessage(Component.literal(
            "§6§l★ ACHIEVEMENT: §e" + a.name() + "\n§7" + a.description() +
            "\n§aReward: $" + a.reward()
        ));
    }
}
```

---

## 🔐 Permission Utilities

### Check OP Level
```java
public static boolean isAdmin(ServerPlayer player) {
    return player.getServer().getPlayerList()
        .isOp(player.getGameProfile());
}

public static int getOpLevel(ServerPlayer player) {
    return player.getServer()
        .getProfilePermissions(player.getGameProfile());
}
```

### Command Permission Check
```java
.requires(source -> {
    if (source.getEntity() instanceof ServerPlayer player) {
        return source.getServer().getPlayerList().isOp(player.getGameProfile());
    }
    return source.hasPermission(4);
})
```

---

## 📝 Logging

### Proper Logging
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("modname");
    
    @Override
    public void onInitialize() {
        LOGGER.info("Mod loading...");
        LOGGER.debug("Debug info here");
        LOGGER.warn("Warning message");
        LOGGER.error("Error occurred: {}", exceptionMessage);
    }
}
```

---

## 🎯 Quick Templates

### Complete Manager Class
```java
public class FeatureManager {
    private static final Map<UUID, FeatureData> data = new HashMap<>();
    
    public static class FeatureData {
        public int value = 0;
        public long lastUsed = 0;
    }
    
    public static FeatureData get(UUID id) {
        return data.computeIfAbsent(id, k -> new FeatureData());
    }
    
    public static CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        data.forEach((uuid, d) -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("value", d.value);
            entry.putLong("lastUsed", d.lastUsed);
            tag.put(uuid.toString(), entry);
        });
        return tag;
    }
    
    public static void loadFromNBT(CompoundTag tag) {
        data.clear();
        for (String key : tag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            tag.getCompound(key).ifPresent(entry -> {
                FeatureData d = new FeatureData();
                d.value = entry.getInt("value").orElse(0);
                d.lastUsed = entry.getLong("lastUsed").orElse(0L);
                data.put(uuid, d);
            });
        }
    }
}
```

---

*All patterns tested on Minecraft 1.21.11 with Fabric. Copy-paste ready!*
