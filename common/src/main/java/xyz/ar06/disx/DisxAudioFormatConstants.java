package xyz.ar06.disx;

import javax.sound.sampled.AudioFormat;

public final class DisxAudioFormatConstants {
    private DisxAudioFormatConstants() {
        // Prevent instantiation
    }

    public static final int bitDepth = 16;
    public static final int channelCount = 2;
    public static final int frameSize = (bitDepth / 8) * channelCount;
    public static final int sampleRate = 48000;
    public static final double streamInterval = 5;
    public static final int chunkSize = (int) (sampleRate * frameSize * streamInterval); //(calculates to 882000)
    public static final AudioFormat format = new AudioFormat(
            sampleRate,
            bitDepth,       // sample size in bits
            channelCount,        // channels
            true,     // signed
            true      // big-endian
    );
}
