package xyz.ar06.disx.enchantments;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.audio_filters.DisxAudioFilterType;

import java.util.ArrayList;

public class DisxDiscEnchantmentHelper {
    public static ArrayList<DisxAudioFilterType> buildAudioFilterArray(ItemStack itemStack){
        ArrayList<DisxAudioFilterType> audioFilters = new ArrayList<DisxAudioFilterType>();
        if (EnchantmentHelper.getItemEnchantmentLevel(DisxRetrogradeCurseEnchantment.enchantmentRegistration.get(), itemStack) > 0){
            audioFilters.add(DisxAudioFilterType.REVERSE);
            DisxLogger.debug("Detected REVERSE curse on disc, adding to audioFilter array");
        }
        int tempoEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(DisxTempoEnchantment.enchantmentRegistration.get(), itemStack);
        if (tempoEnchantmentLevel > 0){
            DisxLogger.debug("Detected TEMPO curse on disc, adding to audioFilter array");
            switch (tempoEnchantmentLevel){
                case 1 -> audioFilters.add(DisxAudioFilterType.TEMPO_1);
                case 2 -> audioFilters.add(DisxAudioFilterType.TEMPO_2);
                case 3 -> audioFilters.add(DisxAudioFilterType.TEMPO_3);
            };
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(DisxResonanceEnchantment.enchantmentRegistration.get(), itemStack) > 0){
            audioFilters.add(DisxAudioFilterType.ECHO);
            DisxLogger.debug("Detected ECHO curse on disc, adding to audioFilter array");
        }
        return audioFilters;
    }
}
