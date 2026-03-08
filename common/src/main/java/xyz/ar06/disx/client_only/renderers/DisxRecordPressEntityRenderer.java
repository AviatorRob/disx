package xyz.ar06.disx.client_only.renderers;

import xyz.ar06.disx.entities.DisxRecordPressEntity;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.recipe_types.DisxCustomDiscRecipe;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DisxRecordPressEntityRenderer implements BlockEntityRenderer<DisxRecordPressEntity> {

    public DisxRecordPressEntityRenderer(BlockEntityRendererProvider.Context context){

    }
    private float rotation_counter0 = 0f;
    private float rotation_counter1= 90f;
    @Override
    public void render(DisxRecordPressEntity blockEntity, float f, PoseStack poseStack,
                       MultiBufferSource multiBufferSource, int i, int j) {
        ItemStack blank_disc = blockEntity.getItem(0);
        ItemStack record_stamp = blockEntity.getItem(1);
        ItemStack disc_factor = blockEntity.getItem(2);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack discRenderStack = getDiscRenderStack(blockEntity);
        if (!blank_disc.isEmpty()){
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.2f, 0.5f);
            poseStack.mulPose(Axis.YN.rotationDegrees(rotation_counter0));
            poseStack.scale(0.25f,0.25f,0.25f);
            itemRenderer.renderStatic(discRenderStack, ItemDisplayContext.FIXED, getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, blockEntity.getLevel(), 1);
            poseStack.popPose();
        }
        if (!record_stamp.isEmpty()){
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.4f, 0.5f);
            poseStack.mulPose(Axis.YN.rotationDegrees(rotation_counter1));
            poseStack.scale(0.25f,0.25f,0.25f);
            itemRenderer.renderStatic(record_stamp, ItemDisplayContext.FIXED, getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, blockEntity.getLevel(), 1);
            poseStack.popPose();
        }
        rotation_counter0 += 0.5f;
        if (rotation_counter0 == 360f){
            rotation_counter0 = 0f;
        }
        rotation_counter1 += 0.5f;
        if (rotation_counter1 == 360f){
            rotation_counter1 = 0f;
        }
    }

    private ItemStack getDiscRenderStack(DisxRecordPressEntity blockEntity){
        Level level = blockEntity.getLevel();
        RecipeManager recipeManager = level.getRecipeManager();
        Optional<DisxCustomDiscRecipe> recipe = recipeManager.getRecipeFor(DisxCustomDiscRecipe.DisxCustomDiscRecipeType.INSTANCE, blockEntity, level);
        if (!recipe.isEmpty()){
            ItemStack stack = recipe.get().getResultItem();
            return stack;
        } else {
            ItemStack blank_disc = blockEntity.getItem(0);
            return blank_disc;
        }
    }

    private int getLightLevel(Level level, BlockPos pos){
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
