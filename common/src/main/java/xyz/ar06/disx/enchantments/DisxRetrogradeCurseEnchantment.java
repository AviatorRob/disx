package xyz.ar06.disx.enchantments;

import dev.architectury.event.EventFactory;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.items.DisxCustomDisc;

public class DisxRetrogradeCurseEnchantment extends Enchantment {
    public static RegistrySupplier<Enchantment> enchantmentRegistration;

    protected DisxRetrogradeCurseEnchantment(Rarity rarity, EnchantmentCategory enchantmentCategory, EquipmentSlot[] equipmentSlots) {
        super(rarity, enchantmentCategory, equipmentSlots);
    }


    @Override
    public int getMinCost(int i) {
        return 5;
    }

    @Override
    public int getMaxCost(int i) {
        return 20;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return (itemStack.getItem() instanceof DisxCustomDisc);
    }

    public static void registerEnchantment(Registrar<Enchantment> enchantmentRegistrar, RegistrySupplier<CreativeModeTab> creativeModeTab){
        enchantmentRegistration = enchantmentRegistrar.register(
                new ResourceLocation("disx","retrograde_curse"),
                () -> new DisxRetrogradeCurseEnchantment(Rarity.COMMON, EnchantmentCategory.FISHING_ROD, new EquipmentSlot[]{})
        );
        /*CreativeTabRegistry.appendBuiltinStack(
                creativeModeTab.get(),
                EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(enchantmentRegistration.get(), 1))
        );*/
    }


}
