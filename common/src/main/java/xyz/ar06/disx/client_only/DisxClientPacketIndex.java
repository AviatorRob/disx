package xyz.ar06.disx.client_only;

import io.netty.buffer.ByteBuf;
import xyz.ar06.disx.DisxAudioMotionType;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.client_only.gui.screens.DisxStampMakerGUI;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class DisxClientPacketIndex  {

    static Logger logger = LoggerFactory.getLogger("disx");
    public static void registerClientPacketReceivers() {
        //NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","audioplayerplayevent"), (ClientPacketReceivers::receiveAudioPlayerPlayEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","serveraudioregistryevent"), (ClientPacketReceivers::receiveServerAudioRegistryEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","playingvidmsg"), (ClientPacketReceivers::receiveLoadedMsgEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","openvideoidscreen"), (ClientPacketReceivers::receiveOpenVideoIdScreenEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx", "muteplayer"), ClientPacketReceivers::receivePlayerMute);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","audiodata"), ClientPacketReceivers::receiveAudioData);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","loadingvidmsg"), ClientPacketReceivers::receiveLoadingMsgEvent);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","loopmsg"), ClientPacketReceivers::receiveLoopMsg);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","pausemsg"), ClientPacketReceivers::receivePauseMsg);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx", "configinfo"), ClientPacketReceivers::receiveConfigInfo);
    }

    public class ClientPacketReceivers{

        /* MARK FOR REMOVAL
        public static void receiveAudioPlayerPlayEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
            String videoId = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            Boolean fromSoundCommand = buf.readBoolean();
            int seconds = 0;
            DisxAudioPlayer newAudioPlayer = null;

            newAudioPlayer = new DisxAudioPlayer(blockPos, videoId, fromSoundCommand, seconds);
        }
         */

        public static void receiveServerAudioRegistryEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            DisxLogger.debug("got audio registry event");
            String type = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            ResourceLocation dimensionLocation = buf.readResourceLocation();
            UUID instanceOwner = buf.readUUID();
            Boolean loop = Boolean.valueOf(buf.readUtf());
            int preferredVolume = buf.readInt();
            String motionTypeUtf = buf.readUtf();
            DisxAudioMotionType motionType;
            if (!motionTypeUtf.isEmpty()){
                motionType = DisxAudioMotionType.valueOf(motionTypeUtf);
            } else {
                motionType = null;
            }
            UUID entityUuid = buf.readUUID();
            if (type.equals("add")){
                DisxLogger.debug("calling for add");
                CompletableFuture.runAsync(() -> {
                    DisxAudioInstanceRegistry.newAudioPlayer(blockPos, dimensionLocation, instanceOwner, loop, preferredVolume, motionType, entityUuid);
                });
            }
            if (type.equals("remove")){
                CompletableFuture.runAsync(() -> {
                    if (motionType.equals(DisxAudioMotionType.LIVE)){
                        DisxAudioInstanceRegistry.removeAudioInstance(entityUuid);
                    } else {
                        DisxAudioInstanceRegistry.removeAudioInstance(blockPos, dimensionLocation);
                    }

                });
            }
            if (type.equals("modify")){
                CompletableFuture.runAsync(() -> {
                    if (motionType.equals(DisxAudioMotionType.LIVE)){
                        DisxAudioInstanceRegistry.modifyAudioInstance(entityUuid, loop, preferredVolume);
                    } else {
                        DisxAudioInstanceRegistry.modifyAudioInstance(blockPos, dimensionLocation, loop, preferredVolume);
                    }
                });
            }
        }

        public static void receiveLoadedMsgEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String videoId = buf.readUtf();
            DisxLogger.debug("Sending playing video message");
            DisxSystemMessages.playingVideo(videoId);
        }

        public static void receiveLoadingMsgEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String videoId = buf.readUtf();
            DisxSystemMessages.loadingVideo(videoId);
        }

        public static void receiveOpenVideoIdScreenEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            BlockPos blockPos = buf.readBlockPos();
            Minecraft.getInstance().execute(() -> {
                DisxStampMakerGUI.setScreen(blockPos);
            });
        }

        public static void receivePlayerMute(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            UUID uuid = buf.readUUID();
            String action = buf.readUtf();
            if (action.equals("add")){
                DisxAudioInstanceRegistry.addToMuted(uuid);
            } else if (action.equals("remove")){
                DisxAudioInstanceRegistry.removeFromMuted(uuid);
            }
        }

        public static void receiveAudioData(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            DisxLogger.debug("audio data packet received");
            if (!buf.isReadable()){
                DisxLogger.error("No readable data found in received audio data packet!");
                return;
            }
            BlockPos blockPos = buf.readBlockPos();
            ResourceLocation dimension = buf.readResourceLocation();
            String motionTypeUtf = buf.readUtf();
            DisxAudioMotionType motionType;
            if (!motionTypeUtf.isEmpty()){
                motionType = DisxAudioMotionType.valueOf(motionTypeUtf);
            } else {
                motionType = DisxAudioMotionType.STATIC;
            }
            UUID entityUuid = buf.readUUID();
            ByteBuf bufCopy = buf.copy();
            CompletableFuture.runAsync(() -> DisxAudioInstanceRegistry.routeAudioData(bufCopy, blockPos, dimension, entityUuid, motionType));
        }

        public static void receiveLoopMsg(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            boolean b = buf.readBoolean();
            DisxSystemMessages.loopStatusMsg(b);
        }

        public static void receivePauseMsg(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            boolean b = buf.readBoolean();
            DisxSystemMessages.pauseStatusMsg(b);
        }

        public static void receiveConfigInfo(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            int audioRadius = buf.readInt();
            boolean soundParticles = buf.readBoolean();
            DisxConfigRecordS2C.setDetails(audioRadius, soundParticles);
        }

    }

    public class ClientPackets{
        public static void playerSuccessStatus(String status, BlockPos blockPos, String videoId, Boolean fromSoundCommand, Boolean playerCanHear){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(status);
            buf.writeBlockPos(blockPos);
            buf.writeUtf(videoId);
            buf.writeBoolean(fromSoundCommand);
            buf.writeBoolean(playerCanHear);
            NetworkManager.sendToServer(new ResourceLocation("disx","playersuccessstatus"), buf);
        }

        public static void getServerAudioRegistry(){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            NetworkManager.sendToServer(new ResourceLocation("disx","retrieveserveraudioregistry"), buf);
        }

        public static void pushVideoId(String videoId, BlockPos blockPos){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(videoId);
            buf.writeBlockPos(blockPos);
            NetworkManager.sendToServer(
                    new ResourceLocation("disx","videoidselection"),
                    buf
            );
        }

        public static void singleplayerTrackEnd(BlockPos blockPos, ResourceLocation dimension){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockPos);
            buf.writeResourceLocation(dimension);
            NetworkManager.sendToServer(
                    new ResourceLocation("disx","singleplayertrackend"),
                    buf
            );
        }

        public static void scrolledCheckHit(BlockPos blockPos, double amount, UUID entityUuid){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockPos);
            buf.writeDouble(amount);
            buf.writeUUID(entityUuid);
            NetworkManager.sendToServer(
                    new ResourceLocation("disx","scrolledcheckhit"),
                    buf
            );
        }

        public static void getServerConfig(){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            NetworkManager.sendToServer(
                    new ResourceLocation("disx","requestconfiginfo"),
                    buf
            );
        }

    }

}
