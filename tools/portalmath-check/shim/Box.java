package shim;

/** Stand-in for AxisAlignedBB (same expand / offset / intersects semantics as 1.12.2). */
public class Box
{
    public double minX, minY, minZ, maxX, maxY, maxZ;

    public Box(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        minX = Math.min(x1, x2); minY = Math.min(y1, y2); minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2); maxY = Math.max(y1, y2); maxZ = Math.max(z1, z2);
    }

    public Box expand(double x, double y, double z)
    {
        double d0 = minX, d1 = minY, d2 = minZ, d3 = maxX, d4 = maxY, d5 = maxZ;
        if(x < 0.0D) d0 += x; else if(x > 0.0D) d3 += x;
        if(y < 0.0D) d1 += y; else if(y > 0.0D) d4 += y;
        if(z < 0.0D) d2 += z; else if(z > 0.0D) d5 += z;
        return new Box(d0, d1, d2, d3, d4, d5);
    }

    public Box offset(double x, double y, double z)
    {
        return new Box(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    public boolean intersects(Box o)
    {
        return minX < o.maxX && maxX > o.minX && minY < o.maxY && maxY > o.minY && minZ < o.maxZ && maxZ > o.minZ;
    }
}
