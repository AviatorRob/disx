package xyz.ar06.disx.fabric;

import dev.architectury.registry.fuel.FuelRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
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
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            CompoundTag oldTag = new CompoundTag();
            oldPlayer.addAdditionalSaveData(oldTag);

            CompoundTag enderAdvancedJukeboxTag = oldTag.getCompound("EnderAdvancedJukeboxInventory.disx");

            CompoundTag newTag = new CompoundTag();
            newTag.put("EnderAdvancedJukeboxInventory.disx", enderAdvancedJukeboxTag.copy());

            newPlayer.readAdditionalSaveData(newTag);
        });
        DisxClientMainFabric.onInitializeClient();
    }
}
