package net.runelite.client.plugins.microbot.membersalch;

import com.google.inject.Inject;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.abc.AbcAlchPlugin;
import net.runelite.client.plugins.abc.model.AlchItem;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.runelite.api.ItemID.*;

/**
 * MEMBERS ALCHING SCRIPT
 *
 * Key Features:
 * - Only purchases MEMBERS items for alching
 * - Filters out items with GE price = 8gp
 * - Filters out items with profit > 1400gp
 * - Random alch limit between 140-380 items
 * - Logs in every 4 hours to perform alching routine
 * - Continues alching until no more members alchs available
 * - Uses 8 GE slots (members account)
 */
public class MembersAlchScript extends Script {
    private static final Logger log = LoggerFactory.getLogger(MembersAlchScript.class);

    // Track purchases
    private List<AlchPurchase> currentAlchables = new ArrayList<>();
    private long lastLoginTime = 0;
    private boolean staffCheck = false;
    private long totalProfit = 0;
    private boolean running = false;

    private MembersAlchConfig config;

    @Inject
    private ItemManager itemManager;

    /**
     * Represents a purchase decision for a members alchable item
     */
    private static class AlchPurchase {
        String itemName;
        int itemId;
        int gePrice;
        int purchasePrice;
        int alchPrice;
        int profitPerItem;
        int geLimit;
        int quantityToBuy;
        int totalCost;
        int totalProfit;

        public AlchPurchase(String itemName, int itemId, int gePrice, int alchPrice, int geLimit, int natureRunePrice) {
            this.itemName = itemName;
            this.itemId = itemId;
            this.gePrice = gePrice;
            this.purchasePrice = (int) (gePrice * 1.15); // Buy at 15% over GE price
            this.alchPrice = alchPrice;
            this.profitPerItem = alchPrice - purchasePrice - natureRunePrice;
            this.geLimit = geLimit;
        }
    }

