package fr.minestrator.helper.screen;

import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.api.BoxInfo;
import fr.minestrator.helper.api.ServerInfo;
import fr.minestrator.helper.api.ServerLiveData;
import fr.minestrator.helper.config.ModConfig;
import fr.minestrator.helper.MinestratorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Contains all business logic for the hosted servers screen.
 * This class is shared across all Minecraft versions.
 */
public class HostedServersLogic {
    private List<BoxInfo> boxes = new ArrayList<>();
    private boolean loading = false;
    private String errorMessage = null;

    private final Consumer<List<BoxInfo>> onBoxesLoaded;
    private final Consumer<String> onError;
    private final Runnable onLoadingStateChanged;

    public HostedServersLogic(Consumer<List<BoxInfo>> onBoxesLoaded,
                              Consumer<String> onError,
                              Runnable onLoadingStateChanged) {
        this.onBoxesLoaded = onBoxesLoaded;
        this.onError = onError;
        this.onLoadingStateChanged = onLoadingStateChanged;
    }

    public boolean isConfigured() {
        return ModConfig.get().isConfigured();
    }

    public void refreshServers(Runnable onComplete) {
        if (!isConfigured()) {
            this.errorMessage = "minestratorhelper.servers.not_configured";
            onError.accept(errorMessage);
            return;
        }

        this.loading = true;
        this.errorMessage = null;
        onLoadingStateChanged.run();

        ApiClient.fetchBoxes().thenAccept(boxList -> {
            this.boxes = boxList;
            this.loading = false;

            if (boxList.isEmpty()) {
                this.errorMessage = "minestratorhelper.servers.no_servers";
            }

            onBoxesLoaded.accept(boxList);
            onLoadingStateChanged.run();
            if (onComplete != null) onComplete.run();
        }).exceptionally(e -> {
            this.loading = false;
            this.errorMessage = "minestratorhelper.servers.error";
            MinestratorHelper.LOGGER.error("Error fetching servers", e);
            onError.accept(errorMessage);
            onLoadingStateChanged.run();
            if (onComplete != null) onComplete.run();
            return null;
        });
    }

    public CompletableFuture<Boolean> startServer(int serverId) {
        return ApiClient.startServer(serverId);
    }

    public CompletableFuture<Boolean> stopServer(int serverId) {
        return ApiClient.stopServer(serverId);
    }

    public void connectToServer(ServerInfo server) {
        ServerStateManager.setCurrentServer(server.getId(), server.getName());
    }

    public List<BoxInfo> getBoxes() {
        return boxes;
    }

    public boolean isLoading() {
        return loading;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Determines the button states based on selected server
     */
    public static class ButtonStates {
        public boolean joinEnabled = false;
        public boolean startEnabled = false;
        public boolean stopEnabled = false;
        public String startText = "minestratorhelper.servers.start";
        public String stopText = "minestratorhelper.servers.stop";

        public static ButtonStates calculate(ServerInfo server, ServerLiveData liveData) {
            ButtonStates states = new ButtonStates();

            if (server == null) {
                return states;
            }

            // Join button: active if server is playable and online
            boolean canJoin = server.isPlayable() && liveData != null && liveData.isOnline();
            states.joinEnabled = canJoin;

            if (!server.isPlayable()) {
                // Server not playable (expired, suspended, disabled)
                states.startEnabled = false;
                states.stopEnabled = false;
            } else if (liveData == null) {
                // Live data not loaded yet
                states.startEnabled = false;
                states.stopEnabled = false;
            } else if (liveData.isOnline()) {
                // Server online: can stop, cannot start
                states.startEnabled = false;
                states.stopEnabled = true;
            } else if (liveData.isStarting()) {
                // Server starting: both disabled
                states.startEnabled = false;
                states.stopEnabled = false;
                states.startText = "minestratorhelper.servers.starting";
            } else if (liveData.isStopping()) {
                // Server stopping: both disabled
                states.startEnabled = false;
                states.stopEnabled = false;
                states.stopText = "minestratorhelper.servers.stopping";
            } else {
                // Server offline: can start, cannot stop
                states.startEnabled = true;
                states.stopEnabled = false;
            }

            return states;
        }
    }
}
