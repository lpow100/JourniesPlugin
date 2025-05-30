package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityThrownTrident;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class ItemTrident extends Item implements ProjectileItem {

    public static final int THROW_THRESHOLD_TIME = 10;
    public static final float BASE_DAMAGE = 8.0F;
    public static final float PROJECTILE_SHOOT_POWER = 2.5F;

    public ItemTrident(Item.Info item_info) {
        super(item_info);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(GenericAttributes.ATTACK_DAMAGE, new AttributeModifier(ItemTrident.BASE_ATTACK_DAMAGE_ID, 8.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(GenericAttributes.ATTACK_SPEED, new AttributeModifier(ItemTrident.BASE_ATTACK_SPEED_ID, (double) -2.9F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 2, false);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        return 72000;
    }

    @Override
    public boolean releaseUsing(ItemStack itemstack, World world, EntityLiving entityliving, int i) {
        if (entityliving instanceof EntityHuman entityhuman) {
            int j = this.getUseDuration(itemstack, entityliving) - i;

            if (j < 10) {
                return false;
            } else {
                float f = EnchantmentManager.getTridentSpinAttackStrength(itemstack, entityhuman);

                if (f > 0.0F && !entityhuman.isInWaterOrRain()) {
                    return false;
                } else if (itemstack.nextDamageWillBreak()) {
                    return false;
                } else {
                    Holder<SoundEffect> holder = (Holder) EnchantmentManager.pickHighestLevel(itemstack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEffects.TRIDENT_THROW);

                    entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                    if (world instanceof WorldServer) {
                        WorldServer worldserver = (WorldServer) world;

                        itemstack.hurtWithoutBreaking(1, entityhuman);
                        if (f == 0.0F) {
                            ItemStack itemstack1 = itemstack.consumeAndReturn(1, entityhuman);
                            EntityThrownTrident entitythrowntrident = (EntityThrownTrident) IProjectile.spawnProjectileFromRotation(EntityThrownTrident::new, worldserver, itemstack1, entityhuman, 0.0F, 2.5F, 1.0F);

                            if (entityhuman.hasInfiniteMaterials()) {
                                entitythrowntrident.pickup = EntityArrow.PickupStatus.CREATIVE_ONLY;
                            }

                            world.playSound((Entity) null, (Entity) entitythrowntrident, holder.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                            return true;
                        }
                    }

                    if (f > 0.0F) {
                        float f1 = entityhuman.getYRot();
                        float f2 = entityhuman.getXRot();
                        float f3 = -MathHelper.sin(f1 * ((float) Math.PI / 180F)) * MathHelper.cos(f2 * ((float) Math.PI / 180F));
                        float f4 = -MathHelper.sin(f2 * ((float) Math.PI / 180F));
                        float f5 = MathHelper.cos(f1 * ((float) Math.PI / 180F)) * MathHelper.cos(f2 * ((float) Math.PI / 180F));
                        float f6 = MathHelper.sqrt(f3 * f3 + f4 * f4 + f5 * f5);

                        f3 *= f / f6;
                        f4 *= f / f6;
                        f5 *= f / f6;
                        entityhuman.push((double) f3, (double) f4, (double) f5);
                        entityhuman.startAutoSpinAttack(20, 8.0F, itemstack);
                        if (entityhuman.onGround()) {
                            float f7 = 1.1999999F;

                            entityhuman.move(EnumMoveType.SELF, new Vec3D(0.0D, (double) 1.1999999F, 0.0D));
                        }

                        world.playSound((Entity) null, (Entity) entityhuman, holder.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (itemstack.nextDamageWillBreak()) {
            return EnumInteractionResult.FAIL;
        } else if (EnchantmentManager.getTridentSpinAttackStrength(itemstack, entityhuman) > 0.0F && !entityhuman.isInWaterOrRain()) {
            return EnumInteractionResult.FAIL;
        } else {
            entityhuman.startUsingItem(enumhand);
            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        EntityThrownTrident entitythrowntrident = new EntityThrownTrident(world, iposition.x(), iposition.y(), iposition.z(), itemstack.copyWithCount(1));

        entitythrowntrident.pickup = EntityArrow.PickupStatus.ALLOWED;
        return entitythrowntrident;
    }
}
