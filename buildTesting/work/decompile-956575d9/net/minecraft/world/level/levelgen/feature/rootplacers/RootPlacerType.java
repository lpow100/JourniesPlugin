package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class RootPlacerType<P extends RootPlacer> {

    public static final RootPlacerType<MangroveRootPlacer> MANGROVE_ROOT_PLACER = register("mangrove_root_placer", MangroveRootPlacer.CODEC);
    private final MapCodec<P> codec;

    private static <P extends RootPlacer> RootPlacerType<P> register(String s, MapCodec<P> mapcodec) {
        return (RootPlacerType) IRegistry.register(BuiltInRegistries.ROOT_PLACER_TYPE, s, new RootPlacerType(mapcodec));
    }

    private RootPlacerType(MapCodec<P> mapcodec) {
        this.codec = mapcodec;
    }

    public MapCodec<P> codec() {
        return this.codec;
    }
}
