# Lessons Learned from Pocket Estate Mod Development

**Minecraft Version:** 1.21.11  
**Fabric Loader:** 0.18.1  
**SGUI Version:** 1.12.0+1.21.11  
**Last Updated:** January 2026

This document captures specific lessons learned during the development of the Pocket Estate virtual farming mod.

---

## 🔴 Critical Issues & Solutions

### 1. Permission Checks for OP-Only Commands

**Problem:** Using old `hasPermissionLevel()` method doesn't work in 1.21.11

**Wrong Approach:**
```java
// This doesn't exist in 1.21.11!
source.hasPermissionLevel(4)
```

**Correct Solution for Fabric 1.21.11:**
```java
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SourceResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

// Method 1: Check if source is a player with OP level 4
.requires(source -> {
    if (source.getEntity() instanceof ServerPlayer player) {
        return source.getServer().getProfilePermissions(player.getGameProfile()) >= 4;
    }
    return source.hasPermission(4); // Console/command blocks
})

// Method 2: Use NameAndId helper (cleaner)
import net.minecraft.server.network.NameAndId;

.requires(source -> {
    if (source.getEntity() instanceof ServerPlayer player) {
        // NameAndId.id() returns the player's UUID
        return source.getServer().getPlayerList().isOp(new NameAndId(
            player.getGameProfile().getName(),
            NameAndId.id(player.getGameProfile())
        ));
    }
    return source.hasPermission(4);
})
```

**Key Insight:** `NameAndId.id()` is the correct method to get the UUID from a GameProfile in 1.21.11.

---

### 2. Mojang Mappings vs Yarn Method Names

**Critical:** Minecraft 1.21.11 uses Mojang Official Mappings, not Yarn!

| Yarn Name | Mojang Name | Usage |
|-----------|-------------|-------|
| `server.getPlayerManager()` | `server.getPlayerList()` | Get PlayerList |
| `CompoundTag.getInt("key")` | Same, but returns `Optional<Integer>` | NBT access |
| `entity.sendSystemMessage()` | Same | Send chat message |
| `Items.WHEAT_SEEDS` | Same | Item references |

**Always use Mojang mapping names when using `loom.officialMojangMappings()`!**

---

### 3. Output Buffer Pattern for Selling Systems

**Problem:** Need to store harvested items before selling, not give directly to player

**Solution - Output Buffer Pattern:**
```java
public class PlayerData {
    // Store items waiting to be sold/collected
    private final List<ItemStack> outputBuffer = new ArrayList<>();
    
    public void addToOutput(ItemStack stack) {
        // Try to merge with existing stacks first
        for (ItemStack existing : outputBuffer) {
            if (ItemStack.isSameItemSameComponents(existing, stack) && 
                existing.getCount() < existing.getMaxStackSize()) {
                int toAdd = Math.min(stack.getCount(), 
                    existing.getMaxStackSize() - existing.getCount());
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
}
```

**Workflow:**
1. Player harvests virtual crops → items go to `outputBuffer`
2. Player opens Sell GUI → sees items in buffer with prices
3. Player clicks Sell → money added, buffer cleared

---

### 4. SGUI Pagination Pattern

**Problem:** Need to display many items across multiple pages

**Solution:**
```java
public class PaginatedGui extends SimpleGui {
    private static final int ITEMS_PER_PAGE = 9;
    private int currentPage = 0;
    
    private int getMaxPages() {
        return (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
    }
    
    private void buildGui() {
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int totalPages = getMaxPages();
        
        // Render current page items...
        
        // Previous button (slot 48)
        if (currentPage > 0) {
            setSlot(48, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§l← Previous Page"))
                .setCallback((i, t, a) -> { currentPage--; buildGui(); })
            );
        }
        
        // Page indicator (slot 49)
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§ePage " + (currentPage + 1) + "/" + totalPages))
        );
        
        // Next button (slot 50)
        if (currentPage < totalPages - 1) {
            setSlot(50, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§lNext Page →"))
                .setCallback((i, t, a) -> { currentPage++; buildGui(); })
            );
        }
    }
}
```

---

### 5. NBT Serialization of ItemStacks in 1.21.11

**Problem:** Need to save/load ItemStacks to NBT

