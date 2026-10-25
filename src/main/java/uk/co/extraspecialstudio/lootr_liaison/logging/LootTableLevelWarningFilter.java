package uk.co.extraspecialstudio.lootr_liaison.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

/**
 * Suppresses the "block entity had its loot table set before its level was set" warning
 * that can appear during worldgen when Lootr (or similar) inspects block entities before
 * their chunk/level is fully set. The warning is harmless; this filter avoids log noise.
 * <p>
 * Out of scope for this mod: the "BlockStateRandomizer exceeds max probability of 1"
 * warning comes from worldgen/ore config (e.g. YUNG's or other mods), not Lootr Liaison.
 */
public final class LootTableLevelWarningFilter extends AbstractFilter {

    private static final String SUPPRESSED_MESSAGE = "had its loot table set before its level was set";

    public LootTableLevelWarningFilter() {
        super(Result.DENY, Result.NEUTRAL);
    }

    @Override
    public Result filter(final LogEvent event) {
        if (event == null || event.getMessage() == null) {
            return Result.NEUTRAL;
        }
        String msg = event.getMessage().getFormattedMessage();
        return msg != null && msg.contains(SUPPRESSED_MESSAGE) ? Result.DENY : Result.NEUTRAL;
    }
}
