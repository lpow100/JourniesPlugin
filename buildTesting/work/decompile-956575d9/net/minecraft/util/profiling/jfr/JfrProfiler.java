package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.FileUtils;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.ChunkRegionReadEvent;
import net.minecraft.util.profiling.jfr.event.ChunkRegionWriteEvent;
import net.minecraft.util.profiling.jfr.event.NetworkSummaryEvent;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.StructureGenerationEvent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.chunk.storage.RegionFileCompression;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class JfrProfiler implements JvmProfiler {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ROOT_CATEGORY = "Minecraft";
    public static final String WORLD_GEN_CATEGORY = "World Generation";
    public static final String TICK_CATEGORY = "Ticking";
    public static final String NETWORK_CATEGORY = "Network";
    public static final String STORAGE_CATEGORY = "Storage";
    private static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(ChunkGenerationEvent.class, ChunkRegionReadEvent.class, ChunkRegionWriteEvent.class, PacketReceivedEvent.class, PacketSentEvent.class, NetworkSummaryEvent.class, ServerTickTimeEvent.class, StructureGenerationEvent.class, WorldLoadFinishedEvent.class);
    private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = (new DateTimeFormatterBuilder()).appendPattern("yyyy-MM-dd-HHmmss").toFormatter().withZone(ZoneId.systemDefault());
    private static final JfrProfiler INSTANCE = new JfrProfiler();
    @Nullable
    Recording recording;
    private float currentAverageTickTime;
    private final Map<String, NetworkSummaryEvent.b> networkTrafficByAddress = new ConcurrentHashMap();

    private JfrProfiler() {
        JfrProfiler.CUSTOM_EVENTS.forEach(FlightRecorder::register);
        FlightRecorder.addPeriodicEvent(ServerTickTimeEvent.class, () -> {
            (new ServerTickTimeEvent(this.currentAverageTickTime)).commit();
        });
        FlightRecorder.addPeriodicEvent(NetworkSummaryEvent.class, () -> {
            Iterator<NetworkSummaryEvent.b> iterator = this.networkTrafficByAddress.values().iterator();

            while (iterator.hasNext()) {
                ((NetworkSummaryEvent.b) iterator.next()).commitEvent();
                iterator.remove();
            }

        });
    }

    public static JfrProfiler getInstance() {
        return JfrProfiler.INSTANCE;
    }

    @Override
    public boolean start(Environment environment) {
        URL url = JfrProfiler.class.getResource("/flightrecorder-config.jfc");

        if (url == null) {
            JfrProfiler.LOGGER.warn("Could not find default flight recorder config at {}", "/flightrecorder-config.jfc");
            return false;
        } else {
            try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return this.start(bufferedreader, environment);
            } catch (IOException ioexception) {
                JfrProfiler.LOGGER.warn("Failed to start flight recorder using configuration at {}", url, ioexception);
                return false;
            }
        }
    }

    @Override
    public Path stop() {
        if (this.recording == null) {
            throw new IllegalStateException("Not currently profiling");
        } else {
            this.networkTrafficByAddress.clear();
            Path path = this.recording.getDestination();

            this.recording.stop();
            return path;
        }
    }

    @Override
    public boolean isRunning() {
        return this.recording != null;
    }

    @Override
    public boolean isAvailable() {
        return FlightRecorder.isAvailable();
    }

    private boolean start(Reader reader, Environment environment) {
        if (this.isRunning()) {
            JfrProfiler.LOGGER.warn("Profiling already in progress");
            return false;
        } else {
            try {
                Configuration configuration = Configuration.create(reader);
                String s = JfrProfiler.DATE_TIME_FORMATTER.format(Instant.now());

                this.recording = (Recording) SystemUtils.make(new Recording(configuration), (recording) -> {
                    List list = JfrProfiler.CUSTOM_EVENTS;

                    Objects.requireNonNull(recording);
                    list.forEach(recording::enable);
                    recording.setDumpOnExit(true);
                    recording.setToDisk(true);
                    recording.setName(String.format(Locale.ROOT, "%s-%s-%s", environment.getDescription(), SharedConstants.getCurrentVersion().getName(), s));
                });
                Path path = Paths.get(String.format(Locale.ROOT, "debug/%s-%s.jfr", environment.getDescription(), s));

                FileUtils.createDirectoriesSafe(path.getParent());
                this.recording.setDestination(path);
                this.recording.start();
                this.setupSummaryListener();
            } catch (ParseException | IOException ioexception) {
                JfrProfiler.LOGGER.warn("Failed to start jfr profiling", ioexception);
                return false;
            }

            JfrProfiler.LOGGER.info("Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command", new Object[]{this.recording.getId(), this.recording.getName(), this.recording.getDestination()});
            return true;
        }
    }

    private void setupSummaryListener() {
        FlightRecorder.addListener(new FlightRecorderListener() {
            final SummaryReporter summaryReporter = new SummaryReporter(() -> {
                JfrProfiler.this.recording = null;
            });

            public void recordingStateChanged(Recording recording) {
                if (recording == JfrProfiler.this.recording && recording.getState() == RecordingState.STOPPED) {
                    this.summaryReporter.recordingStopped(recording.getDestination());
                    FlightRecorder.removeListener(this);
                }
            }
        });
    }

    @Override
    public void onServerTick(float f) {
        if (ServerTickTimeEvent.TYPE.isEnabled()) {
            this.currentAverageTickTime = f;
        }

    }

    @Override
    public void onPacketReceived(EnumProtocol enumprotocol, PacketType<?> packettype, SocketAddress socketaddress, int i) {
        if (PacketReceivedEvent.TYPE.isEnabled()) {
            (new PacketReceivedEvent(enumprotocol.id(), packettype.flow().id(), packettype.id().toString(), socketaddress, i)).commit();
        }

        if (NetworkSummaryEvent.TYPE.isEnabled()) {
            this.networkStatFor(socketaddress).trackReceivedPacket(i);
        }

    }

    @Override
    public void onPacketSent(EnumProtocol enumprotocol, PacketType<?> packettype, SocketAddress socketaddress, int i) {
        if (PacketSentEvent.TYPE.isEnabled()) {
            (new PacketSentEvent(enumprotocol.id(), packettype.flow().id(), packettype.id().toString(), socketaddress, i)).commit();
        }

        if (NetworkSummaryEvent.TYPE.isEnabled()) {
            this.networkStatFor(socketaddress).trackSentPacket(i);
        }

    }

    private NetworkSummaryEvent.b networkStatFor(SocketAddress socketaddress) {
        return (NetworkSummaryEvent.b) this.networkTrafficByAddress.computeIfAbsent(socketaddress.toString(), NetworkSummaryEvent.b::new);
    }

    @Override
    public void onRegionFileRead(RegionStorageInfo regionstorageinfo, ChunkCoordIntPair chunkcoordintpair, RegionFileCompression regionfilecompression, int i) {
        if (ChunkRegionReadEvent.TYPE.isEnabled()) {
            (new ChunkRegionReadEvent(regionstorageinfo, chunkcoordintpair, regionfilecompression, i)).commit();
        }

    }

    @Override
    public void onRegionFileWrite(RegionStorageInfo regionstorageinfo, ChunkCoordIntPair chunkcoordintpair, RegionFileCompression regionfilecompression, int i) {
        if (ChunkRegionWriteEvent.TYPE.isEnabled()) {
            (new ChunkRegionWriteEvent(regionstorageinfo, chunkcoordintpair, regionfilecompression, i)).commit();
        }

    }

    @Nullable
    @Override
    public ProfiledDuration onWorldLoadedStarted() {
        if (!WorldLoadFinishedEvent.TYPE.isEnabled()) {
            return null;
        } else {
            WorldLoadFinishedEvent worldloadfinishedevent = new WorldLoadFinishedEvent();

            worldloadfinishedevent.begin();
            return (flag) -> {
                worldloadfinishedevent.commit();
            };
        }
    }

    @Nullable
    @Override
    public ProfiledDuration onChunkGenerate(ChunkCoordIntPair chunkcoordintpair, ResourceKey<World> resourcekey, String s) {
        if (!ChunkGenerationEvent.TYPE.isEnabled()) {
            return null;
        } else {
            ChunkGenerationEvent chunkgenerationevent = new ChunkGenerationEvent(chunkcoordintpair, resourcekey, s);

            chunkgenerationevent.begin();
            return (flag) -> {
                chunkgenerationevent.commit();
            };
        }
    }

    @Nullable
    @Override
    public ProfiledDuration onStructureGenerate(ChunkCoordIntPair chunkcoordintpair, ResourceKey<World> resourcekey, Holder<Structure> holder) {
        if (!StructureGenerationEvent.TYPE.isEnabled()) {
            return null;
        } else {
            StructureGenerationEvent structuregenerationevent = new StructureGenerationEvent(chunkcoordintpair, holder, resourcekey);

            structuregenerationevent.begin();
            return (flag) -> {
                structuregenerationevent.success = flag;
                structuregenerationevent.commit();
            };
        }
    }
}
