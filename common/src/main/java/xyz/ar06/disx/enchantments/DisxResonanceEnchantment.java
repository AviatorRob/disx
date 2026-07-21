package xyz.ar06.disx.enchantments;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class DisxResonanceEnchantment extends Enchantment {
    public static RegistrySupplier<Enchantment> enchantmentRegistration;

    protected DisxResonanceEnchantment(Rarity rarity, EnchantmentCategory enchantmentCategory, EquipmentSlot[] equipmentSlots) {
        super(rarity, enchantmentCategory, equipmentSlots);
    }

    public static void registerEnchantment(Registrar<Enchantment> enchantmentRegistrar, RegistrySupplier<CreativeModeTab> tab){
        enchantmentRegistration = enchantmentRegistrar.register(
                new ResourceLocation("disx","resonance_curse"),
                () -> new DisxResonanceEnchantment(Rarity.UNCOMMON, EnchantmentCategory.FISHING_ROD, new EquipmentSlot[]{})
        );
        CreativeTabRegistry.appendBuiltinStack(
                tab.get(),
                EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantmentRegistration.get(), 1))
        );
    }
}
