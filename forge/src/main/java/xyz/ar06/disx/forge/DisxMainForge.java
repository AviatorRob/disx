package xyz.ar06.disx.forge;

import net.minecraftforge.common.MinecraftForge;
import xyz.ar06.disx.DisxMain;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.ar06.disx.forge.client.DisxClientForge;

@Mod(DisxMain.MOD_ID)
public class DisxMainForge {
    public DisxMainForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(DisxMain.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        DisxMain.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DisxClientForge::onClientInitialize);
        FMLJavaModLoadingContext.get().getModEventBus().register(DisxForgeModBusEventHandler.class);
        MinecraftForge.EVENT_BUS.register(DisxForgeBusEventHandler.class);
    }
}
