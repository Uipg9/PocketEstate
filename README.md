# Pocket Estate

A **"Vanilla Plus"** management mod for Fabric 1.21.11. Construct and manage a virtual agricultural and industrial complex purely through a beautiful, vanilla-native GUI.

Designed for builders who want resources without the lag of hundreds of entities or the ugliness of massive mob grinders.

## ✨ Features

### 🎨 The Vanilla-Native Interface

We believe mod GUIs shouldn't look like spreadsheets. Pocket Estate utilizes existing game assets to create an immersive "dashboard":

- **Immersive Tabs**: Navigate between "Fields" (Crops), "Pens" (Mobs), and "Sell" tabs
- **Full-Screen Farm GUI**: View 21 crop plots at once (7x3 grid) with pagination
- **Vanilla Textures**: Built using the standard vanilla palette (Oak Planks, Stone Bricks, Dark UI slots)
- **Glow Effects**: Ready crops and available actions glow to attract attention

### 🌾 The Virtual Garden (v1.2.0 Redesign!)

Manage your crops in a redesigned full-screen farm system with idle-game mechanics:

- **180 Plots Available**: Unlock up to 180 crop plots for massive farming!
- **Full-Screen Layout**: View 21 plots at once in a 7x3 grid with pagination
- **Idle-Game Mechanics**: XP and money rewards for every harvest!
- **Plant All Button**: Select a crop type and plant in all empty plots at once
- **Harvest All Button**: Harvest all ready crops with one click
- **Bonemeal Boost**: Store bonemeal and boost all growing crops by 25%
  - Left-click to use stored bonemeal
  - Right-click to add bonemeal from your inventory
- **Compost Bin**: Passive bonemeal production over time
  - Add crops from your output to speed up compost production
- **Buy Plots**: Purchase new plots with in-game currency
  - Left-click: Buy 1 plot
  - Right-click: Buy 5 plots at once
- **Auto-Harvest Toggle**: Automatically harvest ready crops!
- **Crop Output Buffer**: Harvested crops go to a "Collect" buffer
- **Stats Tracking**: Track total crops harvested, XP earned, and money earned
- **Visual Growth Bars**: See exact growth progress for each crop

**Supported Crops:**
- Wheat (30s growth, 5x yield)
- Carrots (40s growth, 3x yield)
- Potatoes (40s growth, 4x yield)
- Beetroots (50s growth, 2x yield)
- Melons (60s growth, 4x yield)
- Pumpkins (60s growth, 1x yield)
- Nether Wart (75s growth, 3x yield)

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

## 🚀 Getting Started

1. **Craft the Estate Ledger:**
   - Recipe: Book + Emerald + Iron Bars (shaped)

2. **Open the GUI:**
   - Right-click with the Estate Ledger, or use `/estate`

3. **Start Farming:**
   - Click on empty crop plots to plant crops
   - Use "Plant All" to plant in all empty plots at once
   - Watch your crops grow and harvest with "Harvest All"
   - Use bonemeal to speed up growth!

4. **Collect & Sell Resources:**
   - Click "Collect" to receive your harvested crops
   - Use the Sell tab or `/estate sell` to sell for money
   - Use money to buy more plots and expand your farm!

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
- External economy integration

## 📦 Dependencies

- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Fabric API 0.141.1+1.21.11
- **SGUI Library** 1.12.0+1.21.11 (included/bundled)

## 📜 Changelog

### v1.2.0 - Major Farm Redesign
- **Full-Screen Farm GUI**: New 7x3 grid layout showing 21 plots at once
- **Increased Plot Limit**: Now supports up to 180 crop plots!
- **Idle-Game Mechanics**: XP and money rewards for harvesting
- **Plant All Feature**: Plant same crop in all empty plots
- **Harvest All Feature**: Harvest all ready crops with one click
- **Bonemeal Storage**: Store bonemeal and boost all crops at once
- **Bonemeal from Inventory**: Right-click to add bonemeal from your inventory
- **Compost Bin**: Passive bonemeal production over time
- **Auto-Harvest Toggle**: Automatically harvest when crops are ready
- **Stats Tracking**: Track harvests, XP earned, and money earned
- **Glow Effects**: Visual indicators for ready crops and available actions
- **Buy Plots Button**: Purchase new plots with left/right click options

### v1.1.0 - Initial Release
- Basic crop farming with 54 plots
- Mob pens for passive and hostile mob farming
- Economy integration with MultiEconomy and EasyEconomy
- Sell GUI for turning resources into money

## 🏗️ Building

`ash
./gradlew build
`

The built JAR will be in `build/libs/`.

## 📄 License

MIT License - Feel free to use, modify, and distribute!

---

Made with ❤️ for the Minecraft modding community
