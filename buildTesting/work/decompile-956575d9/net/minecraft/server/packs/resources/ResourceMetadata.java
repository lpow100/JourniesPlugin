package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ChatDeserializer;

public interface ResourceMetadata {

    ResourceMetadata EMPTY = new ResourceMetadata() {
        @Override
        public <T> Optional<T> getSection(MetadataSectionType<T> metadatasectiontype) {
            return Optional.empty();
        }
    };
    IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> {
        return ResourceMetadata.EMPTY;
    };

    static ResourceMetadata fromJsonStream(InputStream inputstream) throws IOException {
        try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))) {
            final JsonObject jsonobject = ChatDeserializer.parse((Reader) bufferedreader);

            return new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> metadatasectiontype) {
                    String s = metadatasectiontype.name();

                    if (jsonobject.has(s)) {
                        T t0 = (T) metadatasectiontype.codec().parse(JsonOps.INSTANCE, jsonobject.get(s)).getOrThrow(JsonParseException::new);

                        return Optional.of(t0);
                    } else {
                        return Optional.empty();
                    }
                }
            };
        }
    }

    <T> Optional<T> getSection(MetadataSectionType<T> metadatasectiontype);

    default ResourceMetadata copySections(Collection<MetadataSectionType<?>> collection) {
        ResourceMetadata.a resourcemetadata_a = new ResourceMetadata.a();

        for (MetadataSectionType<?> metadatasectiontype : collection) {
            this.copySection(resourcemetadata_a, metadatasectiontype);
        }

        return resourcemetadata_a.build();
    }

    private <T> void copySection(ResourceMetadata.a resourcemetadata_a, MetadataSectionType<T> metadatasectiontype) {
        this.getSection(metadatasectiontype).ifPresent((object) -> {
            resourcemetadata_a.put(metadatasectiontype, object);
        });
    }

    public static class a {

        private final ImmutableMap.Builder<MetadataSectionType<?>, Object> map = ImmutableMap.builder();

        public a() {}

        public <T> ResourceMetadata.a put(MetadataSectionType<T> metadatasectiontype, T t0) {
            this.map.put(metadatasectiontype, t0);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap<MetadataSectionType<?>, Object> immutablemap = this.map.build();

            return immutablemap.isEmpty() ? ResourceMetadata.EMPTY : new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> metadatasectiontype) {
                    return Optional.ofNullable(immutablemap.get(metadatasectiontype));
                }
            };
        }
    }
}
