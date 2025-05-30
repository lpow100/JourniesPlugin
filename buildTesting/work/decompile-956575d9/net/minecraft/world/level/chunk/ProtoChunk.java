package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.SectionPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTickList;
import net.minecraft.world.ticks.TickContainerAccess;

public class ProtoChunk extends IChunkAccess {

    @Nullable
    private volatile LevelLightEngine lightEngine;
    private volatile ChunkStatus status;
    private final List<NBTTagCompound> entities;
    @Nullable
    private CarvingMask carvingMask;
    @Nullable
    private BelowZeroRetrogen belowZeroRetrogen;
    private final ProtoChunkTickList<Block> blockTicks;
    private final ProtoChunkTickList<FluidType> fluidTicks;

    public ProtoChunk(ChunkCoordIntPair chunkcoordintpair, ChunkConverter chunkconverter, LevelHeightAccessor levelheightaccessor, IRegistry<BiomeBase> iregistry, @Nullable BlendingData blendingdata) {
        this(chunkcoordintpair, chunkconverter, (ChunkSection[]) null, new ProtoChunkTickList(), new ProtoChunkTickList(), levelheightaccessor, iregistry, blendingdata);
    }

    public ProtoChunk(ChunkCoordIntPair chunkcoordintpair, ChunkConverter chunkconverter, @Nullable ChunkSection[] achunksection, ProtoChunkTickList<Block> protochunkticklist, ProtoChunkTickList<FluidType> protochunkticklist1, LevelHeightAccessor levelheightaccessor, IRegistry<BiomeBase> iregistry, @Nullable BlendingData blendingdata) {
        super(chunkcoordintpair, chunkconverter, levelheightaccessor, iregistry, 0L, achunksection, blendingdata);
        this.status = ChunkStatus.EMPTY;
        this.entities = Lists.newArrayList();
        this.blockTicks = protochunkticklist;
        this.fluidTicks = protochunkticklist1;
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public TickContainerAccess<FluidType> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public IChunkAccess.a getTicksForSerialization(long i) {
        return new IChunkAccess.a(this.blockTicks.pack(i), this.fluidTicks.pack(i));
    }

    @Override
    public IBlockData getBlockState(BlockPosition blockposition) {
        int i = blockposition.getY();

        if (this.isOutsideBuildHeight(i)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            ChunkSection chunksection = this.getSection(this.getSectionIndex(i));

            return chunksection.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : chunksection.getBlockState(blockposition.getX() & 15, i & 15, blockposition.getZ() & 15);
        }
    }

    @Override
    public Fluid getFluidState(BlockPosition blockposition) {
        int i = blockposition.getY();

        if (this.isOutsideBuildHeight(i)) {
            return FluidTypes.EMPTY.defaultFluidState();
        } else {
            ChunkSection chunksection = this.getSection(this.getSectionIndex(i));

            return chunksection.hasOnlyAir() ? FluidTypes.EMPTY.defaultFluidState() : chunksection.getFluidState(blockposition.getX() & 15, i & 15, blockposition.getZ() & 15);
        }
    }

    @Nullable
    @Override
    public IBlockData setBlockState(BlockPosition blockposition, IBlockData iblockdata, int i) {
        int j = blockposition.getX();
        int k = blockposition.getY();
        int l = blockposition.getZ();

        if (this.isOutsideBuildHeight(k)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            int i1 = this.getSectionIndex(k);
            ChunkSection chunksection = this.getSection(i1);
            boolean flag = chunksection.hasOnlyAir();

            if (flag && iblockdata.is(Blocks.AIR)) {
                return iblockdata;
            } else {
                int j1 = SectionPosition.sectionRelative(j);
                int k1 = SectionPosition.sectionRelative(k);
                int l1 = SectionPosition.sectionRelative(l);
                IBlockData iblockdata1 = chunksection.setBlockState(j1, k1, l1, iblockdata);

                if (this.status.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                    boolean flag1 = chunksection.hasOnlyAir();

                    if (flag1 != flag) {
                        this.lightEngine.updateSectionStatus(blockposition, flag1);
                    }

                    if (LightEngine.hasDifferentLightProperties(iblockdata1, iblockdata)) {
                        this.skyLightSources.update(this, j1, k, l1);
                        this.lightEngine.checkBlock(blockposition);
                    }
                }

                EnumSet<HeightMap.Type> enumset = this.getPersistedStatus().heightmapsAfter();
                EnumSet<HeightMap.Type> enumset1 = null;

                for (HeightMap.Type heightmap_type : enumset) {
                    HeightMap heightmap = (HeightMap) this.heightmaps.get(heightmap_type);

                    if (heightmap == null) {
                        if (enumset1 == null) {
                            enumset1 = EnumSet.noneOf(HeightMap.Type.class);
                        }

                        enumset1.add(heightmap_type);
                    }
                }

                if (enumset1 != null) {
                    HeightMap.primeHeightmaps(this, enumset1);
                }

                for (HeightMap.Type heightmap_type1 : enumset) {
                    ((HeightMap) this.heightmaps.get(heightmap_type1)).update(j1, k, l1, iblockdata);
                }

                return iblockdata1;
            }
        }
    }

    @Override
    public void setBlockEntity(TileEntity tileentity) {
        this.pendingBlockEntities.remove(tileentity.getBlockPos());
        this.blockEntities.put(tileentity.getBlockPos(), tileentity);
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPosition blockposition) {
        return (TileEntity) this.blockEntities.get(blockposition);
    }

    public Map<BlockPosition, TileEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void addEntity(NBTTagCompound nbttagcompound) {
        this.entities.add(nbttagcompound);
    }

    @Override
    public void addEntity(Entity entity) {
        if (!entity.isPassenger()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            entity.save(nbttagcompound);
            this.addEntity(nbttagcompound);
        }
    }

    @Override
    public void setStartForStructure(Structure structure, StructureStart structurestart) {
        BelowZeroRetrogen belowzeroretrogen = this.getBelowZeroRetrogen();

        if (belowzeroretrogen != null && structurestart.isValid()) {
            StructureBoundingBox structureboundingbox = structurestart.getBoundingBox();
            LevelHeightAccessor levelheightaccessor = this.getHeightAccessorForGeneration();

            if (structureboundingbox.minY() < levelheightaccessor.getMinY() || structureboundingbox.maxY() > levelheightaccessor.getMaxY()) {
                return;
            }
        }

        super.setStartForStructure(structure, structurestart);
    }

    public List<NBTTagCompound> getEntities() {
        return this.entities;
    }

    @Override
    public ChunkStatus getPersistedStatus() {
        return this.status;
    }

    public void setPersistedStatus(ChunkStatus chunkstatus) {
        this.status = chunkstatus;
        if (this.belowZeroRetrogen != null && chunkstatus.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
            this.setBelowZeroRetrogen((BelowZeroRetrogen) null);
        }

        this.markUnsaved();
    }

    @Override
    public Holder<BiomeBase> getNoiseBiome(int i, int j, int k) {
        if (this.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
            return super.getNoiseBiome(i, j, k);
        } else {
            throw new IllegalStateException("Asking for biomes before we have biomes");
        }
    }

    public static short packOffsetCoordinates(BlockPosition blockposition) {
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();
        int l = i & 15;
        int i1 = j & 15;
        int j1 = k & 15;

        return (short) (l | i1 << 4 | j1 << 8);
    }

    public static BlockPosition unpackOffsetCoordinates(short short0, int i, ChunkCoordIntPair chunkcoordintpair) {
        int j = SectionPosition.sectionToBlockCoord(chunkcoordintpair.x, short0 & 15);
        int k = SectionPosition.sectionToBlockCoord(i, short0 >>> 4 & 15);
        int l = SectionPosition.sectionToBlockCoord(chunkcoordintpair.z, short0 >>> 8 & 15);

        return new BlockPosition(j, k, l);
    }

    @Override
    public void markPosForPostprocessing(BlockPosition blockposition) {
        if (!this.isOutsideBuildHeight(blockposition)) {
            IChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(blockposition.getY())).add(packOffsetCoordinates(blockposition));
        }

    }

