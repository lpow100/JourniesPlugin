package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.EnumChatFormat;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentChatComponent;
import net.minecraft.commands.arguments.ArgumentChatFormat;
import net.minecraft.commands.arguments.ArgumentScoreboardTeam;
import net.minecraft.commands.arguments.ArgumentScoreholder;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;

public class CommandTeam {

    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.add.duplicate"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.empty.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.name.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.color.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.friendlyfire.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.friendlyfire.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.seeFriendlyInvisibles.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.seeFriendlyInvisibles.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.nametagVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.deathMessageVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.team.option.collisionRule.unchanged"));

    public CommandTeam() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("team").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("list").executes((commandcontext) -> {
            return listTeams((CommandListenerWrapper) commandcontext.getSource());
        })).then(net.minecraft.commands.CommandDispatcher.argument("team", ArgumentScoreboardTeam.team()).executes((commandcontext) -> {
            return listMembers((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("add").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("team", StringArgumentType.word()).executes((commandcontext) -> {
            return createTeam((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "team"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("displayName", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return createTeam((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "team"), ArgumentChatComponent.getResolvedComponent(commandcontext, "displayName"));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("remove").then(net.minecraft.commands.CommandDispatcher.argument("team", ArgumentScoreboardTeam.team()).executes((commandcontext) -> {
            return deleteTeam((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("empty").then(net.minecraft.commands.CommandDispatcher.argument("team", ArgumentScoreboardTeam.team()).executes((commandcontext) -> {
            return emptyTeam((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("join").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("team", ArgumentScoreboardTeam.team()).executes((commandcontext) -> {
            return joinTeam((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), Collections.singleton(((CommandListenerWrapper) commandcontext.getSource()).getEntityOrException()));
        })).then(net.minecraft.commands.CommandDispatcher.argument("members", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).executes((commandcontext) -> {
            return joinTeam((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "members"));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("leave").then(net.minecraft.commands.CommandDispatcher.argument("members", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).executes((commandcontext) -> {
            return leaveTeam((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "members"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("modify").then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("team", ArgumentScoreboardTeam.team()).then(net.minecraft.commands.CommandDispatcher.literal("displayName").then(net.minecraft.commands.CommandDispatcher.argument("displayName", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return setDisplayName((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ArgumentChatComponent.getResolvedComponent(commandcontext, "displayName"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("color").then(net.minecraft.commands.CommandDispatcher.argument("value", ArgumentChatFormat.color()).executes((commandcontext) -> {
            return setColor((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ArgumentChatFormat.getColor(commandcontext, "value"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("friendlyFire").then(net.minecraft.commands.CommandDispatcher.argument("allowed", BoolArgumentType.bool()).executes((commandcontext) -> {
            return setFriendlyFire((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), BoolArgumentType.getBool(commandcontext, "allowed"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("seeFriendlyInvisibles").then(net.minecraft.commands.CommandDispatcher.argument("allowed", BoolArgumentType.bool()).executes((commandcontext) -> {
            return setFriendlySight((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), BoolArgumentType.getBool(commandcontext, "allowed"));
        })))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("nametagVisibility").then(net.minecraft.commands.CommandDispatcher.literal("never").executes((commandcontext) -> {
            return setNametagVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("hideForOtherTeams").executes((commandcontext) -> {
            return setNametagVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("hideForOwnTeam").executes((commandcontext) -> {
            return setNametagVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OWN_TEAM);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("always").executes((commandcontext) -> {
            return setNametagVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
        })))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("deathMessageVisibility").then(net.minecraft.commands.CommandDispatcher.literal("never").executes((commandcontext) -> {
            return setDeathMessageVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("hideForOtherTeams").executes((commandcontext) -> {
            return setDeathMessageVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("hideForOwnTeam").executes((commandcontext) -> {
            return setDeathMessageVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OWN_TEAM);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("always").executes((commandcontext) -> {
            return setDeathMessageVisibility((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
        })))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("collisionRule").then(net.minecraft.commands.CommandDispatcher.literal("never").executes((commandcontext) -> {
            return setCollision((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumTeamPush.NEVER);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("pushOwnTeam").executes((commandcontext) -> {
            return setCollision((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumTeamPush.PUSH_OWN_TEAM);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("pushOtherTeams").executes((commandcontext) -> {
            return setCollision((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumTeamPush.PUSH_OTHER_TEAMS);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("always").executes((commandcontext) -> {
            return setCollision((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ScoreboardTeamBase.EnumTeamPush.ALWAYS);
        })))).then(net.minecraft.commands.CommandDispatcher.literal("prefix").then(net.minecraft.commands.CommandDispatcher.argument("prefix", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return setPrefix((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ArgumentChatComponent.getResolvedComponent(commandcontext, "prefix"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("suffix").then(net.minecraft.commands.CommandDispatcher.argument("suffix", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return setSuffix((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardTeam.getTeam(commandcontext, "team"), ArgumentChatComponent.getResolvedComponent(commandcontext, "suffix"));
        }))))));
    }

    private static IChatBaseComponent getFirstMemberName(Collection<ScoreHolder> collection) {
        return ((ScoreHolder) collection.iterator().next()).getFeedbackDisplayName();
    }

    private static int leaveTeam(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection) {
        Scoreboard scoreboard = commandlistenerwrapper.getServer().getScoreboard();

        for (ScoreHolder scoreholder : collection) {
            scoreboard.removePlayerFromTeam(scoreholder.getScoreboardName());
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.leave.success.single", getFirstMemberName(collection));
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.leave.success.multiple", collection.size());
            }, true);
        }

        return collection.size();
    }

    private static int joinTeam(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, Collection<ScoreHolder> collection) {
        Scoreboard scoreboard = commandlistenerwrapper.getServer().getScoreboard();

        for (ScoreHolder scoreholder : collection) {
            scoreboard.addPlayerToTeam(scoreholder.getScoreboardName(), scoreboardteam);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.join.success.single", getFirstMemberName(collection), scoreboardteam.getFormattedDisplayName());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.join.success.multiple", collection.size(), scoreboardteam.getFormattedDisplayName());
            }, true);
        }

        return collection.size();
    }

    private static int setNametagVisibility(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, ScoreboardTeamBase.EnumNameTagVisibility scoreboardteambase_enumnametagvisibility) throws CommandSyntaxException {
        if (scoreboardteam.getNameTagVisibility() == scoreboardteambase_enumnametagvisibility) {
            throw CommandTeam.ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
        } else {
            scoreboardteam.setNameTagVisibility(scoreboardteambase_enumnametagvisibility);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.option.nametagVisibility.success", scoreboardteam.getFormattedDisplayName(), scoreboardteambase_enumnametagvisibility.getDisplayName());
            }, true);
            return 0;
        }
    }

    private static int setDeathMessageVisibility(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, ScoreboardTeamBase.EnumNameTagVisibility scoreboardteambase_enumnametagvisibility) throws CommandSyntaxException {
        if (scoreboardteam.getDeathMessageVisibility() == scoreboardteambase_enumnametagvisibility) {
            throw CommandTeam.ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
        } else {
            scoreboardteam.setDeathMessageVisibility(scoreboardteambase_enumnametagvisibility);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.option.deathMessageVisibility.success", scoreboardteam.getFormattedDisplayName(), scoreboardteambase_enumnametagvisibility.getDisplayName());
            }, true);
            return 0;
        }
    }

    private static int setCollision(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, ScoreboardTeamBase.EnumTeamPush scoreboardteambase_enumteampush) throws CommandSyntaxException {
        if (scoreboardteam.getCollisionRule() == scoreboardteambase_enumteampush) {
            throw CommandTeam.ERROR_TEAM_COLLISION_UNCHANGED.create();
        } else {
            scoreboardteam.setCollisionRule(scoreboardteambase_enumteampush);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.option.collisionRule.success", scoreboardteam.getFormattedDisplayName(), scoreboardteambase_enumteampush.getDisplayName());
            }, true);
            return 0;
        }
    }

    private static int setFriendlySight(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, boolean flag) throws CommandSyntaxException {
        if (scoreboardteam.canSeeFriendlyInvisibles() == flag) {
            if (flag) {
                throw CommandTeam.ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
            } else {
                throw CommandTeam.ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
            }
        } else {
            scoreboardteam.setSeeFriendlyInvisibles(flag);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.option.seeFriendlyInvisibles." + (flag ? "enabled" : "disabled"), scoreboardteam.getFormattedDisplayName());
            }, true);
            return 0;
        }
    }

    private static int setFriendlyFire(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, boolean flag) throws CommandSyntaxException {
        if (scoreboardteam.isAllowFriendlyFire() == flag) {
            if (flag) {
                throw CommandTeam.ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
            } else {
                throw CommandTeam.ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
            }
        } else {
            scoreboardteam.setAllowFriendlyFire(flag);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.option.friendlyfire." + (flag ? "enabled" : "disabled"), scoreboardteam.getFormattedDisplayName());
            }, true);
            return 0;
        }
    }

    private static int setDisplayName(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, IChatBaseComponent ichatbasecomponent) throws CommandSyntaxException {
        if (scoreboardteam.getDisplayName().equals(ichatbasecomponent)) {
            throw CommandTeam.ERROR_TEAM_ALREADY_NAME.create();
        } else {
            scoreboardteam.setDisplayName(ichatbasecomponent);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.option.name.success", scoreboardteam.getFormattedDisplayName());
            }, true);
            return 0;
        }
    }

    private static int setColor(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, EnumChatFormat enumchatformat) throws CommandSyntaxException {
        if (scoreboardteam.getColor() == enumchatformat) {
            throw CommandTeam.ERROR_TEAM_ALREADY_COLOR.create();
        } else {
            scoreboardteam.setColor(enumchatformat);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.option.color.success", scoreboardteam.getFormattedDisplayName(), enumchatformat.getName());
            }, true);
            return 0;
        }
    }

    private static int emptyTeam(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam) throws CommandSyntaxException {
        Scoreboard scoreboard = commandlistenerwrapper.getServer().getScoreboard();
        Collection<String> collection = Lists.newArrayList(scoreboardteam.getPlayers());

        if (collection.isEmpty()) {
            throw CommandTeam.ERROR_TEAM_ALREADY_EMPTY.create();
        } else {
            for (String s : collection) {
                scoreboard.removePlayerFromTeam(s, scoreboardteam);
            }

            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.empty.success", collection.size(), scoreboardteam.getFormattedDisplayName());
            }, true);
            return collection.size();
        }
    }

    private static int deleteTeam(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam) {
        Scoreboard scoreboard = commandlistenerwrapper.getServer().getScoreboard();

        scoreboard.removePlayerTeam(scoreboardteam);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.team.remove.success", scoreboardteam.getFormattedDisplayName());
        }, true);
        return scoreboard.getPlayerTeams().size();
    }

    private static int createTeam(CommandListenerWrapper commandlistenerwrapper, String s) throws CommandSyntaxException {
        return createTeam(commandlistenerwrapper, s, IChatBaseComponent.literal(s));
    }

    private static int createTeam(CommandListenerWrapper commandlistenerwrapper, String s, IChatBaseComponent ichatbasecomponent) throws CommandSyntaxException {
        Scoreboard scoreboard = commandlistenerwrapper.getServer().getScoreboard();

        if (scoreboard.getPlayerTeam(s) != null) {
            throw CommandTeam.ERROR_TEAM_ALREADY_EXISTS.create();
        } else {
            ScoreboardTeam scoreboardteam = scoreboard.addPlayerTeam(s);

            scoreboardteam.setDisplayName(ichatbasecomponent);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.add.success", scoreboardteam.getFormattedDisplayName());
            }, true);
            return scoreboard.getPlayerTeams().size();
        }
    }

    private static int listMembers(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam) {
        Collection<String> collection = scoreboardteam.getPlayers();

        if (collection.isEmpty()) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.list.members.empty", scoreboardteam.getFormattedDisplayName());
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.list.members.success", scoreboardteam.getFormattedDisplayName(), collection.size(), ChatComponentUtils.formatList(collection));
            }, false);
        }

        return collection.size();
    }

    private static int listTeams(CommandListenerWrapper commandlistenerwrapper) {
        Collection<ScoreboardTeam> collection = commandlistenerwrapper.getServer().getScoreboard().getPlayerTeams();

        if (collection.isEmpty()) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.list.teams.empty");
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.team.list.teams.success", collection.size(), ChatComponentUtils.formatList(collection, ScoreboardTeam::getFormattedDisplayName));
            }, false);
        }

        return collection.size();
    }

    private static int setPrefix(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, IChatBaseComponent ichatbasecomponent) {
        scoreboardteam.setPlayerPrefix(ichatbasecomponent);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.team.option.prefix.success", ichatbasecomponent);
        }, false);
        return 1;
    }

    private static int setSuffix(CommandListenerWrapper commandlistenerwrapper, ScoreboardTeam scoreboardteam, IChatBaseComponent ichatbasecomponent) {
        scoreboardteam.setPlayerSuffix(ichatbasecomponent);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.team.option.suffix.success", ichatbasecomponent);
        }, false);
        return 1;
    }
}
