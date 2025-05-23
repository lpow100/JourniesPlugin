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
import net.minecraft.server.players.WhiteList;
import net.minecraft.server.players.WhiteListEntry;

public class CommandWhitelist {

    private static final SimpleCommandExceptionType ERROR_ALREADY_ENABLED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.whitelist.alreadyOn"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_DISABLED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.whitelist.alreadyOff"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.whitelist.add.failed"));
    private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.whitelist.remove.failed"));

    public CommandWhitelist() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("whitelist").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.literal("on").executes((commandcontext) -> {
            return enableWhitelist((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("off").executes((commandcontext) -> {
            return disableWhitelist((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("list").executes((commandcontext) -> {
            return showList((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("add").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentProfile.gameProfile()).suggests((commandcontext, suggestionsbuilder) -> {
            PlayerList playerlist = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getPlayerList();

            return ICompletionProvider.suggest(playerlist.getPlayers().stream().filter((entityplayer) -> {
                return !playerlist.getWhiteList().isWhiteListed(entityplayer.getGameProfile());
            }).map((entityplayer) -> {
                return entityplayer.getGameProfile().getName();
            }), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return addPlayers((CommandListenerWrapper) commandcontext.getSource(), ArgumentProfile.getGameProfiles(commandcontext, "targets"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("remove").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentProfile.gameProfile()).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest(((CommandListenerWrapper) commandcontext.getSource()).getServer().getPlayerList().getWhiteListNames(), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return removePlayers((CommandListenerWrapper) commandcontext.getSource(), ArgumentProfile.getGameProfiles(commandcontext, "targets"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("reload").executes((commandcontext) -> {
            return reload((CommandListenerWrapper) commandcontext.getSource());
        })));
    }

    private static int reload(CommandListenerWrapper commandlistenerwrapper) {
        commandlistenerwrapper.getServer().getPlayerList().reloadWhiteList();
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.whitelist.reloaded");
        }, true);
        commandlistenerwrapper.getServer().kickUnlistedPlayers(commandlistenerwrapper);
        return 1;
    }

    private static int addPlayers(CommandListenerWrapper commandlistenerwrapper, Collection<GameProfile> collection) throws CommandSyntaxException {
        WhiteList whitelist = commandlistenerwrapper.getServer().getPlayerList().getWhiteList();
        int i = 0;

        for (GameProfile gameprofile : collection) {
            if (!whitelist.isWhiteListed(gameprofile)) {
                WhiteListEntry whitelistentry = new WhiteListEntry(gameprofile);

                whitelist.add(whitelistentry);
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.whitelist.add.success", IChatBaseComponent.literal(gameprofile.getName()));
                }, true);
                ++i;
            }
        }

        if (i == 0) {
            throw CommandWhitelist.ERROR_ALREADY_WHITELISTED.create();
        } else {
            return i;
        }
    }

    private static int removePlayers(CommandListenerWrapper commandlistenerwrapper, Collection<GameProfile> collection) throws CommandSyntaxException {
        WhiteList whitelist = commandlistenerwrapper.getServer().getPlayerList().getWhiteList();
        int i = 0;

        for (GameProfile gameprofile : collection) {
            if (whitelist.isWhiteListed(gameprofile)) {
                WhiteListEntry whitelistentry = new WhiteListEntry(gameprofile);

                whitelist.remove(whitelistentry);
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.whitelist.remove.success", IChatBaseComponent.literal(gameprofile.getName()));
                }, true);
                ++i;
            }
        }

        if (i == 0) {
            throw CommandWhitelist.ERROR_NOT_WHITELISTED.create();
        } else {
            commandlistenerwrapper.getServer().kickUnlistedPlayers(commandlistenerwrapper);
            return i;
        }
    }

    private static int enableWhitelist(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        PlayerList playerlist = commandlistenerwrapper.getServer().getPlayerList();

        if (playerlist.isUsingWhitelist()) {
            throw CommandWhitelist.ERROR_ALREADY_ENABLED.create();
        } else {
            playerlist.setUsingWhiteList(true);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.whitelist.enabled");
            }, true);
            commandlistenerwrapper.getServer().kickUnlistedPlayers(commandlistenerwrapper);
            return 1;
        }
    }

    private static int disableWhitelist(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        PlayerList playerlist = commandlistenerwrapper.getServer().getPlayerList();

        if (!playerlist.isUsingWhitelist()) {
            throw CommandWhitelist.ERROR_ALREADY_DISABLED.create();
        } else {
            playerlist.setUsingWhiteList(false);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.whitelist.disabled");
            }, true);
            return 1;
        }
    }

    private static int showList(CommandListenerWrapper commandlistenerwrapper) {
        String[] astring = commandlistenerwrapper.getServer().getPlayerList().getWhiteListNames();

        if (astring.length == 0) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.whitelist.none");
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.whitelist.list", astring.length, String.join(", ", astring));
            }, false);
        }

        return astring.length;
    }
}
