package xyz.ar06.disx.forge.client;

import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.blocks.DisxStampMaker;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class DisxClientForge {
    public static void onClientInitialize(final FMLCommonSetupEvent event){
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            ItemBlockRenderTypes.setRenderLayer(DisxLacquerBlock.blockRegistration.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(DisxStampMaker.blockRegistration.get(), RenderType.cutout());
            MinecraftForge.EVENT_BUS.register(new DisxBehaviorHandlingForge());
        }
    }
}
