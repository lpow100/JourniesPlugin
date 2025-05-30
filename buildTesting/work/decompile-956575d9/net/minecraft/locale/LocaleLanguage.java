package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatFormatted;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.FormattedString;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class LocaleLanguage {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT = "en_us";
    private static volatile LocaleLanguage instance = loadDefault();

    public LocaleLanguage() {}

    private static LocaleLanguage loadDefault() {
        DeprecatedTranslationsInfo deprecatedtranslationsinfo = DeprecatedTranslationsInfo.loadFromDefaultResource();
        Map<String, String> map = new HashMap();

        Objects.requireNonNull(map);
        BiConsumer<String, String> biconsumer = map::put;

        parseTranslations(biconsumer, "/assets/minecraft/lang/en_us.json");
        deprecatedtranslationsinfo.applyToMap(map);
        final Map<String, String> map1 = Map.copyOf(map);

        return new LocaleLanguage() {
            @Override
            public String getOrDefault(String s, String s1) {
                return (String) map1.getOrDefault(s, s1);
            }

            @Override
            public boolean has(String s) {
                return map1.containsKey(s);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedString getVisualOrder(IChatFormatted ichatformatted) {
                return (formattedstringempty) -> {
                    return ichatformatted.visit((chatmodifier, s) -> {
                        return StringDecomposer.iterateFormatted(s, chatmodifier, formattedstringempty) ? Optional.empty() : IChatFormatted.STOP_ITERATION;
                    }, ChatModifier.EMPTY).isPresent();
                };
            }
        };
    }

    private static void parseTranslations(BiConsumer<String, String> biconsumer, String s) {
        try (InputStream inputstream = LocaleLanguage.class.getResourceAsStream(s)) {
            loadFromJson(inputstream, biconsumer);
        } catch (JsonParseException | IOException ioexception) {
            LocaleLanguage.LOGGER.error("Couldn't read strings from {}", s, ioexception);
        }

    }

    public static void loadFromJson(InputStream inputstream, BiConsumer<String, String> biconsumer) {
        JsonObject jsonobject = (JsonObject) LocaleLanguage.GSON.fromJson(new InputStreamReader(inputstream, StandardCharsets.UTF_8), JsonObject.class);

        for (Map.Entry<String, JsonElement> map_entry : jsonobject.entrySet()) {
            String s = LocaleLanguage.UNSUPPORTED_FORMAT_PATTERN.matcher(ChatDeserializer.convertToString((JsonElement) map_entry.getValue(), (String) map_entry.getKey())).replaceAll("%$1s");

            biconsumer.accept((String) map_entry.getKey(), s);
        }

    }

    public static LocaleLanguage getInstance() {
        return LocaleLanguage.instance;
    }

    public static void inject(LocaleLanguage localelanguage) {
        LocaleLanguage.instance = localelanguage;
    }

    public String getOrDefault(String s) {
        return this.getOrDefault(s, s);
    }

    public abstract String getOrDefault(String s, String s1);

    public abstract boolean has(String s);

    public abstract boolean isDefaultRightToLeft();

    public abstract FormattedString getVisualOrder(IChatFormatted ichatformatted);

    public List<FormattedString> getVisualOrder(List<IChatFormatted> list) {
        return (List) list.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
    }
}
