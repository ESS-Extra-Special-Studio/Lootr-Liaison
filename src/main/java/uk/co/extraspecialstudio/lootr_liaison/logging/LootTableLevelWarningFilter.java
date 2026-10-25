package uk.co.extraspecialstudio.lootr_liaison.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public final class LootTableLevelWarningFilter extends AbstractFilter {
    private static final String NEEDLE = "loot table set before its level was set";

    @Override
    public Filter.Result filter(LogEvent event) {
        if (event == null || event.getMessage() == null) return Result.NEUTRAL;
        String msg = event.getMessage().getFormattedMessage();
        if (msg != null && msg.contains(NEEDLE)) return Result.DENY;
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        if (msg != null && String.valueOf(msg.getFormattedMessage()).contains(NEEDLE)) return Result.DENY;
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        if (msg != null && String.valueOf(msg).contains(NEEDLE)) return Result.DENY;
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        if (msg != null && msg.contains(NEEDLE)) return Result.DENY;
        return Result.NEUTRAL;
    }
}