package me.ichun.mods.portalgunclassic.client.core;

import me.ichun.mods.portalgunclassic.client.portal.PortalStatus;
import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.packet.PacketRequestTeleport;
import me.ichun.mods.portalgunclassic.common.tileentity.TileEntityPortal;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/** Client side parts of TileEntityPortal. Only ever called on the logical client. */
public class ClientPortalHelper
{
    /** Same condition as the original: an orange portal needs the blue one to exist and the other way around. (No status yet counts as no pair.) */
    public static boolean hasPair(boolean orange)
    {
        PortalStatus status = EventHandlerClient.status;
        if(status == null)
        {
            return false;
        }
        return !(orange && !status.blue || !orange && !status.orange);
    }

    public static void handleClientTeleport(TileEntityPortal portal, Player player)
    {
        if(EventHandlerClient.teleportCooldown <= 0 && player == Minecraft.getInstance().player)
        {
            EventHandlerClient.teleportCooldown = 3;
            PortalGunClassic.channel.sendToServer(new PacketRequestTeleport(portal.getBlockPos()));
        }
    }
}
