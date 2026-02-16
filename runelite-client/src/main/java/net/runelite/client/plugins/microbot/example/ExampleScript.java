package net.runelite.client.plugins.microbot.example;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExampleScript extends Script {

    // Just a chill script
    private static final List<String> PHRASES = Arrays.asList(
            "Just walking around, don't mind me!",
            "Anyone else just love the GE vibes?",
            "Look at me go!",
            "Walking is the best skill.",
            "I'm not lost, just exploring.",
            "Running circles around the economy.",
            "Is this the way to Varrock?",
            "Can someone trade me some attention?",
            "I'm fast as lightning!",
            "Just checking out the prices.",
            "Walking simulator 2024.",
            "Anyone want to race?",
            "I bet I can walk faster than you.",
            "Just getting my steps in.",
            "Does this armor make me look fast?",
            "Why run when you can walk with style?",
            "I'm the king of the GE!",
            "Just passing through.",
            "Nothing to see here, just a legend walking.",
            "I'm on a mission... to walk.",
            "Walking: the ultimate XP waste.",
            "Anyone seen a stray cat?",
            "I think I dropped my gold somewhere here.",
            "Just admiring the architecture.",
            "Is it just me or is it crowded?",
            "I'm glowing, aren't I?",
            "Watch out, coming through!",
            "I'm so bored I'm just walking.",
            "Anyone know a good joke?",
            "I'm here for the free attention.",
            "Walking builds character.",
            "I'm not a bot, I promise!",
            "Just doing my daily laps.",
            "Cardio is important, you know.",
            "I'm training Agility... sort of.",
            "Anyone want to buy some air?",
            "I'm richer than I look.",
            "Just waiting for the price to drop.",
            "I'm a professional walker.",
            "Don't follow me, I'm lost too.",
            "Is there a party nearby?",
            "I love the smell of GE in the morning.",
            "Just patrolling the area.",
            "Keeping the GE safe, one step at a time.",
            "I'm a moving target.",
            "Can't touch this!",
            "I'm so fast I'm a blur.",
            "Just testing my boots.",
            "Anyone want to be my friend?",
            "I'm lonely, please notice me.",
            "Walking away from my problems.",
            "I'm on a secret mission.",
            "Shh, I'm undercover.",
            "Just looking for deals.",
            "I'm the GE mascot.",
            "Walking is my passion.",
            "I'm going places.",
            "Just another day in Gielinor.",
            "I wonder what's for dinner.",
            "Anyone else hungry?",
            "I'm thirsty for attention.",
            "Look at my cape!",
            "I'm the best walker in the game.",
            "Challenge me to a walk-off.",
            "I'm unstoppable.",
            "Just cruising.",
            "I'm in the zone.",
            "Walking tall.",
            "I'm a walking legend.",
            "Don't hate the walker, hate the game.",
            "I'm too cool for school.",
            "Just vibing.",
            "Anyone want to dance?",
            "I've got the moves.",
            "Walking is life.",
            "I'm a free spirit.",
            "Just wandering.",
            "I'm a nomad.",
            "Where am I going? Who knows.",
            "The journey is the destination.",
            "I'm finding myself.",
            "Just reflecting on life.",
            "Why are we here? To walk.",
            "I'm a philosopher.",
            "Deep thoughts while walking.",
            "Anyone seen my pet rock?",
            "I'm a rockstar.",
            "Walking like a boss.",
            "I own this place.",
            "Just kidding, I'm broke.",
            "Spare change?",
            "I'm walking for charity.",
            "Support my cause: walking.",
            "I'm raising awareness for walking.",
            "Join my walking club.",
            "We are the walkers.",
            "Walking revolution!",
            "Power to the walkers!",
            "I'm tired now."
    );

    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false; // We might want to toggle run manually or let random logic handle it

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run())
                    return;

                if (!Microbot.isLoggedIn())
                    return;

                // 20% chance to say something
                if (Rs2Random.between(0, 10) > 8) {
                    String phrase = PHRASES.get(Rs2Random.between(0, PHRASES.size()));
                    Rs2Keyboard.typeString(phrase);
                    Rs2Keyboard.enter();
                    sleep(1000, 3000);
                }

                // 80% chance to walk somewhere
                if (Rs2Random.between(0, 10) > 2) {
                    // GE Center is roughly 3164, 3487
                    int randomX = 3164 + Rs2Random.between(-10, 10);
                    int randomY = 3487 + Rs2Random.between(-10, 10);
                    WorldPoint target = new WorldPoint(randomX, randomY, 0);

                    Rs2Walker.walkTo(target);
                    sleep(2000, 5000); // wait a bit after walking
                }

                // Random sleep to make it periodic
                sleep(2000, 8000);

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
