package xyz.ar06.disx;


import com.google.common.io.Files;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import xyz.ar06.disx.audio_filters.DisxAudioFilterType;
import xyz.ar06.disx.utils.DisxYoutubeResolver;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxAudioStreamingNode {
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


    //@Deprecated private AudioPlayer audioPlayer = new DefaultAudioPlayer(playerManager);
    //private AudioInputStream inputStream = AudioPlayerInputStream.createStream(audioPlayer, LPformat, 20000, true);;
    private AudioInputStream inputStream;// = AudioPlayerInputStream.createStream(audioPlayer, LPformat, 20000, true);;
    private File audioFile;

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

            try {
                File audioFile = null;
                if (!this.activeFilters.isEmpty()){
                    DisxLogger.debug("Detected audio filters active; checking for already-processed audio");
                    List<File> filesStream = java.nio.file.Files.list(Path.of(DisxTmpHandler.TMP_PROCESSED_CACHE_PATH))
                            .map(path -> new File(path.toUri()))
                            .toList();
                    for (File file : filesStream){
                        String fileName = file.getName();
                        HashMap<DisxAudioFilterType, Boolean> matchedEffects = new HashMap<>();
                        for (DisxAudioFilterType queriedFilter : this.activeFilters){
                            if (fileName.contains("." + queriedFilter.name())){
                                matchedEffects.put(queriedFilter, true);
                            } else {
                                matchedEffects.put(queriedFilter, false);
                            }
                        }
                        if (!matchedEffects.containsValue(false)){
                            DisxLogger.debug("Found cached processed file, using it");
                            audioFile = file;
                            Thread.sleep(1000);
                            break;
                        }
                    }
                    if (audioFile == null){
                        DisxLogger.debug("Did not find cached processed file, requesting regular version");
                        File downloadFile = DisxYoutubeResolver.resolveFile(videoId);
                        byte[] receivedData = Files.toByteArray(downloadFile);
                        DisxLogger.debug("Beginning audio processing");
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
                        StringBuilder cachePathBuilder = new StringBuilder(DisxTmpHandler.TMP_PROCESSED_CACHE_PATH + "/"
                                + videoId);
                        for (DisxAudioFilterType filter : this.getActiveFilters()){
                            cachePathBuilder.append(".").append(filter.name());
                        }
                        cachePathBuilder.append(".pcm.wav");
                        File cacheFile = new File(cachePathBuilder.toString());
                        Files.write(receivedData, cacheFile);
                        DisxLogger.debug("Processing done; storing audio cache and passing to audio streamer");
                        audioFile = cacheFile;
                    }
                } else {
                    DisxLogger.debug("Requesting audio file from YT-SRC API");
                    audioFile = DisxYoutubeResolver.resolveFile(videoId);
                }
                if (audioFile == null || !audioFile.exists()) throw new IOException("File does not exist");
                DisxLogger.debug("Starting data stream loop");
                this.audioFile = audioFile;
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
                    this.inputStream = new AudioInputStream(new FileInputStream(audioFile), format, (audioFile.length() / format.getFrameSize()));
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
            if (this.audioFile != null){
                //this.audioDataCache.close();
                this.audioFile = null;
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


}
