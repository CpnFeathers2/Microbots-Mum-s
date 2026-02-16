package net.runelite.client.plugins.microbot.beescreenshot;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ScreenShotScript extends Script {
    private final Client client = Microbot.getInjector().getInstance(Client.class);
    private final DrawManager drawManager = Microbot.getInjector().getInstance(DrawManager.class);
    private final ScheduledExecutorService executor = Microbot.getInjector().getInstance(ScheduledExecutorService.class);

    public void captureNow() {
        ClientThread clientThread = Microbot.getClientThread();

        if (clientThread == null) {
            Microbot.log("ClientThread is null!");
            return;
        }

        if (client == null || client.getGameState() == GameState.LOGIN_SCREEN) {
            Microbot.log("Client is null or on login screen!");
            return;
        }

        // Use DrawManager to capture the next frame (this captures just the game client)
        drawManager.requestNextFrameListener((img) -> {
            // This callback is on the game thread, move to executor thread
            executor.submit(() -> {
                try {
                    BufferedImage screenshot = ImageUtil.bufferedImageFromImage(img);

                    String fileName = "shot_" + System.currentTimeMillis() + ".png";
                    File output = new File(System.getProperty("user.home")
                            + "/microbot-screenshots/"
                            + fileName);
                    output.getParentFile().mkdirs();

                    ImageIO.write(screenshot, "png", output);
                    //Microbot.log("Saved screenshot: " + output.getAbsolutePath());
                } catch (IOException e) {
                    Microbot.log("Screenshot failed: " + e.getMessage());
                }
            });
        });
    }


    public boolean run(ScreenShotConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                captureNow();

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
    }
}