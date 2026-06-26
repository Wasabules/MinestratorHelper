package fr.minestrator.helper.client;

import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.screen.ServerStateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client commands (/reboot, /mstop, /mstart), registered through Architectury
 * so the same code drives Fabric and NeoForge.
 */
public final class Commands {
    private Commands() {
    }

    public static void register() {
        ClientCommandRegistrationEvent.EVENT.register((dispatcher, registry) -> {
            dispatcher.register(ClientCommandRegistrationEvent.literal("reboot")
                    .executes(ctx -> powerAction(ctx.getSource(), "restart", "reboot")));
            dispatcher.register(ClientCommandRegistrationEvent.literal("mstop")
                    .executes(ctx -> powerAction(ctx.getSource(), "stop", "stop")));
            dispatcher.register(ClientCommandRegistrationEvent.literal("mstart")
                    .executes(ctx -> powerAction(ctx.getSource(), "start", "start")));
        });
    }

    private static int powerAction(ClientCommandSourceStack source, String action, String key) {
        Integer serverId = ServerStateManager.getCurrentServerId();
        String serverName = ServerStateManager.getCurrentServerName();

        if (serverId == null) {
            source.arch$sendFailure(Component.translatable("minestratorhelper.command.not_on_hosted")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        source.arch$sendSuccess(() -> Component.translatable("minestratorhelper.command." + key + "_sending", serverName)
                .withStyle(ChatFormatting.YELLOW), false);

        ApiClient.sendPowerAction(serverId, action).thenAccept(success ->
                Minecraft.getInstance().execute(() -> {
                    var player = Minecraft.getInstance().player;
                    if (player == null) return;
                    if (success) {
                        player.displayClientMessage(Component.translatable(
                                "minestratorhelper.command." + key + "_success", serverName)
                                .withStyle(ChatFormatting.GREEN), false);
                    } else {
                        player.displayClientMessage(Component.translatable(
                                "minestratorhelper.command." + key + "_failed")
                                .withStyle(ChatFormatting.RED), false);
                    }
                }));
        return 1;
    }
}
