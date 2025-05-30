package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class VillagePlaceSection {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Short2ObjectMap<VillagePlaceRecord> records;
    private final Map<Holder<VillagePlaceType>, Set<VillagePlaceRecord>> byType;
    private final Runnable setDirty;
    private boolean isValid;

    public VillagePlaceSection(Runnable runnable) {
        this(runnable, true, ImmutableList.of());
    }

    VillagePlaceSection(Runnable runnable, boolean flag, List<VillagePlaceRecord> list) {
        this.records = new Short2ObjectOpenHashMap();
        this.byType = Maps.newHashMap();
        this.setDirty = runnable;
        this.isValid = flag;
        list.forEach(this::add);
    }

    public VillagePlaceSection.a pack() {
        return new VillagePlaceSection.a(this.isValid, this.records.values().stream().map(VillagePlaceRecord::pack).toList());
    }

    public Stream<VillagePlaceRecord> getRecords(Predicate<Holder<VillagePlaceType>> predicate, VillagePlace.Occupancy villageplace_occupancy) {
        return this.byType.entrySet().stream().filter((entry) -> {
            return predicate.test((Holder) entry.getKey());
        }).flatMap((entry) -> {
            return ((Set) entry.getValue()).stream();
        }).filter(villageplace_occupancy.getTest());
    }

    public void add(BlockPosition blockposition, Holder<VillagePlaceType> holder) {
        if (this.add(new VillagePlaceRecord(blockposition, holder, this.setDirty))) {
            VillagePlaceSection.LOGGER.debug("Added POI of type {} @ {}", holder.getRegisteredName(), blockposition);
            this.setDirty.run();
        }

    }

    private boolean add(VillagePlaceRecord villageplacerecord) {
        BlockPosition blockposition = villageplacerecord.getPos();
        Holder<VillagePlaceType> holder = villageplacerecord.getPoiType();
        short short0 = SectionPosition.sectionRelativePos(blockposition);
        VillagePlaceRecord villageplacerecord1 = (VillagePlaceRecord) this.records.get(short0);

        if (villageplacerecord1 != null) {
            if (holder.equals(villageplacerecord1.getPoiType())) {
                return false;
            }

            SystemUtils.logAndPauseIfInIde("POI data mismatch: already registered at " + String.valueOf(blockposition));
        }

        this.records.put(short0, villageplacerecord);
        ((Set) this.byType.computeIfAbsent(holder, (holder1) -> {
            return Sets.newHashSet();
        })).add(villageplacerecord);
        return true;
    }

    public void remove(BlockPosition blockposition) {
        VillagePlaceRecord villageplacerecord = (VillagePlaceRecord) this.records.remove(SectionPosition.sectionRelativePos(blockposition));

        if (villageplacerecord == null) {
            VillagePlaceSection.LOGGER.error("POI data mismatch: never registered at {}", blockposition);
        } else {
            ((Set) this.byType.get(villageplacerecord.getPoiType())).remove(villageplacerecord);
            Logger logger = VillagePlaceSection.LOGGER;

            Objects.requireNonNull(villageplacerecord);
            Object object = LogUtils.defer(villageplacerecord::getPoiType);

            Objects.requireNonNull(villageplacerecord);
            logger.debug("Removed POI of type {} @ {}", object, LogUtils.defer(villageplacerecord::getPos));
            this.setDirty.run();
        }
    }

    /** @deprecated */
    @Deprecated
    @VisibleForDebug
    public int getFreeTickets(BlockPosition blockposition) {
        return (Integer) this.getPoiRecord(blockposition).map(VillagePlaceRecord::getFreeTickets).orElse(0);
    }

    public boolean release(BlockPosition blockposition) {
        VillagePlaceRecord villageplacerecord = (VillagePlaceRecord) this.records.get(SectionPosition.sectionRelativePos(blockposition));

        if (villageplacerecord == null) {
            throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException("POI never registered at " + String.valueOf(blockposition)));
        } else {
            boolean flag = villageplacerecord.releaseTicket();

            this.setDirty.run();
            return flag;
        }
    }

    public boolean exists(BlockPosition blockposition, Predicate<Holder<VillagePlaceType>> predicate) {
        return this.getType(blockposition).filter(predicate).isPresent();
    }

    public Optional<Holder<VillagePlaceType>> getType(BlockPosition blockposition) {
        return this.getPoiRecord(blockposition).map(VillagePlaceRecord::getPoiType);
    }

    private Optional<VillagePlaceRecord> getPoiRecord(BlockPosition blockposition) {
        return Optional.ofNullable((VillagePlaceRecord) this.records.get(SectionPosition.sectionRelativePos(blockposition)));
    }

    public void refresh(Consumer<BiConsumer<BlockPosition, Holder<VillagePlaceType>>> consumer) {
        if (!this.isValid) {
            Short2ObjectMap<VillagePlaceRecord> short2objectmap = new Short2ObjectOpenHashMap(this.records);

            this.clear();
            consumer.accept((BiConsumer) (blockposition, holder) -> {
                short short0 = SectionPosition.sectionRelativePos(blockposition);
                VillagePlaceRecord villageplacerecord = (VillagePlaceRecord) short2objectmap.computeIfAbsent(short0, (short1) -> {
                    return new VillagePlaceRecord(blockposition, holder, this.setDirty);
                });

                this.add(villageplacerecord);
            });
            this.isValid = true;
            this.setDirty.run();
        }

    }

    private void clear() {
        this.records.clear();
        this.byType.clear();
    }

    boolean isValid() {
        return this.isValid;
    }

    public static record a(boolean isValid, List<VillagePlaceRecord.a> records) {

        public static final Codec<VillagePlaceSection.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.BOOL.lenientOptionalFieldOf("Valid", false).forGetter(VillagePlaceSection.a::isValid), VillagePlaceRecord.a.CODEC.listOf().fieldOf("Records").forGetter(VillagePlaceSection.a::records)).apply(instance, VillagePlaceSection.a::new);
        });

        public VillagePlaceSection unpack(Runnable runnable) {
            return new VillagePlaceSection(runnable, this.isValid, this.records.stream().map((villageplacerecord_a) -> {
                return villageplacerecord_a.unpack(runnable);
            }).toList());
        }
    }
}
