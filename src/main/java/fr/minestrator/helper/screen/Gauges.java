package fr.minestrator.helper.screen;

import net.minecraft.client.gui.Font;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

/**
 * Small UI helper to draw a labelled fill gauge laid out vertically:
 * label on top, the bar in the middle, the value below. Colour goes
 * green → yellow → red with the percentage. Mojmap only, version-agnostic.
 */
public final class Gauges {
    private Gauges() {
    }

    /** Total vertical space a gauge occupies (label + bar + value). */
    public static final int HEIGHT = 25;

    //? if >=26.1 {
    /*public static void drawGauge(GuiGraphicsExtractor ctx, Font font, int x, int y, int w, int percent,
                                 String label, String value) {
    *///?} else {
    public static void drawGauge(GuiGraphics ctx, Font font, int x, int y, int w, int percent,
                                 String label, String value) {
    //?}
        int p = Math.max(0, Math.min(100, percent));
        Gfx.text(ctx, font, label, x, y, 0xFFBBBBBB, true);

        int barY = y + 9;
        int barH = 6;
        ctx.fill(x, barY, x + w, barY + barH, 0xFF1E1E1E);
        int fillW = Math.round(w * p / 100.0f);
        ctx.fill(x, barY, x + fillW, barY + barH, 0xFF000000 | colorFor(p));

        Gfx.text(ctx, font, value, x, barY + barH + 2, 0xFFFFFFFF, true);
    }

    public static int colorFor(int percent) {
        if (percent >= 85) return 0xE05555; // red
        if (percent >= 60) return 0xE0C040; // yellow
        return 0x55C055;                    // green
    }

    /** Draws a 1px rectangular border. */
    //? if >=26.1 {
    /*public static void drawBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
    *///?} else {
    public static void drawBorder(GuiGraphics ctx, int x, int y, int w, int h, int color) {
    //?}
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }
}
