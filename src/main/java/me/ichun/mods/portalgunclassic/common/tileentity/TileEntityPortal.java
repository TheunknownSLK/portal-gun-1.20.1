package me.ichun.mods.portalgunclassic.common.tileentity;

import me.ichun.mods.portalgunclassic.client.core.ClientPortalHelper;
import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.core.EventHandlerServer;
import me.ichun.mods.portalgunclassic.common.packet.PacketEntityLocation;
import me.ichun.mods.portalgunclassic.common.portal.PortalInfo;
import me.ichun.mods.portalgunclassic.common.portal.PortalMath;
import me.ichun.mods.portalgunclassic.common.sounds.SoundRegistry;
import me.ichun.mods.portalgunclassic.common.world.PortalSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEntityPortal extends BlockEntity
{
    public boolean setup;
    public boolean top;
    public boolean orange;
    public Direction face;

    public TileEntityPortal(BlockPos pos, BlockState state)
    {
        super(PortalGunClassic.TILE_PORTAL.get(), pos, state);
        top = false;
        orange = false;
        face = Direction.DOWN;
    }

    /** Area scanned for entities: the portal block(s) plus 4 blocks in front of it. */
    public AABB getScanBox()
    {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + (face.getAxis() != Direction.Axis.Y ? 2 : 1), pos.getZ() + 1).expandTowards(face.getStepX() * 4, face.getStepY() * 4, face.getStepZ() * 4);
    }

    /** Slab behind the plane of the portal. An entity that reaches it goes through the portal. */
    public AABB getInsideBox()
    {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + (face.getAxis() != Direction.Axis.Y ? 2 : 1), pos.getZ() + 1).expandTowards(face.getStepX() * 9, face.getStepY() * 9, face.getStepZ() * 9).move(-face.getStepX() * 9.999D, -face.getStepY() * 9.999D, -face.getStepZ() * 9.999D);
    }

    public void tick()
    {
        if(top)
        {
            return; //The top never does anything and is there just to look pretty.
        }

        Level level = getLevel();
        if(level == null)
        {
            return;
        }

        BlockPos pairLocation = BlockPos.ZERO;
        if(!level.isClientSide)
        {
            HashMap<String, PortalInfo> map = EventHandlerServer.getSaveData(level).portalInfo.get(PortalSavedData.dimKey(level));
            if(map == null)
            {
                return;
            }
            PortalInfo info = map.get(orange ? "blue" : "orange");
            if(info == null)
            {
                return;
            }
            pairLocation = info.pos;
        }
        else
        {
            if(!ClientPortalHelper.hasPair(orange))
            {
                return;
            }
        }
        //Only hits here if we have a pair
        AABB aabbScan = getScanBox();
        AABB aabbInside = getInsideBox();
        List<Entity> ents = new ArrayList<>();
        if(level.isClientSide)
        {
            ents.addAll(level.getEntitiesOfClass(Player.class, aabbScan));
        }
        else
        {
            ents.addAll(level.getEntitiesOfClass(Entity.class, aabbScan));
        }
        for(Entity ent : ents)
        {
            if(!level.isClientSide && ent instanceof Player)
            {
                continue; //we ignore players. They tell the server when they want a teleport.
            }

            Vec3 motion = ent.getDeltaMovement();
            if(ent.getBoundingBox().move(motion.x, motion.y, motion.z).intersects(aabbInside))
            {
                if(level.isClientSide)
                {
                    ClientPortalHelper.handleClientTeleport(this, (Player)ent);
                }
                else
                {
                    if(level.getBlockEntity(pairLocation) instanceof TileEntityPortal pair)
                    {
                        teleport(ent, pair);
                    }
                }
            }
        }
    }

    //Server side only. The position / rotation / velocity math is in PortalMath.
    public void teleport(Entity ent, TileEntityPortal pair)
    {
        Level level = getLevel();
        BlockPos pos = getBlockPos();
        BlockPos pairPos = pair.getBlockPos();

        AABB bb = ent.getBoundingBox();
        Vec3 motion = ent.getDeltaMovement();
        PortalMath.Result result = PortalMath.compute(face.getStepX(), face.getStepY(), face.getStepZ(), pair.face.getStepX(), pair.face.getStepY(), pair.face.getStepZ(), pairPos.getX(), pairPos.getY(), pairPos.getZ(), bb.maxX - bb.minX, bb.maxZ - bb.minZ, bb.maxY - bb.minY, motion.x, motion.y, motion.z, ent.getYRot(), ent.getXRot());

        ent.setDeltaMovement(result.mx(), result.my(), result.mz());
        if(result.resetFall())
        {
            ent.fallDistance = 0F;
        }
        ent.moveTo(result.x(), result.y(), result.z(), result.yaw(), result.pitch());

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + (face.getAxis() != Direction.Axis.Y ? 1D : 0.5D), pos.getZ() + 0.5D, SoundRegistry.enter.get(), SoundSource.BLOCKS, 0.1F, 1.0F);
        level.playSound(null, pairPos.getX() + 0.5D, pairPos.getY() + (pair.face.getAxis() != Direction.Axis.Y ? 1D : 0.5D), pairPos.getZ() + 0.5D, SoundRegistry.exit.get(), SoundSource.BLOCKS, 0.1F, 1.0F);

        PortalGunClassic.channel.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 256, level.dimension())), new PacketEntityLocation(ent));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    public void setup(boolean top, boolean orange, Direction face)
    {
        this.setup = true;

        this.top = top;
        this.orange = orange;
        this.face = face;

        setChanged();
        if(level != null)
        {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putBoolean("setup", setup);
        tag.putBoolean("top", top);
        tag.putBoolean("orange", orange);
        tag.putInt("face", face.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        setup = tag.getBoolean("setup");
        top = tag.getBoolean("top");
        orange = tag.getBoolean("orange");
        face = Direction.from3DDataValue(tag.getInt("face"));
    }
}
