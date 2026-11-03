package zenovka.stormfront;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class StormConfig {
    public static int dimension = 0;
    public static boolean wetSurfaces = true;
    public static boolean wetDiagnostics = false;
    public static boolean wetFootsteps = true;
    public static boolean wetDigSounds = true;
    public static float wetFootstepVolume = 0.10F;
    public static String[] rainPermeableBlocks = new String[] { "minecraft:cake", "te:gregtech.tileentity.plants.MultiTileEntityBush" };
    public static int wetRadius = 12;
    public static float wetDarkness = 0.20F;
    public static float wetRiseSeconds = 6.0F;
    public static float wetDryDelaySeconds = 10.0F;
    public static float wetDryFadeSeconds = 8.0F;
    public static int maxCells = 3;
    public static int spawnInterval = 1200;
    public static int minRadius = 96;
    public static int maxRadius = 224;
    public static int minLifetime = 3600;
    public static int maxLifetime = 9000;
    public static double strikesPerMinute = 1.5;
    public static int attemptsPerStrike = 8;
    private StormConfig() {}
    public static void load(File file) {
        Configuration cfg = new Configuration(file);
        try {
            cfg.load();
            wetSurfaces = cfg.get("wetness", "enabled", true, "Client-only prototype: darken rain-exposed full-block top faces.").getBoolean(true);
            wetDiagnostics = cfg.get("wetness", "diagnostics", false, "Show rain strength, wetness, opacity and drying delay in the HUD.").getBoolean(false);
            wetFootsteps = cfg.get("wetness", "footstepSounds", true, "Layer damp steps over existing footsteps near the player.").getBoolean(true);
            wetDigSounds = cfg.get("wetness", "wetDigSounds", true, "Also layer damp sounds over dig.* events.").getBoolean(true);
            wetFootstepVolume = (float)cfg.get("wetness", "footstepVolume", 0.10, "Maximum wet footstep layer volume.").getDouble(0.15);
            rainPermeableBlocks = cfg.get("wetness", "rainPermeableBlocks", new String[] { "minecraft:cake" },
                "Rain-permeable blocks: modid:block (all metadata), modid:block@3 (one metadata), or te:fully.qualified.TileEntityClass (exact tile-entity class; e.g. GT6 berry bushes). Existing config files need the te: entry added manually.").getStringList();
            RainPermeability.configure(rainPermeableBlocks);
            wetRadius = cfg.get("wetness", "radiusBlocks", 12).getInt(12);
            wetDarkness = (float)cfg.get("wetness", "darkness", 0.20, "Overlay opacity: 0 = invisible, 1 = black.").getDouble(0.20);
            dimension = cfg.get("storm", "dimension", 0).getInt(0);
            maxCells = cfg.get("storm", "maxCells", 3).getInt(3);
            spawnInterval = cfg.get("storm", "spawnIntervalTicks", 1200).getInt(1200);
            minRadius = cfg.get("storm", "minRadiusBlocks", 96).getInt(96);
            maxRadius = cfg.get("storm", "maxRadiusBlocks", 224).getInt(224);
            minLifetime = cfg.get("storm", "minLifetimeTicks", 3600).getInt(3600);
            maxLifetime = cfg.get("storm", "maxLifetimeTicks", 9000).getInt(9000);
            strikesPerMinute = cfg.get("lightning", "strikesPerMinuteAtFullIntensityPerCell", 1.5).getDouble(1.5);
            attemptsPerStrike = cfg.get("lightning", "placementAttempts", 8).getInt(8);
            wetRiseSeconds = (float)cfg.get("wetness", "wetRiseSeconds", 6.0, "Seconds for wetness to rise linearly from zero to maximum.").getDouble(6.0);
            wetDryDelaySeconds = (float)cfg.get("wetness", "dryDelaySeconds", 10.0, "Seconds to remain fully wet after rain ends.").getDouble(10.0);
            wetDryFadeSeconds = (float)cfg.get("wetness", "dryFadeSeconds", 8.0, "Seconds to fade linearly to dry.").getDouble(8.0);
            wetRiseSeconds = Math.max(0.1F, Math.min(300.0F, wetRiseSeconds));
            wetDryDelaySeconds = Math.max(0.0F, Math.min(300.0F, wetDryDelaySeconds));
            wetDryFadeSeconds = Math.max(0.1F, Math.min(600.0F, wetDryFadeSeconds));
            wetFootstepVolume = Math.max(0.0F, Math.min(1.0F, wetFootstepVolume));
            wetRadius = Math.max(2, Math.min(24, wetRadius));
            wetDarkness = Math.max(0.0F, Math.min(0.95F, wetDarkness));
            maxCells = Math.max(0, Math.min(20, maxCells));
            spawnInterval = Math.max(100, spawnInterval);
            minRadius = Math.max(16, minRadius);
            maxRadius = Math.max(minRadius, maxRadius);
            minLifetime = Math.max(200, minLifetime);
            maxLifetime = Math.max(minLifetime, maxLifetime);
            strikesPerMinute = Math.max(0, Math.min(60, strikesPerMinute));
            attemptsPerStrike = Math.max(1, Math.min(64, attemptsPerStrike));
        } finally { if (cfg.hasChanged()) cfg.save(); }
    }
}
