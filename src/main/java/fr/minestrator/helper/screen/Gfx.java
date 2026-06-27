package fr.minestrator.helper.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

/**
 * Bridges the text-drawing API across MC versions: 1.21.x uses
 * {@code GuiGraphics.drawString(...)}, 26.1+ renamed it to
 * {@code GuiGraphicsExtractor.text(...)}. The {@code ctx} type matches the
 * Screen's render/extractRenderState parameter on the active node, so callers
 * pass their own context unchanged. {@code fill}/{@code enableScissor}/{@code pose}
 * kept their names, so those are called directly on the context.
 */
public final class Gfx {
    private Gfx() {
    }

    //? if >=26.1 {
    /*public static void text(GuiGraphicsExtractor ctx, Font font, String s, int x, int y, int color, boolean shadow) {
        ctx.text(font, s, x, y, color, shadow);
    }

    public static void text(GuiGraphicsExtractor ctx, Font font, FormattedCharSequence s, int x, int y, int color, boolean shadow) {
        ctx.text(font, s, x, y, color, shadow);
    }
    *///?} else {
    public static void text(GuiGraphics ctx, Font font, String s, int x, int y, int color, boolean shadow) {
        ctx.drawString(font, s, x, y, color, shadow);
    }

    public static void text(GuiGraphics ctx, Font font, FormattedCharSequence s, int x, int y, int color, boolean shadow) {
        ctx.drawString(font, s, x, y, color, shadow);
    }
    //?}
}
