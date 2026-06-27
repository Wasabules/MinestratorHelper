package fr.minestrator.helper.screen;

import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.api.UserInfo;
import fr.minestrator.helper.config.ModConfig;

import java.util.function.Consumer;

/**
 * Contains all business logic for the config screen.
 * Shared across all Minecraft versions.
 */
public class ConfigLogic {
    private String statusMessage = null;
    private String statusArg = null;
    private boolean statusSuccess = false;
    private boolean testing = false;

    public String getApiBaseUrl() {
        return ModConfig.API_BASE_URL;
    }

    public String getCurrentToken() {
        return ModConfig.get().getBearerToken();
    }

    public void saveToken(String token) {
        ModConfig.get().setBearerToken(token);
        ModConfig.save();
    }

    public void testConnection(String token, Consumer<UserInfo> onSuccess, Consumer<String> onFailure, Runnable onComplete) {
        statusArg = null;
        if (token == null || token.isEmpty()) {
            statusMessage = "minestratorhelper.config.token_required";
            statusSuccess = false;
            onFailure.accept(statusMessage);
            return;
        }

        // Temporarily save the token for testing
        String oldToken = ModConfig.get().getBearerToken();
        ModConfig.get().setBearerToken(token);

        testing = true;
        statusMessage = "minestratorhelper.config.testing";

        ApiClient.fetchUser().thenAccept(user -> {
            testing = false;
            if (user != null) {
                statusMessage = "minestratorhelper.config.test_success";
                statusArg = user.getPseudo();
                statusSuccess = true;
                onSuccess.accept(user);
            } else {
                statusMessage = "minestratorhelper.config.test_failed";
                statusSuccess = false;
                // Restore the old token if the test fails
                ModConfig.get().setBearerToken(oldToken);
                onFailure.accept(statusMessage);
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getStatusArg() {
        return statusArg;
    }

    public boolean isStatusSuccess() {
        return statusSuccess;
    }

    public boolean isTesting() {
        return testing;
    }
}
