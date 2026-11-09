package me.ichun.mods.portalgunclassic.common.portal;

import me.ichun.mods.portalgunclassic.common.sounds.SoundRegistry;
import me.ichun.mods.portalgunclassic.common.tileentity.TileEntityPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PortalInfo
{
    public boolean isOrange;
    public BlockPos pos;

    public PortalInfo(boolean o, BlockPos poss)
    {
        isOrange = o;
        pos = poss;
    }

    public void kill(Level world)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof TileEntityPortal portal)
        {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if(portal.face.getAxis() != Direction.Axis.Y)
            {
                BlockPos offset = portal.top ? pos.below() : pos.above();
                if(world.getBlockEntity(offset) instanceof TileEntityPortal)
                {
                    world.setBlock(offset, Blocks.AIR.defaultBlockState(), 3);
                }
            }

            world.playSound(null, pos.getX() + (portal.face.getAxis() != Direction.Axis.Y ? 1D : 0.5D), pos.getY() + (portal.face.getAxis() == Direction.Axis.Y ? 0.0D : 0.5D), pos.getZ() + (portal.face.getAxis() != Direction.Axis.Y ? 1D : 0.5D), SoundRegistry.fizzle.get(), SoundSource.BLOCKS, 0.3F, 1F);
        }
        else
        {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    public CompoundTag toNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("orange", isOrange);
        tag.putLong("pos", pos.asLong());
        return tag;
    }

    public static PortalInfo createFromNBT(CompoundTag tag)
    {
        return new PortalInfo(tag.getBoolean("orange"), BlockPos.of(tag.getLong("pos")));
    }
}
