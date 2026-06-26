package fr.minestrator.helper.screen;

import fr.minestrator.helper.util.AnsiParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Live server console: streams the last console lines (ANSI colours, word-wrapped),
 * shows a live stats header (CPU / RAM / disk gauges + players), and sends commands.
 * Scrollable (wheel + draggable scrollbar); the view is frozen while scrolled up so
 * incoming lines don't shift what you're reading. Polls every ~1.5s.
 */
public class ConsoleCommandScreen extends Screen {
    private final ConsoleCommandLogic logic;
    private EditBox commandField;
    private Button sendButton;
    private Button downButton;
    private int tickCounter = 0;

    private int scrollOffset = 0; // lines scrolled up from the bottom (0 = follow latest)
    private boolean draggingScrollbar = false;
    private List<List<AnsiParser.Segment>> frozen; // snapshot shown while scrolled up
    private int lastAreaTop, lastAreaBottom, lastMaxScroll;

    public ConsoleCommandScreen() {
        super(Component.translatable("minestratorhelper.console.title"));
        this.logic = new ConsoleCommandLogic();
    }

    @Override
    protected void init() {
        int inputY = this.height - 28;
        this.commandField = new EditBox(this.font, 8, inputY, this.width - 90, 20,
                Component.translatable("minestratorhelper.console.placeholder"));
        this.commandField.setMaxLength(256);
        this.addRenderableWidget(this.commandField);
        this.setInitialFocus(this.commandField);

        this.sendButton = Button.builder(Component.translatable("minestratorhelper.console.send"),
                button -> sendCommand()).bounds(this.width - 78, inputY, 70, 20).build();
        this.addRenderableWidget(this.sendButton);

        this.downButton = Button.builder(Component.literal("↓"),
                button -> jumpToBottom()).bounds(this.width - 36, inputY - 24, 20, 20).build();
        this.downButton.visible = false;
        this.addRenderableWidget(this.downButton);

        logic.refreshLogs(null);
        logic.refreshLive(null);
    }

    private void jumpToBottom() {
        scrollOffset = 0;
        frozen = null;
    }

    @Override
    public void tick() {
        super.tick();
        tickCounter++;
        if (tickCounter % 30 == 0) { // ~1.5s at 20 tps
            logic.refreshLogs(null);
            logic.refreshLive(null);
        }
    }

    private void sendCommand() {
        String cmd = this.commandField.getValue();
        if (cmd == null || cmd.trim().isEmpty()) {
            return;
        }
        this.sendButton.active = false;
        logic.sendCommand(cmd,
                () -> this.minecraft.execute(() -> this.commandField.setValue("")),
                error -> {
                },
                () -> this.minecraft.execute(() -> {
                    this.sendButton.active = true;
                    logic.refreshLogs(null);
                }));
    }

    private void scrollBy(int lines) {
        scrollOffset = Math.max(0, scrollOffset + lines);
    }

