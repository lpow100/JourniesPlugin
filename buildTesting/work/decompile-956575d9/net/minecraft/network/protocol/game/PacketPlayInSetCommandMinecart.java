package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.EntityMinecartCommandBlock;
import net.minecraft.world.level.CommandBlockListenerAbstract;
import net.minecraft.world.level.World;

public class PacketPlayInSetCommandMinecart implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInSetCommandMinecart> STREAM_CODEC = Packet.<PacketDataSerializer, PacketPlayInSetCommandMinecart>codec(PacketPlayInSetCommandMinecart::write, PacketPlayInSetCommandMinecart::new);
    private final int entity;
    private final String command;
    private final boolean trackOutput;

    public PacketPlayInSetCommandMinecart(int i, String s, boolean flag) {
        this.entity = i;
        this.command = s;
        this.trackOutput = flag;
    }

    private PacketPlayInSetCommandMinecart(PacketDataSerializer packetdataserializer) {
        this.entity = packetdataserializer.readVarInt();
        this.command = packetdataserializer.readUtf();
        this.trackOutput = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.entity);
        packetdataserializer.writeUtf(this.command);
        packetdataserializer.writeBoolean(this.trackOutput);
    }

    @Override
    public PacketType<PacketPlayInSetCommandMinecart> type() {
        return GamePacketTypes.SERVERBOUND_SET_COMMAND_MINECART;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetCommandMinecart(this);
    }

    @Nullable
    public CommandBlockListenerAbstract getCommandBlock(World world) {
        Entity entity = world.getEntity(this.entity);

        return entity instanceof EntityMinecartCommandBlock ? ((EntityMinecartCommandBlock) entity).getCommandBlock() : null;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }
}
