package zenovka.stormfront;

import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;

public final class StormManager {
    private StormManager() {}
    public static void tick(WorldServer world) {
        if (world.provider.dimensionId != StormConfig.dimension) return;
        StormWorldData data = StormWorldData.get(world);
        data.ticks++;
        if (!world.isThundering()) {
            if (!data.cells.isEmpty()) {
                data.cells.clear();
                data.markDirty();
            }
            return;
        }
        for (Iterator<StormCell> it = data.cells.iterator(); it.hasNext();) {
            StormCell cell = it.next();
            if (!cell.advance()) it.remove();
            else LightningController.tick(world, cell);
        }
        if (data.ticks % StormConfig.spawnInterval == 0
                && data.cells.size() < StormConfig.maxCells
                && !world.playerEntities.isEmpty()) {
            EntityPlayer player = (EntityPlayer) world.playerEntities.get(
                    world.rand.nextInt(world.playerEntities.size()));
            // Spawn in the broader active region, not on top of the player.
            double angle = world.rand.nextDouble() * Math.PI * 2;
            double distance = 128 + world.rand.nextDouble() * 256;
            data.cells.add(StormCell.create(player.posX + Math.cos(angle) * distance,
                    player.posZ + Math.sin(angle) * distance, world.rand));
        }
        if (data.ticks % 200 == 0) data.markDirty();
    }
}
