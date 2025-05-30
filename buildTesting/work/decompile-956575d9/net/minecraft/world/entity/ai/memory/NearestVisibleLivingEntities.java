package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class NearestVisibleLivingEntities {

    private static final NearestVisibleLivingEntities EMPTY = new NearestVisibleLivingEntities();
    private final List<EntityLiving> nearbyEntities;
    private final Predicate<EntityLiving> lineOfSightTest;

    private NearestVisibleLivingEntities() {
        this.nearbyEntities = List.of();
        this.lineOfSightTest = (entityliving) -> {
            return false;
        };
    }

    public NearestVisibleLivingEntities(WorldServer worldserver, EntityLiving entityliving, List<EntityLiving> list) {
        this.nearbyEntities = list;
        Object2BooleanOpenHashMap<EntityLiving> object2booleanopenhashmap = new Object2BooleanOpenHashMap(list.size());
        Predicate<EntityLiving> predicate = (entityliving1) -> {
            return Sensor.isEntityTargetable(worldserver, entityliving, entityliving1);
        };

        this.lineOfSightTest = (entityliving1) -> {
            return object2booleanopenhashmap.computeIfAbsent(entityliving1, predicate);
        };
    }

    public static NearestVisibleLivingEntities empty() {
        return NearestVisibleLivingEntities.EMPTY;
    }

    public Optional<EntityLiving> findClosest(Predicate<EntityLiving> predicate) {
        for (EntityLiving entityliving : this.nearbyEntities) {
            if (predicate.test(entityliving) && this.lineOfSightTest.test(entityliving)) {
                return Optional.of(entityliving);
            }
        }

        return Optional.empty();
    }

    public Iterable<EntityLiving> findAll(Predicate<EntityLiving> predicate) {
        return Iterables.filter(this.nearbyEntities, (entityliving) -> {
            return predicate.test(entityliving) && this.lineOfSightTest.test(entityliving);
        });
    }

    public Stream<EntityLiving> find(Predicate<EntityLiving> predicate) {
        return this.nearbyEntities.stream().filter((entityliving) -> {
            return predicate.test(entityliving) && this.lineOfSightTest.test(entityliving);
        });
    }

    public boolean contains(EntityLiving entityliving) {
        return this.nearbyEntities.contains(entityliving) && this.lineOfSightTest.test(entityliving);
    }

    public boolean contains(Predicate<EntityLiving> predicate) {
        for (EntityLiving entityliving : this.nearbyEntities) {
            if (predicate.test(entityliving) && this.lineOfSightTest.test(entityliving)) {
                return true;
            }
        }

        return false;
    }
}
