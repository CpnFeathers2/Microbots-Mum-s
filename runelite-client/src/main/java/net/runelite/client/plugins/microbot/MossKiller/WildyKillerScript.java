package net.runelite.client.plugins.microbot.MossKiller;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.MossKiller.Enums.CombatMode;
import net.runelite.client.plugins.microbot.MossKiller.Enums.MossKillerState;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Pvp;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.walker.WalkerState;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.api.EquipmentInventorySlot.AMMO;
import static net.runelite.api.EquipmentInventorySlot.HEAD;
import static net.runelite.api.ItemID.*;
import static net.runelite.api.ItemID.BIG_BONES;
import static net.runelite.api.ItemID.RUNE_CHAINBODY;
import static net.runelite.api.ItemID.WILLOW_LONGBOW;
import static net.runelite.api.ObjectID.POOL_OF_REFRESHMENT;
import static net.runelite.api.Skill.*;
import static net.runelite.api.VarPlayer.ATTACK_STYLE;
import static net.runelite.api.gameval.ItemID.ADAMANT_ARROW;
import static net.runelite.api.gameval.ItemID.BARRONITE_MACE;
import static net.runelite.api.gameval.ItemID.BLACK_SQ_SHIELD;
import static net.runelite.api.gameval.ItemID.LONGBOW;
import static net.runelite.api.gameval.ItemID.MAPLE_LONGBOW;
import static net.runelite.api.gameval.ItemID.MAPLE_SHORTBOW;
import static net.runelite.api.gameval.ItemID.MITHRIL_SWORD;
import static net.runelite.api.gameval.ItemID.OAK_LONGBOW;
import static net.runelite.api.gameval.ItemID.OAK_SHORTBOW;
import static net.runelite.api.gameval.ItemID.RUNE_2H_SWORD;
import static net.runelite.api.gameval.ItemID.RUNE_BATTLEAXE;
import static net.runelite.api.gameval.ItemID.RUNE_MACE;
import static net.runelite.api.gameval.ItemID.RUNE_SCIMITAR;
import static net.runelite.api.gameval.ItemID.RUNE_SWORD;
import static net.runelite.api.gameval.ItemID.RUNE_WARHAMMER;
import static net.runelite.api.gameval.ItemID.SHORTBOW;
import static net.runelite.api.gameval.ItemID.STAFF_OF_AIR;
import static net.runelite.api.gameval.ItemID.STAFF_OF_EARTH;
import static net.runelite.api.gameval.ItemID.STAFF_OF_FIRE;
import static net.runelite.api.gameval.ItemID.STAFF_OF_WATER;
import static net.runelite.api.gameval.ItemID.STEEL_BAR;
import static net.runelite.api.gameval.ItemID.STEEL_KITESHIELD;
import static net.runelite.api.gameval.ItemID.UNCUT_DIAMOND;
import static net.runelite.api.gameval.ItemID.UNCUT_RUBY;
import static net.runelite.api.gameval.ItemID.WILLOW_SHORTBOW;
import static net.runelite.client.plugins.microbot.MossKiller.Enums.CombatMode.LURE;
import static net.runelite.client.plugins.microbot.globval.GlobalWidgetInfo.*;
import static net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity.LOW;
import static net.runelite.client.plugins.microbot.util.bank.Rs2Bank.depositEquipment;
import static net.runelite.client.plugins.microbot.util.magic.Rs2CombatSpells.FIRE_STRIKE;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.*;
import static net.runelite.client.plugins.microbot.util.player.Rs2Pvp.getWildernessLevelFrom;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.isPrayerActive;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.*;
import static net.runelite.client.plugins.skillcalculator.skills.MagicAction.HIGH_LEVEL_ALCHEMY;
import static net.runelite.client.plugins.skillcalculator.skills.MagicAction.WIND_BLAST;


public class WildyKillerScript extends Script {

    @Inject
    private Client client;

    @Inject
    ConfigManager configManager;

    @Inject
    private MossKillerPlugin mossKillerPlugin;

    @Inject
    private MossKillerScript mossKillerScript;

    @Inject
    private MossKillerConfig mossKillerConfig;

    @Inject
    private Rs2Pvp rs2Pvp;

    public static double version = 1.0;
    public static MossKillerConfig config;

    public boolean isStarted = false;
    public int playerCounter = 0;

    public final WorldPoint MOSS_GIANT_SPOT = new WorldPoint(3143, 3823, 0);
    public final WorldPoint VARROCK_SQUARE = new WorldPoint(3212, 3422, 0);
    public final WorldPoint VARROCK_WEST_BANK = new WorldPoint(3182, 3440, 0);
    public final WorldPoint TWENTY_WILD = new WorldPoint(3105, 3673, 0);
    public final WorldPoint ZERO_WILD = new WorldPoint(3106, 3523, 0);
    public final WorldPoint CASTLE_WARS = new WorldPoint(3142, 3473, 0);
    public final WorldPoint DWARFS = new WorldPoint(3210, 3797, 0);
    public static final WorldArea WEST_BARRIER_OUTSIDE = new WorldArea(3120, 3626, 3, 4, 0);
    public static final WorldArea NORTH_BARRIER_OUTSIDE = new WorldArea(3131, 3640, 6, 2, 0);
    public static final WorldArea MOSS_GIANT_AREA = new WorldArea(3122, 3752, 45, 91, 0);
    public static final WorldArea CORRIDOR = new WorldArea(3119, 3641, 25, 150, 0);
    public static final WorldArea TOTAL_FEROX_ENCLAVE = new WorldArea(3109, 3606, 56, 46, 0);
    public static final WorldArea FEROX_TELEPORT_AREA = new WorldArea(3147, 3631, 7, 7, 0);
    public static final WorldArea LUMBRIDGE_AREA = new WorldArea(3189, 3183, 63, 62, 0);
    public static final WorldArea CASTLE_WARS_AREA = new WorldArea(2433, 3076, 15, 25, 0);
    private static final WorldArea WILDERNESS_AREA = new WorldArea(2944, 3520, 448, 384, 0);
    public static final int COMBAT_TAB_WIDGET_ID = 35913791;  // Combat tab
    public static final int CHOOSE_SPELL_WIDGET_ID = 38862875; // Choose spell
    public static final int CHOOSE_SPELL_DEFENSIVE_WIDGET_ID = 38862870; // Choose spell
    public boolean hitsplatApplied = false;
    public boolean isTargetOutOfReach = false;
    private static final String[] WIZARD_HATS = {
            "Blue wizard hat",
            "Blue wizard hat (g)",
            "Blue wizard hat (t)",
            "Black wizard hat (g)",
            "Black wizard hat (t)"
    };



    // Items
    public final int AIR_RUNE = 556;
    public final int FIRE_RUNE = 554;
    public final int LAW_RUNE = 563;

    public int FOOD = 373;

    public int MOSSY_KEY = 22374;

    public int NATURE_RUNE = 561;
    public int DEATH_RUNE = 560;
    public int[] LOOT_LIST = new int[]{MOSSY_KEY, LAW_RUNE, AIR_RUNE, COSMIC_RUNE, STEEL_BAR, DEATH_RUNE, NATURE_RUNE, UNCUT_RUBY, UNCUT_DIAMOND, STEEL_KITESHIELD, MITHRIL_SWORD, BLACK_SQ_SHIELD};
    public int[] strengthPotionIds = {STRENGTH_POTION1, STRENGTH_POTION2, STRENGTH_POTION3, STRENGTH_POTION4}; // Replace ID1, ID2, etc., with the actual potion IDs.
    public int[] ALCHABLES = new int[]{STEEL_KITESHIELD, MITHRIL_SWORD, BLACK_SQ_SHIELD};


    boolean hasStrengthPotion = false;

    public MossKillerState state = MossKillerState.BANK;

    public void MossKillerScript(MossKillerConfig config) {
        WildyKillerScript.config = config;
    }

