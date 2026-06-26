package fr.minestrator.helper.screen;

/**
 * Manages the current server connection state.
 * This is shared across all versions.
 */
public class ServerStateManager {
    private static Integer currentServerId = null;
    private static String currentServerName = null;

    public static void setCurrentServer(int serverId, String serverName) {
        currentServerId = serverId;
        currentServerName = serverName;
    }

    public static void clearCurrentServer() {
        currentServerId = null;
        currentServerName = null;
    }

    public static Integer getCurrentServerId() {
        return currentServerId;
    }

    public static String getCurrentServerName() {
        return currentServerName;
    }

    public static boolean isOnHostedServer() {
        return currentServerId != null;
    }
}
