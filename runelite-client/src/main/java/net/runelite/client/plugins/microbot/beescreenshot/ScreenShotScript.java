package net.runelite.client.plugins.microbot.beescreenshot;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.MossKiller.MossKillerPlugin;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenShotScript extends Script {

    // 600ms when fighting a target, 5s when idle
    private static final long INTERVAL_ON_TARGET_MS = 600;
    private static final long INTERVAL_IDLE_MS       = 5000;

    private final Client client =
        Microbot.getInjector().getInstance(Client.class);
    private final DrawManager drawManager =
        Microbot.getInjector().getInstance(DrawManager.class);
    private final ScheduledExecutorService executor =
        Microbot.getInjector().getInstance(ScheduledExecutorService.class);

    private long lastShotTime = 0;

    // ── screenshot save ───────────────────────────────────────────────────────

    public void captureNow() {
        if (client == null || client.getGameState() == GameState.LOGIN_SCREEN) {
            return;
        }
        drawManager.requestNextFrameListener((img) -> {
            executor.submit(() -> {
                try {
                    BufferedImage screenshot = ImageUtil.bufferedImageFromImage(img);
                    String fileName = "shot_" + System.currentTimeMillis() + ".png";
                    // Save to ~/microbot-screenshots/ — watched by oracle_test_runner.py
                    File output = new File(
                        System.getProperty("user.home") + "/microbot-screenshots/" + fileName
                    );
                    output.getParentFile().mkdirs();
                    ImageIO.write(screenshot, "png", output);
                } catch (IOException e) {
                    Microbot.log("Screenshot failed: " + e.getMessage());
                }
            });
        });
    }

    // ── target detection ─────────────────────────────────────────────────────

    private boolean hasActiveTarget() {
        try {
            // Check if MossKillerPlugin has a target
             MossKillerPlugin mossKiller = (MossKillerPlugin) Microbot.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin instanceof MossKillerPlugin)
                .findFirst()
                .orElse(null);

             if (mossKiller != null) {
                 return mossKiller.getCurrentTarget() != null;
             }
             return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ── main loop ────────────────────────────────────────────────────────────

    public boolean run(ScreenShotConfig config) {
        Microbot.enableAutoRunOn = false;
        // Poll every 100ms so we can react quickly when a target appears
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                long now      = System.currentTimeMillis();
                long interval = hasActiveTarget() ? INTERVAL_ON_TARGET_MS : INTERVAL_IDLE_MS;

                if (now - lastShotTime >= interval) {
                    lastShotTime = now;
                    captureNow();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
