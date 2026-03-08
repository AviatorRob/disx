package xyz.ar06.disx.client_only.renderers;

import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.entities.DisxStampMakerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

@Environment(EnvType.CLIENT)
public class DisxStampMakerEntityRenderer implements BlockEntityRenderer<DisxStampMakerEntity> {

    public float rotation_counter = 0f;
    public float translation_counter = 0.1f;
    public boolean translation_Up = true;
    public final boolean translation_enabled = false;
    public DisxStampMakerEntityRenderer(BlockEntityRendererProvider.Context context){

    }
    @Override
    public void render(DisxStampMakerEntity blockEntity, float f, PoseStack poseStack,
                       MultiBufferSource multiBufferSource, int i, int j) {
        if (!blockEntity.getItem(0).isEmpty()){
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            ItemStack stack = new ItemStack(blockEntity.getItem(0).getItem());
            poseStack.pushPose();
            poseStack.translate(0.5f, translation_counter, 0.5f);
            poseStack.mulPose(Axis.YN.rotationDegrees(rotation_counter));
            rotation_counter += 0.5f;
            if (rotation_counter == 360){
                rotation_counter = 0;
            }
            if (translation_Up && translation_enabled){
                translation_counter += 0.0025f;
            } else if (translation_enabled){
                translation_counter -= 0.0025f;
            }
            if (translation_counter > 0.35f){
                translation_counter = 0.35f;
                translation_Up = false;
            }
            if (translation_counter < 0.2f){
                translation_counter = 0.2f;
                translation_Up = true;
            }
            poseStack.scale(0.35f,0.35f,0.35f);
            renderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, blockEntity.getLevel(), 1);
            poseStack.popPose();
        }
    }

    private int getLightLevel(Level level, BlockPos pos){
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
