package zenovka.stormfront;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;

/** Adds a quiet wet layer when vanilla or modded footstep audio actually plays. */
public final class WetFootstepSounds {
    private final WetSurfaceRenderer renderer;
    private boolean playingLayer;
    private int diagnosticEvents;

    public WetFootstepSounds(WetSurfaceRenderer renderer) {
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void onSound(PlaySoundEvent17 event) {
        if (!StormConfig.wetFootsteps || playingLayer || event.name == null || event.sound == null) return;
        String name = event.name.toLowerCase(java.util.Locale.ROOT);
        if (StormConfig.wetDiagnostics && diagnosticEvents < 30) {
            System.out.println("[StormFront] Heard sound event: " + name);
            diagnosticEvents++;
        }
        if (!(name.startsWith("step.") || name.contains(".step.") || name.startsWith("dig."))) return;

        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityPlayer player = mc.thePlayer;
        if (world == null || player == null) return;

        ISound sound = event.sound;
        double x = sound.getXPosF();
        double y = sound.getYPosF();
        double z = sound.getZPosF();
        // Prototype: local player's footsteps only; do not double up nearby mobs.
        if (player.getDistanceSq(x, y, z) > 9.0D) return;
        int bx = (int)Math.floor(x);
        int by = (int)Math.floor(y - 0.1D);
        int bz = (int)Math.floor(z);
        if (!world.getChunkProvider().chunkExists(bx >> 4, bz >> 4)) return;
        if (!world.getBiomeGenForCoords(bx, bz).canSpawnLightningBolt()) return;
        if (world.getPrecipitationHeight(bx, bz) > by + 2) return;

        float wetness = renderer.currentWetness();
        if (wetness <= 0.01F) return;
        float volume = StormConfig.wetFootstepVolume * wetness;
        if (volume <= 0.001F) return;

        // A distinct event avoids interfering with the original step's playback.
        playingLayer = true;
        try {
            mc.getSoundHandler().playSound(new PositionedSoundRecord(
                new ResourceLocation("stormfront", "wet_step"), volume,
                0.96F + world.rand.nextFloat() * 0.08F, (float)x, (float)y, (float)z));
            if (StormConfig.wetDiagnostics) System.out.println("[StormFront] Layered wet footstep: " + name + " volume=" + volume);
        } finally {
            playingLayer = false;
        }
    }
}
