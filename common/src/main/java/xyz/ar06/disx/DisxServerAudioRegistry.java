package xyz.ar06.disx;

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
    public static void addToRegistry(BlockPos pos, String videoId, Player player, ResourceKey<Level> dimension, boolean loop, DisxAudioMotionType motionType){
        ResourceLocation dimensionLocation = dimension.location();
        if (player != null){
            DisxSystemMessages.playingAtLocation(player.getServer(), player.getName().getString(), pos, videoId, dimensionLocation);
        }
        registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, player, loop, 0, motionType));
        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, UUID.randomUUID(), loop, 100, motionType);
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, player.getUUID(), loop, 100, motionType);
            });
        }

    }

    //Variant for Sound Command - Takes server, dimension as resourcelocation, and start time
    public static void addToRegistry(BlockPos pos, String videoId, Player player, MinecraftServer server, ResourceLocation dimension, int startTime, boolean loop, int volume){
        ResourceLocation dimensionLocation = dimension;
        if (player != null){
            DisxSystemMessages.playingAtLocation(server, player.getName().getString(), pos, videoId, dimensionLocation);
            registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, player, loop, startTime, DisxAudioMotionType.STATIC));
        } else {
            DisxSystemMessages.playingAtLocation(server, "Server", pos, videoId, dimensionLocation);
            registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, null, loop, startTime, DisxAudioMotionType.STATIC));
        }

        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, UUID.randomUUID(), loop, 100, DisxAudioMotionType.STATIC);
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.add(plr, pos, dimensionLocation, player.getUUID(), loop, 100, DisxAudioMotionType.STATIC);
            });
        }

    }

    public static void removeFromRegistry(DisxAudioStreamingNode node){
        BlockPos pos = node.getBlockPos();
        ResourceLocation dimensionLocation = node.getDimension();
        if (pos == null || dimensionLocation == null){
            return;
        }
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.remove(plr, pos, dimensionLocation);
        });
        node.deconstruct();
        registry.remove(node);
    }

    public static void removeFromRegistry(BlockPos pos, ResourceKey<Level> dimension){
        DisxLogger.debug("Calling remove from registry");
        ResourceLocation dimensionLocation = dimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.remove(plr, pos, dimensionLocation);
        });
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(pos) && node.getDimension().equals(dimensionLocation)){
                node.deconstruct();
                registry.remove(node);
                break;
            }
        }
    }

    //RemoveFromRegistry variant for singleplayer track ending
    public static void removeFromRegistry(BlockPos pos, ResourceLocation dimensionLocation){
        /*players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, "", false, 0, dimensionLocation, UUID.randomUUID(), false, pos, dimensionLocation);
        });
        ArrayList<DisxAudioStreamingNode> toRemove = new ArrayList<>();
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(pos) && node.getDimension().equals(dimensionLocation)){
                toRemove.add(node);
            }
        }
        for (DisxAudioStreamingNode details : toRemove){
            registry.remove(details);
        }
         */
    }

    public static void modifyEntryLocation(BlockPos blockPos, ResourceKey<Level> dimension, BlockPos newBlockPos, ResourceKey<Level> newDimension){
        ResourceLocation dimensionLocation = dimension.location();
        ResourceLocation newDimensionLocation = newDimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.modifyLocation(plr, blockPos, dimensionLocation, newBlockPos, newDimensionLocation);
        });
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimensionLocation)){
                node.setBlockPos(newBlockPos);
                node.setDimension(newDimensionLocation);
            }
        }
    }

    public static void modifyEntryLoop(BlockPos blockPos, ResourceKey<Level> dimension, boolean loop){
        ResourceLocation dimensionLocation = dimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.modifyLoop(plr, blockPos, dimensionLocation, loop);
        });
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimensionLocation)){
                node.setLoop(loop);
            }
        }
    }

    public static boolean isNodeAtLocation(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location())){
                return true;
            }
        }
        return false;
    }

    public static boolean isNodeAtLocation(BlockPos blockPos, ResourceLocation dimension){
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension)){
                return true;
            }
        }
        return false;
    }

    public static boolean isUnpausedAtLocation(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location()) && !node.isPaused()){
                return true;
            }
        }
        return false;
    }

    public static void onServerClose(){
        for (DisxAudioStreamingNode node : registry){
            node.deconstruct();
        }
        DisxAudioStreamingNode.shutdownPlayerManager();
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
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location())){
                node.pausePlayer();
                break;
            }
        }
    }

    public static void resumeNode(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry) {
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location())){
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

    public static void incrementVolume(BlockPos blockPos, ResourceKey<Level> dimension, double amount){
        ResourceLocation dimensionLocation = dimension.location();
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimensionLocation)){
                int modifiedVol = node.incrementVolume(amount);
                for (Player player : players){
                    DisxServerPacketIndex.ServerPackets.AudioRegistrySyncPackets.modifyPrefVolume(player, blockPos, dimensionLocation, modifiedVol);
                }
            }
        }
    }

}
