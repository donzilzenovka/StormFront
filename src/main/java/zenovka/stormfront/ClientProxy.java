package zenovka.stormfront;

import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy {
    @Override public void registerRenderers() {
        MinecraftForge.EVENT_BUS.register(new WetSurfaceRenderer());
    }
}
