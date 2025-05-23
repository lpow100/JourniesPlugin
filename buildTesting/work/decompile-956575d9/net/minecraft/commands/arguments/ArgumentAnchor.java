package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

public class ArgumentAnchor implements ArgumentType<ArgumentAnchor.Anchor> {

    private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.anchor.invalid", object);
    });

    public ArgumentAnchor() {}

    public static ArgumentAnchor.Anchor getAnchor(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (ArgumentAnchor.Anchor) commandcontext.getArgument(s, ArgumentAnchor.Anchor.class);
    }

    public static ArgumentAnchor anchor() {
        return new ArgumentAnchor();
    }

    public ArgumentAnchor.Anchor parse(StringReader stringreader) throws CommandSyntaxException {
        int i = stringreader.getCursor();
        String s = stringreader.readUnquotedString();
        ArgumentAnchor.Anchor argumentanchor_anchor = ArgumentAnchor.Anchor.getByName(s);

        if (argumentanchor_anchor == null) {
            stringreader.setCursor(i);
            throw ArgumentAnchor.ERROR_INVALID.createWithContext(stringreader, s);
        } else {
            return argumentanchor_anchor;
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        return ICompletionProvider.suggest(ArgumentAnchor.Anchor.BY_NAME.keySet(), suggestionsbuilder);
    }

    public Collection<String> getExamples() {
        return ArgumentAnchor.EXAMPLES;
    }

    public static enum Anchor {

        FEET("feet", (vec3d, entity) -> {
            return vec3d;
        }), EYES("eyes", (vec3d, entity) -> {
            return new Vec3D(vec3d.x, vec3d.y + (double) entity.getEyeHeight(), vec3d.z);
        });

        static final Map<String, ArgumentAnchor.Anchor> BY_NAME = (Map) SystemUtils.make(Maps.newHashMap(), (hashmap) -> {
            for (ArgumentAnchor.Anchor argumentanchor_anchor : values()) {
                hashmap.put(argumentanchor_anchor.name, argumentanchor_anchor);
            }

        });
        private final String name;
        private final BiFunction<Vec3D, Entity, Vec3D> transform;

        private Anchor(final String s, final BiFunction bifunction) {
            this.name = s;
            this.transform = bifunction;
        }

        @Nullable
        public static ArgumentAnchor.Anchor getByName(String s) {
            return (ArgumentAnchor.Anchor) ArgumentAnchor.Anchor.BY_NAME.get(s);
        }

        public Vec3D apply(Entity entity) {
            return (Vec3D) this.transform.apply(entity.position(), entity);
        }

        public Vec3D apply(CommandListenerWrapper commandlistenerwrapper) {
            Entity entity = commandlistenerwrapper.getEntity();

            return entity == null ? commandlistenerwrapper.getPosition() : (Vec3D) this.transform.apply(commandlistenerwrapper.getPosition(), entity);
        }
    }
}
