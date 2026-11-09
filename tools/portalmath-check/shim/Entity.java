package shim;

/** Stand-in for the 1.12.2 Entity fields used by the original teleport code. */
public class Entity
{
    public double posX, posY, posZ;
    public double motionX, motionY, motionZ;
    public float rotationYaw, rotationPitch, prevRotationPitch, fallDistance;
    public Box box;

    public Box getEntityBoundingBox() { return box; }

    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch)
    {
        this.posX = x; this.posY = y; this.posZ = z;
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
    }
}
