package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalAvoidTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalBreakDoor;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStroll;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.util.PathfinderGoalUtil;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.EntityVillagerAbstract;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.raid.EntityRaider;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;

public class EntityVindicator extends EntityIllagerAbstract {

    private static final String TAG_JOHNNY = "Johnny";
    static final Predicate<EnumDifficulty> DOOR_BREAKING_PREDICATE = (enumdifficulty) -> {
        return enumdifficulty == EnumDifficulty.NORMAL || enumdifficulty == EnumDifficulty.HARD;
    };
    private static final boolean DEFAULT_JOHNNY = false;
    public boolean isJohnny = false;

    public EntityVindicator(EntityTypes<? extends EntityVindicator> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(1, new PathfinderGoalAvoidTarget(this, Creaking.class, 8.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(2, new EntityVindicator.a(this));
        this.goalSelector.addGoal(3, new EntityIllagerAbstract.b(this));
        this.goalSelector.addGoal(4, new EntityRaider.a(this, 10.0F));
        this.goalSelector.addGoal(5, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.targetSelector.addGoal(1, (new PathfinderGoalHurtByTarget(this, new Class[]{EntityRaider.class})).setAlertOthers());
        this.targetSelector.addGoal(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.targetSelector.addGoal(3, new PathfinderGoalNearestAttackableTarget(this, EntityVillagerAbstract.class, true));
        this.targetSelector.addGoal(3, new PathfinderGoalNearestAttackableTarget(this, EntityIronGolem.class, true));
        this.targetSelector.addGoal(4, new EntityVindicator.b(this));
        this.goalSelector.addGoal(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.addGoal(9, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        if (!this.isNoAi() && PathfinderGoalUtil.hasGroundPathNavigation(this)) {
            boolean flag = worldserver.isRaided(this.blockPosition());

            ((Navigation) this.getNavigation()).setCanOpenDoors(flag);
        }

        super.customServerAiStep(worldserver);
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityMonster.createMonsterAttributes().add(GenericAttributes.MOVEMENT_SPEED, (double) 0.35F).add(GenericAttributes.FOLLOW_RANGE, 12.0D).add(GenericAttributes.MAX_HEALTH, 24.0D).add(GenericAttributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        if (this.isJohnny) {
            nbttagcompound.putBoolean("Johnny", true);
        }

    }

    @Override
    public EntityIllagerAbstract.a getArmPose() {
        return this.isAggressive() ? EntityIllagerAbstract.a.ATTACKING : (this.isCelebrating() ? EntityIllagerAbstract.a.CELEBRATING : EntityIllagerAbstract.a.CROSSED);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.isJohnny = nbttagcompound.getBooleanOr("Johnny", false);
    }

    @Override
    public SoundEffect getCelebrateSound() {
        return SoundEffects.VINDICATOR_CELEBRATE;
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        GroupDataEntity groupdataentity1 = super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);

        ((Navigation) this.getNavigation()).setCanOpenDoors(true);
        RandomSource randomsource = worldaccess.getRandom();

        this.populateDefaultEquipmentSlots(randomsource, difficultydamagescaler);
        this.populateDefaultEquipmentEnchantments(worldaccess, randomsource, difficultydamagescaler);
        return groupdataentity1;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        if (this.getCurrentRaid() == null) {
            this.setItemSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        }

    }

    @Override
    public void setCustomName(@Nullable IChatBaseComponent ichatbasecomponent) {
        super.setCustomName(ichatbasecomponent);
        if (!this.isJohnny && ichatbasecomponent != null && ichatbasecomponent.getString().equals("Johnny")) {
            this.isJohnny = true;
        }

    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.VINDICATOR_DEATH;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.VINDICATOR_HURT;
    }

    @Override
    public void applyRaidBuffs(WorldServer worldserver, int i, boolean flag) {
        ItemStack itemstack = new ItemStack(Items.IRON_AXE);
        Raid raid = this.getCurrentRaid();
        boolean flag1 = this.random.nextFloat() <= raid.getEnchantOdds();

        if (flag1) {
            ResourceKey<EnchantmentProvider> resourcekey = i > raid.getNumGroups(EnumDifficulty.NORMAL) ? VanillaEnchantmentProviders.RAID_VINDICATOR_POST_WAVE_5 : VanillaEnchantmentProviders.RAID_VINDICATOR;

            EnchantmentManager.enchantItemFromProvider(itemstack, worldserver.registryAccess(), resourcekey, worldserver.getCurrentDifficultyAt(this.blockPosition()), this.random);
        }

        this.setItemSlot(EnumItemSlot.MAINHAND, itemstack);
    }

    private static class a extends PathfinderGoalBreakDoor {

        public a(EntityInsentient entityinsentient) {
            super(entityinsentient, 6, EntityVindicator.DOOR_BREAKING_PREDICATE);
            this.setFlags(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean canContinueToUse() {
            EntityVindicator entityvindicator = (EntityVindicator) this.mob;

            return entityvindicator.hasActiveRaid() && super.canContinueToUse();
        }

        @Override
        public boolean canUse() {
            EntityVindicator entityvindicator = (EntityVindicator) this.mob;

            return entityvindicator.hasActiveRaid() && entityvindicator.random.nextInt(reducedTickDelay(10)) == 0 && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }

    private static class b extends PathfinderGoalNearestAttackableTarget<EntityLiving> {

        public b(EntityVindicator entityvindicator) {
            super(entityvindicator, EntityLiving.class, 0, true, true, (entityliving, worldserver) -> {
                return entityliving.attackable();
            });
        }

        @Override
        public boolean canUse() {
            return ((EntityVindicator) this.mob).isJohnny && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }
}
