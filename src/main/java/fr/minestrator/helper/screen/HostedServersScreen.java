package fr.minestrator.helper.screen;

import fr.minestrator.helper.api.BoxInfo;
import fr.minestrator.helper.api.ServerInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class HostedServersScreen extends Screen {
    private final Screen parent;
    private final HostedServersLogic logic;
    private List<BoxInfo> boxes = new ArrayList<>();
    private ServerListWidget serverListWidget;
    private Button joinButton;
    private Button startButton;
    private Button stopButton;
    private Button refreshButton;

    public HostedServersScreen(Screen parent) {
        super(Component.translatable("minestratorhelper.servers.title"));
        this.parent = parent;
        this.logic = new HostedServersLogic(
                this::onBoxesLoaded, this::onError, this::onLoadingStateChanged);
    }

    private void onBoxesLoaded(List<BoxInfo> boxList) {
        this.minecraft.execute(() -> {
            this.boxes = boxList;
            this.serverListWidget.setBoxes(boxList);
        });
    }

    private void onError(String errorKey) {
        // Error surfaced via logic.getErrorMessage()
    }

    private void onLoadingStateChanged() {
        this.minecraft.execute(() -> this.refreshButton.active = !logic.isLoading());
    }

    @Override
    protected void init() {
        // Server list added first: on 1.20.1 the list paints top/bottom edge gradients over the
        // area outside its bounds, which would hide any widget added before it (the top-bar buttons).
        this.serverListWidget = new ServerListWidget(this.minecraft, this.width, this.height, 72, this.height - 32, 42);
        this.serverListWidget.setBoxes(this.boxes);
        this.addRenderableWidget(this.serverListWidget);

        // Top bar: Back (left) and Settings (right) — always reachable, even on small screens.
        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"),
                button -> this.minecraft.setScreen(this.parent)).bounds(6, 6, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("minestratorhelper.servers.config"),
                button -> this.minecraft.setScreen(new ConfigScreen(this))).bounds(this.width - 76, 6, 70, 20).build());

        // Bottom: action buttons for the selected server.
        int buttonY = this.height - 28;
        int buttonWidth = 80;
        int spacing = 4;
        int startX = this.width / 2 - (buttonWidth * 4 + spacing * 3) / 2;

        this.joinButton = Button.builder(Component.translatable("minestratorhelper.servers.join"),
                button -> joinSelectedServer()).bounds(startX, buttonY, buttonWidth, 20).build();
        this.joinButton.active = false;
        this.addRenderableWidget(this.joinButton);

        this.startButton = Button.builder(Component.translatable("minestratorhelper.servers.start"),
                button -> startSelectedServer()).bounds(startX + buttonWidth + spacing, buttonY, buttonWidth, 20).build();
        this.startButton.active = false;
        this.addRenderableWidget(this.startButton);

        this.stopButton = Button.builder(Component.translatable("minestratorhelper.servers.stop"),
                button -> stopSelectedServer()).bounds(startX + (buttonWidth + spacing) * 2, buttonY, buttonWidth, 20).build();
        this.stopButton.active = false;
        this.addRenderableWidget(this.stopButton);

        this.refreshButton = Button.builder(Component.translatable("minestratorhelper.servers.refresh"),
                button -> refreshServers()).bounds(startX + (buttonWidth + spacing) * 3, buttonY, buttonWidth, 20).build();
        this.addRenderableWidget(this.refreshButton);

        refreshServers();
    }

    private void refreshServers() {
        logic.refreshServers(null);
    }

    private void joinSelectedServer() {
        ServerListWidget.ServerEntry entry = this.serverListWidget.getSelectedServer();
        if (entry != null) {
            connectToServer(entry.getServerInfo());
        }
    }

    private void startSelectedServer() {
        ServerListWidget.ServerEntry entry = this.serverListWidget.getSelectedServer();
        if (entry == null) return;
        int serverId = entry.getServerInfo().getId();
        this.startButton.active = false;
        this.startButton.setMessage(Component.translatable("minestratorhelper.servers.starting"));
        logic.startServer(serverId).thenAccept(success -> this.minecraft.execute(() -> {
            if (success) {
                this.startButton.setMessage(Component.translatable("minestratorhelper.servers.started"));
                refreshLiveDataDelayed();
            } else {
                this.startButton.setMessage(Component.translatable("minestratorhelper.servers.start_failed"));
                this.startButton.active = true;
            }
        }));
    }

    private void stopSelectedServer() {
        ServerListWidget.ServerEntry entry = this.serverListWidget.getSelectedServer();
        if (entry == null) return;
        int serverId = entry.getServerInfo().getId();
        this.stopButton.active = false;
        this.stopButton.setMessage(Component.translatable("minestratorhelper.servers.stopping"));
        logic.stopServer(serverId).thenAccept(success -> this.minecraft.execute(() -> {
            if (success) {
                this.stopButton.setMessage(Component.translatable("minestratorhelper.servers.stopped"));
                refreshLiveDataDelayed();
            } else {
                this.stopButton.setMessage(Component.translatable("minestratorhelper.servers.stop_failed"));
                this.stopButton.active = true;
            }
        }));
    }

    private void refreshLiveDataDelayed() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                this.minecraft.execute(() -> {
                    this.serverListWidget.refreshLiveData();
                    updateButtonStates();
                });
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void connectToServer(ServerInfo server) {
        logic.connectToServer(server);
        //? if >=1.21 {
        ServerData serverData = new ServerData(
                server.getName(), server.getConnectionAddress(), ServerData.Type.OTHER);
        ConnectScreen.startConnecting(
                this, this.minecraft,
                ServerAddress.parseString(server.getConnectionAddress()),
                serverData, false, null);
        //?} else {
        /*ServerData serverData = new ServerData(
                server.getName(), server.getConnectionAddress(), false);
        ConnectScreen.startConnecting(
                this, this.minecraft,
                ServerAddress.parseString(server.getConnectionAddress()),
                serverData, false);
        *///?}
    }

    public void updateButtonStates() {
        ServerListWidget.ServerEntry entry = this.serverListWidget.getSelectedServer();
        ServerInfo server = entry != null ? entry.getServerInfo() : null;
        var liveData = entry != null ? entry.getLiveData() : null;
        HostedServersLogic.ButtonStates states = HostedServersLogic.ButtonStates.calculate(server, liveData);
        this.joinButton.active = states.joinEnabled;
        this.startButton.active = states.startEnabled;
        this.stopButton.active = states.stopEnabled;
        this.startButton.setMessage(Component.translatable(states.startText));
        this.stopButton.setMessage(Component.translatable(states.stopText));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        //? if <1.21
        /*this.renderBackground(context);*/
        super.render(context, mouseX, mouseY, delta);
        String titleStr = this.title.getString();
        context.drawString(this.font, titleStr,
                this.width / 2 - this.font.width(titleStr) / 2, 12, 0xFFFFFFFF, true);

        // Live stats gauges for the selected server (monitoring)
        ServerListWidget.ServerEntry sel = this.serverListWidget != null
                ? this.serverListWidget.getSelectedServer() : null;
        if (sel != null && sel.getLiveData() != null) {
            var live = sel.getLiveData();
            int barW = 90, gap = 10;
            int total = (barW + gap) * 4 - gap;
            int bx = this.width / 2 - total / 2;
            int gy = 40;
            int pad = 8;
            int px = bx - pad, py = 30, pw = total + pad * 2, ph = Gauges.HEIGHT + 14;
            context.fill(px, py, px + pw, py + ph, 0xD0141414);
            Gauges.drawBorder(context, px, py, pw, ph, 0x40FFFFFF);
            int playerPct = live.getMaxPlayers() > 0 ? live.getCurrentPlayers() * 100 / live.getMaxPlayers() : 0;
            Gauges.drawGauge(context, this.font, bx, gy, barW, live.getCpuPercent(),
                    "CPU", live.getCpuPercent() + "%");
            Gauges.drawGauge(context, this.font, bx + (barW + gap), gy, barW, live.getMemoryPercent(),
                    "RAM", live.getMemoryCurrent() + "/" + live.getMemoryLimit() + " Mo");
            Gauges.drawGauge(context, this.font, bx + (barW + gap) * 2, gy, barW, live.getDiskPercent(),
                    "Disk", live.getDiskPercent() + "%");
            Gauges.drawGauge(context, this.font, bx + (barW + gap) * 3, gy, barW, playerPct,
                    "Joueurs", live.getCurrentPlayers() + "/" + live.getMaxPlayers());
        } else if (!logic.isLoading() && logic.getErrorMessage() == null) {
            String hint = "Sélectionne un serveur pour voir ses statistiques";
            context.drawString(this.font, hint,
                    this.width / 2 - this.font.width(hint) / 2, 44, 0xFF888888, true);
        }

        if (logic.isLoading()) {
            String loadingStr = Component.translatable("minestratorhelper.servers.loading").getString();
            context.drawString(this.font, loadingStr,
                    this.width / 2 - this.font.width(loadingStr) / 2, this.height / 2, 0xFFAAAAAA, true);
        } else if (logic.getErrorMessage() != null) {
            String errorStr = Component.translatable(logic.getErrorMessage()).getString();
            context.drawString(this.font, errorStr,
                    this.width / 2 - this.font.width(errorStr) / 2, this.height / 2, 0xFFFF5555, true);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
