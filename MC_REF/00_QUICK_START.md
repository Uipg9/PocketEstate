# ⚡ Quick Start Guide - Fabric 1.21.11

Get a working Fabric mod in 5 minutes.

---

## Step 1: Create Project (2 min)

### Option A: Use Fabric Template Generator
1. Go to https://fabricmc.net/develop/template/
2. Set Minecraft version to **1.21.11**
3. Download and extract

### Option B: Clone Example Mod
```powershell
git clone https://github.com/FabricMC/fabric-example-mod
cd fabric-example-mod
```

---

## Step 2: Configure gradle.properties (30 sec)

Replace contents with:

```properties
# Fabric Properties - VERIFIED WORKING FOR 1.21.11
minecraft_version=1.21.11
loader_version=0.18.4
fabric_version=0.141.1+1.21.11

# Mod Properties
mod_version=1.0.0
maven_group=com.yourname
archives_base_name=mymod

# Build Settings
loom.platform=fabric
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
```

---

## Step 3: Configure build.gradle (1 min)

**Critical change - use Mojang mappings:**

```gradle
plugins {
    id 'fabric-loom' version '1.14.10'
    id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

repositories {
    mavenCentral()
    maven { url 'https://maven.nucleoid.xyz' }  // For SGUI
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    
    // CRITICAL: Use Mojang mappings, NOT Yarn
    mappings loom.officialMojangMappings()
    
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
    
    // SGUI for inventory GUIs (optional but recommended)
    include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

---

## Step 4: Build & Test (1 min)

```powershell
# Build the mod
.\gradlew.bat build

# Output: build/libs/mymod-1.0.0.jar
```

---

## Step 5: Basic Mod Class

```java
package com.yourname.mymod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyMod implements ModInitializer {
    public static final String MOD_ID = "mymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loading " + MOD_ID);
        
        // Register a simple command: /hello
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            dispatcher.register(Commands.literal("hello")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§aHello from MyMod!"));
                    return 1;
                })
            );
        });
        
        LOGGER.info(MOD_ID + " loaded!");
    }
}
```

---

## Step 6: fabric.mod.json

```json
{
  "schemaVersion": 1,
  "id": "mymod",
  "version": "${version}",
  "name": "My Mod",
  "description": "A Fabric mod for Minecraft 1.21.11",
  "authors": ["Your Name"],
  "license": "MIT",
  "icon": "assets/mymod/icon.png",
  "environment": "*",
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

---

## ✅ Checklist

- [ ] gradle.properties has `minecraft_version=1.21.11`
- [ ] build.gradle uses `loom.officialMojangMappings()`
- [ ] fabric.mod.json entrypoint matches your main class
- [ ] Java imports use Mojang names (ServerPlayer, Component, CompoundTag)
- [ ] Build succeeds with `.\gradlew.bat build`

---

## 🚨 If Build Fails

### "cannot find symbol: ServerPlayer"
```gradle
// In build.gradle, ensure you have:
mappings loom.officialMojangMappings()
```

### Build hangs
```powershell
.\gradlew.bat --stop
.\gradlew.bat build --no-daemon
```

### "Unsupported class file major version 65"
- Install JDK 21
- Verify with `java -version`

---

## Next Steps

1. **Add commands:** See [04_COMMANDS_EVENTS.md](04_COMMANDS_EVENTS.md)
2. **Create GUIs:** See [03_GUI_DEVELOPMENT.md](03_GUI_DEVELOPMENT.md)
3. **Save data:** See [05_DATA_PERSISTENCE.md](05_DATA_PERSISTENCE.md)
4. **Common patterns:** See [07_CODE_PATTERNS.md](07_CODE_PATTERNS.md)

---

*You now have a working Fabric 1.21.11 mod! 🎉*
