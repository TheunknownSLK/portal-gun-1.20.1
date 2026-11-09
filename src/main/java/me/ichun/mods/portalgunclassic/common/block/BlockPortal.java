package me.ichun.mods.portalgunclassic.common.block;

import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.core.EventHandlerServer;
import me.ichun.mods.portalgunclassic.common.tileentity.TileEntityPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BlockPortal extends Block implements EntityBlock
{
    public BlockPortal()
    {
        //Original: Material.CIRCUITS (no collision, not opaque, not replaceable), hardness -1, resistance 1000000, light level 0.5 (= 7 of 15).
        super(BlockBehaviour.Properties.of().noCollission().noOcclusion().strength(-1F, 1000000.0F).lightLevel(state -> 7));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileEntityPortal(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if(type != PortalGunClassic.TILE_PORTAL.get())
        {
            return null;
        }
        return (lvl, pos, st, be) -> ((TileEntityPortal)be).tick();
    }

    //Original: EMPTY_AABB as bounding box (cannot be targeted) and null collision box.
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    //The block has no model of its own. The portal is drawn by the block entity renderer (the original used a TESR plus an empty dummy model).
    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof TileEntityPortal portal)
        {
            if(portal.setup)
            {
                if(portal.face.getAxis() == Direction.Axis.Y)
                {
                    if(!isSideSolid(world, pos.relative(portal.face, -1), portal.face))
                    {
                        EventHandlerServer.getSaveData(world).kill(world, portal.orange);
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                else
                {
                    BlockPos other = portal.top ? pos.below() : pos.above();
                    if(!(isSideSolid(world, pos.relative(portal.face, -1), portal.face) && isSideSolid(world, other.relative(portal.face, -1), portal.face)) || world.getBlockState(other).getBlock() != this)
                    {
                        EventHandlerServer.getSaveData(world).kill(world, portal.orange);
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        else
        {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    //Equivalent of World#isSideSolid (BlockFaceShape.SOLID): the face of the block must be a full, sturdy face.
    public static boolean isSideSolid(Level world, BlockPos pos, Direction side)
    {
        return world.getBlockState(pos).isFaceSturdy(world, pos, side);
    }

    private static boolean isReplaceableOrSamePortal(Level world, BlockPos pos, boolean isOrange)
    {
        if(world.getBlockState(pos).canBeReplaced())
        {
            return true;
        }
        BlockEntity te = world.getBlockEntity(pos);
        return te instanceof TileEntityPortal portal && portal.setup && portal.orange == isOrange;
    }

    public static boolean canPlace(Level world, BlockPos pos, Direction sideHit, boolean isOrange)
    {
        if(isReplaceableOrSamePortal(world, pos, isOrange))
        {
            if(sideHit.getAxis() == Direction.Axis.Y) //1 block portal
            {
                return isSideSolid(world, pos.relative(sideHit, -1), sideHit);
            }
            else
            {
                BlockPos posDown = pos.below();
                return isSideSolid(world, pos.relative(sideHit, -1), sideHit) && isReplaceableOrSamePortal(world, posDown, isOrange) && isSideSolid(world, posDown.relative(sideHit, -1), sideHit);
            }
        }
        return false;
    }
}
