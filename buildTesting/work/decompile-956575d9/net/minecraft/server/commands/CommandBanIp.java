package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentChat;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.players.IpBanEntry;
import net.minecraft.server.players.IpBanList;

public class CommandBanIp {

    private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.banip.invalid"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.banip.failed"));

    public CommandBanIp() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("ban-ip").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("target", StringArgumentType.word()).executes((commandcontext) -> {
            return banIpOrName((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "target"), (IChatBaseComponent) null);
        })).then(net.minecraft.commands.CommandDispatcher.argument("reason", ArgumentChat.message()).executes((commandcontext) -> {
            return banIpOrName((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "target"), ArgumentChat.getMessage(commandcontext, "reason"));
        }))));
    }

    private static int banIpOrName(CommandListenerWrapper commandlistenerwrapper, String s, @Nullable IChatBaseComponent ichatbasecomponent) throws CommandSyntaxException {
        if (InetAddresses.isInetAddress(s)) {
            return banIp(commandlistenerwrapper, s, ichatbasecomponent);
        } else {
            EntityPlayer entityplayer = commandlistenerwrapper.getServer().getPlayerList().getPlayerByName(s);

            if (entityplayer != null) {
                return banIp(commandlistenerwrapper, entityplayer.getIpAddress(), ichatbasecomponent);
            } else {
                throw CommandBanIp.ERROR_INVALID_IP.create();
            }
        }
    }

    private static int banIp(CommandListenerWrapper commandlistenerwrapper, String s, @Nullable IChatBaseComponent ichatbasecomponent) throws CommandSyntaxException {
        IpBanList ipbanlist = commandlistenerwrapper.getServer().getPlayerList().getIpBans();

        if (ipbanlist.isBanned(s)) {
            throw CommandBanIp.ERROR_ALREADY_BANNED.create();
        } else {
            List<EntityPlayer> list = commandlistenerwrapper.getServer().getPlayerList().getPlayersWithAddress(s);
            IpBanEntry ipbanentry = new IpBanEntry(s, (Date) null, commandlistenerwrapper.getTextName(), (Date) null, ichatbasecomponent == null ? null : ichatbasecomponent.getString());

            ipbanlist.add(ipbanentry);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.banip.success", s, ipbanentry.getReason());
            }, true);
            if (!list.isEmpty()) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.banip.info", list.size(), EntitySelector.joinNames(list));
                }, true);
            }

            for (EntityPlayer entityplayer : list) {
                entityplayer.connection.disconnect((IChatBaseComponent) IChatBaseComponent.translatable("multiplayer.disconnect.ip_banned"));
            }

            return list.size();
        }
    }
}
