package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface IRegistryCustom extends HolderLookup.a {

    Logger LOGGER = LogUtils.getLogger();
    IRegistryCustom.Dimension EMPTY = (new IRegistryCustom.c(Map.of())).freeze();

    @Override
    <E> Optional<IRegistry<E>> lookup(ResourceKey<? extends IRegistry<? extends E>> resourcekey);

    @Override
    default <E> IRegistry<E> lookupOrThrow(ResourceKey<? extends IRegistry<? extends E>> resourcekey) {
        return (IRegistry) this.lookup(resourcekey).orElseThrow(() -> {
            return new IllegalStateException("Missing registry: " + String.valueOf(resourcekey));
        });
    }

    Stream<IRegistryCustom.d<?>> registries();

    @Override
    default Stream<ResourceKey<? extends IRegistry<?>>> listRegistryKeys() {
        return this.registries().map((iregistrycustom_d) -> {
            return iregistrycustom_d.key;
        });
    }

    static IRegistryCustom.Dimension fromRegistryOfRegistries(final IRegistry<? extends IRegistry<?>> iregistry) {
        return new IRegistryCustom.Dimension() {
            @Override
            public <T> Optional<IRegistry<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                IRegistry<IRegistry<T>> iregistry1 = iregistry;

                return iregistry1.getOptional(resourcekey);
            }

            @Override
            public Stream<IRegistryCustom.d<?>> registries() {
                return iregistry.entrySet().stream().map(IRegistryCustom.d::fromMapEntry);
            }

            @Override
            public IRegistryCustom.Dimension freeze() {
                return this;
            }
        };
    }

    default IRegistryCustom.Dimension freeze() {
        class a extends IRegistryCustom.c implements IRegistryCustom.Dimension {

            protected a(final Stream stream) {
                super(stream);
            }
        }

        return new a(this.registries().map(IRegistryCustom.d::freeze));
    }

    public static record d<T>(ResourceKey<? extends IRegistry<T>> key, IRegistry<T> value) {

        private static <T, R extends IRegistry<? extends T>> IRegistryCustom.d<T> fromMapEntry(Map.Entry<? extends ResourceKey<? extends IRegistry<?>>, R> map_entry) {
            return fromUntyped((ResourceKey) map_entry.getKey(), (IRegistry) map_entry.getValue());
        }

        private static <T> IRegistryCustom.d<T> fromUntyped(ResourceKey<? extends IRegistry<?>> resourcekey, IRegistry<?> iregistry) {
            return new IRegistryCustom.d<T>(resourcekey, iregistry);
        }

        private IRegistryCustom.d<T> freeze() {
            return new IRegistryCustom.d<T>(this.key, this.value.freeze());
        }
    }

    public static class c implements IRegistryCustom {

        private final Map<? extends ResourceKey<? extends IRegistry<?>>, ? extends IRegistry<?>> registries;

        public c(List<? extends IRegistry<?>> list) {
            this.registries = (Map) list.stream().collect(Collectors.toUnmodifiableMap(IRegistry::key, (iregistry) -> {
                return iregistry;
            }));
        }

        public c(Map<? extends ResourceKey<? extends IRegistry<?>>, ? extends IRegistry<?>> map) {
            this.registries = Map.copyOf(map);
        }

        public c(Stream<IRegistryCustom.d<?>> stream) {
            this.registries = (Map) stream.collect(ImmutableMap.toImmutableMap(IRegistryCustom.d::key, IRegistryCustom.d::value));
        }

        @Override
        public <E> Optional<IRegistry<E>> lookup(ResourceKey<? extends IRegistry<? extends E>> resourcekey) {
            return Optional.ofNullable((IRegistry) this.registries.get(resourcekey)).map((iregistry) -> {
                return iregistry;
            });
        }

        @Override
        public Stream<IRegistryCustom.d<?>> registries() {
            return this.registries.entrySet().stream().map(IRegistryCustom.d::fromMapEntry);
        }
    }

    public interface Dimension extends IRegistryCustom {}
}
