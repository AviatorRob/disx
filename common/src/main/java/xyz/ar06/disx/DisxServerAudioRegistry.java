package xyz.ar06.disx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class DisxServerAudioRegistry {

    public static LinkedList<Player> players = new LinkedList<>();
    public static LinkedList<DisxAudioStreamingNode> registry = new LinkedList<>();

    //Default variant
    public static void addToRegistry(BlockPos pos, String videoId, Player player, ResourceKey<Level> dimension, boolean loop, DisxAudioMotionType motionType, UUID entityUuid){
        ResourceLocation dimensionLocation = dimension.location();
        if (player != null){
            DisxSystemMessages.playingAtLocation(player.getServer(), player.getName().getString(), pos, videoId, dimensionLocation);
        }
        registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, player, loop, 0, motionType, entityUuid));
        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, new UUID(0L, 0L), loop, 100, motionType, entityUuid);
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, player.getUUID(), loop, 100, motionType, entityUuid);
            });
        }

    }

    //Variant for Sound Command - Takes server, dimension as resourcelocation, and start time
    public static void addToRegistry(BlockPos pos, String videoId, Player player, MinecraftServer server, ResourceLocation dimension, int startTime, boolean loop, int volume){
        ResourceLocation dimensionLocation = dimension;
        if (player != null){
            DisxSystemMessages.playingAtLocation(server, player.getName().getString(), pos, videoId, dimensionLocation);
            registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, player, loop, startTime, DisxAudioMotionType.STATIC, new UUID(0L, 0L)));
        } else {
            DisxSystemMessages.playingAtLocation(server, "Server", pos, videoId, dimensionLocation);
            registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, null, loop, startTime, DisxAudioMotionType.STATIC, new UUID(0L, 0L)));
        }

        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, new UUID(0L, 0L), loop, volume, DisxAudioMotionType.STATIC, new UUID(0L, 0L));
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, player.getUUID(), loop, volume, DisxAudioMotionType.STATIC, new UUID(0L, 0L));
            });
        }

    }

    public static void removeFromRegistry(DisxAudioStreamingNode node){
        BlockPos pos = node.getBlockPos();
        ResourceLocation dimensionLocation = node.getDimension();
        DisxAudioMotionType motionType = node.getMotionType();
        UUID entityUuid = node.getEntityUuid();
        if (pos == null || dimensionLocation == null){
            return;
        }
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.remove(plr, pos, dimensionLocation, entityUuid, motionType);
        });
        node.deconstruct();
        registry.remove(node);
    }

    public static void removeFromRegistry(BlockPos pos, ResourceKey<Level> dimension, UUID entityUuid, DisxAudioMotionType motionType){
        DisxLogger.debug("Calling remove from registry");
        ResourceLocation dimensionLocation = dimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.remove(plr, pos, dimensionLocation, entityUuid, motionType);
        });
        if (motionType.equals(DisxAudioMotionType.LIVE)){
            for (DisxAudioStreamingNode node : registry){
                if (node.getEntityUuid().equals((entityUuid)) && node.getMotionType().equals(DisxAudioMotionType.LIVE)){
                    node.deconstruct();
                    registry.remove(node);
                    break;
                }
            }
        } else {
            for (DisxAudioStreamingNode node : registry){
                if (node.getBlockPos().equals(pos) && node.getDimension().equals(dimensionLocation) && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                    node.deconstruct();
                    registry.remove(node);
                    break;
                }
            }
        }
    }

    public static void modifyEntryLoop(BlockPos blockPos, ResourceKey<Level> dimension, boolean loop){
        ResourceLocation dimensionLocation = dimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.modifyLoop(plr, blockPos, dimensionLocation, loop, DisxAudioMotionType.STATIC, new UUID(0L, 0L));
        });
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimensionLocation) && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                node.setLoop(loop);
                break;
            }
        }
    }

    public static void modifyEntryLoop(UUID entityUuid, boolean loop){
        for (DisxAudioStreamingNode node : registry){
            if (node.getEntityUuid().equals(entityUuid) && node.getMotionType().equals(DisxAudioMotionType.LIVE)){
                if (node.isLoop() != loop){
                    DisxLogger.debug("Setting loop in LIVE audio node: " + loop);
                    node.setLoop(loop);
                    players.forEach(plr -> {
                        DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.modifyLoop(plr, BlockPos.ZERO, new ResourceLocation("",""), loop, DisxAudioMotionType.LIVE, entityUuid);
                    });
                    break;
                }
            }
        }

    }

    public static boolean isNodeAtLocation(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location()) && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                return true;
            }
        }
        return false;
    }

    public static boolean isNodeAtLocation(BlockPos blockPos, ResourceLocation dimension){
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension) && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                return true;
            }
        }
        return false;
    }

    public static boolean isNodeOnEntity(UUID entityUuid){
        for (DisxAudioStreamingNode node : registry){
            if (node.getEntityUuid().equals(entityUuid) && node.getMotionType().equals(DisxAudioMotionType.LIVE)){
                return true;
            }
        }
        return false;
    }

    public static boolean isUnpausedAtLocation(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location()) && !node.isPaused() && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                return true;
            }
        }
        return false;
    }

    public static boolean isUnpausedOnEntity(UUID entityUuid){
        for (DisxAudioStreamingNode node : registry){
            if (node.getEntityUuid().equals(entityUuid) && node.getMotionType().equals(DisxAudioMotionType.LIVE) && !node.isPaused()){
                return true;
            }
        }
        return false;
    }


    public static void onServerClose(){
        for (DisxAudioStreamingNode node : registry){
            node.deconstruct();
        }
        registry.clear();
    }

    public static int getRegistryCount(){
        return registry.size();
    }

    public static void forceStopAll(){
        for (DisxAudioStreamingNode node : registry){
            removeFromRegistry(node);
        }
    }

    public static List<Player> getMcPlayers(){
        return players;
    }

    public static void pauseNode(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry) {
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location()) && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                node.pausePlayer();
                break;
            }
        }
    }

    public static void resumeNode(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry) {
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location()) && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                node.resumePlayer();
                break;
            }
        }
    }
    public static boolean pauseOrPlayNode(BlockPos blockPos, ResourceKey<Level> dimension){
        if (isUnpausedAtLocation(blockPos, dimension)){
            pauseNode(blockPos, dimension);
            return true;
        } else {
            resumeNode(blockPos, dimension);
            return false;
        }
    }

    public static void pauseNode(UUID entityUuid){
        for (DisxAudioStreamingNode node : registry) {
            if (node.getEntityUuid().equals(entityUuid) && node.getMotionType().equals(DisxAudioMotionType.LIVE)){
                node.pausePlayer();
                break;
            }
        }
    }

    public static void resumeNode(UUID entityUuid){
        for (DisxAudioStreamingNode node : registry) {
            if (node.getEntityUuid().equals(entityUuid) && node.getMotionType().equals(DisxAudioMotionType.LIVE)){
                node.resumePlayer();
                break;
            }
        }
    }
    public static boolean pauseOrPlayNode(UUID entityUuid){
        if (isUnpausedOnEntity(entityUuid)){
            pauseNode(entityUuid);
            return true;
        } else {
            resumeNode(entityUuid);
            return false;
        }
    }

    public static void incrementVolume(BlockPos blockPos, ResourceKey<Level> dimension, double amount){
        ResourceLocation dimensionLocation = dimension.location();
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimensionLocation) && node.getMotionType().equals(DisxAudioMotionType.STATIC)){
                int modifiedVol = node.incrementVolume(amount);
                for (Player player : players){
                    DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.modifyPrefVolume(player, blockPos, dimensionLocation, modifiedVol, DisxAudioMotionType.STATIC, new UUID(0L, 0L));
                }
            }
        }
    }

    public static void incrementVolume(UUID entityUuid, double amount){
        for (DisxAudioStreamingNode node : registry){
            if (node.getEntityUuid().equals(entityUuid) && node.getMotionType().equals(DisxAudioMotionType.LIVE)){
                int modifiedVol = node.incrementVolume(amount);
                for (Player player : players){
                    DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.modifyPrefVolume(player, BlockPos.ZERO, new ResourceLocation("", ""), modifiedVol, DisxAudioMotionType.LIVE, entityUuid);
                }
            }
        }
    }

}
