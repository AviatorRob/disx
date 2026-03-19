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
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.http.client.config.RequestConfig;
import xyz.ar06.disx.audio_filters.DisxAudioFilterType;
import xyz.ar06.disx.utils.DisxYTDLPWrapper;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxAudioStreamingNode {
    @Deprecated static AudioDataFormat LPformat = StandardAudioDataFormats.COMMON_PCM_S16_BE;
    private int bitDepth = 16;
    private int channelCount = 2;
    private int frameSize = (bitDepth / 8) * channelCount;
    private int sampleRate = 48000;
    private static double streamInterval = 5;
    private int chunkSize = (int) (sampleRate * frameSize * streamInterval); //(calculates to 882000)
    public AudioFormat format = new AudioFormat(
            sampleRate,
            bitDepth,       // sample size in bits
            channelCount,        // channels
            true,     // signed
            true      // big-endian
    );



    @Deprecated private static DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    @Deprecated private static final YoutubeAudioSourceManager youtubeAudioSourceManager = buildYoutubeAudioSourceManager();
    //@Deprecated private AudioPlayer audioPlayer = new DefaultAudioPlayer(playerManager);
    //private AudioInputStream inputStream = AudioPlayerInputStream.createStream(audioPlayer, LPformat, 20000, true);;
    private AudioInputStream inputStream;// = AudioPlayerInputStream.createStream(audioPlayer, LPformat, 20000, true);;

    private byte[] audioDataCache;

    private BlockPos blockPos;
    private ResourceLocation dimension;
    private Player nodeOwner;
    private boolean loop;
    private String videoId;

    private int preferredVolume;
    private boolean paused = false;
    @Deprecated boolean useLiveYtSrc = false;
    private int rogueRadius;

    private DisxAudioMotionType motionType;
    private UUID entityUuid;

    private ArrayList<DisxAudioFilterType> activeFilters;

    private static YoutubeAudioSourceManager buildYoutubeAudioSourceManager(){
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setVideoLoading(true);
        clientOptions.setSearching(false);
        clientOptions.setPlayback(true);
        clientOptions.setPlaylistLoading(false);
        Client[] clients = new Client[]{new Tv(clientOptions), new TvHtml5Embedded(clientOptions)};
        return new YoutubeAudioSourceManager(false, clients);
    }

    public DisxAudioStreamingNode(String videoId, BlockPos blockPos, ResourceLocation dimension, Player nodeOwner, boolean loop, int startTime, DisxAudioMotionType motionType, UUID entityUuid, int rogueRadius, ArrayList<DisxAudioFilterType> audioFilters){
        DisxLogger.debug("New Audio Streaming Node called for; setting details (MOTION TYPE: " + motionType.name() + "); Running Async!");
        this.videoId = videoId;
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.nodeOwner = nodeOwner;
        this.loop = loop;
        //this.audioPlayer.addListener(new TrackHandler());
        this.preferredVolume = 100;
        this.motionType = motionType;
        this.entityUuid = entityUuid;
        this.rogueRadius = rogueRadius;
        this.activeFilters = audioFilters;
        //this.useLiveYtSrc = true; //DisxModInfo.isUseYtsrc();
        CompletableFuture.runAsync(() -> {
            //DisxLogger.debug(this.useLiveYtSrc ? "Server is configured to use live YouTube source; setting load URL" : "Server is configured to use Disx-YTSRC-API; setting load URL");
            //String url = this.useLiveYtSrc ? "https://www.youtube.com/watch?v=" + videoId : "http://disxytsourceapi.ar06.xyz/stream_audio?id=" + videoId;
            DisxLogger.debug("Calling YTDLP to download, cache, delete WHOLE audio file");
            try {
                byte[] receivedData = DisxYTDLPWrapper.downloadToBuffer(videoId);
            } catch (IOException | InterruptedException e) {
                DisxSystemMessages.errorLoading(nodeOwner);
                DisxLogger.error(e);
            }
            try {
                byte[] receivedData = DisxYTDLPWrapper.downloadToBuffer(videoId);
                DisxLogger.debug("YTDLP successfully returned data");
                if (this.activeFilters.contains(DisxAudioFilterType.REVERSE)){
                    DisxLogger.debug("REVERSE effect is active; Reversing audio data before creating cache");
                    int wavHeaderSize = 44;
                    int length = receivedData.length;
                    int pcmLength = length - wavHeaderSize;
                    byte[] temp = new byte[frameSize];
                    for (int i = wavHeaderSize; i < pcmLength / 2; i += frameSize) {
                        int j = length - frameSize - (i - wavHeaderSize);
                        // swap frame at i with frame at j
                        System.arraycopy(receivedData, i, temp, 0, frameSize);
                        System.arraycopy(receivedData, j, receivedData, i, frameSize);
                        System.arraycopy(temp, 0, receivedData, j, frameSize);
                    }
                }
                DisxLogger.debug("Storing audio cache");
                this.audioDataCache = receivedData;
                DisxLogger.debug("Starting data stream loop");
                DisxAudioStreamingNode.this.streamAudioData();
            } catch (Exception e){
                DisxLogger.error("Hit an error trying to load video audio!");
                DisxLogger.error(e);
                DisxSystemMessages.errorLoading(nodeOwner);
                DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
            }


        });

    }

    private void streamAudioData() {
        CompletableFuture.runAsync(() -> {
            try {
                byte[] buffer = new byte[chunkSize];
                DisxServerPacketIndex.ServerPackets.playingVideoIdMessage(DisxAudioStreamingNode.this.videoId, DisxAudioStreamingNode.this.nodeOwner);
                if (inputStream == null){
                    DisxLogger.debug("Input stream found null; making new one");
                    this.inputStream = new AudioInputStream(new ByteArrayInputStream(audioDataCache), format, (audioDataCache.length / format.getFrameSize()));
                }
                int bytesRead;
                while (inputStream != null && !this.isPaused()){
                    bytesRead = inputStream.read(buffer);
                    if (bytesRead != 0 && bytesRead != -1){
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
                if (this.isPaused()){
                    DisxLogger.debug("Audio was paused; streaming should stop");
                    //lastPosition = currentPosition;
                } else if (this.motionType == null){
                    DisxLogger.debug("Audio streaming stopped; audio node deregistered?");
                    //DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                } else {
                    try {
                        if (loop){
                            DisxLogger.debug("Audio streaming finished; loop == true; calling for track replay and restream");
                            //this.audioPlayer.playTrack(cachedTrack.makeClone());
                            this.streamAudioData();
                        } else {
                            DisxLogger.debug("Audio streaming finished; loop != true; waiting to deregister audio node (many seconds) if not already done so");
                            Thread.sleep(((long) streamInterval * 1000L * 2L));
                            DisxLogger.debug("deregistering audio node if not already done so");
                            DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                        }
                    } catch (Exception e) {
                        DisxLogger.error("Failed to remove DisxAudioStreamingNode from server registry:");
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
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
        /*if (this.audioPlayer != null){
            this.audioPlayer.stopTrack();
            this.audioPlayer.destroy();
            this.audioPlayer = null;
        }*/
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
                //this.audioDataCache.close();
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

    @Deprecated public static void initPlayerManager(MinecraftServer server){
        DisxLogger.debug("Initializing audio player manager object");
        playerManager.setHttpRequestConfigurator(requestConfig -> RequestConfig.copy(requestConfig)
                .setSocketTimeout(20000)
                .setConnectTimeout(20000)
                .build()
        );
        playerManager.registerSourceManager(youtubeAudioSourceManager);
        playerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        AudioSourceManagers.registerLocalSource(playerManager);
        playerManager.getConfiguration().setOutputFormat(LPformat);
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

    public int getRogueRadius() { return rogueRadius; }

    public ArrayList<DisxAudioFilterType> getActiveFilters() { return activeFilters; }

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
