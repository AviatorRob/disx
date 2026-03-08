package xyz.ar06.disx.client_only.gui.screens;

import xyz.ar06.disx.client_only.gui.components.DisxCheckButton;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.architectury.injectables.annotations.PlatformOnly;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class DisxStampMakerGUI extends Screen {

    GuiGraphics guiGraphics;

    boolean renderedOnce = false;
    boolean mouseOverCheck = false;
    EditBox textBox;

    public static String textBoxValue = null;

    public static BlockPos pos;


    public static void setScreen(BlockPos blockPos){
        if (Platform.getEnv().equals(EnvType.CLIENT)){
            Minecraft.getInstance().setScreen(new DisxStampMakerGUI(Component.literal("Video Id Box")));
            pos = blockPos;
        }
    }
    Logger logger = LoggerFactory.getLogger("disx");

    public DisxStampMakerGUI(Component component) {
        super(component);
    }


    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }


    @Override
    protected void init() {
        super.init();

    }

    private void buildButton(GuiGraphics graphics){
        DisxCheckButton checkButton = new DisxCheckButton(
                ((graphics.guiWidth() - 257) / 2) + 238,
                ((graphics.guiHeight() - 39) / 2) + 11,
                20,
                20,
                Component.empty());
        addRenderableWidget(checkButton);
    }

    private void buildTextBox(GuiGraphics graphics){
        if (this.textBox != null){
            textBoxValue = this.textBox.getValue();
        }
        this.textBox = new EditBox(
                Minecraft.getInstance().font,
                ((graphics.guiWidth() - 257) / 2) + 9,
                ((graphics.guiHeight() - 39) / 2) + 14,
                220,
                18,
                Component.empty()
        );
        if (this.textBoxValue != null){
            this.textBox.insertText(textBoxValue);
        }
        addRenderableWidget(this.textBox);
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        super.render(graphics, i, j, f);
        this.guiGraphics = graphics;
        if (!renderedOnce) {
            buildButton(graphics);
            buildTextBox(graphics);
            renderedOnce = true;
        }
        graphics.blit(
                new ResourceLocation("disx","textures/gui/video_id_box.png"),
                (graphics.guiWidth() - 257) / 2,
                (graphics.guiHeight() - 39) / 2,
                1,
                6,
                236,
                39
        );
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Enter Video Id").withStyle(ChatFormatting.WHITE),
                ((graphics.guiWidth() - 257) / 2) + 8,
                ((graphics.guiHeight() - 39) / 2) + 3,
                3
        );
        textBoxValue = this.textBox.getValue();
    }

    @Override
    public void mouseMoved(double d, double e) {
        super.mouseMoved(d, e);
        if (guiGraphics != null){
            int minX = ((guiGraphics.guiWidth() - 257) / 2) + 238;
            int minY = ((guiGraphics.guiHeight() - 39) / 2) + 11;
            int maxX = minX + 20;
            int maxY = minY + 20;
            if (d >= minX && d <= maxX) {
                if (e >= minY && d <= maxY){
                    mouseOverCheck = true;
                } else {
                    mouseOverCheck = false;
                }
            } else {
                mouseOverCheck = false;
            }
        }
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        rebuildWidgets();
        clearWidgets();
        buildButton(this.guiGraphics);
        buildTextBox(this.guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
