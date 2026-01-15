package com.pocketestate.gui;

import com.pocketestate.PocketEstate;
import com.pocketestate.data.PlayerData;
import com.pocketestate.farm.CropPlot;
import com.pocketestate.farm.CropType;
import com.pocketestate.farm.VirtualCropManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Redesigned Fields GUI - Full-screen farming with idle-game mechanics
 * 
 * Features:
 * - Spread across entire screen (21 plots visible per page)
 * - Plant All, Harvest All, Bonemeal All buttons
 * - Auto-buy plots, Auto-harvest toggle
 * - Compost bin for passive bonemeal
 * - XP and money rewards
 */
public class FieldsGui extends SimpleGui {

    private static final int PLOTS_PER_PAGE = 21; // 3 rows of 7
    private boolean showCropSelector = false;
    private int selectedPlotIndex = -1; // -1 means plant all
    private int currentPage = 0;

    public FieldsGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        setTitle(Component.literal("§a§l✿ Virtual Farm ✿"));
        buildGui();
    }

    private PlayerData getData() {
        return PocketEstate.dataManager.getPlayerData(player.getUUID());
    }

    private int getMaxPages() {
        return (int) Math.ceil((double) PlayerData.MAX_CROP_PLOTS / PLOTS_PER_PAGE);
    }

    private void buildGui() {
        // Clear all slots
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }

        if (showCropSelector) {
            buildCropSelector();
        } else {
            buildFieldsView();
        }
    }

    /**
     * Build the main fields view with plots spread across the screen
     */
    private void buildFieldsView() {
        PlayerData data = getData();
        int totalPages = getMaxPages();
        int startSlot = currentPage * PLOTS_PER_PAGE;
        
        int readyCrops = VirtualCropManager.getReadyCropCount(data);
        int growingCrops = VirtualCropManager.getGrowingCropCount(data);
        int emptyPlots = VirtualCropManager.getEmptyPlotCount(data);

        // === TOP ROW: Controls ===
        
        // Back button
        setSlot(0, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7← Back to Estate"))
            .setCallback((index, type, action) -> {
                new EstateGui(player).open();
            })
        );
        
        // Info display
        setSlot(2, new GuiElementBuilder(Items.OAK_SIGN)
            .setName(Component.literal("§e§lFarm Stats"))
            .addLoreLine(Component.literal("§7Plots: §a" + data.getUnlockedCropSlots() + "§7/§e" + PlayerData.MAX_CROP_PLOTS))
            .addLoreLine(Component.literal("§7Growing: §e" + growingCrops))
            .addLoreLine(Component.literal("§7Ready: §a" + readyCrops))
            .addLoreLine(Component.literal("§7Empty: §7" + emptyPlots))
            .addLoreLine(Component.literal("§7Page: §b" + (currentPage + 1) + "§7/§b" + totalPages))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§6Stats:"))
            .addLoreLine(Component.literal("§7Harvested: §f" + data.getTotalCropsHarvested()))
            .addLoreLine(Component.literal("§7XP Earned: §b" + data.getTotalXpEarned()))
            .addLoreLine(Component.literal("§7Money Earned: §a$" + data.getTotalMoneyEarned()))
        );
        
        // Plant All button
        setSlot(3, new GuiElementBuilder(Items.WHEAT_SEEDS)
            .setName(Component.literal("§a§lPlant All"))
            .addLoreLine(Component.literal("§7Plant crops in all empty plots"))
            .addLoreLine(Component.literal("§7Empty plots: §e" + emptyPlots))
            .addLoreLine(Component.literal(""))
            .addLoreLine(emptyPlots > 0 ? Component.literal("§aClick to select crop") : Component.literal("§7No empty plots"))
            .glow(emptyPlots > 0)
            .setCallback((index, type, action) -> {
                if (emptyPlots > 0) {
                    showCropSelector = true;
                    selectedPlotIndex = -1; // -1 = plant all
                    buildGui();
                }
            })
        );
        
        // Harvest All button
        int xpReward = readyCrops * VirtualCropManager.XP_PER_HARVEST;
        long moneyReward = (long) readyCrops * VirtualCropManager.MONEY_PER_HARVEST;
        setSlot(4, new GuiElementBuilder(Items.GOLDEN_HOE)
            .setName(Component.literal("§6§lHarvest All"))
            .addLoreLine(Component.literal("§7Harvest all ready crops"))
            .addLoreLine(Component.literal("§7Ready: §a" + readyCrops))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Rewards: §b+" + xpReward + " XP §7| §a+$" + moneyReward))
            .addLoreLine(Component.literal(""))
            .addLoreLine(readyCrops > 0 ? Component.literal("§aClick to harvest!") : Component.literal("§7No crops ready"))
            .glow(readyCrops > 0)
            .setCallback((index, type, action) -> {
                if (readyCrops > 0) {
                    int harvested = VirtualCropManager.harvestAllWithRewards(data, player);
                    if (harvested > 0) {
                        player.sendSystemMessage(Component.literal(
                            "§a§l[FARM] §rHarvested §e" + harvested + " items§r! Use §6Collect§r to get them."));
                        player.playSound(SoundEvents.CROP_BREAK, 0.7f, 1.0f);
                    }
                    buildGui();
                }
            })
        );
        
        // Count bonemeal in player inventory
        int invBonemeal = countItemInInventory(player, Items.BONE_MEAL);
        
        // Bonemeal button
        setSlot(5, new GuiElementBuilder(Items.BONE_MEAL)
            .setName(Component.literal("§2§lBonemeal Boost"))
            .addLoreLine(Component.literal("§7Boost ALL growing crops"))
            .addLoreLine(Component.literal("§7Stored Bonemeal: §e" + data.getStoredBonemeal()))
            .addLoreLine(Component.literal("§7In Inventory: §b" + invBonemeal))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Effect: §a25% growth boost to all"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§aLeft-click: §7Use stored bonemeal"))
            .addLoreLine(Component.literal("§eRight-click: §7Add from inventory"))
            .glow(data.getStoredBonemeal() > 0 || invBonemeal > 0)
            .setCallback((index, type, action) -> {
                if (type.isRight) {
                    // Add bonemeal from inventory
                    int added = transferItemFromInventory(player, Items.BONE_MEAL, 64);
                    if (added > 0) {
                        data.addStoredBonemeal(added);
                        player.sendSystemMessage(Component.literal("§a§l[FARM] §rAdded §e" + added + " bonemeal§r to storage!"));
                        player.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                    } else {
                        player.sendSystemMessage(Component.literal("§c§l[FARM] §rNo bonemeal in inventory!"));
                    }
                } else {
                    // Use bonemeal
                    if (data.getStoredBonemeal() > 0) {
                        int boosted = VirtualCropManager.applyBonemealBoost(data);
                        if (boosted > 0) {
                            data.useBonemeal(1);
                            player.sendSystemMessage(Component.literal("§a§l[FARM] §rBoosted §e" + boosted + " crops§r with bonemeal!"));
                            player.playSound(SoundEvents.BONE_MEAL_USE, 0.7f, 1.0f);
                        } else {
                            player.sendSystemMessage(Component.literal("§e§l[FARM] §rNo growing crops to boost!"));
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§c§l[FARM] §rNo bonemeal! Right-click to add from inventory."));
                    }
                }
                buildGui();
            })
        );
        
        // Compost Bin
        int compostProgress = data.getCompostProgress();
        String progressBar = "§a" + "█".repeat(compostProgress / 10) + "§7" + "░".repeat(10 - compostProgress / 10);
        setSlot(6, new GuiElementBuilder(Items.COMPOSTER)
            .setName(Component.literal("§6§lCompost Bin"))
            .addLoreLine(Component.literal("§7Produces bonemeal over time"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Progress: " + progressBar + " §f" + compostProgress + "%"))
            .addLoreLine(Component.literal("§7Stored: §e" + data.getStoredBonemeal() + " bonemeal"))
            .addLoreLine(Component.literal("§7Resources: §e" + data.getCompostResources() + " §7(speeds production)"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Click with crops to add resources"))
            .setCallback((index, type, action) -> {
                // Add resources from harvested crops
                if (!data.getCropOutput().isEmpty()) {
                    int added = Math.min(data.getCropOutput().size(), 5);
                    for (int i = 0; i < added; i++) {
                        if (!data.getCropOutput().isEmpty()) {
                            data.getCropOutput().remove(0);
                            data.addCompostResource(1);
                        }
                    }
                    player.sendSystemMessage(Component.literal("§a§l[COMPOST] §rAdded §e" + added + " resources§r to compost!"));
                    player.playSound(SoundEvents.COMPOSTER_FILL, 0.7f, 1.0f);
                } else {
                    player.sendSystemMessage(Component.literal("§c§l[COMPOST] §rNo crops in output! Harvest first."));
                }
                buildGui();
            })
        );
        
        // Buy plots button
        int unlockCost = 100 + (data.getUnlockedCropSlots() * 10);
        setSlot(7, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§e§lBuy More Plots"))
            .addLoreLine(Component.literal("§7Unlock new crop plots"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Next plot cost: §e$" + unlockCost))
            .addLoreLine(Component.literal("§7Your balance: §a$" + data.getBalance()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(data.getBalance() >= unlockCost 
                ? Component.literal("§aLeft-click: Buy 1 | Right-click: Buy 5")
                : Component.literal("§cNot enough money"))
            .glow(data.getBalance() >= unlockCost)
            .setCallback((index, type, action) -> {
                int toBuy = type.isRight ? 5 : 1;
                int bought = 0;
                for (int i = 0; i < toBuy; i++) {
                    int cost = 100 + (data.getUnlockedCropSlots() * 10);
                    if (data.getBalance() >= cost && data.getUnlockedCropSlots() < PlayerData.MAX_CROP_PLOTS) {
                        data.addBalance(-cost);
                        data.setUnlockedCropSlots(data.getUnlockedCropSlots() + 1);
                        bought++;
                    } else {
                        break;
                    }
                }
                if (bought > 0) {
                    player.sendSystemMessage(Component.literal("§a§l[FARM] §rUnlocked §e" + bought + " new plot(s)§r!"));
                    player.playSound(SoundEvents.PLAYER_LEVELUP, 0.5f, 1.2f);
                }
                buildGui();
            })
        );
        
        // Auto-harvest toggle
        setSlot(8, new GuiElementBuilder(data.isAutoHarvestEnabled() ? Items.DIAMOND_HOE : Items.IRON_HOE)
            .setName(Component.literal("§b§lAuto-Harvest"))
            .addLoreLine(Component.literal("§7Automatically harvest ready crops"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Status: " + (data.isAutoHarvestEnabled() ? "§a§lON" : "§c§lOFF")))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Harvested crops go to Collect"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Click to toggle"))
            .glow(data.isAutoHarvestEnabled())
            .setCallback((index, type, action) -> {
                data.setAutoHarvestEnabled(!data.isAutoHarvestEnabled());
                player.sendSystemMessage(Component.literal("§a§l[FARM] §rAuto-harvest: " + (data.isAutoHarvestEnabled() ? "§aON" : "§cOFF")));
                player.playSound(SoundEvents.LEVER_CLICK, 0.5f, data.isAutoHarvestEnabled() ? 1.2f : 0.8f);
                buildGui();
            })
        );

        // === CROP PLOTS: Rows 2-4 (7 plots per row = 21 total) ===
        // Slots: 10-16, 19-25, 28-34
        int[] plotSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < PLOTS_PER_PAGE; i++) {
            int plotIndex = startSlot + i;
            int slot = plotSlots[i];

            if (plotIndex >= PlayerData.MAX_CROP_PLOTS) {
                setSlot(slot, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(" "))
                    .hideTooltip()
                );
                continue;
            }

            if (!data.isCropSlotUnlocked(plotIndex)) {
                // Locked slot - show as iron bars
                setSlot(slot, new GuiElementBuilder(Items.IRON_BARS)
                    .setName(Component.literal("§c🔒 Plot #" + (plotIndex + 1)))
                    .addLoreLine(Component.literal("§7Unlock with Buy Plots button"))
                );
                continue;
            }

            CropPlot plot = data.getCropPlot(plotIndex);
            if (plot == null || plot.getCropType() == null) {
                // Empty plot
                final int plotIdx = plotIndex;
                setSlot(slot, new GuiElementBuilder(Items.BROWN_STAINED_GLASS_PANE)
                    .setName(Component.literal("§7Plot #" + (plotIndex + 1) + " §8(Empty)"))
                    .addLoreLine(Component.literal("§aClick to plant"))
                    .setCallback((index, type, action) -> {
                        showCropSelector = true;
                        selectedPlotIndex = plotIdx;
                        buildGui();
                    })
                );
            } else {
                // Has a crop
                CropType cropType = plot.getCropType();
                float progress = plot.getGrowthPercent();
                int progressBars = (int) (progress * 10);
                String cropProgressBar = "§a" + "█".repeat(progressBars) + "§7" + "░".repeat(10 - progressBars);

                if (plot.isReady()) {
                    // Ready to harvest
                    final int plotIdx = plotIndex;
                    setSlot(slot, new GuiElementBuilder(cropType.getHarvestItem())
                        .setName(Component.literal("§a§l" + cropType.getDisplayName() + " §7#" + (plotIndex + 1)))
                        .addLoreLine(Component.literal("§a§l✓ READY TO HARVEST!"))
                        .addLoreLine(Component.literal("§7Yield: §e" + cropType.getBaseYield() + "x"))
                        .addLoreLine(Component.literal("§7Rewards: §b+" + VirtualCropManager.XP_PER_HARVEST + " XP §a+$" + VirtualCropManager.MONEY_PER_HARVEST))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§aClick to harvest"))
                        .glow()
                        .setCount(cropType.getBaseYield())
                        .setCallback((index, type, action) -> {
                            ItemStack harvested = VirtualCropManager.harvestCropWithRewards(data, plotIdx, player);
                            if (harvested != null && !harvested.isEmpty()) {
                                data.addToCropOutput(harvested);
                                player.sendSystemMessage(Component.literal(
                                    "§a§l[FARM] §rHarvested §e" + harvested.getCount() + "x " + 
                                    cropType.getDisplayName() + " §b+" + VirtualCropManager.XP_PER_HARVEST + " XP §a+$" + VirtualCropManager.MONEY_PER_HARVEST));
                                player.playSound(SoundEvents.CROP_BREAK, 0.5f, 1.0f);
                            }
                            buildGui();
                        })
                    );
                } else {
                    // Growing
                    int remaining = plot.getGrowthTimeRemaining() / 20;
                    final int plotIdx = plotIndex;
                    setSlot(slot, new GuiElementBuilder(cropType.getSeedItem())
                        .setName(Component.literal("§e" + cropType.getDisplayName() + " §7#" + (plotIndex + 1)))
                        .addLoreLine(Component.literal("§7Growth: " + cropProgressBar + " §f" + (int)(progress * 100) + "%"))
                        .addLoreLine(Component.literal("§7Time left: §e" + remaining + "s"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§cRight-click to clear"))
                        .setCallback((index, type, action) -> {
                            if (type.isRight) {
                                plot.clear();
                                player.sendSystemMessage(Component.literal("§e§l[FARM] §rCleared plot #" + (plotIdx + 1)));
                                player.playSound(SoundEvents.GRASS_BREAK, 0.5f, 1.0f);
                                buildGui();
                            }
                        })
                    );
                }
            }
        }

        // === BOTTOM ROW: Pagination & Collect ===
        
        // Previous page
        if (currentPage > 0) {
            setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Previous Page"))
                .setCallback((index, type, action) -> {
                    currentPage--;
                    buildGui();
                })
            );
        }
        
        // Collect crops button
        int cropOutputCount = data.getCropOutputCount();
        List<ItemStack> cropOutput = data.getCropOutput();
        
        GuiElementBuilder collectBuilder = new GuiElementBuilder(cropOutputCount > 0 ? Items.CHEST : Items.ENDER_CHEST)
            .setName(Component.literal("§e§lCollect Crops"))
            .addLoreLine(Component.literal("§7Items waiting: §a" + cropOutputCount));
        
        if (!cropOutput.isEmpty() && cropOutput.size() <= 5) {
            collectBuilder.addLoreLine(Component.literal(""));
            for (ItemStack stack : cropOutput) {
                if (!stack.isEmpty()) {
                    collectBuilder.addLoreLine(Component.literal("§7- §f" + stack.getCount() + "x §e" + stack.getHoverName().getString()));
                }
            }
        }
        
        collectBuilder.addLoreLine(Component.literal(""))
            .addLoreLine(cropOutputCount > 0 ? Component.literal("§aClick to collect!") : Component.literal("§7Nothing to collect"))
            .glow(cropOutputCount > 0);
        
        setSlot(49, collectBuilder
            .setCallback((index, type, action) -> {
                if (!cropOutput.isEmpty()) {
                    int given = 0;
                    for (ItemStack stack : new ArrayList<>(cropOutput)) {
                        if (!player.getInventory().add(stack.copy())) {
                            player.drop(stack.copy(), false);
                        }
                        given += stack.getCount();
                    }
                    data.clearCropOutput();
                    player.sendSystemMessage(Component.literal("§a§l[FARM] §rCollected §e" + given + " items§r!"));
                    player.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                }
                buildGui();
            })
        );

        // Next page
        if (currentPage < getMaxPages() - 1) {
            setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eNext Page →"))
                .setCallback((index, type, action) -> {
                    currentPage++;
                    buildGui();
                })
            );
        }
    }

    /**
     * Build the crop selection menu
     */
    private void buildCropSelector() {
        // Clear all slots
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }

        String title = selectedPlotIndex >= 0 
            ? "§7Select crop for Plot #" + (selectedPlotIndex + 1)
            : "§7Select crop to plant in ALL empty plots";
            
        setSlot(4, new GuiElementBuilder(Items.WHEAT_SEEDS)
            .setName(Component.literal("§e§lSelect a Crop"))
            .addLoreLine(Component.literal(title))
        );

        // Display crops in a nice grid
        int[] cropSlots = {19, 20, 21, 22, 23, 24, 25};
        int slotIdx = 0;
        
        PlayerData data = getData();
        
        for (CropType crop : CropType.values()) {
            if (slotIdx >= cropSlots.length) break;
            
            // Count seeds in inventory
            int seedCount = countItemInInventory(player, crop.getSeedItem());
            int emptyPlots = VirtualCropManager.getEmptyPlotCount(data);
            
            setSlot(cropSlots[slotIdx], new GuiElementBuilder(crop.getSeedItem())
                .setName(Component.literal("§a" + crop.getDisplayName()))
                .addLoreLine(Component.literal("§7Yield: §e" + crop.getBaseYield() + "x " + crop.getHarvestItem().toString().replace("_", " ")))
                .addLoreLine(Component.literal("§7Growth time: §e" + crop.getGrowthTimeSeconds() + "s"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Seeds in inventory: §e" + seedCount))
                .addLoreLine(Component.literal(""))
                .addLoreLine(selectedPlotIndex >= 0 
                    ? Component.literal("§aClick to plant in plot #" + (selectedPlotIndex + 1))
                    : Component.literal("§aClick to plant in §e" + Math.min(seedCount, emptyPlots) + "§a plots"))
                .setCallback((index, type, action) -> {
                    if (selectedPlotIndex >= 0) {
                        // Plant in single plot - consume seed from inventory
                        if (consumeItemFromInventory(player, crop.getSeedItem(), 1)) {
                            VirtualCropManager.plantCrop(data, selectedPlotIndex, crop);
                            player.sendSystemMessage(Component.literal("§a§l[FARM] §rPlanted §e" + crop.getDisplayName() + "§r in plot #" + (selectedPlotIndex + 1)));
                            player.playSound(SoundEvents.CROP_PLANTED, 0.5f, 1.0f);
                        } else {
                            player.sendSystemMessage(Component.literal("§c§l[FARM] §rNo seeds in inventory!"));
                        }
                    } else {
                        // Plant all - consume seeds
                        int maxToPlant = countItemInInventory(player, crop.getSeedItem());
                        int planted = 0;
                        for (int i = 0; i < data.getUnlockedCropSlots() && planted < maxToPlant; i++) {
                            CropPlot plot = data.getCropPlot(i);
                            if (plot != null && plot.getCropType() == null) {
                                if (consumeItemFromInventory(player, crop.getSeedItem(), 1)) {
                                    plot.plant(crop);
                                    planted++;
                                }
                            }
                        }
                        if (planted > 0) {
                            player.sendSystemMessage(Component.literal("§a§l[FARM] §rPlanted §e" + crop.getDisplayName() + "§r in §e" + planted + " plots§r!"));
                            player.playSound(SoundEvents.CROP_PLANTED, 0.7f, 1.0f);
                        } else {
                            player.sendSystemMessage(Component.literal("§c§l[FARM] §rNo seeds or empty plots!"));
                        }
                    }
                    showCropSelector = false;
                    buildGui();
                })
            );
            slotIdx++;
        }

        // Back button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c← Cancel"))
            .setCallback((index, type, action) -> {
                showCropSelector = false;
                buildGui();
            })
        );
    }
    
    /**
     * Count how many of an item the player has in their inventory
     */
    private int countItemInInventory(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    /**
     * Transfer items from player inventory to estate storage
     * @return the amount actually transferred
     */
    private int transferItemFromInventory(ServerPlayer player, Item item, int maxAmount) {
        int transferred = 0;
        for (int i = 0; i < player.getInventory().getContainerSize() && transferred < maxAmount; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int toTake = Math.min(stack.getCount(), maxAmount - transferred);
                stack.shrink(toTake);
                transferred += toTake;
            }
        }
        return transferred;
    }
    
    /**
     * Consume items from player inventory
     * @return true if successfully consumed
     */
    private boolean consumeItemFromInventory(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int toTake = Math.min(stack.getCount(), remaining);
                stack.shrink(toTake);
                remaining -= toTake;
            }
        }
        return remaining == 0;
    }
}
