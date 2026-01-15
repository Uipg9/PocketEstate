# 🔧 Project Setup - Complete Configuration Guide

Complete setup instructions for Fabric 1.21.11 mod development.

---

## Prerequisites

### Required Software
| Software | Version | Download |
|----------|---------|----------|
| Java JDK | 21+ | https://adoptium.net/ |
| Git | Latest | https://git-scm.com/ |
| IDE | VSCode or IntelliJ | Your choice |

### Verify Installation
```powershell
java -version    # Should show 21+
git --version    # Any recent version
```

---

## Project Structure

### Standard Fabric Mod Layout
```
MyMod/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/yourname/modname/
│       │       ├── ModName.java          # Main initializer
│       │       ├── commands/             # Command classes
│       │       │   └── MyCommand.java
│       │       ├── managers/             # Business logic
│       │       │   └── DataManager.java
│       │       ├── gui/                  # GUI classes
│       │       │   └── MyGui.java
│       │       └── util/                 # Helpers
│       │           └── Helper.java
│       └── resources/
│           ├── fabric.mod.json           # Mod metadata
│           └── assets/
│               └── modname/
│                   ├── icon.png          # 128x128 mod icon
│                   └── lang/
│                       └── en_us.json    # Translations
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle                          # Build configuration
├── gradle.properties                     # Version settings
├── settings.gradle                       # Project name
├── gradlew                               # Unix build script
├── gradlew.bat                           # Windows build script
└── .gitignore                            # Git exclusions
```

---

## Configuration Files

### gradle.properties (Complete)

```properties
# ========================================
# MINECRAFT 1.21.11 FABRIC MOD CONFIGURATION
# ========================================
# VERIFIED WORKING - January 2026

# Fabric Properties
minecraft_version=1.21.11
loader_version=0.18.4
fabric_version=0.141.1+1.21.11

# Mod Properties
mod_version=1.0.0
maven_group=com.yourname
archives_base_name=mymod

# Loom Platform
loom.platform=fabric

# Gradle Performance
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
```

### build.gradle (Complete)

```gradle
plugins {
    id 'fabric-loom' version '1.14.10'
    id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

repositories {
    mavenCentral()
    
    // Nucleoid Maven for SGUI library
    maven {
        name = 'Nucleoid'
        url = 'https://maven.nucleoid.xyz/'
    }
}

dependencies {
    // Minecraft
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    
    // ================================
    // CRITICAL: Use Mojang Official Mappings
    // Do NOT use Yarn for 1.21.11 - causes build errors
    // ================================
    mappings loom.officialMojangMappings()
    
    // Fabric
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
    
    // SGUI Library for Inventory GUIs (optional but recommended)
    // include() bundles it into your mod JAR
    include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))
}

processResources {
    inputs.property "version", project.version
    
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 21
}

java {
    withSourcesJar()
    
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

jar {
    from("LICENSE") {
        rename { "${it}_${project.archives_base_name}" }
    }
}
```

### settings.gradle

```gradle
pluginManagement {
    repositories {
        maven { url 'https://maven.fabricmc.net/' }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = 'mymod'
```

### fabric.mod.json (Complete)

```json
{
  "schemaVersion": 1,
  "id": "mymod",
  "version": "${version}",
  "name": "My Mod Name",
  "description": "A description of your mod",
  "authors": [
    "Your Name"
  ],
  "contact": {
    "homepage": "https://github.com/username/mymod",
    "sources": "https://github.com/username/mymod",
    "issues": "https://github.com/username/mymod/issues"
  },
  "license": "MIT",
  "icon": "assets/mymod/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": [
      "com.yourname.mymod.MyMod"
    ]
  },
  "mixins": [],
  "depends": {
    "fabricloader": ">=0.18.0",
    "fabric-api": "*",
    "minecraft": "~1.21.11",
    "java": ">=21"
  },
  "suggests": {}
}
```

### .gitignore

