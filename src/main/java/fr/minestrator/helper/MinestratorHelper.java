package fr.minestrator.helper;

import fr.minestrator.helper.client.Commands;
import fr.minestrator.helper.client.KeyBinds;
import fr.minestrator.helper.client.ScreenButtons;
import fr.minestrator.helper.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic entry point. Each loader module (Fabric, NeoForge) calls
 * {@link #init()} from its own bootstrap class.
 */
public final class MinestratorHelper {
    public static final String MOD_ID = "minestratorhelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private MinestratorHelper() {
    }

    public static void init() {
        LOGGER.info("Minestrator Helper initializing");
        ModConfig.load();
        Commands.register();
        KeyBinds.register();
        ScreenButtons.register();
    }
}
