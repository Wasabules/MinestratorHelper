package fr.minestrator.helper.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import fr.minestrator.helper.api.ApiClient;
import fr.minestrator.helper.screen.ServerStateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Client commands (/reboot, /mstop, /mstart), registered through Architectury
 * so the same code drives Fabric and NeoForge.
 */
public final class Commands {
    private Commands() {
    }

    /** Common server commands suggested as the first word of /sudo. */
    private static final List<String> POPULAR_COMMANDS = List.of(
            "op", "deop", "kick", "ban", "ban-ip", "pardon", "gamemode", "give", "tp", "teleport",
            "time", "weather", "difficulty", "whitelist", "say", "kill", "effect", "enchant",
            "xp", "gamerule", "clear", "seed", "list", "msg", "tell", "stop", "save-all");
    /** Commands whose next argument is a player name (suggested from the local player list). */
    private static final Set<String> PLAYER_COMMANDS = Set.of(
            "op", "deop", "kick", "ban", "pardon", "tp", "teleport", "kill", "msg", "tell", "give");
    private static final List<String> GAMEMODE_MODES = List.of("survival", "creative", "adventure", "spectator");

    public static void register() {
        ClientCommandRegistrationEvent.EVENT.register((dispatcher, registry) -> {
            dispatcher.register(ClientCommandRegistrationEvent.literal("reboot")
                    .executes(ctx -> powerAction(ctx.getSource(), "restart", "reboot")));
            dispatcher.register(ClientCommandRegistrationEvent.literal("mstop")
                    .executes(ctx -> powerAction(ctx.getSource(), "stop", "stop")));
            dispatcher.register(ClientCommandRegistrationEvent.literal("mstart")
                    .executes(ctx -> powerAction(ctx.getSource(), "start", "start")));
            dispatcher.register(ClientCommandRegistrationEvent.literal("sudo")
                    .then(ClientCommandRegistrationEvent.argument("command", StringArgumentType.greedyString())
                            .suggests(Commands::suggestSudo)
                            .executes(ctx -> sudo(ctx.getSource(), StringArgumentType.getString(ctx, "command")))));
        });
    }

    /** /sudo <command> — sends a raw command to the hosted server console via the API. */
    private static int sudo(ClientCommandSourceStack source, String command) {
        Integer serverId = ServerStateManager.getCurrentServerId();
        if (serverId == null) {
            source.arch$sendFailure(Component.translatable("minestratorhelper.command.not_on_hosted")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        source.arch$sendSuccess(() -> Component.literal("» " + command).withStyle(ChatFormatting.GRAY), false);

        ApiClient.sendConsoleCommand(serverId, command).thenAccept(success ->
                Minecraft.getInstance().execute(() -> {
                    var player = Minecraft.getInstance().player;
                    if (player == null) return;
                    if (success) {
                        ChatFeedback.send(player, Component.translatable(
                                "minestratorhelper.command.sudo_sent", command)
                                .withStyle(ChatFormatting.GREEN));
                    } else {
                        ChatFeedback.send(player, Component.translatable(
                                "minestratorhelper.command.sudo_failed")
                                .withStyle(ChatFormatting.RED));
                    }
                }));
        return 1;
    }

    /** Suggests popular commands for the first word, then online players (or gamemodes) for later args. */
    private static CompletableFuture<Suggestions> suggestSudo(CommandContext<ClientCommandSourceStack> ctx,
                                                             SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        int firstSpace = remaining.indexOf(' ');
        if (firstSpace < 0) {
            String prefix = remaining.toLowerCase();
            for (String cmd : POPULAR_COMMANDS) {
                if (cmd.startsWith(prefix)) builder.suggest(cmd);
            }
            return builder.buildFuture();
        }

        String first = remaining.substring(0, firstSpace).toLowerCase();
        int lastSpace = remaining.lastIndexOf(' ');
        String token = remaining.substring(lastSpace + 1).toLowerCase();
        SuggestionsBuilder offset = builder.createOffset(builder.getStart() + lastSpace + 1);
        if (PLAYER_COMMANDS.contains(first)) {
            for (String name : onlinePlayers()) {
                if (name.toLowerCase().startsWith(token)) offset.suggest(name);
            }
        } else if (first.equals("gamemode")) {
            for (String mode : GAMEMODE_MODES) {
                if (mode.startsWith(token)) offset.suggest(mode);
            }
        }
        return offset.buildFuture();
    }

    /** Online player names from the local client player list (no API call). */
    private static List<String> onlinePlayers() {
        List<String> names = new ArrayList<>();
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            for (var info : conn.getOnlinePlayers()) {
                //? if >=1.21.11 {
                String name = info.getProfile().name();
                //?} else
                //String name = info.getProfile().getName();
                if (name != null) names.add(name);
            }
        }
        return names;
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
                        ChatFeedback.send(player, Component.translatable(
                                "minestratorhelper.command." + key + "_success", serverName)
                                .withStyle(ChatFormatting.GREEN));
                    } else {
                        ChatFeedback.send(player, Component.translatable(
                                "minestratorhelper.command." + key + "_failed")
                                .withStyle(ChatFormatting.RED));
                    }
                }));
        return 1;
    }
}
