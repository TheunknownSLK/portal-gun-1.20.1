package me.ichun.mods.portalgunclassic.common.packet;

import me.ichun.mods.portalgunclassic.client.core.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server to clients near a portal: the exact position, previous position, motion and rotation of an entity that just went through. */
public class PacketEntityLocation
{
    public int id;
    public double ltX;
    public double ltY;
    public double ltZ;
    public double prevX;
    public double prevY;
    public double prevZ;
    public double posX;
    public double posY;
    public double posZ;
    public double mX;
    public double mY;
    public double mZ;
    public float prevYaw;
    public float prevPitch;
    public float yaw;
    public float pitch;

    public PacketEntityLocation()
    {}

    public PacketEntityLocation(Entity ent)
    {
        Vec3 motion = ent.getDeltaMovement();
        id = ent.getId();
        ltX = ent.xOld;
        ltY = ent.yOld;
        ltZ = ent.zOld;
        prevX = ent.xo;
        prevY = ent.yo;
        prevZ = ent.zo;
        posX = ent.getX();
        posY = ent.getY();
        posZ = ent.getZ();
        mX = motion.x;
        mY = motion.y;
        mZ = motion.z;
        prevYaw = ent.yRotO;
        prevPitch = ent.xRotO;
        yaw = ent.getYRot();
        pitch = ent.getXRot();
    }

    public static void encode(PacketEntityLocation message, FriendlyByteBuf buf)
    {
        buf.writeInt(message.id);
        buf.writeDouble(message.ltX);
        buf.writeDouble(message.ltY);
        buf.writeDouble(message.ltZ);
        buf.writeDouble(message.prevX);
        buf.writeDouble(message.prevY);
        buf.writeDouble(message.prevZ);
        buf.writeDouble(message.posX);
        buf.writeDouble(message.posY);
        buf.writeDouble(message.posZ);
        buf.writeDouble(message.mX);
        buf.writeDouble(message.mY);
        buf.writeDouble(message.mZ);
        buf.writeFloat(message.prevYaw);
        buf.writeFloat(message.prevPitch);
        buf.writeFloat(message.yaw);
        buf.writeFloat(message.pitch);
    }

    public static PacketEntityLocation decode(FriendlyByteBuf buf)
    {
        PacketEntityLocation message = new PacketEntityLocation();
        message.id = buf.readInt();
        message.ltX = buf.readDouble();
        message.ltY = buf.readDouble();
        message.ltZ = buf.readDouble();
        message.prevX = buf.readDouble();
        message.prevY = buf.readDouble();
        message.prevZ = buf.readDouble();
        message.posX = buf.readDouble();
        message.posY = buf.readDouble();
        message.posZ = buf.readDouble();
        message.mX = buf.readDouble();
        message.mY = buf.readDouble();
        message.mZ = buf.readDouble();
        message.prevYaw = buf.readFloat();
        message.prevPitch = buf.readFloat();
        message.yaw = buf.readFloat();
        message.pitch = buf.readFloat();
        return message;
    }

    public static void handle(PacketEntityLocation message, Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleEntityLocation(message)));
        context.setPacketHandled(true);
    }
}
