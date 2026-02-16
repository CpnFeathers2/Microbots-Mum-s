package net.runelite.client.plugins.microbot.MossKiller;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.MossKiller.Enums.MossKillerState;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.accountselector.AutoLoginPlugin;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerPlugin;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeAction;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeRequest;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2CombatSpells;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.api.ItemID.*;
import static net.runelite.api.Skill.*;
import static net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity.HIGH;
import static net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity.LOW;
import static net.runelite.client.plugins.microbot.util.bank.Rs2Bank.*;
import static net.runelite.client.plugins.microbot.util.bank.enums.BankLocation.GRAND_EXCHANGE;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.eatAt;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.PROTECT_MAGIC;
import static net.runelite.client.plugins.microbot.util.walker.Rs2Walker.walkTo;
import static net.runelite.client.plugins.skillcalculator.skills.MagicAction.HIGH_LEVEL_ALCHEMY;


public class MossKillerScript extends Script {

    public static double version = 1.0;
    public static MossKillerConfig config;

    public boolean isStarted = false;
    public int playerCounter = 0;
    /// // making bossmode always true
    public boolean bossMode = true;
    private int lastBoneCount = 0;
    private boolean resetUnlocked = false;



    @Inject
    static Client client;

    private final MossKillerPlugin plugin;

    @Inject
    public MossKillerScript (MossKillerPlugin plugin) {
        this.plugin = plugin;
    }

    public final WorldPoint SEWER_ENTRANCE = new WorldPoint(3237, 3459, 0);
    public final WorldPoint SEWER_LADDER = new WorldPoint(3237, 9859, 0);
    public final WorldPoint NORTH_OF_WEB = new WorldPoint(3210, 9900, 0);
    public final WorldPoint SOUTH_OF_WEB = new WorldPoint(3210, 9898, 0);
    public final WorldPoint OUTSIDE_BOSS_GATE_SPOT = new WorldPoint(3174, 9900, 0);
    public final WorldPoint INSIDE_BOSS_GATE_SPOT = new WorldPoint(3214, 9937, 0);
    public final WorldPoint MOSS_GIANT_SPOT = new WorldPoint(3165, 9879, 0);
    public final WorldPoint VARROCK_SQUARE = new WorldPoint(3212, 3422, 0);
    public final WorldPoint VARROCK_WEST_BANK = new WorldPoint(3253, 3420, 0);

    public int[] strengthPotionIds = {STRENGTH_POTION1, STRENGTH_POTION2, STRENGTH_POTION3, STRENGTH_POTION4}; // Replace ID1, ID2, etc., with the actual potion IDs.


    // Items
    public final int AIR_RUNE = 556;
    public final int FIRE_RUNE = 554;
    public static final int LAW_RUNE = 563;

    // TODO: convert axe and food to be a list of all available stuff
    public int BRONZE_AXE = 1351;
    public int FOOD = SWORDFISH;

    public static int MOSSY_KEY = 22374;

    public static int NATURE_RUNE = 561;
    public static int DEATH_RUNE = 560;
    public static int CHAOS_RUNE = 562;
    // TODO: add stuff for boss too
    public int[] LOOT_LIST2 = new int[]{COSMIC_RUNE,RUNE_PLATELEGS, RUNE_LONGSWORD, RUNE_MED_HELM, RUNE_SWORD, ADAMANT_KITESHIELD, RUNE_CHAINBODY, RUNITE_BAR, RUNE_PLATESKIRT, RUNE_SQ_SHIELD, RUNE_SWORD, RUNE_MED_HELM, ADAMANT_PLATEBODY};
    public int[] LOOT_LIST = new int[]{MOSSY_KEY, LAW_RUNE, AIR_RUNE, FIRE_RUNE, COSMIC_RUNE, DEATH_RUNE, CHAOS_RUNE, NATURE_RUNE};
    public static final int[] LOOT_LIST1 = new int[]{2354, BIG_BONES, RUNE_PLATELEGS, RUNE_LONGSWORD, RUNE_MED_HELM, RUNE_SWORD, ADAMANT_KITESHIELD, RUNE_CHAINBODY, RUNITE_BAR, RUNE_PLATESKIRT, RUNE_SQ_SHIELD, RUNE_SWORD, RUNE_MED_HELM, 1124, ADAMANT_KITESHIELD, NATURE_RUNE, COSMIC_RUNE, LAW_RUNE, DEATH_RUNE, CHAOS_RUNE, ADAMANT_ARROW, RUNITE_BAR, 1620, ADAMANT_KITESHIELD, 1618, 2354, 995, 114, BRYOPHYTAS_ESSENCE, MOSSY_KEY};
    public int[] ALCHABLES = new int[]{STEEL_KITESHIELD, MITHRIL_SWORD, BLACK_SQ_SHIELD};
    public String[] bryophytaDrops = {
            "Rune platelegs",
            "Rune longsword",
            "Rune med helm",
            "Rune chainbody",
            "Rune plateskirt",
            "Rune sq shield",
            "Rune sword",
            "Adamant platebody",
            "Adamant kiteshield",
            "Nature rune",
            "Cosmic rune",
            "Law rune",
            "Death rune",
            "Chaos rune",
            "Adamant arrow",
            "Runite bar",
            "Uncut ruby",
            "Uncut diamond",
            "Coins",
            "Strength potion(4)",
            "Bryophyta's essence",
            "Mossy key",
            "Steel bar",
            "Big bones"
    };
    public MossKillerState state = MossKillerState.BANK;


    private boolean attemptingEscape = false;
    private int escapeRetryCount = 0;
    private static final int MAX_ESCAPE_ATTEMPTS = 5;

    private int bossModeFalseCounter = 0;

    private boolean growthlingPhaseComplete = false;

    private boolean round1 = false;
    private boolean round2 = false;
    private long timeEnteredInstance = 0;


    public boolean run(MossKillerConfig config) {
        MossKillerScript.config = config;
        Microbot.enableAutoRunOn = false;
        Rs2Walker.disableTeleports = true;
        Rs2Antiban.resetAntibanSettings();
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.dynamicActivity = true;
        Rs2AntibanSettings.profileSwitching = true;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.moveMouseOffScreenChance = 0.07;
        Rs2AntibanSettings.moveMouseRandomly = true;
        Rs2AntibanSettings.moveMouseRandomlyChance = 0.04;
        Rs2Antiban.setActivityIntensity(LOW);

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                //if (Microbot.bryoFinished) return;
                //if (!Microbot.bryoFinished) {
                   // if (Microbot.isPluginEnabled(AutoLooterPlugin.class)) {
                    //stopLootingPlugin();}
                //}
                long startTime = System.currentTimeMillis();

                Microbot.log("bossmode false counter " + bossModeFalseCounter);
                if(!isStarted){
                    init();
                }

                if (plugin.preparingForShutdown) {prepareSoftStop();}

                Microbot.log(String.valueOf(state));
                Microbot.log("BossMode: " + bossMode);
                if (!bossMode & config.bossOnly()) {
                    bossModeFalseCounter++;
                    if (bossModeFalseCounter > 50) {
                        Microbot.log("bossMode was false > 50 times, initiating moarShutdown() after walking to bank");
                        Rs2Bank.walkToBank1();
                        BreakHandlerScript.breakIn = 15;
                        sleep(14000);
                        Microbot.log("break has triggered, so next account should activate");
                        moarShutDownLite();
                        return;
                    }
                } else {
                    bossModeFalseCounter = 0; // reset counter if bossMode is true
                }

                if (bossMode && Rs2AntibanSettings.actionCooldownChance > 0.05) {
                    Rs2AntibanSettings.actionCooldownChance = 0.00;
                } else if (!bossMode && Rs2AntibanSettings.actionCooldownChance < 0.06){
                    Rs2AntibanSettings.actionCooldownChance = 0.06;}

                if (Rs2Player.getRealSkillLevel(Skill.DEFENCE) >= config.defenseLevel()) {
                    moarShutDown();
                }

                if (Rs2Player.getRealSkillLevel(ATTACK) >= config.attackLevel()) {
                    moarShutDown();
                }

                if (Rs2Player.getRealSkillLevel(Skill.STRENGTH) >= config.strengthLevel()) {
                    moarShutDown();
                }

                switch(state){
                    case BANK: handleBanking(); break;
                    case TELEPORT: varrockTeleport(); break;
                    case WALK_TO_BANK: walkToVarrockWestBank(); break;
                    case WALK_TO_MOSS_GIANTS: walkToMossGiants(); break;
                    case FIGHT_BOSS: handleBossFight(); break;
                    case FIGHT_MOSS_GIANTS: handleMossGiants(); break;
                    case EXIT_SCRIPT: sleep(10000, 15000); init(); moarShutDown(); break;
                }

                if (BreakHandlerScript.breakIn < 15 * 60 && BreakHandlerScript.breakIn > 100) {
                    Microbot.log("less than 15 minutes and more than 100 seconds (to not interrupt reset) until break, stopping bryo");
                    handleVarrockTeleportIsolated();
                    round1 = false;
                    round2 = false;
                    resetUnlocked = false;
                    lastBoneCount = 0;
                    Microbot.bryoFinished = true; if(BreakHandlerScript.breakIn < 30 * 60) {BreakHandlerScript.breakIn = 30 * 60; Microbot.log("Made break 30 minutes");}
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
       bossModeFalseCounter = 0;
       Microbot.bryoFinished = false;
       super.shutdown();
    }

    public static void prepareSchedulerStart() {
        if (needsRegear(config)) {
            Microbot.log("Outfit mismatch detected — initiating regear.");

            walkToBank(BankLocation.VARROCK_EAST);
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen);
            Rs2Bank.depositEquipment();
            Rs2Bank.depositAll();

            OutfitHelper.equipOutfit(config.selectedOutfit(), config);
            equipCustomWeapon(config);
            Rs2Bank.closeBank();

            if (!config.includeHelmet()) {
                Rs2Equipment.unEquip(EquipmentInventorySlot.HEAD);
                Rs2Bank.openBank();
                sleepUntil(Rs2Bank::isOpen);
                Rs2Bank.depositAll();
            }
        }
    }

    public static void equipCustomWeapon(MossKillerConfig config) {
        String customWeapon = config.customWeapon().trim();
        String defaultWeapon = "Rune scimitar";

        // Use custom weapon only if it's different from default
        if (!customWeapon.equalsIgnoreCase(defaultWeapon)) {
            Microbot.log("Trying to equip custom weapon: " + customWeapon);
            Rs2Bank.withdrawAndEquip(customWeapon);

            if (sleepUntil(() -> Rs2Equipment.isWearing(customWeapon), 5000)) {
                Microbot.log("Successfully equipped custom weapon: " + customWeapon);
                return;
            } else {
                Microbot.log("Custom weapon not found or failed to equip. Falling back to default.");
            }
        }

        // Equip default weapon
        Microbot.log("Equipping default weapon: " + defaultWeapon);
        Rs2Bank.withdrawAndEquip(defaultWeapon);

        if (!sleepUntil(() -> Rs2Equipment.isWearing(defaultWeapon), 5000)) {
            Microbot.log("Failed to equip default weapon as well: " + defaultWeapon);
        } else {
            Microbot.log("Default weapon equipped: " + defaultWeapon);
        }
    }


