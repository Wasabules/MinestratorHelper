package fr.minestrator.helper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import fr.minestrator.helper.MinestratorHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;

    // Fixed base URL for the Minestrator API
    public static final String API_BASE_URL = "https://mine.sttr.io";

    private String bearerToken = "";
    private transient Integer cachedUserId = null;

    private static Path getConfigPath() {
        return Platform.getConfigFolder().resolve("minestratorhelper.json");
    }

    public static void load() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                instance = GSON.fromJson(json, ModConfig.class);
                MinestratorHelper.LOGGER.info("Configuration loaded");
            } catch (IOException e) {
                MinestratorHelper.LOGGER.error("Failed to load config", e);
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(instance));
            MinestratorHelper.LOGGER.info("Configuration saved");
        } catch (IOException e) {
            MinestratorHelper.LOGGER.error("Failed to save config", e);
        }
    }

    public static ModConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
        this.cachedUserId = null; // Reset cache when token changes
    }

    public Integer getCachedUserId() {
        return cachedUserId;
    }

    public void setCachedUserId(Integer userId) {
        this.cachedUserId = userId;
    }

    public boolean isConfigured() {
        return bearerToken != null && !bearerToken.isEmpty();
    }
}
