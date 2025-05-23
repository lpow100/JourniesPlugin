package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.PacketDataSerializer;

public record AdvancementRequirements(List<List<String>> requirements) {

    public static final Codec<AdvancementRequirements> CODEC = Codec.STRING.listOf().listOf().xmap(AdvancementRequirements::new, AdvancementRequirements::requirements);
    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(List.of());

    public AdvancementRequirements(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readList((packetdataserializer1) -> {
            return packetdataserializer1.readList(PacketDataSerializer::readUtf);
        }));
    }

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.requirements, (packetdataserializer1, list) -> {
            packetdataserializer1.writeCollection(list, PacketDataSerializer::writeUtf);
        });
    }

    public static AdvancementRequirements allOf(Collection<String> collection) {
        return new AdvancementRequirements(collection.stream().map(List::of).toList());
    }

    public static AdvancementRequirements anyOf(Collection<String> collection) {
        return new AdvancementRequirements(List.of(List.copyOf(collection)));
    }

    public int size() {
        return this.requirements.size();
    }

    public boolean test(Predicate<String> predicate) {
        if (this.requirements.isEmpty()) {
            return false;
        } else {
            for (List<String> list : this.requirements) {
                if (!anyMatch(list, predicate)) {
                    return false;
                }
            }

            return true;
        }
    }

    public int count(Predicate<String> predicate) {
        int i = 0;

        for (List<String> list : this.requirements) {
            if (anyMatch(list, predicate)) {
                ++i;
            }
        }

        return i;
    }

    private static boolean anyMatch(List<String> list, Predicate<String> predicate) {
        for (String s : list) {
            if (predicate.test(s)) {
                return true;
            }
        }

        return false;
    }

    public DataResult<AdvancementRequirements> validate(Set<String> set) {
        Set<String> set1 = new ObjectOpenHashSet();

        for (List<String> list : this.requirements) {
            if (list.isEmpty() && set.isEmpty()) {
                return DataResult.error(() -> {
                    return "Requirement entry cannot be empty";
                });
            }

            set1.addAll(list);
        }

        if (!set.equals(set1)) {
            Set<String> set2 = Sets.difference(set, set1);
            Set<String> set3 = Sets.difference(set1, set);

            return DataResult.error(() -> {
                String s = String.valueOf(set2);

                return "Advancement completion requirements did not exactly match specified criteria. Missing: " + s + ". Unknown: " + String.valueOf(set3);
            });
        } else {
            return DataResult.success(this);
        }
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> names() {
        Set<String> set = new ObjectOpenHashSet();

        for (List<String> list : this.requirements) {
            set.addAll(list);
        }

        return set;
    }

    public interface a {

        AdvancementRequirements.a AND = AdvancementRequirements::allOf;
        AdvancementRequirements.a OR = AdvancementRequirements::anyOf;

        AdvancementRequirements create(Collection<String> collection);
    }
}