    public static boolean needsRegear(MossKillerConfig config) {
        OutfitHelper.OutfitType selectedOutfit = config.selectedOutfit(); // e.g., FULL_RUNE
        List<String> requiredItems = new ArrayList<>(Arrays.asList(selectedOutfit.getOutfitItems()));

        // Handle user weapon preference
        String weapon = config.customWeapon(); // e.g., "Rune sword"
        if (weapon != null && !weapon.isEmpty()) {
            requiredItems.removeIf(item -> item.toLowerCase().contains("scimitar")); // crude fallback
            requiredItems.add(weapon);
        }

        // Add cape
        requiredItems.add(config.cape().name()); // Uses GearEnums.Cape.getItemName()

        // Handle optional helmet
        if (!config.includeHelmet()) {
            requiredItems.removeIf(item -> item.toLowerCase().contains("helm"));
        }

        // Compare current worn gear
        for (String item : requiredItems) {
            if (!Rs2Equipment.isWearing(item)) {
                Microbot.log("Missing: " + item);
                return true;
            }
        }

        return false;
    }


    public static void prepareSoftStop() {
        if (!config.wildy() || !config.wildySafer()) {
            if (Rs2Magic.canCast(MagicAction.VARROCK_TELEPORT)) {
                Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);
                sleep(1000, 3000);
            }
            walkToBank(BankLocation.VARROCK_EAST);
        } else if (Rs2Player.getWorldLocation().getY() > 3520) {
            walkToBank(BankLocation.FEROX_ENCLAVE);
        }

