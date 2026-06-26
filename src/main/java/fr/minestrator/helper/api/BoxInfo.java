package fr.minestrator.helper.api;

import java.util.ArrayList;
import java.util.List;

public class BoxInfo {
    private int id;
    private String name;
    private String hashSupport;
    private String offer;
    private int cpu;
    private int ram;
    private int disk;
    private int tendDays;
    private boolean isExpired;
    private boolean isSuspended;
    private List<ServerInfo> servers = new ArrayList<>();

    public BoxInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHashSupport() {
        return hashSupport;
    }

    public void setHashSupport(String hashSupport) {
        this.hashSupport = hashSupport;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public int getTendDays() {
        return tendDays;
    }

    public void setTendDays(int tendDays) {
        this.tendDays = tendDays;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspended(boolean suspended) {
        isSuspended = suspended;
    }

    public List<ServerInfo> getServers() {
        return servers;
    }

    public void setServers(List<ServerInfo> servers) {
        this.servers = servers;
    }

    public void addServer(ServerInfo server) {
        this.servers.add(server);
    }
}
