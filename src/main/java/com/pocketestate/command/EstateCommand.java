package com.pocketestate.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pocketestate.PocketEstate;
import com.pocketestate.config.SellPrices;
import com.pocketestate.currency.CurrencyManager;
import com.pocketestate.data.EstateManager;
import com.pocketestate.economy.EconomyIntegration;
import com.pocketestate.gui.EstateGui;
import com.pocketestate.gui.SellGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.pocketestate.registry.ModItems;

import java.util.List;

/**
 * Commands for Pocket Estate
 * 
 * /estate - Opens the main Estate GUI
 * /estate give - Gives the player an Estate Ledger
 * /estate balance - Shows current balance
 * /estate sell - Opens sell GUI or sells all resources
 * /estate sellall - Quick sell all resources
 * /estate addmoney <amount> - (OP) Adds money to player
 * /estate stats - Shows estate statistics
 */
public class EstateCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("estate")
            // /estate - Open GUI
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                EstateGui.openFor(player);
                return 1;
            })
            
            // /estate give - Give Estate Ledger
            .then(Commands.literal("give")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack ledger = new ItemStack(ModItems.ESTATE_LEDGER);
                    
                    if (!player.addItem(ledger)) {
                        player.drop(ledger, false);
                    }
                    
                    player.sendSystemMessage(Component.literal("§a§l[ESTATE] §rReceived Estate Ledger!"));
                    return 1;
                })
            )
            
            // /estate balance - Show balance
            .then(Commands.literal("balance")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CurrencyManager.sendBalanceMessage(player);
                    
                    // Show economy provider info
                    if (EconomyIntegration.isUsingExternalEconomy()) {
                        player.sendSystemMessage(Component.literal(
                            "§7(via " + EconomyIntegration.getProviderName() + ")"));
                    }
                    return 1;
                })
            )
            
            // /estate sell - Open sell GUI
            .then(Commands.literal("sell")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    SellGui.openFor(player);
                    return 1;
                })
            )
            
            // /estate sellall - Quick sell all resources
            .then(Commands.literal("sellall")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    
                    if (PocketEstate.dataManager == null) {
                        player.sendSystemMessage(Component.literal("§c§l[SELL] §rData not loaded!"));
                        return 0;
                    }
                    
                    List<ItemStack> buffer = PocketEstate.dataManager
                        .getPlayerData(player.getUUID()).getOutputBuffer();
                    
                    if (buffer.isEmpty()) {
                        player.sendSystemMessage(Component.literal("§c§l[SELL] §rNo resources to sell!"));
                        return 0;
                    }
                    
                    long totalValue = 0;
                    int totalItems = 0;
                    
                    for (ItemStack stack : buffer) {
                        long value = SellPrices.getValue(stack);
                        totalValue += value;
                        totalItems += stack.getCount();
                    }
                    
                    if (totalValue > 0) {
                        EconomyIntegration.addBalance(player.getUUID(), totalValue);
                        buffer.clear();
                        
                        player.sendSystemMessage(Component.literal("§a§l[SELL] §rSold §e" + 
                            totalItems + " items§r for §a" + CurrencyManager.format(totalValue) + "§r!"));
                    } else {
                        player.sendSystemMessage(Component.literal("§c§l[SELL] §rNo sellable items found!"));
                    }
                    
                    return 1;
                })
            )
            
            // /estate stats - Show statistics
            .then(Commands.literal("stats")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    EstateManager.EstateStats stats = EstateManager.getEstateStats(player.getUUID());
                    
                    player.sendSystemMessage(Component.literal("§6§l=== Pocket Estate Stats ==="));
                    player.sendSystemMessage(Component.literal("§7Balance: §a" + CurrencyManager.format(stats.balance)));
                    player.sendSystemMessage(Component.literal("§7Crop Slots: §e" + stats.unlockedCropSlots + "/9"));
                    player.sendSystemMessage(Component.literal("§7Planted Crops: §e" + stats.plantedCrops));
                    player.sendSystemMessage(Component.literal("§7Ready to Harvest: §a" + stats.readyCrops));
                    player.sendSystemMessage(Component.literal("§7Mob Pens: §e" + stats.unlockedPens));
                    player.sendSystemMessage(Component.literal("§7Pending Resources: §e" + stats.pendingOutput + " items"));
                    player.sendSystemMessage(Component.literal("§6§l==========================="));
                    
                    return 1;
                })
            )
            
            // /estate addmoney <amount> - Admin command to add money (requires OP)
            .then(Commands.literal("addmoney")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        
                        // Check if player is OP
                        if (!isPlayerOp(context.getSource(), player)) {
                            player.sendSystemMessage(Component.literal(
                                "§c§l[ESTATE] §rYou need to be an operator to use this command!"));
                            return 0;
                        }
                        
                        int amount = IntegerArgumentType.getInteger(context, "amount");
                        
                        CurrencyManager.addMoney(player, amount);
                        CurrencyManager.sendMoneyReceivedMessage(player, amount, "Admin Grant");
                        
                        return 1;
                    })
                )
            )
            
            // /estate setmoney <amount> - Admin command to set money (requires OP)
            .then(Commands.literal("setmoney")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        
                        // Check if player is OP
                        if (!isPlayerOp(context.getSource(), player)) {
                            player.sendSystemMessage(Component.literal(
                                "§c§l[ESTATE] §rYou need to be an operator to use this command!"));
                            return 0;
                        }
                        
                        int amount = IntegerArgumentType.getInteger(context, "amount");
                        
                        com.pocketestate.PocketEstate.dataManager.getPlayerData(player.getUUID())
                            .setBalance(amount);
                        
                        player.sendSystemMessage(Component.literal(
                            "§a§l[ESTATE] §rBalance set to §6" + CurrencyManager.format(amount)));
                        
                        return 1;
                    })
                )
            )
            
            // /estate help - Show help
            .then(Commands.literal("help")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    
                    player.sendSystemMessage(Component.literal("§6§l=== Pocket Estate Help ==="));
                    player.sendSystemMessage(Component.literal("§e/estate §7- Open Estate GUI"));
                    player.sendSystemMessage(Component.literal("§e/estate give §7- Get Estate Ledger item"));
                    player.sendSystemMessage(Component.literal("§e/estate balance §7- Check your balance"));
                    player.sendSystemMessage(Component.literal("§e/estate sell §7- Open sell GUI"));
                    player.sendSystemMessage(Component.literal("§e/estate sellall §7- Quick sell all resources"));
                    player.sendSystemMessage(Component.literal("§e/estate stats §7- View estate statistics"));
                    player.sendSystemMessage(Component.literal("§e/estate help §7- Show this help"));
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("§8Admin commands:"));
                    player.sendSystemMessage(Component.literal("§7/estate addmoney <amount>"));
                    player.sendSystemMessage(Component.literal("§7/estate setmoney <amount>"));
                    player.sendSystemMessage(Component.literal("§6§l=========================="));
                    
                    return 1;
                })
            )
        );
        
        // Alias: /pe for quick access
        dispatcher.register(Commands.literal("pe")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                EstateGui.openFor(player);
                return 1;
            })
        );
        
        // Alias: /sell for quick selling
        dispatcher.register(Commands.literal("sell")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                SellGui.openFor(player);
                return 1;
            })
        );
    }
    
    /**
     * Check if a player is an operator.
     * Works in both singleplayer (with cheats enabled) and multiplayer.
     */
    private static boolean isPlayerOp(CommandSourceStack source, ServerPlayer player) {
        var server = source.getServer();
        
        // In singleplayer, the owner always has OP-like permissions
        if (server.isSingleplayer()) {
            return true;
        }
        
        // Check by using server's player list ops with player UUID
        try {
            var ops = server.getPlayerList().getOps();
            // NameAndId is a record, so id() method should work
            return ops.getEntries().stream()
                .anyMatch(entry -> entry.getUser().id().equals(player.getUUID()));
        } catch (Exception e) {
            // Fallback: if server is singleplayer owner, allow
            return server.isSingleplayer();
        }
    }
}
