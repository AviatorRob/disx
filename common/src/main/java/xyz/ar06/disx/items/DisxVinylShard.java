package xyz.ar06.disx.items;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class DisxVinylShard extends Item {

    public DisxVinylShard(Properties properties) {
        super(properties);
    }

    public static void registerItem(Registrar<Item> itemRegistry, RegistrySupplier<CreativeModeTab> tab){
        RegistrySupplier<Item> newItem = itemRegistry.register(new ResourceLocation("disx", "vinyl_shard"), () -> new DisxBlankDisc(new Item.Properties().stacksTo(64).arch$tab(tab)));

    }
}
