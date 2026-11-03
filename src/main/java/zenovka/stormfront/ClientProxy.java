package zenovka.stormfront;

import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy {
    @Override public void registerRenderers() {
        WetSurfaceRenderer renderer = new WetSurfaceRenderer();
        MinecraftForge.EVENT_BUS.register(renderer);
        MinecraftForge.EVENT_BUS.register(new WetnessDiagnostics(renderer));
    }
}
