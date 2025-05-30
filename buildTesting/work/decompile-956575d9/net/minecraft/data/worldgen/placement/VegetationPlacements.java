package net.minecraft.data.worldgen.placement;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public class VegetationPlacements {

    public static final ResourceKey<PlacedFeature> BAMBOO_LIGHT = PlacementUtils.createKey("bamboo_light");
    public static final ResourceKey<PlacedFeature> BAMBOO = PlacementUtils.createKey("bamboo");
    public static final ResourceKey<PlacedFeature> VINES = PlacementUtils.createKey("vines");
    public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = PlacementUtils.createKey("patch_sunflower");
    public static final ResourceKey<PlacedFeature> PATCH_PUMPKIN = PlacementUtils.createKey("patch_pumpkin");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = PlacementUtils.createKey("patch_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_MEADOW = PlacementUtils.createKey("patch_grass_meadow");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_FOREST = PlacementUtils.createKey("patch_grass_forest");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_BADLANDS = PlacementUtils.createKey("patch_grass_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_SAVANNA = PlacementUtils.createKey("patch_grass_savanna");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_NORMAL = PlacementUtils.createKey("patch_grass_normal");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA_2 = PlacementUtils.createKey("patch_grass_taiga_2");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA = PlacementUtils.createKey("patch_grass_taiga");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_JUNGLE = PlacementUtils.createKey("patch_grass_jungle");
    public static final ResourceKey<PlacedFeature> GRASS_BONEMEAL = PlacementUtils.createKey("grass_bonemeal");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_2 = PlacementUtils.createKey("patch_dead_bush_2");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH = PlacementUtils.createKey("patch_dead_bush");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_BADLANDS = PlacementUtils.createKey("patch_dead_bush_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_DRY_GRASS_BADLANDS = PlacementUtils.createKey("patch_dry_grass_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_DRY_GRASS_DESERT = PlacementUtils.createKey("patch_dry_grass_desert");
    public static final ResourceKey<PlacedFeature> PATCH_MELON = PlacementUtils.createKey("patch_melon");
    public static final ResourceKey<PlacedFeature> PATCH_MELON_SPARSE = PlacementUtils.createKey("patch_melon_sparse");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_COMMON = PlacementUtils.createKey("patch_berry_common");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_RARE = PlacementUtils.createKey("patch_berry_rare");
    public static final ResourceKey<PlacedFeature> PATCH_WATERLILY = PlacementUtils.createKey("patch_waterlily");
    public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS_2 = PlacementUtils.createKey("patch_tall_grass_2");
    public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS = PlacementUtils.createKey("patch_tall_grass");
    public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = PlacementUtils.createKey("patch_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_BUSH = PlacementUtils.createKey("patch_bush");
    public static final ResourceKey<PlacedFeature> PATCH_LEAF_LITTER = PlacementUtils.createKey("patch_leaf_litter");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DESERT = PlacementUtils.createKey("patch_cactus_desert");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DECORATED = PlacementUtils.createKey("patch_cactus_decorated");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_SWAMP = PlacementUtils.createKey("patch_sugar_cane_swamp");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_DESERT = PlacementUtils.createKey("patch_sugar_cane_desert");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_BADLANDS = PlacementUtils.createKey("patch_sugar_cane_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE = PlacementUtils.createKey("patch_sugar_cane");
    public static final ResourceKey<PlacedFeature> PATCH_FIREFLY_BUSH_SWAMP = PlacementUtils.createKey("patch_firefly_bush_swamp");
    public static final ResourceKey<PlacedFeature> PATCH_FIREFLY_BUSH_NEAR_WATER_SWAMP = PlacementUtils.createKey("patch_firefly_bush_near_water_swamp");
    public static final ResourceKey<PlacedFeature> PATCH_FIREFLY_BUSH_NEAR_WATER = PlacementUtils.createKey("patch_firefly_bush_near_water");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NETHER = PlacementUtils.createKey("brown_mushroom_nether");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NETHER = PlacementUtils.createKey("red_mushroom_nether");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NORMAL = PlacementUtils.createKey("brown_mushroom_normal");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NORMAL = PlacementUtils.createKey("red_mushroom_normal");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_TAIGA = PlacementUtils.createKey("brown_mushroom_taiga");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_TAIGA = PlacementUtils.createKey("red_mushroom_taiga");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("brown_mushroom_old_growth");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("red_mushroom_old_growth");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_SWAMP = PlacementUtils.createKey("brown_mushroom_swamp");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_SWAMP = PlacementUtils.createKey("red_mushroom_swamp");
    public static final ResourceKey<PlacedFeature> FLOWER_WARM = PlacementUtils.createKey("flower_warm");
    public static final ResourceKey<PlacedFeature> FLOWER_DEFAULT = PlacementUtils.createKey("flower_default");
    public static final ResourceKey<PlacedFeature> FLOWER_FLOWER_FOREST = PlacementUtils.createKey("flower_flower_forest");
    public static final ResourceKey<PlacedFeature> FLOWER_SWAMP = PlacementUtils.createKey("flower_swamp");
    public static final ResourceKey<PlacedFeature> FLOWER_PLAINS = PlacementUtils.createKey("flower_plains");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = PlacementUtils.createKey("flower_meadow");
    public static final ResourceKey<PlacedFeature> FLOWER_CHERRY = PlacementUtils.createKey("flower_cherry");
    public static final ResourceKey<PlacedFeature> FLOWER_PALE_GARDEN = PlacementUtils.createKey("flower_pale_garden");
    public static final ResourceKey<PlacedFeature> WILDFLOWERS_BIRCH_FOREST = PlacementUtils.createKey("wildflowers_birch_forest");
    public static final ResourceKey<PlacedFeature> WILDFLOWERS_MEADOW = PlacementUtils.createKey("wildflowers_meadow");
    public static final ResourceKey<PlacedFeature> TREES_PLAINS = PlacementUtils.createKey("trees_plains");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_VEGETATION = PlacementUtils.createKey("dark_forest_vegetation");
    public static final ResourceKey<PlacedFeature> PALE_GARDEN_VEGETATION = PlacementUtils.createKey("pale_garden_vegetation");
    public static final ResourceKey<PlacedFeature> FLOWER_FOREST_FLOWERS = PlacementUtils.createKey("flower_forest_flowers");
    public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = PlacementUtils.createKey("forest_flowers");
    public static final ResourceKey<PlacedFeature> PALE_GARDEN_FLOWERS = PlacementUtils.createKey("pale_garden_flowers");
    public static final ResourceKey<PlacedFeature> PALE_MOSS_PATCH = PlacementUtils.createKey("pale_moss_patch");
    public static final ResourceKey<PlacedFeature> TREES_FLOWER_FOREST = PlacementUtils.createKey("trees_flower_forest");
    public static final ResourceKey<PlacedFeature> TREES_MEADOW = PlacementUtils.createKey("trees_meadow");
    public static final ResourceKey<PlacedFeature> TREES_CHERRY = PlacementUtils.createKey("trees_cherry");
    public static final ResourceKey<PlacedFeature> TREES_TAIGA = PlacementUtils.createKey("trees_taiga");
    public static final ResourceKey<PlacedFeature> TREES_GROVE = PlacementUtils.createKey("trees_grove");
    public static final ResourceKey<PlacedFeature> TREES_BADLANDS = PlacementUtils.createKey("trees_badlands");
    public static final ResourceKey<PlacedFeature> TREES_SNOWY = PlacementUtils.createKey("trees_snowy");
    public static final ResourceKey<PlacedFeature> TREES_SWAMP = PlacementUtils.createKey("trees_swamp");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_SAVANNA = PlacementUtils.createKey("trees_windswept_savanna");
    public static final ResourceKey<PlacedFeature> TREES_SAVANNA = PlacementUtils.createKey("trees_savanna");
    public static final ResourceKey<PlacedFeature> BIRCH_TALL = PlacementUtils.createKey("birch_tall");
    public static final ResourceKey<PlacedFeature> TREES_BIRCH = PlacementUtils.createKey("trees_birch");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_FOREST = PlacementUtils.createKey("trees_windswept_forest");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_HILLS = PlacementUtils.createKey("trees_windswept_hills");
    public static final ResourceKey<PlacedFeature> TREES_WATER = PlacementUtils.createKey("trees_water");
    public static final ResourceKey<PlacedFeature> TREES_BIRCH_AND_OAK_LEAF_LITTER = PlacementUtils.createKey("trees_birch_and_oak_leaf_litter");
    public static final ResourceKey<PlacedFeature> TREES_SPARSE_JUNGLE = PlacementUtils.createKey("trees_sparse_jungle");
    public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacementUtils.createKey("trees_old_growth_spruce_taiga");
    public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_PINE_TAIGA = PlacementUtils.createKey("trees_old_growth_pine_taiga");
    public static final ResourceKey<PlacedFeature> TREES_JUNGLE = PlacementUtils.createKey("trees_jungle");
    public static final ResourceKey<PlacedFeature> BAMBOO_VEGETATION = PlacementUtils.createKey("bamboo_vegetation");
    public static final ResourceKey<PlacedFeature> MUSHROOM_ISLAND_VEGETATION = PlacementUtils.createKey("mushroom_island_vegetation");
    public static final ResourceKey<PlacedFeature> TREES_MANGROVE = PlacementUtils.createKey("trees_mangrove");
    private static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);

    public VegetationPlacements() {}

    public static List<PlacementModifier> worldSurfaceSquaredWithCount(int i) {
        return List.of(CountPlacement.of(i), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    }

    private static List<PlacementModifier> getMushroomPlacement(int i, @Nullable PlacementModifier placementmodifier) {
        ImmutableList.Builder<PlacementModifier> immutablelist_builder = ImmutableList.builder();

        if (placementmodifier != null) {
            immutablelist_builder.add(placementmodifier);
        }

        if (i != 0) {
            immutablelist_builder.add(RarityFilter.onAverageOnceEvery(i));
        }

        immutablelist_builder.add(InSquarePlacement.spread());
        immutablelist_builder.add(PlacementUtils.HEIGHTMAP);
        immutablelist_builder.add(BiomeFilter.biome());
        return immutablelist_builder.build();
    }

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase(PlacementModifier placementmodifier) {
        return ImmutableList.builder().add(placementmodifier).add(InSquarePlacement.spread()).add(VegetationPlacements.TREE_THRESHOLD).add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR).add(BiomeFilter.biome());
    }

    public static List<PlacementModifier> treePlacement(PlacementModifier placementmodifier) {
        return treePlacementBase(placementmodifier).build();
    }

    public static List<PlacementModifier> treePlacement(PlacementModifier placementmodifier, Block block) {
        return treePlacementBase(placementmodifier).add(BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPosition.ZERO))).build();
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapcontext) {
        HolderGetter<WorldGenFeatureConfigured<?, ?>> holdergetter = bootstrapcontext.<WorldGenFeatureConfigured<?, ?>>lookup(Registries.CONFIGURED_FEATURE);
        Holder<WorldGenFeatureConfigured<?, ?>> holder = holdergetter.getOrThrow(VegetationFeatures.BAMBOO_NO_PODZOL);
        Holder<WorldGenFeatureConfigured<?, ?>> holder1 = holdergetter.getOrThrow(VegetationFeatures.BAMBOO_SOME_PODZOL);
        Holder<WorldGenFeatureConfigured<?, ?>> holder2 = holdergetter.getOrThrow(VegetationFeatures.VINES);
        Holder<WorldGenFeatureConfigured<?, ?>> holder3 = holdergetter.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
        Holder<WorldGenFeatureConfigured<?, ?>> holder4 = holdergetter.getOrThrow(VegetationFeatures.PATCH_PUMPKIN);
        Holder<WorldGenFeatureConfigured<?, ?>> holder5 = holdergetter.getOrThrow(VegetationFeatures.PATCH_GRASS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder6 = holdergetter.getOrThrow(VegetationFeatures.PATCH_GRASS_MEADOW);
        Holder<WorldGenFeatureConfigured<?, ?>> holder7 = holdergetter.getOrThrow(VegetationFeatures.PATCH_LEAF_LITTER);
        Holder<WorldGenFeatureConfigured<?, ?>> holder8 = holdergetter.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder9 = holdergetter.getOrThrow(VegetationFeatures.PATCH_GRASS_JUNGLE);
        Holder<WorldGenFeatureConfigured<?, ?>> holder10 = holdergetter.getOrThrow(VegetationFeatures.SINGLE_PIECE_OF_GRASS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder11 = holdergetter.getOrThrow(VegetationFeatures.PATCH_DEAD_BUSH);
        Holder<WorldGenFeatureConfigured<?, ?>> holder12 = holdergetter.getOrThrow(VegetationFeatures.PATCH_DRY_GRASS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder13 = holdergetter.getOrThrow(VegetationFeatures.PATCH_FIREFLY_BUSH);
        Holder<WorldGenFeatureConfigured<?, ?>> holder14 = holdergetter.getOrThrow(VegetationFeatures.PATCH_MELON);
        Holder<WorldGenFeatureConfigured<?, ?>> holder15 = holdergetter.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
        Holder<WorldGenFeatureConfigured<?, ?>> holder16 = holdergetter.getOrThrow(VegetationFeatures.PATCH_WATERLILY);
        Holder<WorldGenFeatureConfigured<?, ?>> holder17 = holdergetter.getOrThrow(VegetationFeatures.PATCH_TALL_GRASS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder18 = holdergetter.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
        Holder<WorldGenFeatureConfigured<?, ?>> holder19 = holdergetter.getOrThrow(VegetationFeatures.PATCH_BUSH);
        Holder<WorldGenFeatureConfigured<?, ?>> holder20 = holdergetter.getOrThrow(VegetationFeatures.PATCH_CACTUS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder21 = holdergetter.getOrThrow(VegetationFeatures.PATCH_SUGAR_CANE);
        Holder<WorldGenFeatureConfigured<?, ?>> holder22 = holdergetter.getOrThrow(VegetationFeatures.PATCH_BROWN_MUSHROOM);
        Holder<WorldGenFeatureConfigured<?, ?>> holder23 = holdergetter.getOrThrow(VegetationFeatures.PATCH_RED_MUSHROOM);
        Holder<WorldGenFeatureConfigured<?, ?>> holder24 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_DEFAULT);
        Holder<WorldGenFeatureConfigured<?, ?>> holder25 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_FLOWER_FOREST);
        Holder<WorldGenFeatureConfigured<?, ?>> holder26 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_SWAMP);
        Holder<WorldGenFeatureConfigured<?, ?>> holder27 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
        Holder<WorldGenFeatureConfigured<?, ?>> holder28 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_MEADOW);
        Holder<WorldGenFeatureConfigured<?, ?>> holder29 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_CHERRY);
        Holder<WorldGenFeatureConfigured<?, ?>> holder30 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_PALE_GARDEN);
        Holder<WorldGenFeatureConfigured<?, ?>> holder31 = holdergetter.getOrThrow(VegetationFeatures.WILDFLOWERS_BIRCH_FOREST);
        Holder<WorldGenFeatureConfigured<?, ?>> holder32 = holdergetter.getOrThrow(VegetationFeatures.WILDFLOWERS_MEADOW);
        Holder<WorldGenFeatureConfigured<?, ?>> holder33 = holdergetter.getOrThrow(VegetationFeatures.TREES_PLAINS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder34 = holdergetter.getOrThrow(VegetationFeatures.DARK_FOREST_VEGETATION);
        Holder<WorldGenFeatureConfigured<?, ?>> holder35 = holdergetter.getOrThrow(VegetationFeatures.PALE_GARDEN_VEGETATION);
        Holder<WorldGenFeatureConfigured<?, ?>> holder36 = holdergetter.getOrThrow(VegetationFeatures.FOREST_FLOWERS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder37 = holdergetter.getOrThrow(VegetationFeatures.PALE_FOREST_FLOWERS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder38 = holdergetter.getOrThrow(VegetationFeatures.PALE_MOSS_PATCH);
        Holder<WorldGenFeatureConfigured<?, ?>> holder39 = holdergetter.getOrThrow(VegetationFeatures.TREES_FLOWER_FOREST);
        Holder<WorldGenFeatureConfigured<?, ?>> holder40 = holdergetter.getOrThrow(VegetationFeatures.MEADOW_TREES);
        Holder<WorldGenFeatureConfigured<?, ?>> holder41 = holdergetter.getOrThrow(VegetationFeatures.TREES_TAIGA);
        Holder<WorldGenFeatureConfigured<?, ?>> holder42 = holdergetter.getOrThrow(VegetationFeatures.TREES_BADLANDS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder43 = holdergetter.getOrThrow(VegetationFeatures.TREES_GROVE);
        Holder<WorldGenFeatureConfigured<?, ?>> holder44 = holdergetter.getOrThrow(VegetationFeatures.TREES_SNOWY);
        Holder<WorldGenFeatureConfigured<?, ?>> holder45 = holdergetter.getOrThrow(TreeFeatures.CHERRY_BEES_005);
        Holder<WorldGenFeatureConfigured<?, ?>> holder46 = holdergetter.getOrThrow(TreeFeatures.SWAMP_OAK);
        Holder<WorldGenFeatureConfigured<?, ?>> holder47 = holdergetter.getOrThrow(VegetationFeatures.TREES_SAVANNA);
        Holder<WorldGenFeatureConfigured<?, ?>> holder48 = holdergetter.getOrThrow(VegetationFeatures.BIRCH_TALL);
        Holder<WorldGenFeatureConfigured<?, ?>> holder49 = holdergetter.getOrThrow(VegetationFeatures.TREES_BIRCH);
        Holder<WorldGenFeatureConfigured<?, ?>> holder50 = holdergetter.getOrThrow(VegetationFeatures.TREES_WINDSWEPT_HILLS);
        Holder<WorldGenFeatureConfigured<?, ?>> holder51 = holdergetter.getOrThrow(VegetationFeatures.TREES_WATER);
        Holder<WorldGenFeatureConfigured<?, ?>> holder52 = holdergetter.getOrThrow(VegetationFeatures.TREES_BIRCH_AND_OAK_LEAF_LITTER);
        Holder<WorldGenFeatureConfigured<?, ?>> holder53 = holdergetter.getOrThrow(VegetationFeatures.TREES_SPARSE_JUNGLE);
        Holder<WorldGenFeatureConfigured<?, ?>> holder54 = holdergetter.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA);
        Holder<WorldGenFeatureConfigured<?, ?>> holder55 = holdergetter.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
        Holder<WorldGenFeatureConfigured<?, ?>> holder56 = holdergetter.getOrThrow(VegetationFeatures.TREES_JUNGLE);
        Holder<WorldGenFeatureConfigured<?, ?>> holder57 = holdergetter.getOrThrow(VegetationFeatures.BAMBOO_VEGETATION);
        Holder<WorldGenFeatureConfigured<?, ?>> holder58 = holdergetter.getOrThrow(VegetationFeatures.MUSHROOM_ISLAND_VEGETATION);
        Holder<WorldGenFeatureConfigured<?, ?>> holder59 = holdergetter.getOrThrow(VegetationFeatures.MANGROVE_VEGETATION);

        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BAMBOO_LIGHT, holder, RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BAMBOO, holder1, NoiseBasedCountPlacement.of(160, 80.0D, 0.3D), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.VINES, holder2, CountPlacement.of(127), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_SUNFLOWER, holder3, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_PUMPKIN, holder4, RarityFilter.onAverageOnceEvery(300), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_PLAIN, holder5, NoiseThresholdCountPlacement.of(-0.8D, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_MEADOW, holder6, NoiseThresholdCountPlacement.of(-0.8D, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_FOREST, holder5, worldSurfaceSquaredWithCount(2));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_LEAF_LITTER, holder7, worldSurfaceSquaredWithCount(2));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_BADLANDS, holder5, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_SAVANNA, holder5, worldSurfaceSquaredWithCount(20));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_NORMAL, holder5, worldSurfaceSquaredWithCount(5));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_TAIGA_2, holder8, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_TAIGA, holder8, worldSurfaceSquaredWithCount(7));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_GRASS_JUNGLE, holder9, worldSurfaceSquaredWithCount(25));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.GRASS_BONEMEAL, holder10, PlacementUtils.isEmpty());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_DEAD_BUSH_2, holder11, worldSurfaceSquaredWithCount(2));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_DEAD_BUSH, holder11, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_DEAD_BUSH_BADLANDS, holder11, worldSurfaceSquaredWithCount(20));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_DRY_GRASS_BADLANDS, holder12, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_DRY_GRASS_DESERT, holder12, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_MELON, holder14, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_MELON_SPARSE, holder14, RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_BERRY_COMMON, holder15, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_BERRY_RARE, holder15, RarityFilter.onAverageOnceEvery(384), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_WATERLILY, holder16, worldSurfaceSquaredWithCount(4));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_TALL_GRASS_2, holder17, NoiseThresholdCountPlacement.of(-0.8D, 0, 7), RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_TALL_GRASS, holder17, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_LARGE_FERN, holder18, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_BUSH, holder19, RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_CACTUS_DESERT, holder20, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_CACTUS_DECORATED, holder20, RarityFilter.onAverageOnceEvery(13), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_SUGAR_CANE_SWAMP, holder21, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_SUGAR_CANE_DESERT, holder21, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_SUGAR_CANE_BADLANDS, holder21, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_SUGAR_CANE, holder21, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER, holder13, CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_NO_LEAVES, BiomeFilter.biome(), VegetationFeatures.nearWaterPredicate(Blocks.FIREFLY_BUSH));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER_SWAMP, holder13, CountPlacement.of(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome(), VegetationFeatures.nearWaterPredicate(Blocks.FIREFLY_BUSH));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PATCH_FIREFLY_BUSH_SWAMP, holder13, RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BROWN_MUSHROOM_NETHER, holder22, RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.RED_MUSHROOM_NETHER, holder23, RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BROWN_MUSHROOM_NORMAL, holder22, getMushroomPlacement(256, (PlacementModifier) null));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.RED_MUSHROOM_NORMAL, holder23, getMushroomPlacement(512, (PlacementModifier) null));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BROWN_MUSHROOM_TAIGA, holder22, getMushroomPlacement(4, (PlacementModifier) null));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.RED_MUSHROOM_TAIGA, holder23, getMushroomPlacement(256, (PlacementModifier) null));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BROWN_MUSHROOM_OLD_GROWTH, holder22, getMushroomPlacement(4, CountPlacement.of(3)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.RED_MUSHROOM_OLD_GROWTH, holder23, getMushroomPlacement(171, (PlacementModifier) null));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BROWN_MUSHROOM_SWAMP, holder22, getMushroomPlacement(0, CountPlacement.of(2)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.RED_MUSHROOM_SWAMP, holder23, getMushroomPlacement(64, (PlacementModifier) null));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_WARM, holder24, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_DEFAULT, holder24, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_FLOWER_FOREST, holder25, CountPlacement.of(3), RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_SWAMP, holder26, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_PLAINS, holder27, NoiseThresholdCountPlacement.of(-0.8D, 15, 4), RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_CHERRY, holder29, NoiseThresholdCountPlacement.of(-0.8D, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_MEADOW, holder28, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_PALE_GARDEN, holder30, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.WILDFLOWERS_BIRCH_FOREST, holder31, CountPlacement.of(3), RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.WILDFLOWERS_MEADOW, holder32, NoiseThresholdCountPlacement.of(-0.8D, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementModifier placementmodifier = SurfaceWaterDepthFilter.forMaxDepth(0);

        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_PLAINS, holder33, PlacementUtils.countExtra(0, 0.05F, 1), InSquarePlacement.spread(), placementmodifier, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPosition.ZERO)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.DARK_FOREST_VEGETATION, holder34, CountPlacement.of(16), InSquarePlacement.spread(), placementmodifier, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PALE_GARDEN_VEGETATION, holder35, CountPlacement.of(16), InSquarePlacement.spread(), placementmodifier, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FLOWER_FOREST_FLOWERS, holder36, RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, CountPlacement.of(ClampedInt.of(UniformInt.of(-1, 3), 0, 3)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.FOREST_FLOWERS, holder36, RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PALE_GARDEN_FLOWERS, holder37, RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_NO_LEAVES, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.PALE_MOSS_PATCH, holder38, CountPlacement.of(1), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_NO_LEAVES, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_FLOWER_FOREST, holder39, treePlacement(PlacementUtils.countExtra(6, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_MEADOW, holder40, treePlacement(RarityFilter.onAverageOnceEvery(100)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_CHERRY, holder45, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.CHERRY_SAPLING));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_TAIGA, holder41, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_GROVE, holder43, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_BADLANDS, holder42, treePlacement(PlacementUtils.countExtra(5, 0.1F, 1), Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_SNOWY, holder44, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1), Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_SWAMP, holder46, PlacementUtils.countExtra(2, 0.1F, 1), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(2), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(), BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPosition.ZERO)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_WINDSWEPT_SAVANNA, holder47, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_SAVANNA, holder47, treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BIRCH_TALL, holder48, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_BIRCH, holder49, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.BIRCH_SAPLING));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_WINDSWEPT_FOREST, holder50, treePlacement(PlacementUtils.countExtra(3, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_WINDSWEPT_HILLS, holder50, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_WATER, holder51, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_BIRCH_AND_OAK_LEAF_LITTER, holder52, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_SPARSE_JUNGLE, holder53, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA, holder54, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA, holder55, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_JUNGLE, holder56, treePlacement(PlacementUtils.countExtra(50, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.BAMBOO_VEGETATION, holder57, treePlacement(PlacementUtils.countExtra(30, 0.1F, 1)));
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.MUSHROOM_ISLAND_VEGETATION, holder58, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapcontext, VegetationPlacements.TREES_MANGROVE, holder59, CountPlacement.of(25), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(5), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(), BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.MANGROVE_PROPAGULE.defaultBlockState(), BlockPosition.ZERO)));
    }
}
