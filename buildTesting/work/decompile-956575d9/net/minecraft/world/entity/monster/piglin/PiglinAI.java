package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeRange;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.BehaviorAttack;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetForget;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetSet;
import net.minecraft.world.entity.ai.behavior.BehaviorCelebrateDeath;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorCrossbowAttack;
import net.minecraft.world.entity.ai.behavior.BehaviorExpirableMemory;
import net.minecraft.world.entity.ai.behavior.BehaviorFindAdmirableItem;
import net.minecraft.world.entity.ai.behavior.BehaviorForgetAnger;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorInteract;
import net.minecraft.world.entity.ai.behavior.BehaviorInteractDoor;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookInteract;
import net.minecraft.world.entity.ai.behavior.BehaviorLookTarget;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorNop;
import net.minecraft.world.entity.ai.behavior.BehaviorRemoveMemory;
import net.minecraft.world.entity.ai.behavior.BehaviorRetreat;
import net.minecraft.world.entity.ai.behavior.BehaviorStartRiding;
import net.minecraft.world.entity.ai.behavior.BehaviorStopRiding;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorUtil;
import net.minecraft.world.entity.ai.behavior.BehaviorWalkAway;
import net.minecraft.world.entity.ai.behavior.BehaviorWalkAwayOutOfRange;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.hoglin.EntityHoglin;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;

public class PiglinAI {

    public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
    public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
    public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
    private static final int PLAYER_ANGER_RANGE = 16;
    private static final int ANGER_DURATION = 600;
    private static final int ADMIRE_DURATION = 119;
    private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
    private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
    private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
    private static final int CELEBRATION_TIME = 300;
    protected static final UniformInt TIME_BETWEEN_HUNTS = TimeRange.rangeOfSeconds(30, 120);
    private static final int BABY_FLEE_DURATION_AFTER_GETTING_HIT = 100;
    private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
    private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
    private static final UniformInt RIDE_START_INTERVAL = TimeRange.rangeOfSeconds(10, 40);
    private static final UniformInt RIDE_DURATION = TimeRange.rangeOfSeconds(10, 30);
    private static final UniformInt RETREAT_DURATION = TimeRange.rangeOfSeconds(5, 20);
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final int EAT_COOLDOWN = 200;
    private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    private static final int MAX_LOOK_DIST = 8;
    private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
    private static final int INTERACTION_RANGE = 8;
    private static final int MIN_DESIRED_DIST_FROM_TARGET_WHEN_HOLDING_CROSSBOW = 5;
    private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75F;
    private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
    private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeRange.rangeOfSeconds(5, 7);
    private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeRange.rangeOfSeconds(5, 7);
    private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1F;
    private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;

    public PiglinAI() {}

    protected static BehaviorController<?> makeBrain(EntityPiglin entitypiglin, BehaviorController<EntityPiglin> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initAdmireItemActivity(behaviorcontroller);
        initFightActivity(entitypiglin, behaviorcontroller);
        initCelebrateActivity(behaviorcontroller);
        initRetreatActivity(behaviorcontroller);
        initRideHoglinActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(ImmutableSet.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    protected static void initMemories(EntityPiglin entitypiglin, RandomSource randomsource) {
        int i = PiglinAI.TIME_BETWEEN_HUNTS.sample(randomsource);

        entitypiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long) i);
    }

