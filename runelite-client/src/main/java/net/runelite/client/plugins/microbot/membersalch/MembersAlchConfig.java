package net.runelite.client.plugins.microbot.membersalch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("membersalch")
public interface MembersAlchConfig extends Config {

    @ConfigSection(
        name = "Alch Limits",
        description = "Configure alching quantity limits",
        position = 0
    )
    String alchLimitsSection = "alchLimits";

    @ConfigSection(
        name = "Profit Settings",
        description = "Configure profit thresholds",
        position = 1
    )
    String profitSection = "profit";

    @ConfigSection(
        name = "Timing",
        description = "Configure login intervals",
        position = 2
    )
    String timingSection = "timing";

    @ConfigSection(
        name = "Filters",
        description = "Configure item filters",
        position = 3
    )
    String filtersSection = "filters";

    // === ALCH LIMITS ===

    @ConfigItem(
        keyName = "minAlchLimit",
        name = "Min Alch Limit",
        description = "Minimum items to purchase per session",
        position = 0,
        section = alchLimitsSection
    )
    default int minAlchLimit() {
        return 140;
    }

    @ConfigItem(
        keyName = "maxAlchLimit",
        name = "Max Alch Limit",
        description = "Maximum items to purchase per session",
        position = 1,
        section = alchLimitsSection
    )
    default int maxAlchLimit() {
        return 380;
    }

    // === PROFIT SETTINGS ===

    @ConfigItem(
        keyName = "minProfit",
        name = "Min Profit per Alch",
        description = "Minimum profit per item (gp)",
        position = 0,
        section = profitSection
    )
    default int minProfit() {
        return 250;
    }

    @ConfigItem(
        keyName = "maxProfit",
        name = "Max Profit per Alch",
        description = "Maximum profit per item (gp) - prevents suspicious high-profit items",
        position = 1,
        section = profitSection
    )
    default int maxProfit() {
        return 1400;
    }

    @ConfigItem(
        keyName = "natureRunePrice",
        name = "Nature Rune Price",
        description = "Price to pay for nature runes (gp)",
        position = 2,
        section = profitSection
    )
    default int natureRunePrice() {
        return 200;
    }

    // === TIMING ===

    @ConfigItem(
        keyName = "loginIntervalHours",
        name = "Login Interval (hours)",
        description = "How often to log in and perform alching routine",
        position = 0,
        section = timingSection
    )
    default int loginIntervalHours() {
        return 4;
    }

    // === FILTERS ===

    @ConfigItem(
        keyName = "minGeLimit",
        name = "Min GE Limit",
        description = "Minimum GE buy limit (filters out items with low limits like 8)",
        position = 0,
        section = filtersSection
    )
    default int minGeLimit() {
        return 40;
    }

    @ConfigItem(
        keyName = "membersOnly",
        name = "Members Items Only",
        description = "Only purchase members items (not F2P)",
        position = 1,
        section = filtersSection
    )
    default boolean membersOnly() {
        return true;
    }

    // === DEBUG ===

    @ConfigItem(
        keyName = "debugLogging",
        name = "Debug Logging",
        description = "Enable detailed debug logs",
        position = 99
    )
    default boolean debugLogging() {
        return false;
    }
}
