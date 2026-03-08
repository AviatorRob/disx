package xyz.ar06.disx;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.minecraft.server.MinecraftServer;
import org.apache.http.client.config.RequestConfig;

import javax.sound.sampled.*;

public class DisxLavaplayerTest {
    private static AudioDataFormat FORMAT = StandardAudioDataFormats.COMMON_PCM_S16_BE;
    private static DefaultAudioPlayerManager PLAYER_MANAGER = new DefaultAudioPlayerManager();
    private static AudioPlayer AUDIO_PLAYER = new DefaultAudioPlayer(PLAYER_MANAGER);

    private static AudioInputStream INPUT_STREAM = AudioPlayerInputStream.createStream(AUDIO_PLAYER, FORMAT, 99999999L, true);

    public static void testTrack(MinecraftServer server){
        DisxLogger.debug("test track called");
        PLAYER_MANAGER.setHttpRequestConfigurator(requestConfig -> RequestConfig.copy(requestConfig)
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .build()
        );
        String URL = "http://disxytsourceapi.ar06.xyz/stream_audio?id=dboPZUcTAW4";
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
        PLAYER_MANAGER.getConfiguration().setOutputFormat(FORMAT);
        DisxLogger.debug("calling load audio");
        PLAYER_MANAGER.loadItem(URL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                DisxLogger.debug("audio loaded");
                AUDIO_PLAYER.addListener(new TrackHandler());
                AUDIO_PLAYER.playTrack(track);
                DisxLogger.debug("called audio player to play track");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
            }
        });
    }


    public static class TrackHandler extends AudioEventAdapter {
        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            DisxLogger.debug("Track load success; track started");
            super.onTrackStart(player, track);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            player.destroy();
            PLAYER_MANAGER.shutdown();
            super.onTrackEnd(player, track, endReason);
        }
    }
}
