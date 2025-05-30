package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICommandListener;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.packs.resources.IReloadListener;
import net.minecraft.server.packs.resources.IResource;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.tags.TagDataPack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class CustomFunctionManager implements IReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<IRegistry<CommandFunction<CommandListenerWrapper>>> TYPE_KEY = ResourceKey.createRegistryKey(MinecraftKey.withDefaultNamespace("function"));
    private static final FileToIdConverter LISTER = new FileToIdConverter(Registries.elementsDirPath(CustomFunctionManager.TYPE_KEY), ".mcfunction");
    private volatile Map<MinecraftKey, CommandFunction<CommandListenerWrapper>> functions = ImmutableMap.of();
    private final TagDataPack<CommandFunction<CommandListenerWrapper>> tagsLoader;
    private volatile Map<MinecraftKey, List<CommandFunction<CommandListenerWrapper>>> tags;
    private final int functionCompilationLevel;
    private final CommandDispatcher<CommandListenerWrapper> dispatcher;

    public Optional<CommandFunction<CommandListenerWrapper>> getFunction(MinecraftKey minecraftkey) {
        return Optional.ofNullable((CommandFunction) this.functions.get(minecraftkey));
    }

    public Map<MinecraftKey, CommandFunction<CommandListenerWrapper>> getFunctions() {
        return this.functions;
    }

    public List<CommandFunction<CommandListenerWrapper>> getTag(MinecraftKey minecraftkey) {
        return (List) this.tags.getOrDefault(minecraftkey, List.of());
    }

    public Iterable<MinecraftKey> getAvailableTags() {
        return this.tags.keySet();
    }

    public CustomFunctionManager(int i, CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        this.tagsLoader = new TagDataPack<CommandFunction<CommandListenerWrapper>>((minecraftkey, flag) -> {
            return this.getFunction(minecraftkey);
        }, Registries.tagsDirPath(CustomFunctionManager.TYPE_KEY));
        this.tags = Map.of();
        this.functionCompilationLevel = i;
        this.dispatcher = commanddispatcher;
    }

    @Override
    public CompletableFuture<Void> reload(IReloadListener.a ireloadlistener_a, IResourceManager iresourcemanager, Executor executor, Executor executor1) {
        CompletableFuture<Map<MinecraftKey, List<TagDataPack.b>>> completablefuture = CompletableFuture.supplyAsync(() -> {
            return this.tagsLoader.load(iresourcemanager);
        }, executor);
        CompletableFuture<Map<MinecraftKey, CompletableFuture<CommandFunction<CommandListenerWrapper>>>> completablefuture1 = CompletableFuture.supplyAsync(() -> {
            return CustomFunctionManager.LISTER.listMatchingResources(iresourcemanager);
        }, executor).thenCompose((map) -> {
            Map<MinecraftKey, CompletableFuture<CommandFunction<CommandListenerWrapper>>> map1 = Maps.newHashMap();
            CommandListenerWrapper commandlistenerwrapper = new CommandListenerWrapper(ICommandListener.NULL, Vec3D.ZERO, Vec2F.ZERO, (WorldServer) null, this.functionCompilationLevel, "", CommonComponents.EMPTY, (MinecraftServer) null, (Entity) null);

            for (Map.Entry<MinecraftKey, IResource> map_entry : map.entrySet()) {
                MinecraftKey minecraftkey = (MinecraftKey) map_entry.getKey();
                MinecraftKey minecraftkey1 = CustomFunctionManager.LISTER.fileToId(minecraftkey);

                map1.put(minecraftkey1, CompletableFuture.supplyAsync(() -> {
                    List<String> list = readLines((IResource) map_entry.getValue());

                    return CommandFunction.fromLines(minecraftkey1, this.dispatcher, commandlistenerwrapper, list);
                }, executor));
            }

            CompletableFuture<?>[] acompletablefuture = (CompletableFuture[]) map1.values().toArray(new CompletableFuture[0]);

            return CompletableFuture.allOf(acompletablefuture).handle((ovoid, throwable) -> {
                return map1;
            });
        });
        CompletableFuture completablefuture2 = completablefuture.thenCombine(completablefuture1, Pair::of);

        Objects.requireNonNull(ireloadlistener_a);
        return completablefuture2.thenCompose(ireloadlistener_a::wait).thenAcceptAsync((pair) -> {
            Map<MinecraftKey, CompletableFuture<CommandFunction<CommandListenerWrapper>>> map = (Map) pair.getSecond();
            ImmutableMap.Builder<MinecraftKey, CommandFunction<CommandListenerWrapper>> immutablemap_builder = ImmutableMap.builder();

            map.forEach((minecraftkey, completablefuture3) -> {
                completablefuture3.handle((commandfunction, throwable) -> {
                    if (throwable != null) {
                        CustomFunctionManager.LOGGER.error("Failed to load function {}", minecraftkey, throwable);
                    } else {
                        immutablemap_builder.put(minecraftkey, commandfunction);
                    }

                    return null;
                }).join();
            });
            this.functions = immutablemap_builder.build();
            this.tags = this.tagsLoader.build((Map) pair.getFirst());
        }, executor1);
    }

    private static List<String> readLines(IResource iresource) {
        try (BufferedReader bufferedreader = iresource.openAsReader()) {
            return bufferedreader.lines().toList();
        } catch (IOException ioexception) {
            throw new CompletionException(ioexception);
        }
    }
}
