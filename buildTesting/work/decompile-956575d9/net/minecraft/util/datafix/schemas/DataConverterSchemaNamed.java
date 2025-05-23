package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const.PrimitiveType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.resources.MinecraftKey;

public class DataConverterSchemaNamed extends Schema {

    public static final PrimitiveCodec<String> NAMESPACED_STRING_CODEC = new PrimitiveCodec<String>() {
        public <T> DataResult<String> read(DynamicOps<T> dynamicops, T t0) {
            return dynamicops.getStringValue(t0).map(DataConverterSchemaNamed::ensureNamespaced);
        }

        public <T> T write(DynamicOps<T> dynamicops, String s) {
            return (T) dynamicops.createString(s);
        }

        public String toString() {
            return "NamespacedString";
        }
    };
    private static final Type<String> NAMESPACED_STRING = new PrimitiveType(DataConverterSchemaNamed.NAMESPACED_STRING_CODEC);

    public DataConverterSchemaNamed(int i, Schema schema) {
        super(i, schema);
    }

    public static String ensureNamespaced(String s) {
        MinecraftKey minecraftkey = MinecraftKey.tryParse(s);

        return minecraftkey != null ? minecraftkey.toString() : s;
    }

    public static Type<String> namespacedString() {
        return DataConverterSchemaNamed.NAMESPACED_STRING;
    }

    public Type<?> getChoiceType(TypeReference typereference, String s) {
        return super.getChoiceType(typereference, ensureNamespaced(s));
    }
}
