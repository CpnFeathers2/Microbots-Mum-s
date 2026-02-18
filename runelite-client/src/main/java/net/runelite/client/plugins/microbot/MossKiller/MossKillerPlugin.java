package net.runelite.client.plugins.microbot.MossKiller;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.MossKiller.Enums.CombatMode;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static net.runelite.api.GraphicID.SNARE;
import static net.runelite.api.GraphicID.SPLASH;
import static net.runelite.api.HeadIcon.*;
import static net.runelite.api.ItemID.RUNE_SCIMITAR;

@PluginDescriptor(
        name = PluginDescriptor.Bee + "Moss Killer",
        description = "Bee & Mntn's Moss Killer",
        tags = {"Keys", "Bryophyta", "Mntn", "Bee", "Moss Giants", "F2p"},
        enabledByDefault = false
)
@Slf4j
public class MossKillerPlugin extends Plugin {
    @Inject
    private MossKillerConfig config;

    @Provides
    MossKillerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MossKillerConfig.class);
    }

    @Inject
    private Client client;
    private ScheduledFuture<?> mainScheduledFuture;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MossKillerOverlay mossKillerOverlay;
    @Inject
    private WildyKillerScript wildyKillerScript;
    @Inject
    private WildySaferScript wildySaferScript;
    @Inject
    private MossKillerScript exampleScript;
    private boolean worldHopFlag = false;
    @Getter
    private int deathCounter = 0;
    private boolean isDeathProcessed = false;

    private boolean isTeleblocked = false;
    private boolean lobsterEaten = false;
    public boolean noKnife = false;
    private boolean autoCastMinds = false;
    private int tickCount = 0;
    private int mindsTickCount = 0;

    // Key is the NAME (String), not the Object. This persists through game updates.
    private final Map<String, Integer> attackerTickMap = new HashMap<>();
    //private final Map<Rs2PlayerModel, Integer> attackerTickMap = new HashMap<>();
    private static final int MIN_TICKS_TO_TRACK = 2;
    private static final String MOSS_GIANT_NAME = "Moss Giant";

    private static boolean isSnared = false;
    private int snareTickCounter = 0;


    public static WorldPoint bryoTile = null; // Stores the southwest tile when Bryophyta dies

    private boolean useWindBlast = false;
    private boolean useMelee = false;
    private boolean useRange = false;

    private int consecutiveHitsplatsMain = 0;
    private boolean runeScimitar = false;

    private final Object targetLock = new Object();
    public Rs2PlayerModel currentTarget = null;

    private boolean hideOverlay;
    public boolean dead = false;
    @Getter
    private boolean defensive;

    private int projectileCount = 0;
    public boolean windStrikeflag = false;

    @Getter
    private boolean superNullTarget = false;

    private boolean isJammed;
    private boolean superJammed;
    private Set<String> migratedThisTick = new HashSet<>();

    private boolean hitsplatIsTheirs = false;

    private int hitsplatSetTick = -1; // Initialize to an invalid tick value
    private long lastHitsplatTimeMain = 0;
    // Tracks the nearby player condition
    private boolean isPlayerNearby = false;
    private long lastTargetSeenTime = 0;
    private static final long TARGET_GRACE_PERIOD_MS = 5000; // 5 second grace period
    private Rs2PlayerModel lastKnownTarget = null; // Store last valid target reference


    @Override
    protected void startUp() throws AWTException, PluginInstantiationException {
        if (overlayManager != null) {
            overlayManager.add(mossKillerOverlay);
        }
        Microbot.useStaminaPotsIfNeeded = false;
        Microbot.enableAutoRunOn = false;
        hideOverlay = config.isHideOverlay();
        toggleOverlay(hideOverlay);
        if (!config.wildy() && !config.wildySafer()) {
            exampleScript.run(config);
        }
        if (config.wildy() && !config.wildySafer()) {
            wildyKillerScript.run(config);
            wildyKillerScript.handleAsynchWalk("Start-up");
        }
        if (config.wildySafer() && !config.wildy()) {
            wildySaferScript.run(config);
            wildySaferScript.handleAsynchWalk("Start-up");
            System.out.println("running wildy safer script");
        }
    }

    boolean isSamePlayer(Rs2PlayerModel p1, Rs2PlayerModel p2) {
        // 1. Basic Null Checks
        if (p1 == null || p2 == null) return false;

        // 2. Memory Address Match (Fastest & 100% accurate if true)
        if (p1 == p2) return true;

        // 3. Name Match (The Source of Truth)
        String n1 = p1.getName();
        String n2 = p2.getName();

        if (n1 != null && n2 != null && !n1.isEmpty() && !n2.isEmpty()) {
            return n1.equals(n2);
        }

        // 4. Fallback: Last Resort
        // If names are loading/null, we assume they are the same person if Combat Level matches.
        // We do NOT check distance because movement is erratic.
        // Risk: We might confuse two Lv 126s standing on top of each other.
        // Benefit: We won't lose our target tick count during a cache glitch.
        return p1.getCombatLevel() == p2.getCombatLevel();
    }

    /*
    /**
     * Migrates tick count from old player object to new player object.
     * This MUST happen BEFORE any clearing/resetting to maintain continuous tracking.
     */
    /*
    private void migrateTickCount(Rs2PlayerModel oldPlayer, Rs2PlayerModel newPlayer) {
        // FIX: Use the NAME as the key, not the object HashCode.
        // If we migrated "W Y L D" once this tick, we don't need to do it again.
        String playerKey = newPlayer.getName();

        // Only migrate ONCE per player NAME per tick
        if (migratedThisTick.contains(playerKey)) {
            return; // Already migrated this tick - ABORT
        }
        migratedThisTick.add(playerKey);

        Integer oldTicks = attackerTickMap.get(oldPlayer);
        if (oldTicks != null && oldTicks > 0) {
            // Log commented out to save CPU
            // Microbot.log(String.format("MIGRATING %d ticks: %s", oldTicks, newPlayer.getName()));

            attackerTickMap.put(newPlayer, oldTicks);
            attackerTickMap.remove(oldPlayer);

            if (currentTarget != null && isSamePlayer(currentTarget, oldPlayer)) {
                currentTarget = newPlayer;
            }
        }
    }

     */

    public Rs2PlayerModel getCurrentTarget() {
        synchronized (targetLock) {
            return currentTarget;
        }
    }

    public void setCurrentTarget(Rs2PlayerModel target) {
        synchronized (targetLock) {
            currentTarget = target;
            System.out.println("Target set to: " + (target != null ? target.getName() : "null"));
        }
    }
    @Getter
    private int mossyKeyCounter = 0;
    private long lastMossyKeyTime = 0;
    private static final long MOSSY_KEY_COOLDOWN = 30000; // 30 seconds in milliseconds

    public void incrementMossyKeyCounter() {
        long currentTime = System.currentTimeMillis();

        // Check if enough time has passed since last increment
        if (currentTime - lastMossyKeyTime >= MOSSY_KEY_COOLDOWN) {
            mossyKeyCounter++;
            lastMossyKeyTime = currentTime;
            Microbot.log("Mossy key picked up! Total: " + mossyKeyCounter);
        } else {
            long remainingCooldown = (MOSSY_KEY_COOLDOWN - (currentTime - lastMossyKeyTime)) / 1000;
            Microbot.log("Mossy key cooldown active. " + remainingCooldown + " seconds remaining.");
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("Take") &&
                event.getMenuTarget().contains("Mossy key")) {
            incrementMossyKeyCounter(); // Remove the duplicate log from here
        }
    }


    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        if (actorDeath.getActor() == Microbot.getClient().getLocalPlayer()) {
            if (!dead) type();
        }
    }

    private void type() {
        Rs2Keyboard.typeString("gg");
        Rs2Keyboard.enter();
        dead = true;
    }

    @Subscribe
    public void onConfigChanged(final ConfigChanged event) {

        if (!event.getGroup().equals(MossKillerConfig.configGroup)) {
            return;
        }

        if (event.getKey().equals(MossKillerConfig.hideOverlay)) {
            hideOverlay = config.isHideOverlay();
            toggleOverlay(hideOverlay);
        }

    }

    public void updateTargetByName() {
        if (currentTarget == null || currentTarget.getPlayer() == null) return;

        String name = currentTarget.getName();

        // Use Rs2Player.getPlayers with a predicate to match the name
        Optional<Rs2PlayerModel> updatedTarget = Rs2Player.getPlayers(
                p -> p.getName() != null && p.getName().equals(name)
        ).findFirst();

        if (updatedTarget.isPresent()) {
            currentTarget = updatedTarget.get();
            Microbot.log("Refreshed target reference for: " + name);
        } else {
            Microbot.log("Target " + name + " not found in current player list.");
        }
    }


    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && config.wildySafer()) {
            if (mainScheduledFuture == null || mainScheduledFuture.isCancelled() || mainScheduledFuture.isDone()) {
                if (!Rs2Player.isMoving()) { Microbot.log("GameState is LOGGED_IN and script was idle. Restarting run loop..."); wildySaferScript.run(config);}
            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (config.wildy()) {
            if (event.getVarbitId() == Varbits.TELEBLOCK) {
                int teleblockValue = event.getValue(); // Get the current value of the teleblock varbit
                isTeleblocked = teleblockValue > 100;
            }
        }
    }

    public boolean isAttackStyleStrength() {
        int attackStyle = Microbot.getClient().getVarpValue(VarPlayerID.COM_MODE);
        return attackStyle == 1;
    }


    public Boolean getAttackStyle() {
        // Check current attack style
        int attackStyle = Microbot.getClient().getVarpValue(VarPlayerID.COM_MODE);

        if (attackStyle == 4) {
            return true; // Return true if attackStyle is exactly 4
        } else if (attackStyle < 4) {
            return false; // Return false if attackStyle is less than 4
        }

        return null; // Optional: Return null for unexpected cases
    }

    private void toggleOverlay(boolean hideOverlay) {
        if (overlayManager != null) {
            boolean hasOverlay = overlayManager.anyMatch(ov -> ov.getName().equalsIgnoreCase(MossKillerOverlay.class.getSimpleName()));

            if (hideOverlay) {
                if (!hasOverlay) return;

                overlayManager.remove(mossKillerOverlay);
            } else {
                if (hasOverlay) return;

                overlayManager.add(mossKillerOverlay);
            }
        }
    }

    public void resetTarget() {
        currentTarget = null;
    }

    public boolean hasRuneScimitar() {
        return runeScimitar;
    }

    public boolean isTeleblocked() {
        return isTeleblocked;
    }

    public boolean shouldHopWorld() {
        return worldHopFlag;
    }

    public boolean useWindBlast() {
        return useWindBlast;
    }

    public boolean useMelee() {
        return useMelee;
    }

    public boolean useRange() {
        return useRange;
    }

    public boolean playerJammed() {
        return isJammed;
    }

    public boolean isSuperJammed() {
        return superJammed;
    }

    public void resetWorldHopFlag() {
        worldHopFlag = false;
    }

    public void targetPrayers() {
        if (currentTarget != null) {

            // Check and act based on their prayer overhead
            if (currentTarget.getOverheadIcon() == RANGED) {
                if (isSnared) {
                    useWindBlast = true; // Use wind blast if snared
                    useMelee = false;    // Don't use melee
                } else {
                    useWindBlast = false;
                    useMelee = true; // Use melee if not snared
                }
                useRange = false; // No ranged in this case

            } else if (currentTarget.getOverheadIcon() == MELEE) {
                useRange = true;  // Use ranged attacks
                useWindBlast = false;
                useMelee = false;

            } else if (currentTarget.getOverheadIcon() == MAGIC) {
                if (isSnared || shouldPrioritizeRange()) {
                    useRange = true;  // Use ranged if snared or prioritize range
                    useMelee = false; // No melee if snared or prioritize range
                } else {
                    useRange = false;
                    useMelee = true; // Use melee if not snared
                }
                useWindBlast = false; // No wind blast in this case
            }
        }
    }

    public boolean shouldPrioritizeRange() {
        int strength = Microbot.getClient().getRealSkillLevel(Skill.STRENGTH);
        int ranged = Microbot.getClient().getRealSkillLevel(Skill.RANGED);
        return strength <= 65 && ranged >= 55;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (config.wildy()) {

            checkNearbyPlayers();

            if (hitsplatIsTheirs && hitsplatSetTick != -1) {
                System.out.println("hitsplat counter is more than -1");
                if (client.getTickCount() - hitsplatSetTick >= 4) {
                    hitsplatIsTheirs = false; // Reset the flag
                    hitsplatSetTick = -1;     // Reset the tracker
                }
            }

            if (client.getLocalPlayer() != null) {
                int currentHp = client.getLocalPlayer().getHealthRatio();

                // Player has 0 HP, and this death has not been processed yet
                if (currentHp == 0 && !isDeathProcessed) {
                    worldHopFlag = true; // Notify the script to hop worlds
                    deathCounter++; // Increment the death counter
                    isDeathProcessed = true; // Mark this death as processed
                }

                // Reset the death processed flag when the player's HP is greater than 0
                if (currentHp > 0) {
                    isDeathProcessed = false;
                }
            }

            targetPrayers();

            runeScimitar = Rs2Equipment.isWearing(RUNE_SCIMITAR);

            if (isSnared) {
                snareTickCounter++;
                if (snareTickCounter >= 16) {  // Reset after 16 ticks
                    isSnared = false;
                    snareTickCounter = 0;
                    System.out.println("Snare effect ended.");
                }
            }

            int health = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS); // Assuming Rs2Player has a method to get health

            if (health == 0) {
                stopWalking();
            }

            if (lobsterEaten) {
                tickCount++;

                if (tickCount >= 3) {
                    lobsterEaten = false; // Reset the boolean after 3 ticks
                    tickCount = 0; // Reset the counter
                    System.out.println("3 ticks elapsed. eating attack delay over.");
                    // Set your script's boolean or trigger action here
                }
            }

            if (client.getTickCount() % 5 == 0) {
                String targetStatus = (currentTarget != null) ? currentTarget.getName() : "NONE";
                int threatCount = attackerTickMap.size();
                System.out.println(String.format("[TICK %d] Target: %s | Active Threats: %d",
                        client.getTickCount(), targetStatus, threatCount));
            }

            if (config.combatMode() == CombatMode.FIGHT) {
                //note to self - this is where target is set
                trackAttackers();
            }

            int attackStyle = client.getVarpValue(VarPlayerID.COM_MODE);
            defensive = attackStyle == 3;

            if (currentTarget == null) {
                tickCount++;

                if (tickCount >= 9) {
                    superNullTarget = true;
                    //System.out.println("9 ticks elapsed. target probably properly gone.");
                }
            } else {
                superNullTarget = false;
            }
        }

        if (config.wildy() || config.wildySafer()) {
            if (!Rs2Player.isMoving() && !Rs2Player.isAnimating()) {
                tickCount++;

                if (tickCount >= 50) {
                    isJammed = true;
                }

                if (tickCount >= 500) {
                    Microbot.log("500 ticks without moving or animating");
                    if (BreakHandlerScript.isBreakActive()) {
                        tickCount = 0;
                        superJammed = false;
                        Microbot.superJammed = false;
                    }
                    superJammed = true;
                    Microbot.superJammed = true;
                }

            } else {
                tickCount = 0;
                isJammed = false;
                superJammed = false;
                Microbot.superJammed = false;
            }

            if (client.getLocalPlayer() != null) {
                int currentHp = client.getLocalPlayer().getHealthRatio();

                // Player has 0 HP, and this death has not been processed yet
                if (currentHp == 0 && !isDeathProcessed) {
                    worldHopFlag = true; // Notify the script to hop worlds
                    isDeathProcessed = true; // Mark this death as processed
                }

                // Reset the death processed flag when the player's HP is greater than 0
                if (currentHp > 0) {
                    isDeathProcessed = false;
                }
            }
        }

        if (!config.wildy() && !config.wildySafer()) {
            NPC bryophyta = findBryophyta();

            // Check if Bryophyta's HP is 0
            if (bryophyta != null && bryophyta.getHealthRatio() == 0) {
                bryoTile = getBryophytaWorldLocation(bryophyta);
            }
        }

        if (autoCastMinds) {
            mindsTickCount++;
            if (mindsTickCount == 100) {
                Microbot.log("100 ticks since can't cast chaos, turning autocastminds to false");
                autoCastMinds = false;
            }
        }

        /// reserved for wildysafer anti-pk logic ///

        /*if (config.wildySafer()
                && !wildySaferScript.fired
                && Rs2Player.getWorldLocation().getY() > 3700
                && Rs2Player.getPlayersInCombatLevelRange() != null) {
            wildySaferScript.checkCombatAndRunToBank();
        }*/
    }


    public NPC findBryophyta() {
        return client.getNpcs().stream()
                .filter(npc -> npc.getName() != null && npc.getName().equalsIgnoreCase("Bryophyta"))
                .findFirst()
                .orElse(null);
    }

    private WorldPoint getBryophytaWorldLocation(NPC bryophyta) {
        if (bryophyta == null) {
            return null;
        }

        if (Microbot.getClient().getTopLevelWorldView().getScene().isInstance()) {
            LocalPoint l = LocalPoint.fromWorld(Microbot.getClient().getTopLevelWorldView(), bryophyta.getWorldLocation());
            return WorldPoint.fromLocalInstance(Microbot.getClient(), l);
        } else {
            return bryophyta.getWorldLocation();
        }
    }

    private boolean anyPlayerInteracting() {
        return Rs2Player.getPlayersInCombatLevelRange().stream()
                .anyMatch(p -> p.getInteracting() == Microbot.getClient().getLocalPlayer());
    }


    private void trackAttackers() {
        migratedThisTick.clear();
        Player localPlayer = Microbot.getClient().getLocalPlayer();

        // 1. EMERGENCY CLEAR: If we died or left wilderness
        if (localPlayer.getHealthRatio() == 0 || Rs2Player.getWorldLocation().getY() < 3520) {
            if (currentTarget != null) {
                Microbot.log("Player died or left wilderness - clearing data");
                attackerTickMap.clear();
                resetTarget();
                return;
            }
        }

        // 2. JAMMED CHECK
        if (superJammed) {
            attackerTickMap.clear();
            resetTarget();
            return;
        }

        // 3. TARGET MAINTENANCE
        // Check if current target is still valid/visible
        if (currentTarget != null) {
            Optional<Rs2PlayerModel> visibleTarget = Rs2Player.getPlayersInCombatLevelRange().stream()
                    .filter(p -> p.getName() != null && p.getName().equals(currentTarget.getName()))
                    .findFirst();

            if (visibleTarget.isPresent()) {
                currentTarget = visibleTarget.get(); // Update object reference
                lastTargetSeenTime = System.currentTimeMillis();
            } else {
                // They are invisible. Do NOT reset immediately (Guerilla Logic).
                // Only reset if they have been gone for too long (e.g. 5 seconds)
                if (System.currentTimeMillis() - lastTargetSeenTime > TARGET_GRACE_PERIOD_MS) {
                    Microbot.log("Target lost for > 5s. Dropping lock.");
                    resetTarget();
                }
            }
        }

        // 4. INVISIBLE PLAYER HANDLING (Decay)
        // We must decay ticks for players we cannot see, so we don't hold grudges forever.
        List<Rs2PlayerModel> visiblePlayers = Rs2Player.getPlayers(p -> true).collect(Collectors.toList());
        Set<String> visibleNames = visiblePlayers.stream()
                .map(Rs2PlayerModel::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Iterate a copy of the keys to avoid ConcurrentModificationException
        for (String name : new ArrayList<>(attackerTickMap.keySet())) {
            if (!visibleNames.contains(name)) {
                int oldTicks = attackerTickMap.get(name);
                // Decay 1 tick per game tick.
                // 75 ticks = 45 seconds of memory.
                int newTicks = Math.max(0, oldTicks - 1);

                if (newTicks == 0) {
                    attackerTickMap.remove(name);
                } else {
                    attackerTickMap.put(name, newTicks);
                }
            }
        }

        // 5. SCAN VISIBLE PLAYERS & UPDATE MAP
        for (Rs2PlayerModel player : visiblePlayers) {
            String playerName = player.getName();
            if (playerName == null) continue;

            int tickCount = attackerTickMap.getOrDefault(playerName, 0);
            boolean isInteracting = player.getInteracting() == localPlayer;
            boolean isDead = player.getAnimation() == 829;
            boolean hasHitsplat = hitsplatIsTheirs(); // Be careful with this global flag

            // --- TICK UPDATE LOGIC ---
            if (isInteracting && !isDead) {
                // BOOST: They are actively fighting us.
                int boost = hasHitsplat ? 3 : 2;
                int newTickCount = tickCount + boost;

                // FLOOR: If active, never drop below 50.
                if (newTickCount < 50) newTickCount = 50;

                attackerTickMap.put(playerName, newTickCount);
            } else {
                // DECAY: They are visible but NOT fighting (Eating/Running/Standing)
                int decay = isDead ? 100 : 1;
                int newTickCount = Math.max(0, tickCount - decay);

                if (newTickCount == 0) attackerTickMap.remove(playerName);
                else attackerTickMap.put(playerName, newTickCount);
            }

            // --- TARGET SELECTION LOGIC (The "Skull Safety" Check) ---
            // CRITICAL: We only consider them a target if they are INTERACTING.
            // This prevents attacking randoms who return 20 mins later unskulled.

            if (isInteracting && !isDead) {
                int currentTicks = (currentTarget != null) ? attackerTickMap.getOrDefault(currentTarget.getName(), 0) : 0;
                int myTicks = attackerTickMap.getOrDefault(playerName, 0);

                // Case A: We have no target. Take the first aggressor who crosses the threshold.
                if (currentTarget == null) {
                    if (myTicks > MIN_TICKS_TO_TRACK || hasHitsplat) {
                        Microbot.log("Acquiring Target: " + playerName);
                        currentTarget = player;
                    }
                }
                // Case B: We have a target, but this guy is a bigger threat.
                else if (!currentTarget.getName().equals(playerName)) {
                    // Only switch if this new guy has significantly more history/threat
                    if (myTicks > (currentTicks + 10)) {
                        Microbot.log("Switching Target (Higher Threat): " + playerName);
                        currentTarget = player;
                    }
                }
            }
        }

        // 6. SNARE FALLBACK (Strict Attribution)
        // If we are snared, we need to find who did it, but ONLY if they are actively aggressive.
        if (MossKillerPlugin.isPlayerSnared() && currentTarget == null) {
            for (Rs2PlayerModel player : visiblePlayers) {
                // Must be interacting AND (Anim OR Hitsplat) to be blamed for a snare
                if (player.getInteracting() == localPlayer &&
                        (hitsplatIsTheirs() || !isNonCombatAnimation(player))) {

                    Microbot.log("Snare Detected: Blaming aggressor " + player.getName());
                    attackerTickMap.put(player.getName(), 100); // Instant max threat
                    currentTarget = player;
                    break; // Found the culprit
                }
            }
        }
    }

    // Add this method to check if a player is above the combat threshold
    private boolean isAboveCombatThreshold(Rs2PlayerModel player) {
        if (player == null) return false;

        // Define your combat threshold - adjust this value as needed
        final int COMBAT_THRESHOLD = 87; // Example threshold

        return player.getCombatLevel() > COMBAT_THRESHOLD;
    }

    /**
     * Determines if a player is performing a non-combat animation (walking/running).
     */
    private boolean isNonCombatAnimation(Player player) {
        int animationId = player.getAnimation();
        //walking, running, or doing defense animations
        return animationId == -1 || animationId == 1156 || animationId == 403 || animationId == 420 || animationId == 410;
    }


    public boolean hitsplatIsTheirs() {
        return hitsplatIsTheirs;
    }

    private void checkNearbyPlayers() {
        Player localPlayer = Microbot.getClient().getLocalPlayer();
        if (localPlayer == null) {
            isPlayerNearby = false;
            return;
        }

        isPlayerNearby = client.getPlayers().stream()
                .anyMatch(player -> player != null
                        && player != localPlayer // Exclude yourself
                        && player.getWorldLocation().distanceTo(localPlayer.getWorldLocation()) <= 15); // Adjust distance
    }


    private boolean isMossGiant(Actor actor) {
        return actor instanceof NPC && Objects.requireNonNull(((NPC) actor).getName()).equalsIgnoreCase(MOSS_GIANT_NAME);
    }

    private void resetAttackers() {
        attackerTickMap.clear();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {

        if(config.bossOnly()) {
            if (event.getMessage().equals("Only a sharp blade can cut through this sticky web.")) {
                noKnife = true;
            }
        }

        if (config.wildy()) {
            if (event.getMessage().equals("You eat the swordfish.")) {
                lobsterEaten = true;
                tickCount = 0; // Reset the tick counter
            }

            if (currentTarget != null) {
                wildyKillerScript.isTargetOutOfReach = event.getMessage().equals("I can't reach that.");
            } else {
                wildyKillerScript.isTargetOutOfReach = false; // Explicitly set to false when no target
            }
        }

        if (config.wildy() || config.wildySafer()) {
            if (event.getMessage().equals("You do not have enough Chaos Runes to cast this spell.")) {
                autoCastMinds = true;
            }
        }

    }

    /**
     * Map to track recent hitsplats and their timestamps for each player.
     */
    private final Map<Player, Integer> recentHitsplats = new HashMap<>();

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        Actor target = event.getActor();
        Hitsplat hitsplat = event.getHitsplat();

        if (config.wildySafer()) {
            // SafeSpot logic
            if (wildySaferScript.isAtSafeSpot()) {
                if (target == client.getLocalPlayer()) {
                    long currentTime = System.currentTimeMillis();

                    // Debug to see what's happening
                    System.out.println("Hit registered at safespot, count: " + consecutiveHitsplatsMain);
                    System.out.println("*** SETTING MOVE TO TRUE ***");
                    wildySaferScript.move = true;
                    consecutiveHitsplatsMain = 0; // Reset counter

                    // Update the last hitsplat time
                    lastHitsplatTimeMain = currentTime;
                }

                if (target == client.getLocalPlayer()){
                    if (wildySaferScript.iveMoved && wildySaferScript.isAtSafeSpot1()) {
                        System.out.println("*** SETTING SAFESPOT1ATTACK TO TRUE ***");
                        wildySaferScript.safeSpot1Attack = true;
                        System.out.println("*** you've been hit while at safespot1 ***");
                    }
                }

            }
        }

        if(config.wildy()) {

            if (currentTarget != null) {
                wildyKillerScript.hitsplatApplied = event.getHitsplat().isMine();
            }


            if (target == Microbot.getClient().getLocalPlayer()) {
                if (hitsplat.getHitsplatType() == HitsplatID.BLOCK_ME || hitsplat.getHitsplatType() == HitsplatID.DAMAGE_ME) {
                    //System.out.println("registered a hit");
                    WorldView worldView = client.getWorldView(-1); // or getTopLevelWorldView()
                    if (worldView != null && worldView.players() != null && worldView.players().iterator().hasNext()) {
                        int playerCount = 0;
                        for (Player player : worldView.players()) {
                            if (player != client.getLocalPlayer()) {
                                playerCount++;
                            }
                        }

                        if (playerCount > 0) {
                            // There are players other than the local player
                            System.out.println("There are other players in the world view.");
                            for (Player player : worldView.players()) {
                                System.out.println("there is a player nearby");
                                System.out.println("is doing combat animation " + (!isNonCombatAnimation(player)));
                                System.out.println("Interacting with me he is " + (player.getInteracting() == client.getLocalPlayer()));
                                if (player.getInteracting() == client.getLocalPlayer() && !isNonCombatAnimation (player)) {
                                    Microbot.log("Someone is interacting with me while doing a combat animation");
                                    recentHitsplats.put(player, client.getTickCount());
                                    hitsplatIsTheirs = true;
                                    hitsplatSetTick = client.getTickCount(); // Record the tick when the flag is set
                                }
                            }
                        }
                    }

                }
            }
        }
    }
    /**
     * Determines which player caused the hitsplat based on interaction and proximity.
     */
    private Player getAttackerForHitsplat(Rs2PlayerModel localPlayer) {
        for (Rs2PlayerModel player : Rs2Player.getPlayersInCombatLevelRange()) {
            if (player.getInteracting() == localPlayer && !isNonCombatAnimation(player)) {
                ;
                return player;
            }
        }
        return null;
    }

    public boolean autoCastMinds() {return autoCastMinds;}

    public boolean lobsterEaten() {
        return lobsterEaten;
    }

    public boolean isNoKnife() {
        return noKnife;
    }


    private void stopWalking() {
        Rs2Walker.setTarget(null); // Stops the player from walking
        System.out.println("Walking stopped due to zero health.");
    }


    public void resetTeleblock() {
        isTeleblocked = false; // Reset when needed
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event) {
        Player localPlayer = Microbot.getClient().getLocalPlayer();
        if (localPlayer != null && localPlayer.hasSpotAnim(SNARE)) {
            handleSnare();  // Method to handle being snared
        }
        if (localPlayer != null && localPlayer.hasSpotAnim(SPLASH)) {
            hitsplatIsTheirs = true;  // Method to handle being snared
        }
    }

    private void handleSnare() {
        isSnared = true;
        snareTickCounter = 0;  // Reset the counter when snared
        Microbot.log("Player is snared!");
    }

    public static boolean isPlayerSnared() {
        return isSnared;
    }

    public boolean preparingForShutdown = false;

    protected void shutDown() {
        exampleScript.shutdown();
        wildyKillerScript.shutdown();
        wildySaferScript.shutdown();
        preparingForShutdown = false;
        projectileCount = 0;
        overlayManager.remove(mossKillerOverlay);
        autoCastMinds = false;
        noKnife = false;
    }
}