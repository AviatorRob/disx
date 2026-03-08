package xyz.ar06.disx.fabric;

import dev.architectury.event.events.client.ClientChatEvent;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.utils.Env;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.blocks.DisxStampMaker;

import java.util.logging.Logger;

public class DisxClientMainFabric {
    public static void onInitializeClient() {
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            BlockRenderLayerMap.INSTANCE.putBlock(DisxLacquerBlock.blockRegistration.get(), RenderType.translucent());
            BlockRenderLayerMap.INSTANCE.putBlock(DisxStampMaker.blockRegistration.get(), RenderType.cutout());
            ClientSendMessageEvents.ALLOW_CHAT.register(DisxBehaviorHandlingFabric::chatListener);
        }
    }
}
