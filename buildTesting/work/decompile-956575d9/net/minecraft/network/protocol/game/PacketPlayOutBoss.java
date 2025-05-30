package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.BossBattle;

public class PacketPlayOutBoss implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutBoss> STREAM_CODEC = Packet.<RegistryFriendlyByteBuf, PacketPlayOutBoss>codec(PacketPlayOutBoss::write, PacketPlayOutBoss::new);
    private static final int FLAG_DARKEN = 1;
    private static final int FLAG_MUSIC = 2;
    private static final int FLAG_FOG = 4;
    private final UUID id;
    private final PacketPlayOutBoss.Action operation;
    static final PacketPlayOutBoss.Action REMOVE_OPERATION = new PacketPlayOutBoss.Action() {
        @Override
        public PacketPlayOutBoss.d getType() {
            return PacketPlayOutBoss.d.REMOVE;
        }

        @Override
        public void dispatch(UUID uuid, PacketPlayOutBoss.b packetplayoutboss_b) {
            packetplayoutboss_b.remove(uuid);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {}
    };

    private PacketPlayOutBoss(UUID uuid, PacketPlayOutBoss.Action packetplayoutboss_action) {
        this.id = uuid;
        this.operation = packetplayoutboss_action;
    }

    private PacketPlayOutBoss(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.id = registryfriendlybytebuf.readUUID();
        PacketPlayOutBoss.d packetplayoutboss_d = (PacketPlayOutBoss.d) registryfriendlybytebuf.readEnum(PacketPlayOutBoss.d.class);

        this.operation = packetplayoutboss_d.reader.decode(registryfriendlybytebuf);
    }

    public static PacketPlayOutBoss createAddPacket(BossBattle bossbattle) {
        return new PacketPlayOutBoss(bossbattle.getId(), new PacketPlayOutBoss.a(bossbattle));
    }

    public static PacketPlayOutBoss createRemovePacket(UUID uuid) {
        return new PacketPlayOutBoss(uuid, PacketPlayOutBoss.REMOVE_OPERATION);
    }

    public static PacketPlayOutBoss createUpdateProgressPacket(BossBattle bossbattle) {
        return new PacketPlayOutBoss(bossbattle.getId(), new PacketPlayOutBoss.f(bossbattle.getProgress()));
    }

    public static PacketPlayOutBoss createUpdateNamePacket(BossBattle bossbattle) {
        return new PacketPlayOutBoss(bossbattle.getId(), new PacketPlayOutBoss.e(bossbattle.getName()));
    }

    public static PacketPlayOutBoss createUpdateStylePacket(BossBattle bossbattle) {
        return new PacketPlayOutBoss(bossbattle.getId(), new PacketPlayOutBoss.h(bossbattle.getColor(), bossbattle.getOverlay()));
    }

    public static PacketPlayOutBoss createUpdatePropertiesPacket(BossBattle bossbattle) {
        return new PacketPlayOutBoss(bossbattle.getId(), new PacketPlayOutBoss.g(bossbattle.shouldDarkenScreen(), bossbattle.shouldPlayBossMusic(), bossbattle.shouldCreateWorldFog()));
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeUUID(this.id);
        registryfriendlybytebuf.writeEnum(this.operation.getType());
        this.operation.write(registryfriendlybytebuf);
    }

    static int encodeProperties(boolean flag, boolean flag1, boolean flag2) {
        int i = 0;

        if (flag) {
            i |= 1;
        }

        if (flag1) {
            i |= 2;
        }

        if (flag2) {
            i |= 4;
        }

        return i;
    }

    @Override
    public PacketType<PacketPlayOutBoss> type() {
        return GamePacketTypes.CLIENTBOUND_BOSS_EVENT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleBossUpdate(this);
    }

    public void dispatch(PacketPlayOutBoss.b packetplayoutboss_b) {
        this.operation.dispatch(this.id, packetplayoutboss_b);
    }

    private static enum d {

        ADD(PacketPlayOutBoss.a::new), REMOVE((registryfriendlybytebuf) -> {
            return PacketPlayOutBoss.REMOVE_OPERATION;
        }), UPDATE_PROGRESS(PacketPlayOutBoss.f::new), UPDATE_NAME(PacketPlayOutBoss.e::new), UPDATE_STYLE(PacketPlayOutBoss.h::new), UPDATE_PROPERTIES(PacketPlayOutBoss.g::new);

        final StreamDecoder<RegistryFriendlyByteBuf, PacketPlayOutBoss.Action> reader;

        private d(final StreamDecoder streamdecoder) {
            this.reader = streamdecoder;
        }
    }

    public interface b {

        default void add(UUID uuid, IChatBaseComponent ichatbasecomponent, float f, BossBattle.BarColor bossbattle_barcolor, BossBattle.BarStyle bossbattle_barstyle, boolean flag, boolean flag1, boolean flag2) {}

        default void remove(UUID uuid) {}

        default void updateProgress(UUID uuid, float f) {}

        default void updateName(UUID uuid, IChatBaseComponent ichatbasecomponent) {}

        default void updateStyle(UUID uuid, BossBattle.BarColor bossbattle_barcolor, BossBattle.BarStyle bossbattle_barstyle) {}

        default void updateProperties(UUID uuid, boolean flag, boolean flag1, boolean flag2) {}
    }

    private static class a implements PacketPlayOutBoss.Action {

        private final IChatBaseComponent name;
        private final float progress;
        private final BossBattle.BarColor color;
        private final BossBattle.BarStyle overlay;
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        a(BossBattle bossbattle) {
            this.name = bossbattle.getName();
            this.progress = bossbattle.getProgress();
            this.color = bossbattle.getColor();
            this.overlay = bossbattle.getOverlay();
            this.darkenScreen = bossbattle.shouldDarkenScreen();
            this.playMusic = bossbattle.shouldPlayBossMusic();
            this.createWorldFog = bossbattle.shouldCreateWorldFog();
        }

        private a(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            this.name = (IChatBaseComponent) ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryfriendlybytebuf);
            this.progress = registryfriendlybytebuf.readFloat();
            this.color = (BossBattle.BarColor) registryfriendlybytebuf.readEnum(BossBattle.BarColor.class);
            this.overlay = (BossBattle.BarStyle) registryfriendlybytebuf.readEnum(BossBattle.BarStyle.class);
            int i = registryfriendlybytebuf.readUnsignedByte();

            this.darkenScreen = (i & 1) > 0;
            this.playMusic = (i & 2) > 0;
            this.createWorldFog = (i & 4) > 0;
        }

        @Override
        public PacketPlayOutBoss.d getType() {
            return PacketPlayOutBoss.d.ADD;
        }

        @Override
        public void dispatch(UUID uuid, PacketPlayOutBoss.b packetplayoutboss_b) {
            packetplayoutboss_b.add(uuid, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryfriendlybytebuf, this.name);
            registryfriendlybytebuf.writeFloat(this.progress);
            registryfriendlybytebuf.writeEnum(this.color);
            registryfriendlybytebuf.writeEnum(this.overlay);
            registryfriendlybytebuf.writeByte(PacketPlayOutBoss.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    private static record f(float progress) implements PacketPlayOutBoss.Action {

        private f(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            this(registryfriendlybytebuf.readFloat());
        }

        @Override
        public PacketPlayOutBoss.d getType() {
            return PacketPlayOutBoss.d.UPDATE_PROGRESS;
        }

        @Override
        public void dispatch(UUID uuid, PacketPlayOutBoss.b packetplayoutboss_b) {
            packetplayoutboss_b.updateProgress(uuid, this.progress);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            registryfriendlybytebuf.writeFloat(this.progress);
        }
    }

    private static record e(IChatBaseComponent name) implements PacketPlayOutBoss.Action {

        private e(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            this((IChatBaseComponent) ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryfriendlybytebuf));
        }

        @Override
        public PacketPlayOutBoss.d getType() {
            return PacketPlayOutBoss.d.UPDATE_NAME;
        }

        @Override
        public void dispatch(UUID uuid, PacketPlayOutBoss.b packetplayoutboss_b) {
            packetplayoutboss_b.updateName(uuid, this.name);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryfriendlybytebuf, this.name);
        }
    }

    private static class h implements PacketPlayOutBoss.Action {

        private final BossBattle.BarColor color;
        private final BossBattle.BarStyle overlay;

        h(BossBattle.BarColor bossbattle_barcolor, BossBattle.BarStyle bossbattle_barstyle) {
            this.color = bossbattle_barcolor;
            this.overlay = bossbattle_barstyle;
        }

        private h(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            this.color = (BossBattle.BarColor) registryfriendlybytebuf.readEnum(BossBattle.BarColor.class);
            this.overlay = (BossBattle.BarStyle) registryfriendlybytebuf.readEnum(BossBattle.BarStyle.class);
        }

        @Override
        public PacketPlayOutBoss.d getType() {
            return PacketPlayOutBoss.d.UPDATE_STYLE;
        }

        @Override
        public void dispatch(UUID uuid, PacketPlayOutBoss.b packetplayoutboss_b) {
            packetplayoutboss_b.updateStyle(uuid, this.color, this.overlay);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            registryfriendlybytebuf.writeEnum(this.color);
            registryfriendlybytebuf.writeEnum(this.overlay);
        }
    }

    private static class g implements PacketPlayOutBoss.Action {

        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        g(boolean flag, boolean flag1, boolean flag2) {
            this.darkenScreen = flag;
            this.playMusic = flag1;
            this.createWorldFog = flag2;
        }

        private g(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            int i = registryfriendlybytebuf.readUnsignedByte();

            this.darkenScreen = (i & 1) > 0;
            this.playMusic = (i & 2) > 0;
            this.createWorldFog = (i & 4) > 0;
        }

        @Override
        public PacketPlayOutBoss.d getType() {
            return PacketPlayOutBoss.d.UPDATE_PROPERTIES;
        }

        @Override
        public void dispatch(UUID uuid, PacketPlayOutBoss.b packetplayoutboss_b) {
            packetplayoutboss_b.updateProperties(uuid, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            registryfriendlybytebuf.writeByte(PacketPlayOutBoss.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    private interface Action {

        PacketPlayOutBoss.d getType();

        void dispatch(UUID uuid, PacketPlayOutBoss.b packetplayoutboss_b);

        void write(RegistryFriendlyByteBuf registryfriendlybytebuf);
    }
}
