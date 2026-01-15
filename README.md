# Pocket Estate

A **"Vanilla Plus"** management mod for Fabric 1.21.11. Construct and manage a virtual agricultural and industrial complex purely through a beautiful, vanilla-native GUI.

Designed for builders who want resources without the lag of hundreds of entities or the ugliness of massive mob grinders.

## ✨ Features

### 🎨 The Vanilla-Native Interface

We believe mod GUIs shouldn't look like spreadsheets. Pocket Estate utilizes existing game assets to create an immersive "dashboard":

- **Immersive Tabs**: Navigate between "Fields" (Crops), "Pens" (Mobs), and "Sell" tabs
- **Entity Rendering**: Mini-mobs rendered in their slots (concept prepared for client-side implementation)
- **Vanilla Textures**: Built using the standard vanilla palette (Oak Planks, Stone Bricks, Dark UI slots)

### 🌾 The Virtual Garden

Manage your crops in a grid-based plot system:

- **54 Plots Available**: Unlock up to 54 crop plots across 6 pages
- **Pagination System**: Navigate between pages of plots easily
- **Affordable Unlocks**: Progressive pricing from free to 55,000 coins
- **Visual Growth**: Crops display growth progress with progress bars
- **Universal Compatibility**: Supports Wheat, Carrots, Potatoes, Beetroots, Melons, Pumpkins, and Nether Wart
- **One-Click Farming**: Harvest and Replant buttons automate the drudgery

### ⚔️ The Menagerie (Mob Farms)

Construct virtual pens to farm passive and hostile mobs safely from your base:

**The Pasture:**
- Sheep (Wool/Mutton) - Just feed them!
- Cows (Leather/Beef) - Just feed them!
- Chickens (Feathers/Eggs/Chicken) - Just feed them!

**The Dungeon:**
- Spiders (String/Spider Eyes) - Fully automated!
- Zombies (Rotten Flesh/Iron Ingots) - Fully automated!
- Skeletons (Bones/Arrows) - Fully automated!

**The Foundry:**
- Iron Golems (Iron Ingots/Poppies) - Requires 4 Iron Blocks to construct

> **Note:** Tools are no longer required! All production is automatic once you provide fodder.

### 💰 Sell Resources for Money

Turn your harvested crops and mob products into currency:

- **Sell GUI**: Browse all your resources with prices displayed
- **Quick Sell**: Use `/estate sellall` to instantly sell everything
- **Configurable Prices**: All prices defined in `SellPrices.java`

### 🔗 Economy Integration

Works **standalone** OR integrates with external economy mods:

- **MultiEconomy** - Full integration via reflection
- **EasyEconomy** - Full integration via reflection
- **Custom Mods** - Implement `EconomyProvider` interface
- **Standalone Mode** - Uses internal balance when no economy mod present

### 🛠️ Simple Resource System

- **Living Mobs** require Fodder (Wheat/Seeds) to produce resources
- **Hostile Mobs** require Trophies (Rotten Flesh, Bones) to maintain spawn rates
- **Iron Golems** require a one-time investment of Iron Blocks
- **No Tools Required** - All production is automated!

## 🚀 Getting Started

1. **Craft the Estate Ledger:**
   - Recipe: Book + Emerald + Iron Bars (shaped)

2. **Open the GUI:**
   - Right-click with the Estate Ledger, or use `/estate`

3. **Start Farming:**
   - Click on empty crop plots to plant crops
   - Purchase mob pens with currency
   - Add fodder and tools to produce resources

4. **Collect & Sell Resources:**
   - Resources accumulate in the output buffer
   - Click "Collect" to receive your items
   - Use the Sell tab or `/estate sell` to sell for money

## 📝 Commands

| Command | Description |
|---------|-------------|
| `/estate` | Opens the Estate GUI |
| `/pe` | Alias for `/estate` |
| `/estate give` | Gives you an Estate Ledger |
| `/estate balance` | Shows your current balance |
| `/estate sell` | Opens the Sell GUI |
| `/estate sellall` | Quick sell all resources |
| `/estate stats` | Shows estate statistics |
| `/estate help` | Shows help information |
| `/estate addmoney <amount>` | (OP only) Adds money |
| `/estate setmoney <amount>` | (OP only) Sets balance |

## ⚙️ Configuration

Configuration options are available in `EstateConfig.java`:

- Crop growth intervals
- Slot unlock costs
- Mob pen costs
- Production intervals
- Upkeep settings (optional)
- External economy integration (`USE_EXTERNAL_ECONOMY`)

Sell prices are configured in `SellPrices.java`.

## 📦 Dependencies

- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Fabric API 0.141.1+1.21.11
- **SGUI Library** 1.12.0+1.21.11 (included/bundled)

## 🔗 Economy Mod Integration

Pocket Estate automatically detects and integrates with economy mods:

```java
// Enable in EstateConfig.java
public static boolean USE_EXTERNAL_ECONOMY = true;
public static String EXTERNAL_ECONOMY_MOD = "multieconomy";
```

Supported mods:
- **MultiEconomy** by bencrow11
- **EasyEconomy** by Sumutiu

For custom integration, implement `EconomyIntegration.EconomyProvider`:

```java
EconomyIntegration.registerProvider(new EconomyProvider() {
    public long getBalance(UUID playerId) { /* ... */ }
    public void addBalance(UUID playerId, long amount) { /* ... */ }
    public boolean removeBalance(UUID playerId, long amount) { /* ... */ }
    public String getName() { return "MyEconomyMod"; }
});
```

## 📁 Project Structure

```
src/main/java/com/pocketestate/
├── PocketEstate.java          # Main mod initializer
├── command/
│   └── EstateCommand.java     # /estate commands
├── config/
│   ├── EstateConfig.java      # Configuration options
│   └── SellPrices.java        # Item sell prices
├── currency/
│   └── CurrencyManager.java   # Currency helpers
├── economy/
│   └── EconomyIntegration.java # External mod integration
├── data/
│   ├── DataManager.java       # NBT persistence
│   ├── EstateManager.java     # Estate operations
│   └── PlayerData.java        # Per-player data
├── farm/
│   ├── CropPlot.java          # Individual crop plot
│   ├── CropType.java          # Crop type enum
│   ├── MobPen.java            # Individual mob pen
│   ├── PenType.java           # Pen type enum
│   ├── VirtualCropManager.java
│   └── VirtualMobManager.java
├── gui/
│   ├── EstateGui.java         # Main dashboard
│   ├── FieldsGui.java         # Crop management
│   ├── PensGui.java           # Mob pen management
│   └── SellGui.java           # Resource selling
│   ├── FieldsGui.java         # Crop management
│   ├── PensGui.java           # Mob pen management
│   └── EntityRenderHelper.java
└── registry/
    └── ModItems.java          # Item registration
```

## 🏗️ Building

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## 📄 License

MIT License - Feel free to use, modify, and distribute!

---

Made with ❤️ for the Minecraft modding community
