package net.minecraft.world.item.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsEntity;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentManager;

public record Equippable(EnumItemSlot slot, Holder<SoundEffect> equipSound, Optional<ResourceKey<EquipmentAsset>> assetId, Optional<MinecraftKey> cameraOverlay, Optional<HolderSet<EntityTypes<?>>> allowedEntities, boolean dispensable, boolean swappable, boolean damageOnHurt, boolean equipOnInteract) {

    public static final Codec<Equippable> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(EnumItemSlot.CODEC.fieldOf("slot").forGetter(Equippable::slot), SoundEffect.CODEC.optionalFieldOf("equip_sound", SoundEffects.ARMOR_EQUIP_GENERIC).forGetter(Equippable::equipSound), ResourceKey.codec(EquipmentAssets.ROOT_ID).optionalFieldOf("asset_id").forGetter(Equippable::assetId), MinecraftKey.CODEC.optionalFieldOf("camera_overlay").forGetter(Equippable::cameraOverlay), RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).optionalFieldOf("allowed_entities").forGetter(Equippable::allowedEntities), Codec.BOOL.optionalFieldOf("dispensable", true).forGetter(Equippable::dispensable), Codec.BOOL.optionalFieldOf("swappable", true).forGetter(Equippable::swappable), Codec.BOOL.optionalFieldOf("damage_on_hurt", true).forGetter(Equippable::damageOnHurt), Codec.BOOL.optionalFieldOf("equip_on_interact", false).forGetter(Equippable::equipOnInteract)).apply(instance, Equippable::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, Equippable> STREAM_CODEC = StreamCodec.composite(EnumItemSlot.STREAM_CODEC, Equippable::slot, SoundEffect.STREAM_CODEC, Equippable::equipSound, ResourceKey.streamCodec(EquipmentAssets.ROOT_ID).apply(ByteBufCodecs::optional), Equippable::assetId, MinecraftKey.STREAM_CODEC.apply(ByteBufCodecs::optional), Equippable::cameraOverlay, ByteBufCodecs.holderSet(Registries.ENTITY_TYPE).apply(ByteBufCodecs::optional), Equippable::allowedEntities, ByteBufCodecs.BOOL, Equippable::dispensable, ByteBufCodecs.BOOL, Equippable::swappable, ByteBufCodecs.BOOL, Equippable::damageOnHurt, ByteBufCodecs.BOOL, Equippable::equipOnInteract, Equippable::new);

    public static Equippable llamaSwag(EnumColor enumcolor) {
        return builder(EnumItemSlot.BODY).setEquipSound(SoundEffects.LLAMA_SWAG).setAsset((ResourceKey) EquipmentAssets.CARPETS.get(enumcolor)).setAllowedEntities(EntityTypes.LLAMA, EntityTypes.TRADER_LLAMA).build();
    }

    public static Equippable saddle() {
        HolderGetter<EntityTypes<?>> holdergetter = BuiltInRegistries.<EntityTypes<?>>acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);

        return builder(EnumItemSlot.SADDLE).setEquipSound(SoundEffects.HORSE_SADDLE).setAsset(EquipmentAssets.SADDLE).setAllowedEntities(holdergetter.getOrThrow(TagsEntity.CAN_EQUIP_SADDLE)).setEquipOnInteract(true).build();
    }

    public static Equippable.a builder(EnumItemSlot enumitemslot) {
        return new Equippable.a(enumitemslot);
    }

    public EnumInteractionResult swapWithEquipmentSlot(ItemStack itemstack, EntityHuman entityhuman) {
        if (entityhuman.canUseSlot(this.slot) && this.canBeEquippedBy(entityhuman.getType())) {
            ItemStack itemstack1 = entityhuman.getItemBySlot(this.slot);

            if ((!EnchantmentManager.has(itemstack1, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || entityhuman.isCreative()) && !ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                if (!entityhuman.level().isClientSide()) {
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
                }

                if (itemstack.getCount() <= 1) {
                    ItemStack itemstack2 = itemstack1.isEmpty() ? itemstack : itemstack1.copyAndClear();
                    ItemStack itemstack3 = entityhuman.isCreative() ? itemstack.copy() : itemstack.copyAndClear();

                    entityhuman.setItemSlot(this.slot, itemstack3);
                    return EnumInteractionResult.SUCCESS.heldItemTransformedTo(itemstack2);
                } else {
                    ItemStack itemstack4 = itemstack1.copyAndClear();
                    ItemStack itemstack5 = itemstack.consumeAndReturn(1, entityhuman);

                    entityhuman.setItemSlot(this.slot, itemstack5);
                    if (!entityhuman.getInventory().add(itemstack4)) {
                        entityhuman.drop(itemstack4, false);
                    }

                    return EnumInteractionResult.SUCCESS.heldItemTransformedTo(itemstack);
                }
            } else {
                return EnumInteractionResult.FAIL;
            }
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    public EnumInteractionResult equipOnTarget(EntityHuman entityhuman, EntityLiving entityliving, ItemStack itemstack) {
        if (entityliving.isEquippableInSlot(itemstack, this.slot) && !entityliving.hasItemInSlot(this.slot) && entityliving.isAlive()) {
            if (!entityhuman.level().isClientSide()) {
                entityliving.setItemSlot(this.slot, itemstack.split(1));
                if (entityliving instanceof EntityInsentient) {
                    EntityInsentient entityinsentient = (EntityInsentient) entityliving;

                    entityinsentient.setGuaranteedDrop(this.slot);
                }
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    public boolean canBeEquippedBy(EntityTypes<?> entitytypes) {
        return this.allowedEntities.isEmpty() || ((HolderSet) this.allowedEntities.get()).contains(entitytypes.builtInRegistryHolder());
    }

    public static class a {

        private final EnumItemSlot slot;
        private Holder<SoundEffect> equipSound;
        private Optional<ResourceKey<EquipmentAsset>> assetId;
        private Optional<MinecraftKey> cameraOverlay;
        private Optional<HolderSet<EntityTypes<?>>> allowedEntities;
        private boolean dispensable;
        private boolean swappable;
        private boolean damageOnHurt;
        private boolean equipOnInteract;

        a(EnumItemSlot enumitemslot) {
            this.equipSound = SoundEffects.ARMOR_EQUIP_GENERIC;
            this.assetId = Optional.empty();
            this.cameraOverlay = Optional.empty();
            this.allowedEntities = Optional.empty();
            this.dispensable = true;
            this.swappable = true;
            this.damageOnHurt = true;
            this.slot = enumitemslot;
        }

        public Equippable.a setEquipSound(Holder<SoundEffect> holder) {
            this.equipSound = holder;
            return this;
        }

        public Equippable.a setAsset(ResourceKey<EquipmentAsset> resourcekey) {
            this.assetId = Optional.of(resourcekey);
            return this;
        }

        public Equippable.a setCameraOverlay(MinecraftKey minecraftkey) {
            this.cameraOverlay = Optional.of(minecraftkey);
            return this;
        }

        public Equippable.a setAllowedEntities(EntityTypes<?>... aentitytypes) {
            return this.setAllowedEntities(HolderSet.direct(EntityTypes::builtInRegistryHolder, aentitytypes));
        }

        public Equippable.a setAllowedEntities(HolderSet<EntityTypes<?>> holderset) {
            this.allowedEntities = Optional.of(holderset);
            return this;
        }

        public Equippable.a setDispensable(boolean flag) {
            this.dispensable = flag;
            return this;
        }

        public Equippable.a setSwappable(boolean flag) {
            this.swappable = flag;
            return this;
        }

        public Equippable.a setDamageOnHurt(boolean flag) {
            this.damageOnHurt = flag;
            return this;
        }

        public Equippable.a setEquipOnInteract(boolean flag) {
            this.equipOnInteract = flag;
            return this;
        }

        public Equippable build() {
            return new Equippable(this.slot, this.equipSound, this.assetId, this.cameraOverlay, this.allowedEntities, this.dispensable, this.swappable, this.damageOnHurt, this.equipOnInteract);
        }
    }
}
