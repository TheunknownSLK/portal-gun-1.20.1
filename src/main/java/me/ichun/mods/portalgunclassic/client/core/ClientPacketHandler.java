package me.ichun.mods.portalgunclassic.client.core;

import me.ichun.mods.portalgunclassic.client.portal.PortalStatus;
import me.ichun.mods.portalgunclassic.common.packet.PacketEntityLocation;
import me.ichun.mods.portalgunclassic.common.packet.PacketPortalStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Client side handling of the server to client packets. Only ever called on the logical client, on the main thread. */
public class ClientPacketHandler
{
    public static void handlePortalStatus(PacketPortalStatus message)
    {
        EventHandlerClient.status = new PortalStatus(message.blue, message.orange);
    }

    public static void handleEntityLocation(PacketEntityLocation message)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level == null)
        {
            return;
        }
        Entity ent = mc.level.getEntity(message.id);
        if(ent != null)
        {
            ent.setPos(message.posX, message.posY, message.posZ);
            ent.xOld = message.ltX;
            ent.yOld = message.ltY;
            ent.zOld = message.ltZ;
            ent.xo = message.prevX;
            ent.yo = message.prevY;
            ent.zo = message.prevZ;
            ent.setDeltaMovement(message.mX, message.mY, message.mZ);
            ent.yRotO = message.prevYaw;
            ent.xRotO = message.prevPitch;
            ent.setYRot(message.yaw);
            ent.setXRot(message.pitch);

            if(ent == mc.player)
            {
                EventHandlerClient.justTeleported = true;
                EventHandlerClient.mX = message.mX;
                EventHandlerClient.mY = message.mY;
                EventHandlerClient.mZ = message.mZ;
            }
        }
    }
}
