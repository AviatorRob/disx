package xyz.ar06.disx.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DisxForgeBusEventHandler {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event){
        if (!(event.getOriginal() instanceof ServerPlayer)) return;

        CompoundTag oldTag = new CompoundTag();
        event.getOriginal().addAdditionalSaveData(oldTag);

        CompoundTag enderAdvancedJukeboxTag = oldTag.getCompound("EnderAdvancedJukeboxInventory.disx");

        CompoundTag newTag = new CompoundTag();
        newTag.put("EnderAdvancedJukeboxInventory.disx", enderAdvancedJukeboxTag.copy());

        event.getEntity().readAdditionalSaveData(newTag);
    }
}