    @Override
    public void addPackedPostProcess(ShortList shortlist, int i) {
        IChunkAccess.getOrCreateOffsetList(this.postProcessing, i).addAll(shortlist);
    }

    public Map<BlockPosition, NBTTagCompound> getBlockEntityNbts() {
        return Collections.unmodifiableMap(this.pendingBlockEntities);
    }

    @Nullable
    @Override
    public NBTTagCompound getBlockEntityNbtForSaving(BlockPosition blockposition, HolderLookup.a holderlookup_a) {
        TileEntity tileentity = this.getBlockEntity(blockposition);

        return tileentity != null ? tileentity.saveWithFullMetadata(holderlookup_a) : (NBTTagCompound) this.pendingBlockEntities.get(blockposition);
    }

    @Override
    public void removeBlockEntity(BlockPosition blockposition) {
        this.blockEntities.remove(blockposition);
        this.pendingBlockEntities.remove(blockposition);
    }

    @Nullable
    public CarvingMask getCarvingMask() {
        return this.carvingMask;
    }

    public CarvingMask getOrCreateCarvingMask() {
        if (this.carvingMask == null) {
            this.carvingMask = new CarvingMask(this.getHeight(), this.getMinY());
        }

        return this.carvingMask;
    }

    public void setCarvingMask(CarvingMask carvingmask) {
        this.carvingMask = carvingmask;
    }

    public void setLightEngine(LevelLightEngine levellightengine) {
        this.lightEngine = levellightengine;
    }

    public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowzeroretrogen) {
        this.belowZeroRetrogen = belowzeroretrogen;
    }

    @Nullable
    @Override
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return this.belowZeroRetrogen;
    }

    private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTickList<T> protochunkticklist) {
        return new LevelChunkTicks<T>(protochunkticklist.scheduledTicks());
    }

    public LevelChunkTicks<Block> unpackBlockTicks() {
        return unpackTicks(this.blockTicks);
    }

    public LevelChunkTicks<FluidType> unpackFluidTicks() {
        return unpackTicks(this.fluidTicks);
    }

    @Override
    public LevelHeightAccessor getHeightAccessorForGeneration() {
        return (LevelHeightAccessor) (this.isUpgrading() ? BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR : this);
    }
}
