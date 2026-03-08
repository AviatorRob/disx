package xyz.ar06.disx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import xyz.ar06.disx.entities.DisxStampMakerEntity;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DisxServerPacketIndex {

    public static void registerServerPacketReceivers(){
        //NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","playersuccessstatus"), (ServerPacketReceivers::onPlayerSuccessStatusReceive));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","retrieveserveraudioregistry"), ((ServerPacketReceivers::onPlayerRegistryRequest)));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","videoidselection"), ServerPacketReceivers::onVideoIdPushRequest);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","singleplayertrackend"), ServerPacketReceivers::onSingleplayerTrackEnd);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","scrolledcheckhit"), ServerPacketReceivers::onScrolledHitCheck);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","requestconfiginfo"), ServerPacketReceivers::onServerConfigRequest);
    }

    public class ServerPacketReceivers {

        public static void onPlayerSuccessStatusReceive(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
            Logger logger = LoggerFactory.getLogger("disx");
            DisxLogger.debug("got packet");
            String status = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            String videoId = buf.readUtf();
            Boolean fromSoundCommand = buf.readBoolean();
            Boolean playerCanHear = buf.readBoolean();
            Player player = context.getPlayer();
            if (status.equals("Video Not Found") && (fromSoundCommand.equals(true) || playerCanHear.equals(true))){
                DisxSystemMessages.noVideoFound(player);
            }
            if (status.equals("Failed") && (fromSoundCommand.equals(true) || playerCanHear.equals(true))){
                DisxSystemMessages.errorLoading(player);
            }
            if (status.equals("Success") && (fromSoundCommand.equals(true))){
                //DisxSystemMessages.playingAtLocation(player, blockPos, videoId);
            }
        }

        public static void onPlayerRegistryRequest(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String name = "retrieveserveraudioregistry";
            Player player = context.getPlayer();
            for (DisxAudioStreamingNode node : DisxServerAudioRegistry.registry){
                DisxLogger.debug("got registry grab request");
                BlockPos blockPos = node.getBlockPos();
                ResourceLocation dimensionLocation = node.getDimension();
                Player nodeOwner = node.getNodeOwner();
                UUID nodeOwnerUuid = (nodeOwner == null) ? new UUID(0L, 0L) : nodeOwner.getUUID();
                boolean loop = node.isLoop();
                int preferredVolume = node.getPreferredVolume();
                DisxAudioMotionType motionType = node.getMotionType();
                UUID entityUuid = node.getEntityUuid();
                ServerPackets.AudioRegistrySyncPackets.add(player, blockPos, dimensionLocation, nodeOwnerUuid, loop, preferredVolume, motionType, entityUuid);
                DisxLogger.debug("sent registry add event");
            }
        }

        public static void onVideoIdPushRequest(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String videoId = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            MinecraftServer server = context.getPlayer().getServer();
            server.executeIfPossible(() -> {
               BlockEntity entity = context.getPlayer().level().getBlockEntity(blockPos);
               if (entity == null){
                   DisxLogger.debug("ENTITY IS NULL");
               } else {
                   DisxLogger.debug("ENTITY IS NOT NULL");
                   if (entity instanceof DisxStampMakerEntity){
                       ((DisxStampMakerEntity) entity).setVideoId(videoId, context.getPlayer());
                       DisxLogger.debug("SET VIDEO ID TO " + videoId);
                   }
               }
            });
        }

        public static void onSingleplayerTrackEnd(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            BlockPos blockPos = buf.readBlockPos();
            ResourceLocation dimension = buf.readResourceLocation();
            //DisxServerAudioRegistry.removeFromRegistry(blockPos, dimension);
        }

        public static void onScrolledHitCheck(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            BlockPos blockPos = buf.readBlockPos();
            double amount = buf.readDouble();
            UUID entityUuid = buf.readUUID();
            Player player = context.getPlayer();
            if (entityUuid.equals(new UUID(0L, 0L))){
                Vec3 vec3 = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                if (player.position().distanceTo(vec3) <= 15){
                    DisxServerAudioRegistry.incrementVolume(blockPos, player.level().dimension(), amount);
                }
            } else {
                ServerLevel serverLevel = (ServerLevel) context.getPlayer().level();
                blockPos = serverLevel.getEntity(entityUuid).getOnPos();
                Vec3 vec3 = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                if (player.position().distanceTo(vec3) <= 15){
                    DisxServerAudioRegistry.incrementVolume(entityUuid, amount);
                }
            }

        }

        public static void onServerConfigRequest(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            ServerPackets.serverConfigSend(context.getPlayer());
        }
    }

    public class ServerPackets {

        public class AudioRegistrySyncPackets {
            private static void packetBuildSend(String type, Player player, BlockPos pos, ResourceLocation dimension, UUID playerOwner, Boolean loop,
                                                   int preferredVolume, String motionType, UUID entityUuid){
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeUtf(type);
                buf.writeBlockPos(pos);
                buf.writeResourceLocation(dimension);
                buf.writeUUID(playerOwner);
                buf.writeUtf(String.valueOf(loop));
                buf.writeInt(preferredVolume);
                buf.writeUtf(motionType);
                buf.writeUUID(entityUuid);
                NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation("disx","serveraudioregistryevent"), buf);
            }
            public static void add(Player player, BlockPos pos, ResourceLocation dimension, UUID playerOwner, boolean loop,
                                   int preferredVolume, DisxAudioMotionType motionType, UUID entityUuid){
                packetBuildSend("add", player, pos, dimension, playerOwner, loop, preferredVolume, motionType.name(), entityUuid);
            }

            public static void modifyPrefVolume(Player player, BlockPos pos, ResourceLocation dimension, int preferredVolume, DisxAudioMotionType motionType, UUID entityUuid){
                packetBuildSend("modify", player, pos, dimension, new UUID(0L, 0L), null, preferredVolume, motionType.name(), entityUuid);
            }

            public static void modifyLoop(Player player, BlockPos pos, ResourceLocation dimension, boolean loop, DisxAudioMotionType motionType, UUID entityUuid){
                packetBuildSend("modify", player, pos, dimension, new UUID(0L, 0L), loop, -1, motionType.name(), entityUuid);
            }

            public static void remove(Player player, BlockPos pos, ResourceLocation dimension, UUID entityUuid, DisxAudioMotionType motionType){
                packetBuildSend("remove", player, pos, dimension, new UUID(0L, 0L), false, -1, motionType.name(), entityUuid);
            }
        }

        public static void playingVideoIdMessage(String videoId, Player player){
            if (player != null){
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeUtf(videoId);
                DisxLogger.debug("Sent playing vid msg packet");
                NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation("disx","playingvidmsg"), buf);
            } else {
                DisxLogger.debug("Player to send playing message to is null");
            }
        }

        public static void loadingVideoIdMessage(String videoId, Player player){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(videoId);
            NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation("disx","loadingvidmsg"), buf);
        }

        public static void openVideoIdScreen(Player player, BlockPos blockPos){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockPos);
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx","openvideoidscreen"),
                    buf
            );
        }

        public static void mutePlayer(Player player, UUID toMute){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(toMute);
            buf.writeUtf("add");
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx","muteplayer"),
                    buf
            );
        }

        public static void unmutePlayer(Player player, UUID toUnmute){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(toUnmute);
            buf.writeUtf("remove");
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx","muteplayer"),
                    buf
            );
        }

        public static void audioData(Player player, FriendlyByteBuf buf){
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx","audiodata"),
                    buf
            );
        }

        public static void loopMsg(Player player, boolean b){
            DisxLogger.debug("Sending loop message packet");
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBoolean(b);
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx", "loopmsg"),
                    buf
            );
        }

        public static void pauseMsg(ServerPlayer serverPlayer, boolean b){
            DisxLogger.debug("Sending pause message packet");
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBoolean(b);
            NetworkManager.sendToPlayer(
                    serverPlayer,
                    new ResourceLocation("disx", "pausemsg"),
                    buf
            );
        }

        public static void serverConfigSend(Player player){
            DisxLogger.debug("Sending server config to " + player.getName().getString());
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(DisxModInfo.getAudioRadius());
            buf.writeBoolean(DisxModInfo.getSoundParticles());
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx", "configinfo"),
                    buf
            );
        }

        public static void serverConfigSendAll(){
            for (Player p : DisxServerAudioRegistry.getMcPlayers()){
                serverConfigSend(p);
            }
        }
    }

}
