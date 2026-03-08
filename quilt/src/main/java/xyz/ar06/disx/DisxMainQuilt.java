package xyz.ar06.disx;


import dev.architectury.registry.fuel.FuelRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.items.DisxLacquerDrop;

public class DisxMainQuilt implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
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
        DisxClientMainQuilt.initializeClient();
    }
}
