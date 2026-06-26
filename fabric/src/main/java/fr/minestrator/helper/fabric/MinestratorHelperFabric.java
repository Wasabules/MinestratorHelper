package fr.minestrator.helper.fabric;

import fr.minestrator.helper.MinestratorHelper;
import net.fabricmc.api.ClientModInitializer;

public final class MinestratorHelperFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MinestratorHelper.init();
    }
}
