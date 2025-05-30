package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.BlockPropertyJigsawOrientation;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.WorldGenFeaturePieces;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.BlockJigsaw;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntityJigsaw;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class WorldGenFeatureDefinedStructurePoolFeature extends WorldGenFeatureDefinedStructurePoolStructure {

    public static final MapCodec<WorldGenFeatureDefinedStructurePoolFeature> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(PlacedFeature.CODEC.fieldOf("feature").forGetter((worldgenfeaturedefinedstructurepoolfeature) -> {
            return worldgenfeaturedefinedstructurepoolfeature.feature;
        }), projectionCodec()).apply(instance, WorldGenFeatureDefinedStructurePoolFeature::new);
    });
    private static final MinecraftKey DEFAULT_JIGSAW_NAME = MinecraftKey.withDefaultNamespace("bottom");
    private final Holder<PlacedFeature> feature;
    private final NBTTagCompound defaultJigsawNBT;

    protected WorldGenFeatureDefinedStructurePoolFeature(Holder<PlacedFeature> holder, WorldGenFeatureDefinedStructurePoolTemplate.Matching worldgenfeaturedefinedstructurepooltemplate_matching) {
        super(worldgenfeaturedefinedstructurepooltemplate_matching);
        this.feature = holder;
        this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
    }

    private NBTTagCompound fillDefaultJigsawNBT() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.store("name", MinecraftKey.CODEC, WorldGenFeatureDefinedStructurePoolFeature.DEFAULT_JIGSAW_NAME);
        nbttagcompound.putString("final_state", "minecraft:air");
        nbttagcompound.store("pool", TileEntityJigsaw.POOL_CODEC, WorldGenFeaturePieces.EMPTY);
        nbttagcompound.store("target", MinecraftKey.CODEC, TileEntityJigsaw.EMPTY_ID);
        nbttagcompound.store("joint", TileEntityJigsaw.JointType.CODEC, TileEntityJigsaw.JointType.ROLLABLE);
        return nbttagcompound;
    }

    @Override
    public BaseBlockPosition getSize(StructureTemplateManager structuretemplatemanager, EnumBlockRotation enumblockrotation) {
        return BaseBlockPosition.ZERO;
    }

    @Override
    public List<DefinedStructure.a> getShuffledJigsawBlocks(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, EnumBlockRotation enumblockrotation, RandomSource randomsource) {
        return List.of(DefinedStructure.a.of(new DefinedStructure.BlockInfo(blockposition, (IBlockData) Blocks.JIGSAW.defaultBlockState().setValue(BlockJigsaw.ORIENTATION, BlockPropertyJigsawOrientation.fromFrontAndTop(EnumDirection.DOWN, EnumDirection.SOUTH)), this.defaultJigsawNBT)));
    }

    @Override
    public StructureBoundingBox getBoundingBox(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, EnumBlockRotation enumblockrotation) {
        BaseBlockPosition baseblockposition = this.getSize(structuretemplatemanager, enumblockrotation);

        return new StructureBoundingBox(blockposition.getX(), blockposition.getY(), blockposition.getZ(), blockposition.getX() + baseblockposition.getX(), blockposition.getY() + baseblockposition.getY(), blockposition.getZ() + baseblockposition.getZ());
    }

    @Override
    public boolean place(StructureTemplateManager structuretemplatemanager, GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, BlockPosition blockposition, BlockPosition blockposition1, EnumBlockRotation enumblockrotation, StructureBoundingBox structureboundingbox, RandomSource randomsource, LiquidSettings liquidsettings, boolean flag) {
        return ((PlacedFeature) this.feature.value()).place(generatoraccessseed, chunkgenerator, randomsource, blockposition);
    }

    @Override
    public WorldGenFeatureDefinedStructurePools<?> getType() {
        return WorldGenFeatureDefinedStructurePools.FEATURE;
    }

    public String toString() {
        return "Feature[" + String.valueOf(this.feature) + "]";
    }
}
