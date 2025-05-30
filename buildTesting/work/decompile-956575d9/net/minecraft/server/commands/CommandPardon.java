package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentProfile;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.players.GameProfileBanList;

public class CommandPardon {

    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.pardon.failed"));

    public CommandPardon() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("pardon").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentProfile.gameProfile()).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest(((CommandListenerWrapper) commandcontext.getSource()).getServer().getPlayerList().getBans().getUserList(), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return pardonPlayers((CommandListenerWrapper) commandcontext.getSource(), ArgumentProfile.getGameProfiles(commandcontext, "targets"));
        })));
    }

    private static int pardonPlayers(CommandListenerWrapper commandlistenerwrapper, Collection<GameProfile> collection) throws CommandSyntaxException {
        GameProfileBanList gameprofilebanlist = commandlistenerwrapper.getServer().getPlayerList().getBans();
        int i = 0;

        for (GameProfile gameprofile : collection) {
            if (gameprofilebanlist.isBanned(gameprofile)) {
                gameprofilebanlist.remove(gameprofile);
                ++i;
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.pardon.success", IChatBaseComponent.literal(gameprofile.getName()));
                }, true);
            }
        }

        if (i == 0) {
            throw CommandPardon.ERROR_NOT_BANNED.create();
        } else {
            return i;
        }
    }
}
