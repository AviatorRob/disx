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
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import xyz.ar06.disx.items.DisxCustomDisc;

public class DisxTempoEnchantment extends Enchantment {
    public static RegistrySupplier<Enchantment> enchantmentRegistration;

    protected DisxTempoEnchantment(Rarity rarity, EnchantmentCategory enchantmentCategory, EquipmentSlot[] equipmentSlots) {
        super(rarity, enchantmentCategory, equipmentSlots);
    }

    public static void registerEnchantment(Registrar<Enchantment> enchantmentRegistrar, RegistrySupplier<CreativeModeTab> creativeModeTab, Registrar<Item> itemsRegistrar){
        enchantmentRegistration = enchantmentRegistrar.register(new ResourceLocation("disx","tempo_curse"),
                () -> new DisxTempoEnchantment(Rarity.UNCOMMON, EnchantmentCategory.FISHING_ROD, new EquipmentSlot[]{})
        );
        CreativeTabRegistry.appendBuiltinStack(
                creativeModeTab.get(),
                EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(enchantmentRegistration.get(), 1))
        );
        CreativeTabRegistry.appendBuiltinStack(
                creativeModeTab.get(),
                EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(enchantmentRegistration.get(), 2))
        );
        CreativeTabRegistry.appendBuiltinStack(
                creativeModeTab.get(),
                EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(enchantmentRegistration.get(), 3))
        );
    }

    @Override
    public int getMinCost(int i) {
        return 10 + (i - 1) * 15;
    }

    @Override
    public int getMaxCost(int i) {
        return getMinCost(i) + 5;
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return (itemStack.getItem() instanceof DisxCustomDisc);
    }

    @Override
    public boolean isCurse() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }
}
