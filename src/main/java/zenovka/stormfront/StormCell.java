package zenovka.stormfront;

import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;

public final class StormCell {
    public double x, z, dx, dz, intensity;
    public int radius, age, lifetime;
    public StormCell() {}
    public static StormCell create(double x, double z, Random random) {
        StormCell c = new StormCell();
        c.x = x; c.z = z;
        c.radius = StormConfig.minRadius + random.nextInt(StormConfig.maxRadius - StormConfig.minRadius + 1);
        c.lifetime = StormConfig.minLifetime + random.nextInt(StormConfig.maxLifetime - StormConfig.minLifetime + 1);
        double angle = random.nextDouble() * Math.PI * 2;
        double speed = 0.03 + random.nextDouble() * 0.10;
        c.dx = Math.cos(angle) * speed;
        c.dz = Math.sin(angle) * speed;
        c.intensity = 0.5 + random.nextDouble() * 0.5;
        return c;
    }
    public boolean advance() {
        x += dx; z += dz; age++;
        return age < lifetime;
    }
    public double strength() {
        double ramp = Math.min(1.0, age / 600.0);
        double decay = Math.min(1.0, (lifetime - age) / 600.0);
        return intensity * Math.max(0.0, Math.min(ramp, decay));
    }
    public NBTTagCompound write() {
        NBTTagCompound n = new NBTTagCompound();
        n.setDouble("x", x); n.setDouble("z", z);
        n.setDouble("dx", dx); n.setDouble("dz", dz);
        n.setDouble("intensity", intensity);
        n.setInteger("radius", radius);
        n.setInteger("age", age); n.setInteger("lifetime", lifetime);
        return n;
    }
    public static StormCell read(NBTTagCompound n) {
        StormCell c = new StormCell();
        c.x = n.getDouble("x"); c.z = n.getDouble("z");
        c.dx = n.getDouble("dx"); c.dz = n.getDouble("dz");
        c.intensity = n.getDouble("intensity");
        c.radius = n.getInteger("radius");
        c.age = n.getInteger("age"); c.lifetime = n.getInteger("lifetime");
        return c;
    }
}
