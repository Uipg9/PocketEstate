# Pocket Estate - Test Checklist v1.0.0

## Pre-Test Setup
- [ ] Run `./gradlew clean build`
- [ ] Run `./gradlew runClient`
- [ ] Create new world in creative mode

---

## 1. Basic Commands
- [ ] `/estate` - Opens Estate GUI
- [ ] `/pe` - Alias opens Estate GUI
- [ ] `/estate give` - Gives Estate Ledger item
- [ ] `/estate balance` - Shows balance (starting $1,000)
- [ ] `/estate stats` - Shows estate statistics
- [ ] `/estate help` - Shows help text
- [ ] `/estate addmoney 500` - Adds $500 to balance
- [ ] `/estate setmoney 5000` - Sets balance to $5,000
- [ ] `/sell` - Opens Sell GUI

---

## 2. Estate Ledger Item
- [ ] Item appears in inventory after `/estate give`
- [ ] Right-click item opens Estate GUI
- [ ] Item shows correct name "Estate Ledger"
- [ ] Item has lore/description

---

## 3. Estate GUI - Overview Tab
- [ ] GUI opens with title "✦ Pocket Estate ✦"
- [ ] Balance displayed correctly
- [ ] Fields quick-access button works
- [ ] Pens quick-access button works
- [ ] Collect button shows pending items count
- [ ] Close button works
- [ ] All tabs visible (Overview, Fields, Pens, Collect, Sell)

---

## 4. Fields Tab (Crop Farming)
- [ ] Click Fields tab or button
- [ ] Shows 9 crop slots (1 unlocked by default)
- [ ] Click unlocked slot to plant crop
- [ ] Crop selection GUI appears
- [ ] Plant Wheat - shows planted status
- [ ] Wait ~10 seconds for growth stage
- [ ] Growth progress displayed
- [ ] When ready, shows "Ready to Harvest"
- [ ] Harvest button collects crop
- [ ] Harvested items go to output buffer
- [ ] Unlock slot 2 for $500
- [ ] Slot 2 becomes usable

---

## 5. Pens Tab (Mob Farming)
- [ ] Click Pens tab
- [ ] Shows available pen types to unlock
- [ ] Chicken Pen costs $3,000
- [ ] Sheep Pen costs $5,000
- [ ] Cow Pen costs $7,500
- [ ] Use `/estate addmoney 10000` if needed
- [ ] Purchase a Chicken Pen
- [ ] Pen shows in owned pens
- [ ] Add fodder (wheat/seeds) to pen
- [ ] Wait ~1 minute for production cycle
- [ ] Eggs/feathers appear in output buffer

---

## 6. Collect Tab
- [ ] Click Collect tab
- [ ] Shows all pending items from crops/pens
- [ ] Shows item counts and types
- [ ] "Collect All" button works
- [ ] Items transfer to player inventory
- [ ] Shows "No Resources" when empty

---

## 7. Sell Tab & Selling
- [ ] Click Sell tab - opens Sell GUI
- [ ] `/estate sell` opens Sell GUI
- [ ] Shows all items in output buffer
- [ ] Each item shows:
  - [ ] Item name
  - [ ] Amount
  - [ ] Price per item
  - [ ] Total value
- [ ] Click item to sell individual stack
- [ ] Balance increases correctly
- [ ] "Sell All" button sells everything
- [ ] Total value displayed correctly
- [ ] `/estate sellall` quick sells all resources
- [ ] Chat message shows sale amount
- [ ] Back button returns to Estate GUI

---

## 8. Sell Prices Verification
Test a few items have correct prices:
- [ ] Wheat = $5 each
- [ ] Carrot = $8 each
- [ ] Potato = $8 each
- [ ] Egg = $10 each
- [ ] Feather = $5 each
- [ ] White Wool = $15 each
- [ ] Iron Ingot = $50 each

---

## 9. Economy Integration (Standalone Mode)
- [ ] Balance shows "Pocket Estate" as provider
- [ ] `/estate balance` works without external mods
- [ ] Selling adds to internal balance
- [ ] Purchases deduct from internal balance
- [ ] Balance persists after world reload

---

## 10. Data Persistence
- [ ] Plant crops, unlock pens
- [ ] Save and quit world
- [ ] Reload world
- [ ] Crops/pens still present
- [ ] Balance preserved
- [ ] Output buffer preserved
- [ ] Unlocked slots preserved

---

## 11. Error Handling
- [ ] `/estate sellall` with empty buffer shows "No resources"
- [ ] Trying to buy pen without funds shows error
- [ ] Trying to plant in locked slot shows error/nothing happens

---

## Test Results

| Category | Passed | Failed | Notes |
|----------|--------|--------|-------|
| Commands | | | |
| Item | | | |
| Overview GUI | | | |
| Fields | | | |
| Pens | | | |
| Collect | | | |
| Selling | | | |
| Prices | | | |
| Economy | | | |
| Persistence | | | |
| Errors | | | |

**Overall Status:** [ ] PASS / [ ] FAIL

**Tester:** _______________  
**Date:** _______________  
**Version:** 1.0.0
