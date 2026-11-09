package me.ichun.mods.portalgunclassic.common.entity;

import me.ichun.mods.portalgunclassic.common.PortalGunClassic;
import me.ichun.mods.portalgunclassic.common.block.BlockPortal;
import me.ichun.mods.portalgunclassic.common.core.EventHandlerServer;
import me.ichun.mods.portalgunclassic.common.sounds.SoundRegistry;
import me.ichun.mods.portalgunclassic.common.tileentity.TileEntityPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class EntityPortalProjectile extends Entity
{
    private static final EntityDataAccessor<Boolean> ORANGE = SynchedEntityData.defineId(EntityPortalProjectile.class, EntityDataSerializers.BOOLEAN);
    public int age = 0;

    public EntityPortalProjectile(EntityType<? extends EntityPortalProjectile> type, Level world)
    {
        super(type, world);
        setInvulnerable(true);
    }

    public EntityPortalProjectile(Level world, Entity entity, boolean isOrange)
    {
        this(PortalGunClassic.ENTITY_PORTAL_PROJECTILE.get(), world);
        this.entityData.set(ORANGE, isOrange);
        shoot(entity, 4.999F);
        moveTo(entity.getX(), entity.getY() + entity.getEyeHeight() - (getBbWidth() / 2F), entity.getZ(), entity.getYRot(), entity.getXRot());
    }

    public void setOrange(boolean flag)
    {
        entityData.set(ORANGE, flag);
    }

    public boolean isOrange()
    {
        return entityData.get(ORANGE);
    }

    public void shoot(Entity entity, float velocity)
    {
        float f = -Mth.sin(entity.getYRot() * 0.017453292F) * Mth.cos(entity.getXRot() * 0.017453292F);
        float f1 = -Mth.sin((entity.getXRot()) * 0.017453292F);
        float f2 = Mth.cos(entity.getYRot() * 0.017453292F) * Mth.cos(entity.getXRot() * 0.017453292F);
        this.shoot((double)f, (double)f1, (double)f2, velocity);
        Vec3 shooterMotion = entity.getDeltaMovement();
        Vec3 motion = getDeltaMovement();
        double motionY = motion.y;
        if(!entity.onGround())
        {
            motionY += shooterMotion.y;
        }
        setDeltaMovement(motion.x + shooterMotion.x, motionY, motion.z + shooterMotion.z);
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void shoot(double x, double y, double z, float velocity)
    {
        float f = (float)Math.sqrt(x * x + y * y + z * z);
        x = x / (double)f;
        y = y / (double)f;
        z = z / (double)f;
        x = x * (double)velocity;
        y = y * (double)velocity;
        z = z * (double)velocity;
        setDeltaMovement(x, y, z);
        float f1 = (float)Math.sqrt(x * x + z * z);
        setYRot((float)(Mth.atan2(x, z) * (180D / Math.PI)));
        setXRot((float)(Mth.atan2(y, (double)f1) * (180D / Math.PI)));
        this.yRotO = getYRot();
        this.xRotO = getXRot();
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(ORANGE, false);
    }

    @Override
    public void tick()
    {
        Level world = level();
        if(getY() > world.getHeight() * 2 || getY() < -world.getHeight() * 2 || age > 1200) //a minute
        {
            discard();
            return;
        }

        age++;

        this.xOld = getX();
        this.yOld = getY();
        this.zOld = getZ();

        super.tick();

        Vec3 motion = getDeltaMovement();

        Vec3 vec31 = new Vec3(getX(), getY(), getZ());
        Vec3 vec32 = new Vec3(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        if(!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z))
        {
            if(!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z))
            {
                int i = Mth.floor(vec32.x);
                int j = Mth.floor(vec32.y);
                int k = Mth.floor(vec32.z);
                int l = Mth.floor(vec31.x);
                int i1 = Mth.floor(vec31.y);
                int j1 = Mth.floor(vec31.z);
                BlockPos blockpos;

                int k1 = 200;

                while(k1-- >= 0)
                {
                    if(Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z) || (l == i && i1 == j && j1 == k))
                    {
                        break;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if(i > l)
                    {
                        d0 = (double)l + 1.0D;
                    }
                    else if(i < l)
                    {
                        d0 = (double)l + 0.0D;
                    }
                    else
                    {
                        flag2 = false;
                    }

                    if(j > i1)
                    {
                        d1 = (double)i1 + 1.0D;
                    }
                    else if(j < i1)
                    {
                        d1 = (double)i1 + 0.0D;
                    }
                    else
                    {
                        flag = false;
                    }

                    if(k > j1)
                    {
                        d2 = (double)j1 + 1.0D;
                    }
                    else if(k < j1)
                    {
                        d2 = (double)j1 + 0.0D;
                    }
                    else
                    {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

                    if(flag2)
                    {
                        d3 = (d0 - vec31.x) / d6;
                    }

                    if(flag)
                    {
                        d4 = (d1 - vec31.y) / d7;
                    }

                    if(flag1)
                    {
                        d5 = (d2 - vec31.z) / d8;
                    }

                    if(d3 == -0.0D)
                    {
                        d3 = -1.0E-4D;
                    }

                    if(d4 == -0.0D)
                    {
                        d4 = -1.0E-4D;
                    }

                    if(d5 == -0.0D)
                    {
                        d5 = -1.0E-4D;
                    }

                    Direction enumfacing;

                    if(d3 < d4 && d3 < d5)
                    {
                        enumfacing = i > l ? Direction.WEST : Direction.EAST;
                        vec31 = new Vec3(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    }
                    else if(d4 < d5)
                    {
                        enumfacing = j > i1 ? Direction.DOWN : Direction.UP;
                        vec31 = new Vec3(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    }
                    else
                    {
                        enumfacing = k > j1 ? Direction.NORTH : Direction.SOUTH;
                        vec31 = new Vec3(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = Mth.floor(vec31.x) - (enumfacing == Direction.EAST ? 1 : 0);
                    i1 = Mth.floor(vec31.y) - (enumfacing == Direction.UP ? 1 : 0);
                    j1 = Mth.floor(vec31.z) - (enumfacing == Direction.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    BlockState iblockstate1 = world.getBlockState(blockpos);

                    //Original: blocks whose collision box is NULL_AABB (no collision: air, water, plants...) are skipped.
                    if(!iblockstate1.getCollisionShape(world, blockpos).isEmpty())
                    {
                        //Original: Block#collisionRayTrace, which traces against the block's bounding box (outline shape).
                        BlockHitResult raytraceresult1 = iblockstate1.getShape(world, blockpos).clip(vec31, vec32, blockpos);
                        if(raytraceresult1 != null)
                        {
                            if(iblockstate1.is(Blocks.IRON_BARS))
                            {
                                Vec3 hitVec = raytraceresult1.getLocation();
                                vec31 = new Vec3(hitVec.x + (motion.x / 5000D), hitVec.y + (motion.y / 5000D), hitVec.z + (motion.z / 5000D));
                            }
                            else
                            {
                                createPortal(raytraceresult1);
                                discard();
                                return;
                            }
                        }
                    }
                }
            }
        }

        double posX = getX() + motion.x;
        double posY = getY() + motion.y;
        double posZ = getZ() + motion.z;
        float f = (float)Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float rotationYaw = (float)(Mth.atan2(motion.x, motion.z) * (180D / Math.PI));

        float rotationPitch;
        float prevRotationPitch = this.xRotO;
        float prevRotationYaw = this.yRotO;
        for(rotationPitch = (float)(Mth.atan2(motion.y, (double)f) * (180D / Math.PI)); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
        {
            ;
        }

        while(rotationPitch - prevRotationPitch >= 180.0F)
        {
            prevRotationPitch += 360.0F;
        }

        while(rotationYaw - prevRotationYaw < -180.0F)
        {
            prevRotationYaw -= 360.0F;
        }

        while(rotationYaw - prevRotationYaw >= 180.0F)
        {
            prevRotationYaw += 360.0F;
        }

        this.xRotO = prevRotationPitch;
        this.yRotO = prevRotationYaw;
        setXRot(prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F);
        setYRot(prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F);

        this.setPos(posX, posY, posZ);
    }

    public void createPortal(BlockHitResult rayTraceResult)
    {
        Level world = level();
        if(!world.isClientSide)
        {
            Direction sideHit = rayTraceResult.getDirection();
            BlockPos pos = rayTraceResult.getBlockPos().relative(sideHit);
            if(BlockPortal.canPlace(world, pos, sideHit, isOrange()))
            {
                EventHandlerServer.getSaveData(world).kill(world, isOrange());

                world.setBlock(pos, PortalGunClassic.BLOCK_PORTAL.get().defaultBlockState(), 3);
                BlockEntity te = world.getBlockEntity(pos);
                if(te instanceof TileEntityPortal portal)
                {
                    portal.setup(sideHit.getAxis() != Direction.Axis.Y, isOrange(), sideHit);
                }
                if(sideHit.getAxis() != Direction.Axis.Y)
                {
                    world.setBlock(pos.below(), PortalGunClassic.BLOCK_PORTAL.get().defaultBlockState(), 3);
                    te = world.getBlockEntity(pos.below());
                    if(te instanceof TileEntityPortal portal)
                    {
                        portal.setup(false, isOrange(), sideHit);
                    }
                }
                EventHandlerServer.getSaveData(world).set(world, isOrange(), sideHit.getAxis() != Direction.Axis.Y ? pos.below() : pos);

                world.playSound(null, getX(), getY() + (getBbHeight() / 2F), getZ(), isOrange() ? SoundRegistry.openred.get() : SoundRegistry.openblue.get(), SoundSource.BLOCKS, 0.3F, 1.0F);
            }
            else
            {
                world.playSound(null, getX(), getY() + (getBbHeight() / 2F), getZ(), SoundRegistry.invalid.get(), SoundSource.NEUTRAL, 0.5F, 1.0F);
            }
        }
    }

    //Original: isInRangeToRenderDist with renderDistanceWeight 1.0 (average edge length * 10 * 64 blocks).
    @Override
    public boolean shouldRenderAtSqrDistance(double distance)
    {
        double d0 = this.getBoundingBox().getSize() * 10D;

        if(Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        setOrange(tag.getBoolean("orange"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        tag.putBoolean("orange", isOrange());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
