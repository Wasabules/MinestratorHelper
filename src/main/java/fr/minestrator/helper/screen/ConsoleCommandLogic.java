package fr.minestrator.helper.screen;

import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.api.ServerLiveData;
import fr.minestrator.helper.util.AnsiParser;
import fr.minestrator.helper.util.LogLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Contains all business logic for the console command screen.
 * Shared across all Minecraft versions.
 */
public class ConsoleCommandLogic {
    private String statusMessage = null;
    private boolean statusSuccess = false;
    private long statusTime = 0;
    private boolean sending = false;

    private List<LogLine> logLines = new ArrayList<>();
    private ServerLiveData liveData;

    public Integer getServerId() {
        return ServerStateManager.getEffectiveServerId();
    }

    /** Fetches the console logs and parses ANSI colours; runs onUpdate on the calling thread of the future. */
    public void refreshLogs(Runnable onUpdate) {
        Integer serverId = getServerId();
        if (serverId == null) return;
        ApiClient.fetchConsoleLogs(serverId).thenAccept(lines -> {
            List<LogLine> parsed = new ArrayList<>();
            for (String line : lines) {
                parsed.add(new LogLine(AnsiParser.parse(line), LogLine.detectLevel(line)));
            }
            this.logLines = parsed;
            if (onUpdate != null) onUpdate.run();
        });
    }

    /** Fetches live stats (CPU/RAM/players) for the monitoring header. */
    public void refreshLive(Runnable onUpdate) {
        Integer serverId = getServerId();
        if (serverId == null) return;
        ApiClient.fetchServerLive(serverId).thenAccept(data -> {
            this.liveData = data;
            if (onUpdate != null) onUpdate.run();
        });
    }

    public List<LogLine> getLogLines() {
        return logLines;
    }

    public ServerLiveData getLiveData() {
        return liveData;
    }

    public String getServerName() {
        return ServerStateManager.getEffectiveServerName();
    }

    public boolean isOnHostedServer() {
        return ServerStateManager.isOnHostedServer();
    }

    public void sendCommand(String command, Runnable onSuccess, Consumer<String> onFailure, Runnable onComplete) {
        if (command == null || command.trim().isEmpty()) {
            setStatus("minestratorhelper.console.empty", false);
            onFailure.accept(statusMessage);
            return;
        }

        Integer serverId = getServerId();
        if (serverId == null) {
            setStatus("minestratorhelper.command.not_on_hosted", false);
            onFailure.accept(statusMessage);
            return;
        }

        sending = true;

        ApiClient.sendConsoleCommand(serverId, command.trim()).thenAccept(success -> {
            sending = false;
            if (success) {
                setStatus("minestratorhelper.console.sent", true);
                onSuccess.run();
            } else {
                setStatus("minestratorhelper.console.failed", false);
                onFailure.accept(statusMessage);
            }
            if (onComplete != null) onComplete.run();
        });
    }

    private void setStatus(String message, boolean success) {
        this.statusMessage = message;
        this.statusSuccess = success;
        this.statusTime = System.currentTimeMillis();
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isStatusSuccess() {
        return statusSuccess;
    }

    public long getStatusTime() {
        return statusTime;
    }

    public boolean shouldShowStatus() {
        return statusMessage != null && System.currentTimeMillis() - statusTime < 3000;
    }

    public boolean isSending() {
        return sending;
    }
}
