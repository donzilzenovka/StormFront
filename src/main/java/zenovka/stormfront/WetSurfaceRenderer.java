package zenovka.stormfront;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

public final class WetSurfaceRenderer {
    private World lastWorld;
    private long lastFrameNanos;
    private float wetness;
    private float dryingDelayRemaining;

    private float updateWetness(World world, float rainStrength) {
        if (world != lastWorld) {
            lastWorld = world;
            wetness = 0.0F;
            dryingDelayRemaining = 0.0F;
            lastFrameNanos = 0L;
        }
        long now = System.nanoTime();
        float elapsed = lastFrameNanos == 0L ? 0.0F : Math.min(0.25F, (now - lastFrameNanos) / 1000000000.0F);
        lastFrameNanos = now;
        if (rainStrength > 0.0F) {
            // Approach the actual rain-strength target without overshooting.
            float step = elapsed / StormConfig.wetRiseSeconds;
            if (wetness < rainStrength) wetness = Math.min(rainStrength, wetness + step);
            else wetness = Math.max(rainStrength, wetness - step);
            dryingDelayRemaining = StormConfig.wetDryDelaySeconds;
        } else {
            float delayUsed = Math.min(elapsed, dryingDelayRemaining);
            dryingDelayRemaining -= delayUsed;
            float fadeTime = elapsed - delayUsed;
            if (fadeTime > 0.0F) {
                wetness = Math.max(0.0F, wetness - fadeTime / StormConfig.wetDryFadeSeconds);
            }
        }
        return wetness;
    }

    // This is a deliberately simple, bounded visual prototype, not a chunk recolour.
    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if (!StormConfig.wetSurfaces) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.renderViewEntity == null) return;
        World world = mc.theWorld;
        float rainStrength = world.isRaining() ? Math.max(0.0F, Math.min(1.0F, world.getRainStrength(event.partialTicks))) : 0.0F;
        float wetnessFactor = updateWetness(world, rainStrength);
        if (wetnessFactor <= 0.0F) return;

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
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if (!world.getChunkProvider().chunkExists(x >> 4, z >> 4)) continue;
                BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
                if (biome == null || !biome.canSpawnLightningBolt()) continue;
                int y = world.getPrecipitationHeight(x, z);
                if (y <= 0 || y >= world.getActualHeight()) continue;

                // Tall grass, flowers and vines can sit above the solid ground.
                // Skip a few plant blocks without treating solid roofs as transparent.
                for (int i = 0; i < 4 && y > 1; i++) {
                    Block aboveGround = world.getBlock(x, y - 1, z);
                    Material material = aboveGround.getMaterial();
                    if (!isSmallSurfaceObject(world, aboveGround, x, y - 1, z, material)) break;
                    y--;
                }
                Block block = world.getBlock(x, y - 1, z);
                if (block == null || !block.isOpaqueCube() || block.getRenderType() != 0) continue;

                // Linear fade at the perimeter prevents a hard square following the camera.
                // Inside the perimeter, every rain shower produces the same full wetness.
                double surface = y + 0.003;
                vertex(t, x, surface, z, cx, cz, radius, wetnessFactor);
                vertex(t, x, surface, z + 1, cx, cz, radius, wetnessFactor);
                vertex(t, x + 1, surface, z + 1, cx, cz, radius, wetnessFactor);
                vertex(t, x + 1, surface, z, cx, cz, radius, wetnessFactor);
            }
        }
        t.draw();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    // Treat small surface clutter as rain-permeable, not every non-full block.
    // Collision boxes are world-space and can be absent for decorative objects.
    private static boolean isSmallSurfaceObject(World world, Block block, int x, int y, int z, Material material) {
        if (material == Material.plants || material == Material.vine) return true;
        if (block.isOpaqueCube() || block.getMaterial().isLiquid()) return false;
        AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (box == null) return true;
        double width = box.maxX - box.minX;
        double height = box.maxY - box.minY;
        double depth = box.maxZ - box.minZ;
        // Only low, narrow clutter. Full-width slabs and larger structures remain barriers.
        return height <= 0.375 && width <= 0.75 && depth <= 0.75;
    }

    private static void vertex(Tessellator t, double x, double y, double z,
                               int cx, int cz, int radius, float wetnessFactor) {
        double distance = Math.max(Math.abs(x - (cx + 0.5)), Math.abs(z - (cz + 0.5)));
        double fadeWidth = Math.max(2.0, radius * 0.35);
        float fade = (float)Math.max(0.0, Math.min(1.0, (radius + 0.5 - distance) / fadeWidth));
        t.setColorRGBA_F(0.0F, 0.0F, 0.0F, StormConfig.wetDarkness * wetnessFactor * fade);
        t.addVertex(x, y, z);
    }
}