    public boolean run(MossKillerConfig config) {
        System.out.println("getting to run");
        WildyKillerScript.config = config;
        Microbot.enableAutoRunOn = false;
        Rs2Walker.disableTeleports = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.profileSwitching = true;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.moveMouseOffScreenChance = 0.04;
        Rs2AntibanSettings.moveMouseRandomly = true;
        Rs2AntibanSettings.moveMouseRandomlyChance = 0.04;
        Rs2AntibanSettings.actionCooldownChance = 0.06;
        Rs2Antiban.setActivityIntensity(LOW);
        Rs2AntibanSettings.dynamicActivity = true;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                boolean parentRunResult = super.run();
                if (!parentRunResult) {
                    Microbot.log("PARENT RUN RETURNED FALSE - script stopped");
                    return;
                }
                long startTime = System.currentTimeMillis();

                Microbot.log("SoL " + state);
                if (mossKillerPlugin.preparingForShutdown) {
                    MossKillerScript.prepareSoftStop();}
                Rs2AntibanSettings.antibanEnabled = mossKillerPlugin.currentTarget == null; // Enable Anti-Ban when no target is found
                Rs2AntibanSettings.naturalMouse = mossKillerPlugin.currentTarget == null;
                if (isRunning() && BreakHandlerScript.breakIn <= 120 && Rs2Player.getWorldLocation().getY() < 3520) {
                    Microbot.log("On a break and not in wilderness");
                    if (isRunning()) {
                        sleep(10000);
                        if (isRunning()) {
                            sleepUntil(() -> !Rs2Player.isInCombat());
                        }
                        if (isRunning()) {
                            Rs2Player.logout();
                        }
                        sleep(120000);
                        return;
                    }
                    return;
                } else if (isRunning() && BreakHandlerScript.breakIn <= 120 && Rs2Player.getWorldLocation().getY() > 3520) {
                    Microbot.log("On a break and in wilderness");
                    if (mossKillerPlugin.currentTarget != null) {
                        Microbot.log("Active target detected - postponing break by 10 minutes to finish fight");
                        BreakHandlerScript.breakIn += 600; // Add 10 minutes (600 seconds)
                        return; // Don't walk to bank
                    }
                    if (isRunning()) {
                        toggleRunEnergyOn();
                        Rs2Bank.walkToBank1();
                        sleep(60000);
                        if (isRunning()) {
                            Microbot.log("triggered a logout after supposedly a 60 second sleep");
                            Rs2Player.logout();
                        }
                        return;
                    }
                }


                if (mossKillerPlugin.shouldHopWorld()) {
                    Microbot.log("Player is dead! Hopping worlds...");
                    sleepUntil(() -> !Rs2Player.isInCombat());
                    sleep(4900);
                    performWorldHop();
                    sleep(2900);
                    mossKillerPlugin.resetWorldHopFlag(); // Reset after hopping
                }

                if (Rs2Player.getRealSkillLevel(DEFENCE) >= config.defenseLevel()) {
                    handleAsynchWalk("Twenty Wild");
                    sleepUntil(() -> (ShortestPathPlugin.pathfinder == null), 120000);
                    moarShutDown();
                }

                // CODE HERE
                switch (state) {
                    case BANK:
                        handleBanking();
                        break;
                    case TELEPORT:
                        varrockTeleport();
                        break;
                    case WALK_TO_BANK:
                        walkToVarrockWestBank();
                        break;
                    case CASTLE_WARS_TO_FEROX:
                        handleFerox();
                        break;
                    case WALK_TO_MOSS_GIANTS:
                        walkToMossGiants();
                        break;
                    case FIGHT_MOSS_GIANTS:
                        handleMossGiants();
                        break;
                    case PKER:
                        handlePker();
                        break;
                }
                if (mossKillerPlugin.getCurrentTarget() != null)
                    Microbot.log("Current target is " + mossKillerPlugin.getCurrentTarget().getName());
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);


            } catch (Exception ex) {
                Microbot.log("ERROR: Loop reset - " + ex.getClass().getSimpleName());
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public void moarShutDown() {
        varrockTeleport();
        //static sleep to wait till out of combat
        sleep(10000);
        //turn off breakhandler
        MossKillerScript.stopBreakHandlerPlugin();
        //turn off autologin and all other scripts in 5 seconds
        Microbot.getClientThread().runOnSeperateThread(() -> {
            if (!Microbot.pauseAllScripts) {
                sleep(5000);
                Microbot.pauseAllScripts = true;
            }
            return null;
        });
        Rs2Player.logout();
        sleep(1000);
        shutdown();
    }

    public void handleAsynchWalk(String walkName) {
        // 1. Capture the caller info immediately
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Use index 2 to get the caller of this method
        StackTraceElement caller = stackTrace[2];
        String callSource = caller.getFileName() + ":" + caller.getLineNumber();

        // Print immediately so you know the request was made
        System.out.println("handleAsynchWalk('" + walkName + "') triggered by " + callSource);

        scheduledFuture = scheduledExecutorService.schedule(() -> {
            try {
                // Pass the source into the log so you can trace execution flow
                Microbot.log("Entered Asynch Walking Thread. Request from: " + callSource);
                WorldPoint playerLocation = Rs2Player.getLocalPlayer().getWorldLocation();

                if (playerLocation.getY() > 3520) {
                    switch (walkName) {
                        case "Twenty Wild":
                            Rs2Walker.walkTo(TWENTY_WILD);
                            if (mossKillerPlugin.playerJammed()) {
                                Microbot.log("restarting path");
                                Rs2Walker.setTarget(null);
                                Rs2Walker.walkTo(TWENTY_WILD);
                            }
                            if (Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), TWENTY_WILD) <= 10
                                    && Rs2Player.getWorldLocation().getY() <= 3679) {
                                Microbot.log("Reached TWENTY_WILD.");
                                sleep(1000);
                                if (isTeleBlocked()) {
                                    if(!Rs2Player.isInCombat()) {Rs2Player.logout();}
                                    handleAsynchWalk("Zero Wild");
                                }
                                teleportAndStopWalking();
                            } else {Rs2Walker.walkFastCanvas(TWENTY_WILD);}
                            break;
                        case "Zero Wild":
                            if (isTeleBlocked()) {
                                if(!Rs2Player.isInCombat()) {Rs2Player.logout();}
                                Rs2Walker.walkTo(ZERO_WILD);
                                if (mossKillerPlugin.playerJammed()) {
                                    Microbot.log("restarting path");
                                    Rs2Walker.setTarget(null);
                                    Rs2Walker.walkTo(ZERO_WILD);
                                }
                            } else {
                                handleAsynchWalk("Twenty Wild");
                            }
                            if (Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), ZERO_WILD) <= 5) {
                                Microbot.log("Reached ZERO_WILD.");
                                teleportAndStopWalking();
                            }
                            break;
                        case "Moss Giants":
                            if (!Rs2Player.isInMulti() && mossKillerPlugin.currentTarget == null) {
                                Rs2Walker.walkTo(MOSS_GIANT_SPOT);
                                if (mossKillerPlugin.playerJammed()) {
                                    Microbot.log("restarting path");
                                    Rs2Walker.setTarget(null);
                                    Rs2Walker.walkTo(MOSS_GIANT_SPOT);
                                }
                            } else if (Rs2Player.isInMulti() && mossKillerPlugin.currentTarget != null) {
                                sleepUntil(() -> Rs2Walker.walkWithState(MOSS_GIANT_SPOT) == WalkerState.ARRIVED, 12500);
                            }
                            break;
                        case "Dwarfs":
                            Rs2Walker.walkTo(DWARFS);
                            break;
                        case "Start-up":
                            System.out.println("starting up the scheduled future");
                            break;
                    }
                }
                Microbot.log("Exiting Asynch Walking Thread");
            } catch (Exception ex) {
                System.out.println("Error caused by call from " + callSource + ": " + ex.getMessage());
            }
        }, 600, TimeUnit.MILLISECONDS);
    }


    public void performWorldHop() {
        int maxRetries = 5; // Maximum number of attempts to hop worlds
        int retries = 0;
        boolean hopSuccessful = false;

        while (retries < maxRetries && !hopSuccessful) {
            int world;
            do {
                world = Login.getRandomWorld(false, null); // Get a random world
            } while (world == 301 || world == 308 || world == 316); // Skip restricted worlds

            int targetWorld = world;

            // Attempt to hop to the target world
            Microbot.hopToWorld(world);

            // Wait to verify the world hop succeeded
            hopSuccessful = sleepUntil(() -> Rs2Player.getWorld() == targetWorld, 10000);

            if (!hopSuccessful) {
                retries++;
                System.out.println("World hop failed, retrying... (" + retries + "/" + maxRetries + ")");
            }
        }

        if (!hopSuccessful) {
            System.out.println("Failed to hop worlds after " + maxRetries + " attempts.");
        } else {
            System.out.println("World hop successful to world: " + Rs2Player.getWorld());
        }
    }

    private static final int[] MELEE_WEAPONS = {
            RUNE_SCIMITAR,
            RUNE_WARHAMMER,
            RUNE_MACE,
            GILDED_SCIMITAR,
            BARRONITE_MACE,
            RUNE_LONGSWORD,
            RUNE_SWORD,
            RUNE_2H_SWORD,
            RUNE_BATTLEAXE,
            RUNE_AXE
    };


    @Override
    public void shutdown() {
        super.shutdown();
    }

    private int getRandomZoom(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public void handlePker() {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        int currentZoom = Rs2Camera.getZoom(); // Assume Rs2Camera.getZoom() retrieves the current zoom level.

        if (mossKillerPlugin.currentTarget != null) {
            // Check if the zoom is outside the 340-400 range
            if (currentZoom < 340 || currentZoom > 400) {
                int zoomValue = getRandomZoom(340, 400);
                Rs2Camera.setZoom(zoomValue);
            }
        } else {
            // Check if the zoom is outside the 230-290 range
            if (currentZoom < 230 || currentZoom > 290) {
                int zoomValue = getRandomZoom(230, 290);
                Rs2Camera.setZoom(zoomValue);
            }
        }

        int currentPitch = Rs2Camera.getPitch(); // Assume Rs2Camera.getPitch() retrieves the current pitch value.

        // Ensure the pitch is within the desired range
        if (currentPitch < 150 || currentPitch > 200) {
            int pitchValue = Rs2Random.between(150, 200); // Random value within the range
            Rs2Camera.setPitch(pitchValue); // Adjust the pitch
        }


        if (mossKillerPlugin.currentTarget == null) Rs2Player.eatAt(70, false);

        if (mossKillerPlugin.currentTarget == null) {
            // Get the actor we're interacting with
            Actor interactingActor = Rs2Player.getInteracting();

            // Check if it's a player (not an NPC)
            if (interactingActor instanceof Player) {
                // If it's a player, we need to break the interaction
                WorldPoint myTile = getWorldLocation();
                Rs2Walker.walkFastCanvas(myTile);
                sleep(600);
            }
        }


        if (mossKillerPlugin.currentTarget == null && MOSS_GIANT_AREA.contains(playerLocation)) {
            if (!Rs2Inventory.contains(MOSSY_KEY)) {
                state = MossKillerState.FIGHT_MOSS_GIANTS;
            }
        }

        if (mossKillerPlugin.currentTarget == null && playerLocation.getY() < 3520) {
            Microbot.log("not in wilderness and no target, teleport reset");
            state = MossKillerState.TELEPORT;
        }

        if (mossKillerPlugin.currentTarget == null && playerLocation.getY() > 3820) {
            Microbot.log("you've left the moss giant area and you've got no target");
            state = MossKillerState.TELEPORT;
        }

        if (mossKillerPlugin.currentTarget != null
                && mossKillerPlugin.currentTarget.getCombatLevel() < 88
                && getWildernessLevelFrom(Rs2Player.getWorldLocation()) < 25) {
            if (Rs2Pvp.getWildernessLevelFrom(mossKillerPlugin.currentTarget.getWorldLocation()) == 0) {
                handleAsynchWalk("Twenty Wild");
                eatAt(70, false);
                sleep(1800);
                eatAt(70, false);
                sleep(1800);
                eatAt(70, false);
                sleep(1800);
                eatAt(70, false);
                sleep(1800);
            }

        }

        if (!scheduledFuture.isDone()) {
            if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                Microbot.log("equipping staff of fire due to handlepker method");
                Rs2Inventory.equip(STAFF_OF_FIRE);
            }
        }

        if (mossKillerPlugin.currentTarget != null
                && mossKillerPlugin.currentTarget.getCombatLevel() > 87
                && getWildernessLevelFrom(Rs2Player.getWorldLocation()) > 20) {
            Microbot.log("first instance of a higher level pker in handlePker method");
            toggle(STEEL_SKIN, true);
            toggle(MYSTIC_LORE, true);
            boolean targetHasMelee = false;

            for (int weapon : MELEE_WEAPONS) {
                if (hasPlayerEquippedItem(mossKillerPlugin.currentTarget, weapon)) {
                    targetHasMelee = true;
                    break;
                }
            }

            if (targetHasMelee
                    && Microbot.getClient().getRealSkillLevel(PRAYER) > 42
                    && Microbot.getClient().getBoostedSkillLevel(PRAYER) > 0
                    && !Rs2Prayer.isPrayerActive(PROTECT_MELEE)) {

                toggle(PROTECT_MELEE);
            }
            if (hasPlayerEquippedItem(mossKillerPlugin.currentTarget, MAPLE_SHORTBOW)
                    && Microbot.getClient().getRealSkillLevel(PRAYER) > 39
                    && Microbot.getClient().getBoostedSkillLevel(PRAYER) > 0
                    && !Rs2Prayer.isPrayerActive(PROTECT_RANGE)){
                toggle(PROTECT_RANGE);
            }
            if (Microbot.getClient().getBoostedSkillLevel(PRAYER) > 0
                    && !Rs2Prayer.isPrayerActive(PROTECT_MAGIC)){
                if (Rs2Player.getRealSkillLevel(PRAYER) > 36) {
                    Rs2Prayer.toggle(PROTECT_MAGIC, hasPlayerEquippedItem(mossKillerPlugin.currentTarget, STAFF_OF_FIRE)
                            || hasPlayerEquippedItem(mossKillerPlugin.currentTarget, STAFF_OF_AIR)
                            || hasPlayerEquippedItem(mossKillerPlugin.currentTarget, STAFF_OF_WATER)
                            || hasPlayerEquippedItem(mossKillerPlugin.currentTarget, STAFF_OF_EARTH)
                            || hasPlayerEquippedItem(mossKillerPlugin.currentTarget, BRYOPHYTAS_STAFF)
                            || hasPlayerEquippedItem(mossKillerPlugin.currentTarget, BRYOPHYTAS_STAFF_UNCHARGED));
                }
            }
            if (ShortestPathPlugin.getPathfinder() == null && !MossKillerPlugin.isPlayerSnared()) {
                handleAsynchWalk("Twenty Wild");
            }
        }

        if (mossKillerConfig.combatMode() != LURE) {
            if (Rs2Player.isInMulti()) {
                Microbot.log("In multi");
                Microbot.log("scheduledFuture" + scheduledFuture.isDone());
                if(ShortestPathPlugin.getPathfinder() == null) {Microbot.log("shortestpath is null");}
                if(ShortestPathPlugin.getPathfinder() != null) {
                        Microbot.log("shortest path is not null");}
            }
                    if (scheduledFuture.isDone()
                    && ShortestPathPlugin.getPathfinder() == null) {

                if (Rs2Inventory.hasItemAmount(FOOD, 14)) {handleAsynchWalk("Moss Giants");}
            }

        } else if (Rs2Player.isInMulti() && scheduledFuture.isDone() && mossKillerConfig.combatMode() == LURE) {
            handleAsynchWalk("Dwarfs");
        }

        if (mossKillerPlugin.currentTarget == null && Rs2Inventory.contains(MIND_RUNE) && TOTAL_FEROX_ENCLAVE.contains(playerLocation)) {
            if (!Rs2Equipment.isWearing(STAFF_OF_FIRE) && Rs2Inventory.hasItem(STAFF_OF_FIRE)) {
                Rs2Inventory.equip(STAFF_OF_FIRE);
            }

        }

        // Ensure mossKillerConfig is not null
        if (mossKillerConfig == null) {
            Microbot.log("Configuration is not initialized!, returning");
            return;
        }

        CombatMode mode = mossKillerConfig.combatMode();

        switch (mode) {
            case FLEE:
                state = MossKillerState.TELEPORT;
                break;
            case FIGHT:
                fight();
                break;
            case LURE:
                lure();
                break;
        }


        //if config is flee do this
        //if frozen, cast snare
        //teleport when arrived at 20 wild
        //(if tbed, continue running south to 0 wild (how to check if tbed?))

        //else if config is lure do this
        //run to multi or world position X (inputted by user)
        //send discord information either way(s)

    }

    private void lure() {
        if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW) && Rs2Equipment.isWearing(ADAMANT_ARROW)) {
            Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");
        }
        Rs2Player.eatAt(70);
        //if snared, attack (include long-range logic
        //if not snared attack every 6 ticks
        //equip bow for highest dps at range
        //eating calculation to know when to attack them for a hit
        //Rs2Walker.walkTo("dwarfs asynch");
        //if near dwarfs ???
        //use long range weapons and stay in the dwarf area (dont be lured out)
    }

    private void fight() {

        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        Microbot.log("FIGHT CASE");

        Rs2PlayerModel target = mossKillerPlugin.currentTarget;

        if (CORRIDOR.contains(playerLocation) && target != null
                && target.getCombatLevel() < 88
                && Rs2Inventory.hasItemAmount(MIND_RUNE, 750)) {
            Rs2Walker.setTarget(null);
            scheduledFuture.cancel(true);
        }

        eatingMethod(target);

        //if we have a target and we're autocasting
        if (target != null
                && mossKillerPlugin.getAttackStyle()
                && target.getCombatLevel() < 88) {
            if (weHaveEnoughEnergyToPersue() && !MossKillerPlugin.isPlayerSnared() || !isTargetPlayerFar(target)) {
                // 1. Ensure we are holding the weapon
if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
    Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
    sleep(200); // Small sleep to let the game register the switch
}

// 2. SEPARATE CHECK: Ensure we are using the right style
// We check this every time, regardless of whether we just equipped it or already had it.
if (Rs2Equipment.isWearing(BRYOPHYTAS_STAFF) && !mossKillerPlugin.isAttackStyleStrength()) {
    Microbot.log("Fixing Combat Style to Strength");
    Rs2Combat.setAttackStyle(COMBAT_STYLE_TWO);
}
            } else if (isTargetPlayerFarCasting(target)) {
                if (!castWindBlast(target)) {
                    if (Rs2Equipment.isWearing(AMMO)
                            || Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                        if (Rs2Inventory.contains(MAPLE_LONGBOW)) {
                            // 1. Equip the Bow
                            if (!Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                Rs2Inventory.interact(MAPLE_LONGBOW, "Wield");

                                sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_LONGBOW), 1200);

                            }
                            if (Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                // Now safe to check styles because we know we hold the bow

                                setCombatStyle(target);

                            }
                        }
                    }
                }
            } else// 1. Equip the Bow
                if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                    Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                    // WAIT for the switch to actually happen
                    sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                }

            // 2. Only THEN check the style
            if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                // Now safe to check styles because we know we hold the bow
                setCombatStyle(target);
            }
        }


        if (target != null && Rs2Player.getRunEnergy() < 20) {
            Rs2Inventory.useRestoreEnergyItem();
        }

        if (target != null && scheduledFuture.isDone() && isStrengthPotionPresent() && target.getCombatLevel() < 88) {
            checkAndDrinkStrengthPotion();
        }

        if (target != null) {
            basicAttackSetup();
        }

        if (mossKillerPlugin.hasRuneScimitar()
                && target != null
                && target.getCombatLevel() < 88) {
            if (MossKillerPlugin.isPlayerSnared() && isTargetPlayerFar(target) || mossKillerPlugin.lobsterEaten()) {
                ultimateStrengthOff();
            } else {
                ultimateStrengthOn();
            }
        } else if (Rs2Prayer.isPrayerActive(ULTIMATE_STRENGTH)) {
            ultimateStrengthOff();
        }

        if (target != null && !mossKillerPlugin.lobsterEaten() && ShortestPathPlugin.getPathfinder() == null
                && target.getCombatLevel() < 88 && Rs2Player.getInteracting() != target) {
            basicAttackSetup();
            isTargetOnSameTile(target);
            Rs2Walker.setTarget(null);
            scheduledFuture.cancel(true);
            if(!Rs2Player.isInteracting()) {attack(target);}
            sleepUntil(() -> hitsplatApplied || MossKillerPlugin.isPlayerSnared() || healthIsLow());
            eatingMethod(target);
        }

        //if no attack delay from eating and not interacting with target, attack target
        if (target != null && target.getCombatLevel() < 88) {
            if (!mossKillerPlugin.lobsterEaten()
                    && Rs2Player.getInteracting() != target
                    && getPlayersInCombatLevelRange().stream()
                    .anyMatch(p -> p.getId() == target.getId())
                    && !TOTAL_FEROX_ENCLAVE.contains(playerLocation)) {
                if (!Rs2Player.isInMulti() && !isNpcInteractingWithMe()) {
                    if (ShortestPathPlugin.getPathfinder() == null)
                        if (doWeFocusCamera(target)) {
                            sleep(300);
                        }
                    basicAttackSetup();
                    Rs2Walker.setTarget(null);
                    scheduledFuture.cancel(true);
                    if(!Rs2Player.isInteracting()) {attack(target);}
                    sleepUntil(() -> hitsplatApplied || MossKillerPlugin.isPlayerSnared() || healthIsLow());
                    eatingMethod(target);

                }
            }
        }

        if (!Rs2Inventory.contains(FOOD) && target != null && Microbot.getClient().getBoostedSkillLevel(PRAYER) >= 1) {
            toggle(Rs2PrayerEnum.PROTECT_ITEM, true);
        }

        //if you're running away from target, and you're snared or without food, fight back a bit you coward
        if (target != null && ShortestPathPlugin.getPathfinder() != null && target.getCombatLevel() > 87) {
            if (MossKillerPlugin.isPlayerSnared() || !Rs2Inventory.contains(FOOD)) {

                if (!isTargetPlayerFar(target) && mossKillerPlugin.lobsterEaten()) {
                      // 1. Ensure we are holding the weapon
if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
    Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
    sleep(200); // Small sleep to let the game register the switch
}

// 2. SEPARATE CHECK: Ensure we are using the right style
// We check this every time, regardless of whether we just equipped it or already had it.
if (Rs2Equipment.isWearing(BRYOPHYTAS_STAFF) && !mossKillerPlugin.isAttackStyleStrength()) {
    Microbot.log("Fixing Combat Style to Strength");
    Rs2Combat.setAttackStyle(COMBAT_STYLE_TWO);
}                    Rs2Walker.setTarget(null);
                    scheduledFuture.cancel(true);
                    if(!Rs2Player.isInteracting()) {attack(target);}
                    sleepUntil(() -> hitsplatApplied || MossKillerPlugin.isPlayerSnared());
                    eatingMethod(target);
                } else if (!castWindBlast(target)) {
                    if (Rs2Equipment.isWearing(AMMO)
                            || Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                        if (Rs2Inventory.contains(MAPLE_SHORTBOW) && mossKillerPlugin.lobsterEaten()) {
                            // 1. Equip the Bow
                            if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                                // WAIT for the switch to actually happen
                                sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                            }

// 2. Only THEN check the style
                            if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                // Now safe to check styles because we know we hold the bow
                                setCombatStyle(target);
                            }
                            Rs2Walker.setTarget(null);
                            scheduledFuture.cancel(true);
                            if(!Rs2Player.isInteracting()) {attack(target);}
                            sleepUntil(() -> hitsplatApplied || MossKillerPlugin.isPlayerSnared() || healthIsLow());
                            eatingMethod(target);
                        }
                    }
                }

            }
        }

        if (target != null
                && getWildernessLevelFrom(Rs2Player.getWorldLocation()) < 25
                && ShortestPathPlugin.getPathfinder() == null
                && !MossKillerPlugin.isPlayerSnared()
                && target.getCombatLevel() > 87) {
            Microbot.log("less than 25 wild");
            if (!Rs2Player.isTeleBlocked()) {
                handleAsynchWalk("Twenty Wild");
            } else {
                handleAsynchWalk("Zero Wild");
            }
        }

        if (target != null && target.getCombatLevel() > 87
                && getWildernessLevelFrom(Rs2Player.getWorldLocation()) > 20) {
            Microbot.log("Target is over level 87");
            eatingMethod(target);
            if (ShortestPathPlugin.getPathfinder() == null && !MossKillerPlugin.isPlayerSnared()) {
                handleAsynchWalk("Twenty Wild");
            }
            state = MossKillerState.TELEPORT;
        }

        if (target == null && ShortestPathPlugin.getPathfinder() == null && mossKillerPlugin.isSuperNullTarget()) {
            state = MossKillerState.TELEPORT;
        }

        if (target == null &&
                ShortestPathPlugin.getPathfinder() != null
                && mossKillerPlugin.isSuperNullTarget() &&
                mossKillerPlugin.playerJammed()) {
            Rs2Walker.setTarget(null);
            scheduledFuture.cancel(true);
            state = MossKillerState.TELEPORT;
        }

        if (target != null && !Rs2Player.isTeleBlocked() && playerLocation.getY() < 3675) {
            if (Rs2Inventory.contains(STAFF_OF_FIRE)) {
                Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");
                if (Rs2Magic.canCast(MagicAction.VARROCK_TELEPORT)) {
                    Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);
                    sleep(1200);
                }
            }
        }

    }

    public void basicAttackSetup() {
        // --- DIAGNOSTIC & VIDEO SYNC BLOCK START ---
        // 1. Define main variables early for logging

        Microbot.log("Entering basicAttackSetup");

        Player localPlayer = Microbot.getClient().getLocalPlayer();
        Rs2PlayerModel target = mossKillerPlugin.currentTarget;

        // 2. Generate a unique ID for this specific execution tick
        long frameID = System.currentTimeMillis();
        String timeString = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new java.util.Date(frameID));

        // 3. Gather key metrics safely (handling null targets)
        String tName = (target != null) ? target.getName() : "NULL";
        int tCmb = (target != null) ? target.getCombatLevel() : -1;
        HeadIcon tOverhead = (target != null) ? target.getOverheadIcon() : null; // Prints 'PROTECT_FROM_MAGIC', etc.
        int dist = (target != null) ? Rs2Walker.getDistanceBetween(localPlayer.getWorldLocation(), target.getWorldLocation()) : -1;

        // 4. Capture state flags
        boolean isSnared = MossKillerPlugin.isPlayerSnared();
        boolean useMage = mossKillerPlugin.useWindBlast();
        boolean useMelee = mossKillerPlugin.useMelee();
        boolean useRange = mossKillerPlugin.useRange();

        // 5. Construct the Diagnostic String
        String diagnosticMsg = String.format(
                "ID:%d | Time:%s | Tgt:%s(Lvl:%d) | OvH:%s | Dist:%d | Snared:%b | Mode:[Mg:%b, Me:%b, Rg:%b]",
                frameID, timeString, tName, tCmb, tOverhead, dist, isSnared, useMage, useMelee, useRange
        );

        // 6. Output to both locations
        System.out.println("BAS_LOG: " + diagnosticMsg);
        Microbot.log("Frame ID: " + frameID + " Info: " + diagnosticMsg);
        // --- DIAGNOSTIC BLOCK END ---

        Microbot.log("Entering basicAttackSetup");
        Microbot.log("Entering basicAttackSetup");

        // If you're interacting with an NPC, stop and attack target
        Actor currentInteracting = Rs2Player.getInteracting();
        if (currentInteracting instanceof NPC) {
            Microbot.log("Currently attacking NPC, switching to player target");

            if (mossKillerPlugin.currentTarget != null) {
                Microbot.log("Attacking target: " + mossKillerPlugin.currentTarget.getName());
                Rs2Player.attack(mossKillerPlugin.currentTarget);
                sleep(600); // Wait for attack to register
            }
        }

        // (Original code continues below...)
        if (!isInMulti()) {
            if (Rs2Player.getRealSkillLevel(PRAYER) > 39) {
                Rs2Prayer.toggle(PROTECT_RANGE, hasPlayerEquippedItem(target, MAPLE_SHORTBOW)
                        || hasPlayerEquippedItem(target, WILLOW_SHORTBOW)
                        || hasPlayerEquippedItem(target, OAK_SHORTBOW)
                        || hasPlayerEquippedItem(target, MAPLE_LONGBOW)
                        || hasPlayerEquippedItem(target, WILLOW_LONGBOW)
                        || hasPlayerEquippedItem(target, LONGBOW)
                        || hasPlayerEquippedItem(target, SHORTBOW)
                        || hasPlayerEquippedItem(target, OAK_LONGBOW));
            }

            if (Rs2Player.getRealSkillLevel(PRAYER) > 36) {
                Rs2Prayer.toggle(PROTECT_MAGIC, hasPlayerEquippedItem(target, STAFF_OF_FIRE)
                        || hasPlayerEquippedItem(target, STAFF_OF_AIR)
                        || hasPlayerEquippedItem(target, STAFF_OF_WATER)
                        || hasPlayerEquippedItem(target, STAFF_OF_EARTH)
                        || hasPlayerEquippedItem(target, BRYOPHYTAS_STAFF)
                        || hasPlayerEquippedItem(target, BRYOPHYTAS_STAFF_UNCHARGED));
            }

            if (Rs2Player.getRealSkillLevel(PRAYER) > 42) {
                Rs2Prayer.toggle(PROTECT_MELEE, hasPlayerEquippedItem(target, RUNE_SCIMITAR)
                        || hasPlayerEquippedItem(target, RUNE_WARHAMMER)
                        || hasPlayerEquippedItem(target, RUNE_2H_SWORD)
                        || hasPlayerEquippedItem(target, RUNE_BATTLEAXE)
                        || hasPlayerEquippedItem(target, RUNE_SWORD)
                        || hasPlayerEquippedItem(target, BARRONITE_MACE)
                        || hasPlayerEquippedItem(target, RUNE_MACE));
            }
        } else if (isInMulti()) {
            monitorAttacks();
        }


        if (target != null
                && target.getCombatLevel() < 88
                && target.getOverheadIcon() == null
                && !MossKillerPlugin.isPlayerSnared()) {
            Microbot.log("target is not null, less than 88 combat, no overheads, and we're not snared");

            if (EquipmentIdentifier.isWearingRuneArmor(target) && Rs2Inventory.contains(DEATH_RUNE)) {
                castWindBlast(target);
                sleep(600);
            }

            if (weHaveEnoughEnergyToPersue()
                    || !isTargetPlayerFar(target)) {
                  // 1. Ensure we are holding the weapon
if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
    Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
    sleep(200); // Small sleep to let the game register the switch
}

// 2. SEPARATE CHECK: Ensure we are using the right style
// We check this every time, regardless of whether we just equipped it or already had it.
if (Rs2Equipment.isWearing(BRYOPHYTAS_STAFF) && !mossKillerPlugin.isAttackStyleStrength()) {
    Microbot.log("Fixing Combat Style to Strength");
    Rs2Combat.setAttackStyle(COMBAT_STYLE_TWO);
}
                Rs2Walker.setTarget(null);
            } else if (!castWindBlast(target) && !isTargetPlayerFarCasting(target)) {
                if (Rs2Equipment.isWearing(AMMO)
                        || Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                    if (Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                        // 1. Equip the Bow
                        if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                            Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                            // WAIT for the switch to actually happen
                            sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                        }

// 2. Only THEN check the style
                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                            // Now safe to check styles because we know we hold the bow
                            setCombatStyle(target);
                        }
                    }
                }
            } else if (isTargetPlayerFarCasting(target) && !Rs2Inventory.contains(DEATH_RUNE)) {
                if (Rs2Inventory.contains(MAPLE_LONGBOW)) {
                    if (!Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                        Rs2Inventory.interact(MAPLE_LONGBOW, "Wield");

                        sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_LONGBOW), 1200);

                    }
                    if (Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                        // Now safe to check styles because we know we hold the bow

                        setCombatStyle(target);

                    }
                }
            }

        }

        if (target != null
                && target.getCombatLevel() < 88
                && target.getOverheadIcon() == null
                && MossKillerPlugin.isPlayerSnared()) {
            Microbot.log("target is not null, less than 88 combat, no overheads and we are snared");

            if (!isTargetPlayerFar(target)) {
                  // 1. Ensure we are holding the weapon
if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
    Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
    sleep(200); // Small sleep to let the game register the switch
}

// 2. SEPARATE CHECK: Ensure we are using the right style
// We check this every time, regardless of whether we just equipped it or already had it.
if (Rs2Equipment.isWearing(BRYOPHYTAS_STAFF) && !mossKillerPlugin.isAttackStyleStrength()) {
    Microbot.log("Fixing Combat Style to Strength");
    Rs2Combat.setAttackStyle(COMBAT_STYLE_TWO);
}
                Rs2Walker.setTarget(null);
            } else if (!castWindBlast(target) && !isTargetPlayerFarCasting(target)) {
                if (Rs2Equipment.isWearing(AMMO)
                        || Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                    if (Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                        // 1. Equip the Bow
                        if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                            Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                            // WAIT for the switch to actually happen
                            sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                        }

// 2. Only THEN check the style
                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                            // Now safe to check styles because we know we hold the bow
                            setCombatStyle(target);
                        }
                    }
                }
            } else if (isTargetPlayerFarCasting(target) && !Rs2Inventory.contains(DEATH_RUNE)) {
                if (Rs2Inventory.contains(MAPLE_LONGBOW)) {
                    if (!Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                        Rs2Inventory.interact(MAPLE_LONGBOW, "Wield");

                        sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_LONGBOW), 1200);

                    }
                    if (Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                        // Now safe to check styles because we know we hold the bow

                        setCombatStyle(target);

                    }
                }
            }

        }

        // Check if target is valid, has overheads, and is low level
        if (target != null && target.getOverheadIcon() != null && target.getCombatLevel() < 88) {
            Microbot.log("target is not null, he is praying and is below level 88");

            // =================================================================
            // START OF SAFETY WRAPPER
            // We wrap this whole section so if ONE line fails, the bot doesn't "Zombie" out.
            // =================================================================
            try {

                if (useMelee && weHaveEnoughEnergyToPersue()) {
                    // 1. Ensure we are holding the weapon
                    if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
                        Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
                        sleep(200);
                    }

                    // 2. SEPARATE CHECK: Ensure we are using the right style
                    if (Rs2Equipment.isWearing(BRYOPHYTAS_STAFF) && !mossKillerPlugin.isAttackStyleStrength()) {
                        Microbot.log("Fixing Combat Style to Strength");
                        Rs2Combat.setAttackStyle(COMBAT_STYLE_TWO);
                    }

                    Rs2Walker.setTarget(null);

                    // --- FIX: Replaced expensive 'contains' check with simple distance/interaction check ---
                    if (localPlayer.getInteracting() != target &&
                            target.getWorldLocation().distanceTo(localPlayer.getWorldLocation()) < 15 &&
                            !mossKillerPlugin.lobsterEaten()) {

                        if (ShortestPathPlugin.getPathfinder() == null && Rs2Player.getInteracting() != target) {
                            Rs2Walker.setTarget(null);
                            scheduledFuture.cancel(true);
                            if(!Rs2Player.isInteracting()) {attack(target);}

                            // Added lambda safety for the sleep
                            sleepUntil(() -> {
                                try { return hitsplatApplied || MossKillerPlugin.isPlayerSnared() || healthIsLow(); }
                                catch (Exception e) { return false; }
                            });

                            eatingMethod(target);
                        }
                    }
                } else if (useMelee && !weHaveEnoughEnergyToPersue() && isTargetPlayerFar(target)) {
                    Microbot.log("we are to use melee, with not enough energy and enemy is far");
                    if (Rs2Prayer.isPrayerActive(ULTIMATE_STRENGTH)) {
                        ultimateStrengthOff();
                    }

                    if (doWeFocusCamera(target)) {
                        sleep(300);
                    }

                    if (!castWindBlastOverhead(target)) {
                        if (isTargetPlayerFar(target)) {
                            if (Rs2Equipment.isWearing(AMMO) || Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                                if (!isTargetPlayerFarCasting(target)) {
                                    if (Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                                        // 1. Equip the Bow
                                        if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                                            // WAIT for the switch to actually happen
                                            sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                                        }

// 2. Only THEN check the style
                                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            // Now safe to check styles because we know we hold the bow
                                            setCombatStyle(target);
                                        }
                                        eatingMethod(target);
                                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            // Now safe to check styles because we know we hold the bow
                                            setCombatStyle(target);
                                        }
                                        eatingMethod(target);
                                    }
                                } else if (isTargetPlayerFarCasting(target)) {
                                    if (Rs2Inventory.contains(MAPLE_LONGBOW)) {
                                        if (!Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                            Rs2Inventory.interact(MAPLE_LONGBOW, "Wield");

                                            sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_LONGBOW), 1200);

                                        }

                                        eatingMethod(target);
                                        if (Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                            // Now safe to check styles because we know we hold the bow

                                            setCombatStyle(target);

                                        }
                                        eatingMethod(target);
                                    }
                                }
                            }
                        }
                    }
                }

                if (useRange && Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                    if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW) && Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                        // 1. Equip the Bow
                        if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                            Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                            // WAIT for the switch to actually happen
                            sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                        }
                        eatingMethod(target);
                        if (scheduledFuture.isDone()) setCombatStyle(target);
                        eatingMethod(target);
                    }

                    // --- FIX: Replaced expensive 'contains' check here too ---
                    if (localPlayer.getInteracting() != target &&
                            target.getWorldLocation().distanceTo(localPlayer.getWorldLocation()) < 18 &&
                            !mossKillerPlugin.lobsterEaten()) {

                        if (ShortestPathPlugin.getPathfinder() == null && Rs2Player.getInteracting() != target)
                            if (doWeFocusCamera(target)) {
                                sleep(300);
                            }
                        Rs2Walker.setTarget(null);
                        scheduledFuture.cancel(true);
                        if(!Rs2Player.isInteracting()) {attack(target);}

                        // Added lambda safety
                        sleepUntil(() -> {
                            try { return hitsplatApplied || MossKillerPlugin.isPlayerSnared() || healthIsLow(); }
                            catch (Exception e) { return false; }
                        });

                        eatingMethod(target);
                    }
                } else if (useRange && !Rs2Equipment.isWearing(ADAMANT_ARROW)) {castWindBlast(target);}

                if (useMage && MossKillerPlugin.isPlayerSnared()) {
                    if (doWeFocusCamera(target)) {
                        sleep(300);
                    }
                    if (!castWindBlastOverhead(target)) {
                        if (isTargetPlayerFar(target)) {
                            if (Rs2Equipment.isWearing(AMMO) || Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                                if (!isTargetPlayerFarCasting(target)) {
                                    if (Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                                        Microbot.log("About to wield Maple Shortbow");
                                        // 1. Equip the Bow
                                        if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                                            // WAIT for the switch to actually happen
                                            sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                                        }

                                        // 2. Only THEN check the style
                                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            // Now safe to check styles because we know we hold the bow
                                            setCombatStyle(target);
                                        }
                                        Microbot.log("Wielded Maple Shortbow, calling eatingMethod");
                                        eatingMethod(target);
                                        Microbot.log("eatingMethod done, calling setCombatStyle");
                                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            // Now safe to check styles because we know we hold the bow
                                            setCombatStyle(target);
                                        }
                                        Microbot.log("setCombatStyle done, calling eatingMethod again");
                                        eatingMethod(target);
                                        Microbot.log("Final eatingMethod done");
                                    }
                                } else if (isTargetPlayerFarCasting(target)) {
                                    if (Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                                        Microbot.log("About to wield Maple Shortbow");
                                        // 1. Equip the Bow
                                        if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                                            // WAIT for the switch to actually happen
                                            sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                                        }

                                        // 2. Only THEN check the style
                                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            // Now safe to check styles because we know we hold the bow
                                            setCombatStyle(target);
                                        }
                                        Microbot.log("Wielded Maple Shortbow, calling eatingMethod");
                                        eatingMethod(target);
                                        Microbot.log("eatingMethod done, calling setCombatStyle");
                                        // 2. Only THEN check the style
                                        if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                            // Now safe to check styles because we know we hold the bow
                                            setCombatStyle(target);
                                        }
                                        Microbot.log("setCombatStyle done, calling eatingMethod again");
                                        eatingMethod(target);
                                        Microbot.log("Final eatingMethod done");
                                    }
                                }
                            } else if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
                                Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
                                Rs2Walker.setTarget(null);
                                eatingMethod(target);
                                if (!Rs2Prayer.isPrayerActive(ULTIMATE_STRENGTH) && Microbot.getClient().getBoostedSkillLevel(PRAYER) >= 1) {
                                    ultimateStrengthOn();
                                }
                            }
                        } else if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
                            Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
                            Rs2Walker.setTarget(null);
                            eatingMethod(target);
                        }
                    }
                } else if (useMage) {
                    if (!castWindBlastOverhead(target)) {
                        if (isTargetPlayerFar(target) && !weHaveEnoughEnergyToPersue() && !isTargetPlayerFarCasting(target)) {
                            if (Rs2Equipment.isWearing(AMMO) || Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                                if (Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                                    // 1. Equip the Bow
                                    if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                        Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                                        // WAIT for the switch to actually happen
                                        sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                                    }
                                    eatingMethod(target);
                                    // 2. Only THEN check the style
                                    if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                        // Now safe to check styles because we know we hold the bow
                                        setCombatStyle(target);
                                    }
                                    eatingMethod(target);
                                }
                            } else if (!Rs2Equipment.isWearing(BRYOPHYTAS_STAFF)) {
                                Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");Rs2Walker.setTarget(null);
                                eatingMethod(target);
                            }
                        } else if (isTargetPlayerFarCasting(target)) {
                            if (Rs2Inventory.contains(MAPLE_LONGBOW)) {
                                // 1. Equip the Bow

                                if (!Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                    Rs2Inventory.interact(MAPLE_LONGBOW, "Wield");

                                    // WAIT for the switch to actually happen

                                    sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_LONGBOW), 1200);

                                }
                                if (Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                    // Now safe to check styles because we know we hold the bow

                                    setCombatStyle(target);

                                }
                                eatingMethod(target);
                            }
                        }
                    }
                }

                if (MossKillerPlugin.isPlayerSnared() && isTargetPlayerFar(target) && target.getCombatLevel() < 88) {
                    Microbot.log("Target is Far and You are Snared");
                    if (isTargetPlayerFarCasting(target)) {
                        if (!castWindBlast(target)) {
                            if (Rs2Inventory.contains(MAPLE_LONGBOW) && Rs2Equipment.isWearing(AMMO)) {
                                // 1. Equip the Bow

                                if (!Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                    Rs2Inventory.interact(MAPLE_LONGBOW, "Wield");

                                    // WAIT for the switch to actually happen

                                    sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_LONGBOW), 1200);

                                }
                                if (Rs2Equipment.isWearing(MAPLE_LONGBOW)) {

                                    // Now safe to check styles because we know we hold the bow

                                    setCombatStyle(target);

                                }
                                eatingMethod(target);
                            }
                        }
                    } else if (!useMage) {
                        if (Rs2Equipment.isWearing(AMMO)) {
                            if (Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                                // 1. Equip the Bow
                                if (!Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                    Rs2Inventory.interact(MAPLE_SHORTBOW, "Wield");

                                    // WAIT for the switch to actually happen
                                    sleepUntil(() -> Rs2Equipment.isWearing(MAPLE_SHORTBOW), 1200);
                                }

                                eatingMethod(target);

                                if (Rs2Equipment.isWearing(MAPLE_SHORTBOW)) {
                                    // Now safe to check styles because we know we hold the bow
                                    setCombatStyle(target);
                                }
                                eatingMethod(target);
                            }
                        } else {
                            equipBestAvailableStaff();
                        }
                    }
                }

            } catch (Exception e) {
                // =================================================================
                // THIS IS THE NET THAT CATCHES THE "ZOMBIE" FALL
                // =================================================================
                Microbot.log("CRITICAL ERROR in Attack Logic: " + e.getMessage());
                e.printStackTrace(); // Prints the line number so we can fix it permanently
            }

            Microbot.log("Leaving basicAttackSetup");
        }
    }

    public boolean healthIsLow() {
        if (Rs2Player.getHealthPercentage() <= 50.0 && Rs2Inventory.contains(FOOD)) {
            return true;
        } else if (Rs2Player.getHealthPercentage() <= 50.0 && !Rs2Inventory.contains(FOOD)) {
            return false;
        }
        return false;
    }

    public List<Rs2PlayerModel> getAttackers(List<Rs2PlayerModel> potentialTargets) {
        List<Rs2PlayerModel> attackers = new ArrayList<>();

        // Don't cast - just get the actual Player object
        Player localPlayer = Microbot.getClient().getLocalPlayer();

        if (localPlayer != null) {
            for (Rs2PlayerModel player : potentialTargets) {
                // Compare using the actual game engine's interacting target
                if (player != null && player.getInteracting() != null) {
                    // You need to compare the player's target NAME or ID, not object reference
                    Actor target = player.getInteracting();
                    if (target != null && target.getName() != null
                            && target.getName().equals(localPlayer.getName())) {
                        attackers.add(player);
                    }
                }
            }
        }

        return attackers;
    }

    public static void panicEat() {
        // 1. Check our resources
        int fastFoodCount = Rs2Inventory.getInventoryFastFood().size();

        // SCENARIO A: We have plenty of 1-tick food (>= 2)
        // Strategy: Spam 1-tick food up to 4 times.
        // "eats a maximum of 4... minimum is eating 2"
        if (fastFoodCount >= 2) {
            for (int i = 0; i < 4; i++) {
                // eatAt(100, true) forces the bot to try eating Fast Food regardless of HP (since we are <60%)
                boolean ate = eatAt(100, true);

                if (ate) {
                    // "inventory change waits back to back"
                    Rs2Inventory.waitForInventoryChanges(600);
                } else {
                    // Stop if we run out of food or something fails
                    break;
                }
            }
            return;
        }

        // SCENARIO B: We have exactly 1 fast food left
        // Strategy: Combo Eat (Standard + the last Fast Food)
        if (fastFoodCount == 1) {
            tryComboEat();
            Rs2Inventory.waitForInventoryChanges(600);
            return;
        }

        // SCENARIO C: No fast food left
        // Strategy: Just eat normal food
        eatAt(100, false);
        Rs2Inventory.waitForInventoryChanges(600);
    }

    /**
     * Executes a Combo Eat: Standard Food + Fast Food in the same tick.
     */
    private static boolean tryComboEat() {
        // We use standard 'useFood' logic first (which ignores fast food now)
        // Then immediately force 'useFastFood'

        // 1. Try Standard
        boolean ateStandard = eatAt(100, false);

        // 2. Try Fast immediately (no wait)
        boolean ateFast = eatAt(100, true);

        return ateStandard || ateFast;
    }

    /**
     * The Main Health Handler Loop
     */
    public static void handleHealth() {
        double hp = getHealthPercentage();

        // TIER 1: PANIC (< 60%)
        if (hp < 60) {
            panicEat();
            return;
        }

        // TIER 2: URGENT (60% - 80%)
        // Prioritise Fast Food (true) to top up efficiently
        if (hp < 80) {
            eatAt(80, true);
            return;
        }

        // TIER 3: MAINTENANCE (80%+)
        // Eat Standard Food (false) to save Karambwans
        eatAt(80, false);
    }


    public void monitorAttacks() {
        // 1. Get everyone attacking us
        List<Rs2PlayerModel> potentialTargets = getPotentialTargets();
        List<Rs2PlayerModel> attackers = getAttackers(potentialTargets);
        int attackerCount = attackers.size();

        // 2. Basic Survival
        if (attackerCount >= 2) {
            Rs2Player.eatAt(90); // Multi-combat eating rule
        }

        // 3. PRAYER LOGIC (Unified for Single and Multi)
        if (attackerCount > 0) {
            handleProtectionPrayers(attackers);
        } else {
            // Optional: Turn off prayers if no one is attacking?
            // Or keep last prayer active.
        }
    }

    private void handleProtectionPrayers(List<Rs2PlayerModel> attackers) {
        // Counters for each threat type
        int meleeThreat = 0;
        int rangeThreat = 0;
        int magicThreat = 0;

        // 1. Assess the Battlefield
        for (Rs2PlayerModel attacker : attackers) {
            String style = getCombatStyle(attacker); // Your existing method

            // Weighting: Is this attacker our primary target?
            // We give our primary target extra "weight" because they are likely hitting us most
            boolean isPrimary = mossKillerPlugin.currentTarget != null &&
                    mossKillerPlugin.isSamePlayer(attacker, mossKillerPlugin.currentTarget);
            int weight = isPrimary ? 2 : 1;

            switch (style) {
                case "MELEE": meleeThreat += weight; break;
                case "RANGE": rangeThreat += weight; break;
                case "MAGIC": magicThreat += weight; break;
            }
        }

        // 2. Determine the Winner
        Rs2PrayerEnum bestPrayer = null;

        if (meleeThreat > rangeThreat && meleeThreat > magicThreat) {
            bestPrayer = Rs2PrayerEnum.PROTECT_MELEE;
        } else if (rangeThreat > meleeThreat && rangeThreat > magicThreat) {
            bestPrayer = Rs2PrayerEnum.PROTECT_RANGE;
        } else if (magicThreat > meleeThreat && magicThreat > rangeThreat) {
            bestPrayer = Rs2PrayerEnum.PROTECT_MAGIC;
        }
        else {
            // TIE-BREAKER: If threat is equal (e.g. 1 Ranger, 1 Mage), use Combat Level
            // Or default to Magic (most dangerous burst damage in Wildy)
            bestPrayer = Rs2PrayerEnum.PROTECT_MAGIC;
        }

        // 3. Apply
        if (bestPrayer != null && !Rs2Prayer.isPrayerActive(bestPrayer)) {
            // Turn off others first to ensure clean switch
            if (bestPrayer != Rs2PrayerEnum.PROTECT_MELEE) Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, false);
            if (bestPrayer != Rs2PrayerEnum.PROTECT_RANGE) Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, false);
            if (bestPrayer != Rs2PrayerEnum.PROTECT_MAGIC) Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, false);

            Rs2Prayer.toggle(bestPrayer, true);
            Microbot.log("Switched Prayer to: " + bestPrayer.name() + " (Threats - Melee:" + meleeThreat + " Range:" + rangeThreat + " Mage:" + magicThreat + ")");
        }
    }


    public String getCombatStyle(Rs2PlayerModel player) {
        int[] equipmentIds = player.getPlayerComposition().getEquipmentIds();

        if (equipmentIds == null) {
            return "UNKNOWN";
        }

        // Check the weapon slot (usually index 3 in equipment array)
        int weaponId = equipmentIds[3];

        // Check for Ranged weapon (Maple Shortbow)
        if (weaponId == MAPLE_SHORTBOW || weaponId == MAPLE_LONGBOW) {
            return "RANGE";
        }

        // Check for Melee weapons (Rune Scimitar, etc.)
        if (weaponId == RUNE_SCIMITAR || weaponId == GILDED_SCIMITAR) {
            return "MELEE";
        }

        // Check for Magic weapons (Staff of Fire, etc.)
        if (weaponId == STAFF_OF_FIRE || weaponId == STAFF_OF_WATER || weaponId == STAFF_OF_EARTH || weaponId == STAFF_OF_AIR) {
            return "MAGIC";
        }

        return "UNKNOWN";
    }

    // Determine the optimal prayer for a Melee-based bot
    private Rs2PrayerEnum determineOptimalPrayerForMelee(int meleeAttackers, int rangedAttackers, int magicAttackers,
                                                         int meleeCombatLevelSum, int rangedCombatLevelSum, int magicCombatLevelSum) {
        // Melee is weak to Magic, so prioritize Protect from Magic
        if (magicAttackers >= 3 || (magicCombatLevelSum >= meleeCombatLevelSum && magicCombatLevelSum >= rangedCombatLevelSum)) {
            return Rs2PrayerEnum.PROTECT_MAGIC;
        }
        // If Magic is not a significant threat, fallback to Protect from Melee or Ranged based on their presence
        if (rangedAttackers >= 3 || rangedCombatLevelSum >= meleeCombatLevelSum) {
            return Rs2PrayerEnum.PROTECT_RANGE;
        }
        return Rs2PrayerEnum.PROTECT_MELEE;
    }

    // Determine the optimal prayer for a Ranged-based bot
    private Rs2PrayerEnum determineOptimalPrayerForRange(int meleeAttackers, int rangedAttackers, int magicAttackers,
                                                         int meleeCombatLevelSum, int rangedCombatLevelSum, int magicCombatLevelSum) {
        // Ranged is weak to Melee, so prioritize Protect from Melee
        if (meleeAttackers >= 3 || (meleeCombatLevelSum >= rangedCombatLevelSum && meleeCombatLevelSum >= magicCombatLevelSum)) {
            return Rs2PrayerEnum.PROTECT_MELEE;
        }
        // If Melee is not a significant threat, fallback to Protect from Magic or Ranged based on their presence
        if (magicAttackers >= 3 || magicCombatLevelSum >= rangedCombatLevelSum) {
            return Rs2PrayerEnum.PROTECT_MAGIC;
        }
        return Rs2PrayerEnum.PROTECT_RANGE;
    }

    // Determine the optimal prayer for a Mage-based bot
    private Rs2PrayerEnum determineOptimalPrayerForMage(int meleeAttackers, int rangedAttackers, int magicAttackers,
                                                        int meleeCombatLevelSum, int rangedCombatLevelSum, int magicCombatLevelSum) {
        // Magic is weak to Ranged, so prioritize Protect from Missiles
        if (rangedAttackers >= 3 || (rangedCombatLevelSum >= meleeCombatLevelSum && rangedCombatLevelSum >= magicCombatLevelSum)) {
            return Rs2PrayerEnum.PROTECT_RANGE;
        }
        // If Ranged is not a significant threat, fallback to Protect from Melee or Magic based on their presence
        if (meleeAttackers >= 3 || meleeCombatLevelSum >= magicCombatLevelSum) {
            return Rs2PrayerEnum.PROTECT_MELEE;
        }
        return Rs2PrayerEnum.PROTECT_MAGIC;
    }

    public boolean weHaveEnoughEnergyToPersue() {
        return Rs2Player.getRunEnergy() > 5 && hasEnergyPotion();
    }

    public boolean hasEnergyPotion() {
        return Rs2Inventory.contains(ENERGY_POTION1)
                || Rs2Inventory.contains(ENERGY_POTION2)
                || Rs2Inventory.contains(ENERGY_POTION3)
                || Rs2Inventory.contains(ENERGY_POTION4);
    }

    public boolean castWindBlast(Rs2PlayerModel target) {

        if (Rs2Player.getRealSkillLevel(MAGIC) > 40
                && isTargetPlayerFar(target)
                && !mossKillerPlugin.lobsterEaten()
                && Rs2Inventory.contains(DEATH_RUNE)
                && Rs2Magic.canCast(WIND_BLAST)) {

            doWeFocusCamera(target);
            equipBestAvailableStaff();
            Rs2Walker.setTarget(null);
            int retries = 0;
            do {
                Rs2Magic.castOn(WIND_BLAST, target);
                sleep(300);
                eatAt(70, false);
                sleepUntil(() -> Rs2Player.getAnimation() == 711 || Rs2Player.getAnimation() == 1162, 1000);

                boolean noAnimations = Rs2Player.getAnimation() != 711 && Rs2Player.getAnimation() != 1162;
                boolean isNotMoving = !Rs2Player.isMoving();

                if (noAnimations && isNotMoving && retries < 3) {
                    retries++;
                } else {
                    break; // Exit the loop after success or max retries
                }
            } while (true);
            eatingMethod(target);

            return true;
        }

        return false;
    }

    public boolean castWindBlastOverhead(Rs2PlayerModel target) {
        boolean useMelee = mossKillerPlugin.useMelee();

        if (Rs2Player.getRealSkillLevel(MAGIC) > 40
                && !mossKillerPlugin.lobsterEaten()
                && Rs2Inventory.contains(DEATH_RUNE)
                && !useMelee
                && Rs2Magic.canCast(WIND_BLAST)) {

            doWeFocusCamera(target);

            equipBestAvailableStaff();

            Rs2Walker.setTarget(null);
            int retries = 0;
            do {
                Rs2Magic.castOn(WIND_BLAST, target);
                sleepUntil(() -> Rs2Player.getAnimation() == 711 || Rs2Player.getAnimation() == 1162, 1000);

                boolean noAnimations = Rs2Player.getAnimation() != 711 && Rs2Player.getAnimation() != 1162;
                boolean isNotMoving = !Rs2Player.isMoving();

                if (noAnimations && isNotMoving && retries < 3) {
                    retries++;
                } else {
                    break; // Exit the loop after success or max retries
                }
            } while (true);
            eatingMethod(target);

            return true;
        }

        return false;
    }

    /**
     * Equips any available staff from inventory, prioritizing Bryophyta's staff
     */
    private void equipAnyAvailableStaff() {
        // Priority list: Bryophyta's staff first, then Fire staff, then any other staff
        if (Rs2Inventory.hasItem(BRYOPHYTAS_STAFF_UNCHARGED)) {
            Rs2Inventory.interact(BRYOPHYTAS_STAFF_UNCHARGED, "Wield");
            sleepUntil(() -> Rs2Equipment.isWearing(BRYOPHYTAS_STAFF_UNCHARGED), 2000);
        }
        else if (Rs2Inventory.hasItem(STAFF_OF_FIRE)) {
            Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");
            sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE), 2000);
        }
    }

    /**
     * Equips the best available staff, prioritizing Bryophyta's staff over others
     */
    private void equipBestAvailableStaff() {
        // Check what we currently have equipped
        boolean bryoStaffEquipped = Rs2Equipment.isWearing(BRYOPHYTAS_STAFF_UNCHARGED);
        boolean fireStaffEquipped = Rs2Equipment.isWearing(STAFF_OF_FIRE);
        boolean anyStaffEquipped = hasAnyStaffEquipped();

        // If Bryo staff is already equipped, we're good
        if (bryoStaffEquipped) {
            return;
        }

        // If we have Bryo staff in inventory, equip it (regardless of what's currently equipped)
        if (Rs2Inventory.hasItem(BRYOPHYTAS_STAFF)) {
            Rs2Inventory.interact(BRYOPHYTAS_STAFF, "Wield");
            sleepUntil(() -> Rs2Equipment.isWearing(BRYOPHYTAS_STAFF), 2000);
            return;
        }

        // If fire staff isn't equipped but is in inventory, and we don't have any staff equipped
        if (!fireStaffEquipped && Rs2Inventory.hasItem(STAFF_OF_FIRE) && !anyStaffEquipped) {
            Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");
            sleepUntil(() -> Rs2Equipment.isWearing(STAFF_OF_FIRE), 2000);
        }
    }

    /**
     * Checks if the player has any staff equipped
     */
    /**
     * Checks if the player has any staff equipped
     * @return true if any staff is equipped, false otherwise
     */
    private boolean hasAnyStaffEquipped() {
        // Check if any item with "staff" in its name is equipped
        return Rs2Equipment.isWearing("staff");
    }

    public boolean doWeFocusCamera(Rs2PlayerModel target) {
        if (target == null) {
            return false; // Don't crash, just skip camera focus
        }
        if (!Rs2Camera.isTileOnScreen(target.getLocalLocation())) {
            Rs2Camera.turnTo(target);
            return true;
        }
        return false;
    }

    public void eatingMethod(Rs2PlayerModel target) {
        Microbot.log("entering eating method");
        // 1. SAFEGUARD: If target is null, pretend we are in danger and just check our own HP
        // This prevents the NullPointerException crash
        if (target == null) {
            int currentHpPercent = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) * 100 / Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);
            if (currentHpPercent < 70) { // Default safety threshold
                if (Rs2Inventory.contains(FOOD)) {
                    Rs2Player.eatAt(70);
                }
            }
            return; // Exit to avoid deeper errors
        }

        // 2. EXISTING LOGIC (Only runs if target exists)
        if (Rs2Player.isMoving() && !Rs2Player.isRunEnabled()) {
            toggleRunEnergyOn();
        }

        int eatThreshold = 70;
        if (target.getHealthScale() > 0) {
            int healthPercentage = (target.getHealthRatio() * 100) / target.getHealthScale();
            if (healthPercentage < 20) {
                eatThreshold = 50;
            }
        }

        // Non-blocking eat check
        int currentHp = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
        if (currentHp < eatThreshold) {
            if (Rs2Inventory.contains(FOOD)) {
                Rs2Player.eatAt(eatThreshold);
            }
        }
        Microbot.log("exiting eating method");
    }

    public void isTargetOnSameTile(Rs2PlayerModel target) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if (target.getWorldLocation() == playerLocation) {

            if (scheduledFuture.isDone()
                    && Rs2Player.getInteracting() != target
                    && target.getCombatLevel() < 88) {
                if (doWeFocusCamera(target)) {
                    sleep(300);
                }
                attack(target);
            }

        }
    }

    public void setCombatStyle(Rs2PlayerModel target) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        Microbot.log("entering set combat style");

        // Get the current attack style
        int attackStyle = Microbot.getClient().getVarpValue(VarPlayerID.COM_MODE);

        sleep(600);

        // Assuming currentTarget is already declared and holds the NPC or player target
        int distanceToTarget = target.getWorldLocation().distanceTo(playerLocation);
        // Check if the target is within 7 tiles
        if (distanceToTarget <= 7) {
            // If the target is within 7 tiles, switch to Short-range if not already set
            if (mossKillerPlugin.isDefensive()) {
                Rs2Combat.setAttackStyle(COMBAT_STYLE_TWO); // Set Short-range
            }
        } else {
            // If the target is more than 7 tiles away, switch to Long-range if not already set
            if (attackStyle != 3) {
                if (Rs2Tab.getCurrentTab() != InterfaceTab.COMBAT) {
                    Rs2Tab.switchToCombatOptionsTab();
                    sleep(200); // Ensure the tab has time to switch
                }
                Rs2Combat.setAttackStyle(COMBAT_STYLE_FOUR); // Set Long-range
            }
        }
        Microbot.log("exiting set combat style");
    }

    private void ultimateStrengthOn() {
        if (Microbot.getClient().getBoostedSkillLevel(PRAYER) >= 1
                && Microbot.getClient().getRealSkillLevel(PRAYER) >= 33) {
            toggle(Rs2PrayerEnum.ULTIMATE_STRENGTH, true);
        }
    }

    private void ultimateStrengthOff() {
        if (Microbot.getClient().getBoostedSkillLevel(PRAYER) >= 1
                && Microbot.getClient().getRealSkillLevel(PRAYER) >= 33) {
            toggle(Rs2PrayerEnum.ULTIMATE_STRENGTH, false);
        }
    }

    public static boolean isNpcInteractingWithMe() {
        // check if we're not in multi combat
        if (Rs2Player.isInMulti()) {
            return false;
        }

        List<Rs2NpcModel> interactingNpcs = Rs2Npc.getNpcsForPlayer(npc -> true)
                .collect(Collectors.toList());

        // return true if any NPC is interacting with us
        return !interactingNpcs.isEmpty();
    }

    private boolean isStrengthPotionPresent() {
        for (int id : strengthPotionIds) {
            if (Rs2Inventory.contains(id)) {
                return true; // Strength potion found in inventory
            }
        }
        return false; // No strength potion in inventory
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
            } else if (Rs2Inventory.contains("Strength potion(4)")) {
                Rs2Inventory.interact("Strength potion(4)", "Drink");
            }

            sleep(300);
        } else {
            System.out.println("Boosted strength level is high enough, no need to drink.");
        }
    }


    private boolean isTargetPlayerFar(Rs2PlayerModel targetPlayer) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        WorldPoint targetLocation = targetPlayer.getWorldLocation();

        if (playerLocation == null || targetLocation == null) {
            return true; // Treat as far if locations are null
        }

        int dx = Math.abs(playerLocation.getX() - targetLocation.getX());
        int dy = Math.abs(playerLocation.getY() - targetLocation.getY());

        // Only North, South, East, and West are "close"
        return !(dx == 1 && dy == 0 || dx == 0 && dy == 1);
    }

    private boolean isTargetPlayerFarCasting(Rs2PlayerModel targetPlayer) {
        // Get the current player's location
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        // Get the target player's location
        WorldPoint targetLocation = targetPlayer.getWorldLocation();

        // If either location is null, consider them far away
        if (playerLocation == null || targetLocation == null) {
            return true; // Treat as farcasting if locations are null
        }

        // Calculate the Manhattan distance
        int distance = Math.abs(playerLocation.getX() - targetLocation.getX())
                + Math.abs(playerLocation.getY() - targetLocation.getY());
        // Check if the distance is greater than or equal to 10 tiles
        return distance >= 10;
    }


    private void equipItems() {
        String[] items = {"Leather vambraces",
                "Leather boots",
                "Studded chaps", "Rune Chainbody",
                "Amulet of power"};

        for (String item : items) {
            if (Rs2Bank.hasItem(item)) {
                Rs2Bank.withdrawOne(item, 1);
                Rs2Inventory.waitForInventoryChanges(2500);
                if (Rs2Inventory.hasItem(item)) {
                    Rs2Inventory.interact(item, "Wear");
                    Rs2Inventory.waitForInventoryChanges(2500);
                    verifyEquipment(item);
                }
            }
        }
    }

    private void equipAnyWizardHat() {
        for (String hat : WIZARD_HATS) {

            if (!Rs2Inventory.hasItem(hat)) {
                continue;
            }

            if (Rs2Equipment.isWearing(hat)) {
                return;
            }

            Rs2Inventory.interact(hat, "Wear");
            return;
        }
    }


    private boolean hasAnyWizardHat() {
        for (String hat : WIZARD_HATS) {
            if (Rs2Inventory.hasItem(hat)) {
                return true;
            }
        }
        return false;
    }



    private void verifyEquipment(String item) {
        if (!Rs2Equipment.isWearing(item)) {
            System.out.println("Failed to equip: " + item);
        } else {
            System.out.println("Successfully equipped: " + item);
        }
    }

    public void handleMossGiants() {

        // --- DIAGNOSTIC START ---
        long frameID = System.currentTimeMillis();
        int currentHP = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
        Rs2PlayerModel target = mossKillerPlugin.currentTarget;
        String targetName = (target != null) ? target.getName() : "NONE";

        System.out.println(String.format("[MOSS_LOG %d] HP:%d | Target:%s | State:%s",
                frameID, currentHP, targetName, state));
        Microbot.log(String.format("ID:%d | Target:%s | HP:%d", frameID, targetName, currentHP));
        // --- DIAGNOSTIC END ---

        //attack the target before the sleep
        if (!scheduledFuture.isDone() || mossKillerPlugin.currentTarget != null) {
            if (mossKillerPlugin.currentTarget != null) {
                doWeFocusCamera(mossKillerPlugin.currentTarget);
            }
            sleep(300);
            if (mossKillerPlugin.currentTarget != null && Rs2Player.getInteracting() != mossKillerPlugin.currentTarget
                    && mossKillerPlugin.currentTarget.getCombatLevel() < 88) {
                basicAttackSetup();
                attack(mossKillerPlugin.currentTarget);
                sleep(300);
                state = MossKillerState.PKER;
            }
        }

        if (mossKillerPlugin.currentTarget != null && mossKillerPlugin.currentTarget.getCombatLevel() > 87) {
            state = MossKillerState.PKER;
        }

        if (mossKillerPlugin.currentTarget != null && mossKillerPlugin.currentTarget.getCombatLevel() < 87) {
            state = MossKillerState.PKER;
        }

        if (Rs2Prayer.isPrayerActive(STEEL_SKIN)) {
            Rs2Prayer.toggle(STEEL_SKIN, false);
        }

        if (Rs2Prayer.isPrayerActive(MYSTIC_LORE)) {
            Rs2Prayer.toggle(MYSTIC_LORE, false);
        }

        if (Rs2Equipment.isWearing(STAFF_OF_FIRE)
                && !mossKillerPlugin.getAttackStyle()) {
            setAutocastFireStrike();
        }

        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if (!Rs2Inventory.contains(FOOD) || BreakHandlerScript.breakIn <= 120) {
            Microbot.log("Inventory does not contains FOOD or break in less than 120");
            state = MossKillerState.TELEPORT;
            return;
        }

        if (!Rs2Inventory.hasItemAmount(MIND_RUNE, 15)) {
            Microbot.log("inverse of 15 mind runes, let's bail");
            state = MossKillerState.TELEPORT;
            return;
        }

        if (!Rs2Inventory.contains(FOOD) || Rs2Inventory.contains(MOSSY_KEY) || !Rs2Inventory.contains(MIND_RUNE)) {
            Microbot.log("You have a Mossy Key! OR out of food. Get outta there!");
            state = MossKillerState.TELEPORT;
            return;
        }

        if (Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) > 10 && mossKillerPlugin.currentTarget == null
                && MOSS_GIANT_AREA.contains(playerLocation)) {
            Rs2Walker.walkTo(MOSS_GIANT_SPOT);
            return;
        }
        int lobsterCount = Rs2Inventory.count(FOOD);
        if (Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) > 20
                && lobsterCount < 17) {
            Microbot.log("Less than 17 FOOD, teleport reset");
            state = MossKillerState.TELEPORT;
        } else if (lobsterCount == 17
                && playerLocation.getY() > 3500
                && !MOSS_GIANT_AREA.contains(playerLocation)) {
            Microbot.log("17 FOOD, trying to walk to moss giants");
            if (scheduledFuture.isDone()) { // Only initiate if not already walking to Moss Giants
                handleAsynchWalk("Moss Giants");
            }
            return;
        }

        int randomValue = (int) Rs2Random.truncatedGauss(60, 70, 4.0);
        eatAt(randomValue);

        // Check if loot is nearby and pick it up if it's in LOOT_LIST
        for (int lootItem : LOOT_LIST) {
            if (Rs2GroundItem.exists(lootItem, 7)) {
                if (target != null) {
                    System.out.println("[DEBUG] Aborting loot: Target " + targetName + " is active.");
                    break;
                }
            }
            if (Rs2GroundItem.exists(lootItem, 7)
                    && Rs2Inventory.getEmptySlots() == 0) {
                eatAt(100, false);
                sleepUntil(() -> !Rs2Inventory.isFull());
                toggleRunEnergyOn();
                if (mossKillerPlugin.currentTarget != null) {
                    break;
                }
                Rs2GroundItem.interact(lootItem, "Take", 7);
                // Waits for the item to arrive OR the PKer to arrive
                safeWaitForInventoryChanges(3500);

                // IMMEDIATELY check if we stopped because of a PKer
                if (mossKillerPlugin.currentTarget != null) return;

            } else if (Rs2GroundItem.exists(lootItem, 7)
                    && Rs2Inventory.getEmptySlots() > 0) {
                toggleRunEnergyOn();
                if (mossKillerPlugin.currentTarget != null) {
                    break;
                }
                Rs2GroundItem.interact(lootItem, "Take", 7);
                // Waits for the item to arrive OR the PKer to arrive
                safeWaitForInventoryChanges(3500);

                // IMMEDIATELY check if we stopped because of a PKer
                if (mossKillerPlugin.currentTarget != null) return;
            }
        }

        if (mossKillerPlugin.currentTarget == null) {


            if (Rs2GroundItem.loot("Coins", 119, 7)) {
                Rs2Inventory.waitForInventoryChanges(3500);
            }

            if (Rs2GroundItem.loot("Chaos rune", 7, 7)) {
                Rs2Inventory.waitForInventoryChanges(3500);
            }
        }
        if (Rs2Inventory.contains(NATURE_RUNE) &&
                !Rs2Inventory.contains(STAFF_OF_FIRE) &&
                mossKillerPlugin.currentTarget == null &&
                Rs2Inventory.contains(ALCHABLES) &&
                config.alchLoot()){

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
        } else if (Rs2Inventory.contains(STAFF_OF_FIRE) && mossKillerPlugin.currentTarget == null) {
            Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");
        }


        if (config.buryBones()) {
            // Check safety before starting
            if (safetyCheck()) return;

            if (Rs2Inventory.contains(BIG_BONES)) {
                // Sleep 600-1750, BUT break immediately if currentTarget != null
                sleepUntil(() -> safetyCheck(), Rs2Random.between(600, 1750));
                if (safetyCheck()) return; // Double check after sleep

                Rs2Inventory.interact(BIG_BONES, "Bury");

                // Wait for animation, BUT break if we get a target
                sleepUntil(() -> !Rs2Player.isAnimating() || safetyCheck(), 2400);
            }

            // Same logic for the Pickup portion...
            if (!Rs2Inventory.isFull() && Rs2GroundItem.interact(BIG_BONES, "Take", 2)) {
                toggleRunEnergyOn();
                // Wait for bones to appear in inv, OR for a PKer to appear
                sleepUntil(() -> Rs2Inventory.contains(BIG_BONES) || safetyCheck(), 3500);

                if (safetyCheck()) return;

                if (Rs2Inventory.contains(BIG_BONES)) {
                    sleepUntil(() -> safetyCheck(), Rs2Random.between(600, 1750));
                    if (safetyCheck()) return;

                    Rs2Inventory.interact(BIG_BONES, "Bury");
                    sleepUntil(() -> !Rs2Player.isAnimating() || safetyCheck(), 2400);
                }
            }
        }

        if (!Rs2Equipment.isWearing(HEAD) && hasAnyWizardHat()) {
            equipAnyWizardHat();
        }

        // Check if any players are near
        // Check if any players are near (Anti-PK Logic)
        if (mossKillerPlugin.currentTarget == null) {
            if (!getNearbyPlayers(7).isEmpty()) {
                if (playerCounter > 5) {
                    // 1. SAFE SLEEP (Replaces fixed sleep)
                    // If target appears, ABORT immediately
                    if (!safeSleep(Rs2Random.between(600, 1200))) return;

                    int world = Login.getRandomWorld(false, null);
                    if (world == 301 || world == 308) {
                        return;
                    }

                    // 2. TRIGGER HOP
                    Microbot.hopToWorld(world);

                    // 3. SAFE HOP WAIT
                    // We loop manually so we can check for combat constantly
                    boolean isHopped = false;
                    long hopStart = System.currentTimeMillis();

                    while (System.currentTimeMillis() - hopStart < 5000) {
                        // A. Emergency Exit: Combat!
                        if (mossKillerPlugin.currentTarget != null) {
                            Microbot.log("Hopping aborted! Under attack!");
                            return;
                        }

                        // B. Check Hop Status
                        if (Microbot.getClient().getWorld() == world && Microbot.isLoggedIn()) {
                            isHopped = true;
                            break;
                        }
                        sleep(100);
                    }

                    if (!isHopped) return; // Hop failed or timed out

                    // 4. Post-Hop Logic
                    playerCounter = 0;
                    int randomThreshold = (int) Rs2Random.truncatedGauss(0, 5, 1.5);
                    if (randomThreshold > 3) {
                        Rs2Inventory.open();
                    }
                    return;
                }
                playerCounter++;
            } else {
                playerCounter = 0;
            }

            // 5. SAFE EQUIPMENT CHECK
            // Replaced sleep(600) with safeSleep(600)
            if (!Rs2Equipment.isWearing(STAFF_OF_FIRE) && mossKillerPlugin.currentTarget == null) {
                if (!safeSleep(600)) return; // Combat check

                Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");

                if (!safeSleep(600)) return; // Combat check

                setAutocastFireStrike();
            }

            // 6. MOSS GIANT ATTACK LOGIC
            if (!Rs2Combat.inCombat() && mossKillerPlugin.currentTarget == null) {
                System.out.println("attackingmoss");
                Rs2NpcModel mossGiant = Rs2Npc.getNpc("Moss giant");

                if (mossGiant != null && Rs2Npc.getHealth(mossGiant) > 0) {
                    if (!Rs2Camera.isTileOnScreen(mossGiant.getLocalLocation())) {
                        Rs2Camera.turnTo(mossGiant);
                        if (!safeSleep(500)) return; // Combat check during camera turn
                    }

                    if (Objects.equals(mossGiant.getInteracting(), getLocalPlayer())) {
                        System.out.println("moss giant already attacking so not going to attack");
                    } else {
                        Rs2Npc.attack(mossGiant);
                    }

                } else {
                    System.out.println("Skipping attack: Moss Giant has 0 HP or is not found.");
                }
            }

            // 7. FINAL SAFE SLEEP
            // The biggest danger was this sleep(800, 2000) at the end.
            // If a PKer logs in right now, we used to wait 2 seconds before reacting. Not anymore.
            safeSleep(Rs2Random.between(800, 2000));
        }


        if (Rs2Inventory.contains(MOSSY_KEY)) {
            state = MossKillerState.PKER;
        }
    }

    /**
     * A copy of waitForInventoryChanges that interrupts immediately if a Target is found.
     */
    private boolean safeWaitForInventoryChanges(int timeout) {
        // 1. Snapshot the current inventory (Deep Copy logic)
        // We assume Rs2Inventory.items() returns the List<Rs2ItemModel>
        // AFTER (FIXED):
        List<Rs2ItemModel> initialItems = Rs2Inventory.items()
                .collect(Collectors.toList());
        long startTime = System.currentTimeMillis();

        // 2. Loop until timeout
        while (System.currentTimeMillis() - startTime < timeout) {

            // --- THE FIX: Emergency Interrupt ---
            if (mossKillerPlugin.currentTarget != null) {
                return false; // Stop waiting, we have a fight!
            }

            // --- The Deep Check (Replicating hasInventoryChanged) ---
            List<Rs2ItemModel> currentItems = Rs2Inventory.items()
                    .collect(Collectors.toList());

            if (hasInventoryChangedLocal(initialItems, currentItems)) {
                return true; // Inventory changed successfully
            }

            sleep(100); // Check every 100ms (same as your 'time' param)
        }

        return false; // Timed out
    }

    /**
     * Replicates the robust comparison logic locally
     */
    private boolean hasInventoryChangedLocal(List<Rs2ItemModel> initial, List<Rs2ItemModel> current) {
        if (initial.size() != current.size()) return true;

        for (int i = 0; i < initial.size(); i++) {
            Rs2ItemModel startItem = initial.get(i);
            Rs2ItemModel currItem = current.get(i);

            // Check for slot becoming empty or filled
            if ((startItem == null && currItem != null) || (startItem != null && currItem == null)) {
                return true;
            }

            // Check for Item ID change or Stack Size change
            if (startItem != null && currItem != null) {
                if (startItem.getId() != currItem.getId() || startItem.getQuantity() != currItem.getQuantity()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sleeps for a duration, but WAKES UP IMMEDIATELY if we get a target.
     * Returns TRUE if the sleep finished normally.
     * Returns FALSE if we were interrupted by combat (Target found).
     */
    private boolean safeSleep(int duration) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < duration) {
            // --- EMERGENCY INTERRUPT ---
            // If we picked up a target (or are taking damage), STOP SLEEPING
            if (mossKillerPlugin.currentTarget != null) {
                Microbot.log("SafeSleep interrupted! Target detected.");
                return false;
            }

            // Also good to check if we are dying, just in case target logic lags
            if (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) < 40) {
                return false;
            }

            sleep(100); // Check every 100ms
        }
        return true; // Completed the full sleep safely
    }

    private boolean safetyCheck() {
        // If we have a target, STOP whatever we are doing immediately
        if (mossKillerPlugin.currentTarget != null) {
            state = MossKillerState.PKER;
            return true; // We are in danger
        }
        return false; // Safe
    }

    private List<Rs2PlayerModel> getPotentialTargets() {
        return getNearbyPlayers(12);
    }

    public List<Rs2PlayerModel> getNearbyPlayers(int distance) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Use the predicate-based getPlayers method directly
        return Rs2Player.getPlayers(p -> p != null &&
                        p.getWorldLocation().distanceTo(playerLocation) <= distance)
                .collect(Collectors.toList());
    }


    public void walkToMossGiants() {
        Microbot.log(String.valueOf(state));

        if (!scheduledFuture.isDone() || ShortestPathPlugin.pathfinder != null) {
            sleep(600);
            state = MossKillerState.PKER;
        }
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        int currentWorld = Microbot.getClient().getWorld();
        if (currentWorld == 301 || currentWorld == 308 || currentWorld == 316) {
            int world = Login.getRandomWorld(false, null);
            if (world == 301 || world == 308) {
                return;
            }
            boolean isHopped = Microbot.hopToWorld(world);
            sleepUntil(() -> isHopped, 5000);
            if (!isHopped) return;
        }

        if (mossKillerPlugin.currentTarget != null) {

            if (Rs2Player.isInCombat()) {
                Rs2Walker.setTarget(null);
                if (!scheduledFuture.isDone()) {
                    scheduledFuture.cancel(true);
                }

                state = MossKillerState.PKER;

            }
        }

        if (mossKillerPlugin.currentTarget == null) {
            if (WildyKillerScript.WEST_BARRIER_OUTSIDE.contains(playerLocation) ||
                    WildyKillerScript.NORTH_BARRIER_OUTSIDE.contains(playerLocation) ||
                    WildyKillerScript.CORRIDOR.contains(playerLocation)) {
                if (scheduledFuture.isDone()) { // Only initiate if not already walking to Moss Giants
                    handleAsynchWalk("Moss Giants");
                }
                state = MossKillerState.WALK_TO_MOSS_GIANTS;
            }
        }

        if (mossKillerPlugin.currentTarget == null) {
            if (MOSS_GIANT_AREA.contains(playerLocation)
                    && Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) < 10) {
                Rs2Walker.walkTo(MOSS_GIANT_SPOT); //dont need to eat
            }
        }

        if (MOSS_GIANT_AREA.contains(playerLocation) && mossKillerPlugin.currentTarget == null) {
            state = MossKillerState.FIGHT_MOSS_GIANTS;
        }

        if (!Rs2Inventory.hasItemAmount(MIND_RUNE, 750) && mossKillerPlugin.currentTarget == null) {
            Microbot.log("Inventory Empty or Not Recently Banked");
            int lobsterCount = Rs2Inventory.count(FOOD);
            if (lobsterCount < 17) {
                Microbot.log("Less than 17 FOOD, teleport reset");
                state = MossKillerState.TELEPORT;
            }

            if (Rs2Inventory.hasItem(MOSSY_KEY)) {
                Microbot.log("Mossy key detected!");
                state = MossKillerState.TELEPORT;
            }

            if (MOSS_GIANT_AREA.contains(playerLocation) ||
                    CORRIDOR.contains(playerLocation) ||
                    TOTAL_FEROX_ENCLAVE.contains((playerLocation))
                            && mossKillerPlugin.currentTarget == null) {
                Microbot.log("You're in the wilderness and I don't get the problem");
                if (Rs2Inventory.hasItemAmount(FOOD,17)) {
                    Rs2Player.eatAt(100);
                }
                if (!Rs2Equipment.isWearing()) {
                    state = MossKillerState.WALK_TO_BANK;
                }
            }
            if (playerLocation.getY() < 3520) {
                state = MossKillerState.WALK_TO_BANK;
            }
        }

        if (Rs2Inventory.hasItemAmount(MIND_RUNE, 750) && mossKillerPlugin.currentTarget == null) {
            if (MOSS_GIANT_AREA.contains(playerLocation) || CORRIDOR.contains(playerLocation) || TOTAL_FEROX_ENCLAVE.contains((playerLocation))) {
                Microbot.log("Looks like you're in the wilderness with 750 mind runes");
                if (Rs2Inventory.hasItem(FOOD) && Rs2Inventory.hasItem(LAW_RUNE) && Rs2Inventory.hasItem(AIR_RUNE) && !Rs2Inventory.hasItem(MOSSY_KEY)) {
                    Microbot.log("You should be attacking Moss Giants");
                    if (Rs2Player.getRunEnergy() < 90 && TOTAL_FEROX_ENCLAVE.contains(playerLocation)) {
                        Rs2Walker.walkTo(3130, 3636, 0);
                        if (Rs2GameObject.exists(POOL_OF_REFRESHMENT)) {
                            Rs2GameObject.interact(POOL_OF_REFRESHMENT, "Drink");
                            sleepUntil(() -> Rs2Player.getRunEnergy() == 100 && !Rs2Player.isAnimating(2000));
                        }
                    }
                    if (scheduledFuture.isDone()) { // Only initiate if not already walking to Moss Giants
                        handleAsynchWalk("Moss Giants");
                    } else if (!Rs2Inventory.hasItem(FOOD) && Rs2Inventory.hasItem(LAW_RUNE) && Rs2Inventory.hasItem(AIR_RUNE) && !Rs2Inventory.hasItem(MOSSY_KEY)) {
                        Microbot.log("You should be getting food");
                        if (scheduledFuture.isDone()) { // Only initiate if not already walking to Moss Giants
                            state = MossKillerState.BANK;
                        }
                        return;
                    }
                }
            }
        }
        if (Rs2Inventory.hasItemAmount(MIND_RUNE, 1500)) {
            state = MossKillerState.BANK;
        }
        if (playerLocation.getY() < 3520) {
            Microbot.log("You're not in the wilderness and have 750 Mind runes");
            if (Rs2Inventory.hasItem(LAW_RUNE) && Rs2Inventory.hasItem(AIR_RUNE) && !Rs2Inventory.hasItem(MOSSY_KEY)) {
                Microbot.log("Go to ferox");
                state = MossKillerState.CASTLE_WARS_TO_FEROX;
            } else {
                state = MossKillerState.WALK_TO_BANK;
            }
        }
        if (playerLocation.getY() > 9000) {
            Microbot.log("You're in castle wars portal");
            state = MossKillerState.CASTLE_WARS_TO_FEROX;
        }

    }

    public void handleBanking() {

        Microbot.log(String.valueOf(state));

        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if (!scheduledFuture.isDone()) {
            sleep(600);
            state = MossKillerState.PKER;
        }

        sleep(1200);

        if (CASTLE_WARS_AREA.contains(playerLocation)
                && Rs2Inventory.containsAll(AIR_RUNE, LAW_RUNE)
                && Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
            teleportAndStopWalking();
        }

        sleep(1200);

        if (MOSS_GIANT_AREA.contains(playerLocation) || Rs2GroundItem.exists(CHAOS_RUNE, 10)) {
            state = MossKillerState.FIGHT_MOSS_GIANTS;
        }

        sleep(6200);

        if (LUMBRIDGE_AREA.contains(playerLocation) || !Rs2Equipment.isWearing()) {
            state = MossKillerState.WALK_TO_BANK;
        }

        sleep(1200);

        if (playerLocation.getY() > 3500
                && !TOTAL_FEROX_ENCLAVE.contains(playerLocation)) {
            Microbot.log("you're somewhere in wilderness and wanna bank?");
            sleep(1200);
            state = MossKillerState.TELEPORT;
        }

        sleep(1200);

        if (Rs2Inventory.hasItemAmount(MIND_RUNE, 1500) &&
                Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) > 6) {
            state = MossKillerState.WALK_TO_BANK;
        }


        if (Rs2Bank.openBank()) {
            sleepUntil(Rs2Bank::isOpen, 180000);
            Microbot.log("Finished the 3 minute sleepUntil bank is open");
            Rs2Bank.depositAll();
            sleepUntil(Rs2Inventory::isEmpty);
            if (!Rs2Equipment.isWearing()) {
                sleep(1000, 1500);
                Rs2Bank.withdrawX(AIR_RUNE, 100);
                Rs2Inventory.waitForInventoryChanges(2500);
                Rs2Bank.withdrawX(MIND_RUNE, 100);
                sleepUntil(() -> Rs2Inventory.contains(MIND_RUNE));
                Rs2Inventory.waitForInventoryChanges(2500);
                Rs2Bank.withdrawOne("Staff of fire", 1);
                Rs2Inventory.waitForInventoryChanges(2500);
                if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                    if (Rs2Inventory.hasItem(STAFF_OF_FIRE)) {
                        Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");
                    } else {
                        System.out.println("Staff of fire is not in the inventory.");
                    }
                }
                sleep(900, 3500);
                Rs2Bank.closeBank();
                sleep(900, 3500);
                setAutocastFireStrike();
                sleep(900, 3500);
                Rs2Bank.openBank();
                sleep(900, 3500);
                Rs2Bank.depositAll();

            }
            sleep(1000, 1500);
            if (!Rs2Bank.hasItem(AIR_RUNE) || !Rs2Bank.hasItem(LAW_RUNE) || !Rs2Bank.hasItem(FOOD)) {
                state = MossKillerState.WALK_TO_BANK;
                return;
            }
            int keyTotal = Rs2Bank.count("Mossy key");
            Microbot.log("Key Total: " + keyTotal);
            sleep(600, 900);
            if (Rs2Inventory.isEmpty()) {
                Rs2Bank.withdrawX(AIR_RUNE, 1550);
                Rs2Inventory.waitForInventoryChanges(2500);
                if (!Rs2Bank.hasItem(AIR_RUNE)) {
                    JOptionPane.showMessageDialog(null, "The Script has Shut Down due to no Air Runes in bank.");
                    moarShutDown();
                    shutdown();
                }
                if (Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                    sleep(1200,1900);
                    Rs2Bank.withdrawAndEquip(STAFF_OF_FIRE);
                    sleep(1200,1900);

                }
                sleep(500, 1000);
                Rs2Bank.withdrawX(LAW_RUNE, 5);
                Rs2Inventory.waitForInventoryChanges(2500);
                if (!Rs2Bank.hasItem(LAW_RUNE)) {
                    JOptionPane.showMessageDialog(null, "The Script has Shut Down due to no Law Runes in bank.");
                    moarShutDown();
                    shutdown();
                }
                sleep(500, 1000);
                Rs2Bank.withdrawX(MIND_RUNE, 750);
                Rs2Inventory.waitForInventoryChanges(2500);
                if (!Rs2Bank.hasItem(MIND_RUNE)) {
                    JOptionPane.showMessageDialog(null, "The Script has Shut Down due to no Mind Runes in bank.");
                    moarShutDown();
                    shutdown();
                }
                sleep(900, 2100);
                Rs2Bank.withdrawX(DEATH_RUNE, 30);
                Rs2Inventory.waitForInventoryChanges(2500);
                Rs2Bank.withdrawOne(ENERGY_POTION4);
                Rs2Inventory.waitForInventoryChanges(2500);
                for (int id : strengthPotionIds) {
                    if (Rs2Bank.hasItem(id)) {
                        Rs2Bank.withdrawOne(id);
                        break;
                    }
                }


                if (!Rs2Equipment.isWearing(GILDED_CHAINBODY)) {
                    OutfitHelper.equipOutfit(OutfitHelper.OutfitType.GILDED_MOSS_MAGE);
                    //equipItems();

                    CombatMode mode = mossKillerConfig.combatMode();

                    if (Objects.requireNonNull(mode) == CombatMode.FIGHT) {
                        sleep(1500, 2500);
                        if (Microbot.getClient().getRealSkillLevel(Skill.RANGED) >= 30) {
                            Rs2Bank.withdrawX(ADAMANT_ARROW, 75);
                        }
                        Rs2Inventory.waitForInventoryChanges(2500);
                        Rs2Inventory.interact(ADAMANT_ARROW, "Wield");
                        Rs2Inventory.waitForInventoryChanges(2500);
                        if (Microbot.getClient().getRealSkillLevel(Skill.RANGED) >= 30) {
                            Rs2Bank.withdrawOne(MAPLE_SHORTBOW);
                            Rs2Inventory.waitForInventoryChanges(2500);
                            Rs2Bank.withdrawOne(MAPLE_LONGBOW);
                        }
                        sleep(1500, 2500);
                        //Rs2Bank.withdrawOne(RUNE_SCIMITAR);
                        //Rs2Inventory.waitForInventoryChanges(2500);
                        for (int id : strengthPotionIds) {
                            if (Rs2Inventory.contains(id)) {
                                hasStrengthPotion = true;
                                break;
                            }
                        }

                        if (!hasStrengthPotion) for (int id : strengthPotionIds) {
                            if (Rs2Bank.hasItem(id)) {
                                Rs2Bank.withdrawOne(id);
                                break;
                            }
                        }

                    } else if (Objects.requireNonNull(mode) == LURE) {

                        sleep(1500, 2500);
                        Rs2Bank.withdrawX(ENERGY_POTION4, 2);
                        sleep(1500, 2500);
                    }
                }

                if (Rs2Inventory.containsAll(new int[]{AIR_RUNE, LAW_RUNE, MIND_RUNE})) {
                    if (Rs2Bank.closeBank()) {
                        state = MossKillerState.CASTLE_WARS_TO_FEROX;
                    }
                }
            } else Rs2Bank.depositAll();
            sleep(500, 1000);
        } else {Rs2Bank.walkToBank1();}

        mossKillerPlugin.dead = false;
    }

    public void setAutocastFireStrike() {

        Rs2Combat.setAutoCastSpell(FIRE_STRIKE, true);

        /*
        if (Rs2Tab.getCurrentTab() != InterfaceTab.COMBAT) {
            // Step 1: Open the Combat tab
            if (!Rs2Widget.clickWidget(COMBAT_TAB_WIDGET_ID)) {
                System.out.println("Failed to click the Combat tab.");
                return;
            }
        }
        // Optional: Wait for the combat tab to fully load
        sleep(1500); // Adjust this timing as needed or use a condition to validate

        if (!config.forceDefensive()) {
            if (Microbot.getClient().getRealSkillLevel(DEFENCE) < 60 && Rs2Widget.isWidgetVisible(ComponentID.COMBAT_DEFENSIVE_SPELL_BOX)) {
                if (!Rs2Widget.clickWidget(CHOOSE_SPELL_DEFENSIVE_WIDGET_ID)) {
                    System.out.println("Failed to click 'Choose spell Defensive' widget.");
                    return;
                }
            } else if (Microbot.getClient().getRealSkillLevel(DEFENCE) >= 60 && Rs2Widget.isWidgetVisible(ComponentID.COMBAT_SPELL_BOX)) {
                if (!Rs2Widget.clickWidget(CHOOSE_SPELL_WIDGET_ID)) {
                    System.out.println("Failed to click 'Choose spell' widget.");
                    return;
                }
            }

        } else if (config.forceDefensive()) {
            if (Rs2Widget.isWidgetVisible(ComponentID.COMBAT_DEFENSIVE_SPELL_BOX)) {
                if (!Rs2Widget.clickWidget(CHOOSE_SPELL_DEFENSIVE_WIDGET_ID)) {
                    System.out.println("Failed to click 'Choose spell Defensive' widget.");
                    return;
                }
            }

        }
        sleep(1500);

        Rs2Widget.clickWidget("Fire Strike", true);

        sleep(1500);
*/
    }

    public void setAutocastDeathBlast() {
        // Step 1: Open the Combat tab
        if (!Rs2Widget.clickWidget(COMBAT_TAB_WIDGET_ID)) {
            System.out.println("Failed to click the Combat tab.");
            return;
        }
        // Optional: Wait for the combat tab to fully load
        sleep(600); // Adjust this timing as needed or use a condition to validate

        if (Microbot.getClient().getRealSkillLevel(DEFENCE) < 60) {
            if (!Rs2Widget.clickWidget(CHOOSE_SPELL_DEFENSIVE_WIDGET_ID)) {
                System.out.println("Failed to click 'Choose spell Defensive' widget.");
                return;
            }
        } else if (Microbot.getClient().getRealSkillLevel(DEFENCE) >= 60) {
            if (!Rs2Widget.clickWidget(CHOOSE_SPELL_WIDGET_ID)) {
                System.out.println("Failed to click 'Choose spell' widget.");
                return;
            }
        }

        sleep(600);

        Rs2Widget.clickWidget("Wind Blast");

        sleep(800);

    }

    public void handleFerox() {
        Microbot.log(String.valueOf(state));

        if (!scheduledFuture.isDone()) {
            sleep(600);
            state = MossKillerState.PKER;
        }

        sleep(1200);
        if (!Rs2Equipment.isWearing()) {
            state = MossKillerState.BANK;
        }

        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        //if not within 8 tiles and Y < 5000 walk to CASTLE_WARS
        if (Rs2Walker.getDistanceBetween(playerLocation, CASTLE_WARS) > 6
                && playerLocation.getY() < 5000
                && playerLocation.getY() > 3100
                && !TOTAL_FEROX_ENCLAVE.contains(playerLocation)
                && !Rs2Inventory.contains(FOOD)) {
            Microbot.log("Should be walking to castle wars entry");
            Rs2Walker.walkTo(CASTLE_WARS);
            //toggleRunEnergyOff();
            if (mossKillerPlugin.playerJammed()) {
                teleportAndStopWalking();
                sleep(1200);
                Rs2Walker.setTarget(null);
                sleep(1200);
                Rs2Walker.restartPathfinding(playerLocation, CASTLE_WARS);
            }
            return;
        }
        // TO DO if helmet and cape is equipped, unequip helmet and cape
        if (Rs2Walker.getDistanceBetween(playerLocation, CASTLE_WARS) <= 6) {
            Random random = new Random();
            boolean interactWithSaradominist = random.nextBoolean(); // Generates true or false randomly
            int[] wizardHats = {
                    BLUE_WIZARD_HAT,
                    BLUE_WIZARD_HAT_G,
                    BLUE_WIZARD_HAT_T,
                    BLACK_WIZARD_HAT_G,
                    BLACK_WIZARD_HAT_T
            };

            for (int hat : wizardHats) {
                if (Rs2Equipment.isWearing(hat)) {
                    sleep(1200,1800);
                    Rs2Equipment.unEquip(hat);
                    sleep(1200,1800);
                    break; // stop after unequipping the worn one
                }
            }

            if (interactWithSaradominist) {
                Rs2Npc.interact("Saradominist recruiter", "Join Castle Wars");
            } else {
                Rs2Npc.interact("Zamorakian recruiter", "Join Castle Wars");
            }

            sleep(4000, 5000);

            return;
        }
        if (playerLocation.getY() > 9000) {
            Rs2GameObject.interact("Portal", "Exit");
            sleep(4000, 5000);
            return;
        }

        if (!Rs2Inventory.hasItem(FOOD)) {
            Rs2GameObject.interact("Bank chest", "Use");
            sleepUntil(Rs2Bank::isOpen, 10000);
            if (Rs2Bank.isOpen()) {
                //withdraw 20 food, close bank
                Rs2Bank.withdrawX(FOOD, 17);
                Rs2Inventory.waitForInventoryChanges(2500);
                Rs2Bank.withdrawX(NATURE_RUNE, 10);
                Rs2Inventory.waitForInventoryChanges(2500);
                if (!Rs2Bank.hasItem(FOOD)) {
                    JOptionPane.showMessageDialog(null, "The Script has Shut Down due to no FOOD in bank.");
                    shutdown();
                }
                if (Microbot.getClient().getRealSkillLevel(Skill.RANGED) >= 30 &&
                        !Rs2Equipment.isWearing(ADAMANT_ARROW)) {
                    sleep(1500, 2500);
                    Rs2Inventory.interact(ADAMANT_ARROW, "Wield");
                }

                sleep(1500, 2500);
                if (Microbot.getClient().getRealSkillLevel(Skill.RANGED) >= 30 &&
                        !Rs2Inventory.contains(MAPLE_SHORTBOW)) {
                    Rs2Bank.withdrawOne(MAPLE_SHORTBOW);
                }
                sleep(1500, 2500);

                if (Microbot.getClient().getRealSkillLevel(Skill.RANGED) >= 30 &&
                        !Rs2Inventory.contains(MAPLE_LONGBOW)) {
                    Rs2Bank.withdrawOne(MAPLE_LONGBOW);
                }
                //sleep(1500, 2500);
                //Rs2Bank.withdrawOne(RUNE_SCIMITAR);
                //Rs2Inventory.waitForInventoryChanges(2500);
                if(Rs2Bank.hasItem(BRYOPHYTAS_STAFF)) {
                    Rs2Bank.withdrawOne(BRYOPHYTAS_STAFF);
                    Rs2Inventory.waitForInventoryChanges(2500);
                }
                ensureBestWizardHatEquipped();

                if (!Rs2Equipment.isWearing(STAFF_OF_FIRE)) {
                    Rs2Bank.withdrawOne(STAFF_OF_FIRE);
                    Rs2Inventory.waitForInventoryChanges(2500);
                    Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");
                    Rs2Inventory.waitForInventoryChanges(2500);
                }

                if (Rs2Inventory.hasItemAmount(MAPLE_SHORTBOW, 2)) {
                    sleep(900, 1200);
                    Rs2Bank.depositOne(MAPLE_SHORTBOW);
                    sleep(900, 1200);
                }

                if (Rs2Inventory.hasItemAmount(RUNE_SCIMITAR, 2)) {
                    sleep(900, 1200);
                    Rs2Bank.depositAll(RUNE_SCIMITAR);
                    sleep(900, 1200);

                }
                sleep(900, 1200);
                Rs2Bank.closeBank();
                sleep(300, 1200);
            }
        }
        sleep(600, 1200);
        if (playerLocation.getY() < 3100) {
            Microbot.log("trying to open big door");
            Rs2GameObject.interact(30387);
            sleepUntil(Rs2Dialogue::isInDialogue);
            if (Rs2Dialogue.isInDialogue()) {
                Rs2Dialogue.keyPressForDialogueOption("Yes");
            }
            sleepUntil(() -> FEROX_TELEPORT_AREA.contains(playerLocation));
        } else if (playerLocation.getY() > 3100) {
            state = MossKillerState.WALK_TO_MOSS_GIANTS;
        }

        if (FEROX_TELEPORT_AREA.contains(playerLocation)) {
            Microbot.log("in ferox teleport area");
        }
        state = MossKillerState.WALK_TO_MOSS_GIANTS;
    }

    private void ensureBestWizardHatEquipped() {

        int[] wizardHatsPriority = {
                BLUE_WIZARD_HAT_G,
                BLUE_WIZARD_HAT_T,
                BLACK_WIZARD_HAT_G,
                BLACK_WIZARD_HAT_T,
                BLUE_WIZARD_HAT
        };

        // 1. If already wearing the best possible hat, do nothing
        for (int hat : wizardHatsPriority) {
            if (Rs2Equipment.isWearing(hat)) {
                return;
            }
        }

        // 2. Try to wear from inventory (priority order)
        for (int hat : wizardHatsPriority) {
            if (Rs2Inventory.contains(hat)) {
                Rs2Inventory.interact(hat, "Wear");
                Rs2Inventory.waitForInventoryChanges(2500);
                return;
            }
        }

        // 3. Withdraw from bank and wear (priority order)
        for (int hat : wizardHatsPriority) {
            if (Rs2Bank.hasItem(hat)) {
                Rs2Bank.withdrawOne(hat);
                Rs2Inventory.waitForInventoryChanges(2500);
                Rs2Inventory.interact(hat, "Wear");
                Rs2Inventory.waitForInventoryChanges(2500);
                return;
            }
        }
    }



    public void walkToVarrockWestBank() {

        Microbot.log(String.valueOf(state));

        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        //Microbot.log("we are at" + playerLocation + TOTAL_FEROX_ENCLAVE.contains(playerLocation));

        if (!scheduledFuture.isDone()) {
            sleep(600);
            if (isRunning()) {
                state = MossKillerState.PKER;
            }
        }

        if (MOSS_GIANT_AREA.contains(playerLocation)) {
            state = MossKillerState.FIGHT_MOSS_GIANTS;
        }

        if (isRunning()) {

            if (Rs2Inventory.hasItemAmount(MIND_RUNE, 750)
                    && !Rs2Inventory.hasItem(FOOD)) {
                state = MossKillerState.CASTLE_WARS_TO_FEROX;
                return;
            }

            if (Rs2Inventory.containsAll(new int[]{AIR_RUNE, LAW_RUNE, FOOD, MIND_RUNE})
                    && !Rs2Inventory.contains(MOSSY_KEY)
                    && TOTAL_FEROX_ENCLAVE.contains(playerLocation)) {
                state = MossKillerState.WALK_TO_MOSS_GIANTS;
                return;
            }

            if (TOTAL_FEROX_ENCLAVE.contains(playerLocation)) {
                state = MossKillerState.BANK;
            }

            if (Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) > 6
                    || Rs2Player.isTeleBlocked()
                    || getWildernessLevelFrom(Rs2Player.getWorldLocation()) <= 20) {
                Rs2Bank.walkToBank(BankLocation.VARROCK_WEST);
            }

            if (Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) <= 6) {
                Microbot.log("distance to varrock west bank  < 6, bank now");
                state = MossKillerState.BANK;
            }
        }
    }

    public void varrockTeleport() {
        Microbot.log(String.valueOf(state));
        Rs2Player.eatAt(70, false);
        sleep(200);
        if (!scheduledFuture.isDone()) {
            state = MossKillerState.PKER;
        }
        sleep(200);
        if (mossKillerPlugin.currentTarget != null) {
            state = MossKillerState.PKER;
        }

        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if (Rs2Inventory.containsAll(AIR_RUNE, LAW_RUNE, MIND_RUNE, FOOD)
                && !Rs2Inventory.contains(MOSSY_KEY) && MOSS_GIANT_AREA.contains(playerLocation)
                && mossKillerPlugin.currentTarget == null) {
            state = MossKillerState.FIGHT_MOSS_GIANTS;
        }

        sleep(600);

        if (playerLocation.getY() > 3500) {
            if (mossKillerPlugin.isTeleblocked()) {
                walkToAndTeleportZEROWILD();
                return;
            }
            walkToAndTeleport();
        } else {
            state = MossKillerState.WALK_TO_BANK;
        }

        Microbot.log(String.valueOf(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE)));
        if (mossKillerPlugin.currentTarget != null && mossKillerPlugin.currentTarget.getCombatLevel() > 87) {
            state = MossKillerState.PKER;
        }

        Rs2Player.eatAt(70,false);
        sleep(1000, 2000);

        if (Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) <= 10 && playerLocation.getY() < 5000) {
            //toggleRunEnergyOff();
            state = MossKillerState.WALK_TO_BANK;
            return;
        }

        if (Rs2Inventory.contains(MOSSY_KEY)) {
            walkToAndTeleport();
        }

        Rs2Player.eatAt(70, false);
        sleep(2000, 3500);
        Rs2Player.eatAt(70, false);

    }


    public void walkToAndTeleport() {

        // Get the player's current location
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Walk to TWENTY_WILD if not already close enough
        if (Rs2Walker.getDistanceBetween(playerLocation, TWENTY_WILD) > 5
                && !MOSS_GIANT_AREA.contains(playerLocation)
                || Rs2Inventory.contains(MOSSY_KEY)
                || BreakHandlerScript.breakIn <= 120
                || !Rs2Inventory.contains(FOOD)
                || !Rs2Inventory.hasItemAmount(MIND_RUNE, 15)) {
            if (scheduledFuture.isDone() && !Rs2Inventory.hasItemAmount(FOOD, 17)) { // Only initiate if not already walking to Twenty Wild
                handleAsynchWalk("Twenty Wild");
            }
            if (Rs2Walker.getDistanceBetween(playerLocation, TWENTY_WILD) < 5) {
                teleportAndStopWalking();
            if (Rs2Inventory.hasItemAmount(FOOD, 17)) {
                state = MossKillerState.WALK_TO_MOSS_GIANTS;
            }
            Microbot.log("Hitting Return");
            return;
        }
        }

        // Check if the player has teleported (Y-coordinate condition)
        if (playerLocation.getY() < 3500) {
            Microbot.log("Teleport successful.");
            state = MossKillerState.WALK_TO_BANK;
        }
    }

    public void walkToAndTeleportZEROWILD() {

        // Get the player's current location
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Walk to TWENTY_WILD if not already close enough
        if (Rs2Walker.getDistanceBetween(playerLocation, TWENTY_WILD) > 3) {
            if (scheduledFuture.isDone()) { // Only initiate if not already walking to Zero Wild
                handleAsynchWalk("Zero Wild");
            }
            return;
        }

        // Check if the player has teleported (Y-coordinate condition)
        if (playerLocation.getY() < 3500) {
            Microbot.log("Teleport successful.");
            state = MossKillerState.WALK_TO_BANK;
        }
    }

    private void teleportAndStopWalking() {
        if (Rs2Inventory.containsAll(AIR_RUNE, LAW_RUNE) && getWildernessLevelFrom(Rs2Player.getWorldLocation()) <= 20) {
            if (Rs2Inventory.contains(STAFF_OF_FIRE)) {
                sleep(600);
                Rs2Inventory.interact(STAFF_OF_FIRE, "Wield");
            }
            sleep(600);
            Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);
            Microbot.log("Script has cast Varrock Teleport");
        } else if (!Rs2Inventory.containsAll(AIR_RUNE, LAW_RUNE)) {
            Microbot.log("Missing runes for teleportation.");
            state = MossKillerState.WALK_TO_BANK;
        }
    }

    public void toggleRunEnergyOn() {
        if (!Rs2Player.isRunEnabled() && Rs2Player.getRunEnergy() > 5) {
            Rs2Player.toggleRunEnergy(true);
        }
    }

    private void prepareSchedulerStart() {
        if (isWearingOutfit(OutfitHelper.OutfitType.MOSS_MAGE)) {
            Microbot.log("Already wearing MOSS_MAGE outfit. Skipping outfit pre-prep.");
            return;
        }
        Rs2Bank.walkToBank();
        Rs2Bank.openBank();
        sleepUntil(Rs2Bank::isOpen);
        Rs2Bank.depositAll();
        depositEquipment();
        Rs2Bank.closeBank();
        Rs2Bank.walkToBank(BankLocation.FEROX_ENCLAVE);
    }

    public static boolean isWearingOutfit(OutfitHelper.OutfitType outfitType) {
        String[] items = outfitType.getOutfitItems();
        for (String item : items) {
            if (!Rs2Equipment.isWearing(item)) {
                return false;
            }
        }
        return true;
    }

    public void toggleRunEnergyOff() {
        if (Rs2Player.isRunEnabled() && Rs2Player.getRunEnergy() > 0) {
            Rs2Player.toggleRunEnergy(false);
        }
    }

}
