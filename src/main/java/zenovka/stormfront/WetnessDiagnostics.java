package zenovka.stormfront;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public final class WetnessDiagnostics {
    private final WetSurfaceRenderer renderer;

    public WetnessDiagnostics(WetSurfaceRenderer renderer) {
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event) {
        if (!StormConfig.wetDiagnostics || Minecraft.getMinecraft().theWorld == null) return;
        for (String line : renderer.diagnostics()) event.left.add(line);
    }
}
