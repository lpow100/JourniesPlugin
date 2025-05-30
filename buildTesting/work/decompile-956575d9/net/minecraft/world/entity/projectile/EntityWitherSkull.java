package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.wither.EntityWither;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

public class EntityWitherSkull extends EntityFireball {

    private static final DataWatcherObject<Boolean> DATA_DANGEROUS = DataWatcher.<Boolean>defineId(EntityWitherSkull.class, DataWatcherRegistry.BOOLEAN);
    private static final boolean DEFAULT_DANGEROUS = false;

    public EntityWitherSkull(EntityTypes<? extends EntityWitherSkull> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityWitherSkull(World world, EntityLiving entityliving, Vec3D vec3d) {
        super(EntityTypes.WITHER_SKULL, entityliving, vec3d, world);
    }

    @Override
    protected float getInertia() {
        return this.isDangerous() ? 0.73F : super.getInertia();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid, float f) {
        return this.isDangerous() && EntityWither.canDestroy(iblockdata) ? Math.min(0.8F, f) : f;
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            Entity entity = movingobjectpositionentity.getEntity();
            Entity entity1 = this.getOwner();
            boolean flag;

            if (entity1 instanceof EntityLiving entityliving) {
                DamageSource damagesource = this.damageSources().witherSkull(this, entityliving);

                flag = entity.hurtServer(worldserver, damagesource, 8.0F);
                if (flag) {
                    if (entity.isAlive()) {
                        EnchantmentManager.doPostAttackEffects(worldserver, entity, damagesource);
                    } else {
                        entityliving.heal(5.0F);
                    }
                }
            } else {
                flag = entity.hurtServer(worldserver, this.damageSources().magic(), 5.0F);
            }

            if (flag && entity instanceof EntityLiving entityliving1) {
                int i = 0;

                if (this.level().getDifficulty() == EnumDifficulty.NORMAL) {
                    i = 10;
                } else if (this.level().getDifficulty() == EnumDifficulty.HARD) {
                    i = 40;
                }

                if (i > 0) {
                    entityliving1.addEffect(new MobEffect(MobEffects.WITHER, 20 * i, 1), this.getEffectSource());
                }
            }

        }
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, World.a.MOB);
            this.discard();
        }

    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityWitherSkull.DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return (Boolean) this.entityData.get(EntityWitherSkull.DATA_DANGEROUS);
    }

    public void setDangerous(boolean flag) {
        this.entityData.set(EntityWitherSkull.DATA_DANGEROUS, flag);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putBoolean("dangerous", this.isDangerous());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setDangerous(nbttagcompound.getBooleanOr("dangerous", false));
    }
}
