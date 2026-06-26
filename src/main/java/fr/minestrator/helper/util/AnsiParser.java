package fr.minestrator.helper.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal ANSI (SGR) parser: turns a console line with ANSI colour codes into a
 * list of coloured text segments. Loader- and version-agnostic (no Minecraft API),
 * so it lives in the common module; the screen turns segments into Components.
 */
public final class AnsiParser {
    private AnsiParser() {
    }

    private static final char ESC = 27; // ANSI escape (0x1B)

    /** Default foreground colour for text outside any ANSI colour. */
    public static final int DEFAULT_COLOR = 0xE0E0E0;

    public static final class Segment {
        public final String text;
        public final int color; // 0xRRGGBB

        public Segment(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

    /** Parses one line; never returns null. Lines without ANSI yield a single default-coloured segment. */
    public static List<Segment> parse(String line) {
        List<Segment> segments = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            return segments;
        }

        int color = DEFAULT_COLOR;
        StringBuilder current = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == ESC && i + 1 < line.length() && line.charAt(i + 1) == '[') {
                if (current.length() > 0) {
                    segments.add(new Segment(current.toString(), color));
                    current.setLength(0);
                }
                int j = i + 2;
                StringBuilder code = new StringBuilder();
                while (j < line.length() && line.charAt(j) != 'm') {
                    code.append(line.charAt(j));
                    j++;
                }
                color = applyCodes(code.toString(), color);
                i = (j < line.length()) ? j + 1 : j;
            } else if (c == '\r') {
                i++; // drop carriage returns
            } else {
                current.append(c);
                i++;
            }
        }
        if (current.length() > 0) {
            segments.add(new Segment(current.toString(), color));
        }
        return segments;
    }

    private static int applyCodes(String codes, int current) {
        int color = current;
        for (String part : codes.split(";")) {
            if (part.isEmpty()) {
                continue;
            }
            int code;
            try {
                code = Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            if (code == 0) {
                color = DEFAULT_COLOR;
            } else {
                Integer mapped = ansiToRgb(code);
                if (mapped != null) {
                    color = mapped;
                }
            }
        }
        return color;
    }

    private static Integer ansiToRgb(int code) {
        switch (code) {
            case 30: return 0x555555;
            case 31: return 0xCC4444;
            case 32: return 0x44CC44;
            case 33: return 0xCCAA22;
            case 34: return 0x4477DD;
            case 35: return 0xCC44CC;
            case 36: return 0x44CCCC;
            case 37: return 0xCCCCCC;
            case 90: return 0x888888;
            case 91: return 0xFF5555;
            case 92: return 0x55FF55;
            case 93: return 0xFFFF55;
            case 94: return 0x5599FF;
            case 95: return 0xFF55FF;
            case 96: return 0x55FFFF;
            case 97: return 0xFFFFFF;
            default: return null;
        }
    }
}