        sleep (60000); //sleep until soft stop comes into effect
    }


    public void moarShutDownLite() {
        sleep(1000);
        Microbot.log("calling script shutdown");
        shutdown();
    }

    public void moarShutDown() {
        Microbot.log("super shutdown triggered");
        if(Rs2Inventory.containsAll(AIR_RUNE, FIRE_RUNE, LAW_RUNE)){
            Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);}
        sleepUntil(() -> !Rs2Player.isInCombat(), 10000);
        stopBreakHandlerPlugin();
        stopAutologin();
        sleep(1000);
        Microbot.log("calling script shutdown");
        shutdown();
    }

    private void checkAndDrinkStrengthPotion() {
        int currentStrengthLevel = Microbot.getClient().getRealSkillLevel(Skill.STRENGTH); // Unboosted strength level
        int boostedStrengthLevel = Microbot.getClient().getBoostedSkillLevel(Skill.STRENGTH); // Current boosted strength level

        // Calculate the strength potion boost
        int strengthPotionBoost = (int) Math.floor(3 + (0.1 * currentStrengthLevel)); // Potion boost value

        // Calculate the expected boosted strength level
        int expectedBoostedStrength = currentStrengthLevel + strengthPotionBoost;

        // Check if the boosted strength level is less than 2 levels below the expected boosted level
        if (boostedStrengthLevel < expectedBoostedStrength - 2) {
            System.out.println("are we getting into the drinking bracket?");
            // Try to drink any available Strength potion (1 to 4 doses)
            if (Rs2Inventory.contains("Strength potion(1)")) {
                Rs2Inventory.interact("Strength potion(1)", "Drink");
            } else if (Rs2Inventory.contains("Strength potion(2)")) {
                Rs2Inventory.interact("Strength potion(2)", "Drink");
            } else if (Rs2Inventory.contains("Strength potion(3)")) {
                Rs2Inventory.interact("Strength potion(3)", "Drink");
            } else if (Rs2Inventory.contains("Strength potion(4)") && !Rs2Inventory.hasItemAmount("Strength potion(4)", 15, true)) {
                Rs2Inventory.interact("Strength potion(4)", "Drink");
            }

            sleep(300);
        } else {
            System.out.println("Boosted strength level is high enough, no need to drink.");
        }
    }

    /**
     * Stops the BreakHandlerPlugin if it's currently active.
     *
     * @return true if the plugin was successfully stopped, false if it was not found or not active.
     */
    public static boolean stopBreakHandlerPlugin() {
        // Attempt to retrieve the BreakHandlerPlugin from the active plugin list
        BreakHandlerPlugin breakHandlerPlugin = (BreakHandlerPlugin) Microbot.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getClass().getName().equals(BreakHandlerPlugin.class.getName()))
                .findFirst()
                .orElse(null);

        // Check if the plugin was found
        if (breakHandlerPlugin == null) {
            System.out.println("BreakHandlerPlugin not found or not running.");
            return false;
        }

        Microbot.getClientThread().invokeLater(() -> {
            try {
                Microbot.getPluginManager().setPluginEnabled(breakHandlerPlugin, false);
                Microbot.stopPlugin(breakHandlerPlugin);
            } catch (Exception e) {
                Microbot.log("Error stopping plugin", e);
            }
        });
        return true;
    }

    /**
     * Stops the Autologin Plugin if it's currently active.
     *
     * @return true if the plugin was successfully stopped, false if it was not found or not active.
     */


    public static boolean stopAutologin() {
        // Attempt to retrieve the autologin from the active plugin list
        AutoLoginPlugin autoLoginPlugin = (AutoLoginPlugin) Microbot.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getClass().getName().equals(AutoLoginPlugin.class.getName()))
                .findFirst()
                .orElse(null);

        // Check if the plugin was found
        if (autoLoginPlugin == null) {
            System.out.println("autologin not found or not running.");
            return false;
        }

        Microbot.getClientThread().invokeLater(() -> {
            try {
                Microbot.getPluginManager().setPluginEnabled(autoLoginPlugin, false);
                Microbot.stopPlugin(autoLoginPlugin);
            } catch (Exception e) {
                Microbot.log("Error stopping plugin", e);
            }
        });
        return true;
    }


    public void handleMossGiants() {
        if (bossMode) return;
        if (bossMode) state = MossKillerState.FIGHT_BOSS;

        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if (!Rs2Inventory.contains(FOOD) || BreakHandlerScript.breakIn <= 30){
            Microbot.log("Inventory does not contains FOOD or break in less than 30");
            if (Rs2Inventory.contains(FOOD)) {Microbot.log("We have food");}
                if (BreakHandlerScript.breakIn <= 30) {Microbot.log("Break in less than 15");}
            state = MossKillerState.TELEPORT;
            return;
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) > 10){
            init();
            return;
        }

        int randomValue = (int) Rs2Random.truncatedGauss(35, 60, 4.0);
        if (Rs2Player.getCombatLevel() < 69) {
            int randomValue1 = (int) Rs2Random.truncatedGauss(50, 75, 4.0);
            eatAt(randomValue1);
        } else eatAt(randomValue);


        // Check if loot is nearby and pick it up if it's in LOOT_LIST
        for (int lootItem : LOOT_LIST) {
            if (Rs2GroundItem.exists(lootItem, 7)
                    && Rs2Inventory.getEmptySlots() == 0) {
                eatAt(100);}
            if(!Rs2Inventory.isFull() && Rs2GroundItem.interact(lootItem, "Take", 10)){
                sleep(1000, 3000);
            }
        }

        // Check if loot is nearby and pick it up if it's in LOOT_LIST
        if (config.alchLoot()) {
            for (int lootItem : ALCHABLES) {
                if (Rs2GroundItem.exists(lootItem, 7)
                        && Rs2Inventory.getEmptySlots() == 0) {
                    eatAt(100);}
                if (Rs2GroundItem.exists(lootItem, 7) && Rs2Inventory.getEmptySlots() == 0) {
                    eatAt(100);
                    sleepUntil(() -> !Rs2Inventory.isFull());
                    Rs2GroundItem.interact(lootItem, "Take", 7);
                    sleep(2000, 3500);

                } else if (Rs2GroundItem.exists(lootItem, 7)
                        && Rs2Inventory.getEmptySlots() > 0) {
                    Rs2GroundItem.interact(lootItem, "Take", 7);
                    sleep(2000, 3500);
                }
            }

            if (Rs2GroundItem.loot("Coins", 119, 7)) {
                sleep(2000, 3500);
            }

            if (Rs2Inventory.contains(NATURE_RUNE) &&
                    !Rs2Inventory.hasItemAmount(FIRE_RUNE, 5) &&
                    Rs2Inventory.contains(ALCHABLES)) {

                if (Microbot.getClient().getRealSkillLevel(MAGIC) > 54 && Rs2Magic.canCast(HIGH_LEVEL_ALCHEMY)) {

                    if (Rs2Inventory.contains(STEEL_KITESHIELD)) {
                        Rs2Magic.alch("Steel kiteshield");
                    } else if (Rs2Inventory.contains(BLACK_SQ_SHIELD)) {
                        Rs2Magic.alch("Black sq shield");
                    } else if (Rs2Inventory.contains(MITHRIL_SWORD)) {
                        Rs2Magic.alch("Mithril sword");
                    }

                    Rs2Player.waitForXpDrop(Skill.MAGIC, 10000, false);
                }
            }
        }


        if (config.buryBones()) {
            if (Rs2Inventory.contains(BIG_BONES)) {
                sleep(100, 1750);
                Rs2Inventory.interact(BIG_BONES, "Bury");
                Rs2Player.waitForAnimation();
            }
            if (!Rs2Inventory.isFull() && Rs2GroundItem.interact(BIG_BONES, "Take", 2)) {
                sleepUntil(() -> Rs2Inventory.contains(BIG_BONES));
                if (Rs2Inventory.contains(BIG_BONES)) {
                    sleep(100, 1750);
                    Rs2Inventory.interact(BIG_BONES, "Bury");
                    Rs2Player.waitForAnimation();
                }
            }
        }

        // Check if any players are near
        if(!getNearbyPlayers(7).isEmpty() && config.hopWhenPlayerIsNear()){
            // todo: add check in config if member or not
            if(playerCounter > 15) {
                sleep(10000, 15000);
                int world = Login.getRandomWorld(false, null);
                if(world == 301){
                    return;
                }
                boolean isHopped = Microbot.hopToWorld(world);
                sleepUntil(() -> isHopped, 5000);
                if (!isHopped) return;
                playerCounter = 0;
                int randomThreshold = (int) Rs2Random.truncatedGauss(0, 5, 1.5); // Adjust mean and deviation as needed
                if (randomThreshold > 3) {
                    Rs2Inventory.open();
                }
                return;
            }
            playerCounter++;
        } else {
            playerCounter = 0;
        }

        if (!Rs2Combat.inCombat()) {
            Rs2Npc.attack("Moss giant");
        }

        sleep(800, 2000);
    }

    public List<Rs2PlayerModel> getNearbyPlayers(int distance) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Use the predicate-based getPlayers method directly
        return Rs2Player.getPlayers(p -> p != null &&
                        p.getWorldLocation().distanceTo(playerLocation) <= distance)
                .collect(Collectors.toList());
    }

    // Class-level variable (add this at the top of your class)
    private Set<Integer> attackedGrowthlingIds = new HashSet<>();

    // Class-level variables
    private int growthlingKillCount = 0;
    private long lastXpDropTime = 0;

    public void handleBossFight() {
        toggleRunEnergy();
        Rs2Antiban.setActivityIntensity(HIGH);

        // Check if we're at the gate but not in instance yet
        boolean inInstance = Microbot.getClient().getTopLevelWorldView().getScene().isInstance();

        if (!inInstance && Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), OUTSIDE_BOSS_GATE_SPOT) < 5) {
            Microbot.log("🚪 At gate, attempting to enter instance");
            handleGateEntry1();
            return;
        }

        if (!inInstance) {
            Microbot.log("⚠️ Not in instance and not at gate, returning to gate");
            state = MossKillerState.WALK_TO_MOSS_GIANTS;
            return;
        }

        if (!Rs2Inventory.contains(FOOD)) {
            state = MossKillerState.TELEPORT;
            return;
        }

        if (!Rs2Inventory.contains(BRONZE_AXE) && !Rs2Equipment.isWearing(BRONZE_AXE)) {
            if (!Rs2Inventory.contains(MITHRIL_AXE) && !Rs2Equipment.isWearing(MITHRIL_AXE)) {
            getBronzeAxeFromInstance();}
            return;
        }

        // Eating check
        int randomValue = (int) Rs2Random.truncatedGauss(68, 77, 2.5);
        if (eatAt(randomValue)) {
            sleep(450, 650);
        }

        if (!config.magicBoss()) {
            checkAndDrinkStrengthPotion();
        }

        // Get all Growthlings
        List<Rs2NpcModel> growthlings = Rs2Npc.getNpcs()
                .filter(npc -> "Growthling".equals(npc.getName()) || npc.getId() == 8194)
                .collect(Collectors.toList());

        // Priority 1: Handle Growthlings ONLY if phase not complete
        if (!growthlings.isEmpty() && !growthlingPhaseComplete) {
            // Ensure bronze axe equipped
            if (!Rs2Equipment.isWearing(BRONZE_AXE)) {
                if (!Rs2Inventory.wield(BRONZE_AXE)) {
                    Microbot.log("❌ Failed to wield bronze axe");
                    return;
                }

                if (!sleepUntil(() -> Rs2Equipment.isWearing(BRONZE_AXE), 400)) {
                    Microbot.log("⚠️ Bronze axe not equipped after 400ms, aborting");
                    return;
                }
            }
            handleGrowthlings(growthlings);
            return;
        }

        // Priority 2: Reset flag when all Growthlings despawn
        if (growthlings.isEmpty() && growthlingPhaseComplete) {
            Microbot.log("✅ Growthling phase reset");
            growthlingPhaseComplete = false;
        }

        // Priority 3: Switch to staff if needed (removed old growthlingKillCount check)
        if (config.magicBoss() && !Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
            Microbot.log("🔥 Equipping staff for Bryophyta");
            Rs2Inventory.wield(STAFF_OF_FIRE);
            sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE), 600);
        }

        // 1. Define state
        boolean inCombat = Rs2Player.isInCombat();
        long currentTime = System.currentTimeMillis();
        long timeSinceEntry = currentTime - timeEnteredInstance;

        Rs2NpcModel bryophyta = Rs2Npc.getNpc("Bryophyta");

        if (bryophyta != null) {
            handleBryophytaAttack();
        } else {
            Microbot.log("❓ Boss Null. Instance: " + inInstance + " | Combat: " + inCombat + " | TimeSince: " + timeSinceEntry + "ms");


            // ────────────────
            // GUARD 1: Entry Lag
            // ────────────────
            if (inInstance && timeSinceEntry < 6000) {
                Microbot.log("🛡️ Just entered (" + timeSinceEntry + "ms). Ignoring null boss.");
                return;
            }


            // ────────────────
            // INSTANT DEATH DETECTION
            // If looting succeeds → this IS the boss death event
            // ────────────────
            if (Rs2Prayer.isPrayerActive(PROTECT_MAGIC)) {
                Rs2Prayer.toggle(PROTECT_MAGIC, false);
            }


            if (tryLootBones()) {
                Microbot.log("💀 Loot successful → boss dead.");
                handleBossDeath1();
                return;
            }


            // ────────────────
            // GUARD 2: Combat flicker
            // Null boss + in combat is normal for 2–6 ticks
            // We do NOT reset during this period
            // ────────────────
            if (inCombat) {
                Microbot.log("⚔️ In combat flicker. Bones not lootable yet → ignoring null boss this tick.");
                return;
            }


            // ────────────────
            // OUT OF COMBAT now
            // Try looting again — boss should be dead by now
            // ────────────────
            Microbot.log("⚠️ Out of combat. Retesting loot once more...");

            if (tryLootBones()) {
                Microbot.log("💀 Loot succeeded after combat ended → boss dead.");
                handleBossDeath1();
                return;
            }


            // ────────────────
            // FINAL FAILSAFE
            // Boss gone + not in combat + no loot = instance is done
            // ────────────────
            if (!Rs2Player.isInCombat() && Rs2Npc.getNpc("Bryophyta") == null) {
                Microbot.log("❌ No loot, no boss, no combat → Reset instance.");
                handleBossDeath1();
                return;
            }

            // Otherwise unsure, try next tick
            Microbot.log("🕐 Transitional tick. Re-check next loop.");}}

    /**
     * Attempts to loot BIG BONES.
     * Returns true ONLY if the inventory bone count increases.
     */
    private boolean tryLootBones() {
        Microbot.log("🔍 tryLootBones() called");

        int before = Rs2Inventory.count(BIG_BONES);
        Microbot.log("🦴 Bones before: " + before);

        // Ensure we have at least 1 free slot
        if (Rs2Inventory.emptySlotCount() < 1 && Rs2Inventory.contains(FOOD)) {
            Microbot.log("🍖 Eating food to make inventory space...");
            if (Rs2Inventory.interact(FOOD, "Eat")) {
                sleepUntil(() -> Rs2Inventory.emptySlotCount() >= 1 || !Rs2Inventory.contains(FOOD), 2000);
            } else {
                Microbot.log("❌ Failed to eat food");
            }
        }

        // Use the SAME looting method that works in lootBoss1()
        LootingParameters bonesParams = new LootingParameters(
                20,      // range
                1,       // minItems
                1,       // minQuantity
                0,       // minInvSlots
                false,   // delayedLooting
                false,   // antiLureProtection
                "Big bones"
        );

        boolean looted = Rs2GroundItem.lootItemsBasedOnNames(bonesParams);
        Microbot.log("🖱️ lootItemsBasedOnNames returned: " + looted);

        if (looted) {
            sleepUntil(() -> Rs2Inventory.count(BIG_BONES) > before, 1200);
        } else {
            sleep(300, 600);
        }

        int after = Rs2Inventory.count(BIG_BONES);
        Microbot.log("🦴 Bones after: " + after);

        boolean increased = after > before;

        if (increased) {
            Microbot.log("✅ Bones increased!");
            lastBoneCount = after;
        }

        return increased;
    }

    private long lastGrowthlingClickTime = 0;
    private boolean waitingForFirstGrowthlingXp = false;


    // Class-level variables
    private Set<Integer> attackedGrowthlingIndices = new HashSet<>(); // Changed from IDs to indices
    // Add this as a class field

    private void handleGrowthlings(List<Rs2NpcModel> growthlings) {
        Microbot.log("🌱 Starting Growthling phase - " + growthlings.size() + " detected");

        // CAPTURE THE 3 TARGET INDICES AT THE START
        List<Integer> targetIndices = growthlings.stream()
                .map(Rs2NpcModel::getIndex)
                .collect(Collectors.toList());

        if (targetIndices.size() != 3) {
            Microbot.log("⚠️ Expected 3 Growthlings but found " + targetIndices.size());
        }

        Microbot.log("🎯 Target indices: " + targetIndices);

        int killCount = 0;
        Set<Integer> killedIndices = new HashSet<>();
        int totalAttempts = 0;
        int maxAttempts = 15;
        boolean isFirstAttack = true;

        // Attack each of the 3 specific indices we captured
        for (int targetIndex : targetIndices) {
            if (killCount >= 3) break;

            int attempts = 0;
            while (attempts < 5 && !killedIndices.contains(targetIndex)) {
                attempts++;
                totalAttempts++;

                if (totalAttempts > maxAttempts) break;

                // Find THIS specific Growthling by index
                Rs2NpcModel target = Rs2Npc.getNpcs()
                        .filter(npc -> npc.getName() != null && npc.getName().equals("Growthling"))
                        .filter(npc -> npc.getIndex() == targetIndex)
                        .findFirst()
                        .orElse(null);

                if (target == null) {
                    Microbot.log("⚠️ Index " + targetIndex + " not found, assuming dead");
                    killedIndices.add(targetIndex);
                    break;
                }

                // Capture XP
                int startAttackXp = Microbot.getClient().getSkillExperience(Skill.ATTACK);
                int startStrengthXp = Microbot.getClient().getSkillExperience(Skill.STRENGTH);
                int startDefenceXp = Microbot.getClient().getSkillExperience(Skill.DEFENCE);
                int startHpXp = Microbot.getClient().getSkillExperience(Skill.HITPOINTS);

                Microbot.log("🎯 [" + (killCount + 1) + "/3] Index " + targetIndex + " (attempt " + attempts + ")");

                if (!Rs2Npc.interact(target, "Attack")) {  // ← CHANGED: Pass the object, not the ID
                    sleep(30);
                    continue;
                }

                // Wait for combat
                int combatTimeout = isFirstAttack ? 1200 : 400;
                if (!sleepUntil(() -> Microbot.getClient().getLocalPlayer().isInteracting(), combatTimeout)) {
                    sleep(30);
                    continue;
                }

                // Wait for XP
                int xpTimeout = isFirstAttack ? 3500 : 800;
                boolean gotXp = sleepUntilTrue(() -> {
                    return Microbot.getClient().getSkillExperience(Skill.ATTACK) > startAttackXp ||
                            Microbot.getClient().getSkillExperience(Skill.STRENGTH) > startStrengthXp ||
                            Microbot.getClient().getSkillExperience(Skill.DEFENCE) > startDefenceXp ||
                            Microbot.getClient().getSkillExperience(Skill.HITPOINTS) > startHpXp;
                }, 50, xpTimeout);

                if (gotXp) {
                    killCount++;
                    killedIndices.add(targetIndex);
                    isFirstAttack = false;
                    Microbot.log("✅ " + killCount + "/3");

                    if (killCount >= 3) {
                        Microbot.log("✨ All 3 defeated!");
                        growthlingPhaseComplete = true;
                    }

                    sleep(50, 80); // Minimal delay before next target
                    break; // Move to next index in the list
                } else {
                    Microbot.log("⚠️ No XP, retrying same index");
                    sleep(100);
                }
            }
        }

        growthlingKillCount = 0;
        attackedGrowthlingIndices.clear();
    }

    private void handleBryophytaAttack() {
        Rs2NpcModel currentTarget = (Rs2NpcModel) Rs2Player.getInteracting();
        boolean isAttackingBryophyta = currentTarget != null && "Bryophyta".equals(currentTarget.getName());

        // Stay out of melee range if using magic
        if (config.magicBoss()) {
            stayOutOfMeleeRangeFromBryophytaInstancedSafe();

            // Equip staff if not already equipped
            if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                Microbot.log("🔥 Equipping staff for Bryophyta");
                Rs2Inventory.wield(STAFF_OF_FIRE);
                sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE), 1000);
                return; // Wait for equip before attacking
            }

            // Enable prayer protection
            if (!Rs2Prayer.isOutOfPrayer() &&
                    !Rs2Prayer.isPrayerActive(PROTECT_MAGIC) &&
                    Rs2Player.getRealSkillLevel(PRAYER) > 36) {
                Rs2Prayer.toggle(PROTECT_MAGIC, true);
            }
        }

        // Attack Bryophyta if not already attacking
        if (!isAttackingBryophyta) {
            Microbot.log("⚔️ Attacking Bryophyta");
            Rs2Npc.attack("Bryophyta");
            sleep(250, 500);
        }
    }

    private void handleVarrockTeleportIsolated() {
        final int MAX_RETRIES = 5;
        final int TELEPORT_INTERVAL_MS = 2600;

        for (int i = 0; i < MAX_RETRIES; i++) {
            // Equip staff if needed
            if (Rs2Inventory.contains(STAFF_OF_FIRE) && !Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                Rs2Inventory.equip(STAFF_OF_FIRE);
                sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE));
            }

            // Attempt teleport
            Microbot.log("Attempting Varrock teleport (" + (i + 1) + "/" + MAX_RETRIES + ")");
            Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);

            // Wait up to 12s for teleport success
            boolean success = sleepUntil(
                    () -> Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), VARROCK_SQUARE) < 10,
                    4000
            );

            if (success) {
                Microbot.log("Teleport successful! Arrived near Varrock Square.");
                break;
            }

            Microbot.log("Teleport failed, retrying...");
            sleep(TELEPORT_INTERVAL_MS);
        }

        // Give client a small settle time after teleport
        sleep(2000, 6000);
        Microbot.log("Finished Varrock teleport routine.");
    }

    private void handleVarrockTeleport() {
        final int MAX_RETRIES = 5;
        final int TELEPORT_INTERVAL_MS = 2600;

                for (int i = 0; i < MAX_RETRIES; i++) {
                    // Equip staff if needed
                    if (Rs2Inventory.contains(STAFF_OF_FIRE) && !Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                        Rs2Inventory.equip(STAFF_OF_FIRE);
                        sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE));
                    }

                    // Attempt teleport
                    Microbot.log("Attempting Varrock teleport (" + (i + 1) + "/" + MAX_RETRIES + ")");
                    Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);

                    // Wait up to 12s for teleport success
                    boolean success = sleepUntil(
                            () -> Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), VARROCK_SQUARE) < 10,
                            4000
                    );

                    if (success) {
                        Microbot.log("Teleport successful! Arrived near Varrock Square.");
                        break;
                    }

                    Microbot.log("Teleport failed, retrying...");
                    sleep(TELEPORT_INTERVAL_MS);
                }

                // Give client a small settle time after teleport
                sleep(2000, 6000);
                Microbot.log("Finished Varrock teleport routine.");
                state = MossKillerState.TELEPORT;
                if (round2) {Microbot.bryoFinished = true; if(BreakHandlerScript.breakIn < 30 * 60) {BreakHandlerScript.breakIn = 30 * 60; Microbot.log("Made break 30 minutes");} round2 = false;}
                state = MossKillerState.WALK_TO_BANK;
            }

    public void stayOutOfMeleeRangeFromBryophytaInstancedSafe() {
        var bryophyta = Rs2Npc.getNpcs("Bryophyta").findFirst().orElse(null);
        if (bryophyta == null) return;

        LocalPoint bryoLocal = bryophyta.getLocalLocation();
        LocalPoint playerLocal = Microbot.getClient().getLocalPlayer().getLocalLocation();
        if (bryoLocal == null || playerLocal == null) return;

        // Bryophyta is 3x3, so center is +1 tile NE from true tile (SW corner)
        int bryoCenterX = bryoLocal.getSceneX() + 1;
        int bryoCenterY = bryoLocal.getSceneY() + 1;

        int dx = Math.abs(bryoCenterX - playerLocal.getSceneX());
        int dy = Math.abs(bryoCenterY - playerLocal.getSceneY());
        int distance = Math.max(dx, dy);

        // For magic: stay OUT of melee range (> 3 tiles from center)
        if (distance <= 3) {
            // Escape AWAY from center, move 3 tiles out
            int escapeX = playerLocal.getSceneX() > bryoCenterX ? 3 : -3;
            int escapeY = playerLocal.getSceneY() > bryoCenterY ? 3 : -3;

            Microbot.log("⚠️ Too close for magic (dist: " + distance + "), escaping 3 tiles away...");
            moveAndClickAdjacentTile(escapeX, escapeY);
            sleep(100, 300);
        }
    }

    private void moveAndClickAdjacentTile(int dx, int dy) {
        LocalPoint playerLocal = Microbot.getClient().getLocalPlayer().getLocalLocation();
        if (playerLocal == null) return;

        int targetX = playerLocal.getX() + (dx << Perspective.LOCAL_COORD_BITS);
        int targetY = playerLocal.getY() + (dy << Perspective.LOCAL_COORD_BITS);
        LocalPoint targetLocal = new LocalPoint(targetX, targetY);

        Point canvas = Perspective.localToCanvas(
                Microbot.getClient(),
                targetLocal,
                Microbot.getClient().getPlane()
        );

        if (canvas != null) {
            Microbot.getMouse().click(canvas);
            Microbot.log("🏃 Clicked escape tile at: " + canvas);
        } else {
            Microbot.log("❌ Could not calculate canvas point for escape");
        }
    }

    public void lootBoss1() {
        Microbot.log("💰 Starting boss loot collection");

        // Ensure we have at least 2 free slots before looting
        while (Rs2Inventory.emptySlotCount() < 2 && Rs2Inventory.contains(FOOD)) {
            Microbot.log("🍖 Eating food to make inventory space...");
            if (Rs2Inventory.interact(FOOD, "Eat")) {
                sleepUntil(() -> Rs2Inventory.emptySlotCount() >= 2 || !Rs2Inventory.contains(FOOD), 2000);
            } else {
                Microbot.log("❌ Failed to eat food, can't make room");
                break;
            }
        }

        // Priority loot: Big bones and Mossy key - use varargs String array
        LootingParameters bossLootParams = new LootingParameters(
                20,      // range
                1,       // minItems
                1,       // minQuantity
                0,       // minInvSlots (0 = don't care about free slots, we handled it above)
                false,   // delayedLooting
                false,   // antiLureProtection
                "Big bones", "Mossy key", "Law rune", "Air rune", "Chaos rune", "Death rune", "Nature rune", "Steel arrow", "Coins"  // names as varargs
        );

        // First loot attempt
        Microbot.log("🔍 Attempting to loot priority items...");

        if (Rs2GroundItem.lootItemsBasedOnNames(bossLootParams)) {
            Microbot.log("📦 Looted priority items");
            //sleepUntil(() -> Rs2Inventory.contains(BIG_BONES), 3000);
        }

        sleep(500, 1000);

        // Check if we got the essential loot (big bones)
        if (!Rs2Inventory.contains(BIG_BONES)) {
            Microbot.log("⚠️ Didn't get Big bones, waiting and retrying...");
            sleep(1000, 2000);

            // Retry
            if (Rs2GroundItem.lootItemsBasedOnNames(bossLootParams)) {
                Microbot.log("📦 Retry loot attempt for Big bones");
                sleepUntil(() -> Rs2Inventory.contains(BIG_BONES), 3000);
            }
        }

        if (!Rs2Inventory.hasItemAmount(BIG_BONES, 4) && !Rs2Inventory.contains(MOSSY_KEY)) {
            Microbot.log("🔄 Need more kills (have " + Rs2Inventory.count(BIG_BONES) + "/4) and no mossy key, resetting boss...");
            handleBossReset1();
        }
    }

    private void handleBossReset1() {
        Microbot.log("Doing quick check before doing reset logic");
        if (Microbot.getClient().getTopLevelWorldView().getScene().isInstance()) {
            if (sleepUntil(() -> Rs2Npc.getNpc("Bryophyta") != null, 2000)) {
                return;  // ← Now exits if Bryophyta DOES appear
            }
        }
        Microbot.log("🪨 Interacting with rock to reset boss");
        //Rs2GameObject.interact(32535, "Clamber");
        if (Rs2GameObject.interact(32535, "Clamber")) {
            boolean dialogueAppeared = sleepUntil(() -> Rs2Dialogue.isInDialogue(), 5000);
            if (dialogueAppeared) {
                Microbot.log("⌨️ Selecting reset option...");
                sleep(300, 600);
                Rs2Dialogue.keyPressForDialogueOption(1);

                sleepUntil(() -> !Rs2Dialogue.isInDialogue(), 5000);
                if (sleepUntil(() -> !Microbot.getClient().getTopLevelWorldView().getScene().isInstance(), 5000)) {
                    Microbot.log("we are out of the instance, success");
                    Microbot.log("reset locked after performing ONE valid reset.");
                    resetUnlocked = false;
                }

                // Wait for gate to become available after reset
                Microbot.log("⏳ Waiting for gate to reset...");
                sleepUntil(() -> Rs2GameObject.exists(32534), 8000);
                Microbot.log("✅ Gate is ready!");

                sleep(300, 600); // Small buffer for safety
            } else {
                Microbot.log("⚠️ Dialogue didn't appear after clamber");
            }
        } else {
            Microbot.log("❌ Failed to interact with rock");
        }

        // Eat up to healthy HP before next fight
        if (Rs2Inventory.contains(FOOD)) {
            Microbot.log("🍖 Eating before next fight...");
            if (eatAt(70)) {
                sleep(600, 1000);
                eatAt(80);
            }
        }

        // Walk back to gate
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        int distanceToGate = Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT);

        if (distanceToGate > 5) {
            Microbot.log("🚶 Walking to boss gate (distance: " + distanceToGate + ")");

            if (distanceToGate > 10) {
                walkTo(OUTSIDE_BOSS_GATE_SPOT, 10);
                sleepUntil(() -> Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), OUTSIDE_BOSS_GATE_SPOT) <= 10
                        && !Rs2Player.isMoving(), 8000);
            } else {
                Rs2Walker.walkFastCanvas(OUTSIDE_BOSS_GATE_SPOT, true);
                sleepUntil(() -> Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), OUTSIDE_BOSS_GATE_SPOT) < 5
                        && !Rs2Player.isMoving(), 5000);
            }
        }

        // Handle combat if needed, then enter
        handleGateEntry1();
    }

    private void handleGateEntry1() {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if (!bossMode || Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) >= 5) {
            return;
        }

        Microbot.log("🚪 Near boss gate (distance: " + Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) + ")");

        // Handle combat before entry
        if (Rs2Player.isInCombat()) {
            Microbot.log("⚔️ In combat before entering boss room, handling...");
            handleCombatBeforeBossEntry();
        } else {
            Microbot.log("✅ Not in combat, proceeding to gate");
        }

        // Open gate and enter
        if (Rs2GameObject.interact(32534, "Open")) {
            Microbot.log("🚪 Opening gate...");

            boolean dialogueAppeared = sleepUntil(Rs2Dialogue::isInDialogue, 5000);

            if (dialogueAppeared) {
                Microbot.log("📜 Dialogue appeared, progressing...");
                sleep(250, 500);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleep(500, 1000);
                Rs2Keyboard.typeString("1");

                // Activate prayer immediately after entering
                if (config.magicBoss() &&
                        Rs2Player.getBoostedSkillLevel(PRAYER) > 36 &&
                        !Rs2Prayer.isPrayerActive(PROTECT_MAGIC)) {
                    Microbot.log("🙏 Activating Protect from Magic");
                    Rs2Prayer.toggle(PROTECT_MAGIC, true);
                }

                Microbot.log("Waiting for dialogue to close...");
                sleepUntil(() -> !Rs2Dialogue.isInDialogue(), 5000);

                if (!Rs2Dialogue.isInDialogue() && sleepUntil(() -> Microbot.getClient().getTopLevelWorldView().getScene().isInstance(), 3000)) {
                    Microbot.log("Entered boss room!");

                    // --- FIX IS HERE ---
                    timeEnteredInstance = System.currentTimeMillis();
                    Microbot.log("⏱️ Timer Started: " + timeEnteredInstance);
                    // -------------------

                    //if (Rs2Npc.getNpc("Bryophyta") != null) {
                       // handleBryophytaAttack();
                   // }
                    state = MossKillerState.FIGHT_BOSS;
                }
            }
        }
    }

    private void handleBossDeath1() {
        if (!Microbot.getClient().getTopLevelWorldView().getScene().isInstance()) {
            Microbot.log("⚠️ Not in instance, cannot handle boss death");
            state = MossKillerState.WALK_TO_MOSS_GIANTS;
            return;
        }

        growthlingKillCount = 0;
        attackedGrowthlingIds.clear();
        lastGrowthlingClickTime = 0;
        waitingForFirstGrowthlingXp = false;

        if (Rs2Prayer.isPrayerActive(PROTECT_MAGIC)) {
            Rs2Prayer.toggle(PROTECT_MAGIC, false);
        }

        Microbot.log("💀 Boss is dead, let's loot.");
        //sleep(1000, 3000);

        //int bonesPreLoot = Rs2Inventory.count(BIG_BONES);
        lootBoss1();

        // Check if we have a mossy key to use on chest
        if (Rs2Inventory.contains(MOSSY_KEY)) {
            Microbot.log("🔑 Mossy key detected! Opening chest for bonus loot");
            handleChestLoot();

            // After chest is looted, Bryophyta always respawns
            Microbot.log("⏳ Waiting for Bryophyta to respawn...");
            sleepUntil(() -> Rs2Npc.getNpc("Bryophyta") != null, 10000);

            Microbot.log("✅ Bryophyta respawned! Continuing fight");
            state = MossKillerState.FIGHT_BOSS;
            return;
        }

        // Check if we've killed enough bosses
        if (Rs2Inventory.count(BIG_BONES) >= 4 && !round1) {
            Microbot.log("🎉 You have killed enough Bryophyta, round 1 completed");
            round1 = true;
            handleVarrockTeleport();
        } else if (Rs2Inventory.count(BIG_BONES) >= 4 && round1) {
            Microbot.log("🎉 You have killed enough Bryophyta, round 2 completed, should reset now");
            round2 = true;
            round1 = false;
            handleVarrockTeleport();
        }
    }



    private void handleChestLoot() {
        Microbot.log("🎁 Opening chest with mossy key");
        // Make sure we have inventory space (eat food if needed)
        while (Rs2Inventory.emptySlotCount() < 2 && Rs2Inventory.contains(FOOD)) {
            Microbot.log("🍖 Eating food to make space for chest loot...");
            if (Rs2Inventory.interact(FOOD, "Eat")) {
                sleep(1200);
                int emptySlots = Rs2Inventory.emptySlotCount();
                Microbot.log("Empty slots after eating: " + emptySlots);
            } else {
                Microbot.log("Failed to eat food. Can't make room.");
                break;
            }
        }

        // Interact with chest
        if (Rs2GameObject.interact(56378, "Open")) {
            Microbot.log("✅ Chest triggered to open!");

            // Wait for loot to appear
            if (sleepUntil(Rs2Player::isMoving,1600)) {sleepUntil(() -> !Rs2Player.isMoving());}
            sleep(1000,3000); //// static sleep just incase

            // Loot everything from chest using same parameters as boss loot
            LootingParameters chestLootParams = new LootingParameters(
                    20,      // range
                    1,       // minItems
                    1,       // minQuantity
                    0,       // minInvSlots
                    false,   // delayedLooting
                    false,   // antiLureProtection
                    bryophytaDrops  // All items from your drop list
            );

            Microbot.log("📦 Looting chest items (first pass)");
            for (String lootItem : bryophytaDrops) {
                if (Rs2GroundItem.lootItemsBasedOnNames(chestLootParams)) {
                    Microbot.log("Looting " + lootItem);
                    sleepUntil(() -> Rs2Inventory.contains(lootItem), 3000);
                }
            }

            sleep(1000,2000);

            // Second pass to catch any missed items
            Microbot.log("📦 Looting chest items (second pass)");
            for (String lootItem : bryophytaDrops) {
                if (Rs2GroundItem.lootItemsBasedOnNames(chestLootParams)) {
                    Microbot.log("Looting " + lootItem);
                    sleepUntil(() -> Rs2Inventory.contains(lootItem), 3000);
                }
            }

            Microbot.log("✅ Chest loot complete");

        } else {
            Microbot.log("❌ Failed to interact with chest");
        }
    }

    private void handleInstanceExit() {
        if (Microbot.getClient().getTopLevelWorldView().getScene().isInstance()) {
            Microbot.log("🚪 Exiting instance...");
            if (Rs2GameObject.interact(32535, "Clamber")) {
                sleepUntil(() -> Rs2Dialogue.isInDialogue(), 5000);
                if (Rs2Dialogue.isInDialogue()) {
                    sleep(300, 600);
                    Rs2Dialogue.keyPressForDialogueOption(2); // Option 2 = Leave
                    sleepUntil(() -> !Rs2Dialogue.isInDialogue(), 5000);
                }
            }
            sleepUntil(() -> !Microbot.getClient().getTopLevelWorldView().getScene().isInstance(), 8000);
            Microbot.log("✅ Exited instance");
        }
    }

    private void handleCombatBeforeBossEntry() {
        long startTime = System.currentTimeMillis();
        long timeout = 25000; // 10 second timeout

        while (Rs2Player.isInCombat() && (System.currentTimeMillis() - startTime) < timeout) {
            // Find NPC that's interacting with us
            Rs2NpcModel attacker = Rs2Npc.getNpcs()
                    .filter(npc -> {
                        Actor target = npc.getInteracting();
                        return target != null && target.equals(Microbot.getClient().getLocalPlayer());
                    })
                    .findFirst()
                    .orElse(null);

            if (attacker != null) {
                Microbot.log("🎯 Found attacker: " + attacker.getName() + " - fighting back!");
                Rs2NpcModel currentTarget = (Rs2NpcModel) Rs2Player.getInteracting();
                if (Rs2Prayer.isPrayerActive(PROTECT_MAGIC)) {Rs2Prayer.toggle(PROTECT_MAGIC, false);}

                // Only attack if we're not already attacking it
                if (currentTarget == null || currentTarget.getId() != attacker.getId()) {
                    Rs2Npc.interact(attacker.getId(), "Attack");
                    sleep(300, 600);
                }
            } else {
                Microbot.log("⏳ In combat but can't find attacker, waiting...");
            }

            sleep(100, 200); // Quick loop to stay responsive

            // Break if we're out of combat
            if (!Rs2Player.isInCombat()) {
                Microbot.log("✅ Combat ended!");
                break;
            }
        }

        if (Rs2Player.isInCombat()) {
            Microbot.log("⚠️ Still in combat after 10s timeout, attempting to proceed anyway");
        }
    }



    public void walkToMossGiants() {

        if (bossMode && Microbot.bryoFinished) return;

        if (!Rs2Inventory.contains(BRONZE_AXE)) state = MossKillerState.WALK_TO_BANK;

        if (BreakHandlerScript.breakIn <= 45) {
            Microbot.log("you're gonna break soon, may as well idle at bank for a couple mins and restock food");
            //Rs2Bank.walkToBankAndUseBank();
            //if(Rs2Bank.isOpen()) {Rs2Bank.withdrawAll(FOOD);}
            if(Rs2Bank.isOpen()) Rs2Bank.closeBank();
            sleepUntil(() -> !Microbot.isLoggedIn(), 60000);
            return;
        }

        if (Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), VARROCK_SQUARE) < 10 && Rs2Player.getWorldLocation().getPlane() == 0) {
            if (!Rs2Inventory.hasItemAmount(SWORDFISH, 15)) {
                Microbot.log("you're at varrock square and could restock food, let's do that");
                walkToBank(GRAND_EXCHANGE);
                sleep(2000,6000);
                Rs2GrandExchange.openExchange();
                sleep(2000,5000);
                Rs2GrandExchange.collectAllToBank1();
                sleep(1500,2900);
                if (!Rs2GrandExchange.isAnyF2pSlotAvailable()) {
                    Rs2GrandExchange.abortOfferSmart(true);
                    sleep(2000, 4000);
                }
                Rs2GrandExchange.buyItemExact("Swordfish", 250, 400);
                sleep(2000,5000);
                Rs2GrandExchange.collectAllToBank1();
                sleep(1000,5000);
                Rs2GrandExchange.closeExchange();
                sleep(1000,2000);
                Rs2Bank.openBank1();
                sleep(1000,2000);
                state = MossKillerState.BANK;
                return;
            }
        }


        int currentPitch = Rs2Camera.getPitch(); // Assume Rs2Camera.getPitch() retrieves the current pitch value.
        int currentZoom = Rs2Camera.getZoom(); // Assume Rs2Camera.getZoom() retrieves the current zoom level.
        // Ensure the pitch is within the desired range
        if (currentPitch < 350) {
            int pitchValue = Rs2Random.between(360, 400); // Random value within the range
            Rs2Camera.setPitch(pitchValue); // Adjust the pitch
        }

        if (currentZoom < 300) {
            int zoomValue = Rs2Random.between(380, 300);
            Rs2Camera.setZoom(zoomValue);
        }

        if(config.keyThreshold() == 1 && Rs2Inventory.contains(MOSSY_KEY) && Rs2Inventory.contains(BRONZE_AXE)) {
            bossMode = true;
        }

        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if(!Rs2Inventory.contains(FOOD)) {
            state = MossKillerState.WALK_TO_BANK;
        }

        //toggleRunEnergy();

        System.out.println("getting here to walk to sewer entrance");

        if (config.magicBoss()) {
            if (Rs2Player.getRealSkillLevel(MAGIC) > 34) {Rs2Combat.setAutoCastSpell(Rs2CombatSpells.FIRE_BOLT, true);}
            else {Rs2Combat.setAutoCastSpell(Rs2CombatSpells.WIND_BOLT, false);}
            Microbot.log("setting firebolt autocast and checking for auto retaliate off");
            if(!Rs2Combat.setAutoRetaliate(false)){
                Microbot.log("no need to turn off auto retaliate.");
                //state = MossKillerState.EXIT_SCRIPT;
            }

        }

        // Check if in instance OR near the gate
        boolean inInstance = Microbot.getClient().getTopLevelWorldView().getScene().isInstance();
        int distanceToGate = Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT);

        if (inInstance) {
            Microbot.log("⚠️ Still in instance, cannot walk to moss giants yet");
            return;
        }

        if (distanceToGate < 10) {
            Microbot.log("⚠️ Too close to boss gate (distance: " + distanceToGate + "), entering boss");
            state = MossKillerState.FIGHT_BOSS; // Change state to enter boss!
            return;
        }

        // Only restore prayer if NOT in instance
        if (Rs2Prayer.isOutOfPrayer() || Rs2Player.getBoostedSkillLevel(PRAYER) < Rs2Player.getRealSkillLevel(PRAYER)) {
            Microbot.log("⛪ Prayer not full, restoring at altar");
            walkTo(3255, 3483, 0);
            Rs2GameObject.interact(14860, "Pray");
            sleepUntil(() -> Rs2Player.getBoostedSkillLevel(PRAYER) >= Rs2Player.getRealSkillLevel(PRAYER), 12000);
            return;
        }

        System.out.println(Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), SEWER_ENTRANCE));
        System.out.println("test");

        if ((!bossMode && Rs2Walker.getDistanceBetween(playerLocation, SEWER_ENTRANCE) > 3 && playerLocation.getPlane() == 0)
        || (bossMode && Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) > 10 && playerLocation.getPlane() == 0)) {
            if (Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) < 10 || Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) < 10) {
                Microbot.log("est 1");// if near bank
                walkTo(SEWER_ENTRANCE, 5);
                sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, SEWER_ENTRANCE) < 3 && !Rs2Player.isMoving(), 3000);
                if (Rs2GameObject.exists(882)) { // open manhole
                    System.out.println("interacting sewer entrance");
                    Rs2GameObject.interact(882, "Climb-down");
                    sleep(2500,4500);}
                if (Rs2GameObject.exists(881)) { // closed manhole
                    System.out.println("interacting opening manhole");
                    Rs2GameObject.interact(881, "Open");
                    return;
                }
                return;
            }
        }

        System.out.println("getting here after walking to sewer entrance");

        if (Rs2Walker.getDistanceBetween(playerLocation, SEWER_ENTRANCE) < 10) {
            if (Rs2GameObject.exists(882)) { // open manhole
                System.out.println("interacting sewer entrance");
                Rs2GameObject.interact(882, "Climb-down");
                sleep(2500,4500);
                return;
            }
            if (Rs2GameObject.exists(881)) { // closed manhole
                System.out.println("interacting opening manhole");
                Rs2GameObject.interact(881, "Open");
                return;
            }
        }
        System.out.println(Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) > 10);

        if (Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) > 10 || Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) <= 20) {
                        if (bossMode) {
                            //BreakHandlerScript.setLockState(true);
                            /// / changing this for whatever, just to not break our script
                            if (Rs2Inventory.contains(FOOD)) {
                                if (eatAt(70)) {
                                    sleep(1900,2200);
                                    eatAt(80);
                                }
                                Microbot.log("Walking to outside boss gate spot");
                                Rs2Equipment.unEquip(EquipmentInventorySlot.WEAPON);
                                sleep(2000,5000);
                                Rs2Inventory.equip(STAFF_OF_FIRE);
                                sleep(1000,5000);
                                Microbot.log("fired off an unequip and equip to initiliaze inventory IF empty inventory");
                                if (!Rs2Inventory.containsAll(new int[]{AIR_RUNE, LAW_RUNE, FOOD, CHAOS_RUNE})) {
                                    Rs2Bank.walkToBankAndUseBank();
                                    sleep(5000);
                                        state = MossKillerState.WALK_TO_BANK;
                                }
                                if (Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) > 10) {
                                    walkTo(OUTSIDE_BOSS_GATE_SPOT, 10);
                                    sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) <= 10 && !Rs2Player.isMoving(), 600);}
                                else if (Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) < 10) {
                                    Rs2Walker.walkFastCanvas(OUTSIDE_BOSS_GATE_SPOT, true);
                                    sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) < 5 && !Rs2Player.isMoving(), 600);
                                }

                                if (bossMode && Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) < 5) {
                                    if (Rs2Player.isInCombat()) {Microbot.log("🚪 Near boss gate (distance: " + Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) + ")");

                                        boolean inCombat = Rs2Player.isInCombat();
                                        Microbot.log("🔍 Combat check: " + inCombat);

                                        if (inCombat) {
                                            Microbot.log("⚔️ In combat before entering boss room, waiting 5s...");
                                            if (config.magicBoss() &&
                                                    Rs2Player.getBoostedSkillLevel(PRAYER) > 36 &&
                                                    Rs2Prayer.isPrayerActive(PROTECT_MAGIC)) {
                                                Microbot.log("🙏 Deactivating Protect from Magic");
                                                Rs2Prayer.toggle(PROTECT_MAGIC, false);
                                            }
                                            sleep(5000);

                                            boolean stillInCombat = Rs2Player.isInCombat();
                                            Microbot.log("🔍 After 5s wait, still in combat: " + stillInCombat);

                                            if (stillInCombat) {
                                                Microbot.log("🎯 Calling handleCombatBeforeBossEntry()");
                                                handleCombatBeforeBossEntry();
                                            } else {
                                                Microbot.log("✅ Combat ended naturally during wait");
                                            }
                                        } else {
                                            Microbot.log("✅ Not in combat, proceeding to gate");
                                        }}
                                    if (Rs2GameObject.exists(32534) && Rs2GameObject.interact(32534, "Open")) {
                                        Microbot.log("🚪 Opening gate...");
                                        sleepUntil(Rs2Dialogue::isInDialogue, 5000);

                                        if (Rs2Dialogue.isInDialogue()) {
                                            Microbot.log("📜 Dialogue appeared, progressing...");
                                            sleep(250);
                                            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                                            sleep(500, 1700);
                                            Rs2Keyboard.typeString("1");
                                            if (config.magicBoss() &&
                                                    Rs2Player.getBoostedSkillLevel(PRAYER) > 36 &&
                                                    !Rs2Prayer.isPrayerActive(PROTECT_MAGIC)) {
                                                Microbot.log("🙏 Activating Protect from Magic");
                                                Rs2Prayer.toggle(PROTECT_MAGIC, true);
                                            }
                                            // Wait for dialogue to CLOSE after selecting option
                                            Microbot.log("⏳ Waiting for dialogue to close...");
                                            sleepUntil(() -> !Rs2Dialogue.isInDialogue(), 5000);

                                            if (!Rs2Dialogue.isInDialogue()) {
                                                Microbot.log("✅ Entered boss room!");
                                                state = MossKillerState.FIGHT_BOSS;
                                            } else {
                                                Microbot.log("⚠️ Dialogue still open after 5s");
                                            }
                                        }
                                    }
                                }
                                return;
                            } else {
                                state = MossKillerState.BANK;
                                return;
                            }
                        } else {
                            walkTo(MOSS_GIANT_SPOT);
                        }
                    }
            /*
            if (Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) < 7) {
                state = MossKillerState.FIGHT_MOSS_GIANTS;
                return;
            }
            */
            state = MossKillerState.WALK_TO_MOSS_GIANTS;
        }



    public void handleBanking(){
        if(Microbot.bryoFinished) return;
        Microbot.log("removing mossy key requirements"); // && !Rs2Inventory.contains(MOSSY_KEY)
        if(bossMode && !Rs2Bank.isNearBank(6) && BreakHandlerScript.breakIn > 45) {
            state = MossKillerState.WALK_TO_BANK;
            return;
        }

        if (Rs2Bank.openBank1()) {
            sleep(10000);
            Rs2Bank.depositAll();
            sleep(5000);
            if (!Rs2Bank.hasItem(MOSSY_KEY)) {
                Microbot.bryoFinished = true;
                Microbot.log("Prelimenary no mossy key check_ DO NO MOSS BANKING");
            }
        }

        if(Microbot.bryoFinished) return;

        if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
            if (Rs2Bank.openBank1()) {
                sleepUntil(Rs2Bank::isOpen, 15000);
                Rs2Bank.depositAllExcept(BRONZE_AXE, LAW_RUNE, AIR_RUNE, SWORDFISH);
                Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE);
                sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE));
                Rs2Bank.depositAll();
                sleep(1000);
            }
        }

        if (!Rs2Equipment.isWearing(EquipmentInventorySlot.AMULET)) {
            Rs2Bank.depositAll();
            sleep(2000,5000);
            if (!Rs2Bank.withdrawAndEquip(AMULET_OF_MAGIC)) {Rs2Bank.withdrawAndEquip(AMULET_OF_POWER);}
            sleep(2000,5000);
        }

        if (!Rs2Equipment.isWearing(EquipmentInventorySlot.BODY)) {
            Rs2Bank.depositAll();
            sleep(2000,5000);
            if (!Rs2Bank.withdrawAndEquip(AMULET_OF_MAGIC)) {Rs2Bank.withdrawAndEquip(MONKS_ROBE_TOP);}
            sleep(2000,5000);
        }

        if (!Rs2Equipment.isWearing(EquipmentInventorySlot.LEGS)) {
            Rs2Bank.depositAll();
            sleep(2000,5000);
            if (!Rs2Bank.withdrawAndEquip(AMULET_OF_MAGIC)) {Rs2Bank.withdrawAndEquip(MONKS_ROBE);}
            sleep(2000,5000);
        }

        /// // right now, inventory may not initliaze, however they're all pretty much ready to go on login
        if(bossMode && Rs2Inventory.hasItemAmount(AIR_RUNE, 500) &&
                Rs2Inventory.hasItem(LAW_RUNE) &&
                Rs2Inventory.hasItem(CHAOS_RUNE) &&
                Rs2Inventory.hasItemAmount(FOOD, 20) &&
                Rs2Inventory.hasItem(BRONZE_AXE) &&
                Rs2Equipment.isWearing(STAFF_OF_FIRE) && BreakHandlerScript.breakIn > 45) {
            state = MossKillerState.WALK_TO_MOSS_GIANTS;
            return;
        }
        /// // trying to catch it before it goes anywhere
        banking();

        if (BreakHandlerScript.breakIn <= 45) {
            if(Rs2Bank.isOpen()) Rs2Bank.closeBank();
            sleepUntil(() -> !Microbot.isLoggedIn());
            return;
        }
        sleep(500, 1000);
    }

    public void banking() {

        if(Rs2Bank.openBank()) {
            sleepUntil(Rs2Bank::isOpen, 60000);
            Rs2Bank.depositAllExcept(BRONZE_AXE, MITHRIL_AXE);
            sleepUntil(Rs2Inventory::isEmpty);
            sleep(1000, 1500);
            if (Rs2Inventory.hasItem(BRONZE_AXE, MITHRIL_AXE)) {
                Rs2Bank.depositOne(MITHRIL_AXE);
            }
            if (Rs2Inventory.hasItemAmount(BRONZE_AXE,2)) {
                Rs2Bank.depositOne(BRONZE_AXE);
            }
            boolean staffOfFire = false;
            if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                if (!Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE)) {staffOfFire = true;}
                sleep(2000,5000);
            }
            boolean bronzeAxe = false;
            if (!Rs2Bank.hasItem(BRONZE_AXE)) {
                {bronzeAxe = true;}
            }
            if (staffOfFire) {
                Microbot.log("has no fire staff");
                Rs2Bank.walkToBank1(GRAND_EXCHANGE);
                Microbot.log("initatied walk to ge");
                sleep(5200);
                Rs2GrandExchange.openExchange();
                sleep(1400,2900);
                Rs2GrandExchange.collectAllToBank1();
                sleep(1990,2025);
                Rs2GrandExchange.buyItem("Staff of fire", 5000, 2);
                sleep(1400,2900);
                Rs2GrandExchange.collectAllToBank();
                sleep(1400,2900);
                Rs2Bank.openBank1();
                sleep(1400,2900);
                Rs2Bank.depositAll();
                sleep(1400,2900);
                if (!Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE)) {
                    Microbot.log("still couldn't equip a fire staff after trying to purchase");
                }
                sleep(2500,4000);
            }
            if (bronzeAxe) {
                Microbot.log("no bronze axe");
                Rs2Bank.walkToBank1(GRAND_EXCHANGE);
                Microbot.log("initatied walk to ge");
                sleep(5200);
                Rs2GrandExchange.openExchange();
                sleep(1400,2900);
                Rs2GrandExchange.collectAllToBank1();
                sleep(1990,2025);
                Rs2GrandExchange.buyItem("Bronze axe", 1000, 10);
                sleep(1400,2900);
                Rs2GrandExchange.collectAllToBank();
                sleep(1400,2900);
                Rs2Bank.openBank1();
                sleep(1400,2900);
                Rs2Bank.depositAll();
                sleep(1400,2900);
                sleep(2500,4000);
            }
            if (!Rs2Bank.hasItem(new int[]{AIR_RUNE}, 1000) && config.magicBoss() && BreakHandlerScript.breakIn > 45) {
                Microbot.log("has less than 1000 air runes");
                Rs2Bank.walkToBank1(GRAND_EXCHANGE);
                Microbot.log("initatied walk to ge");
                sleep(5200);
                Rs2GrandExchange.openExchange();
                sleep(1400,2900);
                Rs2GrandExchange.collectAllToBank1();
                sleep(1990,2025);
                Rs2GrandExchange.buyItem("Air rune", 7, 4000);
                sleep(1400,2900);
                Rs2GrandExchange.collectAllToBank();
                sleep(1400,2900);
                Rs2Bank.openBank1();
                sleep(1400,2900);
                Rs2Bank.depositAll();
                sleep(1400,2900);
            }
            if (!Rs2Bank.hasItem(new int[]{CHAOS_RUNE}, 333) && config.magicBoss() && BreakHandlerScript.breakIn > 45) {
                Microbot.log("has less than 333 chaos runes");
                Rs2Bank.walkToBank1(GRAND_EXCHANGE);
                Microbot.log("initatied walk to ge");
                sleep(5200);
                Rs2GrandExchange.openExchange();
                sleep(4400,6900);
                Rs2GrandExchange.collectAllToBank1();
                sleep(5000);
                Rs2GrandExchange.buyItem("chaos rune", 70, 1250);
                sleep(1400,2900);
                Rs2GrandExchange.collectAllToBank();
                sleep(1400,2900);
                Rs2Bank.openBank1();
                sleep(1400,2900);
                Rs2Bank.depositAll();
                sleep(1400,2900);
            }
            if(!hasItem(AIR_RUNE) || !hasItem(LAW_RUNE) || !hasItem(FIRE_RUNE) || !hasItem(FOOD) && (BreakHandlerScript.breakIn > 45)){
                Rs2Bank.walkToBank1(GRAND_EXCHANGE);
                Microbot.log("initatied walk to ge");
                sleep(5200);
                sleep(1400,2900);
                Rs2Bank.openBank1();
                sleep(1400,2900);
                Rs2Bank.depositAll();
                sleep(1400,2900);
                if (!Rs2Bank.hasItem(FOOD)) {
                        if (Rs2Bank.hasItem(LOOT_LIST2)) {
                            for (int lootItem : LOOT_LIST2) {
                                if (Rs2Bank.hasItem(lootItem)) {
                                    Rs2Bank.withdrawAll(lootItem);
                                    sleep(600, 1200);
                                }
                            }

                            sleep(1200);
                            if (Rs2Bank.isOpen()) {Rs2Bank.closeBank();}
                            sleep(1200);
                            Rs2GrandExchange.openExchange();
                            sleep(1200);
                            // Only sell loot list items at -10%
                            //sellLoot(Arrays.stream(LOOT_LIST2).boxed().collect(Collectors.toList()), -10);

                            Rs2GrandExchange.collectAllToBank();
                        }
                    sleep(1400,2900);
                        Rs2GrandExchange.collectAllToBank1();
                        sleep(1500,2900);
                    GrandExchangeRequest request = GrandExchangeRequest.builder()
                            .action(GrandExchangeAction.BUY)
                            .itemName("Swordfish")
                            .price(250)
                            .quantity(400)
                            .resultIndex(1)// Ensures exact name match
                            .exact(true)
                            .build();
                    Rs2GrandExchange.processOffer(request);
                    sleep(1400,2900);
                    Rs2GrandExchange.collectAllToBank1();
                    sleep(1500,2900);
                    ///  removing buying fire rune
                    Rs2GrandExchange.buyItem("Fire rune", 10, 10);
                    sleep(1400,2900);
                    Rs2GrandExchange.collectAllToBank();
                    sleep(1400,2900);
                    Rs2GrandExchange.buyItem("Air rune", 7, 1000);
                    sleep(1400,2900);
                    Rs2GrandExchange.collectAllToBank1();
                    sleep(1400,2900);
                    Rs2GrandExchange.closeExchange();
                    sleep(1400,2900);
                    Rs2Bank.openBank1();
                    sleep(1000);
                    //return;
                }
                /*
                if (!Rs2Bank.hasItem(FIRE_RUNE)) {
                    walkTo(3163, 3484,0);
                    sleep(1400,2900);
                    Rs2GrandExchange.buyItem("Fire rune", 10, 100);
                    sleep(1400,2900);
                    Rs2GrandExchange.collectAllToBank();
                    sleep(1400,2900);
                    return;
                }*/
                if (!Rs2Bank.hasItem(LAW_RUNE) && BreakHandlerScript.breakIn > 45) {
                    Microbot.log("you've probably died, cus you got no law runes, gonna restock law and chaos");
                    walkTo(3163, 3484,0);
                    Rs2GrandExchange.collectAllToBank1();
                    sleep(1500,2900);
                    sleep(1400,2900);
                    Rs2GrandExchange.buyItem("Law rune", 150, 125);
                    sleep(1400,2900);
                    Rs2GrandExchange.collectAllToBank();
                    Rs2GrandExchange.buyItem("Chaos rune", 100, 500);
                    sleep(1400,2900);
                    Rs2GrandExchange.collectAllToBank1();
                    sleep(1400,2900);
                    //return;
                }
                //state = MossKillerState.EXIT_SCRIPT;
                //return;
            }
            int keyTotal = Rs2Bank.count("Mossy key");
            Microbot.log("Key Total: " + keyTotal);
            if (keyTotal >= config.keyThreshold()){
                Microbot.log("keyTotal >= config threshold");
                /// /this will be restored again, when we reach a certain amount of keys (like 120 of them) and global flag will switch on to withdraw them
                bossMode = true;
                Rs2Bank.withdrawAll(MOSSY_KEY);
                //Rs2Bank.withdrawOne(MOSSY_KEY);
                Microbot.log("Sleeping until mossy key");
                sleepUntil(() -> Rs2Inventory.contains(MOSSY_KEY));
                sleep(1000, 1300);
                if(!Rs2Inventory.contains(MOSSY_KEY)) {Rs2Bank.withdrawAll(MOSSY_KEY);}
                sleepUntil(() -> Rs2Inventory.contains(MOSSY_KEY));
                sleep(1000, 1300);
                if (!Rs2Equipment.isWearing(BRONZE_AXE) && !Rs2Inventory.contains(BRONZE_AXE)) {withdrawOne(BRONZE_AXE);}
                sleepUntil(() -> Rs2Inventory.contains(BRONZE_AXE));
                sleep(200, 600);
                if (config.magicBoss()) {
                    //Rs2Bank.withdrawAll(CHAOS_RUNE);
                    //sleepUntil(() -> Rs2Inventory.contains(CHAOS_RUNE));
                    //sleep(1000, 1300);
                    if (config.magicBoss()) {
                        if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                            Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE);
                            sleep(1200);
                        }
                    }
                }
                if (!config.magicBoss()) for (int id : strengthPotionIds) {
                    if (hasItem(id)) {
                        withdrawOne(id);
                        break;
                    }
                }
                sleep(1000, 1300);

            } else if(bossMode && keyTotal > 0) {
                Microbot.log("bossMode and keyTotal > 0");
                Rs2Bank.withdrawAll(MOSSY_KEY);
                sleepUntil(() -> Rs2Inventory.contains(MOSSY_KEY));
                sleep(1000, 1300);
                if (Rs2Bank.hasItem(BRONZE_AXE) && !Rs2Inventory.hasItem(BRONZE_AXE))
                {
                    withdrawOne(BRONZE_AXE);
                sleepUntil(() -> Rs2Inventory.contains(BRONZE_AXE));
                sleep(600, 1200);}
                if (config.magicBoss()) {
                   // Rs2Bank.withdrawAll(CHAOS_RUNE);
                   // sleepUntil(() -> Rs2Inventory.contains(CHAOS_RUNE));
                    //sleep(1000, 1300);
                    if (config.magicBoss()) {
                        if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                            Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE);
                            sleep(1200);
                        }
                    }
                }
                if (!config.magicBoss()) for (int id : strengthPotionIds) {
                    if (hasItem(id)) {
                        withdrawOne(id);
                        break;
                    }
                }
                sleep(1000, 1300);
            } else if(keyTotal == 0) {

                Microbot.log("keyTotal == 0");
                Microbot.log("No keys, no bryo needed");
                if (BreakHandlerScript.breakIn > 100 && Microbot.isLoggedIn()) {Microbot.bryoFinished = true; Microbot.bryoFinished1 = true;}
                return;
                /*
                /// // removing bossmade being made false
                //bossMode = false;
                sleep(1000, 1300);
                if (!Rs2Inventory.hasItem(BRONZE_AXE) && !Rs2Equipment.isWearing(BRONZE_AXE))
                {
                    if (!withdrawOne(BRONZE_AXE)) {Rs2Bank.walkToBank(GRAND_EXCHANGE);
                        sleep(500);
                        Microbot.log("Fired off walk to ge by panel just incase");
                        ShortestPathPlugin.walkToViaPanel(new WorldPoint(3164, 3487,0));
                    sleep(5000,1000);
                    sleepUntil(() -> !Rs2Player.isMoving(),90000);
                    Rs2GrandExchange.openExchange();
                    sleep(5000);
                    Rs2GrandExchange.collectAllToBank1();
                    sleep(5000);
                        if (!Rs2GrandExchange.isAnyF2pSlotAvailable()) {
                            Rs2GrandExchange.abortOfferSmart(true);
                            sleep(2000, 4000);
                        }
                        sleep(2500);
                        Rs2GrandExchange.buyItem("Bronze axe", 1000, 10);
                        sleep(15000,30000);
                        Rs2GrandExchange.collectAllToBank1();
                        sleep(9000);
                        Rs2GrandExchange.closeExchange();
                        sleep(5000);
                        Rs2Bank.openBank1();
                        sleep(5000);
                        withdrawOne(BRONZE_AXE);
                    }
                    sleepUntil(() -> Rs2Inventory.contains(BRONZE_AXE));
                    sleep(600, 1200);}
                if (config.magicBoss()) {
                    //Rs2Bank.withdrawAllButOne(CHAOS_RUNE);
                    //sleepUntil(() -> Rs2Inventory.contains(CHAOS_RUNE));
                    sleep(1000, 1300);
                    if (config.magicBoss()) {
                        if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                            Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE);
                            sleep(1200);
                        }
                    }
                }
            */
            }
            Microbot.log(String.valueOf(config.isSlashWeaponEquipped()));

            if(!config.isSlashWeaponEquipped()){
                    if (!Rs2Bank.hasItem(KNIFE)) {
                        sleep(1400,2900);
                        walkTo(3217, 3417,1);
                        sleep(1400,2900);
                        Rs2GroundItem.pickup(KNIFE);
                        return;
                    }
                withdrawOne(KNIFE);
                sleep(500, 1200);
            }

            // Randomize withdrawal order and add sleep between each to mimic human behavior
            /// /removing withdrawing fire rune.....
        /// //removed withdrawing FOOD from withdrawitemWithRandomSleep
            withdrawItemWithRandomSleep(AIR_RUNE, LAW_RUNE, FIRE_RUNE, CHAOS_RUNE, FOOD);
            /// /removing withdrawing alchs
            //if(config.alchLoot()) {Rs2Bank.withdrawAll(NATURE_RUNE);}
            ///  changed from withdrawall to withdraw X
            //Rs2Bank.withdraw(FOOD, 14);
            sleepUntil(() -> Rs2Inventory.contains(FOOD));
            sleep(500, 1000);
            /// /removing checking for fire rune.....
            if (Rs2Inventory.containsAll(AIR_RUNE, LAW_RUNE, FOOD, CHAOS_RUNE)) {
                if(Rs2Bank.closeBank()){

                    state = MossKillerState.WALK_TO_MOSS_GIANTS;
                }
            }


        }

        if(!Rs2Inventory.containsAll(AIR_RUNE, LAW_RUNE, FOOD, CHAOS_RUNE) && !Rs2Bank.openBank()) {
            walkToBank();
            Rs2Bank.walkToBankAndUseBank();
        }
    }