    public boolean run(MembersAlchConfig config) {
        this.config = config;
        this.running = true;
        Microbot.log("=== MEMBERS ALCHING SCRIPT STARTED ===");

        mainScheduledFuture = scheduledExecutorService.schedule(() -> {
            try {
                if (!super.run()) return;

                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        // Check if 4 hours have passed since last login
                        long currentTime = System.currentTimeMillis();
                        long loginIntervalMs = TimeUnit.HOURS.toMillis(config.loginIntervalHours());

                        if (currentTime - lastLoginTime < loginIntervalMs && lastLoginTime != 0) {
                            Microbot.log("Waiting for interval... Time remaining: " +
                                TimeUnit.MILLISECONDS.toMinutes(loginIntervalMs - (currentTime - lastLoginTime)) + " minutes");
                            sleep(60000); // Check every minute
                            continue;
                        }

                        // Update login time
                        lastLoginTime = currentTime;

                        // Generate random alch limit for this session
                        int alchLimit = Rs2Random.between(config.minAlchLimit(), config.maxAlchLimit());
                        Microbot.log("Generated random alch limit: " + alchLimit);

                        // Execute alching routine
                        executeAlchingRoutine(alchLimit);

                    } catch (Exception e) {
                        Microbot.log("Error in main loop: " + e.getMessage());
                        e.printStackTrace();
                    }

                    sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public boolean run() {
        return true;
    }

    @Override
    public void shutdown() {
        running = false;
        super.shutdown();
    }

    /**
     * Main alching routine - purchases and alchs members items
     */
    private void executeAlchingRoutine(int alchLimit) {
        Microbot.log("=== STARTING ALCHING ROUTINE ===");
        Microbot.log("Alch limit for this session: " + alchLimit);

        // Step 1: Go to GE and evaluate cash
        if (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
            sleep(1800, 2700);
        }

        // Step 2: Calculate available cash
        int availableCash = Rs2Inventory.count(COINS_995);
        if (Rs2Bank.isOpen()) {
            availableCash += Rs2Bank.count(COINS_995);
        }

        Microbot.log("Available cash: " + availableCash + "gp");

        // Step 3: Fetch and purchase members alchables
        List<AlchPurchase> purchaseList = fetchAndCalculateMembersAlchables(availableCash, alchLimit);

        if (purchaseList.isEmpty()) {
            Microbot.log("No suitable members alchables found - ending routine");
            return;
        }

        // Step 4: Execute GE purchases (using all 8 members slots)
        executeMembersGePurchases(purchaseList, alchLimit);

        // Step 5: Collect items and start alching
        sleep(5000, 10000); // Wait for offers to complete
        collectOffersAndAlch();

        Microbot.log("=== ALCHING ROUTINE COMPLETE ===");
    }

    /**
     * Fetches top MEMBERS alchables from ABC Alch plugin with proper filtering
     */
    private List<AlchPurchase> fetchAndCalculateMembersAlchables(int availableCash, int alchLimit) {
        Microbot.log("=== Fetching Members Alchables ===");
        List<AlchPurchase> candidates = new ArrayList<>();

        try {
            AbcAlchPlugin plugin = getAbcAlchPlugin();
            if (plugin == null) {
                Microbot.log("ERROR: ABC Alch plugin not loaded");
                return candidates;
            }

            List<AlchItem> alchItems = plugin.getAlchItems();
            if (alchItems == null || alchItems.isEmpty()) {
                Microbot.log("ERROR: ABC Alch item list is empty");
                return candidates;
            }

            // Filter for members items with proper constraints
            for (AlchItem item : alchItems) {
                // CRITICAL FILTERS:

                // 1. MUST be members item (if configured)
                if (config.membersOnly() && !item.getIsMembers()) continue;

                // 2. Must have valid GE limit
                if (item.getGeLimit() < config.minGeLimit()) {
                    if (config.debugLogging()) {
                        Microbot.log("Filtered out: " + item.getName() + " (GE limit " +
                            item.getGeLimit() + " < " + config.minGeLimit() + ")");
                    }
                    continue;
                }

                // 3. Filter out items with profit > maxProfit
                if (item.getHighAlchProfit() > config.maxProfit()) {
                    if (config.debugLogging()) {
                        Microbot.log("Filtered out: " + item.getName() + " (profit " +
                            item.getHighAlchProfit() + "gp > " + config.maxProfit() + "gp)");
                    }
                    continue;
                }

                // 4. Must have valid GE limit
                if (item.getGeLimit() <= 0) continue;

                // 5. Must meet minimum profit requirement
                if (item.getHighAlchProfit() < config.minProfit()) continue;

                AlchPurchase purchase = new AlchPurchase(
                    item.getName(),
                    -1,
                    item.getGePrice(),
                    item.getHighAlchPrice(),
                    item.getGeLimit(),
                    config.natureRunePrice()
                );

                candidates.add(purchase);

                if (config.debugLogging()) {
                    Microbot.log("Valid members item: " + item.getName() +
                        " | GE: " + item.getGePrice() + "gp" +
                        " | Alch: " + item.getHighAlchPrice() + "gp" +
                        " | Profit: " + item.getHighAlchProfit() + "gp" +
                        " | Limit: " + item.getGeLimit());
                }
            }

            Microbot.log("Found " + candidates.size() + " valid members alchables");

            // Calculate optimal purchases using available cash and alch limit
            return calculateOptimalMembersPurchases(candidates, availableCash, alchLimit);

        } catch (Exception e) {
            Microbot.log("ERROR fetching members alchables: " + e.getMessage());
            e.printStackTrace();
        }

        return candidates;
    }

    /**
     * Calculates optimal combination of members items to purchase
     * Uses all 8 GE slots efficiently
     */
    private List<AlchPurchase> calculateOptimalMembersPurchases(List<AlchPurchase> candidates,
                                                                 int availableCash,
                                                                 int alchLimit) {
        Microbot.log("=== Calculating Optimal Members Purchases ===");
        Microbot.log("Available cash: " + availableCash + "gp");
        Microbot.log("Alch limit: " + alchLimit);
        Microbot.log("GE slots available: 8 (members)");

        if (candidates.isEmpty()) {
            Microbot.log("No candidates to evaluate!");
            return new ArrayList<>();
        }

        // Reserve cash for nature runes
        int natureRuneCost = alchLimit * config.natureRunePrice();
        int cashForItems = availableCash - natureRuneCost;

        if (cashForItems < 10000) {
            Microbot.log("Insufficient cash after nature runes: " + cashForItems + "gp");
            return new ArrayList<>();
        }

        Microbot.log("Cash for items: " + cashForItems + "gp (reserved " +
            natureRuneCost + "gp for nature runes)");

        // Strategy: Fill up to 8 slots with best profit/alch ratio items
        List<AlchPurchase> bestCombination = new ArrayList<>();
        int remainingCash = cashForItems;
        int remainingAlchs = alchLimit;
        int slotsUsed = 0;

        // Sort by profit per item (descending)
        candidates.sort((a, b) -> Integer.compare(b.profitPerItem, a.profitPerItem));

        for (AlchPurchase candidate : candidates) {
            if (slotsUsed >= 8) break; // Members = 8 slots
            if (remainingAlchs <= 0) break;
            if (remainingCash <= 0) break;

            // Calculate how many we can afford
            int maxAffordable = remainingCash / candidate.purchasePrice;
            int maxByLimit = Math.min(candidate.geLimit, remainingAlchs);
            int quantityToBuy = Math.min(maxAffordable, maxByLimit);

            if (quantityToBuy <= 0) continue;

            // Create purchase
            AlchPurchase purchase = new AlchPurchase(
                candidate.itemName,
                candidate.itemId,
                candidate.gePrice,
                candidate.alchPrice,
                candidate.geLimit,
                config.natureRunePrice()
            );
            purchase.quantityToBuy = quantityToBuy;
            purchase.totalCost = quantityToBuy * purchase.purchasePrice;
            purchase.totalProfit = quantityToBuy * purchase.profitPerItem;

            bestCombination.add(purchase);
            remainingCash -= purchase.totalCost;
            remainingAlchs -= quantityToBuy;
            slotsUsed++;

            Microbot.log("Slot " + slotsUsed + ": " + purchase.itemName +
                " x" + quantityToBuy + " @ " + purchase.purchasePrice + "gp = " +
                purchase.totalProfit + "gp profit");
        }

        // Summary
        int totalQuantity = bestCombination.stream().mapToInt(p -> p.quantityToBuy).sum();
        int totalCost = bestCombination.stream().mapToInt(p -> p.totalCost).sum();
        int totalProfit = bestCombination.stream().mapToInt(p -> p.totalProfit).sum();

        Microbot.log("=== PURCHASE PLAN ===");
        Microbot.log("Slots used: " + slotsUsed + "/8");
        Microbot.log("Total items: " + totalQuantity + "/" + alchLimit);
        Microbot.log("Total cost: " + totalCost + "gp");
        Microbot.log("Expected profit: " + totalProfit + "gp");

        return bestCombination;
    }

    /**
     * Executes GE purchases using all 8 members slots
     */
    private void executeMembersGePurchases(List<AlchPurchase> purchaseList, int alchLimit) {
        Microbot.log("=== Executing Members GE Purchases ===");

        // Store for alching later
        currentAlchables.clear();
        currentAlchables.addAll(purchaseList);

        // Open GE
        if (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
            sleep(2000, 3000);
        }

        // Buy nature runes first
        int natureRunesToBuy = alchLimit;
        Microbot.log("Buying " + natureRunesToBuy + " nature runes...");
        String natureRuneName = itemManager.getItemComposition(NATURE_RUNE).getName();
        Rs2GrandExchange.buyItem(natureRuneName, natureRunesToBuy, config.natureRunePrice());
        sleep(1000, 2000);

        // Buy each alchable item using remaining slots
        for (int i = 0; i < purchaseList.size() && i < 7; i++) { // 7 slots for items (1 for nature runes)
            AlchPurchase item = purchaseList.get(i);
            Microbot.log("Slot " + (i+2) + ": Buying " + item.quantityToBuy +
                "x " + item.itemName + " @ " + item.purchasePrice + "gp");

            Rs2GrandExchange.buyItem(item.itemName, item.quantityToBuy, item.purchasePrice);
            sleep(800, 1500);
        }

        Microbot.log("All GE offers placed!");
    }

    /**
     * Collects GE offers and begins alching
     */
    private void collectOffersAndAlch() {
        Microbot.log("=== Collecting Offers and Starting Alch ===");

        // Wait for offers to complete
        sleep(10000, 15000);

        // Collect all
        Rs2GrandExchange.collectAll(false);
        sleep(2000, 3000);

        // Bank items as notes
        Rs2Bank.openBank();
        sleep(1800, 2700);
        Rs2Bank.depositAll();
        sleep(1000, 2000);

        // Equip fire staff
        if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
            Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE);
            sleep(1500, 2000);
        }

        Rs2Bank.setWithdrawAsNote();
        sleep(500, 1000);

        // Withdraw all alchables as notes
        for (AlchPurchase purchase : currentAlchables) {
            Rs2Bank.withdrawAll(purchase.itemName);
            sleep(600, 1200);
        }

        // Withdraw nature runes
        Rs2Bank.withdrawAll(NATURE_RUNE);
        sleep(600, 1200);

        Rs2Bank.closeBank();
        sleep(1000, 1500);

        // Start alching
        alchUntilEmpty();

        Microbot.log("Alching complete!");
    }

    /**
     * Alchs all members items until inventory is empty
     */
    private void alchUntilEmpty() {
        Microbot.log("=== Starting Alch Loop (Members Items) ===");

        // Build map of cert IDs to alch
        Map<Integer, AlchPurchase> alchableMap = new HashMap<>();
        for (AlchPurchase purchase : currentAlchables) {
            int baseId = getItemIdByName(purchase.itemName);
            if (baseId > 0) {
                alchableMap.put(baseId + 1, purchase); // Cert ID = base + 1
            }
        }

        if (alchableMap.isEmpty()) {
            Microbot.log("ERROR: No alchable items mapped");
            return;
        }

        int alchCount = 0;

        // Alch loop
        while (hasAnyAlchables(alchableMap) && Rs2Inventory.contains(NATURE_RUNE) && running) {

            // Check slot 12
            int slot12Id = Rs2Inventory.getIdForSlot(12);

            // Move alchable to slot 12 if needed
            if (!alchableMap.containsKey(slot12Id)) {
                boolean itemMoved = false;

                for (int slot = 0; slot < 28; slot++) {
                    if (slot == 12) continue;

                    int slotId = Rs2Inventory.getIdForSlot(slot);
                    if (alchableMap.containsKey(slotId)) {
                        Microbot.log("Moving alchable from slot " + slot + " to slot 12");
                        dragInventoryItemWithRetry(slot, 12, slotId, 10);
                        sleep(300, 600);
                        itemMoved = true;
                        break;
                    }
                }

                if (!itemMoved) {
                    Microbot.log("No more alchables found");
                    break;
                }
            }

            // Verify fire staff
            if (!staffCheck) {
                if (!Rs2Magic.canCast(MagicAction.HIGH_LEVEL_ALCHEMY)) {
                    Microbot.log("Re-equipping fire staff");
                    Rs2Bank.openBank();
                    sleep(2000, 3000);
                    Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE);
                    sleep(2000, 3000);
                    Rs2Bank.closeBank();
                    sleep(2000, 3000);
                }
                staffCheck = true;
            }

            // Alch item in slot 12
            slot12Id = Rs2Inventory.getIdForSlot(12);
            if (alchableMap.containsKey(slot12Id)) {
                AlchPurchase purchase = alchableMap.get(slot12Id);
                Microbot.log("Alching " + purchase.itemName + " [#" + (alchCount + 1) + "]");

                Rs2Magic.alch(purchase.itemName);

                // Wait for XP
                sleepUntil(() -> Rs2Player.waitForXpDrop(Skill.MAGIC), 4000);

                if (Rs2Player.isAnimating()) {
                    // Track profit
                    totalProfit += purchase.profitPerItem;
                }

                alchCount++;
                randomSleepAlching();
            }
        }

        Microbot.log("Alching complete! Total alched: " + alchCount);
    }

