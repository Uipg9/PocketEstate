package com.pocketestate.gui;

import com.pocketestate.currency.CurrencyManager;
import com.pocketestate.data.EstateManager;
import com.pocketestate.farm.VirtualMobManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * Main Estate GUI - The "Dashboard" for Pocket Estate
 * 
 * Features a vanilla-native interface using existing game assets:
 * - Immersive tabs for Fields (Crops) and Pens (Mobs)
 * - Overview panel showing estate statistics
 * - Beautiful vanilla styling with Oak Planks, Stone Bricks, Dark UI slots
 */
public class EstateGui extends SimpleGui {
    
    private Tab currentTab = Tab.OVERVIEW;
    
    public enum Tab {
        OVERVIEW("Overview", Items.BOOK),
        FIELDS("Fields", Items.WHEAT),
        PENS("Pens", Items.IRON_SWORD),
        COLLECT("Collect", Items.CHEST),
        SELL("Sell", Items.GOLD_INGOT);
        
        private final String name;
        private final net.minecraft.world.item.Item icon;
        
        Tab(String name, net.minecraft.world.item.Item icon) {
            this.name = name;
            this.icon = icon;
        }
        
        public String getName() { return name; }
        public net.minecraft.world.item.Item getIcon() { return icon; }
    }
    
