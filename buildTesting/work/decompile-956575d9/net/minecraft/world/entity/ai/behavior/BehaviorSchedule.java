package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class BehaviorSchedule {

    public BehaviorSchedule() {}

    public static BehaviorControl<EntityLiving> create() {
        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.point((Trigger) (worldserver, entityliving, i) -> {
                entityliving.getBrain().updateActivityFromSchedule(worldserver.getDayTime(), worldserver.getGameTime());
                return true;
            });
        });
    }
}
