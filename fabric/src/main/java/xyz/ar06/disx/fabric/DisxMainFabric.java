package xyz.ar06.disx.fabric;

import dev.architectury.registry.fuel.FuelRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import xyz.ar06.disx.DisxMain;
import net.fabricmc.api.ModInitializer;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.items.DisxLacquerDrop;
import xyz.ar06.disx.utils.DisxEnderAdvancedJukeboxInventoryHelper;

public class DisxMainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DisxMain.init();
        FuelRegistry.register(100, DisxLacquerDrop.itemRegistration.get());
        FuelRegistry.register(900, DisxLacquerBlock.itemRegistration.get());
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {


            if (oldPlayer instanceof DisxEnderAdvancedJukeboxInventoryHelper helper){
                CompoundTag enderAdvancedJukeboxTag = helper.disx$getEnderAdvancedJukeboxInventory();
                if (newPlayer instanceof DisxEnderAdvancedJukeboxInventoryHelper helperNew){
                    helperNew.disx$setEnderAdvancedJukeboxInventory(enderAdvancedJukeboxTag);
                }
            }
        });
        DisxClientMainFabric.onInitializeClient();
    }
}
