package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.hoglin.EntityHoglin;
import net.minecraft.world.entity.monster.piglin.EntityPiglin;

public class SensorHoglinSpecific extends Sensor<EntityHoglin> {

    public SensorHoglinSpecific() {}

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, new MemoryModuleType[0]);
    }

    protected void doTick(WorldServer worldserver, EntityHoglin entityhoglin) {
        BehaviorController<?> behaviorcontroller = entityhoglin.getBrain();

        behaviorcontroller.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(worldserver, entityhoglin));
        Optional<EntityPiglin> optional = Optional.empty();
        int i = 0;
        List<EntityHoglin> list = Lists.newArrayList();
        NearestVisibleLivingEntities nearestvisiblelivingentities = (NearestVisibleLivingEntities) behaviorcontroller.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (EntityLiving entityliving : nearestvisiblelivingentities.findAll((entityliving1) -> {
            return !entityliving1.isBaby() && (entityliving1 instanceof EntityPiglin || entityliving1 instanceof EntityHoglin);
        })) {
            if (entityliving instanceof EntityPiglin entitypiglin) {
                ++i;
                if (optional.isEmpty()) {
                    optional = Optional.of(entitypiglin);
                }
            }

            if (entityliving instanceof EntityHoglin entityhoglin1) {
                list.add(entityhoglin1);
            }
        }

        behaviorcontroller.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
        behaviorcontroller.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, list);
        behaviorcontroller.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, i);
        behaviorcontroller.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, list.size());
    }

    private Optional<BlockPosition> findNearestRepellent(WorldServer worldserver, EntityHoglin entityhoglin) {
        return BlockPosition.findClosestMatch(entityhoglin.blockPosition(), 8, 4, (blockposition) -> {
            return worldserver.getBlockState(blockposition).is(TagsBlock.HOGLIN_REPELLENTS);
        });
    }
}
