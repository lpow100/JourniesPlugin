package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.PersistentScoreboard;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardScore;
import net.minecraft.world.scores.ScoreboardTeam;

public class ScoreboardServer extends Scoreboard {

    public static final SavedDataType<PersistentScoreboard> TYPE = new SavedDataType<PersistentScoreboard>("scoreboard", (persistentbase_a) -> {
        return persistentbase_a.levelOrThrow().getScoreboard().createData();
    }, (persistentbase_a) -> {
        ScoreboardServer scoreboardserver = persistentbase_a.levelOrThrow().getScoreboard();
        Codec codec = PersistentScoreboard.a.CODEC;

        Objects.requireNonNull(scoreboardserver);
        return codec.xmap(scoreboardserver::createData, PersistentScoreboard::pack);
    }, DataFixTypes.SAVED_DATA_SCOREBOARD);
    private final MinecraftServer server;
    private final Set<ScoreboardObjective> trackedObjectives = Sets.newHashSet();
    private final List<Runnable> dirtyListeners = Lists.newArrayList();

    public ScoreboardServer(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
    }

    @Override
    protected void onScoreChanged(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective, ScoreboardScore scoreboardscore) {
        super.onScoreChanged(scoreholder, scoreboardobjective, scoreboardscore);
        if (this.trackedObjectives.contains(scoreboardobjective)) {
            this.server.getPlayerList().broadcastAll(new PacketPlayOutScoreboardScore(scoreholder.getScoreboardName(), scoreboardobjective.getName(), scoreboardscore.value(), Optional.ofNullable(scoreboardscore.display()), Optional.ofNullable(scoreboardscore.numberFormat())));
        }

        this.setDirty();
    }

    @Override
    protected void onScoreLockChanged(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) {
        super.onScoreLockChanged(scoreholder, scoreboardobjective);
        this.setDirty();
    }

    @Override
    public void onPlayerRemoved(ScoreHolder scoreholder) {
        super.onPlayerRemoved(scoreholder);
        this.server.getPlayerList().broadcastAll(new ClientboundResetScorePacket(scoreholder.getScoreboardName(), (String) null));
        this.setDirty();
    }

    @Override
    public void onPlayerScoreRemoved(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) {
        super.onPlayerScoreRemoved(scoreholder, scoreboardobjective);
        if (this.trackedObjectives.contains(scoreboardobjective)) {
            this.server.getPlayerList().broadcastAll(new ClientboundResetScorePacket(scoreholder.getScoreboardName(), scoreboardobjective.getName()));
        }

        this.setDirty();
    }

    @Override
    public void setDisplayObjective(DisplaySlot displayslot, @Nullable ScoreboardObjective scoreboardobjective) {
        ScoreboardObjective scoreboardobjective1 = this.getDisplayObjective(displayslot);

        super.setDisplayObjective(displayslot, scoreboardobjective);
        if (scoreboardobjective1 != scoreboardobjective && scoreboardobjective1 != null) {
            if (this.getObjectiveDisplaySlotCount(scoreboardobjective1) > 0) {
                this.server.getPlayerList().broadcastAll(new PacketPlayOutScoreboardDisplayObjective(displayslot, scoreboardobjective));
            } else {
                this.stopTrackingObjective(scoreboardobjective1);
            }
        }

        if (scoreboardobjective != null) {
            if (this.trackedObjectives.contains(scoreboardobjective)) {
                this.server.getPlayerList().broadcastAll(new PacketPlayOutScoreboardDisplayObjective(displayslot, scoreboardobjective));
            } else {
                this.startTrackingObjective(scoreboardobjective);
            }
        }

        this.setDirty();
    }

