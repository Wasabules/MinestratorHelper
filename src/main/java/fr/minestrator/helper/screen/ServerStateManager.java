package fr.minestrator.helper.screen;

/**
 * Tracks which hosted server the console (F6) and pause-menu power buttons act on.
 *
 * Two sources:
 *  - {@code currentServer}: auto-detected when you Join a server through the mod.
 *  - {@code pinnedServer}: manually fixed via "My Servers". A pin overrides the
 *    auto-detection, which is what makes the console usable behind a proxy
 *    (Bungee/Velocity): the connected IP is the proxy, not the backend, so the
 *    auto value is wrong/absent — pinning lets you target the real server.
 *
 * Shared across all versions/loaders.
 */
public class ServerStateManager {
    private static Integer currentServerId = null;
    private static String currentServerName = null;
    private static Integer pinnedServerId = null;
    private static String pinnedServerName = null;

    public static void setCurrentServer(int serverId, String serverName) {
        currentServerId = serverId;
        currentServerName = serverName;
    }

    public static void clearCurrentServer() {
        currentServerId = null;
        currentServerName = null;
    }

    /** Pin a server so the console/power actions target it regardless of the connected IP. */
    public static void pinServer(int serverId, String serverName) {
        pinnedServerId = serverId;
        pinnedServerName = serverName;
    }

    public static void clearPin() {
        pinnedServerId = null;
        pinnedServerName = null;
    }

    public static boolean isPinned() {
        return pinnedServerId != null;
    }

    public static Integer getPinnedServerId() {
        return pinnedServerId;
    }

    /** The server to act on: the pinned one if set, otherwise the auto-detected current one. */
    public static Integer getEffectiveServerId() {
        return pinnedServerId != null ? pinnedServerId : currentServerId;
    }

    public static String getEffectiveServerName() {
        return pinnedServerId != null ? pinnedServerName : currentServerName;
    }

    public static Integer getCurrentServerId() {
        return currentServerId;
    }

    public static String getCurrentServerName() {
        return currentServerName;
    }

    /** True when there is a server to act on (pinned or auto-detected). */
    public static boolean isOnHostedServer() {
        return getEffectiveServerId() != null;
    }
}
