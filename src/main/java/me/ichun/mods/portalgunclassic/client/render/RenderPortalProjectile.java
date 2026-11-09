package me.ichun.mods.portalgunclassic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.ichun.mods.portalgunclassic.common.entity.EntityPortalProjectile;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderPortalProjectile extends EntityRenderer<EntityPortalProjectile>
{
    public static final ResourceLocation txBlue = new ResourceLocation("portalgunclassic", "textures/entity/portalball_blue.png");
    public static final ResourceLocation txOrange = new ResourceLocation("portalgunclassic", "textures/entity/portalball_orange.png");

    public RenderPortalProjectile(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(EntityPortalProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        if(entity.age < 1)
        {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.15D, 0.0D);
        //Original: rotate by -playerViewY / playerViewX (flipped in front view) then 180 degrees about Y. That is the camera facing billboard below.
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        float f = 0F;
        float f1 = 1F;
        float f2 = 0F;
        float f3 = 1F;

        vertex(consumer, matrix, normal, -0.5F, -0.5F, f, f3);
        vertex(consumer, matrix, normal, 0.5F, -0.5F, f1, f3);
        vertex(consumer, matrix, normal, 0.5F, 0.5F, f1, f2);
        vertex(consumer, matrix, normal, -0.5F, 0.5F, f, f2);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float u, float v)
    {
        //Original: getBrightnessForRender() = 15728880 (always fully lit).
        consumer.vertex(matrix, x, y, 0F).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0F, 1F, 0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPortalProjectile entity)
    {
        return entity.isOrange() ? txOrange : txBlue;
    }
}
