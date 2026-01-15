# 🎮 Minecraft 1.21.11 Fabric Mod Development Reference

> **Version:** 1.21.11 (specifically - NOT 1.21 or 1.21.1)  
> **Last Updated:** January 2026  
> **Tested With:** Fabric Loader 0.18.4, Fabric API 0.141.1+1.21.11

---

## 📋 Table of Contents

| Document | Description | When to Use |
|----------|-------------|-------------|
| [00_QUICK_START.md](00_QUICK_START.md) | 5-minute setup guide | Starting a new project |
| [01_PROJECT_SETUP.md](01_PROJECT_SETUP.md) | Complete project configuration | Initial setup, dependencies |
| [02_API_REFERENCE.md](02_API_REFERENCE.md) | API changes & class reference | Looking up method signatures |
| [03_GUI_DEVELOPMENT.md](03_GUI_DEVELOPMENT.md) | SGUI library & inventory GUIs | Building any GUI |
| [04_COMMANDS_EVENTS.md](04_COMMANDS_EVENTS.md) | Commands, events, permissions | Adding commands or event handlers |
| [05_DATA_PERSISTENCE.md](05_DATA_PERSISTENCE.md) | NBT storage & save/load | Saving player/world data |
| [06_TROUBLESHOOTING.md](06_TROUBLESHOOTING.md) | Error solutions & debugging | When things break |
| [07_CODE_PATTERNS.md](07_CODE_PATTERNS.md) | Copy-paste ready examples | Common tasks & patterns |

---

## ⚡ Quick Reference

### Critical Configuration (gradle.properties)
```properties
minecraft_version=1.21.11
loader_version=0.18.4
fabric_version=0.141.1+1.21.11
```

### Required in build.gradle
```gradle
mappings loom.officialMojangMappings()  // NOT Yarn!
```

### Essential Imports (Mojang Mappings)
```java
import net.minecraft.server.level.ServerPlayer;      // NOT ServerPlayerEntity
import net.minecraft.network.chat.Component;          // NOT Text
import net.minecraft.nbt.CompoundTag;                 // NOT NbtCompound
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
```

---

## 🔴 Top 5 Mistakes to Avoid

| Mistake | What Happens | Solution |
|---------|--------------|----------|
| Using Yarn mappings | 47+ "cannot find symbol" errors | Use `loom.officialMojangMappings()` |
| Ternary without parentheses | "bad operand types" in Component.literal() | Wrap ternary: `(cond ? "A" : "B") + "text"` |
| Missing java.util imports | "cannot find symbol: List/Map" | Add `import java.util.*;` |
| No save/load hooks | Data lost on restart | Use ServerLifecycleEvents |
| Items.WATCH | "cannot find symbol" | Use `Items.CLOCK` or `Items.COMPASS` |

---

## 🏗️ Recommended Project Structure

```
src/main/java/com/yourname/modname/
├── ModName.java              # Main initializer (ModInitializer)
├── commands/                 # All command classes
├── managers/                 # Business logic & data storage
├── gui/                      # GUI classes (extend SimpleGui)
└── util/                     # Helper classes (DataManager)

src/main/resources/
├── fabric.mod.json           # Mod metadata
└── assets/modid/
    ├── icon.png
    └── lang/en_us.json
```

---

## 🧰 Verified Working Versions

| Component | Version | Notes |
|-----------|---------|-------|
| Minecraft | 1.21.11 | Exact version required |
| Fabric Loader | 0.18.4 | Latest stable |
| Fabric API | 0.141.1+1.21.11 | Match MC version |
| Fabric Loom | 1.14.10 | In build.gradle plugins |
| SGUI | 1.12.0+1.21.11 | For inventory GUIs |
| Java | JDK 21+ | Required minimum |
| Gradle | 9.2.1 | Via wrapper |

---

## 📚 Real-World Experience

This documentation was created from hands-on development of two complete mods:

### QOL Shop Mod (v1.0.52)
- 32 interconnected systems
- 16 mini-games
- 50+ achievements
- Economy, workers, businesses

### Pocket Estate Mod (v1.1.0)
- Virtual farming system
- 54 expandable crop plots with pagination
- Mob farm system with loot tables
- Output buffer pattern for selling

### Key Insights Gathered:
1. **Mojang mappings are essential** for 1.21.11 stability
2. **SGUI simplifies GUI development** significantly vs vanilla
3. **NBT Optional returns** are the biggest API change in 1.21.11
4. **Enchantment API is complex** - avoid or use NBT alternatives
5. **Command conflicts happen** - use subcommands (/mod cmd) not root commands

---

## 🚀 Getting Started

### New to Fabric Modding?
1. Read [00_QUICK_START.md](00_QUICK_START.md) first
2. Set up your project with [01_PROJECT_SETUP.md](01_PROJECT_SETUP.md)
3. Copy patterns from [07_CODE_PATTERNS.md](07_CODE_PATTERNS.md)

### Debugging an Issue?
1. Check [06_TROUBLESHOOTING.md](06_TROUBLESHOOTING.md) for your error
2. Reference [02_API_REFERENCE.md](02_API_REFERENCE.md) for API changes

### Building a GUI?
1. Follow [03_GUI_DEVELOPMENT.md](03_GUI_DEVELOPMENT.md) for SGUI setup
2. Use pagination patterns for large inventories

---

## 📞 Resources

- **Fabric Wiki:** https://fabricmc.net/wiki/
- **Fabric Discord:** https://discord.gg/v6v4pMv
- **SGUI GitHub:** https://github.com/Patbox/sgui
- **Modrinth (Fabric API):** https://modrinth.com/mod/fabric-api

---

## ⚠️ Version Warning

This documentation is **specifically for Minecraft 1.21.11**.

API differences exist between versions:
- 1.21 ≠ 1.21.1 ≠ 1.21.11
- NBT methods return `Optional<T>` in 1.21.11
- Enchantment system uses `Holder<Enchantment>` (complex)
- Some Items constants differ between versions

**Always verify your `gradle.properties` shows `minecraft_version=1.21.11`**

---

*Documentation maintained from real development experience. All code tested and verified.*
