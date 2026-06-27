package fr.minestrator.helper.screen;

import dev.architectury.event.events.client.ClientGuiEvent;
import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.api.ServerLiveData;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

/**
 * Optional in-game overlay (toggled with F3+F6) showing the pinned/current server's
 * live stats (CPU / RAM / players), so you can keep an eye on them without opening F6.
 * Drawn via Architectury's RENDER_HUD event (no mixin). The delta arg is unused so the
 * lambda is version-agnostic; only renderHud carries the GuiGraphics //? for 26.1.
 */
public final class ServerHud {
    private static boolean enabled = false;
    private static ServerLiveData liveData = null;
    private static int tickCounter = 0;

    private ServerHud() {
    }

    public static void register() {
        ClientGuiEvent.RENDER_HUD.register((graphics, delta) -> renderHud(graphics));
    }

    public static void toggle() {
        enabled = !enabled;
        if (enabled) {
            liveData = null;
            tickCounter = 0;
            poll();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /** Polls the live stats ~every 1.5s while the overlay is on. Call each client tick. */
    public static void tick() {
        if (!enabled) {
            return;
        }
        if (tickCounter++ % 30 == 0) {
            poll();
        }
    }

    private static void poll() {
        Integer serverId = ServerStateManager.getEffectiveServerId();
        if (serverId == null) {
            return;
        }
        ApiClient.fetchServerLive(serverId).thenAccept(data ->
                Minecraft.getInstance().execute(() -> liveData = data));
    }

    //? if >=26.1 {
    /*private static void renderHud(GuiGraphicsExtractor ctx) {
    *///?} else {
    private static void renderHud(GuiGraphics ctx) {
    //?}
        if (!enabled) {
            return;
        }
        ServerLiveData live = liveData;
        Minecraft mc = Minecraft.getInstance();
        if (live == null || mc.options.hideGui) {
            return;
        }

        int right = mc.getWindow().getGuiScaledWidth() - 4;
        int y = 4;
        String name = ServerStateManager.getEffectiveServerName();
        line(ctx, mc, right, y, name != null ? name : "Server", 0xFFFFFFFF);
        line(ctx, mc, right, y + 11, "CPU " + live.getCpuPercent() + "%",
                0xFF000000 | Gauges.colorFor(live.getCpuPercent()));
        line(ctx, mc, right, y + 22, "RAM " + live.getMemoryCurrent() + "/" + live.getMemoryLimit() + " Mo",
                0xFF000000 | Gauges.colorFor(live.getMemoryPercent()));
        line(ctx, mc, right, y + 33, "Joueurs " + live.getCurrentPlayers() + "/" + live.getMaxPlayers(),
                0xFFFFFFFF);
    }

    //? if >=26.1 {
    /*private static void line(GuiGraphicsExtractor ctx, Minecraft mc, int right, int y, String s, int color) {
    *///?} else {
    private static void line(GuiGraphics ctx, Minecraft mc, int right, int y, String s, int color) {
    //?}
        int w = mc.font.width(s);
        ctx.fill(right - w - 3, y - 1, right + 1, y + mc.font.lineHeight, 0x90000000);
        Gfx.text(ctx, mc.font, s, right - w, y, color, false);
    }
}
