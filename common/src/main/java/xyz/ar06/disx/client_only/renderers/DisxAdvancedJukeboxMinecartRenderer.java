package xyz.ar06.disx.client_only.renderers;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import xyz.ar06.disx.entities.vehicle.DisxAdvancedJukeboxMinecart;

public class DisxAdvancedJukeboxMinecartRenderer extends MinecartRenderer {
    public DisxAdvancedJukeboxMinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, modelLayerLocation);
    }
}
