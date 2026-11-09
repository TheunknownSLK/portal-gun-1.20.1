import me.ichun.mods.portalgunclassic.common.portal.PortalMath;
import shim.BlockPos;
import shim.Box;
import shim.EnumFacing;
import shim.Entity;
import shim.OriginalTileEntityPortal;

import java.util.Random;

/**
 * Compares PortalMath (the ported math) with the ORIGINAL 1.12.2 teleport code, run verbatim (shim/OriginalTileEntityPortal),
 * for every pair of portal faces (6 x 6 = 36) and thousands of random entities, then checks two behaviours of the original:
 *  - walking straight into a portal comes out straight, away from the other portal, at the same speed (+ the 0.2 push);
 *  - the entity does not end up inside the exit portal's detection area (no instant bounce back).
 */
public class Check
{
    static int failures = 0;

    static boolean same(double a, double b) { return Double.doubleToLongBits(a) == Double.doubleToLongBits(b); }
    static boolean same(float a, float b) { return Float.floatToIntBits(a) == Float.floatToIntBits(b); }

    static void fail(String msg)
    {
        failures++;
        if(failures <= 25)
        {
            System.out.println("FAIL: " + msg);
        }
    }

    public static void main(String[] args)
    {
        //1. horizontal angle helper
        for(EnumFacing f : EnumFacing.values())
        {
            if(!same(f.getHorizontalAngle(), PortalMath.horizontalAngle(f.getFrontOffsetX(), f.getFrontOffsetY(), f.getFrontOffsetZ())))
            {
                fail("horizontalAngle " + f);
            }
        }

        //2. equivalence with the original code
        Random r = new Random(20261102L);
        double[] widths = {0.25, 0.3, 0.6, 0.98, 1.0, 1.4};
        double[] heights = {0.25, 0.3, 1.8, 2.9};
        long cases = 0;
        for(EnumFacing src : EnumFacing.values())
        {
            for(EnumFacing dst : EnumFacing.values())
            {
                for(int i = 0; i < 3000; i++)
                {
                    cases++;
                    double w = widths[r.nextInt(widths.length)];
                    double d = widths[r.nextInt(widths.length)];
                    double h = heights[r.nextInt(heights.length)];
                    double mx = pick(r), my = pick(r), mz = pick(r);
                    float yaw = (float)(r.nextDouble() * 1440 - 720);
                    float pitch = (float)(r.nextDouble() * 180 - 90);
                    int px = r.nextInt(2000) - 1000, py = r.nextInt(300) - 30, pz = r.nextInt(2000) - 1000;
                    double ex = r.nextDouble() * 50 - 25, ey = r.nextDouble() * 50, ez = r.nextDouble() * 50 - 25;

                    Entity e = new Entity();
                    e.box = new Box(ex - w / 2, ey, ez - d / 2, ex + w / 2, ey + h, ez + d / 2);
                    e.posX = ex; e.posY = ey; e.posZ = ez;
                    e.motionX = mx; e.motionY = my; e.motionZ = mz;
                    e.rotationYaw = yaw; e.rotationPitch = pitch; e.prevRotationPitch = pitch;
                    e.fallDistance = 7F;

                    OriginalTileEntityPortal self = new OriginalTileEntityPortal(new BlockPos(0, 64, 0), src);
                    OriginalTileEntityPortal pair = new OriginalTileEntityPortal(new BlockPos(px, py, pz), dst);
                    self.teleport(e, pair);

                    PortalMath.Result res = PortalMath.compute(src.getFrontOffsetX(), src.getFrontOffsetY(), src.getFrontOffsetZ(), dst.getFrontOffsetX(), dst.getFrontOffsetY(), dst.getFrontOffsetZ(), px, py, pz, e.box.maxX - e.box.minX, e.box.maxZ - e.box.minZ, e.box.maxY - e.box.minY, mx, my, mz, yaw, pitch);

                    String id = src + "->" + dst + " case " + i;
                    if(!same(e.posX, res.x()) || !same(e.posY, res.y()) || !same(e.posZ, res.z())) fail("position " + id);
                    if(!same(e.motionX, res.mx()) || !same(e.motionY, res.my()) || !same(e.motionZ, res.mz())) fail("motion " + id);
                    if(!same(e.rotationYaw, res.yaw()) || !same(e.rotationPitch, res.pitch())) fail("rotation " + id);
                    if((e.fallDistance == 0F) != res.resetFall()) fail("fallDistance " + id);
                }
            }
        }
        System.out.println("Equivalence with the original: " + cases + " cases (36 face pairs), mismatches so far: " + failures);

        //3. straight in -> straight out, no instant bounce (run on the ORIGINAL code)
        int before = failures;
        int semantic = 0;
        double[] speeds = {0.05, 0.1, 0.3, 1.0, 3.9};
        for(EnumFacing src : EnumFacing.values())
        {
            for(EnumFacing dst : EnumFacing.values())
            {
                for(double s : speeds)
                {
                    semantic++;
                    double w = 0.6, h = 1.8;
                    Entity e = new Entity();
                    e.box = new Box(-w / 2, 0, -w / 2, w / 2, h, w / 2);
                    e.motionX = -src.getFrontOffsetX() * s;
                    e.motionY = -src.getFrontOffsetY() * s;
                    e.motionZ = -src.getFrontOffsetZ() * s;
                    e.fallDistance = 7F;
                    OriginalTileEntityPortal self = new OriginalTileEntityPortal(new BlockPos(0, 64, 0), src);
                    OriginalTileEntityPortal pair = new OriginalTileEntityPortal(new BlockPos(10, 64, -7), dst);
                    self.teleport(e, pair);

                    String id = src + "->" + dst + " speed " + s;
                    double exp = s + 0.2;
                    if(Math.abs(e.motionX - dst.getFrontOffsetX() * exp) > 1e-9 || Math.abs(e.motionY - dst.getFrontOffsetY() * exp) > 1e-9 || Math.abs(e.motionZ - dst.getFrontOffsetZ() * exp) > 1e-9)
                    {
                        fail("exit direction " + id + " got (" + e.motionX + ", " + e.motionY + ", " + e.motionZ + ")");
                    }

                    //would the exit portal's detection pick the entity up again on the next tick?
                    Box body = new Box(e.posX - w / 2, e.posY, e.posZ - w / 2, e.posX + w / 2, e.posY + h, e.posZ + w / 2);
                    Box inside = new Box(10, 64, -7, 11, 64 + (dst.getAxis() != EnumFacing.Axis.Y ? 2 : 1), -6).expand(dst.getFrontOffsetX() * 9, dst.getFrontOffsetY() * 9, dst.getFrontOffsetZ() * 9).offset(-dst.getFrontOffsetX() * 9.999D, -dst.getFrontOffsetY() * 9.999D, -dst.getFrontOffsetZ() * 9.999D);
                    if(body.offset(e.motionX, e.motionY, e.motionZ).intersects(inside))
                    {
                        fail("instant bounce " + id);
                    }
                }
            }
        }
        System.out.println("Behaviour of the original (straight in/out, no instant bounce): " + semantic + " cases, failures: " + (failures - before));

        if(failures == 0)
        {
            System.out.println("RESULT: PASS");
        }
        else
        {
            System.out.println("RESULT: FAIL (" + failures + ")");
            System.exit(1);
        }
    }

    static double pick(Random r)
    {
        int k = r.nextInt(10);
        if(k == 0) return 0.0D;
        if(k == 1) return -0.0D;
        return r.nextDouble() * 8 - 4;
    }
}
