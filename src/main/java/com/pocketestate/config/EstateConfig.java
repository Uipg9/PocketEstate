package com.pocketestate.config;

/**
 * Configuration options for Pocket Estate
 * 
 * These can be modified to adjust game balance.
 * In a full implementation, these would be loaded from a config file.
 */
public class EstateConfig {
    
    // ===== CROP SETTINGS =====
    
    /** Base time for crops to grow one stage (in ticks) */
    public static int CROP_GROWTH_INTERVAL = 200; // 10 seconds
    
    /** Maximum number of crop plots */
    public static int MAX_CROP_SLOTS = 9;
    
    /** Costs to unlock each crop slot (index = slot number) */
    public static long[] CROP_SLOT_COSTS = {
        0,       // Slot 1 - free
        500,     // Slot 2
        1000,    // Slot 3
        2500,    // Slot 4
        5000,    // Slot 5
        10000,   // Slot 6
        25000,   // Slot 7
        50000,   // Slot 8
        100000   // Slot 9
    };
    
    // ===== MOB PEN SETTINGS =====
    
    /** Time between production cycles (in ticks) */
    public static int PRODUCTION_INTERVAL = 1200; // 1 minute
    
    /** Maximum fodder storage multiplier (per cycle requirement) */
    public static int MAX_FODDER_MULTIPLIER = 10;
    
    // ===== PEN UNLOCK COSTS =====
    
    public static long SHEEP_PEN_COST = 5000;
    public static long COW_PEN_COST = 7500;
    public static long CHICKEN_PEN_COST = 3000;
    public static long SPIDER_PEN_COST = 10000;
    public static long ZOMBIE_PEN_COST = 10000;
    public static long SKELETON_PEN_COST = 12000;
    public static long IRON_GOLEM_PEN_COST = 100000;
    
    /** Iron blocks required to construct Iron Golem foundry */
    public static int IRON_FOUNDRY_BLOCKS = 4;
    
    // ===== ECONOMY SETTINGS =====
    
    /** Starting balance for new players */
    public static long STARTING_BALANCE = 1000;
    
    /** Whether to enable upkeep costs */
    public static boolean ENABLE_UPKEEP = false;
    
    /** Daily upkeep cost per pen (if enabled) */
    public static long PEN_UPKEEP_COST = 100;
    
    /** Daily upkeep cost per crop slot (if enabled) */
    public static long CROP_UPKEEP_COST = 10;
    
    // ===== AUTO-SAVE SETTINGS =====
    
    /** Auto-save interval (in ticks) */
    public static int AUTO_SAVE_INTERVAL = 6000; // 5 minutes
    
    // ===== INTEGRATION SETTINGS =====
    
    /** Whether to use external economy (like your shop mod) */
    public static boolean USE_EXTERNAL_ECONOMY = false;
    
    /** 
     * If using external economy, this is the mod ID to look for.
     * The CurrencyManager will attempt to hook into it.
     */
    public static String EXTERNAL_ECONOMY_MOD = "shopmod";
}
