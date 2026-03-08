package xyz.ar06.disx;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.blocks.DisxStampMaker;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.renderer.RenderType;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

public class DisxClientMainQuilt {
    public static void initializeClient(){
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            BlockRenderLayerMap.put(RenderType.translucent(), DisxLacquerBlock.blockRegistration.get());
            BlockRenderLayerMap.put(RenderType.cutout(), DisxStampMaker.blockRegistration.get());
            ClientSendMessageEvents.ALLOW_CHAT.register(DisxBehaviorHandlingQuilt::chatListener);
        }
    }
}
