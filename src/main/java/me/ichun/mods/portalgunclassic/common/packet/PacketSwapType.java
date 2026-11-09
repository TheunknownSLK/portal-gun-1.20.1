package me.ichun.mods.portalgunclassic.common.packet;

import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.core.EventHandlerServer;
import me.ichun.mods.portalgunclassic.common.item.ItemPortalGun;
import me.ichun.mods.portalgunclassic.common.sounds.SoundRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client to server: G (switch the colour of the held gun) and R / Shift+R (reset portals). */
public class PacketSwapType
{
    public boolean reset;
    public int type;

    public PacketSwapType()
    {}

    public PacketSwapType(boolean reset, int type)
    {
        this.reset = reset;
        this.type = type;
    }

    public static void encode(PacketSwapType message, FriendlyByteBuf buf)
    {
        buf.writeBoolean(message.reset);
        buf.writeInt(message.type);
    }

    public static PacketSwapType decode(FriendlyByteBuf buf)
    {
        PacketSwapType message = new PacketSwapType();
        message.reset = buf.readBoolean();
        message.type = buf.readInt();
        return message;
    }

    public static void handle(PacketSwapType message, Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> //runs on the server thread
        {
            ServerPlayer player = context.getSender();
            if(player == null || !holdsPortalGun(player)) //the client only sends this while holding a portal gun
            {
                return;
            }
            Level level = player.level();
            if(!message.reset)
            {
                for(InteractionHand hand : InteractionHand.values())
                {
                    ItemStack is = player.getItemInHand(hand);
                    if(is.getItem() == PortalGunClassic.ITEM_PORTAL_GUN.get())
                    {
                        ItemPortalGun.setOrange(is, !ItemPortalGun.isOrange(is));
                    }
                }
            }
            else
            {
                if(message.type == 0)
                {
                    EventHandlerServer.getSaveData(level).kill(level, false);
                    EventHandlerServer.getSaveData(level).kill(level, true);
                }
                else
                {
                    for(InteractionHand hand : InteractionHand.values())
                    {
                        ItemStack is = player.getItemInHand(hand);
                        if(is.getItem() == PortalGunClassic.ITEM_PORTAL_GUN.get())
                        {
                            EventHandlerServer.getSaveData(level).kill(level, ItemPortalGun.isOrange(is));
                        }
                    }
                }
                level.playSound(null, player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), SoundRegistry.reset.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
            }
        });
        context.setPacketHandled(true);
    }

    private static boolean holdsPortalGun(ServerPlayer player)
    {
        for(InteractionHand hand : InteractionHand.values())
        {
            if(player.getItemInHand(hand).getItem() == PortalGunClassic.ITEM_PORTAL_GUN.get())
            {
                return true;
            }
        }
        return false;
    }
}
