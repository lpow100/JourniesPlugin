package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;

public class ArgumentScoreboardObjective implements ArgumentType<String> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
    private static final DynamicCommandExceptionType ERROR_OBJECTIVE_NOT_FOUND = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("arguments.objective.notFound", object);
    });
    private static final DynamicCommandExceptionType ERROR_OBJECTIVE_READ_ONLY = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("arguments.objective.readonly", object);
    });

    public ArgumentScoreboardObjective() {}

    public static ArgumentScoreboardObjective objective() {
        return new ArgumentScoreboardObjective();
    }

    public static ScoreboardObjective getObjective(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        String s1 = (String) commandcontext.getArgument(s, String.class);
        Scoreboard scoreboard = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getScoreboard();
        ScoreboardObjective scoreboardobjective = scoreboard.getObjective(s1);

        if (scoreboardobjective == null) {
            throw ArgumentScoreboardObjective.ERROR_OBJECTIVE_NOT_FOUND.create(s1);
        } else {
            return scoreboardobjective;
        }
    }

    public static ScoreboardObjective getWritableObjective(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        ScoreboardObjective scoreboardobjective = getObjective(commandcontext, s);

        if (scoreboardobjective.getCriteria().isReadOnly()) {
            throw ArgumentScoreboardObjective.ERROR_OBJECTIVE_READ_ONLY.create(scoreboardobjective.getName());
        } else {
            return scoreboardobjective;
        }
    }

    public String parse(StringReader stringreader) throws CommandSyntaxException {
        return stringreader.readUnquotedString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        S s0 = (S) commandcontext.getSource();

        if (s0 instanceof CommandListenerWrapper commandlistenerwrapper) {
            return ICompletionProvider.suggest(commandlistenerwrapper.getServer().getScoreboard().getObjectiveNames(), suggestionsbuilder);
        } else if (s0 instanceof ICompletionProvider icompletionprovider) {
            return icompletionprovider.customSuggestion(commandcontext);
        } else {
            return Suggestions.empty();
        }
    }

    public Collection<String> getExamples() {
        return ArgumentScoreboardObjective.EXAMPLES;
    }
}
