package net.minecraft.world.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.behavior.BehaviorAttack;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetForget;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetSet;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorNop;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorUtil;
import net.minecraft.world.entity.ai.behavior.BehaviorWalkAwayOutOfRange;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.hoglin.IOglin;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.state.IBlockData;

public class EntityZoglin extends EntityMonster implements IOglin {

    private static final DataWatcherObject<Boolean> DATA_BABY_ID = DataWatcher.<Boolean>defineId(EntityZoglin.class, DataWatcherRegistry.BOOLEAN);
    private static final int MAX_HEALTH = 40;
    private static final int ATTACK_KNOCKBACK = 1;
    private static final float KNOCKBACK_RESISTANCE = 0.6F;
    private static final int ATTACK_DAMAGE = 6;
    private static final float BABY_ATTACK_DAMAGE = 0.5F;
    private static final int ATTACK_INTERVAL = 40;
    private static final int BABY_ATTACK_INTERVAL = 15;
    private static final int ATTACK_DURATION = 200;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
    private static final boolean DEFAULT_BABY = false;
    private int attackAnimationRemainingTicks;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super EntityZoglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

    public EntityZoglin(EntityTypes<? extends EntityZoglin> entitytypes, World world) {
        super(entitytypes, world);
        this.xpReward = 5;
    }

    @Override
    protected BehaviorController.b<EntityZoglin> brainProvider() {
        return BehaviorController.<EntityZoglin>provider(EntityZoglin.MEMORY_TYPES, EntityZoglin.SENSOR_TYPES);
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        BehaviorController<EntityZoglin> behaviorcontroller = this.brainProvider().makeBrain(dynamic);

        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initFightActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(ImmutableSet.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    private static void initCoreActivity(BehaviorController<EntityZoglin> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorLook(45, 90), new BehavorMove()));
    }

    private static void initIdleActivity(BehaviorController<EntityZoglin> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, 10, ImmutableList.of(BehaviorAttackTargetSet.create((worldserver, entityzoglin) -> {
            return entityzoglin.findNearestValidAttackTarget(worldserver);
        }), SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)), new BehaviorGateSingle(ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(0.4F), 2), Pair.of(BehaviorLookWalk.create(0.4F, 3), 2), Pair.of(new BehaviorNop(30, 60), 1)))));
    }

    private static void initFightActivity(BehaviorController<EntityZoglin> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(BehaviorWalkAwayOutOfRange.create(1.0F), BehaviorBuilder.triggerIf(EntityZoglin::isAdult, BehaviorAttack.create(40)), BehaviorBuilder.triggerIf(EntityZoglin::isBaby, BehaviorAttack.create(15)), BehaviorAttackTargetForget.create()), MemoryModuleType.ATTACK_TARGET);
    }

    private Optional<? extends EntityLiving> findNearestValidAttackTarget(WorldServer worldserver) {
        return ((NearestVisibleLivingEntities) this.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty())).findClosest((entityliving) -> {
            return this.isTargetable(worldserver, entityliving);
        });
    }

    private boolean isTargetable(WorldServer worldserver, EntityLiving entityliving) {
        EntityTypes<?> entitytypes = entityliving.getType();

        return entitytypes != EntityTypes.ZOGLIN && entitytypes != EntityTypes.CREEPER && Sensor.isEntityAttackable(worldserver, this, entityliving);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityZoglin.DATA_BABY_ID, false);
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (EntityZoglin.DATA_BABY_ID.equals(datawatcherobject)) {
            this.refreshDimensions();
        }

    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        if (worldaccess.getRandom().nextFloat() < 0.2F) {
            this.setBaby(true);
        }

        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityMonster.createMonsterAttributes().add(GenericAttributes.MAX_HEALTH, 40.0D).add(GenericAttributes.MOVEMENT_SPEED, (double) 0.3F).add(GenericAttributes.KNOCKBACK_RESISTANCE, (double) 0.6F).add(GenericAttributes.ATTACK_KNOCKBACK, 1.0D).add(GenericAttributes.ATTACK_DAMAGE, 6.0D);
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    @Override
    public boolean doHurtTarget(WorldServer worldserver, Entity entity) {
        if (entity instanceof EntityLiving entityliving) {
            this.attackAnimationRemainingTicks = 10;
            worldserver.broadcastEntityEvent(this, (byte) 4);
            this.makeSound(SoundEffects.ZOGLIN_ATTACK);
            return IOglin.hurtAndThrowTarget(worldserver, this, entityliving);
        } else {
            return false;
        }
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    protected void blockedByItem(EntityLiving entityliving) {
        if (!this.isBaby()) {
            IOglin.throwTarget(this, entityliving);
        }

    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        boolean flag = super.hurtServer(worldserver, damagesource, f);

        if (flag) {
            Entity entity = damagesource.getEntity();

            if (entity instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) entity;

                if (this.canAttack(entityliving) && !BehaviorUtil.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(this, entityliving, 4.0D)) {
                    this.setAttackTarget(entityliving);
                }

                return true;
            }
        }

        return flag;
    }

    private void setAttackTarget(EntityLiving entityliving) {
        this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        this.brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, entityliving, 200L);
    }

    @Override
    public BehaviorController<EntityZoglin> getBrain() {
        return super.getBrain();
    }

    protected void updateActivity() {
        Activity activity = (Activity) this.brain.getActiveNonCoreActivity().orElse((Object) null);

        this.brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        Activity activity1 = (Activity) this.brain.getActiveNonCoreActivity().orElse((Object) null);

        if (activity1 == Activity.FIGHT && activity != Activity.FIGHT) {
            this.playAngrySound();
        }

        this.setAggressive(this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("zoglinBrain");
        this.getBrain().tick(worldserver, this);
        gameprofilerfiller.pop();
        this.updateActivity();
    }

    @Override
    public void setBaby(boolean flag) {
        this.getEntityData().set(EntityZoglin.DATA_BABY_ID, flag);
        if (!this.level().isClientSide && flag) {
            this.getAttribute(GenericAttributes.ATTACK_DAMAGE).setBaseValue(0.5D);
        }

    }

    @Override
    public boolean isBaby() {
        return (Boolean) this.getEntityData().get(EntityZoglin.DATA_BABY_ID);
    }

    @Override
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }

        super.aiStep();
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 4) {
            this.attackAnimationRemainingTicks = 10;
            this.makeSound(SoundEffects.ZOGLIN_ATTACK);
        } else {
            super.handleEntityEvent(b0);
        }

    }

    @Override
    public int getAttackAnimationRemainingTicks() {
        return this.attackAnimationRemainingTicks;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return this.level().isClientSide ? null : (this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? SoundEffects.ZOGLIN_ANGRY : SoundEffects.ZOGLIN_AMBIENT);
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.ZOGLIN_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.ZOGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.ZOGLIN_STEP, 0.15F, 1.0F);
    }

    protected void playAngrySound() {
        this.makeSound(SoundEffects.ZOGLIN_ANGRY);
    }

    @Nullable
    @Override
    public EntityLiving getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putBoolean("IsBaby", this.isBaby());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setBaby(nbttagcompound.getBooleanOr("IsBaby", false));
    }
}
