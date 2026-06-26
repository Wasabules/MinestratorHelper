package fr.minestrator.helper.api;

import com.google.gson.annotations.SerializedName;

public class ServerLiveData {
    private String state;
    private String status;
    private Stats stats;

    public String getState() {
        return state;
    }

    public boolean isOnline() {
        return "online".equalsIgnoreCase(state);
    }

    public boolean isOffline() {
        return "offline".equalsIgnoreCase(state);
    }

    public boolean isStarting() {
        return "starting".equalsIgnoreCase(state);
    }

    public boolean isStopping() {
        return "stopping".equalsIgnoreCase(state);
    }

    public Stats getStats() {
        return stats;
    }

    public int getCurrentPlayers() {
        if (stats != null && stats.players != null) {
            return stats.players.current;
        }
        return 0;
    }

    public int getMaxPlayers() {
        if (stats != null && stats.players != null) {
            return stats.players.limit;
        }
        return 0;
    }

    public String getVersion() {
        if (stats != null) {
            return stats.version;
        }
        return null;
    }

    // ===== Live resource usage (from /live, used by the monitoring panel) =====

    public int getCpuPercent() {
        return (stats != null && stats.cpu != null) ? stats.cpu.percent : 0;
    }

    public int getMemoryPercent() {
        return (stats != null && stats.memory != null) ? stats.memory.percent : 0;
    }

    public int getMemoryCurrent() {
        return (stats != null && stats.memory != null) ? stats.memory.current : 0;
    }

    public int getMemoryLimit() {
        return (stats != null && stats.memory != null) ? stats.memory.limit : 0;
    }

    public int getDiskPercent() {
        return (stats != null && stats.disk != null) ? stats.disk.percent : 0;
    }

    public int getDiskCurrent() {
        return (stats != null && stats.disk != null) ? stats.disk.current : 0;
    }

    public int getDiskLimit() {
        return (stats != null && stats.disk != null) ? stats.disk.limit : 0;
    }

    public String[] getPlayerList() {
        if (stats != null && stats.players != null && stats.players.list != null) {
            return stats.players.list;
        }
        return new String[0];
    }

    public static class Stats {
        private String state;
        private Cpu cpu;
        private Memory memory;
        private Disk disk;
        private Players players;
        private String version;
        private String hostname;

        public Players getPlayers() {
            return players;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class Cpu {
        private double current;
        private int dedicated;
        private int limit;
        private int percent;
        @SerializedName("is_bursting")
        private boolean isBursting;
    }

    public static class Memory {
        private int current;
        private int limit;
        private int percent;
    }

    public static class Disk {
        private int current;
        private int limit;
        private int percent;
    }

    public static class Players {
        private int current;
        private int limit;
        private String[] list;

        public int getCurrent() {
            return current;
        }

        public int getLimit() {
            return limit;
        }
    }
}
