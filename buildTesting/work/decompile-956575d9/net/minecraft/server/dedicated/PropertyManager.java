package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.IRegistryCustom;
import org.slf4j.Logger;

public abstract class PropertyManager<T extends PropertyManager<T>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    public final Properties properties;

    public PropertyManager(Properties properties) {
        this.properties = properties;
    }

    public static Properties loadFromFile(Path path) {
        try {
            try (InputStream inputstream = Files.newInputStream(path)) {
                CharsetDecoder charsetdecoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
                Properties properties = new Properties();

                properties.load(new InputStreamReader(inputstream, charsetdecoder));
                return properties;
            } catch (CharacterCodingException charactercodingexception) {
                PropertyManager.LOGGER.info("Failed to load properties as UTF-8 from file {}, trying ISO_8859_1", path);

                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
                    Properties properties1 = new Properties();

                    properties1.load(reader);
                    return properties1;
                }
            }
        } catch (IOException ioexception) {
            PropertyManager.LOGGER.error("Failed to load properties from file: {}", path, ioexception);
            return new Properties();
        }
    }

    public void store(Path path) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            this.properties.store(writer, "Minecraft server properties");
        } catch (IOException ioexception) {
            PropertyManager.LOGGER.error("Failed to store properties to file: {}", path);
        }

    }

    private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> function) {
        return (s) -> {
            try {
                return (Number) function.apply(s);
            } catch (NumberFormatException numberformatexception) {
                return null;
            }
        };
    }

    protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> intfunction, Function<String, V> function) {
        return (s) -> {
            try {
                return intfunction.apply(Integer.parseInt(s));
            } catch (NumberFormatException numberformatexception) {
                return function.apply(s);
            }
        };
    }

    @Nullable
    private String getStringRaw(String s) {
        return (String) this.properties.get(s);
    }

    @Nullable
    protected <V> V getLegacy(String s, Function<String, V> function) {
        String s1 = this.getStringRaw(s);

        if (s1 == null) {
            return null;
        } else {
            this.properties.remove(s);
            return (V) function.apply(s1);
        }
    }

    protected <V> V get(String s, Function<String, V> function, Function<V, String> function1, V v0) {
        String s1 = this.getStringRaw(s);
        V v1 = (V) MoreObjects.firstNonNull(s1 != null ? function.apply(s1) : null, v0);

        this.properties.put(s, function1.apply(v1));
        return v1;
    }

    protected <V> PropertyManager<T>.EditableProperty<V> getMutable(String s, Function<String, V> function, Function<V, String> function1, V v0) {
        String s1 = this.getStringRaw(s);
        V v1 = (V) MoreObjects.firstNonNull(s1 != null ? function.apply(s1) : null, v0);

        this.properties.put(s, function1.apply(v1));
        return new PropertyManager.EditableProperty<V>(s, v1, function1);
    }

    protected <V> V get(String s, Function<String, V> function, UnaryOperator<V> unaryoperator, Function<V, String> function1, V v0) {
        return (V) this.get(s, (s1) -> {
            V v1 = (V) function.apply(s1);

            return v1 != null ? unaryoperator.apply(v1) : null;
        }, function1, v0);
    }

    protected <V> V get(String s, Function<String, V> function, V v0) {
        return (V) this.get(s, function, Objects::toString, v0);
    }

    protected <V> PropertyManager<T>.EditableProperty<V> getMutable(String s, Function<String, V> function, V v0) {
        return this.<V>getMutable(s, function, Objects::toString, v0);
    }

    protected String get(String s, String s1) {
        return (String) this.get(s, Function.identity(), Function.identity(), s1);
    }

    @Nullable
    protected String getLegacyString(String s) {
        return (String) this.getLegacy(s, Function.identity());
    }

    protected int get(String s, int i) {
        return (Integer) this.get(s, wrapNumberDeserializer(Integer::parseInt), i);
    }

    protected PropertyManager<T>.EditableProperty<Integer> getMutable(String s, int i) {
        return this.<Integer>getMutable(s, wrapNumberDeserializer(Integer::parseInt), i);
    }

    protected int get(String s, UnaryOperator<Integer> unaryoperator, int i) {
        return (Integer) this.get(s, wrapNumberDeserializer(Integer::parseInt), unaryoperator, Objects::toString, i);
    }

    protected long get(String s, long i) {
        return (Long) this.get(s, wrapNumberDeserializer(Long::parseLong), i);
    }

    protected boolean get(String s, boolean flag) {
        return (Boolean) this.get(s, Boolean::valueOf, flag);
    }

    protected PropertyManager<T>.EditableProperty<Boolean> getMutable(String s, boolean flag) {
        return this.<Boolean>getMutable(s, Boolean::valueOf, flag);
    }

    @Nullable
    protected Boolean getLegacyBoolean(String s) {
        return (Boolean) this.getLegacy(s, Boolean::valueOf);
    }

    protected Properties cloneProperties() {
        Properties properties = new Properties();

        properties.putAll(this.properties);
        return properties;
    }

    protected abstract T reload(IRegistryCustom iregistrycustom, Properties properties);

    public class EditableProperty<V> implements Supplier<V> {

        private final String key;
        private final V value;
        private final Function<V, String> serializer;

        EditableProperty(final String s, final Object object, final Function function) {
            this.key = s;
            this.value = object;
            this.serializer = function;
        }

        public V get() {
            return this.value;
        }

        public T update(IRegistryCustom iregistrycustom, V v0) {
            Properties properties = PropertyManager.this.cloneProperties();

            properties.put(this.key, this.serializer.apply(v0));
            return (T) PropertyManager.this.reload(iregistrycustom, properties);
        }
    }
}
