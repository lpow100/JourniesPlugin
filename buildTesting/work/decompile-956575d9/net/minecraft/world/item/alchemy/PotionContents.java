package net.minecraft.world.item.alchemy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.World;

public record PotionContents(Optional<Holder<PotionRegistry>> potion, Optional<Integer> customColor, List<MobEffect> customEffects, Optional<String> customName) implements ConsumableListener, TooltipProvider {

    public static final PotionContents EMPTY = new PotionContents(Optional.empty(), Optional.empty(), List.of(), Optional.empty());
    private static final IChatBaseComponent NO_EFFECT = IChatBaseComponent.translatable("effect.none").withStyle(EnumChatFormat.GRAY);
    public static final int BASE_POTION_COLOR = -13083194;
    private static final Codec<PotionContents> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(PotionRegistry.CODEC.optionalFieldOf("potion").forGetter(PotionContents::potion), Codec.INT.optionalFieldOf("custom_color").forGetter(PotionContents::customColor), MobEffect.CODEC.listOf().optionalFieldOf("custom_effects", List.of()).forGetter(PotionContents::customEffects), Codec.STRING.optionalFieldOf("custom_name").forGetter(PotionContents::customName)).apply(instance, PotionContents::new);
    });
    public static final Codec<PotionContents> CODEC = Codec.withAlternative(PotionContents.FULL_CODEC, PotionRegistry.CODEC, PotionContents::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionContents> STREAM_CODEC = StreamCodec.composite(PotionRegistry.STREAM_CODEC.apply(ByteBufCodecs::optional), PotionContents::potion, ByteBufCodecs.INT.apply(ByteBufCodecs::optional), PotionContents::customColor, MobEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), PotionContents::customEffects, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), PotionContents::customName, PotionContents::new);

    public PotionContents(Holder<PotionRegistry> holder) {
        this(Optional.of(holder), Optional.empty(), List.of(), Optional.empty());
    }

    public static ItemStack createItemStack(Item item, Holder<PotionRegistry> holder) {
        ItemStack itemstack = new ItemStack(item);

        itemstack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
        return itemstack;
    }

    public boolean is(Holder<PotionRegistry> holder) {
        return this.potion.isPresent() && ((Holder) this.potion.get()).is(holder) && this.customEffects.isEmpty();
    }

    public Iterable<MobEffect> getAllEffects() {
        return (Iterable<MobEffect>) (this.potion.isEmpty() ? this.customEffects : (this.customEffects.isEmpty() ? ((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects() : Iterables.concat(((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects(), this.customEffects)));
    }

    public void forEachEffect(Consumer<MobEffect> consumer, float f) {
        if (this.potion.isPresent()) {
            for (MobEffect mobeffect : ((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects()) {
                consumer.accept(mobeffect.withScaledDuration(f));
            }
        }

        for (MobEffect mobeffect1 : this.customEffects) {
            consumer.accept(mobeffect1.withScaledDuration(f));
        }

    }

    public PotionContents withPotion(Holder<PotionRegistry> holder) {
        return new PotionContents(Optional.of(holder), this.customColor, this.customEffects, this.customName);
    }

    public PotionContents withEffectAdded(MobEffect mobeffect) {
        return new PotionContents(this.potion, this.customColor, SystemUtils.copyAndAdd(this.customEffects, mobeffect), this.customName);
    }

    public int getColor() {
        return this.getColorOr(-13083194);
    }

    public int getColorOr(int i) {
        return this.customColor.isPresent() ? (Integer) this.customColor.get() : getColorOptional(this.getAllEffects()).orElse(i);
    }

    public IChatBaseComponent getName(String s) {
        String s1 = (String) this.customName.or(() -> {
            return this.potion.map((holder) -> {
                return ((PotionRegistry) holder.value()).name();
            });
        }).orElse("empty");

        return IChatBaseComponent.translatable(s + s1);
    }

    public static OptionalInt getColorOptional(Iterable<MobEffect> iterable) {
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;

        for (MobEffect mobeffect : iterable) {
            if (mobeffect.isVisible()) {
                int i1 = ((MobEffectList) mobeffect.getEffect().value()).getColor();
                int j1 = mobeffect.getAmplifier() + 1;

                i += j1 * ARGB.red(i1);
                j += j1 * ARGB.green(i1);
                k += j1 * ARGB.blue(i1);
                l += j1;
            }
        }

        if (l == 0) {
            return OptionalInt.empty();
        } else {
            return OptionalInt.of(ARGB.color(i / l, j / l, k / l));
        }
    }

    public boolean hasEffects() {
        return !this.customEffects.isEmpty() ? true : this.potion.isPresent() && !((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects().isEmpty();
    }

    public List<MobEffect> customEffects() {
        return Lists.transform(this.customEffects, MobEffect::new);
    }

    public void applyToLivingEntity(EntityLiving entityliving, float f) {
        World world = entityliving.level();

        if (world instanceof WorldServer worldserver) {
            EntityHuman entityhuman;

            if (entityliving instanceof EntityHuman entityhuman1) {
                entityhuman = entityhuman1;
            } else {
                entityhuman = null;
            }

            EntityHuman entityhuman2 = entityhuman;

            this.forEachEffect((mobeffect) -> {
                if (((MobEffectList) mobeffect.getEffect().value()).isInstantenous()) {
                    ((MobEffectList) mobeffect.getEffect().value()).applyInstantenousEffect(worldserver, entityhuman2, entityhuman2, entityliving, mobeffect.getAmplifier(), 1.0D);
                } else {
                    entityliving.addEffect(mobeffect);
                }

            }, f);
        }
    }

    public static void addPotionTooltip(Iterable<MobEffect> iterable, Consumer<IChatBaseComponent> consumer, float f, float f1) {
        List<Pair<Holder<AttributeBase>, AttributeModifier>> list = Lists.newArrayList();
        boolean flag = true;

        for (MobEffect mobeffect : iterable) {
            flag = false;
            Holder<MobEffectList> holder = mobeffect.getEffect();
            int i = mobeffect.getAmplifier();

            (holder.value()).createModifiers(i, (holder1, attributemodifier) -> {
                list.add(new Pair(holder1, attributemodifier));
            });
            IChatMutableComponent ichatmutablecomponent = getPotionDescription(holder, i);

            if (!mobeffect.endsWithin(20)) {
                ichatmutablecomponent = IChatBaseComponent.translatable("potion.withDuration", ichatmutablecomponent, MobEffectUtil.formatDuration(mobeffect, f, f1));
            }

            consumer.accept(ichatmutablecomponent.withStyle(((MobEffectList) holder.value()).getCategory().getTooltipFormatting()));
        }

        if (flag) {
            consumer.accept(PotionContents.NO_EFFECT);
        }

        if (!list.isEmpty()) {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(IChatBaseComponent.translatable("potion.whenDrank").withStyle(EnumChatFormat.DARK_PURPLE));

            for (Pair<Holder<AttributeBase>, AttributeModifier> pair : list) {
                AttributeModifier attributemodifier = (AttributeModifier) pair.getSecond();
                double d0 = attributemodifier.amount();
                double d1;

                if (attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    d1 = attributemodifier.amount();
                } else {
                    d1 = attributemodifier.amount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    consumer.accept(IChatBaseComponent.translatable("attribute.modifier.plus." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) ((Holder) pair.getFirst()).value()).getDescriptionId())).withStyle(EnumChatFormat.BLUE));
                } else if (d0 < 0.0D) {
                    d1 *= -1.0D;
                    consumer.accept(IChatBaseComponent.translatable("attribute.modifier.take." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) ((Holder) pair.getFirst()).value()).getDescriptionId())).withStyle(EnumChatFormat.RED));
                }
            }
        }

    }

    public static IChatMutableComponent getPotionDescription(Holder<MobEffectList> holder, int i) {
        IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable(((MobEffectList) holder.value()).getDescriptionId());

        return i > 0 ? IChatBaseComponent.translatable("potion.withAmplifier", ichatmutablecomponent, IChatBaseComponent.translatable("potion.potency." + i)) : ichatmutablecomponent;
    }

    @Override
    public void onConsume(World world, EntityLiving entityliving, ItemStack itemstack, Consumable consumable) {
        this.applyToLivingEntity(entityliving, (Float) itemstack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F));
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        addPotionTooltip(this.getAllEffects(), consumer, (Float) datacomponentgetter.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F), item_b.tickRate());
    }
}
