package com.pocketestate.economy;

import com.pocketestate.PocketEstate;
import com.pocketestate.config.EstateConfig;
import com.pocketestate.config.SellPrices;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Economy integration layer for Pocket Estate.
 * 
 * Supports:
 * - Standalone mode (uses internal PlayerData balance)
 * - External economy mods (MultiEconomy, EasyEconomy, or custom mods)
 * 
 * When an external economy is detected and enabled, this class will
 * delegate balance operations to that mod instead of using internal storage.
 */
public class EconomyIntegration {
    
    private static boolean initialized = false;
    private static EconomyProvider provider = null;
    
    /**
     * Economy provider interface for external mod integration
     */
    public interface EconomyProvider {
        long getBalance(UUID playerId);
        void addBalance(UUID playerId, long amount);
        boolean removeBalance(UUID playerId, long amount);
        String getName();
    }
    
    /**
     * Initialize the economy integration
     * Call this during mod initialization
     */
    public static void initialize() {
        if (initialized) return;
        initialized = true;
        
        if (!EstateConfig.USE_EXTERNAL_ECONOMY) {
            PocketEstate.LOGGER.info("Using internal economy (standalone mode)");
            return;
        }
        
        // Try to find external economy mods
        FabricLoader loader = FabricLoader.getInstance();
        
        // Check for MultiEconomy
        if (loader.isModLoaded("multieconomy")) {
            try {
                provider = createMultiEconomyProvider();
                PocketEstate.LOGGER.info("Integrated with MultiEconomy!");
                return;
            } catch (Exception e) {
                PocketEstate.LOGGER.warn("Failed to integrate with MultiEconomy: " + e.getMessage());
            }
        }
        
        // Check for EasyEconomy
        if (loader.isModLoaded("easyeconomy")) {
            try {
                provider = createEasyEconomyProvider();
                PocketEstate.LOGGER.info("Integrated with EasyEconomy!");
                return;
            } catch (Exception e) {
                PocketEstate.LOGGER.warn("Failed to integrate with EasyEconomy: " + e.getMessage());
            }
        }
        
        // Check for custom mod ID
        if (loader.isModLoaded(EstateConfig.EXTERNAL_ECONOMY_MOD)) {
            PocketEstate.LOGGER.info("External economy mod '" + EstateConfig.EXTERNAL_ECONOMY_MOD + 
                "' detected but no integration available. Using internal economy.");
        }
        
        // Fallback to internal
        PocketEstate.LOGGER.info("No external economy integration found. Using internal economy.");
    }
    
    /**
     * Check if external economy is being used
     */
    public static boolean isUsingExternalEconomy() {
        return provider != null;
    }
    
    /**
     * Get the name of the active economy provider
     */
    public static String getProviderName() {
        return provider != null ? provider.getName() : "Pocket Estate";
    }
    
    /**
     * Get player balance (delegates to provider or internal)
     */
    public static long getBalance(UUID playerId) {
        if (provider != null) {
            return provider.getBalance(playerId);
        }
        // Internal balance
        if (PocketEstate.dataManager == null) return 0;
        return PocketEstate.dataManager.getBalance(playerId);
    }
    
    /**
     * Add to player balance
     */
    public static void addBalance(UUID playerId, long amount) {
        if (provider != null) {
            provider.addBalance(playerId, amount);
        } else {
            if (PocketEstate.dataManager != null) {
                PocketEstate.dataManager.addBalance(playerId, amount);
            }
        }
    }
    
    /**
     * Remove from player balance
     * @return true if successful, false if insufficient funds
     */
    public static boolean removeBalance(UUID playerId, long amount) {
        if (provider != null) {
            return provider.removeBalance(playerId, amount);
        }
        // Internal balance
        if (PocketEstate.dataManager == null) return false;
        long current = PocketEstate.dataManager.getBalance(playerId);
        if (current < amount) return false;
        PocketEstate.dataManager.addBalance(playerId, -amount);
        return true;
    }
    
    /**
     * Sell an item stack
     * @return the amount earned, or 0 if item cannot be sold
     */
    public static long sellItem(ServerPlayer player, ItemStack stack) {
        long value = SellPrices.getValue(stack);
        if (value > 0) {
            addBalance(player.getUUID(), value);
        }
        return value;
    }
    
