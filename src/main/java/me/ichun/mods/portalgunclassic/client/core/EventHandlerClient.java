package me.ichun.mods.portalgunclassic.client.core;

import me.ichun.mods.portalgunclassic.client.portal.PortalStatus;
import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.packet.PacketSwapType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PortalGunClassic.MOD_ID, value = Dist.CLIENT)
public class EventHandlerClient
{
    public static final ResourceLocation txLEmpty = new ResourceLocation("portalgunclassic", "textures/overlay/lempty.png");
    public static final ResourceLocation txLFull = new ResourceLocation("portalgunclassic", "textures/overlay/lfull.png");
    public static final ResourceLocation txREmpty = new ResourceLocation("portalgunclassic", "textures/overlay/rempty.png");
    public static final ResourceLocation txRFull = new ResourceLocation("portalgunclassic", "textures/overlay/rfull.png");

    //G switches the colour of the gun. Registered in the controls screen, like the original.
    public static final KeyMapping keySwitch = new KeyMapping("key.portalgunclassic.switch", GLFW.GLFW_KEY_G, "key.categories.portalgun");
    //R resets the portals (Shift+R: only the colour of the held gun). The original did NOT register this key in the
    //controls screen (fixed to R), so it is not registered here either.
    public static final KeyMapping keyReset = new KeyMapping("key.portalgunclassic.reset", GLFW.GLFW_KEY_R, "key.categories.portalgun");

    public static boolean keySwitchDown = false;
    public static boolean keyResetDown = false;

    public static PortalStatus status = null;
    public static int teleportCooldown = 0;

    public static boolean justTeleported = false;
    public static double mX = 0D;
    public static double mY = 0D;
    public static double mZ = 0D;

    public static boolean isHoldingPortalGun(Minecraft mc)
    {
        return mc.player != null && (mc.player.getMainHandItem().getItem() == PortalGunClassic.ITEM_PORTAL_GUN.get() || mc.player.getOffhandItem().getItem() == PortalGunClassic.ITEM_PORTAL_GUN.get());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.END)
        {
            if(isHoldingPortalGun(mc))
            {
                if(!keySwitchDown && keySwitch.isDown())
                {
                    PortalGunClassic.channel.sendToServer(new PacketSwapType(false, 0));
                }
                if(!keyResetDown && keyReset.isDown())
                {
                    PortalGunClassic.channel.sendToServer(new PacketSwapType(true, Screen.hasShiftDown() ? 1 : 0));
                }
                keySwitchDown = keySwitch.isDown();
                keyResetDown = keyReset.isDown();
            }
        }
        else
        {
            if(teleportCooldown > 0 && !mc.isPaused())
            {
                teleportCooldown--;
            }
            if(justTeleported)
            {
                if(mc.player != null)
                {
                    Vec3 motion = mc.player.getDeltaMovement();
                    if(motion.x == 0.0D && motion.y == 0.0D && motion.z == 0.0D)
                    {
                        justTeleported = false;
                        mc.player.setDeltaMovement(mX, mY, mZ);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onConnectToServerEvent(ClientPlayerNetworkEvent.LoggingIn event)
    {
        status = null;
        justTeleported = false;
    }
}
