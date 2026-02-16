package net.runelite.client.plugins.microbot;

import com.google.inject.Injector;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.annotations.Component;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.RuneLite;
import net.runelite.client.RuneLiteDebug;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ProfileManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.item.Rs2ItemManager;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;
import net.runelite.client.plugins.microbot.util.mouse.Mouse;
import net.runelite.client.plugins.microbot.util.mouse.naturalmouse.NaturalMouse;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import org.slf4j.event.Level;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.*;

@Slf4j
public class Microbot {
    //Version path used to load the client faster when developing by checking version number
    //If the version is the same as the current version we do not download the latest .jar
    //Resulting in a faster startup
    /**
     * Tracks which account is currently active (0-11)
     * 0 = Account 1
     * 1 = Account 2
     * 2 = Account 3
     * 3 = Account 4
     * 4 = Account 5
     * 5 = Account 6
     * 6 = Account 7
     * 7 = Account 8
     * 8 = Account 9
     * 9 = Account 10
     * 10 = Account 11
     * 11 = Account 12
     **/
    public static int activeAccountIndex = 0;
    public static Map<Integer, Boolean> accountToggleMap = new HashMap<>();
    private static final String VERSION_FILE_PATH = "debug_temp_version.txt";
    public static boolean mossKillerOn = false;
    public static boolean killerMossOn = false;
    public static boolean normalMossOn = false;
    public static boolean monkKillerOn = false;
    public static boolean safeMossOn = false;
    public static boolean bonesToBananas = false;
    public static boolean noWaterStaff = false;
    public static boolean lock = false;
    public static boolean bonesToBananasFinish = false;
    public static boolean sellingGe = false;
    public static boolean herringTime = false;
    public static Queue<Integer> playtimeQueue = new LinkedList<>();
    public static boolean usePlaytimeQueue = false;
    public static int queueUsageCount = 0;
    public static final int MAX_QUEUE_USAGE = 7;
    private static final ScheduledExecutorService xpSchedulor = Executors.newSingleThreadScheduledExecutor();
    public static MenuEntry targetMenu;
    public static boolean isGainingExp = false;
    public static boolean pauseAllScripts = false;
    public static boolean runShopper = false;
    public static String status = "IDLE";
    public static boolean enableAutoRunOn = true;
    public static boolean useStaminaPotsIfNeeded = true;
    public static int runEnergyThreshold = 1000;
    public static boolean superJammed = false;
    public static boolean walkJammed = false;
    @Getter
    @Setter
    @Inject
    public static MouseManager mouseManager;
    @Getter
    @Setter
    public static NaturalMouse naturalMouse;
    @Getter
    @Setter
    private static Mouse mouse;
    @Getter
    @Setter
    private static Client client;
    @Getter
    @Setter
    private static ClientThread clientThread;
    @Getter
    @Setter
    private static EventBus eventBus;
    @Getter
    @Setter
    private static WorldMapPointManager worldMapPointManager;
    @Getter
    @Setter
    private static SpriteManager spriteManager;
    @Getter
    @Setter
    private static ItemManager itemManager;
    @Getter
    @Setter
    private static Notifier notifier;
    @Getter
    @Setter
    private static NPCManager npcManager;
    @Getter
    @Setter
    private static ProfileManager profileManager;
    @Getter
    @Setter
    private static ConfigManager configManager;
    @Getter
    @Setter
    private static WorldService worldService;
    @Getter
    @Setter
    private static PluginManager pluginManager;
    @Getter
    @Setter
    private static WorldMapOverlay worldMapOverlay;
    @Getter
    @Setter
    private static InfoBoxManager infoBoxManager;
    @Getter
    @Setter
    private static ChatMessageManager chatMessageManager;
    @Getter
    @Setter
    private static TooltipManager tooltipManager;
    private static ScheduledFuture<?> xpSchedulorFuture;
    private static net.runelite.api.World quickHopTargetWorld;

