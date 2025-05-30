package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.SystemUtils;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.player.EntityHuman;

public class FollowTemptation extends Behavior<EntityCreature> {

    public static final int TEMPTATION_COOLDOWN = 100;
    public static final double DEFAULT_CLOSE_ENOUGH_DIST = 2.5D;
    public static final double BACKED_UP_CLOSE_ENOUGH_DIST = 3.5D;
    private final Function<EntityLiving, Float> speedModifier;
    private final Function<EntityLiving, Double> closeEnoughDistance;

    public FollowTemptation(Function<EntityLiving, Float> function) {
        this(function, (entityliving) -> {
            return 2.5D;
        });
    }

    public FollowTemptation(Function<EntityLiving, Float> function, Function<EntityLiving, Double> function1) {
        super((Map) SystemUtils.make(() -> {
            ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> immutablemap_builder = ImmutableMap.builder();

            immutablemap_builder.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
            immutablemap_builder.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
            immutablemap_builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT);
            immutablemap_builder.put(MemoryModuleType.IS_TEMPTED, MemoryStatus.REGISTERED);
            immutablemap_builder.put(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_PRESENT);
            immutablemap_builder.put(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT);
            immutablemap_builder.put(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT);
            return immutablemap_builder.build();
        }));
        this.speedModifier = function;
        this.closeEnoughDistance = function1;
    }

    protected float getSpeedModifier(EntityCreature entitycreature) {
        return (Float) this.speedModifier.apply(entitycreature);
    }

    private Optional<EntityHuman> getTemptingPlayer(EntityCreature entitycreature) {
        return entitycreature.getBrain().<EntityHuman>getMemory(MemoryModuleType.TEMPTING_PLAYER);
    }

    @Override
    protected boolean timedOut(long i) {
        return false;
    }

    protected boolean canStillUse(WorldServer worldserver, EntityCreature entitycreature, long i) {
        return this.getTemptingPlayer(entitycreature).isPresent() && !entitycreature.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET) && !entitycreature.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    protected void start(WorldServer worldserver, EntityCreature entitycreature, long i) {
        entitycreature.getBrain().setMemory(MemoryModuleType.IS_TEMPTED, true);
    }

    protected void stop(WorldServer worldserver, EntityCreature entitycreature, long i) {
        BehaviorController<?> behaviorcontroller = entitycreature.getBrain();

        behaviorcontroller.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        behaviorcontroller.setMemory(MemoryModuleType.IS_TEMPTED, false);
        behaviorcontroller.eraseMemory(MemoryModuleType.WALK_TARGET);
        behaviorcontroller.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(WorldServer worldserver, EntityCreature entitycreature, long i) {
        EntityHuman entityhuman = (EntityHuman) this.getTemptingPlayer(entitycreature).get();
        BehaviorController<?> behaviorcontroller = entitycreature.getBrain();

        behaviorcontroller.setMemory(MemoryModuleType.LOOK_TARGET, new BehaviorPositionEntity(entityhuman, true));
        double d0 = (Double) this.closeEnoughDistance.apply(entitycreature);

        if (entitycreature.distanceToSqr((Entity) entityhuman) < MathHelper.square(d0)) {
            behaviorcontroller.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else {
            behaviorcontroller.setMemory(MemoryModuleType.WALK_TARGET, new MemoryTarget(new BehaviorPositionEntity(entityhuman, false), this.getSpeedModifier(entitycreature), 2));
        }

    }
}