    @Override
    public boolean addPlayerToTeam(String s, ScoreboardTeam scoreboardteam) {
        if (super.addPlayerToTeam(s, scoreboardteam)) {
            this.server.getPlayerList().broadcastAll(PacketPlayOutScoreboardTeam.createPlayerPacket(scoreboardteam, s, PacketPlayOutScoreboardTeam.a.ADD));
            this.setDirty();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removePlayerFromTeam(String s, ScoreboardTeam scoreboardteam) {
        super.removePlayerFromTeam(s, scoreboardteam);
        this.server.getPlayerList().broadcastAll(PacketPlayOutScoreboardTeam.createPlayerPacket(scoreboardteam, s, PacketPlayOutScoreboardTeam.a.REMOVE));
        this.setDirty();
    }

    @Override
    public void onObjectiveAdded(ScoreboardObjective scoreboardobjective) {
        super.onObjectiveAdded(scoreboardobjective);
        this.setDirty();
    }

    @Override
    public void onObjectiveChanged(ScoreboardObjective scoreboardobjective) {
        super.onObjectiveChanged(scoreboardobjective);
        if (this.trackedObjectives.contains(scoreboardobjective)) {
            this.server.getPlayerList().broadcastAll(new PacketPlayOutScoreboardObjective(scoreboardobjective, 2));
        }

        this.setDirty();
    }

    @Override
    public void onObjectiveRemoved(ScoreboardObjective scoreboardobjective) {
        super.onObjectiveRemoved(scoreboardobjective);
        if (this.trackedObjectives.contains(scoreboardobjective)) {
            this.stopTrackingObjective(scoreboardobjective);
        }

        this.setDirty();
    }

    @Override
    public void onTeamAdded(ScoreboardTeam scoreboardteam) {
        super.onTeamAdded(scoreboardteam);
        this.server.getPlayerList().broadcastAll(PacketPlayOutScoreboardTeam.createAddOrModifyPacket(scoreboardteam, true));
        this.setDirty();
    }

    @Override
    public void onTeamChanged(ScoreboardTeam scoreboardteam) {
        super.onTeamChanged(scoreboardteam);
        this.server.getPlayerList().broadcastAll(PacketPlayOutScoreboardTeam.createAddOrModifyPacket(scoreboardteam, false));
        this.setDirty();
    }

    @Override
    public void onTeamRemoved(ScoreboardTeam scoreboardteam) {
        super.onTeamRemoved(scoreboardteam);
        this.server.getPlayerList().broadcastAll(PacketPlayOutScoreboardTeam.createRemovePacket(scoreboardteam));
        this.setDirty();
    }

    public void addDirtyListener(Runnable runnable) {
        this.dirtyListeners.add(runnable);
    }

    protected void setDirty() {
        for (Runnable runnable : this.dirtyListeners) {
            runnable.run();
        }

    }

    public List<Packet<?>> getStartTrackingPackets(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = Lists.newArrayList();

        list.add(new PacketPlayOutScoreboardObjective(scoreboardobjective, 0));

        for (DisplaySlot displayslot : DisplaySlot.values()) {
            if (this.getDisplayObjective(displayslot) == scoreboardobjective) {
                list.add(new PacketPlayOutScoreboardDisplayObjective(displayslot, scoreboardobjective));
            }
        }

        for (PlayerScoreEntry playerscoreentry : this.listPlayerScores(scoreboardobjective)) {
            list.add(new PacketPlayOutScoreboardScore(playerscoreentry.owner(), scoreboardobjective.getName(), playerscoreentry.value(), Optional.ofNullable(playerscoreentry.display()), Optional.ofNullable(playerscoreentry.numberFormatOverride())));
        }

        return list;
    }

    public void startTrackingObjective(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = this.getStartTrackingPackets(scoreboardobjective);

        for (EntityPlayer entityplayer : this.server.getPlayerList().getPlayers()) {
            for (Packet<?> packet : list) {
                entityplayer.connection.send(packet);
            }
        }

        this.trackedObjectives.add(scoreboardobjective);
    }

    public List<Packet<?>> getStopTrackingPackets(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = Lists.newArrayList();

        list.add(new PacketPlayOutScoreboardObjective(scoreboardobjective, 1));

        for (DisplaySlot displayslot : DisplaySlot.values()) {
            if (this.getDisplayObjective(displayslot) == scoreboardobjective) {
                list.add(new PacketPlayOutScoreboardDisplayObjective(displayslot, scoreboardobjective));
            }
        }

        return list;
    }

    public void stopTrackingObjective(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = this.getStopTrackingPackets(scoreboardobjective);

        for (EntityPlayer entityplayer : this.server.getPlayerList().getPlayers()) {
            for (Packet<?> packet : list) {
                entityplayer.connection.send(packet);
            }
        }

        this.trackedObjectives.remove(scoreboardobjective);
    }

    public int getObjectiveDisplaySlotCount(ScoreboardObjective scoreboardobjective) {
        int i = 0;

        for (DisplaySlot displayslot : DisplaySlot.values()) {
            if (this.getDisplayObjective(displayslot) == scoreboardobjective) {
                ++i;
            }
        }

        return i;
    }

    private PersistentScoreboard createData() {
        PersistentScoreboard persistentscoreboard = new PersistentScoreboard(this);

        Objects.requireNonNull(persistentscoreboard);
        this.addDirtyListener(persistentscoreboard::setDirty);
        return persistentscoreboard;
    }

    private PersistentScoreboard createData(PersistentScoreboard.a persistentscoreboard_a) {
        PersistentScoreboard persistentscoreboard = this.createData();

        persistentscoreboard.loadFrom(persistentscoreboard_a);
        return persistentscoreboard;
    }

    public static enum Action {

        CHANGE, REMOVE;

        private Action() {}
    }
}