    public static boolean isCantReachTargetDetectionEnabled = false;
    public static boolean connectionLost = false;
    public static boolean cantReachTarget = false;
    public static boolean cantHopWorld = false;
    public static boolean cook = false;
    public static boolean finishedCooking = false;
    public static boolean clanHall = false;
    public static boolean goToShop = false;
    public static boolean shutDownAutoLogin = false;
    public static int cantReachTargetRetries = 0;
    public static boolean bryoFinished = false;
    public static boolean bryoFinished1 = false;



    @Getter
    public static final BlockingEventManager blockingEventManager = new BlockingEventManager();

    @Getter
    public static HashMap<String, Integer> scriptRuntimes = new HashMap<>();

    @Getter
    @Setter
    public static Rs2ItemManager rs2ItemManager;

    public static boolean loggedIn = false;

    @Setter
    public static Instant loginTime;

    public static class AccountSessionData {
        public long loginTime = -1;
        public long totalLoginTime = 0;
        public int startMageXp = -1;
        public int currentMageXp = -1;
        public boolean isOnline = false;
        public boolean isBanned = false;
        public int herringsCaught;
        public int deaths;
        //public int mossyKeys;
        public int longswordsSmithed;
        public long gpGainedAlching;
        public int burntChickens;
        public long currentBankCoins = 0;
    }

    private static long lastLockInTime = 0;
    private static final long LOCK_IN_INTERVAL = 1200; // 1200ms
    public static boolean stop4Looting = false;
    public static boolean idlingUntilBreak = false;

    public static void updateAccountGpFromBank(int accountIndex, long bankCoins) {
       // AccountSessionData data = accountSessionMap.getOrDefault(accountIndex, new AccountSessionData());
       // data.gpGainedAlching = bankCoins;
        //accountSessionMap.put(accountIndex, data);
    }

   /* public static void autoLockInMageXp(int accountId) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLockInTime < LOCK_IN_INTERVAL) {
            return; // Too soon since last lock-in
        }

        AccountSessionData data = Microbot.accountSessionMap.get(accountId);
        if (data == null || !data.isOnline || data.startMageXp == -1) {
            return;
        }

        // Safety checks
        if (Microbot.getClient() == null ||
                Microbot.getClient().getLocalPlayer() == null ||
                Microbot.getClient().getGameState() != GameState.LOGGED_IN) {
            return;
        }

        int currentXp = Microbot.getClient().getSkillExperience(Skill.MAGIC);
        if (currentXp <= 0) {
            return; // Invalid XP value
        }

        int sessionGained = currentXp - data.startMageXp;
        if (sessionGained < 0) {
            return; // XP went backwards - definitely wrong account
        }

        // All checks passed - safe to lock in
        data.currentMageXp = currentXp;
        lastLockInTime = currentTime;

        // Optional: Only log significant updates
        if (sessionGained > 0) {
            System.out.println("Account " + accountId + " auto-locked: +" + sessionGained + " XP");
        }
    }
*/
    public static void buildPlaytimeQueue() {
        playtimeQueue.clear();

        int totalAccounts = 7;

        // Priority groups
        List<Integer> noDataAccounts = new ArrayList<>();
        List<Map.Entry<Integer, AccountSessionData>> nonFreshies = new ArrayList<>();
        List<Map.Entry<Integer, AccountSessionData>> freshies = new ArrayList<>();

        for (int i = 0; i < totalAccounts; i++) {
            //AccountSessionData data = Microbot.accountSessionMap.get(i);
/*
            if (data == null) {
                // No session data — highest priority
                noDataAccounts.add(i);
            } else if (data.isFreshie) {
                freshies.add(Map.entry(i, data));
            } else {
                nonFreshies.add(Map.entry(i, data));
            }*/
        }

        // Sort non-freshies and freshies by playtime (ascending)
        nonFreshies.sort(Comparator.comparingLong(e -> e.getValue().totalLoginTime));
        freshies.sort(Comparator.comparingLong(e -> e.getValue().totalLoginTime));

        // Add to queue in strict priority order
        for (int accountId : noDataAccounts) {
            playtimeQueue.offer(accountId);
            Microbot.log("Added no-data account " + accountId + " to queue (assumed playtime: 0)");
        }

        for (Map.Entry<Integer, AccountSessionData> entry : nonFreshies) {
            playtimeQueue.offer(entry.getKey());
            Microbot.log("Added experienced account " + entry.getKey() +
                    " to queue (playtime: " + entry.getValue().totalLoginTime + ")");
        }

        for (Map.Entry<Integer, AccountSessionData> entry : freshies) {
            playtimeQueue.offer(entry.getKey());
            Microbot.log("Added freshie account " + entry.getKey() +
                    " to queue (playtime: " + entry.getValue().totalLoginTime + ")");
        }

        Microbot.log("Final playtime queue: " + playtimeQueue.toString());
    }