    private static void initCoreActivity(BehaviorController<EntityPiglin> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorLook(45, 90), new BehavorMove(), BehaviorInteractDoor.create(), babyAvoidNemesis(), avoidZombified(), BehaviorStopAdmiring.create(), BehaviorStartAdmiringItem.create(119), BehaviorCelebrateDeath.create(300, PiglinAI::wantsToDance), BehaviorForgetAnger.create()));
    }

    private static void initIdleActivity(BehaviorController<EntityPiglin> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, 10, ImmutableList.of(BehaviorLookTarget.create(PiglinAI::isPlayerHoldingLovedItem, 14.0F), BehaviorAttackTargetSet.create((worldserver, entitypiglin) -> {
            return entitypiglin.isAdult();
        }, PiglinAI::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(EntityPiglin::canHunt, BehaviorHuntHoglin.create()), avoidRepellent(), babySometimesRideBabyHoglin(), createIdleLookBehaviors(), createIdleMovementBehaviors(), BehaviorLookInteract.create(EntityTypes.PLAYER, 4)));
    }

    private static void initFightActivity(EntityPiglin entitypiglin, BehaviorController<EntityPiglin> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(BehaviorAttackTargetForget.create((worldserver, entityliving) -> {
            return !isNearestValidAttackTarget(worldserver, entitypiglin, entityliving);
        }), BehaviorBuilder.triggerIf(PiglinAI::hasCrossbow, BehaviorRetreat.create(5, 0.75F)), BehaviorWalkAwayOutOfRange.create(1.0F), BehaviorAttack.create(20), new BehaviorCrossbowAttack(), BehaviorRememberHuntedHoglin.create(), BehaviorRemoveMemory.create(PiglinAI::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCelebrateActivity(BehaviorController<EntityPiglin> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 10, ImmutableList.of(avoidRepellent(), BehaviorLookTarget.create(PiglinAI::isPlayerHoldingLovedItem, 14.0F), BehaviorAttackTargetSet.create((worldserver, entitypiglin) -> {
            return entitypiglin.isAdult();
        }, PiglinAI::findNearestValidAttackTarget), BehaviorBuilder.triggerIf((entitypiglin) -> {
            return !entitypiglin.isDancing();
        }, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0F)), BehaviorBuilder.triggerIf(EntityPiglin::isDancing, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6F)), new BehaviorGateSingle(ImmutableList.of(Pair.of(BehaviorLookTarget.create(EntityTypes.PIGLIN, 8.0F), 1), Pair.of(BehaviorStrollRandomUnconstrained.stroll(0.6F, 2, 1), 1), Pair.of(new BehaviorNop(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
    }

    private static void initAdmireItemActivity(BehaviorController<EntityPiglin> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, ImmutableList.of(BehaviorFindAdmirableItem.create(PiglinAI::isNotHoldingLovedItemInOffHand, 1.0F, true, 9), BehaviorStopAdmiringItem.create(9), BehaviorAdmireTimeout.create(200, 200)), MemoryModuleType.ADMIRING_ITEM);
    }

    private static void initRetreatActivity(BehaviorController<EntityPiglin> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(BehaviorWalkAway.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true), createIdleLookBehaviors(), createIdleMovementBehaviors(), BehaviorRemoveMemory.create(PiglinAI::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static void initRideHoglinActivity(BehaviorController<EntityPiglin> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, ImmutableList.of(BehaviorStartRiding.create(0.8F), BehaviorLookTarget.create(PiglinAI::isPlayerHoldingLovedItem, 8.0F), BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(Entity::isPassenger), TriggerGate.triggerOneShuffled(ImmutableList.builder().addAll(createLookBehaviors()).add(Pair.of(BehaviorBuilder.triggerIf((entitypiglin) -> {
            return true;
        }), 1)).build())), BehaviorStopRiding.create(8, PiglinAI::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
    }

    private static ImmutableList<Pair<OneShot<EntityLiving>, Integer>> createLookBehaviors() {
        return ImmutableList.of(Pair.of(BehaviorLookTarget.create(EntityTypes.PLAYER, 8.0F), 1), Pair.of(BehaviorLookTarget.create(EntityTypes.PIGLIN, 8.0F), 1), Pair.of(BehaviorLookTarget.create(8.0F), 1));
    }

    private static BehaviorGateSingle<EntityLiving> createIdleLookBehaviors() {
        return new BehaviorGateSingle<EntityLiving>(ImmutableList.builder().addAll(createLookBehaviors()).add(Pair.of(new BehaviorNop(30, 60), 1)).build());
    }

    private static BehaviorGateSingle<EntityPiglin> createIdleMovementBehaviors() {
        return new BehaviorGateSingle<EntityPiglin>(ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(0.6F), 2), Pair.of(BehaviorInteract.of(EntityTypes.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(BehaviorBuilder.triggerIf(PiglinAI::doesntSeeAnyPlayerHoldingLovedItem, BehaviorLookWalk.create(0.6F, 3)), 2), Pair.of(new BehaviorNop(30, 60), 1)));
    }

    private static BehaviorControl<EntityCreature> avoidRepellent() {
        return BehaviorWalkAway.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
    }

    private static BehaviorControl<EntityPiglin> babyAvoidNemesis() {
        return BehaviorExpirableMemory.create(EntityPiglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, PiglinAI.BABY_AVOID_NEMESIS_DURATION);
    }

    private static BehaviorControl<EntityPiglin> avoidZombified() {
        return BehaviorExpirableMemory.create(PiglinAI::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, PiglinAI.AVOID_ZOMBIFIED_DURATION);
    }

    protected static void updateActivity(EntityPiglin entitypiglin) {
        BehaviorController<EntityPiglin> behaviorcontroller = entitypiglin.getBrain();
        Activity activity = (Activity) behaviorcontroller.getActiveNonCoreActivity().orElse((Object) null);

        behaviorcontroller.setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        Activity activity1 = (Activity) behaviorcontroller.getActiveNonCoreActivity().orElse((Object) null);

        if (activity != activity1) {
            Optional optional = getSoundForCurrentActivity(entitypiglin);

            Objects.requireNonNull(entitypiglin);
            optional.ifPresent(entitypiglin::makeSound);
        }

        entitypiglin.setAggressive(behaviorcontroller.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!behaviorcontroller.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(entitypiglin)) {
            entitypiglin.stopRiding();
        }

        if (!behaviorcontroller.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            behaviorcontroller.eraseMemory(MemoryModuleType.DANCING);
        }

        entitypiglin.setDancing(behaviorcontroller.hasMemoryValue(MemoryModuleType.DANCING));
    }

    private static boolean isBabyRidingBaby(EntityPiglin entitypiglin) {
        if (!entitypiglin.isBaby()) {
            return false;
        } else {
            Entity entity = entitypiglin.getVehicle();

            return entity instanceof EntityPiglin && ((EntityPiglin) entity).isBaby() || entity instanceof EntityHoglin && ((EntityHoglin) entity).isBaby();
        }
    }

    protected static void pickUpItem(WorldServer worldserver, EntityPiglin entitypiglin, EntityItem entityitem) {
        stopWalking(entitypiglin);
        ItemStack itemstack;

        if (entityitem.getItem().is(Items.GOLD_NUGGET)) {
            entitypiglin.take(entityitem, entityitem.getItem().getCount());
            itemstack = entityitem.getItem();
            entityitem.discard();
        } else {
            entitypiglin.take(entityitem, 1);
            itemstack = removeOneItemFromItemEntity(entityitem);
        }

        if (isLovedItem(itemstack)) {
            entitypiglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            holdInOffhand(worldserver, entitypiglin, itemstack);
            admireGoldItem(entitypiglin);
        } else if (isFood(itemstack) && !hasEatenRecently(entitypiglin)) {
            eat(entitypiglin);
        } else {
            boolean flag = !entitypiglin.equipItemIfPossible(worldserver, itemstack).equals(ItemStack.EMPTY);

            if (!flag) {
                putInInventory(entitypiglin, itemstack);
            }
        }
    }

    private static void holdInOffhand(WorldServer worldserver, EntityPiglin entitypiglin, ItemStack itemstack) {
        if (isHoldingItemInOffHand(entitypiglin)) {
            entitypiglin.spawnAtLocation(worldserver, entitypiglin.getItemInHand(EnumHand.OFF_HAND));
        }

        entitypiglin.holdInOffHand(itemstack);
    }

    private static ItemStack removeOneItemFromItemEntity(EntityItem entityitem) {
        ItemStack itemstack = entityitem.getItem();
        ItemStack itemstack1 = itemstack.split(1);

        if (itemstack.isEmpty()) {
            entityitem.discard();
        } else {
            entityitem.setItem(itemstack);
        }

        return itemstack1;
    }

    protected static void stopHoldingOffHandItem(WorldServer worldserver, EntityPiglin entitypiglin, boolean flag) {
        ItemStack itemstack = entitypiglin.getItemInHand(EnumHand.OFF_HAND);

        entitypiglin.setItemInHand(EnumHand.OFF_HAND, ItemStack.EMPTY);
        if (entitypiglin.isAdult()) {
            boolean flag1 = isBarterCurrency(itemstack);

            if (flag && flag1) {
                throwItems(entitypiglin, getBarterResponseItems(entitypiglin));
            } else if (!flag1) {
                boolean flag2 = !entitypiglin.equipItemIfPossible(worldserver, itemstack).isEmpty();

                if (!flag2) {
                    putInInventory(entitypiglin, itemstack);
                }
            }
        } else {
            boolean flag3 = !entitypiglin.equipItemIfPossible(worldserver, itemstack).isEmpty();

            if (!flag3) {
                ItemStack itemstack1 = entitypiglin.getMainHandItem();

                if (isLovedItem(itemstack1)) {
                    putInInventory(entitypiglin, itemstack1);
                } else {
                    throwItems(entitypiglin, Collections.singletonList(itemstack1));
                }

                entitypiglin.holdInMainHand(itemstack);
            }
        }

    }

    protected static void cancelAdmiring(WorldServer worldserver, EntityPiglin entitypiglin) {
        if (isAdmiringItem(entitypiglin) && !entitypiglin.getOffhandItem().isEmpty()) {
            entitypiglin.spawnAtLocation(worldserver, entitypiglin.getOffhandItem());
            entitypiglin.setItemInHand(EnumHand.OFF_HAND, ItemStack.EMPTY);
        }

    }

    private static void putInInventory(EntityPiglin entitypiglin, ItemStack itemstack) {
        ItemStack itemstack1 = entitypiglin.addToInventory(itemstack);

        throwItemsTowardRandomPos(entitypiglin, Collections.singletonList(itemstack1));
    }

    private static void throwItems(EntityPiglin entitypiglin, List<ItemStack> list) {
        Optional<EntityHuman> optional = entitypiglin.getBrain().<EntityHuman>getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        if (optional.isPresent()) {
            throwItemsTowardPlayer(entitypiglin, (EntityHuman) optional.get(), list);
        } else {
            throwItemsTowardRandomPos(entitypiglin, list);
        }

    }

    private static void throwItemsTowardRandomPos(EntityPiglin entitypiglin, List<ItemStack> list) {
        throwItemsTowardPos(entitypiglin, list, getRandomNearbyPos(entitypiglin));
    }

    private static void throwItemsTowardPlayer(EntityPiglin entitypiglin, EntityHuman entityhuman, List<ItemStack> list) {
        throwItemsTowardPos(entitypiglin, list, entityhuman.position());
    }

    private static void throwItemsTowardPos(EntityPiglin entitypiglin, List<ItemStack> list, Vec3D vec3d) {
        if (!list.isEmpty()) {
            entitypiglin.swing(EnumHand.OFF_HAND);

            for (ItemStack itemstack : list) {
                BehaviorUtil.throwItem(entitypiglin, itemstack, vec3d.add(0.0D, 1.0D, 0.0D));
            }
        }

    }

    private static List<ItemStack> getBarterResponseItems(EntityPiglin entitypiglin) {
        LootTable loottable = entitypiglin.level().getServer().reloadableRegistries().getLootTable(LootTables.PIGLIN_BARTERING);
        List<ItemStack> list = loottable.getRandomItems((new LootParams.a((WorldServer) entitypiglin.level())).withParameter(LootContextParameters.THIS_ENTITY, entitypiglin).create(LootContextParameterSets.PIGLIN_BARTER));

        return list;
    }

    private static boolean wantsToDance(EntityLiving entityliving, EntityLiving entityliving1) {
        return entityliving1.getType() != EntityTypes.HOGLIN ? false : RandomSource.create(entityliving.level().getGameTime()).nextFloat() < 0.1F;
    }

    protected static boolean wantsToPickup(EntityPiglin entitypiglin, ItemStack itemstack) {
        if (entitypiglin.isBaby() && itemstack.is(TagsItem.IGNORED_BY_PIGLIN_BABIES)) {
            return false;
        } else if (itemstack.is(TagsItem.PIGLIN_REPELLENTS)) {
            return false;
        } else if (isAdmiringDisabled(entitypiglin) && entitypiglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else if (isBarterCurrency(itemstack)) {
            return isNotHoldingLovedItemInOffHand(entitypiglin);
        } else {
            boolean flag = entitypiglin.canAddToInventory(itemstack);

            return itemstack.is(Items.GOLD_NUGGET) ? flag : (isFood(itemstack) ? !hasEatenRecently(entitypiglin) && flag : (!isLovedItem(itemstack) ? entitypiglin.canReplaceCurrentItem(itemstack) : isNotHoldingLovedItemInOffHand(entitypiglin) && flag));
        }
    }

    protected static boolean isLovedItem(ItemStack itemstack) {
        return itemstack.is(TagsItem.PIGLIN_LOVED);
    }

    private static boolean wantsToStopRiding(EntityPiglin entitypiglin, Entity entity) {
        if (!(entity instanceof EntityInsentient entityinsentient)) {
            return false;
        } else {
            return !entityinsentient.isBaby() || !entityinsentient.isAlive() || wasHurtRecently(entitypiglin) || wasHurtRecently(entityinsentient) || entityinsentient instanceof EntityPiglin && entityinsentient.getVehicle() == null;
        }
    }

    private static boolean isNearestValidAttackTarget(WorldServer worldserver, EntityPiglin entitypiglin, EntityLiving entityliving) {
        return findNearestValidAttackTarget(worldserver, entitypiglin).filter((entityliving1) -> {
            return entityliving1 == entityliving;
        }).isPresent();
    }

    private static boolean isNearZombified(EntityPiglin entitypiglin) {
        BehaviorController<EntityPiglin> behaviorcontroller = entitypiglin.getBrain();

        if (behaviorcontroller.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            EntityLiving entityliving = (EntityLiving) behaviorcontroller.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();

            return entitypiglin.closerThan(entityliving, 6.0D);
        } else {
            return false;
        }
    }

    private static Optional<? extends EntityLiving> findNearestValidAttackTarget(WorldServer worldserver, EntityPiglin entitypiglin) {
        BehaviorController<EntityPiglin> behaviorcontroller = entitypiglin.getBrain();

        if (isNearZombified(entitypiglin)) {
            return Optional.empty();
        } else {
            Optional<EntityLiving> optional = BehaviorUtil.getLivingEntityFromUUIDMemory(entitypiglin, MemoryModuleType.ANGRY_AT);

            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(worldserver, entitypiglin, (EntityLiving) optional.get())) {
                return optional;
            } else {
                if (behaviorcontroller.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                    Optional<EntityHuman> optional1 = behaviorcontroller.<EntityHuman>getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);

                    if (optional1.isPresent()) {
                        return optional1;
                    }
                }

                Optional<EntityInsentient> optional2 = behaviorcontroller.<EntityInsentient>getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);

                if (optional2.isPresent()) {
                    return optional2;
                } else {
                    Optional<EntityHuman> optional3 = behaviorcontroller.<EntityHuman>getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);

                    return optional3.isPresent() && Sensor.isEntityAttackable(worldserver, entitypiglin, (EntityLiving) optional3.get()) ? optional3 : Optional.empty();
                }
            }
        }
    }

    public static void angerNearbyPiglins(WorldServer worldserver, EntityHuman entityhuman, boolean flag) {
        List<EntityPiglin> list = entityhuman.level().<EntityPiglin>getEntitiesOfClass(EntityPiglin.class, entityhuman.getBoundingBox().inflate(16.0D));

        list.stream().filter(PiglinAI::isIdle).filter((entitypiglin) -> {
            return !flag || BehaviorUtil.canSee(entitypiglin, entityhuman);
        }).forEach((entitypiglin) -> {
            if (worldserver.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                setAngerTargetToNearestTargetablePlayerIfFound(worldserver, entitypiglin, entityhuman);
            } else {
                setAngerTarget(worldserver, entitypiglin, entityhuman);
            }

        });
    }

    public static EnumInteractionResult mobInteract(WorldServer worldserver, EntityPiglin entitypiglin, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (canAdmire(entitypiglin, itemstack)) {
            ItemStack itemstack1 = itemstack.consumeAndReturn(1, entityhuman);

            holdInOffhand(worldserver, entitypiglin, itemstack1);
            admireGoldItem(entitypiglin);
            stopWalking(entitypiglin);
            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    protected static boolean canAdmire(EntityPiglin entitypiglin, ItemStack itemstack) {
        return !isAdmiringDisabled(entitypiglin) && !isAdmiringItem(entitypiglin) && entitypiglin.isAdult() && isBarterCurrency(itemstack);
    }

    protected static void wasHurtBy(WorldServer worldserver, EntityPiglin entitypiglin, EntityLiving entityliving) {
        if (!(entityliving instanceof EntityPiglin)) {
            if (isHoldingItemInOffHand(entitypiglin)) {
                stopHoldingOffHandItem(worldserver, entitypiglin, false);
            }

            BehaviorController<EntityPiglin> behaviorcontroller = entitypiglin.getBrain();

            behaviorcontroller.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
            behaviorcontroller.eraseMemory(MemoryModuleType.DANCING);
            behaviorcontroller.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
            if (entityliving instanceof EntityHuman) {
                behaviorcontroller.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
            }

            getAvoidTarget(entitypiglin).ifPresent((entityliving1) -> {
                if (entityliving1.getType() != entityliving.getType()) {
                    behaviorcontroller.eraseMemory(MemoryModuleType.AVOID_TARGET);
                }

            });
            if (entitypiglin.isBaby()) {
                behaviorcontroller.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, entityliving, 100L);
                if (Sensor.isEntityAttackableIgnoringLineOfSight(worldserver, entitypiglin, entityliving)) {
                    broadcastAngerTarget(worldserver, entitypiglin, entityliving);
                }

            } else if (entityliving.getType() == EntityTypes.HOGLIN && hoglinsOutnumberPiglins(entitypiglin)) {
                setAvoidTargetAndDontHuntForAWhile(entitypiglin, entityliving);
                broadcastRetreat(entitypiglin, entityliving);
            } else {
                maybeRetaliate(worldserver, entitypiglin, entityliving);
            }
        }
    }

    protected static void maybeRetaliate(WorldServer worldserver, EntityPiglinAbstract entitypiglinabstract, EntityLiving entityliving) {
        if (!entitypiglinabstract.getBrain().isActive(Activity.AVOID)) {
            if (Sensor.isEntityAttackableIgnoringLineOfSight(worldserver, entitypiglinabstract, entityliving)) {
                if (!BehaviorUtil.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(entitypiglinabstract, entityliving, 4.0D)) {
                    if (entityliving.getType() == EntityTypes.PLAYER && worldserver.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                        setAngerTargetToNearestTargetablePlayerIfFound(worldserver, entitypiglinabstract, entityliving);
                        broadcastUniversalAnger(worldserver, entitypiglinabstract);
                    } else {
                        setAngerTarget(worldserver, entitypiglinabstract, entityliving);
                        broadcastAngerTarget(worldserver, entitypiglinabstract, entityliving);
                    }

                }
            }
        }
    }

    public static Optional<SoundEffect> getSoundForCurrentActivity(EntityPiglin entitypiglin) {
        return entitypiglin.getBrain().getActiveNonCoreActivity().map((activity) -> {
            return getSoundForActivity(entitypiglin, activity);
        });
    }

    private static SoundEffect getSoundForActivity(EntityPiglin entitypiglin, Activity activity) {
        return activity == Activity.FIGHT ? SoundEffects.PIGLIN_ANGRY : (entitypiglin.isConverting() ? SoundEffects.PIGLIN_RETREAT : (activity == Activity.AVOID && isNearAvoidTarget(entitypiglin) ? SoundEffects.PIGLIN_RETREAT : (activity == Activity.ADMIRE_ITEM ? SoundEffects.PIGLIN_ADMIRING_ITEM : (activity == Activity.CELEBRATE ? SoundEffects.PIGLIN_CELEBRATE : (seesPlayerHoldingLovedItem(entitypiglin) ? SoundEffects.PIGLIN_JEALOUS : (isNearRepellent(entitypiglin) ? SoundEffects.PIGLIN_RETREAT : SoundEffects.PIGLIN_AMBIENT))))));
    }

    private static boolean isNearAvoidTarget(EntityPiglin entitypiglin) {
        BehaviorController<EntityPiglin> behaviorcontroller = entitypiglin.getBrain();

        return !behaviorcontroller.hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? false : ((EntityLiving) behaviorcontroller.getMemory(MemoryModuleType.AVOID_TARGET).get()).closerThan(entitypiglin, 12.0D);
    }

    protected static List<EntityPiglinAbstract> getVisibleAdultPiglins(EntityPiglin entitypiglin) {
        return (List) entitypiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static List<EntityPiglinAbstract> getAdultPiglins(EntityPiglinAbstract entitypiglinabstract) {
        return (List) entitypiglinabstract.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    public static boolean isWearingSafeArmor(EntityLiving entityliving) {
        for (EnumItemSlot enumitemslot : EquipmentSlotGroup.ARMOR) {
            if (entityliving.getItemBySlot(enumitemslot).is(TagsItem.PIGLIN_SAFE_ARMOR)) {
                return true;
            }
        }

        return false;
    }

    private static void stopWalking(EntityPiglin entitypiglin) {
        entitypiglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entitypiglin.getNavigation().stop();
    }

    private static BehaviorControl<EntityLiving> babySometimesRideBabyHoglin() {
        SetEntityLookTargetSometimes.a setentitylooktargetsometimes_a = new SetEntityLookTargetSometimes.a(PiglinAI.RIDE_START_INTERVAL);

        return BehaviorExpirableMemory.create((entityliving) -> {
            return entityliving.isBaby() && setentitylooktargetsometimes_a.tickDownAndCheck(entityliving.level().random);
        }, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, PiglinAI.RIDE_DURATION);
    }

    protected static void broadcastAngerTarget(WorldServer worldserver, EntityPiglinAbstract entitypiglinabstract, EntityLiving entityliving) {
        getAdultPiglins(entitypiglinabstract).forEach((entitypiglinabstract1) -> {
            if (entityliving.getType() != EntityTypes.HOGLIN || entitypiglinabstract1.canHunt() && ((EntityHoglin) entityliving).canBeHunted()) {
                setAngerTargetIfCloserThanCurrent(worldserver, entitypiglinabstract1, entityliving);
            }
        });
    }

    protected static void broadcastUniversalAnger(WorldServer worldserver, EntityPiglinAbstract entitypiglinabstract) {
        getAdultPiglins(entitypiglinabstract).forEach((entitypiglinabstract1) -> {
            getNearestVisibleTargetablePlayer(entitypiglinabstract1).ifPresent((entityhuman) -> {
                setAngerTarget(worldserver, entitypiglinabstract1, entityhuman);
            });
        });
    }

    protected static void setAngerTarget(WorldServer worldserver, EntityPiglinAbstract entitypiglinabstract, EntityLiving entityliving) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(worldserver, entitypiglinabstract, entityliving)) {
            entitypiglinabstract.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            entitypiglinabstract.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, entityliving.getUUID(), 600L);
            if (entityliving.getType() == EntityTypes.HOGLIN && entitypiglinabstract.canHunt()) {
                dontKillAnyMoreHoglinsForAWhile(entitypiglinabstract);
            }

            if (entityliving.getType() == EntityTypes.PLAYER && worldserver.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                entitypiglinabstract.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
            }

        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(WorldServer worldserver, EntityPiglinAbstract entitypiglinabstract, EntityLiving entityliving) {
        Optional<EntityHuman> optional = getNearestVisibleTargetablePlayer(entitypiglinabstract);

        if (optional.isPresent()) {
            setAngerTarget(worldserver, entitypiglinabstract, (EntityLiving) optional.get());
        } else {
            setAngerTarget(worldserver, entitypiglinabstract, entityliving);
        }

    }

    private static void setAngerTargetIfCloserThanCurrent(WorldServer worldserver, EntityPiglinAbstract entitypiglinabstract, EntityLiving entityliving) {
        Optional<EntityLiving> optional = getAngerTarget(entitypiglinabstract);
        EntityLiving entityliving1 = BehaviorUtil.getNearestTarget(entitypiglinabstract, optional, entityliving);

        if (!optional.isPresent() || optional.get() != entityliving1) {
            setAngerTarget(worldserver, entitypiglinabstract, entityliving1);
        }
    }

    private static Optional<EntityLiving> getAngerTarget(EntityPiglinAbstract entitypiglinabstract) {
        return BehaviorUtil.getLivingEntityFromUUIDMemory(entitypiglinabstract, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<EntityLiving> getAvoidTarget(EntityPiglin entitypiglin) {
        return entitypiglin.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? entitypiglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
    }

    public static Optional<EntityHuman> getNearestVisibleTargetablePlayer(EntityPiglinAbstract entitypiglinabstract) {
        return entitypiglinabstract.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) ? entitypiglinabstract.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
    }

    private static void broadcastRetreat(EntityPiglin entitypiglin, EntityLiving entityliving) {
        getVisibleAdultPiglins(entitypiglin).stream().filter((entitypiglinabstract) -> {
            return entitypiglinabstract instanceof EntityPiglin;
        }).forEach((entitypiglinabstract) -> {
            retreatFromNearestTarget((EntityPiglin) entitypiglinabstract, entityliving);
        });
    }

    private static void retreatFromNearestTarget(EntityPiglin entitypiglin, EntityLiving entityliving) {
        BehaviorController<EntityPiglin> behaviorcontroller = entitypiglin.getBrain();
        EntityLiving entityliving1 = BehaviorUtil.getNearestTarget(entitypiglin, behaviorcontroller.getMemory(MemoryModuleType.AVOID_TARGET), entityliving);

        entityliving1 = BehaviorUtil.getNearestTarget(entitypiglin, behaviorcontroller.getMemory(MemoryModuleType.ATTACK_TARGET), entityliving1);
        setAvoidTargetAndDontHuntForAWhile(entitypiglin, entityliving1);
    }

    private static boolean wantsToStopFleeing(EntityPiglin entitypiglin) {
        BehaviorController<EntityPiglin> behaviorcontroller = entitypiglin.getBrain();

        if (!behaviorcontroller.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            EntityLiving entityliving = (EntityLiving) behaviorcontroller.getMemory(MemoryModuleType.AVOID_TARGET).get();
            EntityTypes<?> entitytypes = entityliving.getType();

            return entitytypes == EntityTypes.HOGLIN ? piglinsEqualOrOutnumberHoglins(entitypiglin) : (isZombified(entitytypes) ? !behaviorcontroller.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, entityliving) : false);
        }
    }

    private static boolean piglinsEqualOrOutnumberHoglins(EntityPiglin entitypiglin) {
        return !hoglinsOutnumberPiglins(entitypiglin);
    }

    private static boolean hoglinsOutnumberPiglins(EntityPiglin entitypiglin) {
        int i = (Integer) entitypiglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int j = (Integer) entitypiglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);

        return j > i;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(EntityPiglin entitypiglin, EntityLiving entityliving) {
        entitypiglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        entitypiglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        entitypiglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entitypiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, entityliving, (long) PiglinAI.RETREAT_DURATION.sample(entitypiglin.level().random));
        dontKillAnyMoreHoglinsForAWhile(entitypiglin);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(EntityPiglinAbstract entitypiglinabstract) {
        entitypiglinabstract.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long) PiglinAI.TIME_BETWEEN_HUNTS.sample(entitypiglinabstract.level().random));
    }

    private static void eat(EntityPiglin entitypiglin) {
        entitypiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3D getRandomNearbyPos(EntityPiglin entitypiglin) {
        Vec3D vec3d = LandRandomPos.getPos(entitypiglin, 4, 2);

        return vec3d == null ? entitypiglin.position() : vec3d;
    }

    private static boolean hasEatenRecently(EntityPiglin entitypiglin) {
        return entitypiglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean isIdle(EntityPiglinAbstract entitypiglinabstract) {
        return entitypiglinabstract.getBrain().isActive(Activity.IDLE);
    }

    private static boolean hasCrossbow(EntityLiving entityliving) {
        return entityliving.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(EntityLiving entityliving) {
        entityliving.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 119L);
    }

    private static boolean isAdmiringItem(EntityPiglin entitypiglin) {
        return entitypiglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(ItemStack itemstack) {
        return itemstack.is(PiglinAI.BARTERING_ITEM);
    }

    private static boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.PIGLIN_FOOD);
    }

    private static boolean isNearRepellent(EntityPiglin entitypiglin) {
        return entitypiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean seesPlayerHoldingLovedItem(EntityLiving entityliving) {
        return entityliving.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(EntityLiving entityliving) {
        return !seesPlayerHoldingLovedItem(entityliving);
    }

    public static boolean isPlayerHoldingLovedItem(EntityLiving entityliving) {
        return entityliving.getType() == EntityTypes.PLAYER && entityliving.isHolding(PiglinAI::isLovedItem);
    }

    private static boolean isAdmiringDisabled(EntityPiglin entitypiglin) {
        return entitypiglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean wasHurtRecently(EntityLiving entityliving) {
        return entityliving.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isHoldingItemInOffHand(EntityPiglin entitypiglin) {
        return !entitypiglin.getOffhandItem().isEmpty();
    }

    private static boolean isNotHoldingLovedItemInOffHand(EntityPiglin entitypiglin) {
        return entitypiglin.getOffhandItem().isEmpty() || !isLovedItem(entitypiglin.getOffhandItem());
    }

    public static boolean isZombified(EntityTypes<?> entitytypes) {
        return entitytypes == EntityTypes.ZOMBIFIED_PIGLIN || entitytypes == EntityTypes.ZOGLIN;
    }
}
