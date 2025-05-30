package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootItemFunctionSetStewEffect extends LootItemFunctionConditional {

    private static final Codec<List<LootItemFunctionSetStewEffect.b>> EFFECTS_LIST = LootItemFunctionSetStewEffect.b.CODEC.listOf().validate((list) -> {
        Set<Holder<MobEffectList>> set = new ObjectOpenHashSet();

        for (LootItemFunctionSetStewEffect.b lootitemfunctionsetsteweffect_b : list) {
            if (!set.add(lootitemfunctionsetsteweffect_b.effect())) {
                return DataResult.error(() -> {
                    return "Encountered duplicate mob effect: '" + String.valueOf(lootitemfunctionsetsteweffect_b.effect()) + "'";
                });
            }
        }

        return DataResult.success(list);
    });
    public static final MapCodec<LootItemFunctionSetStewEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(LootItemFunctionSetStewEffect.EFFECTS_LIST.optionalFieldOf("effects", List.of()).forGetter((lootitemfunctionsetsteweffect) -> {
            return lootitemfunctionsetsteweffect.effects;
        })).apply(instance, LootItemFunctionSetStewEffect::new);
    });
    private final List<LootItemFunctionSetStewEffect.b> effects;

    LootItemFunctionSetStewEffect(List<LootItemCondition> list, List<LootItemFunctionSetStewEffect.b> list1) {
        super(list);
        this.effects = list1;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetStewEffect> getType() {
        return LootItemFunctions.SET_STEW_EFFECT;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return (Set) this.effects.stream().flatMap((lootitemfunctionsetsteweffect_b) -> {
            return lootitemfunctionsetsteweffect_b.duration().getReferencedContextParams().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.is(Items.SUSPICIOUS_STEW) && !this.effects.isEmpty()) {
            LootItemFunctionSetStewEffect.b lootitemfunctionsetsteweffect_b = (LootItemFunctionSetStewEffect.b) SystemUtils.getRandom(this.effects, loottableinfo.getRandom());
            Holder<MobEffectList> holder = lootitemfunctionsetsteweffect_b.effect();
            int i = lootitemfunctionsetsteweffect_b.duration().getInt(loottableinfo);

            if (!((MobEffectList) holder.value()).isInstantenous()) {
                i *= 20;
            }

            SuspiciousStewEffects.a suspicioussteweffects_a = new SuspiciousStewEffects.a(holder, i);

            itemstack.update(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY, suspicioussteweffects_a, SuspiciousStewEffects::withEffectAdded);
            return itemstack;
        } else {
            return itemstack;
        }
    }

    public static LootItemFunctionSetStewEffect.a stewEffect() {
        return new LootItemFunctionSetStewEffect.a();
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetStewEffect.a> {

        private final ImmutableList.Builder<LootItemFunctionSetStewEffect.b> effects = ImmutableList.builder();

        public a() {}

        @Override
        protected LootItemFunctionSetStewEffect.a getThis() {
            return this;
        }

        public LootItemFunctionSetStewEffect.a withEffect(Holder<MobEffectList> holder, NumberProvider numberprovider) {
            this.effects.add(new LootItemFunctionSetStewEffect.b(holder, numberprovider));
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionSetStewEffect(this.getConditions(), this.effects.build());
        }
    }

    private static record b(Holder<MobEffectList> effect, NumberProvider duration) {

        public static final Codec<LootItemFunctionSetStewEffect.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(MobEffectList.CODEC.fieldOf("type").forGetter(LootItemFunctionSetStewEffect.b::effect), NumberProviders.CODEC.fieldOf("duration").forGetter(LootItemFunctionSetStewEffect.b::duration)).apply(instance, LootItemFunctionSetStewEffect.b::new);
        });
    }
}
