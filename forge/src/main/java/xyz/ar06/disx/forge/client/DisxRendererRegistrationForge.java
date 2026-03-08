package xyz.ar06.disx.forge.client;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.client_only.renderers.DisxAdvancedJukeboxMinecartRenderer;
import xyz.ar06.disx.entities.vehicle.DisxAdvancedJukeboxMinecart;

@Mod.EventBusSubscriber(modid = "disx", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DisxRendererRegistrationForge {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityType<?> registration = DisxAdvancedJukeboxMinecart.entityTypeRegistration.get();
        if (registration == null){
            DisxLogger.error("Entity type registration is null for the aj minecart!");
        }
        event.registerEntityRenderer(
                DisxAdvancedJukeboxMinecart.entityTypeRegistration.get(),
                (arg) -> new DisxAdvancedJukeboxMinecartRenderer(arg, ModelLayers.COMMAND_BLOCK_MINECART));
    }
}
