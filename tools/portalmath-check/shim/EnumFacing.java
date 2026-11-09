package shim;

/** Stand-in for the 1.12.2 EnumFacing: same steps, same horizontal indices (DOWN/UP -1, SOUTH 0, WEST 1, NORTH 2, EAST 3), same opposites. */
public enum EnumFacing
{
    DOWN(0, -1, 0, -1), UP(0, 1, 0, -1), NORTH(0, 0, -1, 2), SOUTH(0, 0, 1, 0), WEST(-1, 0, 0, 1), EAST(1, 0, 0, 3);

    public enum Axis { X, Y, Z }

    private final int x, y, z, horizontalIndex;

    EnumFacing(int x, int y, int z, int horizontalIndex)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.horizontalIndex = horizontalIndex;
    }

    public int getFrontOffsetX() { return x; }
    public int getFrontOffsetY() { return y; }
    public int getFrontOffsetZ() { return z; }

    public float getHorizontalAngle()
    {
        return (float)((this.horizontalIndex & 3) * 90);
    }

    public Axis getAxis()
    {
        return y != 0 ? Axis.Y : (x != 0 ? Axis.X : Axis.Z);
    }

    public EnumFacing getOpposite()
    {
        switch(this)
        {
            case DOWN: return UP;
            case UP: return DOWN;
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case WEST: return EAST;
            default: return WEST;
        }
    }
}
