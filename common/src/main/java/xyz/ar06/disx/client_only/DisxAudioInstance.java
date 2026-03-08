package xyz.ar06.disx.client_only;

import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import xyz.ar06.disx.DisxAudioMotionType;
import xyz.ar06.disx.DisxLogger;

import javax.sound.sampled.*;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxAudioInstance {
    private static final AudioFormat audioFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,     // PCM signed encoding
            44100,                              // Sample rate (44.1kHz is common)
            16,                                 // Sample size in bits (16-bit audio)
            2,                                  // Number of channels (stereo in this case)
            4,                                  // Frame size (2 bytes per channel = 4 bytes per frame)
            44100,                              // Frame rate (same as sample rate for PCM)
            true                                // Big-endian byte order
    );

    private BlockPos blockPos;
    private ResourceLocation dimension;
    private UUID instanceOwner;
    private boolean loop;
    private SourceDataLine audioLine = null;
    private FloatControl volumeControl;
    private FloatControl balanceControl;
    private boolean playerCanHear;
    private String cachedAudioDeviceName = null;
    private int preferredVolume;
    private DisxAudioMotionType motionType;
    private UUID entityUuid;
    public DisxAudioInstance(BlockPos blockPos, ResourceLocation dimension, UUID instanceOwner, boolean loop, int preferredVolume, DisxAudioMotionType motionType, UUID entityUuid){
        DisxLogger.debug("New DisxAudioInstance called for; setting details:");
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.instanceOwner = instanceOwner;
        this.loop = loop;
        this.preferredVolume = preferredVolume;
        this.motionType = motionType;
        this.entityUuid = entityUuid;
        DisxLogger.debug("Details set successfully");
        DisxLogger.debug("Building audio line and controls");
        this.buildAudioLine();
        DisxLogger.debug("Audio line and controls built");
        try {
            ClientTickEvent.CLIENT_LEVEL_POST.register(this::loops);
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Error registering writing and volume loops:");
            e.printStackTrace();
        }
        DisxLogger.debug("DisxAudioInstance initialized successfully!");
    }

    private void loops(ClientLevel clientLevel){
        this.writingLoop(clientLevel);
        this.volumeLoop(clientLevel);
    }

    public void deconstruct(){
        DisxLogger.debug("call for deconstruct");
        try {
            //ClientTickEvent.CLIENT_POST.unregister(this::loops);
            ClientTickEvent.CLIENT_LEVEL_POST.unregister(this::loops);
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Error deregistering writing and volume loops:");
            e.printStackTrace();
        }
        destroyLine();
        this.blockPos = null;
        this.dimension = null;
        this.instanceOwner = null;
        this.loop = false;
    }

    private static String getPreferredAudioDeviceName(){
        String soundDevice = Minecraft.getInstance().options.soundDevice().get();
        if (!soundDevice.isEmpty()){
            String soundDeviceTrimmed = soundDevice.substring(15);
            if (soundDeviceTrimmed.isEmpty()){
                return null;
            } else {
                return soundDeviceTrimmed;
            }
        } else {
            return null;
        }
    }

    private void buildAudioLine() {
        SourceDataLine resultLine = null;
        String preferredDeviceName = getPreferredAudioDeviceName();
        this.cachedAudioDeviceName = preferredDeviceName;
        try {
            if (preferredDeviceName != null) {
                DisxLogger.debug("Preferred audio device is selected");
                Mixer.Info[] mixers = AudioSystem.getMixerInfo();
                for (Mixer.Info mixerInfo : mixers) {
                    if (mixerInfo.getName().equals(preferredDeviceName)) {
                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                        Mixer mixer = AudioSystem.getMixer(mixerInfo);
                        resultLine = (SourceDataLine) mixer.getLine(info);
                        resultLine.open(audioFormat);
                    }
                }
            } else {
                DisxLogger.debug("Preferred audio device is not selected; using system default");
                resultLine = AudioSystem.getSourceDataLine(audioFormat);
                resultLine.open(audioFormat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.audioLine = resultLine;
        this.volumeControl = (FloatControl) resultLine.getControl(FloatControl.Type.MASTER_GAIN);
        this.balanceControl = (FloatControl) resultLine.getControl(FloatControl.Type.BALANCE);
    }

    private boolean hasPreferredAudioDeviceChanged(){
        boolean result;
        String preferredDeviceName = getPreferredAudioDeviceName();
        if (this.cachedAudioDeviceName != null && preferredDeviceName != null){
            result = !this.cachedAudioDeviceName.equals(preferredDeviceName);
        } else if ((this.cachedAudioDeviceName == null && preferredDeviceName != null) || this.cachedAudioDeviceName != null && preferredDeviceName == null) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private boolean writingToLine = false;
    private LinkedList<byte[]> audioDataPacketQueue = new LinkedList<>();

    public void addToPacketDataQueue(byte[] data){
        this.audioDataPacketQueue.add(data);
    }
    private void writingLoop(ClientLevel clientLevel){
        CompletableFuture.runAsync(() -> {
            if (!writingToLine && !audioDataPacketQueue.isEmpty() && this.audioLine != null){
                writingToLine = true;
                DisxLogger.debug("Audio Line not null and Audio Data in queue");
                byte[] audioData = audioDataPacketQueue.poll();
                DisxLogger.debug("Polled audio data packet data");
                if (this.hasPreferredAudioDeviceChanged()){
                    DisxLogger.debug("Preferred audio device changed; rebuilding audio line");
                    this.destroyLine();
                    this.buildAudioLine();
                }
                DisxLogger.debug("Checking if audio line is open");
                if (!this.audioLine.isOpen()){
                    try {
                        DisxLogger.debug("Audio Line not opened, opening...");
                        this.audioLine.open(audioFormat);
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }
                DisxLogger.debug("Checking if audio line is running");
                if (!this.audioLine.isRunning()){
                    DisxLogger.debug("Audio Line not running, starting...");
                    this.audioLine.start();
                }
                DisxLogger.debug("Writing enqueued audio data to Audio Line...");
                DisxLogger.debug("Audio data length: " + audioData.length);
                this.audioLine.write(audioData, 0, audioData.length);
                DisxLogger.debug("Enqueued audio data written to line");
                writingToLine = false;
            }
        });
    }

    private void destroyLine(){
        DisxLogger.debug("Destroying line");
        this.volumeControl.setValue(-80f);
        this.volumeControl = null;
        this.balanceControl = null;
        this.audioLine.close();
        this.audioLine.flush();
        this.audioLine.stop();
        this.audioLine = null;

    }

    private void volumeLoop(ClientLevel clientLevel) {
        if (this.volumeControl != null && this.balanceControl != null && this.audioLine != null) {
            LocalPlayer plr = Minecraft.getInstance().player;
            ResourceLocation dimension = this.dimension;
            if (plr != null){
                if (plr.level() != null){
                    if (plr.level().dimension().location().equals(dimension)) {
                        if (DisxAudioInstanceRegistry.isMuted(this.instanceOwner)){
                            DisxLogger.debug("is muted");
                            volumeControl.setValue(-80f);
                            playerCanHear = false;
                        } else {
                            BlockPos blockPos = null;
                            if (this.motionType.equals(DisxAudioMotionType.LIVE)){
                                for (Entity entity : clientLevel.entitiesForRendering()){
                                    if (entity.getUUID().equals(this.entityUuid)){
                                        blockPos = new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
                                    }
                                }
                            } else {
                                blockPos = this.blockPos;
                            }
                            if (blockPos != null){
                                Vec3 currentpos = Minecraft.getInstance().getCameraEntity().getPosition(0.01f);
                                Vec3 blockPosVec = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                                double distance = currentpos.distanceTo(blockPosVec);
                                double maxDistance = (double) DisxConfigRecordS2C.getAudioRadius();
                                double plrVolumeConfig_RECORDS = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.RECORDS).get();
                                double plrVolumeConfig_MASTER = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER).get();
                                double volumeCalc = Math.max(0.0f, 1.0f - (distance / maxDistance));
                                volumeCalc *= (plrVolumeConfig_MASTER * plrVolumeConfig_RECORDS);
                                volumeCalc *= ((double) this.preferredVolume / 100);
                                float volumeCalc_line = 20.0f * (float) Math.log10(Math.max(0.01f, volumeCalc));
                                if (distance >= maxDistance || preferredVolume == 0){
                                    volumeCalc_line = -80f;
                                }
                                if (volumeCalc_line < -80f) {
                                    volumeCalc_line = -80f;
                                }
                                if (volumeCalc_line > 6.0206) {
                                    volumeCalc_line = 6.0206f;
                                }
                                if (volumeControl != null) volumeControl.setValue(volumeCalc_line);

                                if (volumeCalc_line <= -80f) {
                                    playerCanHear = false;
                                } else {
                                    playerCanHear = true;
                                }

                                if (plrVolumeConfig_RECORDS != 0.0 && volumeCalc_line > -80f && plrVolumeConfig_MASTER != 0.0 && playerCanHear) {
                                    try {
                                        MusicManager musicManager = Minecraft.getInstance().getMusicManager();
                                        Minecraft.getInstance().getMusicManager().tick();
                                        musicManager.stopPlaying();
                                    } catch (NullPointerException e) {
                                        DisxLogger.error("Audio Instance at " + blockPos + " was unsuccessful in pausing client music. Error: " + e);
                                    }
                                }
                            } else {
                                if (volumeControl != null) volumeControl.setValue(-80f);
                                playerCanHear = false;
                            }
                        }
                    } else {
                        if (volumeControl != null) volumeControl.setValue(-80f);
                        playerCanHear = false;
                    }

                }

            }

        }
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setPreferredVolume(int preferredVolume) {
        this.preferredVolume = preferredVolume;
    }

    public int getPreferredVolume() {
        return preferredVolume;
    }

    public DisxAudioMotionType getMotionType() {
        return motionType;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }
}
