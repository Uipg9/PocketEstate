package com.pocketestate.gui;

import com.pocketestate.PocketEstate;
import com.pocketestate.config.SellPrices;
import com.pocketestate.currency.CurrencyManager;
import com.pocketestate.economy.EconomyIntegration;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for selling harvested resources.
 * 
 * Shows all items in the output buffer with their sell prices.
 * Players can:
 * - Sell individual items
 * - Sell all items at once
 * - Cancel and keep items
 */
public class SellGui extends SimpleGui {
    
    private int page = 0;
    private static final int ITEMS_PER_PAGE = 21; // 3 rows of 7 items
    
    public SellGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        setTitle(Component.literal("§6§l✦ Sell Resources ✦"));
        buildGui();
    }
    
    private void buildGui() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        buildFrame();
        buildContent();
        buildControls();
    }
    
    private void buildFrame() {
        // Top border
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("§6§lSell Resources"))
                .hideTooltip()
            );
        }
        
        // Bottom border
        for (int i = 45; i < 54; i++) {
            if (i != 49 && i != 47 && i != 51 && i != 45 && i != 53) {
                setSlot(i, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Component.literal(" "))
                    .hideTooltip()
                );
            }
        }
        
        // Side borders
        for (int row = 1; row < 5; row++) {
            setSlot(row * 9, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal(" "))
                .hideTooltip()
            );
            setSlot(row * 9 + 8, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal(" "))
                .hideTooltip()
            );
        }
    }
    
    private void buildContent() {
        List<ItemStack> buffer = getOutputBuffer();
        
        if (buffer.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Items to Sell"))
                .addLoreLine(Component.literal("§7Collect resources from your"))
                .addLoreLine(Component.literal("§7fields and pens first!"))
            );
            return;
        }
        
        // Calculate total value
        long totalValue = 0;
        for (ItemStack stack : buffer) {
            totalValue += SellPrices.getValue(stack);
        }
        
        // Display header info
        setSlot(4, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lTotal Value"))
            .addLoreLine(Component.literal("§7If you sell all items:"))
            .addLoreLine(Component.literal("§a" + CurrencyManager.format(totalValue)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Items: §e" + buffer.size()))
        );
        
        // Display items with prices
        int startIndex = page * ITEMS_PER_PAGE;
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };
        
        for (int i = 0; i < ITEMS_PER_PAGE && (startIndex + i) < buffer.size(); i++) {
            ItemStack stack = buffer.get(startIndex + i);
            long value = SellPrices.getValue(stack);
            long priceEach = SellPrices.getPrice(stack.getItem());
            
            final int itemIndex = startIndex + i;
            
            setSlot(slots[i], new GuiElementBuilder(stack.getItem())
                .setCount(Math.min(stack.getCount(), 64))
                .setName(Component.literal("§f" + stack.getHoverName().getString()))
                .addLoreLine(Component.literal("§7Amount: §e" + stack.getCount()))
                .addLoreLine(Component.literal("§7Price each: §a" + CurrencyManager.format(priceEach)))
                .addLoreLine(Component.literal("§7Total: §a" + CurrencyManager.format(value)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to sell this item"))
                .setCallback((index, type, action) -> {
                    sellSingleItem(itemIndex);
                })
            );
        }
    }
    
    private void buildControls() {
        List<ItemStack> buffer = getOutputBuffer();
        long totalValue = 0;
        for (ItemStack stack : buffer) {
            totalValue += SellPrices.getValue(stack);
        }
        
        // Pagination
        int maxPages = Math.max(1, (buffer.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        
        // Previous page
        if (page > 0) {
            setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§7Previous Page"))
                .setCallback((index, type, action) -> {
                    page--;
                    buildGui();
                })
            );
        }
        
        // Page indicator
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§7Page " + (page + 1) + "/" + maxPages))
        );
        
        // Next page
        if (page < maxPages - 1) {
            setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§7Next Page"))
                .setCallback((index, type, action) -> {
                    page++;
                    buildGui();
                })
            );
        }
        
        // Sell All button
        final long finalTotal = totalValue;
        if (!buffer.isEmpty()) {
            setSlot(47, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("§a§lSell All"))
                .addLoreLine(Component.literal("§7Sell all items for:"))
                .addLoreLine(Component.literal("§a" + CurrencyManager.format(totalValue)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to sell everything!"))
                .glow()
                .setCallback((index, type, action) -> {
                    sellAllItems();
                })
            );
        }
        
        // Back button
        setSlot(51, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lBack"))
            .addLoreLine(Component.literal("§7Return without selling"))
            .setCallback((index, type, action) -> {
                new EstateGui(player).open();
            })
        );
    }
    
    private List<ItemStack> getOutputBuffer() {
        if (PocketEstate.dataManager == null) return new ArrayList<>();
        return PocketEstate.dataManager.getPlayerData(player.getUUID()).getOutputBuffer();
    }
    
    private void sellSingleItem(int index) {
        List<ItemStack> buffer = getOutputBuffer();
        if (index < 0 || index >= buffer.size()) return;
        
        ItemStack stack = buffer.get(index);
        long value = SellPrices.getValue(stack);
        
        if (value > 0) {
            EconomyIntegration.addBalance(player.getUUID(), value);
            buffer.remove(index);
            
            player.sendSystemMessage(Component.literal("§a§l[SELL] §rSold §e" + 
                stack.getCount() + "x " + stack.getHoverName().getString() + 
                "§r for §a" + CurrencyManager.format(value)));
            
            // Adjust page if needed
            if (page > 0 && page * ITEMS_PER_PAGE >= buffer.size()) {
                page--;
            }
            
            buildGui();
        } else {
            player.sendSystemMessage(Component.literal("§c§l[SELL] §rThis item cannot be sold!"));
        }
    }
    
    private void sellAllItems() {
        List<ItemStack> buffer = getOutputBuffer();
        if (buffer.isEmpty()) return;
        
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
            
            page = 0;
            buildGui();
        }
    }
    
    public static void openFor(ServerPlayer player) {
        new SellGui(player).open();
    }
}
