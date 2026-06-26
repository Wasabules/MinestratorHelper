package fr.minestrator.helper.util;

import java.util.List;

/** A parsed console line: coloured segments plus a detected log level (for filtering). */
public final class LogLine {
    public static final int OTHER = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;

    public final List<AnsiParser.Segment> segments;
    public final int level;

    public LogLine(List<AnsiParser.Segment> segments, int level) {
        this.segments = segments;
        this.level = level;
    }

    /** Detects the level from a raw line, e.g. "[12:00:03 INFO]: ...". */
    public static int detectLevel(String raw) {
        if (raw == null) {
            return OTHER;
        }
        String u = raw.toUpperCase();
        if (u.contains("ERROR") || u.contains("SEVERE") || u.contains("FATAL")) return ERROR;
        if (u.contains("WARN")) return WARN;
        if (u.contains("INFO")) return INFO;
        return OTHER;
    }
}