    /**
     * Register a custom economy provider (for other mods to integrate)
     */
    public static void registerProvider(EconomyProvider newProvider) {
        if (newProvider != null) {
            provider = newProvider;
            PocketEstate.LOGGER.info("Registered custom economy provider: " + newProvider.getName());
        }
    }
    
    // ===== Provider Factories =====
    
    /**
     * Create MultiEconomy integration provider using reflection
     */
    private static EconomyProvider createMultiEconomyProvider() throws Exception {
        // Load classes via reflection to avoid hard dependency
        Class<?> accountManagerClass = Class.forName("dev.bencrow.multieconomy.account.AccountManager");
        Class<?> configManagerClass = Class.forName("dev.bencrow.multieconomy.config.ConfigManager");
        
        // Get methods
        Method getAccount = accountManagerClass.getMethod("getAccount", UUID.class);
        Method getConfig = configManagerClass.getMethod("getConfig");
        
        return new EconomyProvider() {
            @Override
            public long getBalance(UUID playerId) {
                try {
                    Object account = getAccount.invoke(null, playerId);
                    if (account == null) return 0;
                    
                    // Get default currency balance
                    Object config = getConfig.invoke(null);
                    Method getDefaultCurrency = config.getClass().getMethod("getDefaultCurrency");
                    Object currency = getDefaultCurrency.invoke(config);
                    
                    Method getBalanceMethod = account.getClass().getMethod("getBalance", currency.getClass());
                    Object balance = getBalanceMethod.invoke(account, currency);
                    return ((Number) balance).longValue();
                } catch (Exception e) {
                    PocketEstate.LOGGER.warn("MultiEconomy getBalance failed: " + e.getMessage());
                    return 0;
                }
            }
            
            @Override
            public void addBalance(UUID playerId, long amount) {
                try {
                    Object account = getAccount.invoke(null, playerId);
                    if (account == null) return;
                    
                    Object config = getConfig.invoke(null);
                    Method getDefaultCurrency = config.getClass().getMethod("getDefaultCurrency");
                    Object currency = getDefaultCurrency.invoke(config);
                    
                    Method addMethod = account.getClass().getMethod("add", currency.getClass(), float.class);
                    addMethod.invoke(account, currency, (float) amount);
                } catch (Exception e) {
                    PocketEstate.LOGGER.warn("MultiEconomy addBalance failed: " + e.getMessage());
                }
            }
            
            @Override
            public boolean removeBalance(UUID playerId, long amount) {
                try {
                    Object account = getAccount.invoke(null, playerId);
                    if (account == null) return false;
                    
                    Object config = getConfig.invoke(null);
                    Method getDefaultCurrency = config.getClass().getMethod("getDefaultCurrency");
                    Object currency = getDefaultCurrency.invoke(config);
                    
                    Method removeMethod = account.getClass().getMethod("remove", currency.getClass(), float.class);
                    Object result = removeMethod.invoke(account, currency, (float) amount);
                    return (Boolean) result;
                } catch (Exception e) {
                    PocketEstate.LOGGER.warn("MultiEconomy removeBalance failed: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            public String getName() {
                return "MultiEconomy";
            }
        };
    }
    
    /**
     * Create EasyEconomy integration provider using reflection
     */
    private static EconomyProvider createEasyEconomyProvider() throws Exception {
        Class<?> bankStorageClass = Class.forName("com.sumutiu.easyeconomy.storage.BankStorage");
        
        Method getBalance = bankStorageClass.getMethod("getBalance", UUID.class);
        Method addBalance = bankStorageClass.getMethod("addBalance", UUID.class, long.class);
        Method removeBalance = bankStorageClass.getMethod("removeBalance", UUID.class, long.class);
        
        return new EconomyProvider() {
            @Override
            public long getBalance(UUID playerId) {
                try {
                    return (Long) getBalance.invoke(null, playerId);
                } catch (Exception e) {
                    return 0;
                }
            }
            
            @Override
            public void addBalance(UUID playerId, long amount) {
                try {
                    addBalance.invoke(null, playerId, amount);
                } catch (Exception e) {
                    PocketEstate.LOGGER.warn("EasyEconomy addBalance failed: " + e.getMessage());
                }
            }
            
            @Override
            public boolean removeBalance(UUID playerId, long amount) {
                try {
                    return (Boolean) removeBalance.invoke(null, playerId, amount);
                } catch (Exception e) {
                    return false;
                }
            }
            
            @Override
            public String getName() {
                return "EasyEconomy";
            }
        };
    }
}
