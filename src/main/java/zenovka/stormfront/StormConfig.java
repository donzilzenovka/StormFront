package zenovka.stormfront;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class StormConfig {
    public static int dimension = 0;
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
            dimension = cfg.get("storm", "dimension", 0).getInt(0);
            maxCells = cfg.get("storm", "maxCells", 3).getInt(3);
            spawnInterval = cfg.get("storm", "spawnIntervalTicks", 1200).getInt(1200);
            minRadius = cfg.get("storm", "minRadiusBlocks", 96).getInt(96);
            maxRadius = cfg.get("storm", "maxRadiusBlocks", 224).getInt(224);
            minLifetime = cfg.get("storm", "minLifetimeTicks", 3600).getInt(3600);
            maxLifetime = cfg.get("storm", "maxLifetimeTicks", 9000).getInt(9000);
            strikesPerMinute = cfg.get("lightning", "strikesPerMinuteAtFullIntensityPerCell", 1.5).getDouble(1.5);
            attemptsPerStrike = cfg.get("lightning", "placementAttempts", 8).getInt(8);
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
