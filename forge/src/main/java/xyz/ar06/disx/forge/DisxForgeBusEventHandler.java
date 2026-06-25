package xyz.ar06.disx.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.ar06.disx.mixin.DisxEnderAdvancedJukeboxMixin;
import xyz.ar06.disx.utils.DisxEnderAdvancedJukeboxInventoryHelper;

public class DisxForgeBusEventHandler {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event){
        if (!(event.getOriginal() instanceof ServerPlayer)) return;

        if (event.getOriginal() instanceof DisxEnderAdvancedJukeboxInventoryHelper helperOG){
            CompoundTag enderAdvancedJukeboxTag = helperOG.disx$getEnderAdvancedJukeboxInventory();
            if (event.getEntity() instanceof DisxEnderAdvancedJukeboxInventoryHelper helper){
                helper.disx$setEnderAdvancedJukeboxInventory(enderAdvancedJukeboxTag);
            }
        }
    }
}
