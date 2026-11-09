package shim;

/**
 * The body of teleport() below is copied VERBATIM from the original 1.12.2 source:
 * me/ichun/mods/portalgunclassic/common/tileentity/TileEntityPortal.java, lines 112-182
 * (the sound and packet statements that follow, lines 184-187, are not part of the math).
 */
public class OriginalTileEntityPortal
{
    public EnumFacing face;
    private final BlockPos pos;

    public OriginalTileEntityPortal(BlockPos pos, EnumFacing face) { this.pos = pos; this.face = face; }
    public BlockPos getPos() { return pos; }

    public void teleport(Entity ent, OriginalTileEntityPortal pair)
    {
        ent.posX = pair.getPos().getX() + 0.5D - (0.5D - (ent.getEntityBoundingBox().maxX - ent.getEntityBoundingBox().minX) / 2D) * 0.99D * pair.face.getFrontOffsetX();
        ent.posZ = pair.getPos().getZ() + 0.5D - (0.5D - (ent.getEntityBoundingBox().maxZ - ent.getEntityBoundingBox().minZ) / 2D) * 0.99D * pair.face.getFrontOffsetZ();

        ent.posY = pair.getPos().getY() + (pair.face.getFrontOffsetY() < 0 ? -(ent.getEntityBoundingBox().maxY - ent.getEntityBoundingBox().minY) + 0.999D : 0.001D);

        if(face.getAxis() != EnumFacing.Axis.Y && pair.face.getAxis() != EnumFacing.Axis.Y) //horizontal
        {
            float yawDiff = face.getHorizontalAngle() - (pair.face.getOpposite().getHorizontalAngle());
            ent.prevRotationPitch -= yawDiff;
            ent.rotationYaw -= yawDiff;

            double mX = ent.motionX;
            double mZ = ent.motionZ;

            if(pair.face == face)
            {
                ent.motionX = -mX;
                ent.motionZ = -mZ;
            }
            else if(face.getAxis() == EnumFacing.Axis.X)
            {
                if(pair.face == EnumFacing.NORTH)
                {
                    ent.motionZ = -mX * - face.getFrontOffsetX();
                    ent.motionX = mZ * - face.getFrontOffsetX();
                }
                else if(pair.face == EnumFacing.SOUTH)
                {
                    ent.motionZ = mX * - face.getFrontOffsetX();
                    ent.motionX = -mZ * - face.getFrontOffsetX();
                }
            }
            else if(face.getAxis() == EnumFacing.Axis.Z)
            {
                if(pair.face == EnumFacing.EAST)
                {
                    ent.motionZ = -mX * - face.getFrontOffsetZ();
                    ent.motionX = mZ * - face.getFrontOffsetZ();
                }
                else if(pair.face == EnumFacing.WEST)
                {
                    ent.motionZ = mX * - face.getFrontOffsetZ();
                    ent.motionX = -mZ * - face.getFrontOffsetZ();
                }
            }
        }
        else if(face.getAxis() == EnumFacing.Axis.Y && pair.face.getAxis() != EnumFacing.Axis.Y) //from vertical to horizontal
        {
            ent.rotationPitch = 0F;
            ent.rotationYaw = pair.face.getHorizontalAngle();
            ent.motionX = Math.abs(ent.motionY) * pair.face.getFrontOffsetX();
            ent.motionZ = Math.abs(ent.motionY) * pair.face.getFrontOffsetZ();
            ent.motionY = 0D;
            ent.fallDistance = 0F;
        }
        else if(face.getAxis() != EnumFacing.Axis.Y && pair.face.getAxis() == EnumFacing.Axis.Y) //from horizontal to vertical
        {
            ent.motionY = Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) * pair.face.getFrontOffsetY();
        }
        else //vertical only
        {
            if(pair.face == face)
            {
                ent.motionY = -ent.motionY;
            }
            ent.fallDistance = 0F;
        }
        ent.motionX += pair.face.getFrontOffsetX() * 0.2D;
        ent.motionY += pair.face.getFrontOffsetY() * 0.2D;
        ent.motionZ += pair.face.getFrontOffsetZ() * 0.2D;
        ent.setLocationAndAngles(ent.posX, ent.posY, ent.posZ, ent.rotationYaw, ent.rotationPitch);
    }
}
