package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V3202 extends DataConverterSchemaNamed {

    public V3202(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        map.put("minecraft:hanging_sign", (Supplier) () -> {
            return DataConverterSchemaV99.sign(schema);
        });
        return map;
    }
}
