package zenovka.stormfront;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = "stormfront", name = "StormFront", version = "0.1.0", acceptableRemoteVersions = "*")
public final class StormFront {
    @EventHandler public void preInit(FMLPreInitializationEvent event) {
        StormConfig.load(event.getSuggestedConfigurationFile());
    }
    @EventHandler public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new TickHandler());
    }
    public static final class TickHandler {
        @SubscribeEvent public void tick(TickEvent.WorldTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.world.isRemote
                    || !(event.world instanceof WorldServer)) return;
            StormManager.tick((WorldServer) event.world);
        }
    }
}
