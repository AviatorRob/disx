package xyz.ar06.disx.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DisxJukeboxUsageCooldownDetails {
    private BlockPos blockPos;
    private ResourceLocation dimension;
    private int CooldownCounter;

    private final int CooldownTime = 20;

    public DisxJukeboxUsageCooldownDetails(BlockPos blockPos, ResourceKey<Level> dimension){
        this.blockPos = blockPos;
        this.dimension = dimension.location();
        CooldownCounter = 0;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public int getCooldownCounter() {
        return CooldownCounter;
    }

    public void incrementCounter(){
        this.CooldownCounter += 1;
    }

    public int getCooldownTime() {
        return CooldownTime;
    }
}
