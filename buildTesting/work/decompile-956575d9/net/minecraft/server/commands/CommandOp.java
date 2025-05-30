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
import net.minecraft.server.players.PlayerList;

public class CommandOp {

    private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.op.failed"));

    public CommandOp() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("op").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentProfile.gameProfile()).suggests((commandcontext, suggestionsbuilder) -> {
            PlayerList playerlist = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getPlayerList();

            return ICompletionProvider.suggest(playerlist.getPlayers().stream().filter((entityplayer) -> {
                return !playerlist.isOp(entityplayer.getGameProfile());
            }).map((entityplayer) -> {
                return entityplayer.getGameProfile().getName();
            }), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return opPlayers((CommandListenerWrapper) commandcontext.getSource(), ArgumentProfile.getGameProfiles(commandcontext, "targets"));
        })));
    }

    private static int opPlayers(CommandListenerWrapper commandlistenerwrapper, Collection<GameProfile> collection) throws CommandSyntaxException {
        PlayerList playerlist = commandlistenerwrapper.getServer().getPlayerList();
        int i = 0;

        for (GameProfile gameprofile : collection) {
            if (!playerlist.isOp(gameprofile)) {
                playerlist.op(gameprofile);
                ++i;
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.op.success", gameprofile.getName());
                }, true);
            }
        }

        if (i == 0) {
            throw CommandOp.ERROR_ALREADY_OP.create();
        } else {
            return i;
        }
    }
}