/*

    // Plugin management methods
    public static boolean stopLootingPlugin() {
        AutoLooterPlugin autoLooterPlugin = (AutoLooterPlugin) Microbot.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getClass().getName().equals(AutoLooterPlugin.class.getName()))
                .findFirst()
                .orElse(null);

        if (autoLooterPlugin == null) {
            Microbot.log("AutoLooterPlugin not found");
            return false;
        }

        Microbot.getClientThread().invokeLater(() -> {
            try {
                Microbot.getPluginManager().setPluginEnabled(autoLooterPlugin, false);
                Microbot.stopPlugin(autoLooterPlugin);
            } catch (Exception e) {
                Microbot.log("Error stopping BreakHandler", e);
            }
        });
        return true;
    }

    public static void startLootingPlugin() {
        AutoLooterPlugin autoLooterPlugin = (AutoLooterPlugin) Microbot.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getClass().getName().equals(AutoLooterPlugin.class.getName()))
                .findFirst()
                .orElse(null);

        Microbot.getClientThread().invokeLater(() -> {
            try {
                Microbot.getPluginManager().setPluginEnabled(autoLooterPlugin, true);
                Microbot.startPlugin(autoLooterPlugin);
            } catch (Exception e) {
                Microbot.log("Error starting Autolooter", e);
            }
        });
    }
*/
    private void doBronzeAxeBlock() {
        walkTo(3233, 3293,0);
        sleep(1400,2900);
        Rs2GameObject.interact( 5581, "Take-axe");
        sleep(1400,2900);
    }

    private void withdrawItemWithRandomSleep(int... itemIds) {
        for (int itemId : itemIds) {
            Rs2Bank.withdrawAll(itemId);
            sleepUntil(() -> Rs2Inventory.contains(itemId), 3000);
            sleep(300, 700);
        }
    }

    private void getBronzeAxeFromInstance() {
        if(Rs2Inventory.isFull()) {Rs2Inventory.interact(FOOD, "Eat");}
        sleep(1200,1800);
        Rs2GameObject.interact( 32536, "Take-axe");
        sleepUntil(() -> Rs2Inventory.contains(BRONZE_AXE), 10000);
        eatAt(70);
        if (!Rs2Inventory.contains(BRONZE_AXE)) {
            Rs2GameObject.interact( 32536, "Take-axe");
            sleepUntil(() -> Rs2Inventory.contains(BRONZE_AXE));
        }
    }



    public void walkToVarrockWestBank(){
        BreakHandlerScript.setLockState(false);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        toggleRunEnergy();

        if (BreakHandlerScript.breakIn > 45) {
            if(!bossMode && Rs2Inventory.containsAll(AIR_RUNE, LAW_RUNE, FOOD)){
            state = MossKillerState.WALK_TO_MOSS_GIANTS;
            return;
        }}
        if(!Rs2Bank.isNearBank(6) && BreakHandlerScript.breakIn > 45){
            if (playerLocation.getY() > 6000) {state = MossKillerState.TELEPORT;}
            walkToBank();
        } else {
            System.out.println("distance to bank <5, bank now");
            state = MossKillerState.BANK;
        }
    }

    public void varrockTeleport(){
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        Microbot.log(String.valueOf(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE)));
        sleep(1000, 2000);
        if(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) <= 10 && playerLocation.getY() < 5000){
            state = MossKillerState.WALK_TO_BANK;
            return;
        }
        /// /no fire rune needed because we have the staff equipped
        if(Rs2Inventory.containsAll(AIR_RUNE,LAW_RUNE)){
            if (Rs2Inventory.contains(STAFF_OF_FIRE)) {
                Rs2Inventory.equip(STAFF_OF_FIRE);
                sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE));
            }
            Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);
            sleepUntil(() -> Rs2Player.getWorldLocation().getY() < 6000, 10000);
            if (BreakHandlerScript.breakIn <= 30) {
                sleep(10000,15000);
            }
        } else {
            state = MossKillerState.WALK_TO_BANK;
        }
        /// simple setup for going ge after doing the big bones
        if (Rs2Inventory.hasItemAmount(BIG_BONES,4)) {sleep(6000, 12000);
        walkToBank(GRAND_EXCHANGE);}
        if (Microbot.bryoFinished) return;
        if (Rs2Inventory.hasItemAmount(BIG_BONES, 4) && round2) {
            Microbot.bryoFinished = true; if(BreakHandlerScript.breakIn < 30 * 60) {BreakHandlerScript.breakIn = 30 * 60; Microbot.log("Made break 30 minutes");}
        } else {state = MossKillerState.WALK_TO_BANK;}

    }

    public void toggleRunEnergy(){
        if(Microbot.getClient().getEnergy() > 4000 && !Rs2Player.isRunEnabled()){
            Rs2Player.toggleRunEnergy(true);
        }
    }


    public void getInitiailState(){
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) < 10 || Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) < 10){
            state = MossKillerState.WALK_TO_BANK;
            return;
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) < 10 && !bossMode){
            state = MossKillerState.FIGHT_MOSS_GIANTS;
            return;
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) > 10 || Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) > 10){
            state = MossKillerState.WALK_TO_BANK;
            return;
        }


        Microbot.log("Must start near varrock square, bank, or moss giant spot.");
        state = MossKillerState.EXIT_SCRIPT;
    }

    public void init(){

        getInitiailState();

        if(!Rs2Combat.setAutoRetaliate(false)){
            Microbot.log("no need to turn off auto retaliate.");
            //state = MossKillerState.EXIT_SCRIPT;
        }

        isStarted = true;
    }
}
