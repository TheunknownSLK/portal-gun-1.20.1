package me.ichun.mods.portalgunclassic.common.packet;

import me.ichun.mods.portalgunclassic.common.core.EventHandlerServer;
import me.ichun.mods.portalgunclassic.common.portal.PortalInfo;
import me.ichun.mods.portalgunclassic.common.tileentity.TileEntityPortal;
import me.ichun.mods.portalgunclassic.common.world.PortalSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Client to server: "I am going through the portal at this position".
 * The original trusted the position and did the teleport on the network thread.
 * Here the work runs on the server thread and the request is validated first (see isLegitimate).
 */
public class PacketRequestTeleport
{
    //The client decides it is inside the portal using its own (slightly newer) position and motion.
    //Allow for that lag: a base margin plus a margin that grows with the player's speed, capped.
    public static final double BASE_TOLERANCE = 2.0D;
    public static final double SPEED_TOLERANCE = 4.0D; //per block/tick of speed
    public static final double MAX_TOLERANCE = 8.0D;

    public BlockPos pos;

    public PacketRequestTeleport()
    {}

    public PacketRequestTeleport(BlockPos pos)
    {
        this.pos = pos;
    }

    public static void encode(PacketRequestTeleport message, FriendlyByteBuf buf)
    {
        buf.writeLong(message.pos.asLong());
    }

    public static PacketRequestTeleport decode(FriendlyByteBuf buf)
    {
        PacketRequestTeleport message = new PacketRequestTeleport();
        message.pos = BlockPos.of(buf.readLong());
        return message;
    }

    public static void handle(PacketRequestTeleport message, Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> //runs on the server thread
        {
            ServerPlayer player = context.getSender();
            if(player == null)
            {
                return;
            }
            ServerLevel level = player.serverLevel();
            if(!level.isLoaded(message.pos))
            {
                return;
            }

            BlockEntity te = level.getBlockEntity(message.pos);
            if(te instanceof TileEntityPortal portalCurrent && portalCurrent.setup && !portalCurrent.top)
            {
                PortalSavedData data = EventHandlerServer.getSaveData(level);
                HashMap<String, PortalInfo> map = data.portalInfo.get(PortalSavedData.dimKey(level));
                if(map != null)
                {
                    PortalInfo own = map.get(portalCurrent.orange ? "orange" : "blue");
                    PortalInfo info = map.get(portalCurrent.orange ? "blue" : "orange");
                    //The portal asked for must be the registered portal of its colour, and the player must really be at it.
                    if(own != null && own.pos.equals(message.pos) && info != null && level.isLoaded(info.pos) && isLegitimate(player, portalCurrent))
                    {
                        if(level.getBlockEntity(info.pos) instanceof TileEntityPortal portalDest)
                        {
                            //There is a pair! We can teleport!
                            portalCurrent.teleport(player, portalDest);

                            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    /** The player's body must be at (or about to enter) the slab behind the portal plane, allowing for network lag. */
    public static boolean isLegitimate(ServerPlayer player, TileEntityPortal portal)
    {
        Vec3 pos = player.position();
        double speed = Math.sqrt((pos.x - player.xo) * (pos.x - player.xo) + (pos.y - player.yo) * (pos.y - player.yo) + (pos.z - player.zo) * (pos.z - player.zo));
        double tolerance = Math.min(MAX_TOLERANCE, BASE_TOLERANCE + SPEED_TOLERANCE * speed);
        return player.getBoundingBox().inflate(tolerance).intersects(portal.getInsideBox());
    }
}
