package xyz.ar06.disx.audio_filters;

import xyz.ar06.disx.DisxAudioFormatConstants;

public class DisxReverseFilter implements DisxAudioFilter{
    @Override
    public byte[] process(byte[] audio) {
        int wavHeaderSize = 44;
        int length = audio.length;
        int pcmLength = length - wavHeaderSize;
        byte[] temp = new byte[DisxAudioFormatConstants.frameSize];
        for (int i = wavHeaderSize; i < pcmLength / 2; i += DisxAudioFormatConstants.frameSize) {
            int j = length - DisxAudioFormatConstants.frameSize - (i - wavHeaderSize);
            // swap frame at i with frame at j
            System.arraycopy(audio, i, temp, 0, DisxAudioFormatConstants.frameSize);
            System.arraycopy(audio, j, audio, i, DisxAudioFormatConstants.frameSize);
            System.arraycopy(temp, 0, audio, j, DisxAudioFormatConstants.frameSize);
        }
        return audio;
    }

    @Override
    public DisxAudioFilterType getFilterType() {
        return DisxAudioFilterType.REVERSE;
    }
}
