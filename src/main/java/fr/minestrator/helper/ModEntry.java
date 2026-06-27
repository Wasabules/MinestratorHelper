package fr.minestrator.helper;

// Unified client entry point for both loaders. Stonecutter selects the right
// implementation per node via the `loader` constant (//? if fabric / neoforge).

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
//?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
*///?}

//? if fabric {
public final class ModEntry implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MinestratorHelper.init();
    }
}
//?} else {
/*@Mod(value = MinestratorHelper.MOD_ID, dist = Dist.CLIENT)
public final class ModEntry {
    public ModEntry() {
        MinestratorHelper.init();
    }
}
*///?}
