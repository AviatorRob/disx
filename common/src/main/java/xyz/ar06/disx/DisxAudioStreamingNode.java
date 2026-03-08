package xyz.ar06.disx;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSource;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.http.client.config.RequestConfig;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.utils.DisxYTDLPWrapper;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxAudioStreamingNode {
    public static AudioDataFormat FORMAT = StandardAudioDataFormats.COMMON_PCM_S16_BE;
    private static DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private static final YoutubeAudioSourceManager youtubeAudioSourceManager = buildYoutubeAudioSourceManager();
    private static double streamInterval = 5;
    private AudioPlayer audioPlayer = new DefaultAudioPlayer(playerManager);
    private AudioInputStream inputStream = AudioPlayerInputStream.createStream(audioPlayer, FORMAT, 20000, true);

    private ByteArrayInputStream audioDataCache;
    private BlockPos blockPos;
    private ResourceLocation dimension;
    private Player nodeOwner;
    private boolean loop;
    private String videoId;
    private AudioTrack cachedTrack;

    private int preferredVolume;
    private int lastPosition;
    private boolean paused = false;
    @Deprecated boolean useLiveYtSrc = false;

    private DisxAudioMotionType motionType;
    private UUID entityUuid;

    private static YoutubeAudioSourceManager buildYoutubeAudioSourceManager(){
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setVideoLoading(true);
        clientOptions.setSearching(false);
        clientOptions.setPlayback(true);
        clientOptions.setPlaylistLoading(false);
        Client[] clients = new Client[]{new Tv(clientOptions), new TvHtml5Embedded(clientOptions)};
        return new YoutubeAudioSourceManager(false, clients);
    }

    public DisxAudioStreamingNode(String videoId, BlockPos blockPos, ResourceLocation dimension, Player nodeOwner, boolean loop, int startTime, DisxAudioMotionType motionType, UUID entityUuid){
        DisxLogger.debug("New Audio Streaming Node called for; setting details (MOTION TYPE: " + motionType.name() + "); Running Async!");
        this.videoId = videoId;
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.nodeOwner = nodeOwner;
        this.loop = loop;
        this.audioPlayer.addListener(new TrackHandler());
        this.preferredVolume = 100;
        this.motionType = motionType;
        this.entityUuid = entityUuid;
        //this.useLiveYtSrc = true; //DisxModInfo.isUseYtsrc();
        CompletableFuture.runAsync(() -> {
            //DisxLogger.debug(this.useLiveYtSrc ? "Server is configured to use live YouTube source; setting load URL" : "Server is configured to use Disx-YTSRC-API; setting load URL");
            //String url = this.useLiveYtSrc ? "https://www.youtube.com/watch?v=" + videoId : "http://disxytsourceapi.ar06.xyz/stream_audio?id=" + videoId;
            DisxLogger.debug("Calling for YTDLP Wrapper to grab audio URL");
            String url = "";
            try {
                url = DisxYTDLPWrapper.getVideoAudioURL(videoId);
            } catch (Exception e){
                if (e.getMessage().equals("YT-DLP FAILED START")){
                    DisxLogger.error("YTDLP backend failed to start!");
                }
                if (e.getMessage().equals("NO RETURN URL")){
                    DisxLogger.error("Unable to load specified video");
                }
            }
            DisxLogger.debug("Attempting to load requested video");
            playerManager.loadItem(url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    DisxLogger.debug("Loaded requested video");
                    if (startTime != 0){
                        DisxLogger.debug("Setting position to " + startTime + " seconds");
                        track.setPosition(startTime * 1000L);
                        DisxLogger.debug("Is track seekable?: " + track.isSeekable());
                    }
                    cachedTrack = track.makeClone();
                    DisxLogger.debug("Track length: " + track.getDuration());
                    audioPlayer.playTrack(track);
                    DisxLogger.debug("Ordering streaming of data direct from audio input stream to begin");
                    DisxAudioStreamingNode.this.streamAudioData();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    DisxLogger.error("Playlist loaded?? what is this heresy?");
                }

                @Override
                public void noMatches() {
                    if (nodeOwner != null){
                        DisxSystemMessages.errorLoading(nodeOwner);
                    }
                    DisxLogger.error("Unable to load specified video");
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    DisxLogger.error("Unable to load specified video. Does it exist?");
                    DisxSystemMessages.errorLoading(nodeOwner);
                }
            });
        });

    }

    private void streamAudioData() {
        CompletableFuture.runAsync(() -> {
            try {
                int bitDepth = 16;
                int frameSize = (bitDepth / 8) * FORMAT.channelCount;
                int sampleRate = FORMAT.sampleRate;
                int chunkSize = (int) (sampleRate * frameSize * streamInterval); //(calculates to 882000)
                byte[] buffer = new byte[chunkSize];
                DisxServerPacketIndex.ServerPackets.playingVideoIdMessage(DisxAudioStreamingNode.this.videoId, DisxAudioStreamingNode.this.nodeOwner);

                if (inputStream != null) {
                    int bytesRead;
                    while (inputStream != null && !this.isPaused()) {
                        bytesRead = inputStream.read(buffer);
                        if (bytesRead != 0 && bytesRead != -1) {
                            for (Player p : DisxServerAudioRegistry.getMcPlayers()) {
                                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                                buf.writeBlockPos(this.blockPos);
                                buf.writeResourceLocation(this.dimension);
                                buf.writeUtf(this.motionType.name());
                                buf.writeUUID(this.entityUuid);
                                buf.writeBytes(buffer, 0, bytesRead);
                                DisxServerPacketIndex.ServerPackets.audioData(p, buf);
                            }
                            Thread.sleep((long) (streamInterval * 1000L));
                        } else {
                            break;
                        }
                    }
                    if (this.isPaused()) {
                        DisxLogger.debug("Audio was paused; streaming should stop");
                        //lastPosition = currentPosition;
                    } else if (this.motionType == null) {
                        DisxLogger.debug("Audio streaming stopped; audio node deregistered?");
                        //DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                    } else {
                        try {
                            if (loop) {
                                DisxLogger.debug("Audio streaming finished; loop == true; calling for track replay and restream (live yt src)");
                                this.audioPlayer.playTrack(cachedTrack.makeClone());
                                this.streamAudioData();
                            } else {
                                DisxLogger.debug("Audio streaming finished; loop != true; waiting to deregister audio node if not already done so (live yt src)");
                                Thread.sleep(((long) streamInterval * 1000L) + 1000L);
                                DisxLogger.debug("deregistering audio node if not already done so");
                                DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                            }
                        } catch (Exception e) {
                            DisxLogger.error("Failed to remove DisxAudioStreamingNode from server registry:");
                            e.printStackTrace();
                        }
                    }
                } else {
                    DisxLogger.error("Audio input stream is null (live yt src)");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Deprecated private void readAudioInputStream(){
        CompletableFuture.runAsync(() -> {
           try {
               int bitDepth = 16;
               int frameSize = (bitDepth / 8) * FORMAT.channelCount;
               int sampleRate = FORMAT.sampleRate;
               int bytesPerSecond = sampleRate * frameSize;
               int bufferSize = frameSize * 480;
               byte[] buffer = new byte[bufferSize];
               int bytesRead;
               ByteArrayOutputStream audioDataBridge = new ByteArrayOutputStream();
               if (inputStream != null){
                   DisxLogger.debug("Reading audio input stream");
                   while (inputStream != null) {
                       if (this.blockPos != null && this.dimension != null){
                           bytesRead = inputStream.read(buffer);
                           audioDataBridge.write(buffer, 0, bytesRead);
                           //long sleepTimeMs = (bytesRead * 1000L) / bytesPerSecond;
                           //Thread.sleep(sleepTimeMs);
                       }
                   }
                   DisxLogger.debug("Audio input stream read; creating cache");
                   this.audioDataCache = new ByteArrayInputStream(audioDataBridge.toByteArray());
                   audioDataBridge.close();
                   DisxLogger.debug("Audio data cached successfully; registering data stream loop; dismantling DefaultAudioPlayer object");
                   DisxAudioStreamingNode.this.streamAudioData();
                   this.audioPlayer.stopTrack();
                   this.audioPlayer.destroy();
                   this.audioPlayer = null;
               } else {
                   DisxLogger.error("Audio input stream is null!");
               }
           } catch (Exception e){
               e.printStackTrace();
           }
        });
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void deconstruct(){
        if (this.audioPlayer != null){
            this.audioPlayer.stopTrack();
            this.audioPlayer.destroy();
            this.audioPlayer = null;
        }
        this.blockPos = null;
        this.dimension = null;
        this.entityUuid = null;
        this.motionType = null;
        this.loop = false;
        //this.nodeOwner = null;
        this.videoId = null;
        this.paused = false;
        try {
            if (this.inputStream != null){
                this.inputStream.close();
                this.inputStream = null;
            }
            if (this.audioDataCache != null){
                this.audioDataCache.close();
                this.audioDataCache = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pausePlayer(){
        this.paused = true;
    }

    public void resumePlayer(){
        this.paused = false;
        this.streamAudioData();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public String getVideoId() {
        return videoId;
    }

    public Player getNodeOwner() {
        return nodeOwner;
    }

    public boolean isLoop() {
        return loop;
    }

    public boolean isPaused(){
        return this.paused;
    }

    public static void shutdownPlayerManager(){
        playerManager.shutdown();
    }

    public static void initPlayerManager(MinecraftServer server){
        DisxLogger.debug("Initializing audio player manager object");
        playerManager.setHttpRequestConfigurator(requestConfig -> RequestConfig.copy(requestConfig)
                .setSocketTimeout(20000)
                .setConnectTimeout(20000)
                .build()
        );
        playerManager.registerSourceManager(youtubeAudioSourceManager);
        playerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        AudioSourceManagers.registerLocalSource(playerManager);
        playerManager.getConfiguration().setOutputFormat(FORMAT);
    }

    public int getVolume(){
        return this.audioPlayer.getVolume();
    }

    public int incrementVolume(double amount){
        int currentVolume = this.preferredVolume;
        int castedAmount = (int) amount;
        int newVolume = currentVolume + (castedAmount * 10);
        if (newVolume > 200){
            newVolume = 200;
        }
        if (newVolume < 0){
            newVolume = 0;
        }
        return this.preferredVolume = newVolume;
    }

    public int getPreferredVolume() {
        return preferredVolume;
    }

    public static double getStreamInterval() {
        return streamInterval;
    }

    public DisxAudioMotionType getMotionType() {
        return motionType;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public static YoutubeAudioSourceManager getYoutubeAudioSourceManager() {
        return youtubeAudioSourceManager;
    }

    public class TrackHandler extends AudioEventAdapter {
        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            /*
            DisxLogger.debug("Calling for now playing message packet to be sent");
            DisxServerPacketIndex.ServerPackets.playingVideoIdMessage(DisxAudioStreamingNode.this.videoId, DisxAudioStreamingNode.this.nodeOwner);
            DisxLogger.debug("Audio starting; registered audio data read and send loops");
*/
            super.onTrackStart(player, track);
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            if (exception.getMessage().equals("Please sign in")){
                DisxLogger.debug("Oauth2 error detected; sending node owner message");
                DisxSystemMessages.signInError((ServerPlayer) nodeOwner);
            } else
            if (exception.getCause().toString().equals("java.lang.RuntimeException: Not success status code: 403")){
                DisxLogger.debug("Oauth2 error detected; sending node owner message");
                DisxSystemMessages.status403Error((ServerPlayer) nodeOwner);
            }
            super.onTrackException(player, track, exception);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (endReason.equals(AudioTrackEndReason.FINISHED)){
                try {
                    if (!loop){
                        DisxLogger.debug("Audio input stream read reached end of audio; loop != true; closing audio input stream (live yt source)");
                        inputStream.close();
                        DisxAudioStreamingNode.this.inputStream = null;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            /*
            if (loop && endReason.equals(AudioTrackEndReason.FINISHED)){
                DisxLogger.debug("Track finished, loop = true; replaying track");
                AudioTrack toLoop = cachedTrack.makeClone();
                player.playTrack(toLoop);
            } else if (endReason.equals(AudioTrackEndReason.FINISHED)){
                    DisxLogger.debug("Track finished, loop != true; unregistering send audio data loop and deregistering node in 6 second delay");
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(6000);
                            DisxLogger.debug("6 second finish window is closed; deregistering audio node");
                            DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                        } catch (Exception e) {
                            DisxLogger.error("Failed to remove DisxAudioStreamingNode from server registry:");
                            e.printStackTrace();
                        }

                    });
            }
             */
            super.onTrackEnd(player, track, endReason);
        }
    }

}
