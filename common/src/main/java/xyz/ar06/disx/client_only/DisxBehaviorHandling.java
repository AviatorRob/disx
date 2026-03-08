package xyz.ar06.disx.client_only;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;
import xyz.ar06.disx.blocks.DisxEnderAdvancedJukebox;
import xyz.ar06.disx.blocks.DisxStampMaker;
import xyz.ar06.disx.entities.vehicle.DisxAdvancedJukeboxMinecart;

import java.util.UUID;

public class DisxBehaviorHandling {
    //for advanced jukebox volume scrolling
    public static EventResult scrollListener(Minecraft client, double amount){
        HitResult hitResult = client.hitResult;
        if (hitResult != null){
            if (hitResult.getType().equals(HitResult.Type.BLOCK)){
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (client.level != null){
                    if (client.level.getBlockState(blockPos).getBlock().equals(DisxAdvancedJukebox.blockRegistration.get())){
                        DisxLogger.debug("Player scrolled on advanced jukebox");
                        int currentVolume = DisxAudioInstanceRegistry.getPreferredVolume(blockPos, client.level.dimension().location());
                        DisxLogger.debug("Got current volume: " + currentVolume);
                        if (currentVolume == -1){
                            return EventResult.pass();
                        }
                        int modifiedVolume = currentVolume + ((int) (amount * 10));
                        if (modifiedVolume < 0){
                            modifiedVolume = 0;
                        }
                        if (modifiedVolume > 200){
                            modifiedVolume = 200;
                        }
                        DisxLogger.debug("sending volume set message");
                        DisxSystemMessages.volumeSetMessage(modifiedVolume);
                        if (currentVolume != modifiedVolume){
                            DisxLogger.debug("sending scrolled packet");
                            DisxClientPacketIndex.ClientPackets.scrolledCheckHit(blockPos, amount, new UUID(0L, 0L));
                        }
                        return EventResult.interrupt(true);
                    }
                    if (client.level.getBlockState(blockPos).getBlock().equals(DisxEnderAdvancedJukebox.blockRegistration.get())){
                        DisxLogger.debug("Player scrolled on ender advanced jukebox");
                        UUID playerUuid = client.player.getUUID();
                        int currentVolume = DisxAudioInstanceRegistry.getPreferredVolume(playerUuid);
                        DisxLogger.debug("Got current volume: " + currentVolume);
                        if (currentVolume == -1){
                            return EventResult.pass();
                        }
                        int modifiedVolume = currentVolume + ((int) (amount * 10));
                        if (modifiedVolume < 0){
                            modifiedVolume = 0;
                        }
                        if (modifiedVolume > 200){
                            modifiedVolume = 200;
                        }
                        DisxLogger.debug("sending volume set message");
                        DisxSystemMessages.volumeSetMessage(modifiedVolume);
                        if (currentVolume != modifiedVolume){
                            DisxLogger.debug("sending scrolled packet");
                            DisxClientPacketIndex.ClientPackets.scrolledCheckHit(blockPos, amount, playerUuid);
                        }
                        return EventResult.interrupt(true);
                    }
                }
            }
            if (hitResult.getType().equals(HitResult.Type.ENTITY)){
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();
                if (entity.getType().equals(DisxAdvancedJukeboxMinecart.entityTypeRegistration.get())){
                    DisxLogger.debug("Player scrolled on advanced jukebox minecart");
                    int currentVolume = DisxAudioInstanceRegistry.getPreferredVolume(entity.getUUID());
                    DisxLogger.debug("Got current volume: " + currentVolume);
                    if (currentVolume == -1){
                        return EventResult.pass();
                    }
                    int modifiedVolume = currentVolume + ((int) (amount * 10));
                    if (modifiedVolume < 0){
                        modifiedVolume = 0;
                    }
                    if (modifiedVolume > 200){
                        modifiedVolume = 200;
                    }
                    DisxLogger.debug("sending volume set message");
                    DisxSystemMessages.volumeSetMessage(modifiedVolume);
                    if (currentVolume != modifiedVolume){
                        DisxLogger.debug("sending scrolled packet");
                        DisxClientPacketIndex.ClientPackets.scrolledCheckHit(BlockPos.ZERO, amount, entity.getUUID());
                    }
                    return EventResult.interrupt(true);
                }
            }
        }
        return EventResult.pass();
    }

    //for stamp maker; fixes sneak + click when holding items
    public static CompoundEventResult itemRightClickListener(Player player, InteractionHand hand){
        if (player.equals(Minecraft.getInstance().player)){
            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult != null){
                if (hitResult.getType().equals(HitResult.Type.BLOCK) && player.isCrouching()){
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    BlockState blockState = player.level().getBlockState(blockPos);
                    if (blockState.getBlock().equals(DisxStampMaker.blockRegistration.get())) {
                        blockState.use(player.level(), player, hand, blockHitResult);
                        return CompoundEventResult.interruptTrue(player.getItemInHand(hand));
                    }
                }
            }
        }
        return CompoundEventResult.pass();
    }
}
