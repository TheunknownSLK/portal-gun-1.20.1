package me.ichun.mods.portalgunclassic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.ichun.mods.portalgunclassic.common.tileentity.TileEntityPortal;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TileRendererPortal implements BlockEntityRenderer<TileEntityPortal>
{
    public static final ResourceLocation txBlueBtm = new ResourceLocation("portalgunclassic", "textures/block/blue_bottom.png");
    public static final ResourceLocation txBlueTop = new ResourceLocation("portalgunclassic", "textures/block/blue_top.png");
    public static final ResourceLocation txBlueY = new ResourceLocation("portalgunclassic", "textures/block/blue_floor.png");
    public static final ResourceLocation txOrangeBtm = new ResourceLocation("portalgunclassic", "textures/block/orange_bottom.png");
    public static final ResourceLocation txOrangeTop = new ResourceLocation("portalgunclassic", "textures/block/orange_top.png");
    public static final ResourceLocation txOrangeY = new ResourceLocation("portalgunclassic", "textures/block/orange_floor.png");

    public TileRendererPortal(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(TileEntityPortal te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        if(te.setup)
        {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);

            poseStack.mulPose(Axis.ZP.rotationDegrees(te.face.getStepY() * 90F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-te.face.toYRot()));
            poseStack.translate(0D, 0D, -0.495D);

            ResourceLocation texture = te.face.getAxis() == Direction.Axis.Y ? (te.orange ? txOrangeY : txBlueY) : te.top ? (te.orange ? txOrangeTop : txBlueTop) : te.orange ? txOrangeBtm : txBlueBtm;

            //Original: alpha blended quad, lighting disabled (always fully lit).
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(texture));
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            Matrix3f normal = pose.normal();

            vertex(consumer, matrix, normal, -0.5F, -0.5F, 0F, 0F, 1F);
            vertex(consumer, matrix, normal, 0.5F, -0.5F, 0F, 1F, 1F);
            vertex(consumer, matrix, normal, 0.5F, 0.5F, 0F, 1F, 0F);
            vertex(consumer, matrix, normal, -0.5F, 0.5F, 0F, 0F, 0F);

            poseStack.popPose();
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float u, float v)
    {
        consumer.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0F, 0F, 1F).endVertex();
    }
}