```gitignore
# Gradle
.gradle/
build/
out/

# IDE
.idea/
*.iml
*.ipr
*.iws
.vscode/settings.json
.classpath
.project
.settings/
bin/

# Minecraft
run/
logs/

# OS
.DS_Store
Thumbs.db
desktop.ini

# Keep gradle wrapper
!gradle/wrapper/gradle-wrapper.jar
```

---

## Main Mod Class Template

### Basic Template

```java
package com.yourname.mymod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyMod implements ModInitializer {
    public static final String MOD_ID = "mymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing " + MOD_ID);
        
        // Register systems here
        
        LOGGER.info(MOD_ID + " initialized successfully!");
    }
}
```

### Full-Featured Template

```java
package com.yourname.mymod;

import com.yourname.mymod.commands.MyCommand;
import com.yourname.mymod.managers.DataManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyMod implements ModInitializer {
    public static final String MOD_ID = "mymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static int tickCounter = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing " + MOD_ID);
        
        // ========================================
        // 1. Register Commands
        // ========================================
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            MyCommand.register(dispatcher);
            // Add more commands here
        });
        
        // ========================================
        // 2. Server Lifecycle Events (Data Persistence)
        // ========================================
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DataManager.loadData(server);
            LOGGER.info("Loaded " + MOD_ID + " data");
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DataManager.saveData(server);
            LOGGER.info("Saved " + MOD_ID + " data");
        });
        
        // ========================================
        // 3. Server Tick Events (Auto-save, Timers)
        // ========================================
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            
            // Auto-save every 5 minutes (6000 ticks)
            if (tickCounter >= 6000) {
                DataManager.saveData(server);
                tickCounter = 0;
            }
        });
        
        LOGGER.info(MOD_ID + " initialized successfully!");
    }
}
```

---

## Gradle Wrapper Setup

Ensure you have the correct Gradle wrapper version:

### gradle/wrapper/gradle-wrapper.properties

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

---

## Build Commands

### Standard Build
```powershell
.\gradlew.bat build
```

### Clean Build (after major changes)
```powershell
.\gradlew.bat clean build
```

### Build Without Daemon (if build hangs)
```powershell
.\gradlew.bat --stop
.\gradlew.bat build --no-daemon
```

### Check Dependencies
```powershell
.\gradlew.bat dependencies
```

---

## Output Location

After successful build:
```
build/
└── libs/
    ├── mymod-1.0.0.jar           # Main mod JAR
    └── mymod-1.0.0-sources.jar   # Source code JAR
```

---

## Testing Your Mod

### Deploy to Minecraft Instance
```powershell
# Copy to your Minecraft mods folder
Copy-Item "build\libs\mymod-1.0.0.jar" "C:\path\to\minecraft\mods\"
```

### Verify Mod Loaded
1. Launch Minecraft with Fabric
2. Open Mod Menu (if installed) or check logs
3. Look for your mod in the list

### Check Logs for Errors
```
logs/latest.log
```

---

## Common Setup Issues

### Issue: "Cannot find symbol: ServerPlayer"
**Cause:** Using Yarn mappings instead of Mojang  
**Fix:** Ensure `mappings loom.officialMojangMappings()` in build.gradle

### Issue: Gradle daemon stuck
**Fix:**
```powershell
.\gradlew.bat --stop
.\gradlew.bat build --no-daemon
```

### Issue: Java version mismatch
**Fix:** Install JDK 21, verify with `java -version`

### Issue: Mod not loading
**Check:**
1. fabric.mod.json entrypoint matches your class
2. Package declaration matches file location
3. No errors in logs/latest.log

---

## Next Steps

1. **Add commands:** [04_COMMANDS_EVENTS.md](04_COMMANDS_EVENTS.md)
2. **Create GUIs:** [03_GUI_DEVELOPMENT.md](03_GUI_DEVELOPMENT.md)
3. **Save data:** [05_DATA_PERSISTENCE.md](05_DATA_PERSISTENCE.md)

---

*Project setup complete! Your mod is ready for development.*
