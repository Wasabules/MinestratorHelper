package fr.minestrator.helper.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.minestrator.helper.config.ModConfig;
import fr.minestrator.helper.MinestratorHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static final Gson GSON = new Gson();
    private static final int TIMEOUT = 10000;
    private static final String BASE_URL = ModConfig.API_BASE_URL;

    /**
     * Fetches the connected user's information
     */
    public static CompletableFuture<UserInfo> fetchUser() {
        return CompletableFuture.supplyAsync(() -> {
            if (!ModConfig.get().isConfigured()) {
                MinestratorHelper.LOGGER.warn("API not configured");
                return null;
            }

            try {
                String response = doGet(BASE_URL + "/user/");
                JsonObject json = GSON.fromJson(response, JsonObject.class);

                // Navigate through the structure: api.data.user.datas
                if (json.has("api")) {
                    JsonObject api = json.getAsJsonObject("api");
                    if (api.has("data")) {
                        JsonObject data = api.getAsJsonObject("data");
                        if (data.has("user")) {
                            JsonObject user = data.getAsJsonObject("user");
                            if (user.has("datas")) {
                                JsonObject datas = user.getAsJsonObject("datas");
                                UserInfo userInfo = new UserInfo();
                                userInfo.setId(datas.get("id").getAsInt());
                                userInfo.setPseudo(getStringOrNull(datas, "pseudo"));
                                userInfo.setMail(getStringOrNull(datas, "mail"));
                                userInfo.setMoney(datas.has("money") ? datas.get("money").getAsInt() : 0);
                                userInfo.setLang(getStringOrNull(datas, "lang"));

                                // Cache the user ID
                                ModConfig.get().setCachedUserId(userInfo.getId());

                                return userInfo;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                MinestratorHelper.LOGGER.error("Failed to fetch user", e);
            }

            return null;
        });
    }

    /**
     * Fetches the list of boxes with their Minecraft servers
     */
    public static CompletableFuture<List<BoxInfo>> fetchBoxes() {
        return CompletableFuture.supplyAsync(() -> {
            List<BoxInfo> boxes = new ArrayList<>();

            if (!ModConfig.get().isConfigured()) {
                MinestratorHelper.LOGGER.warn("API not configured");
                return boxes;
            }

            // First get user ID if not cached
            Integer userId = ModConfig.get().getCachedUserId();
            if (userId == null) {
                try {
                    UserInfo user = fetchUser().get();
                    if (user == null) {
                        MinestratorHelper.LOGGER.error("Failed to get user ID");
                        return boxes;
                    }
                    userId = user.getId();
                } catch (Exception e) {
                    MinestratorHelper.LOGGER.error("Failed to fetch user for ID", e);
                    return boxes;
                }
            }

            try {
                String response = doGet(BASE_URL + "/user/" + userId + "/servers");
                JsonObject json = GSON.fromJson(response, JsonObject.class);

                if (json.has("api")) {
                    JsonObject api = json.getAsJsonObject("api");
                    if (api.has("data")) {
                        JsonObject data = api.getAsJsonObject("data");

                        // Parse server groups (boxes)
                        Map<Integer, BoxInfo> boxMap = new HashMap<>();
                        if (data.has("servers_groups")) {
                            JsonObject groups = data.getAsJsonObject("servers_groups");
                            for (String key : groups.keySet()) {
                                BoxInfo box = parseBox(groups.getAsJsonObject(key));
                                if (box != null) {
                                    boxMap.put(box.getId(), box);
                                }
                            }
                        }

                        // Parse servers and associate them with boxes
                        if (data.has("servers") && data.get("servers").isJsonArray()) {
                            JsonArray servers = data.getAsJsonArray("servers");
                            for (JsonElement element : servers) {
                                ServerInfo server = parseServer(element.getAsJsonObject());
                                if (server != null && server.isMinecraft()) {
                                    BoxInfo box = boxMap.get(server.getIdMybox());
                                    if (box != null) {
                                        box.addServer(server);
                                    }
                                }
                            }
                        }

                        // Add only boxes that have Minecraft servers
                        for (BoxInfo box : boxMap.values()) {
                            if (!box.getServers().isEmpty()) {
                                boxes.add(box);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                MinestratorHelper.LOGGER.error("Failed to fetch servers", e);
            }

            return boxes;
        });
    }

    /**
     * Sends a power action to the server
     * @param serverId Server ID
     * @param action "start", "stop", "restart" or "kill"
     */
    public static CompletableFuture<Boolean> sendPowerAction(int serverId, String action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/server/" + serverId + "/poweraction";
                String body = "{\"poweraction\":\"" + action + "\"}";
                MinestratorHelper.LOGGER.info("Sending power action: " + action + " to server " + serverId);
                String response = doPut(url, body);
                MinestratorHelper.LOGGER.info("Power action response: " + response);
                return true;
            } catch (Exception e) {
                MinestratorHelper.LOGGER.error("Failed to send power action: " + action + " - " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Restarts a server
     */
    public static CompletableFuture<Boolean> restartServer(int serverId) {
        return sendPowerAction(serverId, "restart");
    }

    /**
     * Stops a server
     */
    public static CompletableFuture<Boolean> stopServer(int serverId) {
        return sendPowerAction(serverId, "stop");
    }

    /**
     * Starts a server
     */
    public static CompletableFuture<Boolean> startServer(int serverId) {
        return sendPowerAction(serverId, "start");
    }

    /**
     * Kills a server (forced stop)
     */
    public static CompletableFuture<Boolean> killServer(int serverId) {
        return sendPowerAction(serverId, "kill");
    }

    /**
     * Sends a command to the server console
     * @param serverId Server ID
     * @param command The command to execute
     */
    public static CompletableFuture<Boolean> sendConsoleCommand(int serverId, String command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/server/" + serverId + "/command";
                String body = "{\"command\":\"" + command.replace("\"", "\\\"") + "\"}";
                MinestratorHelper.LOGGER.info("Sending console command to server " + serverId + ": " + command);
                String response = doPut(url, body);
                MinestratorHelper.LOGGER.info("Console command response: " + response);
                return true;
            } catch (Exception e) {
                MinestratorHelper.LOGGER.error("Failed to send console command: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Fetches live server data (state, players, etc.)
     */
    public static CompletableFuture<ServerLiveData> fetchServerLive(int serverId) {
        return CompletableFuture.supplyAsync(() -> {
            if (!ModConfig.get().isConfigured()) {
                return null;
            }

            try {
                String response = doGet(BASE_URL + "/server/" + serverId + "/live");
                JsonObject json = GSON.fromJson(response, JsonObject.class);

                if (json.has("api")) {
                    JsonObject api = json.getAsJsonObject("api");
                    if (api.has("data")) {
                        JsonObject data = api.getAsJsonObject("data");
                        return GSON.fromJson(data, ServerLiveData.class);
                    }
                }
            } catch (Exception e) {
                MinestratorHelper.LOGGER.debug("Failed to fetch live data for server " + serverId, e);
            }

            return null;
        });
    }

    private static BoxInfo parseBox(JsonObject obj) {
        try {
            BoxInfo box = new BoxInfo();
            box.setId(obj.get("id").getAsInt());
            box.setName(getStringOrNull(obj, "name"));
            box.setHashSupport(getStringOrNull(obj, "hashsupport"));
            box.setOffer(getStringOrNull(obj, "offer"));
            box.setTendDays(obj.has("tend_days") ? obj.get("tend_days").getAsInt() : 0);
            box.setExpired(obj.has("is_expired") && obj.get("is_expired").getAsInt() == 1);
            box.setSuspended(obj.has("is_suspended") && obj.get("is_suspended").getAsInt() == 1);

            if (obj.has("resources")) {
                JsonObject resources = obj.getAsJsonObject("resources");
                box.setCpu(resources.has("cpu") ? resources.get("cpu").getAsInt() : 0);
                box.setRam(resources.has("ram") ? resources.get("ram").getAsInt() : 0);
                box.setDisk(resources.has("disk") ? resources.get("disk").getAsInt() : 0);
            }

            return box;
        } catch (Exception e) {
            MinestratorHelper.LOGGER.error("Failed to parse box", e);
            return null;
        }
    }

    private static ServerInfo parseServer(JsonObject obj) {
        try {
            ServerInfo server = new ServerInfo();
            server.setId(obj.get("id").getAsInt());
            server.setName(getStringOrNull(obj, "name"));
            server.setHashSupport(getStringOrNull(obj, "hashsupport"));
            server.setIdMybox(obj.has("id_mybox") ? obj.get("id_mybox").getAsInt() : 0);
            server.setIp(getStringOrNull(obj, "ip"));
            server.setPort(obj.has("port") ? obj.get("port").getAsInt() : 25565);
            server.setDns(getStringOrNull(obj, "dns"));
            server.setEggName(getStringOrNull(obj, "egg_name"));
            server.setExpired(obj.has("is_expired") && obj.get("is_expired").getAsInt() == 1);
            server.setSuspended(obj.has("is_suspended") && obj.get("is_suspended").getAsInt() == 1);
            server.setDisabled(obj.has("is_disabled") && obj.get("is_disabled").getAsInt() == 1);
            server.setBedrock(obj.has("is_bedrock") && obj.get("is_bedrock").getAsInt() == 1);

            return server;
        } catch (Exception e) {
            MinestratorHelper.LOGGER.error("Failed to parse server", e);
            return null;
        }
    }

    private static String getStringOrNull(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    private static String doGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + ModConfig.get().getBearerToken());
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        return readResponse(conn);
    }

    private static String doPost(String urlString, String body) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + ModConfig.get().getBearerToken());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        return readResponse(conn);
    }

    private static String doPut(String urlString, String body) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "Bearer " + ModConfig.get().getBearerToken());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        BufferedReader reader;

        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException("HTTP Error " + responseCode + ": " + response);
        }

        return response.toString();
    }
}
