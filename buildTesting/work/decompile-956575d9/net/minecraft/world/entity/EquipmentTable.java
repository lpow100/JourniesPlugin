package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record EquipmentTable(ResourceKey<LootTable> lootTable, Map<EnumItemSlot, Float> slotDropChances) {

    public static final Codec<Map<EnumItemSlot, Float>> DROP_CHANCES_CODEC = Codec.either(Codec.FLOAT, Codec.unboundedMap(EnumItemSlot.CODEC, Codec.FLOAT)).xmap((either) -> {
        return (Map) either.map(EquipmentTable::createForAllSlots, Function.identity());
    }, (map) -> {
        boolean flag = map.values().stream().distinct().count() == 1L;
        boolean flag1 = map.keySet().containsAll(EnumItemSlot.VALUES);

        return flag && flag1 ? Either.left((Float) map.values().stream().findFirst().orElse(0.0F)) : Either.right(map);
    });
    public static final Codec<EquipmentTable> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(EquipmentTable::lootTable), EquipmentTable.DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(EquipmentTable::slotDropChances)).apply(instance, EquipmentTable::new);
    });

    public EquipmentTable(ResourceKey<LootTable> resourcekey, float f) {
        this(resourcekey, createForAllSlots(f));
    }

    private static Map<EnumItemSlot, Float> createForAllSlots(float f) {
        return createForAllSlots(List.of(EnumItemSlot.values()), f);
    }

    private static Map<EnumItemSlot, Float> createForAllSlots(List<EnumItemSlot> list, float f) {
        Map<EnumItemSlot, Float> map = Maps.newHashMap();

        for (EnumItemSlot enumitemslot : list) {
            map.put(enumitemslot, f);
        }

        return map;
    }
}
