package fr.minestrator.helper.client;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import fr.minestrator.helper.screen.ConsoleCommandScreen;
import fr.minestrator.helper.screen.ServerHud;
import fr.minestrator.helper.screen.ServerStateManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinds (registered through Architectury for both loaders):
 *  - F6 opens the live console for the effective (pinned/current) server;
 *  - F7 toggles the in-game server stats overlay (ServerHud).
 * Both are rebindable in Options → Controls.
 */
public final class KeyBinds {
    private static KeyMapping consoleKey;
    private static KeyMapping hudKey;

    private KeyBinds() {
    }

    public static void register() {
        //? if >=1.21.9 {
        KeyMapping.Category category = KeyMapping.Category.register(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minestratorhelper", "keys"));
        consoleKey = new KeyMapping("minestratorhelper.key.console", GLFW.GLFW_KEY_F6, category);
        hudKey = new KeyMapping("minestratorhelper.key.hud", GLFW.GLFW_KEY_F7, category);
        //?} else {
        /*consoleKey = new KeyMapping("minestratorhelper.key.console", GLFW.GLFW_KEY_F6, "minestratorhelper.key.category");
        hudKey = new KeyMapping("minestratorhelper.key.hud", GLFW.GLFW_KEY_F7, "minestratorhelper.key.category");
        *///?}
        KeyMappingRegistry.register(consoleKey);
        KeyMappingRegistry.register(hudKey);
        ClientTickEvent.CLIENT_POST.register(KeyBinds::onTick);
    }

    private static void onTick(Minecraft client) {
        ServerHud.tick();

        while (hudKey.consumeClick()) {
            ServerHud.toggle();
        }

        while (consoleKey.consumeClick()) {
            if (!ServerStateManager.isOnHostedServer()) {
                if (client.player != null) {
                    ChatFeedback.send(client.player,
                            Component.translatable("minestratorhelper.command.not_on_hosted"));
                }
                return;
            }
            client.setScreen(new ConsoleCommandScreen());
        }
    }
}
