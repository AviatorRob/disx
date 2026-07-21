package xyz.ar06.disx.audio_filters;

public enum DisxAudioFilterType {
    TEMPO_1 {
        @Override
        public DisxAudioFilter create() {
            return new DisxTempoFilter(1);
        }
    },
    TEMPO_2 {
        @Override
        public DisxAudioFilter create() {
            return new DisxTempoFilter(2);
        }
    },
    TEMPO_3 {
        @Override
        public DisxAudioFilter create() {
            return new DisxTempoFilter(3);
        }
    },
    ECHO {
        @Override
        public DisxAudioFilter create() {
            return new DisxEchoFilter();
        }
    },
    TREMOLO {
        @Override
        public DisxAudioFilter create() {
            return null;
        }
    },
    REVERSE {
        @Override
        public DisxAudioFilter create() {
            return new DisxReverseFilter();
        }
    },
    CHORUS {
        @Override
        public DisxAudioFilter create() {
            return null;
        }
    },
    DISTORTION {
        @Override
        public DisxAudioFilter create() {
            return null;
        }
    };


    public abstract DisxAudioFilter create();
}
