# ⚠️ MINECRAFT VERSION NOTICE ⚠️

## This Documentation is for Minecraft 1.21.11

### NOT:
- ❌ Minecraft 1.21
- ❌ Minecraft 1.21.1
- ❌ Minecraft 1.21.2
- ❌ Any other version

### YES:
- ✅ Minecraft **1.21.11** specifically

---

## Why Version Matters

Different Minecraft versions, even minor point releases, can have significant API changes:

### Changes from 1.20.x to 1.21.11:
- **Enchantment System:** Complete overhaul
  - Old: `ItemStack.enchant(Enchantment enchant, int level)`
  - New: `Holder<Enchantment>` system with ResourceKeys
  - **Impact:** Our project abandoned built-in enchantments due to complexity

- **Mappings Compatibility:** 
  - Yarn mappings incomplete for 1.21.11
  - Mojang Official mappings required
  - **Impact:** 47 build errors until we switched

- **Fabric API Version:**
  - Must use 0.140.0+1.21.11
  - Other versions won't load properly

---

## Differences Between 1.21.x Versions

### 1.21 vs 1.21.11:
- API maturity differences
- Some item constants may differ
- Fabric Loader compatibility

### 1.21.1 vs 1.21.11:
- Different Fabric API versions required
- Potentially different enchantment handling
- Loader version requirements

### 1.21.11 (What We Used):
- Fabric Loader: 0.18.1+1.21.11
- Fabric API: 0.140.0+1.21.11
- Mappings: Mojang Official
- All code tested January 7-11, 2026

---

## Verify Your Version

### In gradle.properties:
```properties
minecraft_version=1.21.11  # ← Must be exactly this
loader_version=0.18.1+1.21.11
fabric_version=0.140.0+1.21.11
```

### In fabric.mod.json:
```json
"depends": {
  "minecraft": "~1.21.11"  # ← Must be exactly this
}
```

### In Minecraft Launcher:
- Check instance version shows **1.21.11**
- Not 1.21, not 1.21.1, exactly **1.21.11**

---

## If Using Different Version

If you're using a different Minecraft version:

1. **Most code patterns will work** - Manager, Command, GUI structures are universal
2. **Mappings might differ** - May need Yarn instead of Mojang for other versions
3. **APIs may have changed** - Especially enchantments, items, entity handling
4. **Dependencies must match** - Update Fabric API and Loader versions
5. **Test thoroughly** - Don't assume code works without testing

---

## Our Verified Configuration

This entire project (32 systems, 16 games, 50+ achievements) was built and tested with:

```
Minecraft: 1.21.11
Fabric Loader: 0.18.1+1.21.11
Fabric API: 0.140.0+1.21.11
Fabric Loom: 1.14.10
Java: JDK 21
Gradle: 9.2.1
Mappings: Mojang Official
SGui: 1.7.0+1.21
```

**All code in this documentation is guaranteed to work with this exact configuration.**

---

## When in Doubt

If you encounter errors and aren't sure if they're version-related:

1. ✅ Verify you're using Minecraft **1.21.11** exactly
2. ✅ Check gradle.properties matches our configuration
3. ✅ Confirm Mojang Official mappings
4. ✅ Run `.\gradlew.bat clean build`
5. ✅ Check error against our Issue Log (Section 14)

90% of version-related issues come from:
- Wrong minecraft_version
- Wrong Fabric API version  
- Wrong mappings
- Mixing 1.21 with 1.21.11

---

**Remember: When we say "1.21.11" we mean exactly that - not 1.21, not 1.21.1, exactly 1.21.11** 🎯
