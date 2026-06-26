package fr.minestrator.helper.api;

public class ServerInfo {
    private int id;
    private String name;
    private String hashSupport;
    private int idMybox;
    private String ip;
    private int port;
    private String dns;
    private String eggName;
    private boolean isExpired;
    private boolean isSuspended;
    private boolean isDisabled;
    private boolean isBedrock;

    public ServerInfo() {
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

    public int getIdMybox() {
        return idMybox;
    }

    public void setIdMybox(int idMybox) {
        this.idMybox = idMybox;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getEggName() {
        return eggName;
    }

    public void setEggName(String eggName) {
        this.eggName = eggName;
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

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public boolean isBedrock() {
        return isBedrock;
    }

    public void setBedrock(boolean bedrock) {
        isBedrock = bedrock;
    }

    /**
     * Returns the connection address (DNS if available, otherwise IP:port)
     */
    public String getConnectionAddress() {
        if (dns != null && !dns.isEmpty()) {
            return dns;
        }
        if (port == 25565) {
            return ip;
        }
        return ip + ":" + port;
    }

    /**
     * Checks if the server is playable (not expired, suspended or disabled)
     */
    public boolean isPlayable() {
        return !isExpired && !isSuspended && !isDisabled;
    }

    /**
     * Checks if this is a Minecraft server
     */
    public boolean isMinecraft() {
        return "Minecraft".equalsIgnoreCase(eggName);
    }
}
