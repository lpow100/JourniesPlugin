package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.phys.Vec3D;

public abstract class EntityMonsterPatrolling extends EntityMonster {

    private static final boolean DEFAULT_PATROL_LEADER = false;
    private static final boolean DEFAULT_PATROLLING = false;
    @Nullable
    private BlockPosition patrolTarget;
    private boolean patrolLeader = false;
    private boolean patrolling = false;

    protected EntityMonsterPatrolling(EntityTypes<? extends EntityMonsterPatrolling> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new EntityMonsterPatrolling.a(this, 0.7D, 0.595D));
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.storeNullable("patrol_target", BlockPosition.CODEC, this.patrolTarget);
        nbttagcompound.putBoolean("PatrolLeader", this.patrolLeader);
        nbttagcompound.putBoolean("Patrolling", this.patrolling);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.patrolTarget = (BlockPosition) nbttagcompound.read("patrol_target", BlockPosition.CODEC).orElse((Object) null);
        this.patrolLeader = nbttagcompound.getBooleanOr("PatrolLeader", false);
        this.patrolling = nbttagcompound.getBooleanOr("Patrolling", false);
    }

    public boolean canBeLeader() {
        return true;
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        if (entityspawnreason != EntitySpawnReason.PATROL && entityspawnreason != EntitySpawnReason.EVENT && entityspawnreason != EntitySpawnReason.STRUCTURE && worldaccess.getRandom().nextFloat() < 0.06F && this.canBeLeader()) {
            this.patrolLeader = true;
        }

        if (this.isPatrolLeader()) {
            this.setItemSlot(EnumItemSlot.HEAD, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
            this.setDropChance(EnumItemSlot.HEAD, 2.0F);
        }

        if (entityspawnreason == EntitySpawnReason.PATROL) {
            this.patrolling = true;
        }

        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    public static boolean checkPatrollingMonsterSpawnRules(EntityTypes<? extends EntityMonsterPatrolling> entitytypes, GeneratorAccess generatoraccess, EntitySpawnReason entityspawnreason, BlockPosition blockposition, RandomSource randomsource) {
        return generatoraccess.getBrightness(EnumSkyBlock.BLOCK, blockposition) > 8 ? false : checkAnyLightMonsterSpawnRules(entitytypes, generatoraccess, entityspawnreason, blockposition, randomsource);
    }

    @Override
    public boolean removeWhenFarAway(double d0) {
        return !this.patrolling || d0 > 16384.0D;
    }

    public void setPatrolTarget(BlockPosition blockposition) {
        this.patrolTarget = blockposition;
        this.patrolling = true;
    }

    public BlockPosition getPatrolTarget() {
        return this.patrolTarget;
    }

    public boolean hasPatrolTarget() {
        return this.patrolTarget != null;
    }

    public void setPatrolLeader(boolean flag) {
        this.patrolLeader = flag;
        this.patrolling = true;
    }

    public boolean isPatrolLeader() {
        return this.patrolLeader;
    }

    public boolean canJoinPatrol() {
        return true;
    }

    public void findPatrolTarget() {
        this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
        this.patrolling = true;
    }

    protected boolean isPatrolling() {
        return this.patrolling;
    }

    protected void setPatrolling(boolean flag) {
        this.patrolling = flag;
    }

    public static class a<T extends EntityMonsterPatrolling> extends PathfinderGoal {

        private static final int NAVIGATION_FAILED_COOLDOWN = 200;
        private final T mob;
        private final double speedModifier;
        private final double leaderSpeedModifier;
        private long cooldownUntil;

        public a(T t0, double d0, double d1) {
            this.mob = t0;
            this.speedModifier = d0;
            this.leaderSpeedModifier = d1;
            this.cooldownUntil = -1L;
            this.setFlags(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean canUse() {
            boolean flag = this.mob.level().getGameTime() < this.cooldownUntil;

            return this.mob.isPatrolling() && this.mob.getTarget() == null && !this.mob.hasControllingPassenger() && this.mob.hasPatrolTarget() && !flag;
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void tick() {
            boolean flag = this.mob.isPatrolLeader();
            NavigationAbstract navigationabstract = this.mob.getNavigation();

            if (navigationabstract.isDone()) {
                List<EntityMonsterPatrolling> list = this.findPatrolCompanions();

                if (this.mob.isPatrolling() && list.isEmpty()) {
                    this.mob.setPatrolling(false);
                } else if (flag && this.mob.getPatrolTarget().closerToCenterThan(this.mob.position(), 10.0D)) {
                    this.mob.findPatrolTarget();
                } else {
                    Vec3D vec3d = Vec3D.atBottomCenterOf(this.mob.getPatrolTarget());
                    Vec3D vec3d1 = this.mob.position();
                    Vec3D vec3d2 = vec3d1.subtract(vec3d);

                    vec3d = vec3d2.yRot(90.0F).scale(0.4D).add(vec3d);
                    Vec3D vec3d3 = vec3d.subtract(vec3d1).normalize().scale(10.0D).add(vec3d1);
                    BlockPosition blockposition = BlockPosition.containing(vec3d3);

                    blockposition = this.mob.level().getHeightmapPos(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, blockposition);
                    if (!navigationabstract.moveTo((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), flag ? this.leaderSpeedModifier : this.speedModifier)) {
                        this.moveRandomly();
                        this.cooldownUntil = this.mob.level().getGameTime() + 200L;
                    } else if (flag) {
                        for (EntityMonsterPatrolling entitymonsterpatrolling : list) {
                            entitymonsterpatrolling.setPatrolTarget(blockposition);
                        }
                    }
                }
            }

        }

        private List<EntityMonsterPatrolling> findPatrolCompanions() {
            return this.mob.level().<EntityMonsterPatrolling>getEntitiesOfClass(EntityMonsterPatrolling.class, this.mob.getBoundingBox().inflate(16.0D), (entitymonsterpatrolling) -> {
                return entitymonsterpatrolling.canJoinPatrol() && !entitymonsterpatrolling.is(this.mob);
            });
        }

        private boolean moveRandomly() {
            RandomSource randomsource = this.mob.getRandom();
            BlockPosition blockposition = this.mob.level().getHeightmapPos(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + randomsource.nextInt(16), 0, -8 + randomsource.nextInt(16)));

            return this.mob.getNavigation().moveTo((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), this.speedModifier);
        }
    }
}
