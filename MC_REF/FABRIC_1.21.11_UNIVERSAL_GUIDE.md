# Fabric 1.21.11 Universal Development Guide

A comprehensive, copy-paste ready guide for developing **any** Fabric mod for Minecraft 1.21.11.

## Table of Contents
- [Project Structure](#project-structure)
- [Item Registration](#item-registration)
- [Block Registration](#block-registration)
- [GUI Development](#gui-development)
- [Data Storage](#data-storage)
- [Event Handling](#event-handling)
- [Command Registration](#command-registration)
- [Main Mod Initializer](#main-mod-initializer)
- [Best Practices](#best-practices)
- [Common Patterns](#common-patterns)

---

## Project Structure

### Standard Fabric Mod Layout
```
src/main/
├── java/
│   └── com/yourname/modname/
│       ├── ModName.java              # Main mod initializer
│       ├── registry/
│       │   ├── ModItems.java         # Item registration
│       │   ├── ModBlocks.java        # Block registration
│       │   └── ModCommands.java      # Command registration
│       ├── item/
│       │   └── CustomItem.java       # Custom item classes
│       ├── block/
│       │   └── CustomBlock.java      # Custom block classes
│       ├── gui/
│       │   └── CustomGui.java        # GUI classes (SGUI)
│       ├── util/
│       │   └── DataManager.java      # Utility classes
│       └── network/
│           └── ModPackets.java       # Network packets
└── resources/
    ├── fabric.mod.json               # Mod metadata
    ├── assets/modname/
    │   ├── textures/
    │   │   ├── item/
    │   │   └── block/
    │   └── models/
    │       ├── item/
    │       └── block/
    └── data/modname/
        └── recipes/
```

---

## Item Registration

### Basic Item Registration (1.21.11)
```java
package com.yourname.modname.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    
    // Define items
    public static final Item CUSTOM_ITEM = register("custom_item", 
        new Item(new Item.Properties()));
    
    // Register method
    private static Item register(String name, Item item) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("modname", name),
            item
        );
    }
    
    // Call from main mod class
    public static void register() {
        // Registration happens via static initialization
    }
}
```

### Custom Item Class
```java
package com.yourname.modname.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public class CustomItem extends Item {
    
    public CustomItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            // Server-side logic here
            player.sendSystemMessage(Component.literal("§aItem used!"));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
```

---

## Block Registration

### Basic Block Registration
```java
package com.yourname.modname.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    
    // Define block
    public static final Block CUSTOM_BLOCK = register("custom_block",
        new Block(BlockBehaviour.Properties.of()));
    
    // Register block and its item
    private static Block register(String name, Block block) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("modname", name);
        
        // Register block
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        
        // Register block item
        Registry.register(BuiltInRegistries.ITEM, id,
            new BlockItem(block, new Item.Properties()));
        
        return block;
    }
    
    public static void register() {
        // Registration happens via static initialization
    }
}
```

---

## GUI Development

### Using SGUI Library (Recommended)

**Add to build.gradle:**
```gradle
repositories {
    maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
    modImplementation include("eu.pb4:sgui:1.6.0+1.21.11)
}
```

### Simple GUI Example
```java
package com.yourname.modname.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class CustomGui extends SimpleGui {
    
    public CustomGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        this.setTitle(Component.literal("§6Custom GUI"));
        build();
    }
    
    private void build() {
        // Add clickable item with lore
        setSlot(13, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§bClick Me!"))
            .addLoreLine(Component.literal("§7Does something cool"))
            .addLoreLine(Component.literal("§aClick to activate"))
            .setCallback((index, type, action) -> {
                // Handle click
                player.sendSystemMessage(Component.literal("§aClicked!"));
            })
        );
        
        // Close button
        setSlot(22, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cClose"))
            .setCallback((index, type, action) -> this.close())
        );
    }
    
    public static void openFor(ServerPlayer player) {
        new CustomGui(player).open();
    }
}
```

---

## Data Storage

### In-Memory Storage (Simple)
```java
package com.yourname.modname.util;

import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static final Map<UUID, PlayerData> playerData = new HashMap<>();
    
    public static PlayerData getData(Player player) {
        return playerData.computeIfAbsent(player.getUUID(), k -> new PlayerData());
    }
    
    public static class PlayerData {
        public int customValue = 0;
        public boolean toggleEnabled = false;
    }
}
```

### Usage
```java
// Get player data
PlayerDataManager.PlayerData data = PlayerDataManager.getData(player);
data.customValue += 10;
data.toggleEnabled = !data.toggleEnabled;
```

---

## Event Handling

### Fabric API Events
```java
package com.yourname.modname.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;

public class EventHandlers {
    
    public static void register() {
        // Server tick event (runs every tick)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Tick logic here
        });
        
        // Block break event
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!level.isClientSide()) {
                // Award player for breaking block
            }
        });
        
        // Entity attack event
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Custom attack logic
            return InteractionResult.PASS; // Allow action to continue
        });
    }
}
```

---

## Command Registration

### Basic Commands with Arguments
```java
package com.yourname.modname.registry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            
            // Simple command: /mymod
            dispatcher.register(Commands.literal("mymod")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§aCommand executed!"));
                    return 1;
                })
                
                // Subcommand: /mymod help
                .then(Commands.literal("help")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        player.sendSystemMessage(Component.literal("§6Help text here"));
                        return 1;
                    })
                )
                
                // With integer argument: /mymod set <number>
                .then(Commands.literal("set")
                    .then(Commands.argument("number", IntegerArgumentType.integer(0, 100))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int value = IntegerArgumentType.getInteger(context, "number");
                            player.sendSystemMessage(Component.literal("§aSet to: " + value));
                            return 1;
                        })
                    )
                )
                
                // With string argument: /mymod msg <text>
                .then(Commands.literal("msg")
                    .then(Commands.argument("text", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String text = StringArgumentType.getString(context, "text");
                            player.sendSystemMessage(Component.literal("§e" + text));
                            return 1;
                        })
                    )
                )
            );
        });
    }
}
```

---

## Main Mod Initializer

### Standard Setup
```java
package com.yourname.modname;

import com.yourname.modname.registry.*;
import com.yourname.modname.util.*;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModName implements ModInitializer {
    public static final String MOD_ID = "modname";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loading " + MOD_ID + "...");
        
        // Register content
        ModItems.register();
        ModBlocks.register();
        ModCommands.register();
        
        // Initialize systems
        EventHandlers.register();
        
        LOGGER.info(MOD_ID + " loaded successfully!");
    }
}
```

---

## Best Practices

### Essential Rules
1. **Use correct API**: `ResourceLocation.fromNamespaceAndPath()` (not `new ResourceLocation()`)
2. **Server-side checks**: Always check `!level.isClientSide` before server logic
3. **Use SGUI** for GUIs - easier than vanilla containers
4. **Namespace everything**: Use `"modname_key"` for NBT to avoid conflicts
5. **Log events**: Use the Logger for debugging
6. **Handle edge cases**: Check for null, full inventory, etc.
7. **Prefer Fabric events** over mixins when possible
8. **Test multiplayer**: Single player != server behavior

### Code Quality
- Use descriptive variable names
- Add comments for complex logic
- Keep methods short and focused
- Use constants for magic numbers
- Handle errors gracefully

---

## Common Patterns

### Give Item to Player
```java
ItemStack stack = new ItemStack(Items.DIAMOND, 5);
if (!player.addItem(stack)) {
    // Inventory full, drop item
    player.drop(stack, false);
}
```

### Apply Potion Effect
```java
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

player.addEffect(new MobEffectInstance(
    MobEffects.SPEED,     // Effect type
    200,                  // Duration in ticks (10 seconds)
    1,                    // Amplifier (0 = level I, 1 = level II)
    false,                // Ambient
    false                 // Show particles
));
```

### Teleport Player
```java
import net.minecraft.server.level.ServerPlayer;

// Basic teleport
player.teleportTo(x, y, z);

// Teleport with slow falling (prevent fall damage)
player.teleportTo(x, y, z);
player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
```

### Check Player Permissions
```java
if (player.hasPermissions(2)) {
    // Player is OP level 2+ (can use commands)
}

if (player.hasPermissions(4)) {
    // Player is OP level 4 (full admin)
}
```

### Formatted Messages
```java
// Colored messages
player.sendSystemMessage(Component.literal("§aGreen text"));
player.sendSystemMessage(Component.literal("§cRed §eYellow §bBlue"));

// Bold/Italic
player.sendSystemMessage(Component.literal("§l§6BOLD GOLD"));
```

### Color Codes
- `§0` Black
- `§1` Dark Blue
- `§2` Dark Green
- `§3` Dark Aqua
- `§4` Dark Red
- `§5` Dark Purple
- `§6` Gold
- `§7` Gray
- `§8` Dark Gray
- `§9` Blue
- `§a` Green
- `§b` Aqua
- `§c` Red
- `§d` Light Purple
- `§e` Yellow
- `§f` White
- `§l` Bold
- `§o` Italic
- `§r` Reset

---

## Resources

- **Fabric Wiki**: https://fabricmc.net/wiki/
- **Minecraft Wiki**: https://minecraft.wiki/
- **SGUI Documentation**: https://pb4.eu/sgui/
- **Fabric API Javadocs**: https://maven.fabricmc.net/docs/
- **Brigadier Commands**: https://github.com/Mojang/brigadier

---

**This guide is universal and works for any Fabric mod. Copy these patterns and adapt to your needs!**
