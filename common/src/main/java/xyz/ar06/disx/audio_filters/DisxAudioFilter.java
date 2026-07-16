package xyz.ar06.disx.audio_filters;

public interface DisxAudioFilter {
    byte[] process(byte[] audio);
    DisxAudioFilterType getFilterType();
}
