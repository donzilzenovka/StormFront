package zenovka.stormfront;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public final class StormWorldData extends WorldSavedData {
    public final List<StormCell> cells = new ArrayList<StormCell>();
    public long ticks;
    public StormWorldData() { super("StormFrontCells"); }
    public StormWorldData(String name) { super(name); }
    public static StormWorldData get(World world) {
        String key = "StormFrontCells";
        StormWorldData data = (StormWorldData) world.perWorldStorage.loadData(StormWorldData.class, key);
        if (data == null) {
            data = new StormWorldData(key);
            world.perWorldStorage.setData(key, data);
        }
        return data;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        cells.clear();
        ticks = tag.getLong("ticks");
        NBTTagList list = tag.getTagList("cells", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            StormCell c = StormCell.read(list.getCompoundTagAt(i));
            if (c.radius > 0 && c.lifetime > c.age && cells.size() < 20) cells.add(c);
        }
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        tag.setLong("ticks", ticks);
        NBTTagList list = new NBTTagList();
        for (StormCell c : cells) list.appendTag(c.write());
        tag.setTag("cells", list);
    }
}
