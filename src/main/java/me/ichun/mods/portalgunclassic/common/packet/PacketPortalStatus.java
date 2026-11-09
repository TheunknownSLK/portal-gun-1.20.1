package me.ichun.mods.portalgunclassic.common.packet;

import me.ichun.mods.portalgunclassic.client.core.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server to client: which portals exist in the player's dimension (drives the HUD overlay and the client side teleport check). */
public class PacketPortalStatus
{
    public boolean blue;
    public boolean orange;

    public PacketPortalStatus()
    {}

    public PacketPortalStatus(boolean blue, boolean orange)
    {
        this.blue = blue;
        this.orange = orange;
    }

    public static void encode(PacketPortalStatus message, FriendlyByteBuf buf)
    {
        buf.writeBoolean(message.blue);
        buf.writeBoolean(message.orange);
    }

    public static PacketPortalStatus decode(FriendlyByteBuf buf)
    {
        PacketPortalStatus message = new PacketPortalStatus();
        message.blue = buf.readBoolean();
        message.orange = buf.readBoolean();
        return message;
    }

    public static void handle(PacketPortalStatus message, Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePortalStatus(message)));
        context.setPacketHandled(true);
    }
}