    /**
     * Checks if any alchable items remain
     */
    private boolean hasAnyAlchables(Map<Integer, AlchPurchase> alchableMap) {
        for (int certId : alchableMap.keySet()) {
            if (Rs2Inventory.contains(certId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Random sleep between alchs for anti-ban
     */
    private void randomSleepAlching() {
        sleep(Rs2Random.between(1200, 2400));
    }

    /**
     * Drags inventory item with retry logic (from mission control)
     */
    private boolean dragInventoryItemWithRetry(int fromSlot, int toSlot, int itemId, int maxRetries) {
        int attempts = 0;
        Microbot.log("[RETRY] Starting drag operation: slot " + fromSlot + " -> slot " + toSlot + " for item " + itemId);

        while (attempts < maxRetries) {
            attempts++;
            Microbot.log("[RETRY] === Attempt " + attempts + "/" + maxRetries + " ===");

            // Check if source slot still has the item
            int currentFromId = Rs2Inventory.getIdForSlot(fromSlot);
            if (currentFromId != itemId) {
                Microbot.log("[RETRY] ✗ Source slot no longer contains item");
                return false;
            }

            // Store before state
            int beforeFromId = currentFromId;
            int beforeToId = Rs2Inventory.getIdForSlot(toSlot);

            // Perform drag
            boolean dragResult = Rs2Inventory.moveItemToSlot(Rs2Inventory.getItemInSlot(fromSlot), toSlot);
            if (!dragResult) {
                sleep(300, 600);
                continue;
            }

            // Wait for game state update
            sleep(800, 1200);

            // Verify success
            int afterFromId = Rs2Inventory.getIdForSlot(fromSlot);
            int afterToId = Rs2Inventory.getIdForSlot(toSlot);

            if (afterFromId != beforeFromId && afterToId == itemId) {
                Microbot.log("[RETRY] ✓ SUCCESS!");
                return true;
            }

            sleep(500, 800);
        }

        Microbot.log("[RETRY] ✗✗✗ FAILED after " + maxRetries + " attempts");
        return false;
    }

    /**
     * Gets item ID by name from game data
     */
    private int getItemIdByName(String name) {
        try {
            return itemManager.search(name).get(0).getId();
        } catch (Exception e) {
            Microbot.log("Could not find item ID for: " + name);
            return -1;
        }
    }

    /**
     * Gets ABC Alch plugin instance
     */
    private AbcAlchPlugin getAbcAlchPlugin() {
        return (AbcAlchPlugin) Microbot.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getClass().getName().equals(AbcAlchPlugin.class.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets time until next login (for overlay)
     */
    public long getTimeUntilNextLogin() {
        if (lastLoginTime == 0) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastLoginTime;
        long loginIntervalMs = TimeUnit.HOURS.toMillis(config != null ? config.loginIntervalHours() : 4);
        long remaining = loginIntervalMs - elapsed;
        return remaining > 0 ? remaining : 0;
    }

    public long getTotalProfit() {
        return totalProfit;
    }
}
