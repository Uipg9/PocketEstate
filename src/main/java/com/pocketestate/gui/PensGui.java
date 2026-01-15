package com.pocketestate.gui;

import com.pocketestate.PocketEstate;
import com.pocketestate.currency.CurrencyManager;
import com.pocketestate.data.EstateManager;
import com.pocketestate.data.PlayerData;
import com.pocketestate.farm.MobPen;
import com.pocketestate.farm.PenType;
import com.pocketestate.farm.VirtualMobManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Pens GUI - Virtual Mob Farm Management
 * 
 * Features:
 * - Category tabs: Pasture, Dungeon, Foundry
 * - Mini-mob rendering in pen slots (concept - uses item icons)
 * - Tool slot management
 * - Fodder management
 */
public class PensGui extends SimpleGui {
    
    private PenType.Category currentCategory = PenType.Category.PASTURE;
    private PenType selectedPen = null;
    
    public PensGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        setTitle(Component.literal("§c§l⚔ Mob Pens ⚔"));
        buildGui();
    }
    
    // Helper to get item name without using getDescription()
    private static String getItemName(Item item) {
        return new ItemStack(item).getHoverName().getString();
    }
    
    private void buildGui() {
        // Clear
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        if (selectedPen != null) {
            buildPenDetailView();
        } else {
            buildCategoryView();
        }
    }
    
    /**
     * Build the category selection and pen list view
     */
    private void buildCategoryView() {
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        
        // Category tabs at top
        int tabSlot = 1;
        for (PenType.Category category : PenType.Category.values()) {
            boolean isSelected = category == currentCategory;
            
            net.minecraft.world.item.Item tabIcon = switch (category) {
                case PASTURE -> Items.WHEAT;
                case DUNGEON -> Items.IRON_SWORD;
                case FOUNDRY -> Items.IRON_BLOCK;
            };
            
            setSlot(tabSlot, new GuiElementBuilder(tabIcon)
                .setName(Component.literal(category.getDisplayName()))
                .addLoreLine(Component.literal(category.getDescription()))
                .addLoreLine(Component.literal(isSelected ? "§a▶ Currently viewing" : "§7Click to view"))
                .glow(isSelected)
                .setCallback((index, type, action) -> {
                    currentCategory = category;
                    buildGui();
                })
            );
            tabSlot += 3;
        }
        
        // Frame
        buildFrame();
        
        // List pens in current category
        int slot = 19;
        for (PenType penType : PenType.values()) {
            if (penType.getCategory() != currentCategory) continue;
            
            boolean owned = data.hasMobPen(penType);
            MobPen pen = data.getMobPen(penType);
            
            GuiElementBuilder builder;
            
            if (owned) {
                // Owned pen - show status
                builder = new GuiElementBuilder(penType.getDisplayItem())
                    .setName(Component.literal("§a§l" + penType.getDisplayName()))
                    .addLoreLine(Component.literal("§a✓ Owned"))
                    .addLoreLine(Component.literal(""));
                
                // Fodder info
                if (penType.getFodderItem() != null) {
                    builder.addLoreLine(Component.literal("§7Fodder: §e" + pen.getFodderAmount() + 
                        "§7/" + (penType.getFodderPerCycle() * 10) + " " + 
                        getItemName(penType.getFodderItem())));
                }
                
                // Tool info
                if (penType.requiresTool()) {
                    ItemStack tool = pen.getToolSlot();
                    if (tool.isEmpty()) {
                        builder.addLoreLine(Component.literal("§7Tool: §c✗ None equipped"));
                    } else {
                        int durability = tool.getMaxDamage() - tool.getDamageValue();
                        builder.addLoreLine(Component.literal("§7Tool: §a" + tool.getHoverName().getString() + 
                            " §7(" + durability + " uses)"));
                    }
                }
                
                // Production status
                if (pen.canProduce()) {
                    builder.addLoreLine(Component.literal("§a§l✓ Producing"));
                } else if (!pen.isConstructed()) {
                    builder.addLoreLine(Component.literal("§c✗ Needs construction"));
                } else {
                    builder.addLoreLine(Component.literal("§c✗ Needs resources"));
                }
                
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to manage"))
                    .glow();
                
                final PenType pt = penType;
                builder.setCallback((index, type, action) -> {
                    selectedPen = pt;
                    buildGui();
                });
            } else {
                // Not owned - show purchase option
                long cost = penType.getUnlockCost();
                boolean canAfford = CurrencyManager.canAfford(player, cost);
                
                builder = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("§7§l" + penType.getDisplayName()))
                    .addLoreLine(Component.literal("§c✗ Not owned"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Unlock cost: " + 
                        (canAfford ? "§a" : "§c") + CurrencyManager.format(cost)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Produces:"));
                
                // List drops
                for (PenType.LootEntry loot : penType.getLootTable()) {
                    String toolReq = loot.requiredTool() != null 
                        ? " §8(needs " + getItemName(loot.requiredTool()) + ")"
                        : "";
                    builder.addLoreLine(Component.literal("§8• " + loot.minCount() + "-" + loot.maxCount() + 
                        "x " + getItemName(loot.item()) + toolReq));
                }
                
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(canAfford ? "§aClick to purchase" : "§cNot enough money"));
                
                if (canAfford) {
                    final PenType pt = penType;
                    builder.setCallback((index, type, action) -> {
                        if (EstateManager.unlockMobPen(player, pt)) {
                            buildGui();
                        }
                    });
                }
            }
            
            setSlot(slot, builder);
            slot++;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
        }
        
        // Collect resources button
        int pending = VirtualMobManager.getOutputBufferSize(player.getUUID());
        setSlot(49, new GuiElementBuilder(pending > 0 ? Items.CHEST : Items.ENDER_CHEST)
            .setName(Component.literal(pending > 0 ? "§a§lCollect Resources" : "§7§lNo Resources"))
            .addLoreLine(Component.literal(pending > 0 
                ? "§7Click to collect §a" + pending + "§7 items"
                : "§7Nothing to collect yet"))
            .glow(pending > 0)
            .setCallback((index, type, action) -> {
                if (pending > 0) {
                    VirtualMobManager.collectOutput(player);
                    player.sendSystemMessage(Component.literal("§a§l[ESTATE] §rCollected all resources!"));
                    buildGui();
                }
            })
        );
        
        // Back button
        setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7§lBack to Estate"))
            .setCallback((index, type, action) -> {
                new EstateGui(player).open();
            })
        );
    }
    
    /**
     * Build the detailed view for a specific pen
     */
    private void buildPenDetailView() {
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        MobPen pen = data.getMobPen(selectedPen);
        
        if (pen == null) {
            selectedPen = null;
            buildGui();
            return;
        }
        
        // Title/Header
        setSlot(4, new GuiElementBuilder(selectedPen.getDisplayItem())
            .setName(Component.literal("§6§l" + selectedPen.getDisplayName()))
            .addLoreLine(Component.literal(selectedPen.getCategory().getDescription()))
        );
        
        buildFrame();
        
        // Mob display (center) - In a full implementation, this would use entity rendering
        setSlot(22, new GuiElementBuilder(selectedPen.getDisplayItem())
            .setName(Component.literal("§e§lVirtual Mob"))
            .addLoreLine(Component.literal("§7This pen produces resources"))
            .addLoreLine(Component.literal("§7from virtual " + selectedPen.name().toLowerCase().replace("_", " ") + "s"))
        );
        
        // Fodder slot (left of center)
        if (selectedPen.getFodderItem() != null) {
            int fodder = pen.getFodderAmount();
            int maxFodder = selectedPen.getFodderPerCycle() * 10;
            
            setSlot(20, new GuiElementBuilder(selectedPen.getFodderItem())
                .setCount(Math.min(64, Math.max(1, fodder / 10)))
                .setName(Component.literal("§e§lFodder Storage"))
                .addLoreLine(Component.literal("§7Current: §a" + fodder + "§7/" + maxFodder))
                .addLoreLine(Component.literal("§7Per cycle: §e" + selectedPen.getFodderPerCycle()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Drop §e" + getItemName(selectedPen.getFodderItem())))
                .addLoreLine(Component.literal("§7here to add fodder"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to add 64 fodder (from inventory)"))
                .setCallback((index, type, action) -> {
                    // Try to consume fodder from player inventory
                    int added = tryAddFodderFromInventory(64);
                    if (added > 0) {
                        player.sendSystemMessage(Component.literal(
                            "§a§l[PEN] §rAdded §e" + added + "x§r fodder!"));
                        buildGui();
                    } else {
                        player.sendSystemMessage(Component.literal(
                            "§c§l[PEN] §rNo " + getItemName(selectedPen.getFodderItem()) + " in inventory!"));
                    }
                })
            );
        }
        
        // Tool slot (right of center)
        if (selectedPen.requiresTool()) {
            ItemStack currentTool = pen.getToolSlot();
            
            if (currentTool.isEmpty()) {
                setSlot(24, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("§7§lTool Slot"))
                    .addLoreLine(Component.literal("§c✗ No tool equipped"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7This pen needs tools:"))
                    .addLoreLine(Component.literal("§8• Shears for wool"))
                    .addLoreLine(Component.literal("§8• Sword for mob drops"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick with a tool to equip"))
                );
            } else {
                int durability = currentTool.getMaxDamage() - currentTool.getDamageValue();
                int maxDurability = currentTool.getMaxDamage();
                
                setSlot(24, new GuiElementBuilder(currentTool.getItem())
                    .setName(Component.literal("§a§l" + currentTool.getHoverName().getString()))
                    .addLoreLine(Component.literal("§7Durability: §e" + durability + "§7/" + maxDurability))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§cClick to unequip"))
                    .setCallback((index, type, action) -> {
                        ItemStack removed = pen.removeTool();
                        if (!removed.isEmpty()) {
                            player.addItem(removed);
                            player.sendSystemMessage(Component.literal(
                                "§e§l[PEN] §rUnequipped " + removed.getHoverName().getString()));
                        }
                        buildGui();
                    })
                );
            }
        }
        
        // Iron Golem construction (special case)
        if (selectedPen == PenType.IRON_GOLEM && !pen.isConstructed()) {
            int invested = pen.getIronBlocksInvested();
            int required = pen.getIronBlocksRequired();
            
            setSlot(31, new GuiElementBuilder(Items.IRON_BLOCK)
                .setCount(invested)
                .setName(Component.literal("§6§lFoundry Construction"))
                .addLoreLine(Component.literal("§7Iron Blocks: §e" + invested + "§7/" + required))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7The Iron Foundry requires"))
                .addLoreLine(Component.literal("§7" + required + " Iron Blocks to construct."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to invest an Iron Block"))
                .setCallback((index, type, action) -> {
                    if (tryConsumeItem(Items.IRON_BLOCK, 1)) {
                        pen.investIronBlock();
                        if (pen.isConstructed()) {
                            player.sendSystemMessage(Component.literal(
                                "§a§l[FOUNDRY] §rIron Foundry is now operational!"));
                        } else {
                            player.sendSystemMessage(Component.literal(
                                "§e§l[FOUNDRY] §rInvested Iron Block! (" + 
                                (invested + 1) + "/" + required + ")"));
                        }
                        buildGui();
                    } else {
                        player.sendSystemMessage(Component.literal(
                            "§c§l[FOUNDRY] §rNo Iron Blocks in inventory!"));
                    }
                })
            );
        }
        
        // Production status
        boolean canProduce = pen.canProduce();
        setSlot(40, new GuiElementBuilder(canProduce ? Items.LIME_DYE : Items.RED_DYE)
            .setName(Component.literal(canProduce ? "§a§l✓ Producing" : "§c§l✗ Not Producing"))
            .addLoreLine(Component.literal(canProduce 
                ? "§7This pen is actively producing resources!"
                : "§7This pen needs resources to produce"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total produced: §e" + pen.getTotalProduced()))
        );
        
        // Loot table info
        GuiElementBuilder lootBuilder = new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§e§lDrops"))
            .addLoreLine(Component.literal("§7This pen can produce:"));
        for (PenType.LootEntry loot : selectedPen.getLootTable()) {
            lootBuilder.addLoreLine(Component.literal("§8• " + loot.minCount() + "-" + loot.maxCount() + 
                "x " + getItemName(loot.item())));
        }
        setSlot(43, lootBuilder);
        
        // Back button
        setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7§lBack to Pens"))
            .setCallback((index, type, action) -> {
                selectedPen = null;
                buildGui();
            })
        );
    }
    
    /**
     * Try to add fodder from player inventory
     */
    private int tryAddFodderFromInventory(int amount) {
        if (selectedPen == null || selectedPen.getFodderItem() == null) return 0;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        MobPen pen = data.getMobPen(selectedPen);
        if (pen == null) return 0;
        
        net.minecraft.world.item.Item fodderType = selectedPen.getFodderItem();
        int consumed = 0;
        
        for (int i = 0; i < player.getInventory().getContainerSize() && consumed < amount; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(fodderType)) {
                int toTake = Math.min(stack.getCount(), amount - consumed);
                stack.shrink(toTake);
                consumed += toTake;
            }
        }
        
        if (consumed > 0) {
            pen.addFodder(consumed);
        }
        
        return consumed;
    }
    
    /**
     * Try to consume an item from player inventory
     */
    private boolean tryConsumeItem(net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toTake = Math.min(stack.getCount(), remaining);
                stack.shrink(toTake);
                remaining -= toTake;
            }
        }
        
        return remaining == 0;
    }
    
    /**
     * Build a simple frame around the GUI
     */
    private void buildFrame() {
        // Stone brick borders for dungeon feel
        net.minecraft.world.item.Item borderItem = currentCategory == PenType.Category.DUNGEON 
            ? Items.STONE_BRICKS 
            : (currentCategory == PenType.Category.FOUNDRY ? Items.IRON_BLOCK : Items.OAK_PLANKS);
        
        // Left and right borders
        for (int row = 1; row < 5; row++) {
            setSlot(row * 9, new GuiElementBuilder(borderItem).setName(Component.literal(" ")).hideTooltip());
            setSlot(row * 9 + 8, new GuiElementBuilder(borderItem).setName(Component.literal(" ")).hideTooltip());
        }
    }
}