    public EstateGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        setTitle(Component.literal("§6§l✦ Pocket Estate ✦"));
        buildGui();
    }
    
    private void buildGui() {
        // Clear the GUI
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        // Build frame (vanilla-style border)
        buildFrame();
        
        // Build tabs
        buildTabs();
        
        // Build content based on current tab
        switch (currentTab) {
            case OVERVIEW -> buildOverviewContent();
            case FIELDS -> buildFieldsContent();
            case PENS -> buildPensContent();
            case COLLECT -> buildCollectContent();
            case SELL -> buildSellContent();
        }
    }
    
    /**
     * Build the vanilla-style frame around the GUI
     * Uses Oak Planks for borders, creating a "window" effect
     */
    private void buildFrame() {
        // Top border (slots 0-8)
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.OAK_PLANKS)
                .setName(Component.literal(" "))
                .hideTooltip()
            );
        }
        
        // Bottom border (slots 45-53)
        for (int i = 45; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.OAK_PLANKS)
                .setName(Component.literal(" "))
                .hideTooltip()
            );
        }
        
        // Left border (slots 9, 18, 27, 36)
        for (int row = 1; row < 5; row++) {
            setSlot(row * 9, new GuiElementBuilder(Items.OAK_PLANKS)
                .setName(Component.literal(" "))
                .hideTooltip()
            );
        }
        
        // Right border (slots 17, 26, 35, 44)
        for (int row = 1; row < 5; row++) {
            setSlot(row * 9 + 8, new GuiElementBuilder(Items.OAK_PLANKS)
                .setName(Component.literal(" "))
                .hideTooltip()
            );
        }
    }
    
    /**
     * Build the tab buttons at the top of the GUI
     */
    private void buildTabs() {
        int slot = 2;
        for (Tab tab : Tab.values()) {
            boolean isSelected = tab == currentTab;
            
            setSlot(slot, new GuiElementBuilder(tab.getIcon())
                .setName(Component.literal((isSelected ? "§6§l" : "§7") + tab.getName()))
                .addLoreLine(Component.literal(isSelected ? "§a▶ Currently viewing" : "§7Click to view"))
                .glow(isSelected)
                .setCallback((index, type, action) -> {
                    currentTab = tab;
                    buildGui();
                })
            );
            slot++;
        }
    }
    
    /**
     * Build the Overview tab content
     * Shows estate statistics and quick actions
     */
    private void buildOverviewContent() {
        EstateManager.EstateStats stats = EstateManager.getEstateStats(player.getUUID());
        
        // Balance display (center)
        setSlot(22, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance"))
            .addLoreLine(Component.literal("§7Your current funds:"))
            .addLoreLine(Component.literal("§a" + CurrencyManager.format(stats.balance)))
        );
        
        // Crop fields status (left)
        setSlot(20, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§a§lFields"))
            .addLoreLine(Component.literal("§7Unlocked Slots: §a" + stats.unlockedCropSlots + "/9"))
            .addLoreLine(Component.literal("§7Planted Crops: §e" + stats.plantedCrops))
            .addLoreLine(Component.literal("§7Ready to Harvest: §a" + stats.readyCrops))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eClick to manage fields"))
            .setCallback((index, type, action) -> {
                new FieldsGui(player).open();
            })
        );
        
        // Mob pens status (right)
        setSlot(24, new GuiElementBuilder(Items.SPAWNER)
            .setName(Component.literal("§c§lMob Pens"))
            .addLoreLine(Component.literal("§7Unlocked Pens: §a" + stats.unlockedPens))
            .addLoreLine(Component.literal("§7Pending Output: §e" + stats.pendingOutput + " items"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eClick to manage pens"))
            .setCallback((index, type, action) -> {
                new PensGui(player).open();
            })
        );
        
        // Collect all button (bottom center)
        int pendingItems = VirtualMobManager.getOutputBufferSize(player.getUUID());
        setSlot(40, new GuiElementBuilder(pendingItems > 0 ? Items.CHEST : Items.ENDER_CHEST)
            .setName(Component.literal(pendingItems > 0 ? "§a§lCollect Resources" : "§7§lNo Resources"))
            .addLoreLine(Component.literal(pendingItems > 0 
                ? "§7Click to collect §a" + pendingItems + "§7 pending items"
                : "§7Your pens haven't produced anything yet"))
            .glow(pendingItems > 0)
            .setCallback((index, type, action) -> {
                if (pendingItems > 0) {
                    var collected = VirtualMobManager.collectOutput(player);
                    if (!collected.isEmpty()) {
                        player.sendSystemMessage(Component.literal(
                            "§a§l[ESTATE] §rCollected §e" + collected.stream()
                                .mapToInt(s -> s.getCount()).sum() + "§r items!"));
                    }
                    buildGui();
                }
            })
        );
        
        // Close button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
    
    /**
     * Build the Fields tab content (redirects to FieldsGui)
     */
    private void buildFieldsContent() {
        // Show a loading message then open the FieldsGui
        setSlot(22, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§a§lOpening Fields..."))
        );
        
        // Redirect to FieldsGui
        new FieldsGui(player).open();
    }
    
    /**
     * Build the Pens tab content (redirects to PensGui)
     */
    private void buildPensContent() {
        setSlot(22, new GuiElementBuilder(Items.SPAWNER)
            .setName(Component.literal("§c§lOpening Pens..."))
        );
        
        new PensGui(player).open();
    }
    
    /**
     * Build the Collect tab content
     */
    private void buildCollectContent() {
        var outputBuffer = com.pocketestate.PocketEstate.dataManager
            .getPlayerData(player.getUUID()).getOutputBuffer();
        
        if (outputBuffer.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§7§lNo Resources to Collect"))
                .addLoreLine(Component.literal("§7Your pens haven't produced anything yet."))
                .addLoreLine(Component.literal("§7Make sure you have:"))
                .addLoreLine(Component.literal("§8• Unlocked pens"))
                .addLoreLine(Component.literal("§8• Added fodder"))
                .addLoreLine(Component.literal("§8• Equipped tools (if needed)"))
            );
        } else {
            // Display pending items
            int slot = 19;
            for (int i = 0; i < Math.min(outputBuffer.size(), 7); i++) {
                var stack = outputBuffer.get(i);
                setSlot(slot + i, new GuiElementBuilder(stack.getItem())
                    .setCount(Math.min(stack.getCount(), 64))
                    .setName(Component.literal("§f" + stack.getHoverName().getString()))
                    .addLoreLine(Component.literal("§7Amount: §a" + stack.getCount()))
                );
            }
            
            // Collect all button
            int total = outputBuffer.stream().mapToInt(s -> s.getCount()).sum();
            setSlot(31, new GuiElementBuilder(Items.HOPPER)
                .setName(Component.literal("§a§lCollect All"))
                .addLoreLine(Component.literal("§7Click to collect §a" + total + "§7 items"))
                .glow()
                .setCallback((index, type, action) -> {
                    VirtualMobManager.collectOutput(player);
                    player.sendSystemMessage(Component.literal("§a§l[ESTATE] §rCollected all resources!"));
                    buildGui();
                })
            );
        }
        
        // Back button
        setSlot(49, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7§lBack to Overview"))
            .setCallback((index, type, action) -> {
                currentTab = Tab.OVERVIEW;
                buildGui();
            })
        );
    }
    
    /**
     * Build the Sell tab content (redirects to SellGui)
     */
    private void buildSellContent() {
        setSlot(22, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lOpening Sell Menu..."))
        );
        
        new SellGui(player).open();
    }
    
    public static void openFor(ServerPlayer player) {
        new EstateGui(player).open();
    }
}
