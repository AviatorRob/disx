package xyz.ar06.disx;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    public static void addToRegistry(BlockPos pos, String videoId, Player player, ResourceKey<Level> dimension, boolean loop){
        ResourceLocation dimensionLocation = dimension.location();
        if (player != null){
            DisxSystemMessages.playingAtLocation(player.getServer(), player.getName().getString(), pos, videoId, dimensionLocation);
        }
        registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, player, loop, 0));
        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, dimensionLocation, UUID.randomUUID(), loop, pos, dimensionLocation);
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, dimensionLocation, player.getUUID(), loop, pos, dimensionLocation);
            });
        }

    }

    //Variant for Sound Command - Takes server, dimension as resourcelocation, and start time
    public static void addToRegistry(BlockPos pos, String videoId, Player player, MinecraftServer server, ResourceLocation dimension, int startTime, boolean loop){
        ResourceLocation dimensionLocation = dimension;
        if (player != null){
            DisxSystemMessages.playingAtLocation(server, player.getName().getString(), pos, videoId, dimensionLocation);
            registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, player, loop, startTime));
        } else {
            DisxSystemMessages.playingAtLocation(server, "Server", pos, videoId, dimensionLocation);
            registry.add(new DisxAudioStreamingNode(videoId, pos, dimensionLocation, null, loop, startTime));
        }

        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, dimensionLocation, UUID.randomUUID(), loop, pos, dimensionLocation);
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, dimensionLocation, player.getUUID(), loop, pos, dimensionLocation);
            });
        }

    }

    public static void removeFromRegistry(DisxAudioStreamingNode node){
        BlockPos pos = node.getBlockPos();
        ResourceLocation dimensionLocation = node.getDimension();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, dimensionLocation, UUID.randomUUID(), false, pos, dimensionLocation);
        });
        node.deconstruct();
        registry.remove(node);
    }

    public static void removeFromRegistry(BlockPos pos, ResourceKey<Level> dimension){
        ResourceLocation dimensionLocation = dimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, dimensionLocation, UUID.randomUUID(), false, pos, dimensionLocation);
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

    public static void modifyRegistryEntry(BlockPos blockPos, ResourceKey<Level> dimension, BlockPos newBlockPos, ResourceKey<Level> newDimension, boolean loop){
        ResourceLocation dimensionLocation = dimension.location();
        ResourceLocation newDimensionLocation = newDimension.location();
        players.forEach(plr -> {
           DisxServerPacketIndex.ServerPackets.playerRegistryEvent("modify", (Player) plr, blockPos, dimensionLocation, UUID.randomUUID(), loop, newBlockPos, newDimensionLocation);
        });
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimensionLocation)){
                node.setLoop(loop);
                node.setBlockPos(newBlockPos);
                node.setDimension(newDimensionLocation);
            }
        }
    }

    public static boolean isPlayingAtLocation(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxAudioStreamingNode node : registry){
            if (node.getBlockPos().equals(blockPos) && node.getDimension().equals(dimension.location())){
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

}