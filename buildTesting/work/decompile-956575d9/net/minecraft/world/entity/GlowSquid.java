package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.EntitySquid;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Blocks;

public class GlowSquid extends EntitySquid {

    private static final DataWatcherObject<Integer> DATA_DARK_TICKS_REMAINING = DataWatcher.<Integer>defineId(GlowSquid.class, DataWatcherRegistry.INT);
    private static final int DEFAULT_DARK_TICKS_REMAINING = 0;

    public GlowSquid(EntityTypes<? extends GlowSquid> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected ParticleParam getInkParticle() {
        return Particles.GLOW_SQUID_INK;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(GlowSquid.DATA_DARK_TICKS_REMAINING, 0);
    }

    @Nullable
    @Override
    public EntityAgeable getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        return EntityTypes.GLOW_SQUID.create(worldserver, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEffect getSquirtSound() {
        return SoundEffects.GLOW_SQUID_SQUIRT;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.GLOW_SQUID_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.GLOW_SQUID_DEATH;
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putInt("DarkTicksRemaining", this.getDarkTicksRemaining());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setDarkTicks(nbttagcompound.getIntOr("DarkTicksRemaining", 0));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int i = this.getDarkTicksRemaining();

        if (i > 0) {
            this.setDarkTicks(i - 1);
        }

        this.level().addParticle(Particles.GLOW, this.getRandomX(0.6D), this.getRandomY(), this.getRandomZ(0.6D), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        boolean flag = super.hurtServer(worldserver, damagesource, f);

        if (flag) {
            this.setDarkTicks(100);
        }

        return flag;
    }

    public void setDarkTicks(int i) {
        this.entityData.set(GlowSquid.DATA_DARK_TICKS_REMAINING, i);
    }

    public int getDarkTicksRemaining() {
        return (Integer) this.entityData.get(GlowSquid.DATA_DARK_TICKS_REMAINING);
    }

    public static boolean checkGlowSquidSpawnRules(EntityTypes<? extends EntityLiving> entitytypes, WorldAccess worldaccess, EntitySpawnReason entityspawnreason, BlockPosition blockposition, RandomSource randomsource) {
        return blockposition.getY() <= worldaccess.getSeaLevel() - 33 && worldaccess.getRawBrightness(blockposition, 0) == 0 && worldaccess.getBlockState(blockposition).is(Blocks.WATER);
    }
}