    /* public static void switchToNextAccount() {
        // ═══════════════════════════════════════════════════════════
        // RESET FLAGS FOR NEXT ACCOUNT
        // ═══════════════════════════════════════════════════════════
        Microbot.AccountSessionData currentData = Microbot.accountSessionMap.get(Microbot.activeAccountIndex);
        if (AutoLoginScript.sessionState != null) {
            int currentAccount = Microbot.activeAccountIndex;

            Microbot.log("🔄 Switching accounts: " + currentAccount + " → " + ((currentAccount + 1) % 6));

            // Reset all flags so next account starts fresh
            AutoLoginScript.sessionState.controlScriptState.timeToAlch = false;
            AutoLoginScript.sessionState.controlScriptState.timeToSmith = false;
            AutoLoginScript.sessionState.controlScriptState.timeToFish = false;
            AutoLoginScript.sessionState.controlScriptState.timeToBtB = false;
            AutoLoginScript.sessionState.controlScriptState.timeToCook = false;
            AutoLoginScript.sessionState.controlScriptState.finishedFishing = false;
            AutoLoginScript.sessionState.controlScriptState.finishedCooking = false;
            AutoLoginScript.sessionState.controlScriptState.finishedBtB = false;
            AutoLoginScript.sessionState.controlScriptState.gePrepEndCompleted = false;

            // Reset herring flags
            AutoLoginScript.sessionState.controlScriptState.herringFirstRun = false;
            AutoLoginScript.sessionState.controlScriptState.readyToFishShrimp = false;
            AutoLoginScript.sessionState.controlScriptState.readyToFishHerring = false;

            // Reset global flags
            Microbot.herringTime = false;
            Microbot.bonesToBananas = false;

            SessionStateManager.saveSessionState(AutoLoginScript.sessionState);
            Microbot.log("✅ Flags reset for next account");
        }

        // ═══════════════════════════════════════════════════════════
        // SWITCH TO NEXT ACCOUNT
        // ═══════════════════════════════════════════════════════════
        int previousAccount = Microbot.activeAccountIndex;
        Microbot.log("📊 Account " + Microbot.activeAccountIndex + " stats:");
        Microbot.log("   startMageXp: " + currentData.startMageXp);
        Microbot.log("   currentMageXp: " + currentData.currentMageXp);
        // Save current account's XP before switching
        if (currentData.isOnline) {
            Microbot.autoLockInMageXp(Microbot.activeAccountIndex);
        }
        Microbot.activeAccountIndex = (Microbot.activeAccountIndex + 1) % 6;

        Microbot.AccountSessionData nextData = Microbot.accountSessionMap.get(Microbot.activeAccountIndex);
        Microbot.log("📊 Switched to Account " + Microbot.activeAccountIndex + " stats:");
        Microbot.log("   startMageXp: " + (nextData != null ? nextData.startMageXp : "null"));
        Microbot.log("   currentMageXp: " + (nextData != null ? nextData.currentMageXp : "null"));

        Microbot.log("Account switched: " + previousAccount + " → " + Microbot.activeAccountIndex);
    }

    public static long breakHandlerLastSeen = System.currentTimeMillis();

    public static Map<Integer, AccountSessionData> accountSessionMap = new HashMap<>();

        */
    /**
     * Get the total runtime of the script
     * @return
     */
    public static Duration getLoginTime()
    {
        if (loginTime == null)
        {
            return Duration.of(0, ChronoUnit.MILLIS);
        }

        return Duration.between(loginTime, Instant.now());
    }

