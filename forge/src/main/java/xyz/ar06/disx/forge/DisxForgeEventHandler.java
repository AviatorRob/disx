package xyz.ar06.disx.forge;

import dev.architectury.registry.fuel.FuelRegistry;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.items.DisxLacquerDrop;

@Mod(DisxMain.MOD_ID)
public class DisxForgeEventHandler {
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Item lacquerItem = DisxLacquerDrop.itemRegistration.get();
            FuelRegistry.register(100, lacquerItem);
            Item lacquerBlockItem = DisxLacquerBlock.itemRegistration.get();
            FuelRegistry.register(900, lacquerBlockItem);
        });
    }
}
