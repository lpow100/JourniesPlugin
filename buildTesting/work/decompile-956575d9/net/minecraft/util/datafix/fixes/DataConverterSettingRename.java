package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class DataConverterSettingRename extends DataFix {

    private final String fixName;
    private final String fieldFrom;
    private final String fieldTo;

    public DataConverterSettingRename(Schema schema, boolean flag, String s, String s1, String s2) {
        super(schema, flag);
        this.fixName = s;
        this.fieldFrom = s1;
        this.fieldTo = s2;
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.fixName, this.getInputSchema().getType(DataConverterTypes.OPTIONS), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.renameField(this.fieldFrom, this.fieldTo);
            });
        });
    }
}
