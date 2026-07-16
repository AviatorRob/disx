package xyz.ar06.disx.client_only;

import io.netty.buffer.ByteBuf;
import xyz.ar06.disx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class DisxAudioInstanceRegistry {

    public static LinkedList<DisxAudioInstance> registry = new LinkedList<>();

    public static LinkedList<UUID> muted = new LinkedList<>();

    public static void grabServerRegistry(){
        DisxClientPacketIndex.ClientPackets.getServerAudioRegistry();
    }

    public static void newAudioPlayer(BlockPos blockPos, ResourceLocation dimension, UUID instanceOwner, boolean loop, int preferredVolume, DisxAudioMotionType motionType, UUID entityUuid, int rogueRadius){
        registry.add(new DisxAudioInstance(blockPos, dimension, instanceOwner, loop, preferredVolume, motionType, entityUuid, rogueRadius));
        DisxLogger.debug("New DisxAudioInstance registered");
    }

    public static void removeAudioInstance(BlockPos blockPos, ResourceLocation dimension){
        DisxAudioInstance toRemove = null;
        try {
            for (DisxAudioInstance instance : registry){
                if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension) && instance.getMotionType().equals(DisxAudioMotionType.STATIC)){
                    toRemove = instance;
                    instance.deconstruct();
                    break;
                }
            }
            if (toRemove != null){
                registry.remove(toRemove);
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to remove audio instance:");
            e.printStackTrace();
        }

    }

    public static void removeAudioInstance(UUID entityUuid){
        DisxAudioInstance toRemove = null;
        try {
            for (DisxAudioInstance instance : registry){
                if (instance.getEntityUuid().equals(entityUuid) && instance.getMotionType().equals(DisxAudioMotionType.LIVE)){
                    toRemove = instance;
                    instance.deconstruct();
                    break;
                }
            }
            if (toRemove != null){
                registry.remove(toRemove);
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to remove audio instance:");
            e.printStackTrace();
        }

    }

    public static void modifyAudioInstance(BlockPos blockPos, ResourceLocation dimension, Boolean loop, int preferredVolume, int rogueRadius){
        try {
            for (DisxAudioInstance instance : registry){
                if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension) && instance.getMotionType().equals(DisxAudioMotionType.STATIC)){
                    if (loop != null){
                        instance.setLoop(loop);
                    }
                    if (preferredVolume != -1){
                        instance.setPreferredVolume(preferredVolume);
                    }
                    if (rogueRadius != -1){
                        instance.setRogueRadius(rogueRadius);
                    }
                    break;
                }
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to remove audio instance:");
            e.printStackTrace();
        }
    }

    public static void modifyAudioInstance(UUID entityUuid, Boolean loop, int preferredVolume, int rogueRadius){
        try {
            for (DisxAudioInstance instance : registry){
                if (instance.getEntityUuid().equals(entityUuid) && instance.getMotionType().equals(DisxAudioMotionType.LIVE)){
                    if (loop != null){
                        instance.setLoop(loop);
                    }
                    if (preferredVolume != -1){
                        instance.setPreferredVolume(preferredVolume);
                    }
                    if (rogueRadius != -1){instance.setRogueRadius(rogueRadius);}
                    break;
                }
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to remove audio instance:");
            e.printStackTrace();
        }
    }

    public static void clearAllRegisteredInstances(){
        for (DisxAudioInstance instance : registry){
            instance.deconstruct();
        }
        registry.clear();
    }

    public static void pauseAllRegisteredPlayers(){
        /*
        for (DisxAudioPlayerDetails details : registry){
            if (details != null){
                details.getDisxAudioPlayer().pausePlayer();
               }
        }
        */

    }

    public static void unpauseAllRegisteredPlayers(){
        /*
        for (DisxAudioPlayerDetails details : registry) {
            if (details != null) {
                details.getDisxAudioPlayer().unpausePlayer();
            }
        }
        */
    }

    public static void routeAudioData(ByteBuf buf, BlockPos blockPos, ResourceLocation dimension, UUID entityUuid, DisxAudioMotionType motionType){
        if (!buf.isReadable()){
            DisxLogger.error("No readable data found in received audio data packet!");
            return;
        }
        try {
            if (motionType.equals(DisxAudioMotionType.STATIC)){
                for (DisxAudioInstance instance : registry){
                    if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension) && instance.getMotionType().equals(DisxAudioMotionType.STATIC)){
                        byte[] audioData = new byte[DisxAudioFormatConstants.chunkSize];
                        buf.readBytes(audioData);
                        instance.addToPacketDataQueue(audioData);
                        break;
                    }
                }
            } else {
                for (DisxAudioInstance instance : registry){
                    if (instance.getEntityUuid().equals(entityUuid) && instance.getMotionType().equals(DisxAudioMotionType.LIVE)){
                        byte[] audioData = new byte[DisxAudioFormatConstants.chunkSize];
                        buf.readBytes(audioData);
                        instance.addToPacketDataQueue(audioData);
                        break;
                    }
                }
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to route audio data to audio instance:");
            e.printStackTrace();
        }
    }

    public static void onPlayDisconnect() {
        clearAllRegisteredInstances();
    }

    public static void onClientStopping(Minecraft client) {
        clearAllRegisteredInstances();
    }

    public static void onClientPause(){
        pauseAllRegisteredPlayers();
    }

    public static void onClientUnpause(){
        unpauseAllRegisteredPlayers();
    }

    public static String addToMuted(UUID uuid){
        if (muted.contains(uuid)){
            DisxSystemMessages.mutedAlready();
            return "duplicate";
        } else {
            muted.add(uuid);
            DisxSystemMessages.successfulMutation();
            return "success";
        }
    }

    public static String removeFromMuted(UUID uuid){
        if (muted.contains(uuid)){
            muted.remove(uuid);
            DisxSystemMessages.successfulUnmutation();
            return "success";
        } else {
            DisxSystemMessages.notMuted();
            return "notfoundonit";
        }
    }

    public static boolean isMuted(UUID uuid){
        if (muted.contains(uuid)){
            return true;
        } else {
            return false;
        }
    }

    public static int getPreferredVolume(BlockPos blockPos, ResourceLocation dimension){
        for (DisxAudioInstance instance : registry){
            if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension) && instance.getMotionType().equals(DisxAudioMotionType.STATIC)){
                return instance.getPreferredVolume();
            }
        }
        return -1;
    }

    public static int getPreferredVolume(UUID entityUuid){
        for (DisxAudioInstance instance : registry){
            if (instance.getEntityUuid().equals(entityUuid) && instance.getMotionType().equals(DisxAudioMotionType.LIVE)){
                return instance.getPreferredVolume();
            }
        }
        return -1;
    }

    public static boolean isNodeAtLocation(BlockPos blockPos, ResourceLocation dimension){
        for (DisxAudioInstance instance : registry){
            if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension) && instance.getMotionType().equals(DisxAudioMotionType.STATIC)){
                return true;
            }
        }
        return false;
    }

    public static boolean isNodeOnEntity(UUID entityUuid){
        for (DisxAudioInstance instance : registry){
            if (instance.getEntityUuid().equals(entityUuid) && instance.getMotionType().equals(DisxAudioMotionType.LIVE)){
                return true;
            }
        }
        return false;
    }
}
