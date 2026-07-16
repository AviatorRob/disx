package xyz.ar06.disx.audio_filters;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class DisxTempo2Filter implements DisxAudioFilter{
    @Override
    public byte[] process(byte[] audio) {
        int channels = 2;
        double speed = 1.5;
        ShortBuffer shortBuffer = ByteBuffer.wrap(audio)
                .order(ByteOrder.BIG_ENDIAN)
                .asShortBuffer();

        short[] samples = new short[shortBuffer.remaining()];
        shortBuffer.get(samples);

        int inputFrames = samples.length / channels;
        int outputFrames = (int) (inputFrames / speed);

        short[] output = new short[outputFrames * channels];

        double framePos = 0.0;

        for (int outFrame = 0; outFrame < outputFrames; outFrame++) {
            int frame1 = (int) framePos;
            int frame2 = Math.min(frame1 + 1, inputFrames - 1);
            double frac = framePos - frame1;

            for (int ch = 0; ch < channels; ch++) {
                short a = samples[frame1 * channels + ch];
                short b = samples[frame2 * channels + ch];

                output[outFrame * channels + ch] =
                        (short) (a + frac * (b - a));
            }

            framePos += speed;
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
        return DisxAudioFilterType.TEMPO_2;
    }
}
