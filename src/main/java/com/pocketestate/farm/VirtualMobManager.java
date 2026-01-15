package com.pocketestate.farm;

import com.pocketestate.PocketEstate;
import com.pocketestate.data.PlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages virtual mob farm production for all players
 */
public class VirtualMobManager {
    
    /**
     * Process mob production for all players
     * Called periodically from the main tick handler
     */
    public static void processProduction(MinecraftServer server) {
        if (PocketEstate.dataManager == null) return;
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            processPlayerProduction(player);
        }
    }
    
    /**
     * Process mob production for a specific player
     */
    public static void processPlayerProduction(ServerPlayer player) {
        if (PocketEstate.dataManager == null) return;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        
        for (MobPen pen : data.getMobPens().values()) {
            if (pen.canProduce()) {
                List<ItemStack> produced = pen.produce();
                for (ItemStack stack : produced) {
                    data.addToOutput(stack);
                }
            }
        }
    }
    
    /**
     * Unlock a new mob pen for a player
     * @return true if successful
     */
    public static boolean unlockPen(ServerPlayer player, PenType type) {
        if (PocketEstate.dataManager == null) return false;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        
        // Check if already owned
        if (data.hasMobPen(type)) return false;
        
        // Check if player can afford it
        long cost = type.getUnlockCost();
        if (!com.pocketestate.currency.CurrencyManager.canAfford(player, cost)) {
            return false;
        }
        
        // Deduct cost and unlock
        com.pocketestate.currency.CurrencyManager.removeMoney(player, cost);
        data.unlockMobPen(type);
        
        return true;
    }
    
    /**
     * Add fodder to a mob pen
     * @return the amount actually added
     */
    public static int addFodder(UUID playerId, PenType penType, int amount) {
        if (PocketEstate.dataManager == null) return 0;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        MobPen pen = data.getMobPen(penType);
        
        if (pen == null) return 0;
        
        return pen.addFodder(amount);
    }
    
    /**
     * Set a tool in a mob pen's tool slot
     * @return the previously equipped tool (or empty)
     */
    public static ItemStack setTool(UUID playerId, PenType penType, ItemStack tool) {
        if (PocketEstate.dataManager == null) return ItemStack.EMPTY;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        MobPen pen = data.getMobPen(penType);
        
        if (pen == null) return ItemStack.EMPTY;
        
        ItemStack oldTool = pen.removeTool();
        if (!tool.isEmpty()) {
            pen.setTool(tool);
        }
        
        return oldTool;
    }
    
    /**
     * Invest an iron block into the Iron Golem foundry
     * @return true if construction is complete
     */
    public static boolean investIronBlock(UUID playerId) {
        if (PocketEstate.dataManager == null) return false;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        MobPen pen = data.getMobPen(PenType.IRON_GOLEM);
        
        if (pen == null) return false;
        
        return pen.investIronBlock();
    }
    
    /**
     * Collect all produced items from the output buffer
     * @return list of collected items
     */
    public static List<ItemStack> collectOutput(ServerPlayer player) {
        if (PocketEstate.dataManager == null) return new ArrayList<>();
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(player.getUUID());
        List<ItemStack> collected = new ArrayList<>(data.getOutputBuffer());
        data.clearOutput();
        
        // Give items to player
        for (ItemStack stack : collected) {
            if (!player.addItem(stack.copy())) {
                // Inventory full - drop the item
                player.drop(stack.copy(), false);
            }
        }
        
        return collected;
    }
    
    /**
     * Get the total value of items in the output buffer
     */
    public static int getOutputBufferSize(UUID playerId) {
        if (PocketEstate.dataManager == null) return 0;
        
        PlayerData data = PocketEstate.dataManager.getPlayerData(playerId);
        return data.getOutputBuffer().stream()
            .mapToInt(ItemStack::getCount)
            .sum();
    }
}
