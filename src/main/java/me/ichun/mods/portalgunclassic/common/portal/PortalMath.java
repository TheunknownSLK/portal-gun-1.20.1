package me.ichun.mods.portalgunclassic.common.portal;

/**
 * The position / orientation / velocity transformation applied when an entity goes through a portal.
 * This is the math of the original TileEntityPortal#teleport (1.12.2), moved out of the world-touching
 * code unchanged so it can be checked on its own. It only uses plain numbers:
 * face directions are given as their integer steps (Direction#getStepX/Y/Z, same values as EnumFacing#getFrontOffsetX/Y/Z).
 */
public final class PortalMath
{
    private PortalMath() {}

    public record Result(double x, double y, double z, double mx, double my, double mz, float yaw, float pitch, boolean resetFall) {}

    /** Same values as EnumFacing#getHorizontalAngle / Direction#toYRot: SOUTH 0, WEST 90, NORTH 180, EAST 270, UP and DOWN 270. */
    public static float horizontalAngle(int stepX, int stepY, int stepZ)
    {
        int horizontalIndex;
        if(stepY != 0)
        {
            horizontalIndex = -1;
        }
        else if(stepZ > 0)
        {
            horizontalIndex = 0;
        }
        else if(stepX < 0)
        {
            horizontalIndex = 1;
        }
        else if(stepZ < 0)
        {
            horizontalIndex = 2;
        }
        else
        {
            horizontalIndex = 3;
        }
        return (float)((horizontalIndex & 3) * 90);
    }

    /**
     * @param fx,fy,fz  steps of the face of the portal the entity enters ("face" in the original)
     * @param dx,dy,dz  steps of the face of the destination portal ("pair.face" in the original)
     * @param pairX,pairY,pairZ block position of the destination portal
     * @param width,depth,height size of the entity's bounding box (maxX-minX, maxZ-minZ, maxY-minY)
     */
    public static Result compute(int fx, int fy, int fz, int dx, int dy, int dz, int pairX, int pairY, int pairZ, double width, double depth, double height, double motionX, double motionY, double motionZ, float yaw, float pitch)
    {
        double posX = pairX + 0.5D - (0.5D - width / 2D) * 0.99D * dx;
        double posZ = pairZ + 0.5D - (0.5D - depth / 2D) * 0.99D * dz;

        double posY = pairY + (dy < 0 ? -height + 0.999D : 0.001D);

        boolean faceVertical = fy != 0;
        boolean pairVertical = dy != 0;
        boolean sameFace = fx == dx && fy == dy && fz == dz;

        boolean resetFall = false;

        if(!faceVertical && !pairVertical) //horizontal
        {
            float yawDiff = horizontalAngle(fx, fy, fz) - horizontalAngle(-dx, -dy, -dz);
            //Original also did "ent.prevRotationPitch -= yawDiff;" here. Entity#moveTo (the equivalent of
            //setLocationAndAngles, always called at the end) rewrites the previous rotations, so it has no lasting effect.
            yaw -= yawDiff;

            double mX = motionX;
            double mZ = motionZ;

            if(sameFace)
            {
                motionX = -mX;
                motionZ = -mZ;
            }
            else if(fx != 0) //face axis X
            {
                if(dz == -1) //pair NORTH
                {
                    motionZ = -mX * - fx;
                    motionX = mZ * - fx;
                }
                else if(dz == 1) //pair SOUTH
                {
                    motionZ = mX * - fx;
                    motionX = -mZ * - fx;
                }
            }
            else if(fz != 0) //face axis Z
            {
                if(dx == 1) //pair EAST
                {
                    motionZ = -mX * - fz;
                    motionX = mZ * - fz;
                }
                else if(dx == -1) //pair WEST
                {
                    motionZ = mX * - fz;
                    motionX = -mZ * - fz;
                }
            }
        }
        else if(faceVertical && !pairVertical) //from vertical to horizontal
        {
            pitch = 0F;
            yaw = horizontalAngle(dx, dy, dz);
            motionX = Math.abs(motionY) * dx;
            motionZ = Math.abs(motionY) * dz;
            motionY = 0D;
            resetFall = true;
        }
        else if(!faceVertical && pairVertical) //from horizontal to vertical
        {
            motionY = Math.sqrt(motionX * motionX + motionZ * motionZ) * dy;
        }
        else //vertical only
        {
            if(sameFace)
            {
                motionY = -motionY;
            }
            resetFall = true;
        }
        motionX += dx * 0.2D;
        motionY += dy * 0.2D;
        motionZ += dz * 0.2D;

        return new Result(posX, posY, posZ, motionX, motionY, motionZ, yaw, pitch, resetFall);
    }
}
