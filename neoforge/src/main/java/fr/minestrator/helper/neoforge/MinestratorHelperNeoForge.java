package fr.minestrator.helper.neoforge;

import fr.minestrator.helper.MinestratorHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = MinestratorHelper.MOD_ID, dist = Dist.CLIENT)
public final class MinestratorHelperNeoForge {
    public MinestratorHelperNeoForge() {
        MinestratorHelper.init();
    }
}
