package xyz.ar06.disx.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.ar06.disx.DisxModInfo;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;
import xyz.ar06.disx.blocks.DisxEnderAdvancedJukebox;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.entities.vehicle.DisxAdvancedJukeboxMinecart;

public class DisxBehaviorHandlingForge {
    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event){
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null){
            if (hitResult.getType().equals(HitResult.Type.BLOCK)){
                Level level = Minecraft.getInstance().level;
                if (level != null){
                    BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                    if (level.getBlockState(blockPos).getBlock().equals(DisxAdvancedJukebox.blockRegistration.get())){
                        event.setCanceled(true);
                    }
                    if (level.getBlockState(blockPos).getBlock().equals(DisxEnderAdvancedJukebox.blockRegistration.get())){
                        event.setCanceled(true);
                    }
                }
            }
            if (hitResult.getType().equals(HitResult.Type.ENTITY)){
                Level level = Minecraft.getInstance().level;
                if (level != null){
                    EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                    Entity entity = entityHitResult.getEntity();
                    if (entity.getType().equals(DisxAdvancedJukeboxMinecart.entityTypeRegistration.get())){
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    static String[] debugKeys = DisxModInfo.getDebugKeys();
    @SubscribeEvent
    public void onChatSend(ClientChatEvent event){
        String s = event.getMessage();
        if (s.equalsIgnoreCase(debugKeys[0])){
            boolean currentValue = Boolean.parseBoolean(DisxConfigHandler.CLIENT.getProperty("debug_mode"));
            if (!currentValue){
                DisxConfigHandler.CLIENT.updateProperty("debug_mode", "true");
                DisxSystemMessages.debugStatus(true);
                event.setCanceled(true);
            }
        }
        if (s.equalsIgnoreCase(debugKeys[1])){
            boolean currentValue = Boolean.parseBoolean(DisxConfigHandler.CLIENT.getProperty("debug_mode"));
            if (currentValue){
                DisxConfigHandler.CLIENT.updateProperty("debug_mode", "false");
                DisxSystemMessages.debugStatus(false);
                event.setCanceled(true);
            }
        }
    }
}
