package fr.minestrator.helper.client;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import fr.minestrator.helper.screen.ConsoleCommandScreen;
import fr.minestrator.helper.screen.ServerStateManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Console keybind (F6), registered through Architectury for both loaders.
 */
public final class KeyBinds {
    private static KeyMapping consoleKey;

    private KeyBinds() {
    }

    public static void register() {
        //? if >=1.21.9 {
        KeyMapping.Category category = KeyMapping.Category.register(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minestratorhelper", "keys"));
        consoleKey = new KeyMapping("minestratorhelper.key.console", GLFW.GLFW_KEY_F6, category);
        //?} else {
        /*consoleKey = new KeyMapping(
                "minestratorhelper.key.console",
                GLFW.GLFW_KEY_F6,
                "minestratorhelper.key.category");
        *///?}
        KeyMappingRegistry.register(consoleKey);
        ClientTickEvent.CLIENT_POST.register(KeyBinds::onTick);
    }

    private static void onTick(Minecraft client) {
        while (consoleKey.consumeClick()) {
            if (!ServerStateManager.isOnHostedServer()) {
                if (client.player != null) {
                    client.player.displayClientMessage(
                            Component.translatable("minestratorhelper.command.not_on_hosted"), true);
                }
                return;
            }
            client.setScreen(new ConsoleCommandScreen());
        }
    }
}
