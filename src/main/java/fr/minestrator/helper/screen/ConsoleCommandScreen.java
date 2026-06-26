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
 * Polls every ~1.5s.
 */
public class ConsoleCommandScreen extends Screen {
    private final ConsoleCommandLogic logic;
    private EditBox commandField;
    private Button sendButton;
    private int tickCounter = 0;

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

        logic.refreshLogs(null);
        logic.refreshLive(null);
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

        // Logs: ANSI colours, word-wrapped, auto-scrolled to the bottom.
        int maxWidth = this.width - 16;
        int lineHeight = this.font.lineHeight + 1;
        List<FormattedCharSequence> visual = new ArrayList<>();
        for (List<AnsiParser.Segment> logical : logic.getLogLines()) {
            MutableComponent comp = Component.empty();
            for (AnsiParser.Segment seg : logical) {
                comp.append(Component.literal(seg.text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(seg.color))));
            }
            visual.addAll(this.font.split(comp, maxWidth));
        }
        int visibleCount = Math.max(0, (areaBottom - areaTop) / lineHeight);
        int startIdx = Math.max(0, visual.size() - visibleCount);
        int y = areaTop;
        for (int i = startIdx; i < visual.size(); i++) {
            context.drawString(this.font, visual.get(i), 8, y, 0xFFFFFFFF, false);
            y += lineHeight;
        }
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
