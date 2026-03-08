package xyz.ar06.disx.client_only.gui.components;

import xyz.ar06.disx.client_only.DisxClientPacketIndex;
import xyz.ar06.disx.client_only.gui.screens.DisxStampMakerGUI;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DisxCheckButton extends AbstractButton {

    int xCoord;
    int yCoord;
    int width;
    int height;

    public DisxCheckButton(int x, int y, int width, int height, Component component) {
        super(x, y, width, height, component);
        this.xCoord = x;
        this.yCoord = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onPress() {
        if(Platform.getEnvironment().equals(Env.CLIENT)){
            Minecraft.getInstance().setScreen(null);
            DisxClientPacketIndex.ClientPackets.pushVideoId(DisxStampMakerGUI.textBoxValue, DisxStampMakerGUI.pos);
            DisxStampMakerGUI.textBoxValue = null;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        super.render(graphics, i, j, f);
        if (!isHovered()){
            graphics.blit(
                    new ResourceLocation("disx","textures/gui/video_id_box.png"),
                    ((graphics.guiWidth() - 257) / 2) + 238,
                    ((graphics.guiHeight() - 39) / 2) + 11,
                    49,
                    48,
                    20,
                    20
            );
        } else {
            graphics.blit(
                    new ResourceLocation("disx","textures/gui/video_id_box.png"),
                    ((graphics.guiWidth() - 257) / 2) + 238,
                    ((graphics.guiHeight() - 39) / 2) + 11,
                    49,
                    68,
                    20,
                    20
            );
        }

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void mouseMoved(double d, double e) {
        super.mouseMoved(d, e);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        return super.keyReleased(i, j, k);
    }

    @Override
    public boolean charTyped(char c, int i) {
        return super.charTyped(c, i);
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return super.getCurrentFocusPath();
    }

    @Override
    public void setPosition(int i, int j) {
        super.setPosition(i, j);
    }

}
