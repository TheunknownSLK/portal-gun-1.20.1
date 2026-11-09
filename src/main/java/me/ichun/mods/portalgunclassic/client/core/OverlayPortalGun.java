package me.ichun.mods.portalgunclassic.client.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;

/** The crosshair overlay shown while holding a portal gun (blue on the left, orange on the right; full when that portal is open). */
public class OverlayPortalGun implements IGuiOverlay
{
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen == null && !mc.options.hideGui && EventHandlerClient.isHoldingPortalGun(mc))
        {
            //is holding a portal gun

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            double size = 40;
            double x1 = screenWidth / 2D - size + 1;
            double x2 = screenWidth / 2D + size + 1;
            double y1 = screenHeight / 2D - size + 1;
            double y2 = screenHeight / 2D + size + 1;

            draw(guiGraphics, EventHandlerClient.status != null && EventHandlerClient.status.blue ? EventHandlerClient.txLFull : EventHandlerClient.txLEmpty, x1, y1, x2, y2, 5, 130, 255);
            draw(guiGraphics, EventHandlerClient.status != null && EventHandlerClient.status.orange ? EventHandlerClient.txRFull : EventHandlerClient.txREmpty, x1, y1, x2, y2, 255, 176, 6);
        }
    }

    private static void draw(GuiGraphics guiGraphics, ResourceLocation texture, double x1, double y1, double x2, double y2, int r, int g, int b)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);

        Matrix4f matrix = guiGraphics.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        //Same corners and texture coordinates as the original (top left = 0,0), listed in the order vanilla GUI quads use.
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).uv(0F, 0F).color(r, g, b, 255).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).uv(0F, 1F).color(r, g, b, 255).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).uv(1F, 1F).color(r, g, b, 255).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).uv(1F, 0F).color(r, g, b, 255).endVertex();
        tessellator.end();
    }
}
