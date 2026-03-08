package xyz.ar06.disx.client_only;

public abstract class DisxConfigRecordS2C {
    private static int AUDIO_RADIUS = 25;
    private static boolean SOUND_PARTICLES = true;
    public static void setDetails(int audioRadius, boolean soundParticles){
        AUDIO_RADIUS = audioRadius;
        SOUND_PARTICLES = soundParticles;
    }

    public static int getAudioRadius() {
        return AUDIO_RADIUS;
    }

    public static boolean getSoundParticles(){
        return SOUND_PARTICLES;
    }
}
