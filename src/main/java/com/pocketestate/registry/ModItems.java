package com.pocketestate.registry;

import com.pocketestate.PocketEstate;
import com.pocketestate.gui.EstateGui;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;

/**
 * Registry for all Pocket Estate items
 */
public class ModItems {
    
    /**
     * Estate Ledger - The main item to open the Pocket Estate GUI
     * Recipe: Book + Emerald + Iron Bars
     */
    public static final Item ESTATE_LEDGER = register("estate_ledger", 
        EstateLedgerItem::new, 
        new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.UNCOMMON)
    );
    
    /**
     * Register an item (1.21.2+ compatible)
     * @param path The item's path (e.g., "estate_ledger")
     * @param factory Factory function to create the item
     * @param settings The item properties
     * @return The registered item
     */
    private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ResourceKey<Item> registryKey = ResourceKey.create(Registries.ITEM, PocketEstate.id(path));
        return Items.registerItem(registryKey, factory, settings);
    }
    
    /**
     * Call from main mod class to trigger static initialization
     */
    public static void register() {
        PocketEstate.LOGGER.info("Registering Pocket Estate items...");
    }
    
    /**
     * The Estate Ledger item - opens the main GUI
     */
    public static class EstateLedgerItem extends Item {
        
        public EstateLedgerItem(Properties properties) {
            super(properties);
        }
        
        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                // Open the Estate GUI
                EstateGui.openFor(serverPlayer);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        
        // Note: Custom tooltip requires implementation matching 1.21.11 signature
        // Tooltip info is provided in the GUI instead
    }
}
