package xyz.ar06.disx.audio_filters;

import xyz.ar06.disx.DisxAudioFormatConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class DisxEchoFilter implements DisxAudioFilter{
    @Override
    public byte[] process(byte[] audio) {
        final int sampleRate = DisxAudioFormatConstants.sampleRate;
        final int channels = DisxAudioFormatConstants.channelCount;

        // Echo parameters
        final int delayMs = 200;
        final float decay = 0.35f;

        int delayFrames = sampleRate * delayMs / 1000;

        // Convert byte[] -> short[]
        ShortBuffer shortBuffer = ByteBuffer.wrap(audio)
                .order(ByteOrder.BIG_ENDIAN)
                .asShortBuffer();

        short[] input = new short[shortBuffer.remaining()];
        shortBuffer.get(input);

        int inputFrames = input.length / channels;

        // Extend output so the echo can ring out
        short[] output = new short[(inputFrames + delayFrames) * channels];

        // Copy original audio
        System.arraycopy(input, 0, output, 0, input.length);

        // Add the echo
        for (int frame = 0; frame < inputFrames; frame++) {

            int echoFrame = frame + delayFrames;

            for (int ch = 0; ch < channels; ch++) {

                int src = frame * channels + ch;
                int dst = echoFrame * channels + ch;

                int mixed = output[dst] + (int) (input[src] * decay);

                // Clamp to 16-bit
                mixed = Math.max(Short.MIN_VALUE,
                        Math.min(Short.MAX_VALUE, mixed));

                output[dst] = (short) mixed;
            }
        }

        // Convert short[] -> byte[]
        ByteBuffer out = ByteBuffer.allocate(output.length * 2)
                .order(ByteOrder.BIG_ENDIAN);

        for (short sample : output) {
            out.putShort(sample);
        }

        return out.array();
    }

    @Override
    public DisxAudioFilterType getFilterType() {
        return DisxAudioFilterType.ECHO;
    }
}