**Solution:**
```java
// Saving ItemStack to NBT
CompoundTag stackTag = new CompoundTag();
stackTag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
stackTag.putInt("count", stack.getCount());
if (stack.getDamageValue() > 0) {
    stackTag.putInt("damage", stack.getDamageValue());
}

// Loading ItemStack from NBT
String itemId = tag.getString("id").orElse("minecraft:air");
ResourceLocation loc = ResourceLocation.tryParse(itemId);
if (loc != null) {
    Item item = BuiltInRegistries.ITEM.getValue(loc);
    if (item != null && item != Items.AIR) {
        int count = tag.getInt("count").orElse(1);
        ItemStack stack = new ItemStack(item, count);
        tag.getInt("damage").ifPresent(stack::setDamageValue);
        return stack;
    }
}
```

**Key Point:** Use `BuiltInRegistries.ITEM.getKey()` and `BuiltInRegistries.ITEM.getValue()` for registry lookups!

---

### 6. CompoundTag.getList() Returns Optional in 1.21.11

**Problem:** `getList()` returns `Optional<ListTag>` now

**Solution:**
```java
// Wrong (1.20.x style):
ListTag list = tag.getList("items", Tag.TAG_COMPOUND);

// Correct (1.21.11):
ListTag list = tag.getList("items").orElse(new ListTag());

// Then iterate:
for (int i = 0; i < list.size(); i++) {
    list.getCompound(i).ifPresent(itemTag -> {
        // Process each compound tag
    });
}
```

---

### 7. Command Conflict Prevention

**Problem:** Your mod's `/sell` command conflicts with economy/shop mods

**Solution:** Use subcommands instead of standalone commands:
```java
// Instead of registering /sell
CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
    dispatcher.register(Commands.literal("sell")...);  // BAD - conflicts!
});

// Use /yourmod sell
CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
    LiteralArgumentBuilder<CommandSourceStack> main = Commands.literal("estate");
    main.then(Commands.literal("sell")...);  // GOOD - /estate sell
    dispatcher.register(main);
});
```

---

## 🟡 SGUI-Specific Tips

### Callback Patterns
```java
// Capture variables for lambda
final int capturedIndex = i;
final PenType capturedType = penType;

builder.setCallback((index, type, action) -> {
    if (type.isLeft) {
        // Left click action
        handleLeftClick(capturedIndex);
    } else if (type.isRight) {
        // Right click action  
        handleRightClick(capturedType);
    }
    buildGui(); // Refresh the GUI
});
```

### Glowing Effect for Ready Items
```java
builder.glow(isReady);  // Makes item glow if condition is true
```

### Menu Type for 6-Row Chest
```java
super(MenuType.GENERIC_9x6, player, false);  // 54 slots
```

---

## 🟢 Performance Tips

### Avoid Frequent NBT Saves
```java
// Instead of saving after every change:
public void addMoney(long amount) {
    balance += amount;
    saveNBT();  // BAD - too frequent!
}

// Batch saves at intervals:
ServerTickEvents.END_SERVER_TICK.register(server -> {
    tickCounter++;
    if (tickCounter >= 200) {  // Every 10 seconds
        saveAllData(server);
        tickCounter = 0;
    }
});
```

### Use EnumMap for Enum Keys
```java
// Instead of HashMap:
private final Map<PenType, MobPen> mobPens = new HashMap<>();

// Use EnumMap (faster for enum keys):
private final Map<PenType, MobPen> mobPens = new EnumMap<>(PenType.class);
```

---

## 📋 Checklist Before Building

1. ✅ All imports use Mojang mapping names
2. ✅ NBT methods use `.orElse()` for Optional returns
3. ✅ Commands don't conflict with common mod commands
4. ✅ GUI callbacks capture variables correctly
5. ✅ Permission checks use `getProfilePermissions()` or `isOp()`
6. ✅ ItemStack serialization uses `BuiltInRegistries`

---

## 🔗 Related Documentation

- [API_CHANGES_1.21.md](API_CHANGES_1.21.md) - Full NBT API changes
- [TROUBLESHOOTING_CHEAT_SHEET.md](TROUBLESHOOTING_CHEAT_SHEET.md) - Common build errors
- [FABRIC_1.21.11_UNIVERSAL_GUIDE.md](FABRIC_1.21.11_UNIVERSAL_GUIDE.md) - General patterns
