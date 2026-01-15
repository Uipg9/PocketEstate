package com.pocketestate.gui;

import com.pocketestate.PocketEstate;
import com.pocketestate.data.PlayerData;
import com.pocketestate.farm.CropPlot;
import com.pocketestate.farm.CropType;
import com.pocketestate.farm.VirtualCropManager;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Fields GUI - shows crop plots in a 3x3 grid with pagination (9 per page)
 * Updated with Plant All, Harvest All, Bonemeal Boost, Compost Bin, Auto-harvest
 */
public class FieldsGui extends SimpleGui {

    private final PlayerData playerData;
    private int currentPage = 0;
    private CropType selectedCropForPlantAll = null;

    private static final int PLOTS_PER_PAGE = 9; // 3x3 grid
    
    // Slot layout for 6-row chest:
    // Row 0: [Back] [Info] [Harvest All]
    // Row 1: [Plot] [Plot] [Plot]
    // Row 2: [Plot] [Plot] [Plot]
    // Row 3: [Plot] [Plot] [Plot]
    // Row 4: [Prev] [Bonemeal] [Next]
    // Row 5: [Compost] [Collect] [AutoHarvest]

    public FieldsGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.playerData = PocketEstate.dataManager.getPlayerData(player.getUUID());
        this.setTitle(Component.literal("§2§lCrop Fields"));
        updateDisplay();
    }

    private void updateDisplay() {
        // Clear all slots
        for (int i = 0; i < 54; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        int totalPages = (playerData.getUnlockedCropSlots() + PLOTS_PER_PAGE - 1) / PLOTS_PER_PAGE;
        int maxPages = (PlayerData.MAX_CROP_PLOTS + PLOTS_PER_PAGE - 1) / PLOTS_PER_PAGE;

        // === Row 0: Control buttons ===
        // Back button
        this.setSlot(0, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§f« Back to Estate"))
            .setCallback((index, type, action) -> {
                new EstateGui(player).open();
            })
            .build());

        // Info display
        int ready = VirtualCropManager.getReadyCropCount(playerData);
        int growing = VirtualCropManager.getGrowingCropCount(playerData);
        int empty = VirtualCropManager.getEmptyPlotCount(playerData);
        
        this.setSlot(4, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§6§lField Status"))
            .addLoreLine(Component.literal("§7Unlocked: §f" + playerData.getUnlockedCropSlots() + "/" + PlayerData.MAX_CROP_PLOTS))
            .addLoreLine(Component.literal("§aReady: §f" + ready))
            .addLoreLine(Component.literal("§eGrowing: §f" + growing))
            .addLoreLine(Component.literal("§7Empty: §f" + empty))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§6Stats:"))
            .addLoreLine(Component.literal("§7Total Harvested: §f" + playerData.getTotalCropsHarvested()))
            .addLoreLine(Component.literal("§7XP Earned: §f" + playerData.getTotalXpEarned()))
            .addLoreLine(Component.literal("§7Money Earned: §6$" + playerData.getTotalMoneyEarned()))
            .build());

        // Harvest All button
        this.setSlot(8, new GuiElementBuilder(ready > 0 ? Items.GOLDEN_HOE : Items.STONE_HOE)
            .setName(Component.literal("§6§lHarvest All"))
            .addLoreLine(Component.literal("§7Click to harvest all ready crops"))
            .addLoreLine(Component.literal("§aReady: §f" + ready + " crops"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e+2 XP per harvest"))
            .addLoreLine(Component.literal("§6+$5 per harvest"))
            .setCallback((index, type, action) -> {
                if (ready > 0) {
                    int harvested = VirtualCropManager.harvestAllWithRewards(playerData, player);
                    if (harvested > 0) {
                        player.sendSystemMessage(Component.literal("§aHarvested " + harvested + " crops! Check Collect button for items."));
                    }
                    updateDisplay();
                }
            })
            .build());

        // === Rows 1-3: Crop plot grid (3x3) ===
        int startSlot = 9;
        int startIndex = currentPage * PLOTS_PER_PAGE;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotOffset = row * 9 + col + 3; // Center the 3x3 grid (offset by 3)
                int plotIndex = startIndex + (row * 3 + col);

                if (plotIndex < playerData.getUnlockedCropSlots()) {
                    CropPlot plot = playerData.getCropPlot(plotIndex);
                    setPlotSlot(startSlot + slotOffset - 3, plot, plotIndex);
                } else if (plotIndex < PlayerData.MAX_CROP_PLOTS) {
                    // Locked slot
                    int unlockCost = 100 + (plotIndex * 10);
                    final int finalPlotIndex = plotIndex;
                    this.setSlot(startSlot + slotOffset - 3, new GuiElementBuilder(Items.BARRIER)
                        .setName(Component.literal("§c§lLocked Plot #" + (plotIndex + 1)))
                        .addLoreLine(Component.literal("§7Cost: §6$" + unlockCost))
                        .addLoreLine(Component.literal("§eClick to unlock"))
                        .setCallback((index, clickType, action) -> {
                            if (playerData.getBalance() >= unlockCost) {
                                playerData.addBalance(-unlockCost);
                                playerData.setUnlockedCropSlots(finalPlotIndex + 1);
                                player.playSound(SoundEvents.PLAYER_LEVELUP, 0.5f, 1.2f);
                                updateDisplay();
                            } else {
                                player.sendSystemMessage(Component.literal("§cNot enough money! Need $" + unlockCost));
                            }
                        })
                        .build());
                }
            }
        }

        // === Row 4: Navigation and Bonemeal ===
        // Previous page
        if (currentPage > 0) {
            this.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§f« Previous Page"))
                .addLoreLine(Component.literal("§7Page " + currentPage + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    currentPage--;
                    updateDisplay();
                })
                .build());
        }

        // Bonemeal Boost button
        int storedBonemeal = playerData.getStoredBonemeal();
        int inventoryBonemeal = countItemInInventory(player, Items.BONE_MEAL);
        
        this.setSlot(40, new GuiElementBuilder(Items.BONE_MEAL)
            .setName(Component.literal("§a§lBonemeal Boost"))
            .addLoreLine(Component.literal("§7Stored: §f" + storedBonemeal))
            .addLoreLine(Component.literal("§7In Inventory: §f" + inventoryBonemeal))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eLeft-click: §7Use 1 bonemeal (25% faster)"))
            .addLoreLine(Component.literal("§eRight-click: §7Add bonemeal from inventory"))
            .setCallback((index, clickType, action) -> {
                if (clickType == ClickType.MOUSE_RIGHT || clickType == ClickType.MOUSE_RIGHT_SHIFT) {
                    // Add bonemeal from inventory
                    int transferred = transferItemFromInventory(player, Items.BONE_MEAL, 64);
                    if (transferred > 0) {
                        playerData.addStoredBonemeal(transferred);
                        player.sendSystemMessage(Component.literal("§aAdded " + transferred + " bonemeal to storage!"));
                        player.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                    } else {
                        player.sendSystemMessage(Component.literal("§cNo bonemeal in inventory!"));
                    }
                } else {
                    // Use bonemeal
                    if (storedBonemeal > 0) {
                        int affected = VirtualCropManager.applyBonemealBoost(playerData);
                        if (affected > 0) {
                            playerData.useBonemeal(1);
                            player.playSound(SoundEvents.BONE_MEAL_USE, 0.7f, 1.0f);
                            player.sendSystemMessage(Component.literal("§aBoosted " + affected + " crops!"));
                        } else {
                            player.sendSystemMessage(Component.literal("§eNo growing crops to boost!"));
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§cNo bonemeal! Right-click to add from inventory."));
                    }
                }
                updateDisplay();
            })
            .build());

        // Next page
        if (currentPage < totalPages - 1) {
            this.setSlot(44, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§fNext Page »"))
                .addLoreLine(Component.literal("§7Page " + (currentPage + 2) + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    currentPage++;
                    updateDisplay();
                })
                .build());
        }

        // === Row 5: Compost, Collect, Auto-Harvest ===
        // Compost Bin
        int compostProgress = playerData.getCompostProgress();
        int compostResources = playerData.getCompostResources();
        
        this.setSlot(45, new GuiElementBuilder(Items.COMPOSTER)
            .setName(Component.literal("§6§lCompost Bin"))
            .addLoreLine(Component.literal("§7Progress: §f" + compostProgress + "%"))
            .addLoreLine(Component.literal("§7Resources: §f" + compostResources))
            .addLoreLine(Component.literal("§7Stored Bonemeal: §f" + storedBonemeal))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eClick with crops to compost"))
            .addLoreLine(Component.literal("§7Auto-generates bonemeal!"))
            .setCallback((index, clickType, action) -> {
                // Check if player is holding a compostable item
                ItemStack cursor = player.containerMenu.getCarried();
                if (!cursor.isEmpty() && isCompostable(cursor.getItem())) {
                    int amount = cursor.getCount();
                    cursor.shrink(amount);
                    playerData.addCompostResource(amount);
                    player.playSound(SoundEvents.COMPOSTER_FILL, 0.7f, 1.0f);
                    player.sendSystemMessage(Component.literal("§aAdded " + amount + " items to compost!"));
                    updateDisplay();
                }
            })
            .build());

        // Collect Crops button
        int cropOutputCount = playerData.getCropOutputCount();
        List<ItemStack> cropOutput = playerData.getCropOutput();
        
        GuiElementBuilder collectBuilder = new GuiElementBuilder(cropOutputCount > 0 ? Items.CHEST : Items.CHEST_MINECART)
            .setName(Component.literal("§6§lCollect Crops"))
            .addLoreLine(Component.literal("§7Items waiting: §f" + cropOutputCount));
        
        if (!cropOutput.isEmpty()) {
            collectBuilder.addLoreLine(Component.literal(""));
            for (ItemStack stack : cropOutput) {
                if (!stack.isEmpty()) {
                    String itemName = stack.getItem().toString().replace("_", " ");
                    collectBuilder.addLoreLine(Component.literal("§7- " + stack.getCount() + "x " + itemName));
                }
            }
        }
        
        collectBuilder.addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eClick to collect all"));
        
        this.setSlot(49, collectBuilder
            .setCallback((index, clickType, action) -> {
                if (!cropOutput.isEmpty()) {
                    for (ItemStack stack : cropOutput) {
                        if (!player.getInventory().add(stack.copy())) {
                            player.drop(stack.copy(), false);
                        }
                    }
                    playerData.clearCropOutput();
                    player.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                    player.sendSystemMessage(Component.literal("§aCollected all crops!"));
                    updateDisplay();
                }
            })
            .build());

        // Auto-Harvest toggle
        boolean autoHarvest = playerData.isAutoHarvestEnabled();
        this.setSlot(53, new GuiElementBuilder(autoHarvest ? Items.REDSTONE_TORCH : Items.LEVER)
            .setName(Component.literal("§6§lAuto-Harvest: " + (autoHarvest ? "§aON" : "§cOFF")))
            .addLoreLine(Component.literal("§7Automatically harvest ready crops"))
            .addLoreLine(Component.literal("§7and add to collection buffer"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eClick to toggle"))
            .setCallback((index, clickType, action) -> {
                playerData.setAutoHarvestEnabled(!autoHarvest);
                player.playSound(SoundEvents.LEVER_CLICK, 0.5f, autoHarvest ? 0.8f : 1.2f);
                updateDisplay();
            })
            .build());
    }

    private void setPlotSlot(int slot, CropPlot plot, int plotIndex) {
        if (plot.getCropType() == null) {
            // Empty plot - show planting options
            this.setSlot(slot, new GuiElementBuilder(Items.FARMLAND)
                .setName(Component.literal("§7Empty Plot #" + (plotIndex + 1)))
                .addLoreLine(Component.literal("§eClick to plant a crop"))
                .addLoreLine(Component.literal("§7Or hold seeds and click"))
                .setCallback((index, clickType, action) -> {
                    // Check if holding seeds
                    ItemStack held = player.containerMenu.getCarried();
                    if (!held.isEmpty()) {
                        CropType type = CropType.fromSeed(held.getItem());
                        if (type != null) {
                            if (VirtualCropManager.plantCrop(playerData, plotIndex, type)) {
                                held.shrink(1);
                                player.playSound(SoundEvents.CROP_PLANTED, 0.5f, 1.0f);
                                updateDisplay();
                                return;
                            }
                        }
                    }
                    // Open crop selection
                    openCropSelector(plotIndex);
                })
                .build());
        } else if (plot.isReady()) {
            // Ready to harvest
            CropType type = plot.getCropType();
            this.setSlot(slot, new GuiElementBuilder(type.getHarvestItem())
                .setName(Component.literal("§a§l" + type.getDisplayName() + " §7#" + (plotIndex + 1)))
                .addLoreLine(Component.literal("§a§lREADY TO HARVEST!"))
                .addLoreLine(Component.literal("§7Yield: §f" + type.getBaseYield()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e+2 XP, +$5 on harvest"))
                .addLoreLine(Component.literal("§eClick to harvest"))
                .setCallback((index, clickType, action) -> {
                    ItemStack harvested = VirtualCropManager.harvestCropWithRewards(playerData, plotIndex, player);
                    if (!harvested.isEmpty()) {
                        if (!player.getInventory().add(harvested)) {
                            player.drop(harvested, false);
                        }
                        player.playSound(SoundEvents.CROP_BREAK, 0.5f, 1.0f);
                        updateDisplay();
                    }
                })
                .setCount(type.getBaseYield())
                .build());
        } else {
            // Growing
            CropType type = plot.getCropType();
            float progress = plot.getGrowthPercent();
            int remaining = plot.getGrowthTimeRemaining() / 20; // Convert to seconds
            
            String progressBar = createProgressBar(progress);
            
            this.setSlot(slot, new GuiElementBuilder(type.getSeedItem())
                .setName(Component.literal("§e" + type.getDisplayName() + " §7#" + (plotIndex + 1)))
                .addLoreLine(Component.literal("§7Progress: " + progressBar + " §f" + String.format("%.0f%%", progress * 100)))
                .addLoreLine(Component.literal("§7Time left: §f" + remaining + "s"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cClick to uproot"))
                .setCallback((index, clickType, action) -> {
                    plot.clear();
                    player.playSound(SoundEvents.GRASS_BREAK, 0.5f, 1.0f);
                    updateDisplay();
                })
                .build());
        }
    }

    private void openCropSelector(int plotIndex) {
        SimpleGui selector = new SimpleGui(MenuType.GENERIC_9x1, player, false);
        selector.setTitle(Component.literal("§2Select Crop to Plant"));

        int slot = 0;
        for (CropType type : CropType.values()) {
            if (slot >= 9) break;
            
            selector.setSlot(slot++, new GuiElementBuilder(type.getSeedItem())
                .setName(Component.literal("§a" + type.getDisplayName()))
                .addLoreLine(Component.literal("§7Growth time: §f" + type.getGrowthTimeSeconds() + "s"))
                .addLoreLine(Component.literal("§7Yield: §f" + type.getBaseYield()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to plant"))
                .addLoreLine(Component.literal("§eShift-click: Plant All empty plots"))
                .setCallback((index, clickType, action) -> {
                    if (clickType == ClickType.MOUSE_LEFT_SHIFT || clickType == ClickType.MOUSE_RIGHT_SHIFT) {
                        // Plant all
                        int emptyPlots = VirtualCropManager.getEmptyPlotCount(playerData);
                        int seedCount = countItemInInventory(player, type.getSeedItem());
                        int toPlant = Math.min(emptyPlots, seedCount);
                        
                        if (toPlant > 0) {
                            int planted = VirtualCropManager.plantAll(playerData, type, toPlant);
                            transferItemFromInventory(player, type.getSeedItem(), planted);
                            player.playSound(SoundEvents.CROP_PLANTED, 0.7f, 1.0f);
                            player.sendSystemMessage(Component.literal("§aPlanted " + planted + " " + type.getDisplayName() + "!"));
                        } else {
                            player.sendSystemMessage(Component.literal("§cNo empty plots or seeds!"));
                        }
                    } else {
                        // Plant single
                        ItemStack seedStack = findItemInInventory(player, type.getSeedItem());
                        if (seedStack != null && !seedStack.isEmpty()) {
                            if (VirtualCropManager.plantCrop(playerData, plotIndex, type)) {
                                seedStack.shrink(1);
                                player.playSound(SoundEvents.CROP_PLANTED, 0.5f, 1.0f);
                            }
                        } else {
                            player.sendSystemMessage(Component.literal("§cNo seeds in inventory!"));
                        }
                    }
                    this.open();
                })
                .build());
        }

        selector.open();
    }

    private String createProgressBar(float progress) {
        int filled = (int) (progress * 10);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append("█");
            } else if (i == filled) {
                bar.append("§e█");
            } else {
                bar.append("§7░");
            }
        }
        return bar.toString();
    }

    private boolean isCompostable(Item item) {
        return item == Items.WHEAT || item == Items.CARROT || item == Items.POTATO ||
               item == Items.BEETROOT || item == Items.MELON_SLICE || item == Items.PUMPKIN ||
               item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS || 
               item == Items.MELON_SEEDS || item == Items.PUMPKIN_SEEDS ||
               item == Items.NETHER_WART || item == Items.ROTTEN_FLESH;
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
     * Transfer items from player inventory, returns amount transferred
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
     * Find an item stack in player inventory
     */
    private ItemStack findItemInInventory(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item && !stack.isEmpty()) {
                return stack;
            }
        }
        return null;
    }
}
