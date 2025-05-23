package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.WorldGenFeatureStructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructureJigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolStructure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class WorldGenFeaturePillagerOutpostPoolPiece extends StructurePiece {

    protected final WorldGenFeatureDefinedStructurePoolStructure element;
    protected BlockPosition position;
    private final int groundLevelDelta;
    protected final EnumBlockRotation rotation;
    private final List<WorldGenFeatureDefinedStructureJigsawJunction> junctions = Lists.newArrayList();
    private final StructureTemplateManager structureTemplateManager;
    private final LiquidSettings liquidSettings;

    public WorldGenFeaturePillagerOutpostPoolPiece(StructureTemplateManager structuretemplatemanager, WorldGenFeatureDefinedStructurePoolStructure worldgenfeaturedefinedstructurepoolstructure, BlockPosition blockposition, int i, EnumBlockRotation enumblockrotation, StructureBoundingBox structureboundingbox, LiquidSettings liquidsettings) {
        super(WorldGenFeatureStructurePieceType.JIGSAW, 0, structureboundingbox);
        this.structureTemplateManager = structuretemplatemanager;
        this.element = worldgenfeaturedefinedstructurepoolstructure;
        this.position = blockposition;
        this.groundLevelDelta = i;
        this.rotation = enumblockrotation;
        this.liquidSettings = liquidsettings;
    }

    public WorldGenFeaturePillagerOutpostPoolPiece(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound) {
        super(WorldGenFeatureStructurePieceType.JIGSAW, nbttagcompound);
        this.structureTemplateManager = structurepieceserializationcontext.structureTemplateManager();
        this.position = new BlockPosition(nbttagcompound.getIntOr("PosX", 0), nbttagcompound.getIntOr("PosY", 0), nbttagcompound.getIntOr("PosZ", 0));
        this.groundLevelDelta = nbttagcompound.getIntOr("ground_level_delta", 0);
        DynamicOps<NBTBase> dynamicops = structurepieceserializationcontext.registryAccess().<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

        this.element = (WorldGenFeatureDefinedStructurePoolStructure) nbttagcompound.read("pool_element", WorldGenFeatureDefinedStructurePoolStructure.CODEC, dynamicops).orElseThrow(() -> {
            return new IllegalStateException("Invalid pool element found");
        });
        this.rotation = (EnumBlockRotation) nbttagcompound.read("rotation", EnumBlockRotation.LEGACY_CODEC).orElseThrow();
        this.boundingBox = this.element.getBoundingBox(this.structureTemplateManager, this.position, this.rotation);
        NBTTagList nbttaglist = nbttagcompound.getListOrEmpty("junctions");

        this.junctions.clear();
        nbttaglist.forEach((nbtbase) -> {
            this.junctions.add(WorldGenFeatureDefinedStructureJigsawJunction.deserialize(new Dynamic(dynamicops, nbtbase)));
        });
        this.liquidSettings = (LiquidSettings) nbttagcompound.read("liquid_settings", LiquidSettings.CODEC).orElse(JigsawStructure.DEFAULT_LIQUID_SETTINGS);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound) {
        nbttagcompound.putInt("PosX", this.position.getX());
        nbttagcompound.putInt("PosY", this.position.getY());
        nbttagcompound.putInt("PosZ", this.position.getZ());
        nbttagcompound.putInt("ground_level_delta", this.groundLevelDelta);
        DynamicOps<NBTBase> dynamicops = structurepieceserializationcontext.registryAccess().<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

        nbttagcompound.store("pool_element", WorldGenFeatureDefinedStructurePoolStructure.CODEC, dynamicops, this.element);
        nbttagcompound.store("rotation", EnumBlockRotation.LEGACY_CODEC, this.rotation);
        NBTTagList nbttaglist = new NBTTagList();

        for (WorldGenFeatureDefinedStructureJigsawJunction worldgenfeaturedefinedstructurejigsawjunction : this.junctions) {
            nbttaglist.add((NBTBase) worldgenfeaturedefinedstructurejigsawjunction.serialize(dynamicops).getValue());
        }

        nbttagcompound.put("junctions", nbttaglist);
        if (this.liquidSettings != JigsawStructure.DEFAULT_LIQUID_SETTINGS) {
            nbttagcompound.store("liquid_settings", LiquidSettings.CODEC, dynamicops, this.liquidSettings);
        }

    }

    @Override
    public void postProcess(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, StructureBoundingBox structureboundingbox, ChunkCoordIntPair chunkcoordintpair, BlockPosition blockposition) {
        this.place(generatoraccessseed, structuremanager, chunkgenerator, randomsource, structureboundingbox, blockposition, false);
    }

    public void place(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, StructureBoundingBox structureboundingbox, BlockPosition blockposition, boolean flag) {
        this.element.place(this.structureTemplateManager, generatoraccessseed, structuremanager, chunkgenerator, this.position, blockposition, this.rotation, structureboundingbox, randomsource, this.liquidSettings, flag);
    }

    @Override
    public void move(int i, int j, int k) {
        super.move(i, j, k);
        this.position = this.position.offset(i, j, k);
    }

    @Override
    public EnumBlockRotation getRotation() {
        return this.rotation;
    }

    public String toString() {
        return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
    }

    public WorldGenFeatureDefinedStructurePoolStructure getElement() {
        return this.element;
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(WorldGenFeatureDefinedStructureJigsawJunction worldgenfeaturedefinedstructurejigsawjunction) {
        this.junctions.add(worldgenfeaturedefinedstructurejigsawjunction);
    }

    public List<WorldGenFeatureDefinedStructureJigsawJunction> getJunctions() {
        return this.junctions;
    }
}
