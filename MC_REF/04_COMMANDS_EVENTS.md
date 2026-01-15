# ⌨️ Commands & Events Guide

Complete guide to registering commands and handling events in Fabric 1.21.11.

---

## Command Registration

### Basic Setup

In your main mod class:
```java
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

@Override
public void onInitialize() {
    CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
        MyCommand.register(dispatcher);
        AnotherCommand.register(dispatcher);
    });
}
```

### Required Imports
```java
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
```

---

## Command Patterns

### Simple Command

```java
public class HelloCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hello")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                player.sendSystemMessage(Component.literal("§aHello!"));
                return 1;  // Success
            })
        );
    }
}
```

### Command with Subcommands

```java
public class MyModCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mymod")
            // /mymod - base command
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                player.sendSystemMessage(Component.literal("§6MyMod Commands:"));
                player.sendSystemMessage(Component.literal("§7/mymod help - Show help"));
                player.sendSystemMessage(Component.literal("§7/mymod balance - Check balance"));
                return 1;
            })
            
            // /mymod help
            .then(Commands.literal("help")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§6Help text here..."));
                    return 1;
                })
            )
            
            // /mymod balance
            .then(Commands.literal("balance")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    int balance = BalanceManager.getBalance(player.getUUID());
                    player.sendSystemMessage(Component.literal("§eBalance: §a$" + balance));
                    return 1;
                })
            )
        );
    }
}
```

### Command with Integer Argument

```java
// /pay <amount>
dispatcher.register(Commands.literal("pay")
    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000000))
        .executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            
            player.sendSystemMessage(Component.literal("§aPaying $" + amount));
            return 1;
        })
    )
);
```

### Command with String Argument

```java
// /msg <text>
dispatcher.register(Commands.literal("msg")
    // Single word
    .then(Commands.argument("word", StringArgumentType.word())
        .executes(context -> {
            String word = StringArgumentType.getString(context, "word");
            return 1;
        })
    )
    
    // Quoted string (with spaces)
    .then(Commands.argument("text", StringArgumentType.string())
        .executes(context -> {
            String text = StringArgumentType.getString(context, "text");
            return 1;
        })
    )
    
    // Greedy string (rest of line)
    .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(context -> {
            String message = StringArgumentType.getString(context, "message");
            return 1;
        })
    )
);
```

### Command with Player Argument

```java
// /teleport <player>
dispatcher.register(Commands.literal("teleport")
    .then(Commands.argument("target", EntityArgument.player())
        .executes(context -> {
            ServerPlayer executor = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            
            executor.teleportTo(
                target.getX(),
                target.getY(),
                target.getZ()
            );
            
            executor.sendSystemMessage(Component.literal(
                "§aTeleported to " + target.getName().getString()
            ));
            return 1;
        })
    )
);
```

### Command with Multiple Arguments

```java
// /give <player> <amount>
dispatcher.register(Commands.literal("give")
    .then(Commands.argument("player", EntityArgument.player())
        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
            .executes(context -> {
                ServerPlayer target = EntityArgument.getPlayer(context, "player");
                int amount = IntegerArgumentType.getInteger(context, "amount");
                
                BalanceManager.add(target.getUUID(), amount);
                return 1;
            })
        )
    )
);
```

---

## Permission Requirements

### OP-Only Commands

```java
// Require OP level 4 (admin)
dispatcher.register(Commands.literal("admincommand")
    .requires(source -> source.hasPermission(4))
    .executes(context -> {
        // Only admins can run this
        return 1;
    })
);

// Require OP level 2 (can use /give, /tp)
.requires(source -> source.hasPermission(2))
```

### Check if Player is OP (More Robust)

```java
.requires(source -> {
    if (source.getEntity() instanceof ServerPlayer player) {
        return source.getServer().getPlayerList().isOp(player.getGameProfile());
    }
    return source.hasPermission(4);  // Console always allowed
})
```

### Using getProfilePermissions

```java
.requires(source -> {
    if (source.getEntity() instanceof ServerPlayer player) {
        return source.getServer().getProfilePermissions(player.getGameProfile()) >= 4;
    }
    return source.hasPermission(4);
})
```

---

## Avoiding Command Conflicts

### ❌ Bad: Root-Level Commands
```java
// These might conflict with other mods
dispatcher.register(Commands.literal("sell")...);
dispatcher.register(Commands.literal("shop")...);
dispatcher.register(Commands.literal("home")...);
```

### ✅ Good: Subcommands Under Your Mod
```java
dispatcher.register(Commands.literal("mymod")
    .then(Commands.literal("sell")...)
    .then(Commands.literal("shop")...)
    .then(Commands.literal("home")...)
);

// Users type: /mymod sell, /mymod shop, /mymod home
```

---

## Complete Command Class Template

