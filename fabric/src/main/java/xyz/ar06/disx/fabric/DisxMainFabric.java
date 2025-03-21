package xyz.ar06.disx.fabric;

import dev.architectury.registry.fuel.FuelRegistry;
import xyz.ar06.disx.DisxMain;
import net.fabricmc.api.ModInitializer;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.items.DisxLacquerDrop;

public class DisxMainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DisxMain.init();
        FuelRegistry.register(100, DisxLacquerDrop.itemRegistration.get());
        FuelRegistry.register(900, DisxLacquerBlock.itemRegistration.get());
        DisxClientMainFabric.onInitializeClient();
    }
}
