package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionUser;

public abstract class LootEntryAbstract implements LootEntryChildren {

    protected final List<LootItemCondition> conditions;
    private final Predicate<LootTableInfo> compositeCondition;

    protected LootEntryAbstract(List<LootItemCondition> list) {
        this.conditions = list;
        this.compositeCondition = SystemUtils.allOf(list);
    }

    protected static <T extends LootEntryAbstract> Products.P1<RecordCodecBuilder.Mu<T>, List<LootItemCondition>> commonFields(RecordCodecBuilder.Instance<T> recordcodecbuilder_instance) {
        return recordcodecbuilder_instance.group(LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter((lootentryabstract) -> {
            return lootentryabstract.conditions;
        }));
    }

    public void validate(LootCollector lootcollector) {
        for (int i = 0; i < this.conditions.size(); ++i) {
            ((LootItemCondition) this.conditions.get(i)).validate(lootcollector.forChild(".condition[" + i + "]"));
        }

    }

    protected final boolean canRun(LootTableInfo loottableinfo) {
        return this.compositeCondition.test(loottableinfo);
    }

    public abstract LootEntryType getType();

    public abstract static class a<T extends LootEntryAbstract.a<T>> implements LootItemConditionUser<T> {

        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

        public a() {}

        protected abstract T getThis();

        @Override
        public T when(LootItemCondition.a lootitemcondition_a) {
            this.conditions.add(lootitemcondition_a.build());
            return (T) this.getThis();
        }

        @Override
        public final T unwrap() {
            return (T) this.getThis();
        }

        protected List<LootItemCondition> getConditions() {
            return this.conditions.build();
        }

        public LootEntryAlternatives.a otherwise(LootEntryAbstract.a<?> lootentryabstract_a) {
            return new LootEntryAlternatives.a(new LootEntryAbstract.a[]{this, lootentryabstract_a});
        }

        public LootEntryGroup.a append(LootEntryAbstract.a<?> lootentryabstract_a) {
            return new LootEntryGroup.a(new LootEntryAbstract.a[]{this, lootentryabstract_a});
        }

        public LootEntrySequence.a then(LootEntryAbstract.a<?> lootentryabstract_a) {
            return new LootEntrySequence.a(new LootEntryAbstract.a[]{this, lootentryabstract_a});
        }

        public abstract LootEntryAbstract build();
    }
}