```java
package com.yourname.modname.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mymod")
            // /mymod - opens GUI
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                MyGui.openFor(player);
                return 1;
            })
            
            // /mymod help
            .then(Commands.literal("help")
                .executes(ModCommands::showHelp)
            )
            
            // /mymod balance [player]
            .then(Commands.literal("balance")
                .executes(ModCommands::showOwnBalance)
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> source.hasPermission(2))
                    .executes(ModCommands::showPlayerBalance)
                )
            )
            
            // /mymod pay <amount>
            .then(Commands.literal("pay")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(ModCommands::handlePay)
                )
            )
            
            // /mymod admin - admin subcommands
            .then(Commands.literal("admin")
                .requires(source -> source.hasPermission(4))
                
                // /mymod admin give <player> <amount>
                .then(Commands.literal("give")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ModCommands::adminGive)
                        )
                    )
                )
                
                // /mymod admin reset <player>
                .then(Commands.literal("reset")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ModCommands::adminReset)
                    )
                )
            )
        );
    }
    
    private static int showHelp(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.sendSystemMessage(Component.literal("§6§l=== MyMod Help ==="));
            player.sendSystemMessage(Component.literal("§e/mymod §7- Open main menu"));
            player.sendSystemMessage(Component.literal("§e/mymod balance §7- Check balance"));
            player.sendSystemMessage(Component.literal("§e/mymod pay <amount> §7- Pay money"));
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int showOwnBalance(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int balance = BalanceManager.getBalance(player.getUUID());
            player.sendSystemMessage(Component.literal("§eYour balance: §a$" + balance));
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int showPlayerBalance(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            int balance = BalanceManager.getBalance(target.getUUID());
            ctx.getSource().sendSystemMessage(Component.literal(
                "§e" + target.getName().getString() + "'s balance: §a$" + balance
            ));
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int handlePay(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            
            if (BalanceManager.withdraw(player.getUUID(), amount)) {
                player.sendSystemMessage(Component.literal("§aPaid $" + amount));
            } else {
                player.sendSystemMessage(Component.literal("§cInsufficient funds!"));
            }
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int adminGive(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            
            BalanceManager.add(target.getUUID(), amount);
            
            ctx.getSource().sendSystemMessage(Component.literal(
                "§aGave $" + amount + " to " + target.getName().getString()
            ));
            target.sendSystemMessage(Component.literal(
                "§aYou received $" + amount + " from an admin"
            ));
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int adminReset(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            BalanceManager.reset(target.getUUID());
            
            ctx.getSource().sendSystemMessage(Component.literal(
                "§cReset " + target.getName().getString() + "'s data"
            ));
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
```

---

## Event Handling

### Server Lifecycle Events

```java
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

// When server fully starts
ServerLifecycleEvents.SERVER_STARTED.register(server -> {
    DataManager.loadData(server);
    LOGGER.info("Server started, data loaded");
});

// When server begins stopping
ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
    DataManager.saveData(server);
    LOGGER.info("Server stopping, data saved");
});

// When server fully stopped
ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
    LOGGER.info("Server stopped");
});
```

### Server Tick Events

```java
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

private static int tickCounter = 0;

ServerTickEvents.END_SERVER_TICK.register(server -> {
    tickCounter++;
    
    // Every second (20 ticks)
    if (tickCounter % 20 == 0) {
        // Per-second logic
    }
    
    // Every minute (1200 ticks)
    if (tickCounter % 1200 == 0) {
        // Per-minute logic
    }
    
    // Every 5 minutes (6000 ticks)
    if (tickCounter >= 6000) {
        DataManager.saveData(server);
        tickCounter = 0;
    }
});
```

### Player Connection Events

```java
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

// Player joins
ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
    ServerPlayer player = handler.getPlayer();
    
    // Initialize player data
    DataManager.initPlayer(player.getUUID());
    
    // Welcome message
    player.sendSystemMessage(Component.literal("§aWelcome to the server!"));
    
    // Broadcast join
    server.getPlayerList().broadcastSystemMessage(
        Component.literal("§e" + player.getName().getString() + " joined!"),
        false
    );
});

// Player leaves
ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
    ServerPlayer player = handler.getPlayer();
    
    // Save player data
    DataManager.savePlayer(player.getUUID());
    
    // Broadcast leave
    server.getPlayerList().broadcastSystemMessage(
        Component.literal("§e" + player.getName().getString() + " left!"),
        false
    );
});
```

### Block Break Events

```java
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

// After block is broken
PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
    if (player instanceof ServerPlayer serverPlayer) {
        // Award money for breaking blocks
        if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
            BalanceManager.add(serverPlayer.getUUID(), 100);
            serverPlayer.sendSystemMessage(Component.literal("§a+$100 for diamond ore!"));
        }
    }
});

// Before block is broken (can cancel)
PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
    // Return false to cancel the break
    if (state.is(Blocks.BEDROCK)) {
        return false;  // Prevent breaking bedrock
    }
    return true;  // Allow breaking
});
```

### Entity Attack Events

```java
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
    if (player instanceof ServerPlayer serverPlayer && !world.isClientSide) {
        // Custom attack logic
        if (entity instanceof Monster) {
            // Award bounty for attacking monsters
            BalanceManager.add(serverPlayer.getUUID(), 10);
        }
    }
    return InteractionResult.PASS;  // Allow attack to continue
});
```

### Item Use Events

```java
import net.fabricmc.fabric.api.event.player.UseItemCallback;

UseItemCallback.EVENT.register((player, world, hand) -> {
    if (player instanceof ServerPlayer serverPlayer && !world.isClientSide) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (stack.is(Items.COMPASS)) {
            // Custom compass behavior
            serverPlayer.sendSystemMessage(Component.literal(
                "§ePosition: " + serverPlayer.blockPosition().toShortString()
            ));
        }
    }
    return InteractionResult.PASS;
});
```

---

## Event Registration Pattern

### Organize in EventHandlers class

```java
package com.yourname.modname.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventHandlers {
    
    public static void register() {
        registerLifecycleEvents();
        registerTickEvents();
        registerPlayerEvents();
        registerBlockEvents();
    }
    
    private static void registerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DataManager.loadData(server);
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DataManager.saveData(server);
        });
    }
    
    private static void registerTickEvents() {
        // Tick logic here
    }
    
    private static void registerPlayerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Join logic
        });
    }
    
    private static void registerBlockEvents() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, entity) -> {
            // Block break logic
        });
    }
}
```

### Call from main class

```java
@Override
public void onInitialize() {
    EventHandlers.register();
}
```

---

*Commands and events are the backbone of mod interactivity. Use these patterns consistently.*
