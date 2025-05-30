package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class DataConverterSchemaV705 extends DataConverterSchemaNamed {

    static final Map<String, String> ITEM_TO_ENTITY = ImmutableMap.builder().put("minecraft:armor_stand", "minecraft:armor_stand").put("minecraft:painting", "minecraft:painting").put("minecraft:armadillo_spawn_egg", "minecraft:armadillo").put("minecraft:allay_spawn_egg", "minecraft:allay").put("minecraft:axolotl_spawn_egg", "minecraft:axolotl").put("minecraft:bat_spawn_egg", "minecraft:bat").put("minecraft:bee_spawn_egg", "minecraft:bee").put("minecraft:blaze_spawn_egg", "minecraft:blaze").put("minecraft:bogged_spawn_egg", "minecraft:bogged").put("minecraft:breeze_spawn_egg", "minecraft:breeze").put("minecraft:cat_spawn_egg", "minecraft:cat").put("minecraft:camel_spawn_egg", "minecraft:camel").put("minecraft:cave_spider_spawn_egg", "minecraft:cave_spider").put("minecraft:chicken_spawn_egg", "minecraft:chicken").put("minecraft:cod_spawn_egg", "minecraft:cod").put("minecraft:cow_spawn_egg", "minecraft:cow").put("minecraft:creeper_spawn_egg", "minecraft:creeper").put("minecraft:dolphin_spawn_egg", "minecraft:dolphin").put("minecraft:donkey_spawn_egg", "minecraft:donkey").put("minecraft:drowned_spawn_egg", "minecraft:drowned").put("minecraft:elder_guardian_spawn_egg", "minecraft:elder_guardian").put("minecraft:ender_dragon_spawn_egg", "minecraft:ender_dragon").put("minecraft:enderman_spawn_egg", "minecraft:enderman").put("minecraft:endermite_spawn_egg", "minecraft:endermite").put("minecraft:evoker_spawn_egg", "minecraft:evoker").put("minecraft:fox_spawn_egg", "minecraft:fox").put("minecraft:frog_spawn_egg", "minecraft:frog").put("minecraft:ghast_spawn_egg", "minecraft:ghast").put("minecraft:glow_squid_spawn_egg", "minecraft:glow_squid").put("minecraft:goat_spawn_egg", "minecraft:goat").put("minecraft:guardian_spawn_egg", "minecraft:guardian").put("minecraft:hoglin_spawn_egg", "minecraft:hoglin").put("minecraft:horse_spawn_egg", "minecraft:horse").put("minecraft:husk_spawn_egg", "minecraft:husk").put("minecraft:iron_golem_spawn_egg", "minecraft:iron_golem").put("minecraft:llama_spawn_egg", "minecraft:llama").put("minecraft:magma_cube_spawn_egg", "minecraft:magma_cube").put("minecraft:mooshroom_spawn_egg", "minecraft:mooshroom").put("minecraft:mule_spawn_egg", "minecraft:mule").put("minecraft:ocelot_spawn_egg", "minecraft:ocelot").put("minecraft:panda_spawn_egg", "minecraft:panda").put("minecraft:parrot_spawn_egg", "minecraft:parrot").put("minecraft:phantom_spawn_egg", "minecraft:phantom").put("minecraft:pig_spawn_egg", "minecraft:pig").put("minecraft:piglin_spawn_egg", "minecraft:piglin").put("minecraft:piglin_brute_spawn_egg", "minecraft:piglin_brute").put("minecraft:pillager_spawn_egg", "minecraft:pillager").put("minecraft:polar_bear_spawn_egg", "minecraft:polar_bear").put("minecraft:pufferfish_spawn_egg", "minecraft:pufferfish").put("minecraft:rabbit_spawn_egg", "minecraft:rabbit").put("minecraft:ravager_spawn_egg", "minecraft:ravager").put("minecraft:salmon_spawn_egg", "minecraft:salmon").put("minecraft:sheep_spawn_egg", "minecraft:sheep").put("minecraft:shulker_spawn_egg", "minecraft:shulker").put("minecraft:silverfish_spawn_egg", "minecraft:silverfish").put("minecraft:skeleton_spawn_egg", "minecraft:skeleton").put("minecraft:skeleton_horse_spawn_egg", "minecraft:skeleton_horse").put("minecraft:slime_spawn_egg", "minecraft:slime").put("minecraft:sniffer_spawn_egg", "minecraft:sniffer").put("minecraft:snow_golem_spawn_egg", "minecraft:snow_golem").put("minecraft:spider_spawn_egg", "minecraft:spider").put("minecraft:squid_spawn_egg", "minecraft:squid").put("minecraft:stray_spawn_egg", "minecraft:stray").put("minecraft:strider_spawn_egg", "minecraft:strider").put("minecraft:tadpole_spawn_egg", "minecraft:tadpole").put("minecraft:trader_llama_spawn_egg", "minecraft:trader_llama").put("minecraft:tropical_fish_spawn_egg", "minecraft:tropical_fish").put("minecraft:turtle_spawn_egg", "minecraft:turtle").put("minecraft:vex_spawn_egg", "minecraft:vex").put("minecraft:villager_spawn_egg", "minecraft:villager").put("minecraft:vindicator_spawn_egg", "minecraft:vindicator").put("minecraft:wandering_trader_spawn_egg", "minecraft:wandering_trader").put("minecraft:warden_spawn_egg", "minecraft:warden").put("minecraft:witch_spawn_egg", "minecraft:witch").put("minecraft:wither_spawn_egg", "minecraft:wither").put("minecraft:wither_skeleton_spawn_egg", "minecraft:wither_skeleton").put("minecraft:wolf_spawn_egg", "minecraft:wolf").put("minecraft:zoglin_spawn_egg", "minecraft:zoglin").put("minecraft:zombie_spawn_egg", "minecraft:zombie").put("minecraft:zombie_horse_spawn_egg", "minecraft:zombie_horse").put("minecraft:zombie_villager_spawn_egg", "minecraft:zombie_villager").put("minecraft:zombified_piglin_spawn_egg", "minecraft:zombified_piglin").put("minecraft:item_frame", "minecraft:item_frame").put("minecraft:boat", "minecraft:oak_boat").put("minecraft:oak_boat", "minecraft:oak_boat").put("minecraft:oak_chest_boat", "minecraft:oak_chest_boat").put("minecraft:spruce_boat", "minecraft:spruce_boat").put("minecraft:spruce_chest_boat", "minecraft:spruce_chest_boat").put("minecraft:birch_boat", "minecraft:birch_boat").put("minecraft:birch_chest_boat", "minecraft:birch_chest_boat").put("minecraft:jungle_boat", "minecraft:jungle_boat").put("minecraft:jungle_chest_boat", "minecraft:jungle_chest_boat").put("minecraft:acacia_boat", "minecraft:acacia_boat").put("minecraft:acacia_chest_boat", "minecraft:acacia_chest_boat").put("minecraft:cherry_boat", "minecraft:cherry_boat").put("minecraft:cherry_chest_boat", "minecraft:cherry_chest_boat").put("minecraft:dark_oak_boat", "minecraft:dark_oak_boat").put("minecraft:dark_oak_chest_boat", "minecraft:dark_oak_chest_boat").put("minecraft:mangrove_boat", "minecraft:mangrove_boat").put("minecraft:mangrove_chest_boat", "minecraft:mangrove_chest_boat").put("minecraft:bamboo_raft", "minecraft:bamboo_raft").put("minecraft:bamboo_chest_raft", "minecraft:bamboo_chest_raft").put("minecraft:minecart", "minecraft:minecart").put("minecraft:chest_minecart", "minecraft:chest_minecart").put("minecraft:furnace_minecart", "minecraft:furnace_minecart").put("minecraft:tnt_minecart", "minecraft:tnt_minecart").put("minecraft:hopper_minecart", "minecraft:hopper_minecart").build();
    protected static final HookFunction ADD_NAMES = new HookFunction() {
        public <T> T apply(DynamicOps<T> dynamicops, T t0) {
            return (T) DataConverterSchemaV99.addNames(new Dynamic(dynamicops, t0), DataConverterSchemaV704.ITEM_TO_BLOCKENTITY, DataConverterSchemaV705.ITEM_TO_ENTITY);
        }
    };

    public DataConverterSchemaV705(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
        schema.registerSimple(map, s);
    }

    protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
        schema.register(map, s, () -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();

        schema.register(map, "minecraft:area_effect_cloud", (s) -> {
            return DSL.optionalFields("Particle", DataConverterTypes.PARTICLE.in(schema));
        });
        registerMob(schema, map, "minecraft:armor_stand");
        schema.register(map, "minecraft:arrow", (s) -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        registerMob(schema, map, "minecraft:bat");
        registerMob(schema, map, "minecraft:blaze");
        schema.registerSimple(map, "minecraft:boat");
        registerMob(schema, map, "minecraft:cave_spider");
        schema.register(map, "minecraft:chest_minecart", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), "Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        registerMob(schema, map, "minecraft:chicken");
        schema.register(map, "minecraft:commandblock_minecart", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), "LastOutput", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        registerMob(schema, map, "minecraft:cow");
        registerMob(schema, map, "minecraft:creeper");
        schema.register(map, "minecraft:donkey", (s) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "minecraft:dragon_fireball");
        registerThrowableProjectile(schema, map, "minecraft:egg");
        registerMob(schema, map, "minecraft:elder_guardian");
        schema.registerSimple(map, "minecraft:ender_crystal");
        registerMob(schema, map, "minecraft:ender_dragon");
        schema.register(map, "minecraft:enderman", (s) -> {
            return DSL.optionalFields("carried", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        registerMob(schema, map, "minecraft:endermite");
        registerThrowableProjectile(schema, map, "minecraft:ender_pearl");
        schema.registerSimple(map, "minecraft:eye_of_ender_signal");
        schema.register(map, "minecraft:falling_block", (s) -> {
            return DSL.optionalFields("Block", DataConverterTypes.BLOCK_NAME.in(schema), "TileEntityData", DataConverterTypes.BLOCK_ENTITY.in(schema));
        });
        registerThrowableProjectile(schema, map, "minecraft:fireball");
        schema.register(map, "minecraft:fireworks_rocket", (s) -> {
            return DSL.optionalFields("FireworksItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.register(map, "minecraft:furnace_minecart", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        registerMob(schema, map, "minecraft:ghast");
        registerMob(schema, map, "minecraft:giant");
        registerMob(schema, map, "minecraft:guardian");
        schema.register(map, "minecraft:hopper_minecart", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), "Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        schema.register(map, "minecraft:horse", (s) -> {
            return DSL.optionalFields("ArmorItem", DataConverterTypes.ITEM_STACK.in(schema), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:husk");
        schema.register(map, "minecraft:item", (s) -> {
            return DSL.optionalFields("Item", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.register(map, "minecraft:item_frame", (s) -> {
            return DSL.optionalFields("Item", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "minecraft:leash_knot");
        registerMob(schema, map, "minecraft:magma_cube");
        schema.register(map, "minecraft:minecart", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        registerMob(schema, map, "minecraft:mooshroom");
        schema.register(map, "minecraft:mule", (s) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:ocelot");
        schema.registerSimple(map, "minecraft:painting");
        registerMob(schema, map, "minecraft:parrot");
        registerMob(schema, map, "minecraft:pig");
        registerMob(schema, map, "minecraft:polar_bear");
        schema.register(map, "minecraft:potion", (s) -> {
            return DSL.optionalFields("Potion", DataConverterTypes.ITEM_STACK.in(schema), "inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        registerMob(schema, map, "minecraft:rabbit");
        registerMob(schema, map, "minecraft:sheep");
        registerMob(schema, map, "minecraft:shulker");
        schema.registerSimple(map, "minecraft:shulker_bullet");
        registerMob(schema, map, "minecraft:silverfish");
        registerMob(schema, map, "minecraft:skeleton");
        schema.register(map, "minecraft:skeleton_horse", (s) -> {
            return DSL.optionalFields("SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:slime");
        registerThrowableProjectile(schema, map, "minecraft:small_fireball");
        registerThrowableProjectile(schema, map, "minecraft:snowball");
        registerMob(schema, map, "minecraft:snowman");
        schema.register(map, "minecraft:spawner_minecart", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), DataConverterTypes.UNTAGGED_SPAWNER.in(schema));
        });
        schema.register(map, "minecraft:spectral_arrow", (s) -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        registerMob(schema, map, "minecraft:spider");
        registerMob(schema, map, "minecraft:squid");
        registerMob(schema, map, "minecraft:stray");
        schema.registerSimple(map, "minecraft:tnt");
        schema.register(map, "minecraft:tnt_minecart", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        schema.register(map, "minecraft:villager", (s) -> {
            return DSL.optionalFields("Inventory", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DataConverterTypes.VILLAGER_TRADE.in(schema))));
        });
        registerMob(schema, map, "minecraft:villager_golem");
        registerMob(schema, map, "minecraft:witch");
        registerMob(schema, map, "minecraft:wither");
        registerMob(schema, map, "minecraft:wither_skeleton");
        registerThrowableProjectile(schema, map, "minecraft:wither_skull");
        registerMob(schema, map, "minecraft:wolf");
        registerThrowableProjectile(schema, map, "minecraft:xp_bottle");
        schema.registerSimple(map, "minecraft:xp_orb");
        registerMob(schema, map, "minecraft:zombie");
        schema.register(map, "minecraft:zombie_horse", (s) -> {
            return DSL.optionalFields("SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:zombie_pigman");
        schema.register(map, "minecraft:zombie_villager", (s) -> {
            return DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(DataConverterTypes.VILLAGER_TRADE.in(schema))));
        });
        schema.registerSimple(map, "minecraft:evocation_fangs");
        registerMob(schema, map, "minecraft:evocation_illager");
        registerMob(schema, map, "minecraft:illusion_illager");
        schema.register(map, "minecraft:llama", (s) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema), "DecorItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "minecraft:llama_spit");
        registerMob(schema, map, "minecraft:vex");
        registerMob(schema, map, "minecraft:vindication_illager");
        return map;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.ENTITY, () -> {
            return DSL.and(DataConverterTypes.ENTITY_EQUIPMENT.in(schema), DSL.optionalFields("CustomName", DSL.constType(DSL.string()), DSL.taggedChoiceLazy("id", namespacedString(), map)));
        });
        schema.registerType(true, DataConverterTypes.ITEM_STACK, () -> {
            return DSL.hook(DSL.optionalFields("id", DataConverterTypes.ITEM_NAME.in(schema), "tag", DataConverterSchemaV99.itemStackTag(schema)), DataConverterSchemaV705.ADD_NAMES, HookFunction.IDENTITY);
        });
    }
}
