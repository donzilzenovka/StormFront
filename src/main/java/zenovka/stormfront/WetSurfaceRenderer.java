package zenovka.stormfront;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

public final class WetSurfaceRenderer {
    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if (!StormConfig.wetSurfaces) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.renderViewEntity == null) return;
        World world = mc.theWorld;
        if (!world.isRaining() || world.getRainStrength(event.partialTicks) <= 0.01F) return;

        int radius = StormConfig.wetRadius;
        int cx = (int)Math.floor(mc.renderViewEntity.posX);
        int cz = (int)Math.floor(mc.renderViewEntity.posZ);
        double rx = RenderManager.renderPosX;
        double ry = RenderManager.renderPosY;
        double rz = RenderManager.renderPosZ;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_POLYGON_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0F, -2.0F);
        GL11.glPushMatrix();
        GL11.glTranslated(-rx, -ry, -rz);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(0.0F, 0.0F, 0.0F, StormConfig.wetDarkness);
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if (!world.getChunkProvider().chunkExists(x >> 4, z >> 4)) continue;
                BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
                if (biome == null || !biome.canSpawnLightningBolt()) continue;
                int y = world.getPrecipitationHeight(x, z);
                if (y <= 0 || y >= world.getActualHeight()) continue;
                Block block = world.getBlock(x, y - 1, z);
                if (block == null || !block.isOpaqueCube() || block.getRenderType() != 0) continue;
                double surface = y + 0.003;
                t.addVertex(x, surface, z);
                t.addVertex(x, surface, z + 1);
                t.addVertex(x + 1, surface, z + 1);
                t.addVertex(x + 1, surface, z);
            }
        }
        t.draw();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
