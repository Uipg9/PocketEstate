# 🎨 GUI Development - SGUI Library Guide

Complete guide to building inventory GUIs with SGUI for Fabric 1.21.11.

---

## Setup

### Add SGUI Dependency

In `build.gradle`:
```gradle
repositories {
    maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
    // SGUI library - include() bundles it in your mod JAR
    include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))
}
```

### Required Imports
```java
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
```

---

## Basic GUI Structure

### Minimal GUI Example

```java
package com.yourname.modname.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class MyGui extends SimpleGui {
    
    public MyGui(ServerPlayer player) {
        // MenuType determines size: GENERIC_9x1 through GENERIC_9x6
        super(MenuType.GENERIC_9x3, player, false);
        this.setTitle(Component.literal("§6My Menu"));
        buildGui();
    }
    
    private void buildGui() {
        // Add an item at slot 13 (center of 3-row GUI)
        setSlot(13, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§bClick Me!"))
            .addLoreLine(Component.literal("§7This is lore text"))
            .setCallback((index, type, action) -> {
                player.sendSystemMessage(Component.literal("§aYou clicked!"));
            })
        );
        
        // Close button at slot 22
        setSlot(22, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cClose"))
            .setCallback((index, type, action) -> close())
        );
    }
    
    // Static helper to open GUI
    public static void openFor(ServerPlayer player) {
        new MyGui(player).open();
    }
}
```

---

## Menu Types & Sizes

| MenuType | Rows | Slots | Use Case |
|----------|------|-------|----------|
| `GENERIC_9x1` | 1 | 0-8 | Minimal menu |
| `GENERIC_9x2` | 2 | 0-17 | Small menu |
| `GENERIC_9x3` | 3 | 0-26 | Standard menu |
| `GENERIC_9x4` | 4 | 0-35 | Medium menu |
| `GENERIC_9x5` | 5 | 0-44 | Large menu |
| `GENERIC_9x6` | 6 | 0-53 | Full chest GUI |

### Slot Layout (9x6 = 54 slots)
```
Row 0:  [ 0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]
Row 1:  [ 9][10][11][12][13][14][15][16][17]
Row 2:  [18][19][20][21][22][23][24][25][26]
Row 3:  [27][28][29][30][31][32][33][34][35]
Row 4:  [36][37][38][39][40][41][42][43][44]
Row 5:  [45][46][47][48][49][50][51][52][53]
```

### Common Slot Positions
```java
// 6-row GUI (GENERIC_9x6)
int CLOSE_BUTTON = 49;      // Center bottom
int BACK_BUTTON = 45;       // Left bottom
int PREV_PAGE = 48;         // Left of center bottom
int NEXT_PAGE = 50;         // Right of center bottom
int PAGE_INFO = 49;         // Center bottom (same as close)

// Center of each row
int ROW_0_CENTER = 4;
int ROW_1_CENTER = 13;
int ROW_2_CENTER = 22;
int ROW_3_CENTER = 31;
int ROW_4_CENTER = 40;
int ROW_5_CENTER = 49;
```

---

## GuiElementBuilder Methods

### Complete Reference

```java
new GuiElementBuilder(Items.DIAMOND)
    // Display
    .setName(Component.literal("§6Item Name"))
    .addLoreLine(Component.literal("§7Line 1"))
    .addLoreLine(Component.literal("§7Line 2"))
    .setCount(5)                           // Stack size
    .glow()                                // Enchantment glow
    .glow(condition)                       // Conditional glow
    .hideDefaultTooltip()                  // Hide vanilla tooltip
    
    // Click handling
    .setCallback((index, type, action) -> {
        // index = slot clicked
        // type = ClickType (isLeft, isRight, isShift, etc.)
        // action = SlotActionType
    })
    .setCallback(callback)                 // Reusable callback
;
```

### Click Types
```java
.setCallback((index, type, action) -> {
    if (type.isLeft) {
        // Left click
    }
    if (type.isRight) {
        // Right click
    }
    if (type.isMiddle) {
        // Middle click
    }
    if (type.shift) {
        // Shift held
    }
})
```

---

## Full-Featured GUI Example

