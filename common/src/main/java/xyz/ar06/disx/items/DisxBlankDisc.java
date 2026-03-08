package xyz.ar06.disx.items;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class DisxBlankDisc extends Item{

    public static RegistrySupplier<Item> itemRegistration;
    public DisxBlankDisc(Properties properties) {
        super(properties);
    }

    public static void registerItem(Registrar<Item> itemRegistry, RegistrySupplier<CreativeModeTab> tab){
        itemRegistration = itemRegistry.register(new ResourceLocation("disx", "blank_disc"), () -> new DisxBlankDisc(new Item.Properties().stacksTo(64).arch$tab(tab)));
    }
}
