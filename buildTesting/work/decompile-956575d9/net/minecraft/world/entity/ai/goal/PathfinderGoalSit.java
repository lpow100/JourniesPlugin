package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTameableAnimal;

public class PathfinderGoalSit extends PathfinderGoal {

    private final EntityTameableAnimal mob;

    public PathfinderGoalSit(EntityTameableAnimal entitytameableanimal) {
        this.mob = entitytameableanimal;
        this.setFlags(EnumSet.of(PathfinderGoal.Type.JUMP, PathfinderGoal.Type.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isOrderedToSit();
    }

    @Override
    public boolean canUse() {
        boolean flag = this.mob.isOrderedToSit();

        if (!flag && !this.mob.isTame()) {
            return false;
        } else if (this.mob.isInWater()) {
            return false;
        } else if (!this.mob.onGround()) {
            return false;
        } else {
            EntityLiving entityliving = this.mob.getOwner();

            return entityliving == null ? true : (this.mob.distanceToSqr((Entity) entityliving) < 144.0D && entityliving.getLastHurtByMob() != null ? false : flag);
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.mob.setInSittingPose(false);
    }
}
