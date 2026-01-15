# 🔧 Troubleshooting Guide

Complete solutions for common Fabric 1.21.11 modding issues.

---

## 🔴 Build Errors

### Error: "cannot find symbol: class ServerPlayer / Component / CompoundTag"

**Symptom:**
```
error: cannot find symbol
  symbol:   class ServerPlayer
  location: package net.minecraft.server.level
```

**Cause:** Using Yarn mappings instead of Mojang Official mappings

**Solution:**
```gradle
// In build.gradle, change:
mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"

// To:
mappings loom.officialMojangMappings()
```

Then run:
```powershell
.\gradlew.bat clean build
```

---

### Error: "bad operand types for binary operator '+'"

**Symptom:**
```
error: bad operand types for binary operator '+'
    return Component.literal(check ? "Yes" : "No" + " text");
                                                       ^
```

**Cause:** Java operator precedence - ternary has lower precedence than `+`

**Solution:** Wrap ternary in parentheses:
```java
// ❌ Wrong
Component.literal(check ? "Yes" : "No" + " text");

// ✅ Correct
Component.literal((check ? "Yes" : "No") + " text");
```

---

### Error: "cannot find symbol: class List / Map / ArrayList"

**Symptom:**
```
error: cannot find symbol
  symbol:   class List
```

**Solution:** Add imports:
```java
import java.util.*;
// Or specific imports:
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
```

---

### Error: "cannot find symbol: variable WATCH"

**Symptom:**
```
error: cannot find symbol
  symbol:   variable WATCH
  location: class Items
```

**Cause:** `Items.WATCH` doesn't exist in 1.21.11

**Solution:**
```java
// ❌ Wrong
Items.WATCH

// ✅ Correct
Items.CLOCK
// or
Items.COMPASS
```

---

### Error: "incompatible types: Optional<Integer> cannot be converted to int"

**Symptom:**
```
error: incompatible types: Optional<Integer> cannot be converted to int
    int value = tag.getInt("key");
```

**Cause:** In 1.21.11, NBT getters return Optional

**Solution:**
```java
// ❌ Old way (1.20.x)
int value = tag.getInt("key");

// ✅ New way (1.21.11)
int value = tag.getInt("key").orElse(0);
```

---

### Error: "no suitable method found for writeCompressed(CompoundTag, File)"

**Cause:** NbtIo now requires Path, not File

**Solution:**
```java
// ❌ Old way
NbtIo.writeCompressed(tag, file);

// ✅ New way
NbtIo.writeCompressed(tag, file.toPath());
```

---

### Error: "no suitable method found for readCompressed(Path)"

**Cause:** readCompressed now requires NbtAccounter

**Solution:**
```java
// ❌ Old way
CompoundTag tag = NbtIo.readCompressed(path);

// ✅ New way
CompoundTag tag = NbtIo.readCompressed(
    path, 
    NbtAccounter.unlimitedHeap()
);
```

---

### Error: Package structure mismatch

**Symptom:**
```
error: package com.yourname.modname.commands does not exist
```

**Cause:** File location doesn't match package declaration

**Solution:**
1. Check file is in correct folder:
   - Commands go in `src/main/java/com/yourname/modname/commands/`
2. Check package declaration matches:
   ```java
   package com.yourname.modname.commands;
   ```
3. Run clean build:
   ```powershell
   .\gradlew.bat clean build
   ```

---

### Build Hangs / Never Completes

**Symptom:** Build runs for 10+ minutes without progress

**Solution:**
```powershell
# Stop all Gradle daemons
.\gradlew.bat --stop

# Build without daemon
.\gradlew.bat build --no-daemon
```

Alternative - increase memory:
```properties
# In gradle.properties
org.gradle.jvmargs=-Xmx4G
```

---

### Error: "Unsupported class file major version 65"

**Cause:** Java version mismatch - need JDK 21

**Solution:**
1. Install JDK 21: https://adoptium.net/
2. Verify: `java -version` should show 21+
3. Check gradle.properties:
   ```properties
   it.options.release = 21
   ```

---

## 🟡 Runtime Errors

### Mod Doesn't Appear in Mods List

**Check fabric.mod.json:**
```json
{
  "schemaVersion": 1,
  "id": "mymod",
  "version": "${version}",
  "entrypoints": {
    "main": ["com.yourname.mymod.MyMod"]
  },
  "depends": {
    "fabricloader": ">=0.18.0",
    "fabric-api": "*",
    "minecraft": "~1.21.11",
    "java": ">=21"
  }
}
```

