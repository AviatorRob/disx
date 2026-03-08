package xyz.ar06.disx.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;


public class DisxJukeboxUsageCooldownManager {
    private static final ArrayList<DisxJukeboxUsageCooldownDetails> COOLDOWNS = new ArrayList<>();
    private static final int COOLDOWN_TIME = 20; // Cooldown time in ticks (20 ticks = 1 second)

    public static void updateCooldown(BlockPos pos, ResourceKey<Level> dimension) {
        COOLDOWNS.add(new DisxJukeboxUsageCooldownDetails(pos, dimension));
    }

    public static void tickCooldowns() {
        ArrayList<DisxJukeboxUsageCooldownDetails> toRemove = new ArrayList<>();
        for (DisxJukeboxUsageCooldownDetails details : COOLDOWNS){
            details.incrementCounter();
            if (details.getCooldownCounter() > details.getCooldownTime()){
                toRemove.add(details);
            }
        }
        for (DisxJukeboxUsageCooldownDetails details : toRemove){
            COOLDOWNS.remove(details);
        }
    }

    public static boolean isOnCooldown(BlockPos pos, ResourceKey<Level> dimension) {
        for (DisxJukeboxUsageCooldownDetails details : COOLDOWNS){
            if (details.getBlockPos().equals(pos) && details.getDimension().equals(dimension.location())){
                return true;
            }
        }
        return false;
    }
}
