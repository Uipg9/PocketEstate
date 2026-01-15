package com.pocketestate.config;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Sell prices for all items that can be sold in Pocket Estate.
 * 
 * These prices are used for selling harvested crops and mob products.
 * Prices are balanced for a reasonable economy progression.
 */
public class SellPrices {
    
    private static final Map<Item, Long> PRICES = new HashMap<>();
    
    static {
        // ===== CROPS =====
        PRICES.put(Items.WHEAT, 5L);
        PRICES.put(Items.CARROT, 8L);
        PRICES.put(Items.POTATO, 8L);
        PRICES.put(Items.BEETROOT, 10L);
        PRICES.put(Items.MELON_SLICE, 3L);
        PRICES.put(Items.PUMPKIN, 15L);
        PRICES.put(Items.SUGAR_CANE, 6L);
        PRICES.put(Items.CACTUS, 7L);
        PRICES.put(Items.COCOA_BEANS, 12L);
        PRICES.put(Items.NETHER_WART, 25L);
        PRICES.put(Items.CHORUS_FRUIT, 35L);
        PRICES.put(Items.SWEET_BERRIES, 6L);
        PRICES.put(Items.GLOW_BERRIES, 10L);
        
        // Seeds (lower value)
        PRICES.put(Items.WHEAT_SEEDS, 2L);
        PRICES.put(Items.BEETROOT_SEEDS, 3L);
        PRICES.put(Items.MELON_SEEDS, 4L);
        PRICES.put(Items.PUMPKIN_SEEDS, 4L);
        
        // ===== MOB PRODUCTS =====
        // Wool
        PRICES.put(Items.WHITE_WOOL, 15L);
        PRICES.put(Items.ORANGE_WOOL, 15L);
        PRICES.put(Items.MAGENTA_WOOL, 15L);
        PRICES.put(Items.LIGHT_BLUE_WOOL, 15L);
        PRICES.put(Items.YELLOW_WOOL, 15L);
        PRICES.put(Items.LIME_WOOL, 15L);
        PRICES.put(Items.PINK_WOOL, 15L);
        PRICES.put(Items.GRAY_WOOL, 15L);
        PRICES.put(Items.LIGHT_GRAY_WOOL, 15L);
        PRICES.put(Items.CYAN_WOOL, 15L);
        PRICES.put(Items.PURPLE_WOOL, 15L);
        PRICES.put(Items.BLUE_WOOL, 15L);
        PRICES.put(Items.BROWN_WOOL, 15L);
        PRICES.put(Items.GREEN_WOOL, 15L);
        PRICES.put(Items.RED_WOOL, 15L);
        PRICES.put(Items.BLACK_WOOL, 15L);
        
        // Animal products
        PRICES.put(Items.LEATHER, 25L);
        PRICES.put(Items.BEEF, 20L);
        PRICES.put(Items.COOKED_BEEF, 35L);
        PRICES.put(Items.MILK_BUCKET, 40L);
        PRICES.put(Items.EGG, 10L);
        PRICES.put(Items.CHICKEN, 12L);
        PRICES.put(Items.COOKED_CHICKEN, 22L);
        PRICES.put(Items.FEATHER, 5L);
        PRICES.put(Items.MUTTON, 18L);
        PRICES.put(Items.COOKED_MUTTON, 30L);
        PRICES.put(Items.PORKCHOP, 18L);
        PRICES.put(Items.COOKED_PORKCHOP, 30L);
        PRICES.put(Items.RABBIT, 15L);
        PRICES.put(Items.COOKED_RABBIT, 28L);
        PRICES.put(Items.RABBIT_HIDE, 8L);
        PRICES.put(Items.RABBIT_FOOT, 100L);
        
        // Monster drops
        PRICES.put(Items.STRING, 12L);
        PRICES.put(Items.SPIDER_EYE, 20L);
        PRICES.put(Items.FERMENTED_SPIDER_EYE, 45L);
        PRICES.put(Items.ROTTEN_FLESH, 5L);
        PRICES.put(Items.BONE, 15L);
        PRICES.put(Items.ARROW, 8L);
        PRICES.put(Items.GUNPOWDER, 35L);
        PRICES.put(Items.ENDER_PEARL, 150L);
        PRICES.put(Items.BLAZE_ROD, 200L);
        PRICES.put(Items.GHAST_TEAR, 250L);
        PRICES.put(Items.SLIME_BALL, 30L);
        PRICES.put(Items.PHANTOM_MEMBRANE, 80L);
        PRICES.put(Items.WITHER_SKELETON_SKULL, 500L);
        
        // Iron Golem
        PRICES.put(Items.IRON_INGOT, 50L);
        PRICES.put(Items.POPPY, 5L);
        
        // High-value items
        PRICES.put(Items.DIAMOND, 500L);
        PRICES.put(Items.EMERALD, 200L);
        PRICES.put(Items.GOLD_INGOT, 75L);
        PRICES.put(Items.IRON_BLOCK, 450L);
        PRICES.put(Items.GOLD_BLOCK, 675L);
        PRICES.put(Items.DIAMOND_BLOCK, 4500L);
        PRICES.put(Items.EMERALD_BLOCK, 1800L);
    }
    
    /**
     * Get the sell price for an item
     * @return sell price, or 0 if item cannot be sold
     */
    public static long getPrice(Item item) {
        return PRICES.getOrDefault(item, 0L);
    }
    
    /**
     * Check if an item can be sold
     */
    public static boolean canSell(Item item) {
        return PRICES.containsKey(item);
    }
    
    /**
     * Get the total value of an item stack
     */
    public static long getValue(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return getPrice(stack.getItem()) * stack.getCount();
    }
    
    /**
     * Get all sellable items and their prices
     */
    public static Map<Item, Long> getAllPrices() {
        return new HashMap<>(PRICES);
    }
    
    /**
     * Register a custom sell price (for integration with other mods)
     */
    public static void registerPrice(Item item, long price) {
        PRICES.put(item, price);
    }
}
