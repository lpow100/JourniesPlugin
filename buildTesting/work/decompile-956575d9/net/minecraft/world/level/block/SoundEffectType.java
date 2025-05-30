package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;

public class SoundEffectType {

    public static final SoundEffectType EMPTY = new SoundEffectType(1.0F, 1.0F, SoundEffects.EMPTY, SoundEffects.EMPTY, SoundEffects.EMPTY, SoundEffects.EMPTY, SoundEffects.EMPTY);
    public static final SoundEffectType WOOD = new SoundEffectType(1.0F, 1.0F, SoundEffects.WOOD_BREAK, SoundEffects.WOOD_STEP, SoundEffects.WOOD_PLACE, SoundEffects.WOOD_HIT, SoundEffects.WOOD_FALL);
    public static final SoundEffectType GRAVEL = new SoundEffectType(1.0F, 1.0F, SoundEffects.GRAVEL_BREAK, SoundEffects.GRAVEL_STEP, SoundEffects.GRAVEL_PLACE, SoundEffects.GRAVEL_HIT, SoundEffects.GRAVEL_FALL);
    public static final SoundEffectType GRASS = new SoundEffectType(1.0F, 1.0F, SoundEffects.GRASS_BREAK, SoundEffects.GRASS_STEP, SoundEffects.GRASS_PLACE, SoundEffects.GRASS_HIT, SoundEffects.GRASS_FALL);
    public static final SoundEffectType LILY_PAD = new SoundEffectType(1.0F, 1.0F, SoundEffects.BIG_DRIPLEAF_BREAK, SoundEffects.BIG_DRIPLEAF_STEP, SoundEffects.LILY_PAD_PLACE, SoundEffects.BIG_DRIPLEAF_HIT, SoundEffects.BIG_DRIPLEAF_FALL);
    public static final SoundEffectType STONE = new SoundEffectType(1.0F, 1.0F, SoundEffects.STONE_BREAK, SoundEffects.STONE_STEP, SoundEffects.STONE_PLACE, SoundEffects.STONE_HIT, SoundEffects.STONE_FALL);
    public static final SoundEffectType METAL = new SoundEffectType(1.0F, 1.5F, SoundEffects.METAL_BREAK, SoundEffects.METAL_STEP, SoundEffects.METAL_PLACE, SoundEffects.METAL_HIT, SoundEffects.METAL_FALL);
    public static final SoundEffectType GLASS = new SoundEffectType(1.0F, 1.0F, SoundEffects.GLASS_BREAK, SoundEffects.GLASS_STEP, SoundEffects.GLASS_PLACE, SoundEffects.GLASS_HIT, SoundEffects.GLASS_FALL);
    public static final SoundEffectType WOOL = new SoundEffectType(1.0F, 1.0F, SoundEffects.WOOL_BREAK, SoundEffects.WOOL_STEP, SoundEffects.WOOL_PLACE, SoundEffects.WOOL_HIT, SoundEffects.WOOL_FALL);
    public static final SoundEffectType SAND = new SoundEffectType(1.0F, 1.0F, SoundEffects.SAND_BREAK, SoundEffects.SAND_STEP, SoundEffects.SAND_PLACE, SoundEffects.SAND_HIT, SoundEffects.SAND_FALL);
    public static final SoundEffectType SNOW = new SoundEffectType(1.0F, 1.0F, SoundEffects.SNOW_BREAK, SoundEffects.SNOW_STEP, SoundEffects.SNOW_PLACE, SoundEffects.SNOW_HIT, SoundEffects.SNOW_FALL);
    public static final SoundEffectType POWDER_SNOW = new SoundEffectType(1.0F, 1.0F, SoundEffects.POWDER_SNOW_BREAK, SoundEffects.POWDER_SNOW_STEP, SoundEffects.POWDER_SNOW_PLACE, SoundEffects.POWDER_SNOW_HIT, SoundEffects.POWDER_SNOW_FALL);
    public static final SoundEffectType LADDER = new SoundEffectType(1.0F, 1.0F, SoundEffects.LADDER_BREAK, SoundEffects.LADDER_STEP, SoundEffects.LADDER_PLACE, SoundEffects.LADDER_HIT, SoundEffects.LADDER_FALL);
    public static final SoundEffectType ANVIL = new SoundEffectType(0.3F, 1.0F, SoundEffects.ANVIL_BREAK, SoundEffects.ANVIL_STEP, SoundEffects.ANVIL_PLACE, SoundEffects.ANVIL_HIT, SoundEffects.ANVIL_FALL);
    public static final SoundEffectType SLIME_BLOCK = new SoundEffectType(1.0F, 1.0F, SoundEffects.SLIME_BLOCK_BREAK, SoundEffects.SLIME_BLOCK_STEP, SoundEffects.SLIME_BLOCK_PLACE, SoundEffects.SLIME_BLOCK_HIT, SoundEffects.SLIME_BLOCK_FALL);
    public static final SoundEffectType HONEY_BLOCK = new SoundEffectType(1.0F, 1.0F, SoundEffects.HONEY_BLOCK_BREAK, SoundEffects.HONEY_BLOCK_STEP, SoundEffects.HONEY_BLOCK_PLACE, SoundEffects.HONEY_BLOCK_HIT, SoundEffects.HONEY_BLOCK_FALL);
    public static final SoundEffectType WET_GRASS = new SoundEffectType(1.0F, 1.0F, SoundEffects.WET_GRASS_BREAK, SoundEffects.WET_GRASS_STEP, SoundEffects.WET_GRASS_PLACE, SoundEffects.WET_GRASS_HIT, SoundEffects.WET_GRASS_FALL);
    public static final SoundEffectType CORAL_BLOCK = new SoundEffectType(1.0F, 1.0F, SoundEffects.CORAL_BLOCK_BREAK, SoundEffects.CORAL_BLOCK_STEP, SoundEffects.CORAL_BLOCK_PLACE, SoundEffects.CORAL_BLOCK_HIT, SoundEffects.CORAL_BLOCK_FALL);
    public static final SoundEffectType BAMBOO = new SoundEffectType(1.0F, 1.0F, SoundEffects.BAMBOO_BREAK, SoundEffects.BAMBOO_STEP, SoundEffects.BAMBOO_PLACE, SoundEffects.BAMBOO_HIT, SoundEffects.BAMBOO_FALL);
    public static final SoundEffectType BAMBOO_SAPLING = new SoundEffectType(1.0F, 1.0F, SoundEffects.BAMBOO_SAPLING_BREAK, SoundEffects.BAMBOO_STEP, SoundEffects.BAMBOO_SAPLING_PLACE, SoundEffects.BAMBOO_SAPLING_HIT, SoundEffects.BAMBOO_FALL);
    public static final SoundEffectType SCAFFOLDING = new SoundEffectType(1.0F, 1.0F, SoundEffects.SCAFFOLDING_BREAK, SoundEffects.SCAFFOLDING_STEP, SoundEffects.SCAFFOLDING_PLACE, SoundEffects.SCAFFOLDING_HIT, SoundEffects.SCAFFOLDING_FALL);
    public static final SoundEffectType SWEET_BERRY_BUSH = new SoundEffectType(1.0F, 1.0F, SoundEffects.SWEET_BERRY_BUSH_BREAK, SoundEffects.GRASS_STEP, SoundEffects.SWEET_BERRY_BUSH_PLACE, SoundEffects.GRASS_HIT, SoundEffects.GRASS_FALL);
    public static final SoundEffectType CROP = new SoundEffectType(1.0F, 1.0F, SoundEffects.CROP_BREAK, SoundEffects.GRASS_STEP, SoundEffects.CROP_PLANTED, SoundEffects.GRASS_HIT, SoundEffects.GRASS_FALL);
    public static final SoundEffectType HARD_CROP = new SoundEffectType(1.0F, 1.0F, SoundEffects.WOOD_BREAK, SoundEffects.WOOD_STEP, SoundEffects.CROP_PLANTED, SoundEffects.WOOD_HIT, SoundEffects.WOOD_FALL);
    public static final SoundEffectType VINE = new SoundEffectType(1.0F, 1.0F, SoundEffects.VINE_BREAK, SoundEffects.VINE_STEP, SoundEffects.VINE_PLACE, SoundEffects.VINE_HIT, SoundEffects.VINE_FALL);
    public static final SoundEffectType NETHER_WART = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHER_WART_BREAK, SoundEffects.STONE_STEP, SoundEffects.NETHER_WART_PLANTED, SoundEffects.STONE_HIT, SoundEffects.STONE_FALL);
    public static final SoundEffectType LANTERN = new SoundEffectType(1.0F, 1.0F, SoundEffects.LANTERN_BREAK, SoundEffects.LANTERN_STEP, SoundEffects.LANTERN_PLACE, SoundEffects.LANTERN_HIT, SoundEffects.LANTERN_FALL);
    public static final SoundEffectType STEM = new SoundEffectType(1.0F, 1.0F, SoundEffects.STEM_BREAK, SoundEffects.STEM_STEP, SoundEffects.STEM_PLACE, SoundEffects.STEM_HIT, SoundEffects.STEM_FALL);
    public static final SoundEffectType NYLIUM = new SoundEffectType(1.0F, 1.0F, SoundEffects.NYLIUM_BREAK, SoundEffects.NYLIUM_STEP, SoundEffects.NYLIUM_PLACE, SoundEffects.NYLIUM_HIT, SoundEffects.NYLIUM_FALL);
    public static final SoundEffectType FUNGUS = new SoundEffectType(1.0F, 1.0F, SoundEffects.FUNGUS_BREAK, SoundEffects.FUNGUS_STEP, SoundEffects.FUNGUS_PLACE, SoundEffects.FUNGUS_HIT, SoundEffects.FUNGUS_FALL);
    public static final SoundEffectType ROOTS = new SoundEffectType(1.0F, 1.0F, SoundEffects.ROOTS_BREAK, SoundEffects.ROOTS_STEP, SoundEffects.ROOTS_PLACE, SoundEffects.ROOTS_HIT, SoundEffects.ROOTS_FALL);
    public static final SoundEffectType SHROOMLIGHT = new SoundEffectType(1.0F, 1.0F, SoundEffects.SHROOMLIGHT_BREAK, SoundEffects.SHROOMLIGHT_STEP, SoundEffects.SHROOMLIGHT_PLACE, SoundEffects.SHROOMLIGHT_HIT, SoundEffects.SHROOMLIGHT_FALL);
    public static final SoundEffectType WEEPING_VINES = new SoundEffectType(1.0F, 1.0F, SoundEffects.WEEPING_VINES_BREAK, SoundEffects.WEEPING_VINES_STEP, SoundEffects.WEEPING_VINES_PLACE, SoundEffects.WEEPING_VINES_HIT, SoundEffects.WEEPING_VINES_FALL);
    public static final SoundEffectType TWISTING_VINES = new SoundEffectType(1.0F, 0.5F, SoundEffects.WEEPING_VINES_BREAK, SoundEffects.WEEPING_VINES_STEP, SoundEffects.WEEPING_VINES_PLACE, SoundEffects.WEEPING_VINES_HIT, SoundEffects.WEEPING_VINES_FALL);
    public static final SoundEffectType SOUL_SAND = new SoundEffectType(1.0F, 1.0F, SoundEffects.SOUL_SAND_BREAK, SoundEffects.SOUL_SAND_STEP, SoundEffects.SOUL_SAND_PLACE, SoundEffects.SOUL_SAND_HIT, SoundEffects.SOUL_SAND_FALL);
    public static final SoundEffectType SOUL_SOIL = new SoundEffectType(1.0F, 1.0F, SoundEffects.SOUL_SOIL_BREAK, SoundEffects.SOUL_SOIL_STEP, SoundEffects.SOUL_SOIL_PLACE, SoundEffects.SOUL_SOIL_HIT, SoundEffects.SOUL_SOIL_FALL);
    public static final SoundEffectType BASALT = new SoundEffectType(1.0F, 1.0F, SoundEffects.BASALT_BREAK, SoundEffects.BASALT_STEP, SoundEffects.BASALT_PLACE, SoundEffects.BASALT_HIT, SoundEffects.BASALT_FALL);
    public static final SoundEffectType WART_BLOCK = new SoundEffectType(1.0F, 1.0F, SoundEffects.WART_BLOCK_BREAK, SoundEffects.WART_BLOCK_STEP, SoundEffects.WART_BLOCK_PLACE, SoundEffects.WART_BLOCK_HIT, SoundEffects.WART_BLOCK_FALL);
    public static final SoundEffectType NETHERRACK = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHERRACK_BREAK, SoundEffects.NETHERRACK_STEP, SoundEffects.NETHERRACK_PLACE, SoundEffects.NETHERRACK_HIT, SoundEffects.NETHERRACK_FALL);
    public static final SoundEffectType NETHER_BRICKS = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHER_BRICKS_BREAK, SoundEffects.NETHER_BRICKS_STEP, SoundEffects.NETHER_BRICKS_PLACE, SoundEffects.NETHER_BRICKS_HIT, SoundEffects.NETHER_BRICKS_FALL);
    public static final SoundEffectType NETHER_SPROUTS = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHER_SPROUTS_BREAK, SoundEffects.NETHER_SPROUTS_STEP, SoundEffects.NETHER_SPROUTS_PLACE, SoundEffects.NETHER_SPROUTS_HIT, SoundEffects.NETHER_SPROUTS_FALL);
    public static final SoundEffectType NETHER_ORE = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHER_ORE_BREAK, SoundEffects.NETHER_ORE_STEP, SoundEffects.NETHER_ORE_PLACE, SoundEffects.NETHER_ORE_HIT, SoundEffects.NETHER_ORE_FALL);
    public static final SoundEffectType BONE_BLOCK = new SoundEffectType(1.0F, 1.0F, SoundEffects.BONE_BLOCK_BREAK, SoundEffects.BONE_BLOCK_STEP, SoundEffects.BONE_BLOCK_PLACE, SoundEffects.BONE_BLOCK_HIT, SoundEffects.BONE_BLOCK_FALL);
    public static final SoundEffectType NETHERITE_BLOCK = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHERITE_BLOCK_BREAK, SoundEffects.NETHERITE_BLOCK_STEP, SoundEffects.NETHERITE_BLOCK_PLACE, SoundEffects.NETHERITE_BLOCK_HIT, SoundEffects.NETHERITE_BLOCK_FALL);
    public static final SoundEffectType ANCIENT_DEBRIS = new SoundEffectType(1.0F, 1.0F, SoundEffects.ANCIENT_DEBRIS_BREAK, SoundEffects.ANCIENT_DEBRIS_STEP, SoundEffects.ANCIENT_DEBRIS_PLACE, SoundEffects.ANCIENT_DEBRIS_HIT, SoundEffects.ANCIENT_DEBRIS_FALL);
    public static final SoundEffectType LODESTONE = new SoundEffectType(1.0F, 1.0F, SoundEffects.LODESTONE_BREAK, SoundEffects.LODESTONE_STEP, SoundEffects.LODESTONE_PLACE, SoundEffects.LODESTONE_HIT, SoundEffects.LODESTONE_FALL);
    public static final SoundEffectType CHAIN = new SoundEffectType(1.0F, 1.0F, SoundEffects.CHAIN_BREAK, SoundEffects.CHAIN_STEP, SoundEffects.CHAIN_PLACE, SoundEffects.CHAIN_HIT, SoundEffects.CHAIN_FALL);
    public static final SoundEffectType NETHER_GOLD_ORE = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHER_GOLD_ORE_BREAK, SoundEffects.NETHER_GOLD_ORE_STEP, SoundEffects.NETHER_GOLD_ORE_PLACE, SoundEffects.NETHER_GOLD_ORE_HIT, SoundEffects.NETHER_GOLD_ORE_FALL);
    public static final SoundEffectType GILDED_BLACKSTONE = new SoundEffectType(1.0F, 1.0F, SoundEffects.GILDED_BLACKSTONE_BREAK, SoundEffects.GILDED_BLACKSTONE_STEP, SoundEffects.GILDED_BLACKSTONE_PLACE, SoundEffects.GILDED_BLACKSTONE_HIT, SoundEffects.GILDED_BLACKSTONE_FALL);
    public static final SoundEffectType CANDLE = new SoundEffectType(1.0F, 1.0F, SoundEffects.CANDLE_BREAK, SoundEffects.CANDLE_STEP, SoundEffects.CANDLE_PLACE, SoundEffects.CANDLE_HIT, SoundEffects.CANDLE_FALL);
    public static final SoundEffectType AMETHYST = new SoundEffectType(1.0F, 1.0F, SoundEffects.AMETHYST_BLOCK_BREAK, SoundEffects.AMETHYST_BLOCK_STEP, SoundEffects.AMETHYST_BLOCK_PLACE, SoundEffects.AMETHYST_BLOCK_HIT, SoundEffects.AMETHYST_BLOCK_FALL);
    public static final SoundEffectType AMETHYST_CLUSTER = new SoundEffectType(1.0F, 1.0F, SoundEffects.AMETHYST_CLUSTER_BREAK, SoundEffects.AMETHYST_CLUSTER_STEP, SoundEffects.AMETHYST_CLUSTER_PLACE, SoundEffects.AMETHYST_CLUSTER_HIT, SoundEffects.AMETHYST_CLUSTER_FALL);
    public static final SoundEffectType SMALL_AMETHYST_BUD = new SoundEffectType(1.0F, 1.0F, SoundEffects.SMALL_AMETHYST_BUD_BREAK, SoundEffects.AMETHYST_CLUSTER_STEP, SoundEffects.SMALL_AMETHYST_BUD_PLACE, SoundEffects.AMETHYST_CLUSTER_HIT, SoundEffects.AMETHYST_CLUSTER_FALL);
    public static final SoundEffectType MEDIUM_AMETHYST_BUD = new SoundEffectType(1.0F, 1.0F, SoundEffects.MEDIUM_AMETHYST_BUD_BREAK, SoundEffects.AMETHYST_CLUSTER_STEP, SoundEffects.MEDIUM_AMETHYST_BUD_PLACE, SoundEffects.AMETHYST_CLUSTER_HIT, SoundEffects.AMETHYST_CLUSTER_FALL);
    public static final SoundEffectType LARGE_AMETHYST_BUD = new SoundEffectType(1.0F, 1.0F, SoundEffects.LARGE_AMETHYST_BUD_BREAK, SoundEffects.AMETHYST_CLUSTER_STEP, SoundEffects.LARGE_AMETHYST_BUD_PLACE, SoundEffects.AMETHYST_CLUSTER_HIT, SoundEffects.AMETHYST_CLUSTER_FALL);
    public static final SoundEffectType TUFF = new SoundEffectType(1.0F, 1.0F, SoundEffects.TUFF_BREAK, SoundEffects.TUFF_STEP, SoundEffects.TUFF_PLACE, SoundEffects.TUFF_HIT, SoundEffects.TUFF_FALL);
    public static final SoundEffectType TUFF_BRICKS = new SoundEffectType(1.0F, 1.0F, SoundEffects.TUFF_BRICKS_BREAK, SoundEffects.TUFF_BRICKS_STEP, SoundEffects.TUFF_BRICKS_PLACE, SoundEffects.TUFF_BRICKS_HIT, SoundEffects.TUFF_BRICKS_FALL);
    public static final SoundEffectType POLISHED_TUFF = new SoundEffectType(1.0F, 1.0F, SoundEffects.POLISHED_TUFF_BREAK, SoundEffects.POLISHED_TUFF_STEP, SoundEffects.POLISHED_TUFF_PLACE, SoundEffects.POLISHED_TUFF_HIT, SoundEffects.POLISHED_TUFF_FALL);
    public static final SoundEffectType CALCITE = new SoundEffectType(1.0F, 1.0F, SoundEffects.CALCITE_BREAK, SoundEffects.CALCITE_STEP, SoundEffects.CALCITE_PLACE, SoundEffects.CALCITE_HIT, SoundEffects.CALCITE_FALL);
    public static final SoundEffectType DRIPSTONE_BLOCK = new SoundEffectType(1.0F, 1.0F, SoundEffects.DRIPSTONE_BLOCK_BREAK, SoundEffects.DRIPSTONE_BLOCK_STEP, SoundEffects.DRIPSTONE_BLOCK_PLACE, SoundEffects.DRIPSTONE_BLOCK_HIT, SoundEffects.DRIPSTONE_BLOCK_FALL);
    public static final SoundEffectType POINTED_DRIPSTONE = new SoundEffectType(1.0F, 1.0F, SoundEffects.POINTED_DRIPSTONE_BREAK, SoundEffects.POINTED_DRIPSTONE_STEP, SoundEffects.POINTED_DRIPSTONE_PLACE, SoundEffects.POINTED_DRIPSTONE_HIT, SoundEffects.POINTED_DRIPSTONE_FALL);
    public static final SoundEffectType COPPER = new SoundEffectType(1.0F, 1.0F, SoundEffects.COPPER_BREAK, SoundEffects.COPPER_STEP, SoundEffects.COPPER_PLACE, SoundEffects.COPPER_HIT, SoundEffects.COPPER_FALL);
    public static final SoundEffectType COPPER_BULB = new SoundEffectType(1.0F, 1.0F, SoundEffects.COPPER_BULB_BREAK, SoundEffects.COPPER_BULB_STEP, SoundEffects.COPPER_BULB_PLACE, SoundEffects.COPPER_BULB_HIT, SoundEffects.COPPER_BULB_FALL);
    public static final SoundEffectType COPPER_GRATE = new SoundEffectType(1.0F, 1.0F, SoundEffects.COPPER_GRATE_BREAK, SoundEffects.COPPER_GRATE_STEP, SoundEffects.COPPER_GRATE_PLACE, SoundEffects.COPPER_GRATE_HIT, SoundEffects.COPPER_GRATE_FALL);
    public static final SoundEffectType CAVE_VINES = new SoundEffectType(1.0F, 1.0F, SoundEffects.CAVE_VINES_BREAK, SoundEffects.CAVE_VINES_STEP, SoundEffects.CAVE_VINES_PLACE, SoundEffects.CAVE_VINES_HIT, SoundEffects.CAVE_VINES_FALL);
    public static final SoundEffectType SPORE_BLOSSOM = new SoundEffectType(1.0F, 1.0F, SoundEffects.SPORE_BLOSSOM_BREAK, SoundEffects.SPORE_BLOSSOM_STEP, SoundEffects.SPORE_BLOSSOM_PLACE, SoundEffects.SPORE_BLOSSOM_HIT, SoundEffects.SPORE_BLOSSOM_FALL);
    public static final SoundEffectType CACTUS_FLOWER = new SoundEffectType(1.0F, 1.0F, SoundEffects.CACTUS_FLOWER_BREAK, SoundEffects.EMPTY, SoundEffects.CACTUS_FLOWER_PLACE, SoundEffects.EMPTY, SoundEffects.EMPTY);
    public static final SoundEffectType AZALEA = new SoundEffectType(1.0F, 1.0F, SoundEffects.AZALEA_BREAK, SoundEffects.AZALEA_STEP, SoundEffects.AZALEA_PLACE, SoundEffects.AZALEA_HIT, SoundEffects.AZALEA_FALL);
    public static final SoundEffectType FLOWERING_AZALEA = new SoundEffectType(1.0F, 1.0F, SoundEffects.FLOWERING_AZALEA_BREAK, SoundEffects.FLOWERING_AZALEA_STEP, SoundEffects.FLOWERING_AZALEA_PLACE, SoundEffects.FLOWERING_AZALEA_HIT, SoundEffects.FLOWERING_AZALEA_FALL);
    public static final SoundEffectType MOSS_CARPET = new SoundEffectType(1.0F, 1.0F, SoundEffects.MOSS_CARPET_BREAK, SoundEffects.MOSS_CARPET_STEP, SoundEffects.MOSS_CARPET_PLACE, SoundEffects.MOSS_CARPET_HIT, SoundEffects.MOSS_CARPET_FALL);
    public static final SoundEffectType PINK_PETALS = new SoundEffectType(1.0F, 1.0F, SoundEffects.PINK_PETALS_BREAK, SoundEffects.PINK_PETALS_STEP, SoundEffects.PINK_PETALS_PLACE, SoundEffects.PINK_PETALS_HIT, SoundEffects.PINK_PETALS_FALL);
    public static final SoundEffectType LEAF_LITTER = new SoundEffectType(1.0F, 1.0F, SoundEffects.LEAF_LITTER_BREAK, SoundEffects.LEAF_LITTER_STEP, SoundEffects.LEAF_LITTER_PLACE, SoundEffects.LEAF_LITTER_HIT, SoundEffects.LEAF_LITTER_FALL);
    public static final SoundEffectType MOSS = new SoundEffectType(1.0F, 1.0F, SoundEffects.MOSS_BREAK, SoundEffects.MOSS_STEP, SoundEffects.MOSS_PLACE, SoundEffects.MOSS_HIT, SoundEffects.MOSS_FALL);
    public static final SoundEffectType BIG_DRIPLEAF = new SoundEffectType(1.0F, 1.0F, SoundEffects.BIG_DRIPLEAF_BREAK, SoundEffects.BIG_DRIPLEAF_STEP, SoundEffects.BIG_DRIPLEAF_PLACE, SoundEffects.BIG_DRIPLEAF_HIT, SoundEffects.BIG_DRIPLEAF_FALL);
    public static final SoundEffectType SMALL_DRIPLEAF = new SoundEffectType(1.0F, 1.0F, SoundEffects.SMALL_DRIPLEAF_BREAK, SoundEffects.SMALL_DRIPLEAF_STEP, SoundEffects.SMALL_DRIPLEAF_PLACE, SoundEffects.SMALL_DRIPLEAF_HIT, SoundEffects.SMALL_DRIPLEAF_FALL);
    public static final SoundEffectType ROOTED_DIRT = new SoundEffectType(1.0F, 1.0F, SoundEffects.ROOTED_DIRT_BREAK, SoundEffects.ROOTED_DIRT_STEP, SoundEffects.ROOTED_DIRT_PLACE, SoundEffects.ROOTED_DIRT_HIT, SoundEffects.ROOTED_DIRT_FALL);
    public static final SoundEffectType HANGING_ROOTS = new SoundEffectType(1.0F, 1.0F, SoundEffects.HANGING_ROOTS_BREAK, SoundEffects.HANGING_ROOTS_STEP, SoundEffects.HANGING_ROOTS_PLACE, SoundEffects.HANGING_ROOTS_HIT, SoundEffects.HANGING_ROOTS_FALL);
    public static final SoundEffectType AZALEA_LEAVES = new SoundEffectType(1.0F, 1.0F, SoundEffects.AZALEA_LEAVES_BREAK, SoundEffects.AZALEA_LEAVES_STEP, SoundEffects.AZALEA_LEAVES_PLACE, SoundEffects.AZALEA_LEAVES_HIT, SoundEffects.AZALEA_LEAVES_FALL);
    public static final SoundEffectType SCULK_SENSOR = new SoundEffectType(1.0F, 1.0F, SoundEffects.SCULK_SENSOR_BREAK, SoundEffects.SCULK_SENSOR_STEP, SoundEffects.SCULK_SENSOR_PLACE, SoundEffects.SCULK_SENSOR_HIT, SoundEffects.SCULK_SENSOR_FALL);
    public static final SoundEffectType SCULK_CATALYST = new SoundEffectType(1.0F, 1.0F, SoundEffects.SCULK_CATALYST_BREAK, SoundEffects.SCULK_CATALYST_STEP, SoundEffects.SCULK_CATALYST_PLACE, SoundEffects.SCULK_CATALYST_HIT, SoundEffects.SCULK_CATALYST_FALL);
    public static final SoundEffectType SCULK = new SoundEffectType(1.0F, 1.0F, SoundEffects.SCULK_BLOCK_BREAK, SoundEffects.SCULK_BLOCK_STEP, SoundEffects.SCULK_BLOCK_PLACE, SoundEffects.SCULK_BLOCK_HIT, SoundEffects.SCULK_BLOCK_FALL);
    public static final SoundEffectType SCULK_VEIN = new SoundEffectType(1.0F, 1.0F, SoundEffects.SCULK_VEIN_BREAK, SoundEffects.SCULK_VEIN_STEP, SoundEffects.SCULK_VEIN_PLACE, SoundEffects.SCULK_VEIN_HIT, SoundEffects.SCULK_VEIN_FALL);
    public static final SoundEffectType SCULK_SHRIEKER = new SoundEffectType(1.0F, 1.0F, SoundEffects.SCULK_SHRIEKER_BREAK, SoundEffects.SCULK_SHRIEKER_STEP, SoundEffects.SCULK_SHRIEKER_PLACE, SoundEffects.SCULK_SHRIEKER_HIT, SoundEffects.SCULK_SHRIEKER_FALL);
    public static final SoundEffectType GLOW_LICHEN = new SoundEffectType(1.0F, 1.0F, SoundEffects.GRASS_BREAK, SoundEffects.VINE_STEP, SoundEffects.GRASS_PLACE, SoundEffects.GRASS_HIT, SoundEffects.GRASS_FALL);
    public static final SoundEffectType DEEPSLATE = new SoundEffectType(1.0F, 1.0F, SoundEffects.DEEPSLATE_BREAK, SoundEffects.DEEPSLATE_STEP, SoundEffects.DEEPSLATE_PLACE, SoundEffects.DEEPSLATE_HIT, SoundEffects.DEEPSLATE_FALL);
    public static final SoundEffectType DEEPSLATE_BRICKS = new SoundEffectType(1.0F, 1.0F, SoundEffects.DEEPSLATE_BRICKS_BREAK, SoundEffects.DEEPSLATE_BRICKS_STEP, SoundEffects.DEEPSLATE_BRICKS_PLACE, SoundEffects.DEEPSLATE_BRICKS_HIT, SoundEffects.DEEPSLATE_BRICKS_FALL);
    public static final SoundEffectType DEEPSLATE_TILES = new SoundEffectType(1.0F, 1.0F, SoundEffects.DEEPSLATE_TILES_BREAK, SoundEffects.DEEPSLATE_TILES_STEP, SoundEffects.DEEPSLATE_TILES_PLACE, SoundEffects.DEEPSLATE_TILES_HIT, SoundEffects.DEEPSLATE_TILES_FALL);
    public static final SoundEffectType POLISHED_DEEPSLATE = new SoundEffectType(1.0F, 1.0F, SoundEffects.POLISHED_DEEPSLATE_BREAK, SoundEffects.POLISHED_DEEPSLATE_STEP, SoundEffects.POLISHED_DEEPSLATE_PLACE, SoundEffects.POLISHED_DEEPSLATE_HIT, SoundEffects.POLISHED_DEEPSLATE_FALL);
    public static final SoundEffectType FROGLIGHT = new SoundEffectType(1.0F, 1.0F, SoundEffects.FROGLIGHT_BREAK, SoundEffects.FROGLIGHT_STEP, SoundEffects.FROGLIGHT_PLACE, SoundEffects.FROGLIGHT_HIT, SoundEffects.FROGLIGHT_FALL);
    public static final SoundEffectType FROGSPAWN = new SoundEffectType(1.0F, 1.0F, SoundEffects.FROGSPAWN_BREAK, SoundEffects.FROGSPAWNSTEP, SoundEffects.FROGSPAWN_PLACE, SoundEffects.FROGSPAWN_HIT, SoundEffects.FROGSPAWN_FALL);
    public static final SoundEffectType MANGROVE_ROOTS = new SoundEffectType(1.0F, 1.0F, SoundEffects.MANGROVE_ROOTS_BREAK, SoundEffects.MANGROVE_ROOTS_STEP, SoundEffects.MANGROVE_ROOTS_PLACE, SoundEffects.MANGROVE_ROOTS_HIT, SoundEffects.MANGROVE_ROOTS_FALL);
    public static final SoundEffectType MUDDY_MANGROVE_ROOTS = new SoundEffectType(1.0F, 1.0F, SoundEffects.MUDDY_MANGROVE_ROOTS_BREAK, SoundEffects.MUDDY_MANGROVE_ROOTS_STEP, SoundEffects.MUDDY_MANGROVE_ROOTS_PLACE, SoundEffects.MUDDY_MANGROVE_ROOTS_HIT, SoundEffects.MUDDY_MANGROVE_ROOTS_FALL);
    public static final SoundEffectType MUD = new SoundEffectType(1.0F, 1.0F, SoundEffects.MUD_BREAK, SoundEffects.MUD_STEP, SoundEffects.MUD_PLACE, SoundEffects.MUD_HIT, SoundEffects.MUD_FALL);
    public static final SoundEffectType MUD_BRICKS = new SoundEffectType(1.0F, 1.0F, SoundEffects.MUD_BRICKS_BREAK, SoundEffects.MUD_BRICKS_STEP, SoundEffects.MUD_BRICKS_PLACE, SoundEffects.MUD_BRICKS_HIT, SoundEffects.MUD_BRICKS_FALL);
    public static final SoundEffectType PACKED_MUD = new SoundEffectType(1.0F, 1.0F, SoundEffects.PACKED_MUD_BREAK, SoundEffects.PACKED_MUD_STEP, SoundEffects.PACKED_MUD_PLACE, SoundEffects.PACKED_MUD_HIT, SoundEffects.PACKED_MUD_FALL);
    public static final SoundEffectType HANGING_SIGN = new SoundEffectType(1.0F, 1.0F, SoundEffects.HANGING_SIGN_BREAK, SoundEffects.HANGING_SIGN_STEP, SoundEffects.HANGING_SIGN_PLACE, SoundEffects.HANGING_SIGN_HIT, SoundEffects.HANGING_SIGN_FALL);
    public static final SoundEffectType NETHER_WOOD_HANGING_SIGN = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHER_WOOD_HANGING_SIGN_BREAK, SoundEffects.NETHER_WOOD_HANGING_SIGN_STEP, SoundEffects.NETHER_WOOD_HANGING_SIGN_PLACE, SoundEffects.NETHER_WOOD_HANGING_SIGN_HIT, SoundEffects.NETHER_WOOD_HANGING_SIGN_FALL);
    public static final SoundEffectType BAMBOO_WOOD_HANGING_SIGN = new SoundEffectType(1.0F, 1.0F, SoundEffects.BAMBOO_WOOD_HANGING_SIGN_BREAK, SoundEffects.BAMBOO_WOOD_HANGING_SIGN_STEP, SoundEffects.BAMBOO_WOOD_HANGING_SIGN_PLACE, SoundEffects.BAMBOO_WOOD_HANGING_SIGN_HIT, SoundEffects.BAMBOO_WOOD_HANGING_SIGN_FALL);
    public static final SoundEffectType BAMBOO_WOOD = new SoundEffectType(1.0F, 1.0F, SoundEffects.BAMBOO_WOOD_BREAK, SoundEffects.BAMBOO_WOOD_STEP, SoundEffects.BAMBOO_WOOD_PLACE, SoundEffects.BAMBOO_WOOD_HIT, SoundEffects.BAMBOO_WOOD_FALL);
    public static final SoundEffectType NETHER_WOOD = new SoundEffectType(1.0F, 1.0F, SoundEffects.NETHER_WOOD_BREAK, SoundEffects.NETHER_WOOD_STEP, SoundEffects.NETHER_WOOD_PLACE, SoundEffects.NETHER_WOOD_HIT, SoundEffects.NETHER_WOOD_FALL);
    public static final SoundEffectType CHERRY_WOOD = new SoundEffectType(1.0F, 1.0F, SoundEffects.CHERRY_WOOD_BREAK, SoundEffects.CHERRY_WOOD_STEP, SoundEffects.CHERRY_WOOD_PLACE, SoundEffects.CHERRY_WOOD_HIT, SoundEffects.CHERRY_WOOD_FALL);
    public static final SoundEffectType CHERRY_SAPLING = new SoundEffectType(1.0F, 1.0F, SoundEffects.CHERRY_SAPLING_BREAK, SoundEffects.CHERRY_SAPLING_STEP, SoundEffects.CHERRY_SAPLING_PLACE, SoundEffects.CHERRY_SAPLING_HIT, SoundEffects.CHERRY_SAPLING_FALL);
    public static final SoundEffectType CHERRY_LEAVES = new SoundEffectType(1.0F, 1.0F, SoundEffects.CHERRY_LEAVES_BREAK, SoundEffects.CHERRY_LEAVES_STEP, SoundEffects.CHERRY_LEAVES_PLACE, SoundEffects.CHERRY_LEAVES_HIT, SoundEffects.CHERRY_LEAVES_FALL);
    public static final SoundEffectType CHERRY_WOOD_HANGING_SIGN = new SoundEffectType(1.0F, 1.0F, SoundEffects.CHERRY_WOOD_HANGING_SIGN_BREAK, SoundEffects.CHERRY_WOOD_HANGING_SIGN_STEP, SoundEffects.CHERRY_WOOD_HANGING_SIGN_PLACE, SoundEffects.CHERRY_WOOD_HANGING_SIGN_HIT, SoundEffects.CHERRY_WOOD_HANGING_SIGN_FALL);
    public static final SoundEffectType CHISELED_BOOKSHELF = new SoundEffectType(1.0F, 1.0F, SoundEffects.CHISELED_BOOKSHELF_BREAK, SoundEffects.CHISELED_BOOKSHELF_STEP, SoundEffects.CHISELED_BOOKSHELF_PLACE, SoundEffects.CHISELED_BOOKSHELF_HIT, SoundEffects.CHISELED_BOOKSHELF_FALL);
    public static final SoundEffectType SUSPICIOUS_SAND = new SoundEffectType(1.0F, 1.0F, SoundEffects.SUSPICIOUS_SAND_BREAK, SoundEffects.SUSPICIOUS_SAND_STEP, SoundEffects.SUSPICIOUS_SAND_PLACE, SoundEffects.SUSPICIOUS_SAND_HIT, SoundEffects.SUSPICIOUS_SAND_FALL);
    public static final SoundEffectType SUSPICIOUS_GRAVEL = new SoundEffectType(1.0F, 1.0F, SoundEffects.SUSPICIOUS_GRAVEL_BREAK, SoundEffects.SUSPICIOUS_GRAVEL_STEP, SoundEffects.SUSPICIOUS_GRAVEL_PLACE, SoundEffects.SUSPICIOUS_GRAVEL_HIT, SoundEffects.SUSPICIOUS_GRAVEL_FALL);
    public static final SoundEffectType DECORATED_POT = new SoundEffectType(1.0F, 1.0F, SoundEffects.DECORATED_POT_BREAK, SoundEffects.DECORATED_POT_STEP, SoundEffects.DECORATED_POT_PLACE, SoundEffects.DECORATED_POT_HIT, SoundEffects.DECORATED_POT_FALL);
    public static final SoundEffectType DECORATED_POT_CRACKED = new SoundEffectType(1.0F, 1.0F, SoundEffects.DECORATED_POT_SHATTER, SoundEffects.DECORATED_POT_STEP, SoundEffects.DECORATED_POT_PLACE, SoundEffects.DECORATED_POT_HIT, SoundEffects.DECORATED_POT_FALL);
    public static final SoundEffectType TRIAL_SPAWNER = new SoundEffectType(1.0F, 1.0F, SoundEffects.TRIAL_SPAWNER_BREAK, SoundEffects.TRIAL_SPAWNER_STEP, SoundEffects.TRIAL_SPAWNER_PLACE, SoundEffects.TRIAL_SPAWNER_HIT, SoundEffects.TRIAL_SPAWNER_FALL);
    public static final SoundEffectType SPONGE = new SoundEffectType(1.0F, 1.0F, SoundEffects.SPONGE_BREAK, SoundEffects.SPONGE_STEP, SoundEffects.SPONGE_PLACE, SoundEffects.SPONGE_HIT, SoundEffects.SPONGE_FALL);
    public static final SoundEffectType WET_SPONGE = new SoundEffectType(1.0F, 1.0F, SoundEffects.WET_SPONGE_BREAK, SoundEffects.WET_SPONGE_STEP, SoundEffects.WET_SPONGE_PLACE, SoundEffects.WET_SPONGE_HIT, SoundEffects.WET_SPONGE_FALL);
    public static final SoundEffectType VAULT = new SoundEffectType(1.0F, 1.0F, SoundEffects.VAULT_BREAK, SoundEffects.VAULT_STEP, SoundEffects.VAULT_PLACE, SoundEffects.VAULT_HIT, SoundEffects.VAULT_FALL);
    public static final SoundEffectType CREAKING_HEART = new SoundEffectType(1.0F, 1.0F, SoundEffects.CREAKING_HEART_BREAK, SoundEffects.CREAKING_HEART_STEP, SoundEffects.CREAKING_HEART_PLACE, SoundEffects.CREAKING_HEART_HIT, SoundEffects.CREAKING_HEART_FALL);
    public static final SoundEffectType HEAVY_CORE = new SoundEffectType(1.0F, 1.0F, SoundEffects.HEAVY_CORE_BREAK, SoundEffects.HEAVY_CORE_STEP, SoundEffects.HEAVY_CORE_PLACE, SoundEffects.HEAVY_CORE_HIT, SoundEffects.HEAVY_CORE_FALL);
    public static final SoundEffectType COBWEB = new SoundEffectType(1.0F, 1.0F, SoundEffects.COBWEB_BREAK, SoundEffects.COBWEB_STEP, SoundEffects.COBWEB_PLACE, SoundEffects.COBWEB_HIT, SoundEffects.COBWEB_FALL);
    public static final SoundEffectType SPAWNER = new SoundEffectType(1.0F, 1.0F, SoundEffects.SPAWNER_BREAK, SoundEffects.SPAWNER_STEP, SoundEffects.SPAWNER_PLACE, SoundEffects.SPAWNER_HIT, SoundEffects.SPAWNER_FALL);
    public static final SoundEffectType RESIN = new SoundEffectType(1.0F, 1.0F, SoundEffects.RESIN_BREAK, SoundEffects.RESIN_STEP, SoundEffects.RESIN_PLACE, SoundEffects.EMPTY, SoundEffects.RESIN_FALL);
    public static final SoundEffectType RESIN_BRICKS = new SoundEffectType(1.0F, 1.0F, SoundEffects.RESIN_BRICKS_BREAK, SoundEffects.RESIN_BRICKS_STEP, SoundEffects.RESIN_BRICKS_PLACE, SoundEffects.RESIN_BRICKS_HIT, SoundEffects.RESIN_BRICKS_FALL);
    public static final SoundEffectType IRON = new SoundEffectType(1.0F, 1.0F, SoundEffects.IRON_BREAK, SoundEffects.IRON_STEP, SoundEffects.IRON_PLACE, SoundEffects.IRON_HIT, SoundEffects.IRON_FALL);
    public final float volume;
    public final float pitch;
    public final SoundEffect breakSound;
    private final SoundEffect stepSound;
    private final SoundEffect placeSound;
    public final SoundEffect hitSound;
    private final SoundEffect fallSound;

    public SoundEffectType(float f, float f1, SoundEffect soundeffect, SoundEffect soundeffect1, SoundEffect soundeffect2, SoundEffect soundeffect3, SoundEffect soundeffect4) {
        this.volume = f;
        this.pitch = f1;
        this.breakSound = soundeffect;
        this.stepSound = soundeffect1;
        this.placeSound = soundeffect2;
        this.hitSound = soundeffect3;
        this.fallSound = soundeffect4;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public SoundEffect getBreakSound() {
        return this.breakSound;
    }

    public SoundEffect getStepSound() {
        return this.stepSound;
    }

    public SoundEffect getPlaceSound() {
        return this.placeSound;
    }

    public SoundEffect getHitSound() {
        return this.hitSound;
    }

    public SoundEffect getFallSound() {
        return this.fallSound;
    }
}
