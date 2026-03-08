package xyz.ar06.disx.utils;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;

public interface DisxEnderAdvancedJukeboxInventoryHelper {
    CompoundTag disx$getEnderAdvancedJukeboxInventory();
    void disx$setEnderAdvancedJukeboxInventory(CompoundTag compoundTag);
    void disx$addParticlesAroundSelf(ParticleOptions particleOptions);
}
