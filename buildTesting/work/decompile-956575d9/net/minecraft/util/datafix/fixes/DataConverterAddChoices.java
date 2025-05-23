package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Locale;

public class DataConverterAddChoices extends DataFix {

    private final String name;
    private final TypeReference type;

    public DataConverterAddChoices(Schema schema, String s, TypeReference typereference) {
        super(schema, true);
        this.name = s;
        this.type = typereference;
    }

    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(this.type);
        TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype1 = this.getOutputSchema().findChoiceType(this.type);

        return this.cap(taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1);
    }

    private <K> TypeRewriteRule cap(TaggedChoice.TaggedChoiceType<K> taggedchoice_taggedchoicetype, TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype1) {
        if (taggedchoice_taggedchoicetype.getKeyType() != taggedchoice_taggedchoicetype1.getKeyType()) {
            throw new IllegalStateException("Could not inject: key type is not the same");
        } else {
            return this.fixTypeEverywhere(this.name, taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1, (dynamicops) -> {
                return (pair) -> {
                    if (!taggedchoice_taggedchoicetype1.hasType(pair.getFirst())) {
                        throw new IllegalArgumentException(String.format(Locale.ROOT, "%s: Unknown type %s in '%s'", this.name, pair.getFirst(), this.type.typeName()));
                    } else {
                        return pair;
                    }
                };
            });
        }
    }
}
