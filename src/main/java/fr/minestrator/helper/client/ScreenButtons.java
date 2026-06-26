package fr.minestrator.helper.client;

import dev.architectury.event.events.client.ClientGuiEvent;
import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.screen.HostedServersScreen;
import fr.minestrator.helper.screen.ServerStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

/**
 * Injects mod buttons into vanilla screens via Architectury's screen-init event
 * (replaces the old MultiplayerScreenMixin / GameMenuScreenMixin).
 */
public final class ScreenButtons {
    private ScreenButtons() {
    }

    public static void register() {
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (screen instanceof JoinMultiplayerScreen) {
                access.addRenderableWidget(Button.builder(
                        Component.translatable("minestratorhelper.button.my_servers"),
                        b -> Minecraft.getInstance().setScreen(new HostedServersScreen(screen))
                ).bounds(screen.width - 110, 5, 105, 20).build());
            } else if (screen instanceof PauseScreen && ServerStateManager.isOnHostedServer()) {
                int x = screen.width / 2 + 104;
                int y = screen.height / 4 + 72;

                access.addRenderableWidget(Button.builder(
                        Component.translatable("minestratorhelper.button.restart"),
                        b -> powerAction(b, "restart", "restarting", "restart_success", "restart_failed")
                ).bounds(x, y, 98, 20).build());

                access.addRenderableWidget(Button.builder(
                        Component.translatable("minestratorhelper.button.stop"),
                        b -> powerAction(b, "stop", "stopping", "stop_success", "stop_failed")
                ).bounds(x, y + 24, 98, 20).build());
            }
        });
    }

    private static void powerAction(Button button, String action,
                                    String progressKey, String successKey, String failKey) {
        Integer serverId = ServerStateManager.getCurrentServerId();
        if (serverId == null) return;

        button.active = false;
        button.setMessage(Component.translatable("minestratorhelper.button." + progressKey));

        ApiClient.sendPowerAction(serverId, action).thenAccept(success ->
                Minecraft.getInstance().execute(() -> {
                    if (success) {
                        button.setMessage(Component.translatable("minestratorhelper.button." + successKey));
                    } else {
                        button.setMessage(Component.translatable("minestratorhelper.button." + failKey));
                        button.active = true;
                    }
                }));
    }
}
