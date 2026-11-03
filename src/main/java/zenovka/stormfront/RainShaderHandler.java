package zenovka.stormfront;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/** Optional vanilla post-processing when the camera is exposed to rain. */
public final class RainShaderHandler {
    private static final ResourceLocation RAIN_SHADER =
        new ResourceLocation("minecraft", "shaders/post/antialias.json");
    private ShaderGroup ownedShader;
    private World shaderWorld;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!StormConfig.enableRainShader || mc.theWorld == null || mc.renderViewEntity == null) {
            release(mc);
            return;
        }

        World world = mc.theWorld;
        Entity camera = mc.renderViewEntity;
        if (shaderWorld != null && shaderWorld != world) {
            release(mc);
        }

        int x = (int)Math.floor(camera.posX);
        int y = (int)Math.floor(camera.posY + camera.getEyeHeight());
        int z = (int)Math.floor(camera.posZ);
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        // Any equipped head-slot item protects the player's view from rain blur.
        // Vanilla 1.7.10 armorInventory[3] is the helmet slot.
        boolean wearingHelmet = mc.thePlayer != null
            && mc.thePlayer.getCurrentArmor(3) != null;
        boolean exposed = !wearingHelmet && world.isRaining()
            && world.getRainStrength(1.0F) > 0.0F
            && biome != null && biome.canSpawnLightningBolt()
            && !camera.isInsideOfMaterial(net.minecraft.block.material.Material.water)
            && !world.getBlock(x, y, z).getMaterial().isLiquid()
            && isRainExposed(world, x, y, z);

        if (!exposed) {
            release(mc);
            return;
        }

        // Do not replace a shader belonging to HeavySwing or another mod.
        if (ownedShader != null && mc.entityRenderer.theShaderGroup != ownedShader) {
            release(mc);
            return;
        }
        if (ownedShader == null && mc.entityRenderer.theShaderGroup != null) return;

        if (ownedShader == null) {
            try {
                ShaderGroup shader = new ShaderGroup(mc.getTextureManager(),
                    mc.getResourceManager(), mc.getFramebuffer(), RAIN_SHADER);
                shader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                ownedShader = shader;
                shaderWorld = world;
                mc.entityRenderer.theShaderGroup = shader;
            } catch (Exception e) {
                System.err.println("[StormFront] Could not load rain shader: " + e);
            }
        }
    }

    private static boolean isRainExposed(World world, int x, int y, int z) {
        if (!world.getChunkProvider().chunkExists(x >> 4, z >> 4)) return false;
        int precipitationY = world.getPrecipitationHeight(x, z);
        if (precipitationY <= y + 1) return true;
        // Allow configured rain-permeable vegetation above the camera.
        for (int by = y + 1; by < precipitationY; by++) {
            Block block = world.getBlock(x, by, z);
            if (!RainPermeability.matches(world, x, by, z, block,
                    world.getBlockMetadata(x, by, z))) return false;
        }
        return true;
    }

    private void release(Minecraft mc) {
        if (ownedShader == null) return;
        if (mc.entityRenderer.theShaderGroup == ownedShader)
            mc.entityRenderer.theShaderGroup = null;
        ownedShader.deleteShaderGroup();
        ownedShader = null;
        shaderWorld = null;
    }
}
