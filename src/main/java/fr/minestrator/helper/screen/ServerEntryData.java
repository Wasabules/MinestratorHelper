package fr.minestrator.helper.screen;

import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.api.BoxInfo;
import fr.minestrator.helper.api.ServerInfo;
import fr.minestrator.helper.api.ServerLiveData;

import java.util.function.Consumer;

/**
 * Holds data for a server entry in the list.
 * Shared across all Minecraft versions.
 */
public class ServerEntryData {
    private final ServerInfo server;
    private final BoxInfo box;
    private ServerLiveData liveData;
    private boolean loadingLive = false;

    public ServerEntryData(ServerInfo server, BoxInfo box) {
        this.server = server;
        this.box = box;
    }

    public ServerInfo getServer() {
        return server;
    }

    public BoxInfo getBox() {
        return box;
    }

    public ServerLiveData getLiveData() {
        return liveData;
    }

    public boolean isLoadingLive() {
        return loadingLive;
    }

    public void fetchLiveData(Consumer<ServerLiveData> onComplete) {
        if (loadingLive) return;
        loadingLive = true;

        ApiClient.fetchServerLive(server.getId()).thenAccept(data -> {
            this.liveData = data;
            this.loadingLive = false;
            if (onComplete != null) {
                onComplete.accept(data);
            }
        });
    }

    // ============ Display helpers ============

    public int getStateColor() {
        if (!server.isPlayable()) {
            return 0xFFFF5555; // Red - not playable
        }
        if (liveData == null || loadingLive) {
            return 0xFF888888; // Gray - loading
        }
        if (liveData.isOnline()) {
            return 0xFF55FF55; // Green - online
        }
        if (liveData.isStarting()) {
            return 0xFFFFFF55; // Yellow - starting
        }
        if (liveData.isStopping()) {
            return 0xFFFFAA00; // Orange - stopping
        }
        return 0xFFFF5555; // Red - offline
    }

    public String getStatusKey() {
        if (server.isExpired()) {
            return "minestratorhelper.status.expired";
        } else if (server.isSuspended()) {
            return "minestratorhelper.status.suspended";
        } else if (server.isDisabled()) {
            return "minestratorhelper.status.disabled";
        }

        if (liveData != null) {
            if (liveData.isOnline()) {
                return "minestratorhelper.status.online";
            } else if (liveData.isStarting()) {
                return "minestratorhelper.status.starting";
            } else if (liveData.isStopping()) {
                return "minestratorhelper.status.stopping";
            } else {
                return "minestratorhelper.status.offline";
            }
        }

        if (loadingLive) {
            return null; // Will display "..."
        }

        return "minestratorhelper.status.available";
    }

    public int getStatusColor() {
        if (!server.isPlayable()) {
            return 0xFF5555;
        }
        if (liveData != null) {
            if (liveData.isOnline()) {
                return 0x55FF55;
            } else if (liveData.isStarting() || liveData.isStopping()) {
                return 0xFFFF55;
            } else {
                return 0xFF5555;
            }
        }
        return 0x888888;
    }

    public String getPlayersText() {
        if (liveData != null && liveData.isOnline()) {
            return liveData.getCurrentPlayers() + "/" + liveData.getMaxPlayers() + " joueurs";
        }
        return "";
    }

    public String getVersionText() {
        if (liveData != null && liveData.getVersion() != null) {
            return liveData.getVersion();
        }
        return "";
    }

    public int getNameColor() {
        return server.isPlayable() ? 0xFFFFFF : 0xFF5555;
    }
}
