package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.level.ChunkCache;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Pathfinder;
import net.minecraft.world.level.pathfinder.PathfinderAbstract;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public abstract class NavigationAbstract {

    private static final int MAX_TIME_RECOMPUTE = 20;
    private static final int STUCK_CHECK_INTERVAL = 100;
    private static final float STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25F;
    protected final EntityInsentient mob;
    protected final World level;
    @Nullable
    protected PathEntity path;
    protected double speedModifier;
    protected int tick;
    protected int lastStuckCheck;
    protected Vec3D lastStuckCheckPos;
    protected BaseBlockPosition timeoutCachedNode;
    protected long timeoutTimer;
    protected long lastTimeoutCheck;
    protected double timeoutLimit;
    protected float maxDistanceToWaypoint;
    protected boolean hasDelayedRecomputation;
    protected long timeLastRecompute;
    protected PathfinderAbstract nodeEvaluator;
    @Nullable
    private BlockPosition targetPos;
    private int reachRange;
    private float maxVisitedNodesMultiplier;
    private final Pathfinder pathFinder;
    private boolean isStuck;
    private float requiredPathLength;

    public NavigationAbstract(EntityInsentient entityinsentient, World world) {
        this.lastStuckCheckPos = Vec3D.ZERO;
        this.timeoutCachedNode = BaseBlockPosition.ZERO;
        this.maxDistanceToWaypoint = 0.5F;
        this.maxVisitedNodesMultiplier = 1.0F;
        this.requiredPathLength = 16.0F;
        this.mob = entityinsentient;
        this.level = world;
        this.pathFinder = this.createPathFinder(MathHelper.floor(entityinsentient.getAttributeBaseValue(GenericAttributes.FOLLOW_RANGE) * 16.0D));
    }

    public void updatePathfinderMaxVisitedNodes() {
        int i = MathHelper.floor(this.getMaxPathLength() * 16.0F);

        this.pathFinder.setMaxVisitedNodes(i);
    }

    public void setRequiredPathLength(float f) {
        this.requiredPathLength = f;
        this.updatePathfinderMaxVisitedNodes();
    }

    private float getMaxPathLength() {
        return Math.max((float) this.mob.getAttributeValue(GenericAttributes.FOLLOW_RANGE), this.requiredPathLength);
    }

    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0F;
    }

    public void setMaxVisitedNodesMultiplier(float f) {
        this.maxVisitedNodesMultiplier = f;
    }

    @Nullable
    public BlockPosition getTargetPos() {
        return this.targetPos;
    }

    protected abstract Pathfinder createPathFinder(int i);

    public void setSpeedModifier(double d0) {
        this.speedModifier = d0;
    }

    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = this.createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
            }
        } else {
            this.hasDelayedRecomputation = true;
        }

    }

    @Nullable
    public final PathEntity createPath(double d0, double d1, double d2, int i) {
        return this.createPath(BlockPosition.containing(d0, d1, d2), i);
    }

    @Nullable
    public PathEntity createPath(Stream<BlockPosition> stream, int i) {
        return this.createPath((Set) stream.collect(Collectors.toSet()), 8, false, i);
    }

    @Nullable
    public PathEntity createPath(Set<BlockPosition> set, int i) {
        return this.createPath(set, 8, false, i);
    }

    @Nullable
    public PathEntity createPath(BlockPosition blockposition, int i) {
        return this.createPath(ImmutableSet.of(blockposition), 8, false, i);
    }

    @Nullable
    public PathEntity createPath(BlockPosition blockposition, int i, int j) {
        return this.createPath(ImmutableSet.of(blockposition), 8, false, i, (float) j);
    }

    @Nullable
    public PathEntity createPath(Entity entity, int i) {
        return this.createPath(ImmutableSet.of(entity.blockPosition()), 16, true, i);
    }

    @Nullable
    protected PathEntity createPath(Set<BlockPosition> set, int i, boolean flag, int j) {
        return this.createPath(set, i, flag, j, this.getMaxPathLength());
    }

    @Nullable
    protected PathEntity createPath(Set<BlockPosition> set, int i, boolean flag, int j, float f) {
        if (set.isEmpty()) {
            return null;
        } else if (this.mob.getY() < (double) this.level.getMinY()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && set.contains(this.targetPos)) {
            return this.path;
        } else {
            GameProfilerFiller gameprofilerfiller = Profiler.get();

            gameprofilerfiller.push("pathfind");
            BlockPosition blockposition = flag ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int k = (int) (f + (float) i);
            ChunkCache chunkcache = new ChunkCache(this.level, blockposition.offset(-k, -k, -k), blockposition.offset(k, k, k));
            PathEntity pathentity = this.pathFinder.findPath(chunkcache, this.mob, set, f, j, this.maxVisitedNodesMultiplier);

            gameprofilerfiller.pop();
            if (pathentity != null && pathentity.getTarget() != null) {
                this.targetPos = pathentity.getTarget();
                this.reachRange = j;
                this.resetStuckTimeout();
            }

            return pathentity;
        }
    }

    public boolean moveTo(double d0, double d1, double d2, double d3) {
        return this.moveTo(this.createPath(d0, d1, d2, 1), d3);
    }

    public boolean moveTo(double d0, double d1, double d2, int i, double d3) {
        return this.moveTo(this.createPath(d0, d1, d2, i), d3);
    }

    public boolean moveTo(Entity entity, double d0) {
        PathEntity pathentity = this.createPath(entity, 1);

        return pathentity != null && this.moveTo(pathentity, d0);
    }

    public boolean moveTo(@Nullable PathEntity pathentity, double d0) {
        if (pathentity == null) {
            this.path = null;
            return false;
        } else {
            if (!pathentity.sameAs(this.path)) {
                this.path = pathentity;
            }

            if (this.isDone()) {
                return false;
            } else {
                this.trimPath();
                if (this.path.getNodeCount() <= 0) {
                    return false;
                } else {
                    this.speedModifier = d0;
                    Vec3D vec3d = this.getTempMobPos();

                    this.lastStuckCheck = this.tick;
                    this.lastStuckCheckPos = vec3d;
                    return true;
                }
            }
        }
    }

    @Nullable
    public PathEntity getPath() {
        return this.path;
    }

    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vec3D vec3d = this.getTempMobPos();
                Vec3D vec3d1 = this.path.getNextEntityPos(this.mob);

                if (vec3d.y > vec3d1.y && !this.mob.onGround() && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
                    this.path.advance();
                }
            }

            PacketDebug.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vec3D vec3d2 = this.path.getNextEntityPos(this.mob);

                this.mob.getMoveControl().setWantedPosition(vec3d2.x, this.getGroundY(vec3d2), vec3d2.z, this.speedModifier);
            }
        }
    }

    protected double getGroundY(Vec3D vec3d) {
        BlockPosition blockposition = BlockPosition.containing(vec3d);

        return this.level.getBlockState(blockposition.below()).isAir() ? vec3d.y : PathfinderNormal.getFloorLevel(this.level, blockposition);
    }

    protected void followThePath() {
        Vec3D vec3d = this.getTempMobPos();

        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        BaseBlockPosition baseblockposition = this.path.getNextNodePos();
        double d0 = Math.abs(this.mob.getX() - ((double) baseblockposition.getX() + 0.5D));
        double d1 = Math.abs(this.mob.getY() - (double) baseblockposition.getY());
        double d2 = Math.abs(this.mob.getZ() - ((double) baseblockposition.getZ() + 0.5D));
        boolean flag = d0 < (double) this.maxDistanceToWaypoint && d2 < (double) this.maxDistanceToWaypoint && d1 < 1.0D;

        if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3d)) {
            this.path.advance();
        }

        this.doStuckDetection(vec3d);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3D vec3d) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3D vec3d1 = Vec3D.atBottomCenterOf(this.path.getNextNodePos());

            if (!vec3d.closerThan(vec3d1, 2.0D)) {
                return false;
            } else if (this.canMoveDirectly(vec3d, this.path.getNextEntityPos(this.mob))) {
                return true;
            } else {
                Vec3D vec3d2 = Vec3D.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3D vec3d3 = vec3d1.subtract(vec3d);
                Vec3D vec3d4 = vec3d2.subtract(vec3d);
                double d0 = vec3d3.lengthSqr();
                double d1 = vec3d4.lengthSqr();
                boolean flag = d1 < d0;
                boolean flag1 = d0 < 0.5D;

                if (!flag && !flag1) {
                    return false;
                } else {
                    Vec3D vec3d5 = vec3d3.normalize();
                    Vec3D vec3d6 = vec3d4.normalize();

                    return vec3d6.dot(vec3d5) < 0.0D;
                }
            }
        }
    }

    protected void doStuckDetection(Vec3D vec3d) {
        if (this.tick - this.lastStuckCheck > 100) {
            float f = this.mob.getSpeed() >= 1.0F ? this.mob.getSpeed() : this.mob.getSpeed() * this.mob.getSpeed();
            float f1 = f * 100.0F * 0.25F;

            if (vec3d.distanceToSqr(this.lastStuckCheckPos) < (double) (f1 * f1)) {
                this.isStuck = true;
                this.stop();
            } else {
                this.isStuck = false;
            }

            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = vec3d;
        }

        if (this.path != null && !this.path.isDone()) {
            BaseBlockPosition baseblockposition = this.path.getNextNodePos();
            long i = this.level.getGameTime();

            if (baseblockposition.equals(this.timeoutCachedNode)) {
                this.timeoutTimer += i - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = baseblockposition;
                double d0 = vec3d.distanceTo(Vec3D.atBottomCenterOf(this.timeoutCachedNode));

                this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d0 / (double) this.mob.getSpeed() * 20.0D : 0.0D;
            }

            if (this.timeoutLimit > 0.0D && (double) this.timeoutTimer > this.timeoutLimit * 3.0D) {
                this.timeoutPath();
            }

            this.lastTimeoutCheck = i;
        }

    }

    private void timeoutPath() {
        this.resetStuckTimeout();
        this.stop();
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = BaseBlockPosition.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0D;
        this.isStuck = false;
    }

    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    public boolean isInProgress() {
        return !this.isDone();
    }

    public void stop() {
        this.path = null;
    }

    protected abstract Vec3D getTempMobPos();

    protected abstract boolean canUpdatePath();

    protected void trimPath() {
        if (this.path != null) {
            for (int i = 0; i < this.path.getNodeCount(); ++i) {
                PathPoint pathpoint = this.path.getNode(i);
                PathPoint pathpoint1 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
                IBlockData iblockdata = this.level.getBlockState(new BlockPosition(pathpoint.x, pathpoint.y, pathpoint.z));

                if (iblockdata.is(TagsBlock.CAULDRONS)) {
                    this.path.replaceNode(i, pathpoint.cloneAndMove(pathpoint.x, pathpoint.y + 1, pathpoint.z));
                    if (pathpoint1 != null && pathpoint.y >= pathpoint1.y) {
                        this.path.replaceNode(i + 1, pathpoint.cloneAndMove(pathpoint1.x, pathpoint.y + 1, pathpoint1.z));
                    }
                }
            }

        }
    }

    protected boolean canMoveDirectly(Vec3D vec3d, Vec3D vec3d1) {
        return false;
    }

    public boolean canCutCorner(PathType pathtype) {
        return pathtype != PathType.DANGER_FIRE && pathtype != PathType.DANGER_OTHER && pathtype != PathType.WALKABLE_DOOR;
    }

    protected static boolean isClearForMovementBetween(EntityInsentient entityinsentient, Vec3D vec3d, Vec3D vec3d1, boolean flag) {
        Vec3D vec3d2 = new Vec3D(vec3d1.x, vec3d1.y + (double) entityinsentient.getBbHeight() * 0.5D, vec3d1.z);

        return entityinsentient.level().clip(new RayTrace(vec3d, vec3d2, RayTrace.BlockCollisionOption.COLLIDER, flag ? RayTrace.FluidCollisionOption.ANY : RayTrace.FluidCollisionOption.NONE, entityinsentient)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS;
    }

    public boolean isStableDestination(BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();

        return this.level.getBlockState(blockposition1).isSolidRender();
    }

    public PathfinderAbstract getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    public void setCanFloat(boolean flag) {
        this.nodeEvaluator.setCanFloat(flag);
    }

    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    public boolean shouldRecomputePath(BlockPosition blockposition) {
        if (this.hasDelayedRecomputation) {
            return false;
        } else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            PathPoint pathpoint = this.path.getEndNode();
            Vec3D vec3d = new Vec3D(((double) pathpoint.x + this.mob.getX()) / 2.0D, ((double) pathpoint.y + this.mob.getY()) / 2.0D, ((double) pathpoint.z + this.mob.getZ()) / 2.0D);

            return blockposition.closerToCenterThan(vec3d, (double) (this.path.getNodeCount() - this.path.getNextNodeIndex()));
        } else {
            return false;
        }
    }

    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
    }

    public boolean isStuck() {
        return this.isStuck;
    }
}
