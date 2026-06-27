package fr.minestrator.helper.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Sends a client-side chat message. 26.1 removed
 * {@code Player.displayClientMessage(Component, boolean)}; the equivalent for a
 * non-actionbar message is {@code sendSystemMessage(Component)}.
 */
public final class ChatFeedback {
    private ChatFeedback() {
    }

    public static void send(Player player, Component message) {
        //? if >=26.1 {
        /*player.sendSystemMessage(message);
        *///?} else {
        player.displayClientMessage(message, false);
        //?}
    }
}
