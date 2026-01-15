package com.pocketestate;

import com.pocketestate.command.EstateCommand;
import com.pocketestate.data.DataManager;
import com.pocketestate.data.EstateManager;
import com.pocketestate.economy.EconomyIntegration;
import com.pocketestate.farm.VirtualCropManager;
import com.pocketestate.farm.VirtualMobManager;
import com.pocketestate.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pocket Estate - A "Vanilla Plus" management mod for Fabric 1.21.11
 * 
 * Allows players to construct and manage a virtual agricultural and 
 * industrial complex purely through a beautiful, vanilla-native GUI.
 * 
 * Features:
 * - Virtual crop fields with growth mechanics
 * - Virtual mob pens with production cycles
 * - Sell harvested resources for currency
 * - Integration with external economy mods (MultiEconomy, EasyEconomy)
 * - Works standalone or alongside other economy mods
 */
public class PocketEstate implements ModInitializer {
    public static final String MOD_ID = "pocketestate";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Manager instances
    public static DataManager dataManager;
    
    // Tick counter for periodic processing
    private static long tickCounter = 0;
    private static final int PRODUCTION_INTERVAL = 1200; // 1 minute (60 seconds * 20 ticks)
    private static final int CROP_GROWTH_INTERVAL = 200;  // 10 seconds
    
    /**
     * Create an Identifier for this mod
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Pocket Estate...");
        
        // Register items
        ModItems.register();
        
        // Initialize economy integration
        EconomyIntegration.initialize();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EstateCommand.register(dispatcher);
        });
        
        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            dataManager = new DataManager(server);
            dataManager.load();
            LOGGER.info("Pocket Estate data loaded!");
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (dataManager != null) {
                dataManager.save();
                LOGGER.info("Pocket Estate data saved!");
            }
        });
        
        // Auto-save every 5 minutes
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerTickEvents.END_SERVER_TICK.register(s -> {
                tickCounter++;
                
                // Process crop growth
                if (tickCounter % CROP_GROWTH_INTERVAL == 0) {
                    VirtualCropManager.processCropGrowth(s);
                }
                
                // Process mob production
                if (tickCounter % PRODUCTION_INTERVAL == 0) {
                    VirtualMobManager.processProduction(s);
                }
                
                // Auto-save every 5 minutes (6000 ticks)
                if (tickCounter % 6000 == 0) {
                    if (dataManager != null) {
                        dataManager.save();
                    }
                }
            });
        });
        
        LOGGER.info("Pocket Estate initialized successfully!");
    }
}
