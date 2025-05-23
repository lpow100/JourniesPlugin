package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import net.minecraft.SystemUtils;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.ChunkIdentification;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.IoSummary;
import net.minecraft.util.profiling.jfr.stats.PacketIdentification;
import net.minecraft.util.profiling.jfr.stats.StructureGenStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class JfrResultJsonSerializer {

    private static final String BYTES_PER_SECOND = "bytesPerSecond";
    private static final String COUNT = "count";
    private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String COUNT_PER_SECOND = "countPerSecond";
    final Gson gson;

    public JfrResultJsonSerializer() {
        this.gson = (new GsonBuilder()).setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();
    }

    private static void serializePacketId(PacketIdentification packetidentification, JsonObject jsonobject) {
        jsonobject.addProperty("protocolId", packetidentification.protocolId());
        jsonobject.addProperty("packetId", packetidentification.packetId());
    }

    private static void serializeChunkId(ChunkIdentification chunkidentification, JsonObject jsonobject) {
        jsonobject.addProperty("level", chunkidentification.level());
        jsonobject.addProperty("dimension", chunkidentification.dimension());
        jsonobject.addProperty("x", chunkidentification.x());
        jsonobject.addProperty("z", chunkidentification.z());
    }

    public String format(JfrStatsResult jfrstatsresult) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("startedEpoch", jfrstatsresult.recordingStarted().toEpochMilli());
        jsonobject.addProperty("endedEpoch", jfrstatsresult.recordingEnded().toEpochMilli());
        jsonobject.addProperty("durationMs", jfrstatsresult.recordingDuration().toMillis());
        Duration duration = jfrstatsresult.worldCreationDuration();

        if (duration != null) {
            jsonobject.addProperty("worldGenDurationMs", duration.toMillis());
        }

        jsonobject.add("heap", this.heap(jfrstatsresult.heapSummary()));
        jsonobject.add("cpuPercent", this.cpu(jfrstatsresult.cpuLoadStats()));
        jsonobject.add("network", this.network(jfrstatsresult));
        jsonobject.add("fileIO", this.fileIO(jfrstatsresult));
        jsonobject.add("serverTick", this.serverTicks(jfrstatsresult.tickTimes()));
        jsonobject.add("threadAllocation", this.threadAllocations(jfrstatsresult.threadAllocationSummary()));
        jsonobject.add("chunkGen", this.chunkGen(jfrstatsresult.chunkGenSummary()));
        jsonobject.add("structureGen", this.structureGen(jfrstatsresult.structureGenStats()));
        return this.gson.toJson(jsonobject);
    }

    private JsonElement heap(GcHeapStat.a gcheapstat_a) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("allocationRateBytesPerSecond", gcheapstat_a.allocationRateBytesPerSecond());
        jsonobject.addProperty("gcCount", gcheapstat_a.totalGCs());
        jsonobject.addProperty("gcOverHeadPercent", gcheapstat_a.gcOverHead());
        jsonobject.addProperty("gcTotalDurationMs", gcheapstat_a.gcTotalDuration().toMillis());
        return jsonobject;
    }

    private JsonElement structureGen(List<StructureGenStat> list) {
        JsonObject jsonobject = new JsonObject();
        TimedStatSummary<StructureGenStat> timedstatsummary = TimedStatSummary.<StructureGenStat>summary(list);
        JsonArray jsonarray = new JsonArray();

        jsonobject.add("structure", jsonarray);
        ((Map) list.stream().collect(Collectors.groupingBy(StructureGenStat::structureName))).forEach((s, list1) -> {
            JsonObject jsonobject1 = new JsonObject();

            jsonarray.add(jsonobject1);
            jsonobject1.addProperty("name", s);
            TimedStatSummary<StructureGenStat> timedstatsummary1 = TimedStatSummary.<StructureGenStat>summary(list1);

            jsonobject1.addProperty("count", timedstatsummary1.count());
            jsonobject1.addProperty("durationNanosTotal", timedstatsummary1.totalDuration().toNanos());
            jsonobject1.addProperty("durationNanosAvg", timedstatsummary1.totalDuration().toNanos() / (long) timedstatsummary1.count());
            JsonObject jsonobject2 = (JsonObject) SystemUtils.make(new JsonObject(), (jsonobject3) -> {
                jsonobject1.add("durationNanosPercentiles", jsonobject3);
            });

            timedstatsummary1.percentilesNanos().forEach((integer, odouble) -> {
                jsonobject2.addProperty("p" + integer, odouble);
            });
            Function<StructureGenStat, JsonElement> function = (structuregenstat) -> {
                JsonObject jsonobject3 = new JsonObject();

                jsonobject3.addProperty("durationNanos", structuregenstat.duration().toNanos());
                jsonobject3.addProperty("chunkPosX", structuregenstat.chunkPos().x);
                jsonobject3.addProperty("chunkPosZ", structuregenstat.chunkPos().z);
                jsonobject3.addProperty("structureName", structuregenstat.structureName());
                jsonobject3.addProperty("level", structuregenstat.level());
                jsonobject3.addProperty("success", structuregenstat.success());
                return jsonobject3;
            };

            jsonobject.add("fastest", (JsonElement) function.apply(timedstatsummary.fastest()));
            jsonobject.add("slowest", (JsonElement) function.apply(timedstatsummary.slowest()));
            jsonobject.add("secondSlowest", (JsonElement) (timedstatsummary.secondSlowest() != null ? (JsonElement) function.apply(timedstatsummary.secondSlowest()) : JsonNull.INSTANCE));
        });
        return jsonobject;
    }

    private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> list) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("durationNanosTotal", list.stream().mapToDouble((pair) -> {
            return (double) ((TimedStatSummary) pair.getSecond()).totalDuration().toNanos();
        }).sum());
        JsonArray jsonarray = (JsonArray) SystemUtils.make(new JsonArray(), (jsonarray1) -> {
            jsonobject.add("status", jsonarray1);
        });

        for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair : list) {
            TimedStatSummary<ChunkGenStat> timedstatsummary = (TimedStatSummary) pair.getSecond();
            JsonObject jsonobject1 = new JsonObject();

            Objects.requireNonNull(jsonarray);
            JsonObject jsonobject2 = (JsonObject) SystemUtils.make(jsonobject1, jsonarray::add);

            jsonobject2.addProperty("state", ((ChunkStatus) pair.getFirst()).toString());
            jsonobject2.addProperty("count", timedstatsummary.count());
            jsonobject2.addProperty("durationNanosTotal", timedstatsummary.totalDuration().toNanos());
            jsonobject2.addProperty("durationNanosAvg", timedstatsummary.totalDuration().toNanos() / (long) timedstatsummary.count());
            JsonObject jsonobject3 = (JsonObject) SystemUtils.make(new JsonObject(), (jsonobject4) -> {
                jsonobject2.add("durationNanosPercentiles", jsonobject4);
            });

            timedstatsummary.percentilesNanos().forEach((integer, odouble) -> {
                jsonobject3.addProperty("p" + integer, odouble);
            });
            Function<ChunkGenStat, JsonElement> function = (chunkgenstat) -> {
                JsonObject jsonobject4 = new JsonObject();

                jsonobject4.addProperty("durationNanos", chunkgenstat.duration().toNanos());
                jsonobject4.addProperty("level", chunkgenstat.level());
                jsonobject4.addProperty("chunkPosX", chunkgenstat.chunkPos().x);
                jsonobject4.addProperty("chunkPosZ", chunkgenstat.chunkPos().z);
                jsonobject4.addProperty("worldPosX", chunkgenstat.worldPos().x());
                jsonobject4.addProperty("worldPosZ", chunkgenstat.worldPos().z());
                return jsonobject4;
            };

            jsonobject2.add("fastest", (JsonElement) function.apply(timedstatsummary.fastest()));
            jsonobject2.add("slowest", (JsonElement) function.apply(timedstatsummary.slowest()));
            jsonobject2.add("secondSlowest", (JsonElement) (timedstatsummary.secondSlowest() != null ? (JsonElement) function.apply(timedstatsummary.secondSlowest()) : JsonNull.INSTANCE));
        }

        return jsonobject;
    }

    private JsonElement threadAllocations(ThreadAllocationStat.a threadallocationstat_a) {
        JsonArray jsonarray = new JsonArray();

        threadallocationstat_a.allocationsPerSecondByThread().forEach((s, odouble) -> {
            jsonarray.add((JsonElement) SystemUtils.make(new JsonObject(), (jsonobject) -> {
                jsonobject.addProperty("thread", s);
                jsonobject.addProperty("bytesPerSecond", odouble);
            }));
        });
        return jsonarray;
    }

    private JsonElement serverTicks(List<TickTimeStat> list) {
        if (list.isEmpty()) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject jsonobject = new JsonObject();
            double[] adouble = list.stream().mapToDouble((ticktimestat) -> {
                return (double) ticktimestat.currentAverage().toNanos() / 1000000.0D;
            }).toArray();
            DoubleSummaryStatistics doublesummarystatistics = DoubleStream.of(adouble).summaryStatistics();

            jsonobject.addProperty("minMs", doublesummarystatistics.getMin());
            jsonobject.addProperty("averageMs", doublesummarystatistics.getAverage());
            jsonobject.addProperty("maxMs", doublesummarystatistics.getMax());
            Map<Integer, Double> map = Percentiles.evaluate(adouble);

            map.forEach((integer, odouble) -> {
                jsonobject.addProperty("p" + integer, odouble);
            });
            return jsonobject;
        }
    }

    private JsonElement fileIO(JfrStatsResult jfrstatsresult) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.add("write", this.fileIoSummary(jfrstatsresult.fileWrites()));
        jsonobject.add("read", this.fileIoSummary(jfrstatsresult.fileReads()));
        jsonobject.add("chunksRead", this.ioSummary(jfrstatsresult.readChunks(), JfrResultJsonSerializer::serializeChunkId));
        jsonobject.add("chunksWritten", this.ioSummary(jfrstatsresult.writtenChunks(), JfrResultJsonSerializer::serializeChunkId));
        return jsonobject;
    }

    private JsonElement fileIoSummary(FileIOStat.a fileiostat_a) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("totalBytes", fileiostat_a.totalBytes());
        jsonobject.addProperty("count", fileiostat_a.counts());
        jsonobject.addProperty("bytesPerSecond", fileiostat_a.bytesPerSecond());
        jsonobject.addProperty("countPerSecond", fileiostat_a.countsPerSecond());
        JsonArray jsonarray = new JsonArray();

        jsonobject.add("topContributors", jsonarray);
        fileiostat_a.topTenContributorsByTotalBytes().forEach((pair) -> {
            JsonObject jsonobject1 = new JsonObject();

            jsonarray.add(jsonobject1);
            jsonobject1.addProperty("path", (String) pair.getFirst());
            jsonobject1.addProperty("totalBytes", (Number) pair.getSecond());
        });
        return jsonobject;
    }

    private JsonElement network(JfrStatsResult jfrstatsresult) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.add("sent", this.ioSummary(jfrstatsresult.sentPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
        jsonobject.add("received", this.ioSummary(jfrstatsresult.receivedPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
        return jsonobject;
    }

    private <T> JsonElement ioSummary(IoSummary<T> iosummary, BiConsumer<T, JsonObject> biconsumer) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("totalBytes", iosummary.getTotalSize());
        jsonobject.addProperty("count", iosummary.getTotalCount());
        jsonobject.addProperty("bytesPerSecond", iosummary.getSizePerSecond());
        jsonobject.addProperty("countPerSecond", iosummary.getCountsPerSecond());
        JsonArray jsonarray = new JsonArray();

        jsonobject.add("topContributors", jsonarray);
        iosummary.largestSizeContributors().forEach((pair) -> {
            JsonObject jsonobject1 = new JsonObject();

            jsonarray.add(jsonobject1);
            T t0 = (T) pair.getFirst();
            IoSummary.a iosummary_a = (IoSummary.a) pair.getSecond();

            biconsumer.accept(t0, jsonobject1);
            jsonobject1.addProperty("totalBytes", iosummary_a.totalSize());
            jsonobject1.addProperty("count", iosummary_a.totalCount());
            jsonobject1.addProperty("averageSize", iosummary_a.averageSize());
        });
        return jsonobject;
    }

    private JsonElement cpu(List<CpuLoadStat> list) {
        JsonObject jsonobject = new JsonObject();
        BiFunction<List<CpuLoadStat>, ToDoubleFunction<CpuLoadStat>, JsonObject> bifunction = (list1, todoublefunction) -> {
            JsonObject jsonobject1 = new JsonObject();
            DoubleSummaryStatistics doublesummarystatistics = list1.stream().mapToDouble(todoublefunction).summaryStatistics();

            jsonobject1.addProperty("min", doublesummarystatistics.getMin());
            jsonobject1.addProperty("average", doublesummarystatistics.getAverage());
            jsonobject1.addProperty("max", doublesummarystatistics.getMax());
            return jsonobject1;
        };

        jsonobject.add("jvm", (JsonElement) bifunction.apply(list, CpuLoadStat::jvm));
        jsonobject.add("userJvm", (JsonElement) bifunction.apply(list, CpuLoadStat::userJvm));
        jsonobject.add("system", (JsonElement) bifunction.apply(list, CpuLoadStat::system));
        return jsonobject;
    }
}
