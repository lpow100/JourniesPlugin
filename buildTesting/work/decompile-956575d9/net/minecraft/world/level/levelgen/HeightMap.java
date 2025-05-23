package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.DataBits;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.IChunkAccess;
import org.slf4j.Logger;

public class HeightMap {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final Predicate<IBlockData> NOT_AIR = (iblockdata) -> {
        return !iblockdata.isAir();
    };
    static final Predicate<IBlockData> MATERIAL_MOTION_BLOCKING = BlockBase.BlockData::blocksMotion;
    private final DataBits data;
    private final Predicate<IBlockData> isOpaque;
    private final IChunkAccess chunk;

    public HeightMap(IChunkAccess ichunkaccess, HeightMap.Type heightmap_type) {
        this.isOpaque = heightmap_type.isOpaque();
        this.chunk = ichunkaccess;
        int i = MathHelper.ceillog2(ichunkaccess.getHeight() + 1);

        this.data = new SimpleBitStorage(i, 256);
    }

    public static void primeHeightmaps(IChunkAccess ichunkaccess, Set<HeightMap.Type> set) {
        if (!set.isEmpty()) {
            int i = set.size();
            ObjectList<HeightMap> objectlist = new ObjectArrayList(i);
            ObjectListIterator<HeightMap> objectlistiterator = objectlist.iterator();
            int j = ichunkaccess.getHighestSectionPosition() + 16;
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    for (HeightMap.Type heightmap_type : set) {
                        objectlist.add(ichunkaccess.getOrCreateHeightmapUnprimed(heightmap_type));
                    }

                    for (int i1 = j - 1; i1 >= ichunkaccess.getMinY(); --i1) {
                        blockposition_mutableblockposition.set(k, i1, l);
                        IBlockData iblockdata = ichunkaccess.getBlockState(blockposition_mutableblockposition);

                        if (!iblockdata.is(Blocks.AIR)) {
                            while (objectlistiterator.hasNext()) {
                                HeightMap heightmap = (HeightMap) objectlistiterator.next();

                                if (heightmap.isOpaque.test(iblockdata)) {
                                    heightmap.setHeight(k, l, i1 + 1);
                                    objectlistiterator.remove();
                                }
                            }

                            if (objectlist.isEmpty()) {
                                break;
                            }

                            objectlistiterator.back(i);
                        }
                    }
                }
            }

        }
    }

    public boolean update(int i, int j, int k, IBlockData iblockdata) {
        int l = this.getFirstAvailable(i, k);

        if (j <= l - 2) {
            return false;
        } else {
            if (this.isOpaque.test(iblockdata)) {
                if (j >= l) {
                    this.setHeight(i, k, j + 1);
                    return true;
                }
            } else if (l - 1 == j) {
                BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

                for (int i1 = j - 1; i1 >= this.chunk.getMinY(); --i1) {
                    blockposition_mutableblockposition.set(i, i1, k);
                    if (this.isOpaque.test(this.chunk.getBlockState(blockposition_mutableblockposition))) {
                        this.setHeight(i, k, i1 + 1);
                        return true;
                    }
                }

                this.setHeight(i, k, this.chunk.getMinY());
                return true;
            }

            return false;
        }
    }

    public int getFirstAvailable(int i, int j) {
        return this.getFirstAvailable(getIndex(i, j));
    }

    public int getHighestTaken(int i, int j) {
        return this.getFirstAvailable(getIndex(i, j)) - 1;
    }

    private int getFirstAvailable(int i) {
        return this.data.get(i) + this.chunk.getMinY();
    }

    private void setHeight(int i, int j, int k) {
        this.data.set(getIndex(i, j), k - this.chunk.getMinY());
    }

    public void setRawData(IChunkAccess ichunkaccess, HeightMap.Type heightmap_type, long[] along) {
        long[] along1 = this.data.getRaw();

        if (along1.length == along.length) {
            System.arraycopy(along, 0, along1, 0, along.length);
        } else {
            Logger logger = HeightMap.LOGGER;
            String s = String.valueOf(ichunkaccess.getPos());

            logger.warn("Ignoring heightmap data for chunk " + s + ", size does not match; expected: " + along1.length + ", got: " + along.length);
            primeHeightmaps(ichunkaccess, EnumSet.of(heightmap_type));
        }
    }

    public long[] getRawData() {
        return this.data.getRaw();
    }

    private static int getIndex(int i, int j) {
        return i + j * 16;
    }

    public static enum Use {

        WORLDGEN, LIVE_WORLD, CLIENT;

        private Use() {}
    }

    public static enum Type implements INamable {

        WORLD_SURFACE_WG(0, "WORLD_SURFACE_WG", HeightMap.Use.WORLDGEN, HeightMap.NOT_AIR), WORLD_SURFACE(1, "WORLD_SURFACE", HeightMap.Use.CLIENT, HeightMap.NOT_AIR), OCEAN_FLOOR_WG(2, "OCEAN_FLOOR_WG", HeightMap.Use.WORLDGEN, HeightMap.MATERIAL_MOTION_BLOCKING), OCEAN_FLOOR(3, "OCEAN_FLOOR", HeightMap.Use.LIVE_WORLD, HeightMap.MATERIAL_MOTION_BLOCKING), MOTION_BLOCKING(4, "MOTION_BLOCKING", HeightMap.Use.CLIENT, (iblockdata) -> {
            return iblockdata.blocksMotion() || !iblockdata.getFluidState().isEmpty();
        }), MOTION_BLOCKING_NO_LEAVES(5, "MOTION_BLOCKING_NO_LEAVES", HeightMap.Use.CLIENT, (iblockdata) -> {
            return (iblockdata.blocksMotion() || !iblockdata.getFluidState().isEmpty()) && !(iblockdata.getBlock() instanceof BlockLeaves);
        });

        public static final Codec<HeightMap.Type> CODEC = INamable.<HeightMap.Type>fromEnum(HeightMap.Type::values);
        private static final IntFunction<HeightMap.Type> BY_ID = ByIdMap.<HeightMap.Type>continuous((heightmap_type) -> {
            return heightmap_type.id;
        }, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, HeightMap.Type> STREAM_CODEC = ByteBufCodecs.idMapper(HeightMap.Type.BY_ID, (heightmap_type) -> {
            return heightmap_type.id;
        });
        private final int id;
        private final String serializationKey;
        private final HeightMap.Use usage;
        private final Predicate<IBlockData> isOpaque;

        private Type(final int i, final String s, final HeightMap.Use heightmap_use, final Predicate predicate) {
            this.id = i;
            this.serializationKey = s;
            this.usage = heightmap_use;
            this.isOpaque = predicate;
        }

        public String getSerializationKey() {
            return this.serializationKey;
        }

        public boolean sendToClient() {
            return this.usage == HeightMap.Use.CLIENT;
        }

        public boolean keepAfterWorldgen() {
            return this.usage != HeightMap.Use.WORLDGEN;
        }

        public Predicate<IBlockData> isOpaque() {
            return this.isOpaque;
        }

        @Override
        public String getSerializedName() {
            return this.serializationKey;
        }
    }
}
