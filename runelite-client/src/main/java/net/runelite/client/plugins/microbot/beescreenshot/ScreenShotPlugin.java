package net.runelite.client.plugins.microbot.beescreenshot;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Bee + "Screenshot",
        description = "Takes a screenshot every second",
        tags = {"screenshot", "bee"},
        enabledByDefault = true
)
@Slf4j
public class ScreenShotPlugin extends Plugin {
    @Inject
    private ScreenShotConfig config;
    @Provides
    ScreenShotConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ScreenShotConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;


    @Inject
    ScreenShotScript screenshotScript;


    @Override
    protected void startUp() throws AWTException {
        screenshotScript.run(config);
    }

    protected void shutDown() {
        screenshotScript.shutdown();
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
