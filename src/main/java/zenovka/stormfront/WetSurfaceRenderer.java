package zenovka.stormfront;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

public final class WetSurfaceRenderer {
    private World lastWorld;
    private float diagnosticRainStrength;
    private float diagnosticWetness;
    private float diagnosticOpacity;
    private float diagnosticDelay;
    private boolean diagnosticRainFlag;

    public String[] diagnostics() {
        return new String[] {
            String.format(java.util.Locale.ROOT, "StormFront rainFlag=%s strength=%.4f", diagnosticRainFlag, diagnosticRainStrength),
            String.format(java.util.Locale.ROOT, "Wetness=%.4f opacity=%.4f / max=%.4f", diagnosticWetness, diagnosticOpacity, StormConfig.wetDarkness),
            String.format(java.util.Locale.ROOT, "Dry delay remaining=%.2fs", diagnosticDelay)
        };
    }
    public float currentWetness() { return wetness; }
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
            // Match drying: increase actual overlay opacity by 0.025 per second.
            float step = (0.025F / Math.max(0.001F, StormConfig.wetDarkness)) * elapsed;
            if (wetness < rainStrength) wetness = Math.min(rainStrength, wetness + step);
            else wetness = Math.max(rainStrength, wetness - step);
            dryingDelayRemaining = StormConfig.wetDryDelaySeconds;
        } else {
            float delayUsed = Math.min(elapsed, dryingDelayRemaining);
            dryingDelayRemaining -= delayUsed;
            float fadeTime = elapsed - delayUsed;
            if (fadeTime > 0.0F) {
                // Fade at 0.025 actual overlay opacity per second, independent of rain strength.
                float normalizedStep = (0.025F / Math.max(0.001F, StormConfig.wetDarkness)) * fadeTime;
                wetness = Math.max(0.0F, wetness - normalizedStep);
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
        diagnosticRainFlag = world.isRaining();
        diagnosticRainStrength = rainStrength;
        diagnosticWetness = wetnessFactor;
        diagnosticOpacity = StormConfig.wetDarkness * wetnessFactor;
        diagnosticDelay = dryingDelayRemaining;
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
        // Vanilla's alpha test can discard low-opacity overlay fragments.
        GL11.glDisable(GL11.GL_ALPHA_TEST);
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
                    if (!RainPermeability.matches(world, x, y - 1, z, aboveGround, world.getBlockMetadata(x, y - 1, z))
                            && !isSmallSurfaceObject(world, aboveGround, x, y - 1, z, material)) break;
                    y--;
                }
                Block block = world.getBlock(x, y - 1, z);
                if (block == null || !block.isOpaqueCube()) continue;

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
        if (isGT6BerryBush(world, x, y, z)) return true;
        if (material == Material.plants || material == Material.vine || block instanceof BlockBush) return true;
        // Some modded berry bushes use non-plant materials and oversized collision boxes.
        // Keep this narrow: it only affects blocks whose registered ID identifies a berry bush.
        Object registeredName = Block.blockRegistry.getNameForObject(block);
        if (registeredName != null) {
            String name = registeredName.toString().toLowerCase(java.util.Locale.ROOT);
            if (name.contains("berry") && (name.contains("bush") || name.contains("shrub"))) return true;
        }
        if (block.isOpaqueCube() || block.getMaterial().isLiquid()) return false;
        AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (box == null) return true;
        double width = box.maxX - box.minX;
        double height = box.maxY - box.minY;
        double depth = box.maxZ - box.minZ;
        // Only low, narrow clutter. Full-width slabs and larger structures remain barriers.
        return height <= 0.375 && width <= 0.75 && depth <= 0.75;
    }

    // GT6 berry bushes are multi-tile entities sharing a generic block registry ID.
    // Match their actual tile implementation, including subclasses, without linking GT6.
    private static boolean isGT6BerryBush(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) return false;
        for (Class<?> type = tile.getClass(); type != null; type = type.getSuperclass()) {
            if ("gregtech.tileentity.plants.MultiTileEntityBush".equals(type.getName())) return true;
        }
        return false;
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