```java
public class ShopGui extends SimpleGui {
    private final ServerPlayer player;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 28;
    
    // Item slots (4 rows of 7, leaving borders)
    private static final int[] ITEM_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    public ShopGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§lShop"));
        buildGui();
    }
    
    private void buildGui() {
        // Clear all slots
        for (int i = 0; i < 54; i++) {
            setSlot(i, ItemStack.EMPTY);
        }
        
        // Add border (optional, decorative)
        addBorder();
        
        // Add items for current page
        addPageItems();
        
        // Add navigation
        addNavigation();
    }
    
    private void addBorder() {
        ItemStack glass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        glass.setHoverName(Component.literal(" "));
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            setSlot(i, glass);          // Top row
            setSlot(45 + i, ItemStack.EMPTY);  // Clear bottom for nav
        }
        
        // Left and right columns
        for (int row = 1; row < 5; row++) {
            setSlot(row * 9, glass);        // Left
            setSlot(row * 9 + 8, glass);    // Right
        }
    }
    
    private void addPageItems() {
        List<ShopItem> items = ShopManager.getItems();
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < ITEM_SLOTS.length) {
                ShopItem item = items.get(i);
                addShopItem(ITEM_SLOTS[slotIndex], item);
            }
        }
    }
    
    private void addShopItem(int slot, ShopItem item) {
        final ShopItem capturedItem = item;  // Capture for lambda
        
        setSlot(slot, new GuiElementBuilder(item.getItem())
            .setName(Component.literal("§e" + item.getName()))
            .addLoreLine(Component.literal("§7Price: §a$" + item.getPrice()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eClick to buy"))
            .setCallback((index, type, action) -> {
                handlePurchase(capturedItem);
            })
        );
    }
    
    private void handlePurchase(ShopItem item) {
        if (ShopManager.purchase(player, item)) {
            player.sendSystemMessage(Component.literal(
                "§aPurchased " + item.getName() + " for $" + item.getPrice()
            ));
        } else {
            player.sendSystemMessage(Component.literal("§cInsufficient funds!"));
        }
        buildGui();  // Refresh
    }
    
    private void addNavigation() {
        int totalPages = getTotalPages();
        
        // Previous Page (slot 48)
        if (currentPage > 0) {
            setSlot(48, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§l← Previous Page"))
                .addLoreLine(Component.literal("§7Page " + currentPage + "/" + totalPages))
                .setCallback((i, t, a) -> {
                    currentPage--;
                    buildGui();
                })
            );
        } else {
            setSlot(48, ItemStack.EMPTY);
        }
        
        // Page Info (slot 49)
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§ePage " + (currentPage + 1) + "/" + totalPages))
            .addLoreLine(Component.literal("§7Total items: " + ShopManager.getItems().size()))
        );
        
        // Next Page (slot 50)
        if (currentPage < totalPages - 1) {
            setSlot(50, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§lNext Page →"))
                .addLoreLine(Component.literal("§7Page " + (currentPage + 2) + "/" + totalPages))
                .setCallback((i, t, a) -> {
                    currentPage++;
                    buildGui();
                })
            );
        } else {
            setSlot(50, ItemStack.EMPTY);
        }
        
        // Close Button (slot 53)
        setSlot(53, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((i, t, a) -> close())
        );
    }
    
    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil(
            (double) ShopManager.getItems().size() / ITEMS_PER_PAGE
        ));
    }
    
    public static void openFor(ServerPlayer player) {
        new ShopGui(player).open();
    }
}
```

---

## Pagination Pattern (Reusable)

### Generic Paginated GUI Base

