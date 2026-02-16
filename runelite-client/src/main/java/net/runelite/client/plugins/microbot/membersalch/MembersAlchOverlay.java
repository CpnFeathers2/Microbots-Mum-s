package net.runelite.client.plugins.microbot.membersalch;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class MembersAlchOverlay extends OverlayPanel {

    private final MembersAlchPlugin plugin;
    private final MembersAlchScript script;
    private final MembersAlchConfig config;

    @Inject
    public MembersAlchOverlay(MembersAlchPlugin plugin, MembersAlchScript script, MembersAlchConfig config) {
        super(plugin);
        this.plugin = plugin;
        this.script = script;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().clear();

            panelComponent.getChildren().add(TitleComponent.builder()
                .text("Members Alcher")
                .color(Color.CYAN)
                .build());

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(script.isRunning() ? "Running" : "Stopped")
                .rightColor(script.isRunning() ? Color.GREEN : Color.RED)
                .build());

            // Get next login time from script
            long timeUntilLogin = script.getTimeUntilNextLogin();
            if (timeUntilLogin > 0) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeUntilLogin);
                panelComponent.getChildren().add(LineComponent.builder()
                    .left("Next Login:")
                    .right(minutes + " min")
                    .build());
            }

            // Get current session stats from Microbot
            long totalProfit = script.getTotalProfit();
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Alch Profit:")
                .right(totalProfit + " gp")
                .rightColor(Color.GREEN)
                .build());

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Alch Limit:")
                .right(config.minAlchLimit() + "-" + config.maxAlchLimit())
                .build());

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Profit Range:")
                .right(config.minProfit() + "-" + config.maxProfit() + " gp")
                .build());

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Min GE Limit:")
                .right(String.valueOf(config.minGeLimit()))
                .build());

        } catch (Exception e) {
            // Fail silently - overlay errors shouldn't crash the plugin
        }

        return super.render(graphics);
    }
}