    /** Maps a Y inside the scrollbar track to a scroll position. */
    private boolean scrollbarTo(double mx, double my) {
        if (lastMaxScroll <= 0) {
            return false;
        }
        if (mx < this.width - 13 || mx > this.width - 2 || my < lastAreaTop || my > lastAreaBottom) {
            return false;
        }
        int trackH = lastAreaBottom - lastAreaTop;
        double frac = Math.max(0.0, Math.min(1.0, (my - lastAreaTop) / (double) trackH));
        scrollOffset = (int) Math.round(lastMaxScroll * (1.0 - frac));
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > lastMaxScroll) scrollOffset = lastMaxScroll;
        if (scrollOffset == 0) frozen = null;
        return true;
    }

    private boolean handleConsoleKey(int keyCode) {
        if ((keyCode == 257 || keyCode == 335) && this.commandField.isFocused()) {
            sendCommand();
            return true;
        }
        return false;
    }

    //? if >=1.21.11 {
    /*@Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (handleConsoleKey(event.key())) return true;
        return super.keyPressed(event);
    }
    *///?} else {
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handleConsoleKey(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?}

    //? if >=1.21 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollBy((int) Math.signum(scrollY) * 3);
        return true;
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollBy((int) Math.signum(amount) * 3);
        return true;
    }
    *///?}

    //? if >=1.21.11 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
        if (scrollbarTo(event.x(), event.y())) { draggingScrollbar = true; return true; }
        return super.mouseClicked(event, doubled);
    }
    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) { scrollbarTo(event.x(), event.y()); return true; }
        return super.mouseDragged(event, dragX, dragY);
    }
    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (draggingScrollbar) { draggingScrollbar = false; return true; }
        return super.mouseReleased(event);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (scrollbarTo(mx, my)) { draggingScrollbar = true; return true; }
        return super.mouseClicked(mx, my, button);
    }
    @Override
    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        if (draggingScrollbar) { scrollbarTo(mx, my); return true; }
        return super.mouseDragged(mx, my, button, dragX, dragY);
    }
    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingScrollbar) { draggingScrollbar = false; return true; }
        return super.mouseReleased(mx, my, button);
    }
    //?}

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        //? if <1.21
        /*this.renderBackground(context);*/
        super.render(context, mouseX, mouseY, delta);

        int areaTop = 52;
        int areaBottom = this.height - 34;
        context.fill(4, areaTop - 2, this.width - 4, areaBottom + 2, 0xC0000000);

        String serverName = logic.getServerName();
        String header = "Console — " + (serverName != null ? serverName : "?");
        context.drawString(this.font, header, 8, 8, 0xFFFFFFFF, true);

        var live = logic.getLiveData();
        if (live != null) {
            int barW = 90, gap = 10, bx = 8, by = 20;
            int playerPct = live.getMaxPlayers() > 0 ? live.getCurrentPlayers() * 100 / live.getMaxPlayers() : 0;
            Gauges.drawGauge(context, this.font, bx, by, barW, live.getCpuPercent(),
                    "CPU", live.getCpuPercent() + "%");
            Gauges.drawGauge(context, this.font, bx + (barW + gap), by, barW, live.getMemoryPercent(),
                    "RAM", live.getMemoryCurrent() + "/" + live.getMemoryLimit() + " Mo");
            Gauges.drawGauge(context, this.font, bx + (barW + gap) * 2, by, barW, live.getDiskPercent(),
                    "Disk", live.getDiskPercent() + "%");
            Gauges.drawGauge(context, this.font, bx + (barW + gap) * 3, by, barW, playerPct,
                    "Joueurs", live.getCurrentPlayers() + "/" + live.getMaxPlayers());
        }

        // Freeze the displayed logs while scrolled up so new lines don't shift the view.
        if (scrollOffset > 0) {
            if (frozen == null) frozen = logic.getLogLines();
        } else {
            frozen = null;
        }
        List<List<AnsiParser.Segment>> source = frozen != null ? frozen : logic.getLogLines();

        // Logs: ANSI colours, word-wrapped, scrollable, clipped to the log area.
        int maxWidth = this.width - 22; // leave room for the scrollbar
        int lineHeight = this.font.lineHeight + 1;
        List<FormattedCharSequence> visual = new ArrayList<>();
        for (List<AnsiParser.Segment> logical : source) {
            MutableComponent comp = Component.empty();
            for (AnsiParser.Segment seg : logical) {
                comp.append(Component.literal(seg.text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(seg.color))));
            }
            visual.addAll(this.font.split(comp, maxWidth));
        }
        int visibleCount = Math.max(0, (areaBottom - areaTop) / lineHeight);
        int maxScroll = Math.max(0, visual.size() - visibleCount);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        int startIdx = maxScroll - scrollOffset;
        int endIdx = Math.min(visual.size(), startIdx + visibleCount);

        this.lastAreaTop = areaTop;
        this.lastAreaBottom = areaBottom;
        this.lastMaxScroll = maxScroll;

        context.enableScissor(4, areaTop, this.width - 4, areaBottom);
        int y = areaTop;
        for (int i = startIdx; i < endIdx; i++) {
            context.drawString(this.font, visual.get(i), 8, y, 0xFFFFFFFF, false);
            y += lineHeight;
        }
        context.disableScissor();

        // Scrollbar on the right (click / drag to navigate).
        if (maxScroll > 0) {
            int sbX = this.width - 9, sbW = 4;
            int trackH = areaBottom - areaTop;
            context.fill(sbX, areaTop, sbX + sbW, areaBottom, 0x30FFFFFF);
            int thumbH = Math.max(16, trackH * visibleCount / visual.size());
            int thumbY = areaTop + (trackH - thumbH) * (maxScroll - scrollOffset) / maxScroll;
            context.fill(sbX, thumbY, sbX + sbW, thumbY + thumbH, draggingScrollbar ? 0xFFFFFFFF : 0xB0FFFFFF);
        }

        // "Back to bottom" button appears only while scrolled up.
        this.downButton.visible = scrollOffset > 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
