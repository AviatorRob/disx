package xyz.ar06.disx.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.ar06.disx.DisxModInfo;
import xyz.ar06.disx.DisxServerAudioRegistry;
import xyz.ar06.disx.client_only.DisxAudioInstanceRegistry;
import xyz.ar06.disx.client_only.DisxConfigRecordS2C;
import xyz.ar06.disx.utils.DisxEnderAdvancedJukeboxInventoryHelper;

import java.util.UUID;

@Mixin(Player.class)
public abstract class DisxEnderAdvancedJukeboxMixin implements DisxEnderAdvancedJukeboxInventoryHelper {
    @Unique
    CompoundTag disx$enderAdvancedJukeboxInventoryTag = new CompoundTag();

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci){
        compoundTag.put("EnderAdvancedJukeboxInventory.disx", this.disx$enderAdvancedJukeboxInventoryTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci){
        if (compoundTag.contains("EnderAdvancedJukeboxInventory.disx")){
            this.disx$enderAdvancedJukeboxInventoryTag = compoundTag.getCompound("EnderAdvancedJukeboxInventory.disx");
        }
    }

    @Unique
    public CompoundTag disx$getEnderAdvancedJukeboxInventory(){
        return this.disx$enderAdvancedJukeboxInventoryTag;
    }

    @Unique
    public void disx$setEnderAdvancedJukeboxInventory(CompoundTag enderAdvancedJukeboxInventoryTag) {
        this.disx$enderAdvancedJukeboxInventoryTag = enderAdvancedJukeboxInventoryTag;
    }

    @Unique private int particleTickCount = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci){
        Player player = (Player) (Object) this;
        if (player.level() != null){
            if (player.level().isClientSide()){
                if (DisxAudioInstanceRegistry.isNodeOnEntity(player.getUUID()) && DisxConfigRecordS2C.getSoundParticles()){
                    particleTickCount++;
                    if (particleTickCount == 13){
                        BlockPos blockPos = player.getOnPos();
                        float noteColor = player.level().random.nextInt(25) / 24.0f;
                        player.level().addParticle(
                                ParticleTypes.NOTE,
                                blockPos.getX() + (player.level().random.nextDouble() - 0.5) * 0.6,
                                blockPos.getY() + 1.5,
                                blockPos.getZ() + (player.level().random.nextDouble()) * 0.6,
                                noteColor,
                                0,
                                0
                        );
                        particleTickCount = 0;
                    }
                }
            }
        }
    }
}
