package com.pocketestate.gui;

import com.pocketestate.PocketEstate;
import com.pocketestate.currency.CurrencyManager;
import com.pocketestate.data.EstateManager;
import com.pocketestate.data.PlayerData;
import com.pocketestate.farm.CropPlot;
import com.pocketestate.farm.CropType;
import com.pocketestate.farm.VirtualCropManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Fields GUI - Virtual Crop Management
 * 
 * Features:
 * - Visual grid-based plot system (3x3)
 * - Real-time growth stage visualization
 * - One-click harvest and replant
 * - Crop selection menu
 */
public class FieldsGui extends SimpleGui {
    
    private static final int PLOTS_PER_PAGE = 9;
    private boolean showCropSelector = false;
    private int selectedPlotIndex = -1;
    private int currentPage = 0;
    
    public FieldsGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        setTitle(Component.literal("§a§l✿ Virtual Fields ✿"));
        buildGui();
    }
    
    private int getMaxPages() {
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        return (int) Math.ceil((double) data.getUnlockedCropSlots() / PLOTS_PER_PAGE);
    }
    
    private int getTotalPossiblePages() {
        return PlayerData.MAX_CROP_PLOTS / PLOTS_PER_PAGE;
    }
    
    private void buildGui() {
        // Clear
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
     * Build the main fields view with 3x3 plot grid and pagination
     */
    private void buildFieldsView() {
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        
        int totalPages = getTotalPossiblePages();
        int startSlot = currentPage * PLOTS_PER_PAGE;
        
        // Title area
        setSlot(4, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§a§lVirtual Fields"))
            .addLoreLine(Component.literal("§7Grow crops without taking up space!"))
            .addLoreLine(Component.literal("§7Unlocked: §a" + data.getUnlockedCropSlots() + "/" + PlayerData.MAX_CROP_PLOTS + "§7 plots"))
            .addLoreLine(Component.literal("§7Page: §e" + (currentPage + 1) + "/" + totalPages))
        );
        
        // 3x3 grid of plots (centered in the GUI)
        // Grid positions: rows 2-4, columns 2-4 -> slots 20-22, 29-31, 38-40
        int[] gridSlots = {20, 21, 22, 29, 30, 31, 38, 39, 40};
        
        for (int i = 0; i < PLOTS_PER_PAGE; i++) {
            final int plotIndex = startSlot + i; // Actual plot index
            int slot = gridSlots[i];
            boolean unlocked = data.isCropSlotUnlocked(plotIndex);
            CropPlot plot = data.getCropPlot(plotIndex);
            
            if (!unlocked) {
                // Locked slot - show cost to unlock the next one
                long cost = EstateManager.getNextCropSlotCost(player.getUUID());
                if (cost >= 0) {
                    setSlot(slot, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.literal("§8§lLocked Plot #" + (plotIndex + 1)))
                        .addLoreLine(Component.literal("§7Unlock next slot for: §6" + CurrencyManager.format(cost)))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§eClick to unlock next slot"))
                        .setCallback((index, type, action) -> {
                            if (EstateManager.unlockNextCropSlot(player)) {
                                buildGui();
                            }
                        })
                    );
                } else {
                    setSlot(slot, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Component.literal("§8§lLocked Plot #" + (plotIndex + 1)))
                        .addLoreLine(Component.literal("§7All plots unlocked!"))
                    );
                }
            } else if (plot == null || plot.isEmpty()) {
                // Empty plot - can plant
                setSlot(slot, new GuiElementBuilder(Items.FARMLAND)
                    .setName(Component.literal("§e§lEmpty Plot"))
                    .addLoreLine(Component.literal("§7Ready for planting"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§aClick to plant a crop"))
                    .setCallback((index, type, action) -> {
                        selectedPlotIndex = plotIndex;
                        showCropSelector = true;
                        buildGui();
                    })
                );
            } else {
                // Has a crop
                CropType cropType = plot.getCropType();
                float progress = plot.getGrowthProgress();
                int progressBars = (int) (progress * 10);
                String progressBar = "§a" + "▌".repeat(progressBars) + "§8" + "▌".repeat(10 - progressBars);
                
                Item displayItem = cropType.getDisplayItem(plot.getGrowthStage());
                
                GuiElementBuilder builder = new GuiElementBuilder(displayItem)
                    .setName(Component.literal("§a§l" + cropType.getDisplayName()))
                    .addLoreLine(Component.literal("§7Growth: " + progressBar + " §7" + (int)(progress * 100) + "%"));
                
                if (plot.isFullyGrown()) {
                    builder.addLoreLine(Component.literal("§a§l✓ Ready to harvest!"))
                        .addLoreLine(Component.literal("§7Expected yield: §e" + cropType.getBaseYield() + "x"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§aLeft-click: §7Harvest & Replant"))
                        .addLoreLine(Component.literal("§cRight-click: §7Clear plot"))
                        .glow();
                    
                    builder.setCallback((index, type, action) -> {
                        if (type.isLeft) {
                            // Harvest and replant - add to output buffer for selling
                            ItemStack harvested = VirtualCropManager.harvestCrop(player.getUUID(), plotIndex, true);
                            if (harvested != null && !harvested.isEmpty()) {
                                // Add to output buffer (can be sold or collected)
                                PocketEstate.dataManager.getPlayerData(player.getUUID()).addToOutput(harvested);
                                player.sendSystemMessage(Component.literal(
                                    "§a§l[FIELDS] §rHarvested §e" + harvested.getCount() + "x " + 
                                    harvested.getHoverName().getString() + "§r! §7(Use /sell or Collect)"));
                            }
                            buildGui();
                        } else if (type.isRight) {
                            // Clear plot
                            plot.clear();
                            buildGui();
                        }
                    });
                } else {
                    builder.addLoreLine(Component.literal("§7Growing..."))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§cRight-click: §7Clear plot"));
                    
                    builder.setCallback((index, type, action) -> {
                        if (type.isRight) {
                            plot.clear();
                            buildGui();
                        }
                    });
                }
                
                setSlot(slot, builder);
            }
        }
        
        // Harvest All button
        int readyCrops = VirtualCropManager.getReadyCropCount(player.getUUID());
        setSlot(25, new GuiElementBuilder(readyCrops > 0 ? Items.GOLDEN_HOE : Items.IRON_HOE)
            .setName(Component.literal(readyCrops > 0 ? "§a§lHarvest All" : "§7§lNo crops ready"))
            .addLoreLine(Component.literal(readyCrops > 0 
                ? "§7Click to harvest §a" + readyCrops + "§7 ready crops"
                : "§7Wait for crops to grow"))
            .glow(readyCrops > 0)
            .setCallback((index, type, action) -> {
                if (readyCrops > 0) {
                    var harvested = VirtualCropManager.harvestAll(player.getUUID(), true);
                    int total = harvested.stream().mapToInt(ItemStack::getCount).sum();
                    for (var stack : harvested) {
                        // Add to output buffer (can be sold or collected)
                        PocketEstate.dataManager.getPlayerData(player.getUUID()).addToOutput(stack);
                    }
                    player.sendSystemMessage(Component.literal(
                        "§a§l[FIELDS] §rHarvested all crops! Total: §e" + total + "§r items §7(Use /sell or Collect)"));
                    buildGui();
                }
            })
        );
        
        // Previous page button
        if (currentPage > 0) {
            setSlot(48, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§l← Previous Page"))
                .addLoreLine(Component.literal("§7Go to page " + currentPage))
                .setCallback((index, type, action) -> {
                    currentPage--;
                    buildGui();
                })
            );
        } else {
            setSlot(48, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("§8No Previous Page"))
            );
        }
        
        // Page indicator
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lPage " + (currentPage + 1) + " of " + totalPages))
            .addLoreLine(Component.literal("§7Showing plots " + (startSlot + 1) + "-" + Math.min(startSlot + PLOTS_PER_PAGE, PlayerData.MAX_CROP_PLOTS)))
        );
        
        // Next page button
        if (currentPage < totalPages - 1) {
            setSlot(50, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§lNext Page →"))
                .addLoreLine(Component.literal("§7Go to page " + (currentPage + 2)))
                .setCallback((index, type, action) -> {
                    currentPage++;
                    buildGui();
                })
            );
        } else {
            setSlot(50, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("§8No Next Page"))
            );
        }
        
        // Back button
        setSlot(45, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
            .setName(Component.literal("§c§lBack to Estate"))
            .setCallback((index, type, action) -> {
                new EstateGui(player).open();
            })
        );
        
        // Help/Info
        setSlot(53, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§e§lField Info"))
            .addLoreLine(Component.literal("§7Crops grow automatically over time."))
            .addLoreLine(Component.literal("§7Growth speed depends on crop type."))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a✓ §7Ready crops glow"))
            .addLoreLine(Component.literal("§e◐ §7Growing crops show progress"))
            .addLoreLine(Component.literal("§7Total plots: §e" + PlayerData.MAX_CROP_PLOTS + " §7(6 pages)"))
        );
    }
    
    /**
     * Build the crop selection menu
     */
    private void buildCropSelector() {
        // Title
        setSlot(4, new GuiElementBuilder(Items.WHEAT_SEEDS)
            .setName(Component.literal("§e§lSelect a Crop"))
            .addLoreLine(Component.literal("§7Choose what to plant"))
        );
        
        // Crop options
        int slot = 19;
        for (CropType crop : CropType.values()) {
            String harvestName = new ItemStack(crop.getHarvestItem()).getHoverName().getString();
            setSlot(slot, new GuiElementBuilder(crop.getSeedItem())
                .setName(Component.literal("§a§l" + crop.getDisplayName()))
                .addLoreLine(Component.literal("§7Yield: §e" + crop.getBaseYield() + "x " + harvestName))
                .addLoreLine(Component.literal("§7Growth Stages: §e" + crop.getMaxGrowthStage()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aClick to plant"))
                .setCallback((index, type, action) -> {
                    if (selectedPlotIndex >= 0) {
                        VirtualCropManager.plantCrop(player.getUUID(), selectedPlotIndex, crop);
                        player.sendSystemMessage(Component.literal(
                            "§a§l[FIELDS] §rPlanted §e" + crop.getDisplayName() + "§r!"));
                    }
                    showCropSelector = false;
                    selectedPlotIndex = -1;
                    buildGui();
                })
            );
            slot++;
            if (slot == 26) slot = 28; // Skip to next row if needed
        }
        
        // Cancel button
        setSlot(45, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lCancel"))
            .setCallback((index, type, action) -> {
                showCropSelector = false;
                selectedPlotIndex = -1;
                buildGui();
            })
        );
    }
}
