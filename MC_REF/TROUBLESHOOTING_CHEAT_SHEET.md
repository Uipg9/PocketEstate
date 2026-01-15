# FABRIC 1.21.11 MOD TROUBLESHOOTING CHEAT SHEET

**⚠️ IMPORTANT: This is specifically for Minecraft 1.21.11 (not 1.21 or 1.21.1)**

Quick reference for solving common problems in Minecraft 1.21.11 Fabric mod development.

---

## 🔴 BUILD ERRORS

### "cannot find symbol: class ServerPlayer / Component / CompoundTag"
**Problem:** Wrong mappings  
**Fix:**
```gradle
// In build.gradle, line ~40:
mappings loom.officialMojangMappings()
```
Then: `.\gradlew.bat clean build`

---

### "bad operand types for binary operator '+'"
**Problem:** Ternary operator in Component.literal()  
**Example error:**
```java
Component.literal(check ? "Yes" : "No" + " text")
```
**Fix:** Wrap ternary in parentheses
```java
Component.literal((check ? "Yes" : "No") + " text")
```

---

### "cannot find symbol: class List / Map / ArrayList"
**Problem:** Missing imports  
**Fix:** Add to top of file:
```java
import java.util.*;
```

---

### "package does not exist" when importing your own classes
**Problem:** Wrong package or file location  
**Fix:**
1. Check file is in correct folder (commands/ for commands, managers/ for managers)
2. Check package declaration matches folder structure
3. Run `.\gradlew.bat clean build`

---

### "cannot find symbol: variable WATCH"
**Problem:** Item doesn't exist in this version  
**Fix:** Use `Items.COMPASS` or `Items.CLOCK` instead

---

### Build hangs / freezes / never completes
**Problem:** Gradle daemon stuck  
**Fix:**
```powershell
.\gradlew.bat --stop
.\gradlew.bat clean build --no-daemon
```

---

### "Unsupported class file major version 65"
**Problem:** JDK version mismatch - Minecraft 1.21.11 requires Java 21  
**Fix:**
1. Verify Java 21: `java -version`
2. Check gradle.properties: `it.options.release = 21`
3. Update Fabric Loader to 1.21.11 version: `loader_version=0.18.1+1.21.11`

---

### "Task failed with an exception" during build
**Problem:** Syntax error in code  
**Fix:** Read the error carefully - it tells you file name and line number

---

## 🟡 RUNTIME ERRORS

### Mod doesn't appear in mods list
**Problem:** fabric.mod.json misconfigured  
**Check:**
```json
"depends": {
  "fabricloader": ">=0.18.1",
  "fabric-api": "*",
  "minecraft": "~1.21.11",
  "java": ">=21"
}
```

---

### Command doesn't work / "Unknown command"
**Problem:** Command not registered  
**Fix:** In main mod class:
```java
CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
    YourCommand.register(dispatcher);
});
```

---

### GUI doesn't open / crashes
**Problem:** Missing SGui dependency  
**Fix:** In build.gradle dependencies:
```gradle
include(modImplementation("eu.pb4:sgui:1.7.0+1.21"))
```

---

### Data doesn't persist / resets on restart
**Problem:** No save/load hooks  
**Fix:** Add to main mod class:
```java
ServerLifecycleEvents.SERVER_STARTED.register(server -> {
    DataManager.loadData(server);
});

ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
    DataManager.saveData(server);
});
```

---

### NullPointerException when accessing player data
**Problem:** Data not initialized  
**Fix:** Use computeIfAbsent:
```java
playerData.computeIfAbsent(playerId, k -> new PlayerData());
```

---

### Changes to code don't appear in game
**Problem:** Not rebuilding or old JAR still in mods folder  
**Fix:**
```powershell
# Delete old JAR from mods folder
Remove-Item "C:\path\to\mods\yourmod-*.jar"

# Rebuild
.\gradlew.bat build

# Copy new JAR
Copy-Item "build\libs\yourmod-1.0.0.jar" "C:\path\to\mods\"
```

---

## 🟢 GIT / GITHUB ISSUES

### "File too large" when pushing
**Problem:** Committing build files  
**Fix:** Create .gitignore:
```
.gradle/
build/
run/
*.jar
!gradle-wrapper.jar
```
Remove cached:
```powershell
git rm -r --cached .gradle/
git rm -r --cached build/
git commit -m "Remove build artifacts"
```

---

### Can't push to GitHub
**Problem:** No remote configured  
**Fix:**
```powershell
git remote add origin https://github.com/username/repo.git
git push -u origin main
```

---

## 🔍 DEBUGGING CHECKLIST

When something doesn't work:

- [ ] Check logs/latest.log for errors
- [ ] Verify line numbers in error messages
- [ ] Confirm imports are correct
- [ ] Check package declarations
- [ ] Try clean build: `.\gradlew.bat clean build`
- [ ] Restart Minecraft
- [ ] Check spelling/capitalization
- [ ] Verify methods exist in Minecraft 1.21.11 (some APIs changed from 1.20.x)
- [ ] Confirm you're using Minecraft 1.21.11 NOT 1.21 or 1.21.1

---

## 📱 QUICK COMMANDS

```powershell
# Build
.\gradlew.bat build

# Clean build
.\gradlew.bat clean build

# Build without daemon (if stuck)
.\gradlew.bat build --no-daemon

# Stop all daemons
.\gradlew.bat --stop

# Deploy to test instance
Copy-Item "build\libs\*.jar" "C:\path\to\mods\"

# Git workflow
git add .
git commit -m "Description"
git push origin main

# Create release
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin v1.0.0
gh release create v1.0.0 "build\libs\mod-1.0.0.jar"
```

---

## 🎯 Most Common Mistakes

1. ❌ Using Yarn mappings instead of Mojang Official
2. ❌ Forgetting parentheses around ternary in Component.literal()
3. ❌ Missing `import java.util.*;`
4. ❌ Putting files in wrong package folders
5. ❌ Not running clean build after major changes
6. ❌ Committing build/ and .gradle/ to Git
7. ❌ Forgetting to register commands
8. ❌ Not hooking save/load to server lifecycle

---

## 💡 Pro Tips

✅ Always use Mojang Official mappings for Minecraft 1.21.11 (NOT Yarn)  
✅ Confirm your gradle.properties has minecraft_version=1.21.11 (exactly)  
✅ Wrap ALL ternary operators in Component.literal()  
✅ Run `.\gradlew.bat clean build` after dependency changes  
✅ Check logs/latest.log FIRST when debugging  
✅ Test after EVERY major change  
✅ Commit frequently with descriptive messages  
✅ Keep .gitignore updated  
✅ Use `--no-daemon` flag if builds hang  
✅ Remember: We're working with 1.21.11, not 1.21 or 1.21.1  

---

## 📞 Where to Get Help

1. **Check the complete reference:** `MINECRAFT_1.21.11_FABRIC_COMPLETE_REFERENCE.md`
2. **Check this troubleshooting guide** (you are here)
3. **Check logs:** `logs/latest.log` in your Minecraft instance
4. **Fabric Discord:** https://discord.gg/v6v4pMv
5. **Fabric Wiki:** https://fabricmc.net/wiki/

---

**Remember:** 90% of issues are solved by:
1. Using Mojang Official mappings
2. Running clean build
3. Checking the error line number
4. Adding missing imports

Good luck! 🚀
