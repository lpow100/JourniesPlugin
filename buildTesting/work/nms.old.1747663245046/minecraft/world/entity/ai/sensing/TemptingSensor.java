package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
// CraftBukkit end

public class TemptingSensor extends Sensor<EntityCreature> {

    private static final PathfinderTargetCondition TEMPT_TARGETING = PathfinderTargetCondition.forNonCombat().ignoreLineOfSight();
    private final Predicate<ItemStack> temptations;

    public TemptingSensor(Predicate<ItemStack> predicate) {
        this.temptations = predicate;
    }

    protected void doTick(WorldServer worldserver, EntityCreature entitycreature) {
        BehaviorController<?> behaviorcontroller = entitycreature.getBrain();
        PathfinderTargetCondition pathfindertargetcondition = TemptingSensor.TEMPT_TARGETING.copy().range((double) ((float) entitycreature.getAttributeValue(GenericAttributes.TEMPT_RANGE)));
        Stream<net.minecraft.server.level.EntityPlayer> stream = worldserver.players().stream().filter(IEntitySelector.NO_SPECTATORS).filter((entityplayer) -> { // CraftBukkit - decompile error
            return pathfindertargetcondition.test(worldserver, entitycreature, entityplayer);
        }).filter(this::playerHoldingTemptation).filter((entityplayer) -> {
            return !entitycreature.hasPassenger(entityplayer);
        });

        Objects.requireNonNull(entitycreature);
        List<EntityHuman> list = (List) stream.sorted(Comparator.comparingDouble(entitycreature::distanceToSqr)).collect(Collectors.toList());

        if (!list.isEmpty()) {
            EntityHuman entityhuman = (EntityHuman) list.get(0);

            // CraftBukkit start
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(entitycreature, entityhuman, EntityTargetEvent.TargetReason.TEMPT);
            if (event.isCancelled()) {
                return;
            }
            if (event.getTarget() instanceof HumanEntity) {
                behaviorcontroller.setMemory(MemoryModuleType.TEMPTING_PLAYER, ((CraftHumanEntity) event.getTarget()).getHandle());
            } else {
                behaviorcontroller.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
            }
            // CraftBukkit end
        } else {
            behaviorcontroller.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }

    }

    private boolean playerHoldingTemptation(EntityHuman entityhuman) {
        return this.isTemptation(entityhuman.getMainHandItem()) || this.isTemptation(entityhuman.getOffhandItem());
    }

    private boolean isTemptation(ItemStack itemstack) {
        return this.temptations.test(itemstack);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }
}