**Verify:**
- Entrypoint class path matches your main class exactly
- Package/class exists at that location
- Check `logs/latest.log` for errors

---

### Command Says "Unknown command"

**Cause:** Command not registered

**Solution:** Register in main mod class:
```java
@Override
public void onInitialize() {
    CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
        MyCommand.register(dispatcher);
    });
}
```

---

### GUI Doesn't Open / Crashes

**Check for SGUI dependency:**
```gradle
dependencies {
    include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))
}
```

**Check MenuType:**
```java
// For 6-row chest:
super(MenuType.GENERIC_9x6, player, false);
```

---

### Data Resets on Restart

**Cause:** No save/load lifecycle hooks

**Solution:**
```java
ServerLifecycleEvents.SERVER_STARTED.register(server -> {
    DataManager.loadData(server);
});

ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
    DataManager.saveData(server);
});
```

---

### NullPointerException on Player Data

**Cause:** Data not initialized before access

**Solution:** Use `computeIfAbsent`:
```java
public static PlayerData getData(UUID id) {
    return playerData.computeIfAbsent(id, k -> new PlayerData());
}
```

---

### Changes Don't Appear in Game

**Workflow:**
1. Stop Minecraft
2. Delete old JAR from mods folder
3. Run `.\gradlew.bat build`
4. Copy new JAR: `build/libs/modname-1.0.0.jar`
5. Start Minecraft

---

## 🟢 Git Issues

### Git Push Rejected - Large Files

**Symptom:**
```
remote: error: File .gradle/cache.bin is 124.35 MB
```

**Solution:**
1. Create `.gitignore`:
   ```gitignore
   .gradle/
   build/
   run/
   logs/
   ```
2. Remove cached files:
   ```powershell
   git rm -r --cached .gradle/
   git rm -r --cached build/
   git commit -m "Remove build artifacts"
   git push
   ```

---

### Can't Push to GitHub

**Check remote:**
```powershell
git remote -v
```

**Add remote if missing:**
```powershell
git remote add origin https://github.com/username/repo.git
git push -u origin main
```

---

## 📋 Debug Checklist

### Build Failed?
- [ ] Using `loom.officialMojangMappings()` (not Yarn)?
- [ ] All ternary operators in Component.literal() wrapped in parentheses?
- [ ] `import java.util.*;` present?
- [ ] No `Items.WATCH` (use CLOCK or COMPASS)?
- [ ] NBT methods using `.orElse()` for Optional returns?
- [ ] File locations match package declarations?

### Mod Not Loading?
- [ ] fabric.mod.json entrypoint correct?
- [ ] Main class implements `ModInitializer`?
- [ ] Minecraft version in fabric.mod.json is `~1.21.11`?
- [ ] Check `logs/latest.log` for errors?

### Commands Not Working?
- [ ] Registered in `CommandRegistrationCallback.EVENT`?
- [ ] Not conflicting with other mod commands?
- [ ] Using correct permission level in `.requires()`?

### Data Not Saving?
- [ ] `ServerLifecycleEvents.SERVER_STARTED` loads data?
- [ ] `ServerLifecycleEvents.SERVER_STOPPING` saves data?
- [ ] NBT methods using Optional correctly?
- [ ] File path correct (world folder)?

### GUI Issues?
- [ ] SGUI dependency included?
- [ ] Correct MenuType for size needed?
- [ ] Callbacks capture variables correctly (final)?
- [ ] Calling `buildGui()` after state changes?

---

## 🛠️ Quick Commands

```powershell
# Clean build
.\gradlew.bat clean build

# Build without daemon (if hanging)
.\gradlew.bat --stop
.\gradlew.bat build --no-daemon

# Check Java version
java -version

# Deploy to mods folder
Copy-Item "build\libs\*.jar" "C:\path\to\minecraft\mods\"

# Git workflow
git add .
git commit -m "Description"
git push origin main

# Create release
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin v1.0.0
gh release create v1.0.0 "build\libs\modname-1.0.0.jar"
```

---

## 🎯 90% of Issues Are Solved By:

1. **Using Mojang Official mappings** instead of Yarn
2. **Running clean build** after major changes
3. **Checking error line numbers** and fixing that exact line
4. **Adding missing imports** (java.util.*)
5. **Wrapping ternary operators** in parentheses

---

## 📞 Still Stuck?

1. **Read the full error message** - it tells you the file and line
2. **Check `logs/latest.log`** for runtime errors
3. **Search the error** on Fabric Discord
4. **Compare with working examples** in this documentation

---

*Most issues have simple solutions - read errors carefully!*