    /**
     * Checking the Report button will ensure that we are logged in, as there seems to be a small moment in time
     * when at the welcome screen that Rs2Settings.isLevelUpNotificationsEnabled() will return true then turn back to false
     * even if {@code Varbits.DISABLE_LEVEL_UP_INTERFACE} is 1 after clicking play button
     */
    @Component
    private static final int REPORT_BUTTON_COMPONENT_ID = 10616833;

    public static boolean isDebug() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("-agentlib:jdwp");
    }

    public static int getVarbitValue(int varbit) {
        return getClientThread().runOnClientThreadOptional(() -> getClient().getVarbitValue(varbit)).orElse(0);
    }

    public static int getVarbitPlayerValue(int varbit) {
        return getClientThread().runOnClientThreadOptional(() -> getClient().getVarpValue(varbit)).orElse(0);
    }

    public static EnumComposition getEnum(int id) {
        return getClientThread().runOnClientThreadOptional(() -> getClient().getEnum(id)).orElse(null);
    }

    public static StructComposition getStructComposition(int structId) {
        return getClientThread().runOnClientThreadOptional(() -> getClient().getStructComposition(structId)).orElse(null);
    }

    public static void setIsGainingExp(boolean value) {
        isGainingExp = value;
        scheduleIsGainingExp();
    }

    public static void scheduleIsGainingExp() {
        if (xpSchedulorFuture != null && !xpSchedulorFuture.isDone())
            xpSchedulorFuture.cancel(true);
        xpSchedulorFuture = xpSchedulor.schedule(() -> {
            isGainingExp = false;
        }, 4000, TimeUnit.MILLISECONDS);
    }

    public static boolean isLoggedIn() {
        if (loggedIn) return true;
        if (client == null) return false;
        GameState idx = client.getGameState();
        loggedIn = idx == GameState.LOGGED_IN && Rs2Widget.isWidgetVisible(REPORT_BUTTON_COMPONENT_ID);
        return loggedIn;
    }

    public static boolean isHopping() {
        if (client == null) return false;
        GameState idx = client.getGameState();
        return idx == GameState.HOPPING;
    }

    public static boolean hopToWorld(int worldNumber) {
        if (!Microbot.isLoggedIn()) return false;
        if (Microbot.isHopping()) return true;
        if (Microbot.cantHopWorld) return false;
        boolean isHopping = Microbot.getClientThread().runOnClientThreadOptional(() -> {
            if (Microbot.getClient().getLocalPlayer() != null && Microbot.getClient().getLocalPlayer().isInteracting())
                return false;
            if (quickHopTargetWorld != null || Microbot.getClient().getGameState() != GameState.LOGGED_IN) return false;
            if (Microbot.getClient().getWorld() == worldNumber) {
                return false;
            }
            World newWorld = Microbot.getWorldService().getWorlds().findWorld(worldNumber);
            if (newWorld == null) {
                Microbot.getNotifier().notify("Invalid World");
                System.out.println("Tried to hop to an invalid world");
                return false;
            }
            final net.runelite.api.World rsWorld = Microbot.getClient().createWorld();
            quickHopTargetWorld = rsWorld;
            rsWorld.setActivity(newWorld.getActivity());
            rsWorld.setAddress(newWorld.getAddress());
            rsWorld.setId(newWorld.getId());
            rsWorld.setPlayerCount(newWorld.getPlayers());
            rsWorld.setLocation(newWorld.getLocation());
            rsWorld.setTypes(WorldUtil.toWorldTypes(newWorld.getTypes()));
            if (rsWorld == null) {
                return false;
            }
            Microbot.getClient().openWorldHopper();
            Microbot.getClient().hopToWorld(rsWorld);
            quickHopTargetWorld = null;
            sleep(600);
            sleepUntil(() -> Microbot.isHopping() || Rs2Widget.getWidget(193, 0) != null, 2000);
            return Microbot.isHopping();
        }).orElse(false);
        if (!isHopping && Rs2Widget.getWidget(193, 0) != null) {
            List<Widget> areYouSureToSwitchWorldWidget = Arrays.stream(Rs2Widget.getWidget(193, 0).getDynamicChildren()).collect(Collectors.toList());
            Widget switchWorldWidget = sleepUntilNotNull(() -> Rs2Widget.findWidget("Switch world", areYouSureToSwitchWorldWidget, true), 2000);
            return Rs2Widget.clickWidget(switchWorldWidget);
        }
        return false;
    }

    public static void showMessage(String message) {
        try {
            SwingUtilities.invokeAndWait(() ->
            {
                JOptionPane.showConfirmDialog(null, message, "Message",
                        JOptionPane.DEFAULT_OPTION);
            });
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

    public static void showMessage(String message, int disposeTime) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                final JOptionPane optionPane = new JOptionPane(
                        message,
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION
                );

                final JDialog dialog = optionPane.createDialog("Message");

                // Set up timer to close the dialog after 10 seconds
                Timer timer = new Timer(disposeTime, e -> {
                    dialog.dispose();
                });
                timer.setRepeats(false);
                timer.start();
                dialog.setVisible(true);
                timer.stop();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static List<Rs2ItemModel> updateItemContainer(int id, ItemContainerChanged e) {
        if (e.getContainerId() == id) {
            List<Rs2ItemModel> list = new ArrayList<>();
            int i = -1;
            for (Item item : e.getItemContainer().getItems()) {
                if (item == null) {
                    i++;
                    continue;
                }
                i++; //increment before checking if it is a placeholder. This way the index will match the slots in the bank
                ItemComposition composition = Microbot.getItemManager().getItemComposition(item.getId());
                boolean isPlaceholder = composition.getPlaceholderTemplateId() > 0;
                if (isPlaceholder) continue;

                list.add(new Rs2ItemModel(item, composition, i));
            }
            return list;
        }
        return null;
    }

    /**
     * Starts the specified plugin by enabling it and starting all plugins in the plugin manager.
     * This method checks if the provided plugin is non-null before proceeding.
     *
     * @param plugin the plugin to be started.
     */
    @SneakyThrows
    public static void startPlugin(Plugin plugin) {
        if (plugin == null) return;
        SwingUtilities.invokeAndWait(() ->
        {
            try {
                getPluginManager().setPluginEnabled(plugin, true);
                getPluginManager().startPlugin(plugin);
                getPluginManager().startPlugins();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Retrieves a plugin by its class name from the plugin manager.
     * This method searches through the available plugins and returns the one matching the specified class name.
     *
     * @param className the fully qualified class name of the plugin to retrieve.
     *                  For example: {@code BreakHandlerPlugin.getClass().getName()}.
     * @return the plugin instance matching the specified class name, or {@code null} if no such plugin is found.
     */
    public static Plugin getPlugin(String className) {
        return getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getClass().getName().equals(className))
                .findFirst()
                .orElse(null);
    }

    /**
     * Stops the specified plugin using the plugin manager.
     * If the plugin is non-null, this method attempts to stop it and handles any instantiation exceptions.
     *
     * @param plugin the plugin to be stopped.
     */
    @SneakyThrows
    public static void stopPlugin(Plugin plugin) {
        if (plugin == null) return;
        SwingUtilities.invokeAndWait(() ->
        {
        try {
            getPluginManager().setPluginEnabled(plugin, false);
            getPluginManager().stopPlugin(plugin);
            //getPluginManager().startPlugins();
        } catch (PluginInstantiationException e) {
            e.printStackTrace();
        }
        });
    }


    public static void doInvoke(NewMenuEntry entry, Rectangle rectangle) {
        try {
            if (Rs2UiHelper.isRectangleWithinCanvas(rectangle)) {
                click(rectangle, entry);
            } else {
                click(new Rectangle(1, 1), entry);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            // Handle the error as needed
        }
    }

    public static void drag(Rectangle start, Rectangle end) {
        if (start == null || end == null) return;
        if (!Rs2UiHelper.isRectangleWithinCanvas(start) || !Rs2UiHelper.isRectangleWithinCanvas(end)) return;
        Point startPoint = Rs2UiHelper.getClickingPoint(start, true);
        Point endPoint = Rs2UiHelper.getClickingPoint(end, true);
        mouse.drag(startPoint, endPoint);
        if (!Microbot.getClient().isClientThread()) {
            sleep(50, 80);
        }
    }

    public static void click(Rectangle rectangle, NewMenuEntry entry) {
        if (entry.getType() == MenuAction.WALK) {
            mouse.click(new Point(entry.getParam0(), entry.getParam1()), entry);
        } else {
            Point point = Rs2UiHelper.getClickingPoint(rectangle, true);
            mouse.click(point, entry);
        }

        if (!Microbot.getClient().isClientThread()) {
            sleep(50, 100);
        }
    }

    public static void click(Rectangle rectangle) {

        Point point = Rs2UiHelper.getClickingPoint(rectangle, true);
        mouse.click(point);


        if (!Microbot.getClient().isClientThread()) {
            sleep(50, 80);
        }
    }


    /**
     * Logs the stack trace of an exception to the console and chat.
     * @param scriptName
     * @param e
     */
    public static void logStackTrace(String scriptName, Exception e) {
        log(scriptName, Level.ERROR, e);
    }

    public static void log(String message) {
        log(message, Level.INFO);
    }

    public static void log(String format, Object... args) {
        log(String.format(format, args), Level.INFO);
    }

    public static void log(Level level, String format, Object... args) {
        log(String.format(format, args), level);
    }

    public static void log(String message, Level level) {
        log(message, level, null);
    }

    public static void log(String message, Level level, Exception ex) {
        if (message == null || message.isEmpty()) return;
        if (level == null) return;

        switch (level) {
            case WARN:
                log.warn(message);
                break;
            case ERROR:
                if (ex != null) {
                    log.error(message, ex);
                } else {
                    log.error(message);
                }
                break;
            case DEBUG:
                log.debug(message);
                break;
            default:
                log.info(message);
                break;
        }

        if (Microbot.isLoggedIn()) {
            if (level == Level.DEBUG && !isDebug()) return;

            final String _message = ex == null ? message : ex.getMessage();

            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            Microbot.getClientThread().runOnClientThreadOptional(() ->
                    Microbot.getClient().addChatMessage(ChatMessageType.ENGINE, "", "[" + formattedTime + "]: " +  _message, "", false)
            );
        }
    }

    private static boolean isPluginEnabled(String name) {
        Plugin dashboard = Microbot.getPluginManager().getPlugins().stream()
                .filter(x -> x.getClass().getName().equals(name))
                .findFirst()
                .orElse(null);

        if (dashboard == null) return false;

        return Microbot.getPluginManager().isPluginEnabled(dashboard);
    }

    public static boolean isPluginEnabled(Class c) {
        return isPluginEnabled(c.getName());
    }

    @Deprecated(since = "1.6.2 - Use Rs2Player variant")
    public static QuestState getQuestState(Quest quest) {
        return getClientThread().runOnClientThreadOptional(() -> quest.getState(client)).orElse(null);
    }

    public static void writeVersionToFile(String version) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VERSION_FILE_PATH))) {
            writer.write(version);
        }
    }

    public static boolean isFirstRun() {
        File file = new File(VERSION_FILE_PATH);
        // Check if the version file exists
        return !file.exists();
    }

    public static String readVersionFromFile() throws IOException {
        try (Scanner scanner = new Scanner(new File(VERSION_FILE_PATH))) {
            return scanner.hasNextLine() ? scanner.nextLine() : "";
        }
    }

     public static boolean shouldSkipVanillaClientDownload() {
         if (isDebug()) {
             try {
                 String currentVersion = RuneLiteProperties.getVersion();
                 if (Microbot.isFirstRun()) {
                     Microbot.writeVersionToFile(currentVersion);
                     System.out.println("First run in debug mode. Version written to file.");
                 } else {
                     String storedVersion = Microbot.readVersionFromFile();
                     if (currentVersion.equals(storedVersion)) {
                         System.out.println("Running in debug mode. Version matches stored version.");
                         return true;
                     } else {
                         System.out.println("Version mismatch detected...updating client.");
                         Microbot.writeVersionToFile(currentVersion);
                     }
                 }
             } catch(Exception ex) {
                 ex.printStackTrace();
                 System.out.println(ex.getMessage());
             }
         }
         return false;
     }

     public static Injector getInjector() {
        if (RuneLiteDebug.getInjector() != null) {
            return RuneLiteDebug.getInjector();
        }
        return RuneLite.getInjector();
     }

    /**
     * Retrieves a list of active plugins that are part of the "microbot" package, excluding specific plugins.
     *
     * This method filters the active plugins managed by the `pluginManager` to include only those whose
     * package name contains "microbot" (case-insensitive). It further excludes certain plugins based on
     * their class names, such as "QuestHelperPlugin", "MInventorySetupsPlugin", "MicrobotPlugin",
     * "MicrobotConfigPlugin", "ShortestPathPlugin", "AntibanPlugin", and "ExamplePlugin".
     *
     * @return a list of active plugins belonging to the "microbot" package, excluding the specified plugins.
     */
     public static List<Plugin> getActiveMicrobotPlugins() {
         return pluginManager.getActivePlugins().stream()
                 .filter(x -> x.getClass().getPackage().getName().toLowerCase().contains("microbot"))
                 .filter(x -> !x.getClass().getSimpleName().equalsIgnoreCase("QuestHelperPlugin")
                         && !x.getClass().getSimpleName().equalsIgnoreCase("MInventorySetupsPlugin")
                         && !x.getClass().getSimpleName().equalsIgnoreCase("MicrobotPlugin")
                         && !x.getClass().getSimpleName().equalsIgnoreCase("MicrobotConfigPlugin")
                         && !x.getClass().getSimpleName().equalsIgnoreCase("ShortestPathPlugin")
                         && !x.getClass().getSimpleName().equalsIgnoreCase("AntibanPlugin")
                         && !x.getClass().getSimpleName().equalsIgnoreCase("ExamplePlugin"))
                 .collect(Collectors.toList());
     }

    /**
     * Retrieves a list of active `Script` instances from the currently active microbot plugins.
     *
     * This method iterates through all active microbot plugins and inspects their fields using reflection
     * to find fields of type `Script` or its subclasses. The identified `Script` fields are extracted
     * and returned as a list.
     *
     * Key Details:
     * - The method uses reflection to access the fields of each plugin class.
     * - Only fields that are assignable to the `Script` type are included.
     * - Private fields are made accessible via `field.setAccessible(true)` to retrieve their values.
     * - Any exceptions encountered during field access (e.g., `IllegalAccessException`) are logged, and
     *   the corresponding field is skipped.
     * - Null values resulting from inaccessible or uninitialized fields are filtered out.
     *
     * @return a list of active `Script` instances extracted from the microbot plugins.
     */
    public static List<Script> getActiveScripts() {
        return getActiveMicrobotPlugins().stream()
                .flatMap(x -> {
                    // Get all fields of the class
                    Field[] fields = x.getClass().getDeclaredFields();

                    // Filter fields that are assignable to Script
                    return Arrays.stream(fields)
                            .filter(field -> Script.class.isAssignableFrom(field.getType()))
                            .map(field -> {
                                field.setAccessible(true); // Allow access to private fields
                                try {
                                    return (Script) field.get(x); // Map the field to a Script instance
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    return null; // Handle exception if field cannot be accessed
                                }
                            });
                })
                .filter(Objects::nonNull) // Exclude nulls
                .collect(Collectors.toList());
    }
}

