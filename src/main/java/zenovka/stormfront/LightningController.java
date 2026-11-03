package zenovka.stormfront;

import java.util.Random;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;

public final class LightningController {
    private LightningController() {}
    public static void tick(WorldServer world, StormCell cell) {
        double expected = StormConfig.strikesPerMinute * cell.strength() / 1200.0;
        if (world.rand.nextDouble() >= expected) return;
        strike(world, cell, world.rand);
    }
    private static void strike(WorldServer world, StormCell cell, Random random) {
        for (int attempt = 0; attempt < StormConfig.attemptsPerStrike; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = cell.radius * Math.sqrt(random.nextDouble()) * random.nextDouble();
            int x = (int) Math.floor(cell.x + Math.cos(angle) * distance);
            int z = (int) Math.floor(cell.z + Math.sin(angle) * distance);
            if (!world.getChunkProvider().chunkExists(x >> 4, z >> 4)) continue;
            BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
            if (biome == null || !biome.canSpawnLightningBolt()) continue;
            int y = world.getPrecipitationHeight(x, z);
            if (y <= 0 || y >= world.getActualHeight()) continue;
            world.addWeatherEffect(new EntityLightningBolt(world, x + 0.5, y, z + 0.5));
            return;
        }
    }
}
