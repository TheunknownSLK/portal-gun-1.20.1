package me.ichun.mods.portalgunclassic.client.core;

import me.ichun.mods.portalgunclassic.client.render.RenderPortalProjectile;
import me.ichun.mods.portalgunclassic.client.render.TileRendererPortal;
import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.item.ItemPortalGun;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Mod bus, client only: what ProxyClient#preInit did in the original. */
@Mod.EventBusSubscriber(modid = PortalGunClassic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents
{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        //Original: damage 0 used the model pg_blue and damage 1 pg_orange. Now picked by the "orange" item property (see models/item/portalgun.json).
        event.enqueueWork(() -> ItemProperties.register(PortalGunClassic.ITEM_PORTAL_GUN.get(), new ResourceLocation(PortalGunClassic.MOD_ID, "orange"), (stack, level, entity, seed) -> ItemPortalGun.isOrange(stack) ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(EventHandlerClient.keySwitch);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(PortalGunClassic.TILE_PORTAL.get(), TileRendererPortal::new);
        event.registerEntityRenderer(PortalGunClassic.ENTITY_PORTAL_PROJECTILE.get(), RenderPortalProjectile::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll("portalgun_overlay", new OverlayPortalGun());
    }
}
