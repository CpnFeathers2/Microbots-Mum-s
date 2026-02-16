package net.runelite.client.plugins.microbot.membersalch;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
    name = PluginDescriptor.CpnFeathers + "Members Alcher",
    description = "Automated members-only alching with 4-hour login cycles",
    tags = {"alch", "magic", "members", "automation", "cpnfeathers"},
    enabledByDefault = false
)
@Slf4j
public class MembersAlchPlugin extends Plugin {

    @Inject
    private MembersAlchConfig config;

    @Inject
    private Client client;

    @Inject
    private net.runelite.client.callback.ClientThread clientThread;

    @Inject
    private MembersAlchScript script;

    @Provides
    MembersAlchConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MembersAlchConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState() == net.runelite.api.GameState.LOGGED_IN) {
            Microbot.pauseAllScripts = false;
            Microbot.setClient(client);
            Microbot.setClientThread(clientThread);
            Microbot.setMouse(new VirtualMouse());
            if (overlayManager != null) {
                overlayManager.add(overlay);
            }
            script.run(config);
            log.info("Members Alcher plugin started");
        }
    }

    @Override
    protected void shutDown() throws Exception {
        script.shutdown();
        if (overlayManager != null) {
            overlayManager.remove(overlay);
        }
        log.info("Members Alcher plugin stopped");
    }

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MembersAlchOverlay overlay;
}