```java
public abstract class PaginatedGui<T> extends SimpleGui {
    protected int currentPage = 0;
    protected static final int ITEMS_PER_PAGE = 9;
    
    protected final ServerPlayer player;
    
    public PaginatedGui(ServerPlayer player, String title) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal(title));
    }
    
    // Override in subclass
    protected abstract List<T> getItems();
    protected abstract void renderItem(int slot, T item);
    
    protected void buildGui() {
        // Clear
        for (int i = 0; i < 54; i++) {
            setSlot(i, ItemStack.EMPTY);
        }
        
        // Render items
        List<T> items = getItems();
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());
        
        for (int i = start; i < end; i++) {
            renderItem(i - start, items.get(i));
        }
        
        // Navigation
        int totalPages = getMaxPages();
        
        // Previous
        if (currentPage > 0) {
            setSlot(48, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Previous"))
                .setCallback((a, b, c) -> { currentPage--; buildGui(); })
            );
        }
        
        // Page info
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§ePage " + (currentPage + 1) + "/" + totalPages))
        );
        
        // Next
        if (currentPage < totalPages - 1) {
            setSlot(50, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eNext →"))
                .setCallback((a, b, c) -> { currentPage++; buildGui(); })
            );
        }
    }
    
    protected int getMaxPages() {
        return Math.max(1, (int) Math.ceil((double) getItems().size() / ITEMS_PER_PAGE));
    }
}
```

---

## Common Patterns

### Confirmation Dialog

```java
public class ConfirmGui extends SimpleGui {
    private final Runnable onConfirm;
    private final Runnable onCancel;
    
    public ConfirmGui(ServerPlayer player, String message, 
                      Runnable onConfirm, Runnable onCancel) {
        super(MenuType.GENERIC_9x3, player, false);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        setTitle(Component.literal("§6Confirm"));
        
        // Message
        setSlot(4, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e" + message))
        );
        
        // Confirm (green wool)
        setSlot(11, new GuiElementBuilder(Items.LIME_WOOL)
            .setName(Component.literal("§a§lConfirm"))
            .setCallback((i, t, a) -> {
                close();
                onConfirm.run();
            })
        );
        
        // Cancel (red wool)
        setSlot(15, new GuiElementBuilder(Items.RED_WOOL)
            .setName(Component.literal("§c§lCancel"))
            .setCallback((i, t, a) -> {
                close();
                onCancel.run();
            })
        );
    }
}

// Usage:
new ConfirmGui(player, "Delete all data?",
    () -> { DataManager.deleteAll(player); },
    () -> { player.sendSystemMessage(Component.literal("Cancelled")); }
).open();
```

### Back Button to Previous GUI

```java
public class SubMenuGui extends SimpleGui {
    private final SimpleGui previousGui;
    
    public SubMenuGui(ServerPlayer player, SimpleGui previous) {
        super(MenuType.GENERIC_9x3, player, false);
        this.previousGui = previous;
        buildGui();
    }
    
    private void buildGui() {
        // Back button
        setSlot(18, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e← Back"))
            .setCallback((i, t, a) -> {
                close();
                previousGui.open();
            })
        );
    }
}
```

### Glowing Items (Highlight Selected)

```java
private int selectedIndex = 0;

private void buildGui() {
    for (int i = 0; i < options.size(); i++) {
        final int index = i;
        setSlot(i, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal(options.get(i)))
            .glow(i == selectedIndex)  // Glow if selected
            .setCallback((a, b, c) -> {
                selectedIndex = index;
                buildGui();  // Refresh to update glow
            })
        );
    }
}
```

---

## Tips & Best Practices

### 1. Capture Variables for Lambdas
```java
// Variables used in lambda must be effectively final
for (int i = 0; i < items.size(); i++) {
    final int capturedIndex = i;
    final Item capturedItem = items.get(i);
    
    setSlot(i, new GuiElementBuilder(capturedItem.getIcon())
        .setCallback((a, b, c) -> {
            handleClick(capturedIndex, capturedItem);
        })
    );
}
```

### 2. Always Rebuild After State Changes
```java
.setCallback((i, t, a) -> {
    currentPage++;
    buildGui();  // Refresh the GUI!
})
```

### 3. Use ItemStack.EMPTY for Clearing
```java
setSlot(slot, ItemStack.EMPTY);  // Clear slot
```

### 4. Debounce Rapid Clicks
```java
private long lastClick = 0;

.setCallback((i, t, a) -> {
    long now = System.currentTimeMillis();
    if (now - lastClick < 200) return;  // 200ms cooldown
    lastClick = now;
    // Handle click
})
```

---

## Opening GUI from Command

```java
// In your command class
public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("shop")
        .executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ShopGui.openFor(player);
            return 1;
        })
    );
}
```

---

*SGUI makes GUI development much simpler than vanilla. Use these patterns as your foundation.*
