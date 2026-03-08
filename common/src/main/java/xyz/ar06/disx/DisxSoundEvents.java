package xyz.ar06.disx;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class DisxSoundEvents {
    public class SoundInstances {
        public static RegistrySupplier<SoundEvent> ADVANCED_JUKEBOX_STATIC;
    }
    public static void registerAdvancedJukeboxStatic(Registrar<SoundEvent> registrar){
        SoundInstances.ADVANCED_JUKEBOX_STATIC = registrar.register(
                new ResourceLocation("disx","advancedjukeboxstatic"),
                () -> SoundEvent.createFixedRangeEvent(
                        new ResourceLocation("disx","advancedjukeboxstatic"),
                        25f)
        );

    }
}
