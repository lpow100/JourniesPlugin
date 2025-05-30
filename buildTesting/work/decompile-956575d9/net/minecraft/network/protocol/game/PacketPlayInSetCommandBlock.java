package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.entity.TileEntityCommand;

public class PacketPlayInSetCommandBlock implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInSetCommandBlock> STREAM_CODEC = Packet.<PacketDataSerializer, PacketPlayInSetCommandBlock>codec(PacketPlayInSetCommandBlock::write, PacketPlayInSetCommandBlock::new);
    private static final int FLAG_TRACK_OUTPUT = 1;
    private static final int FLAG_CONDITIONAL = 2;
    private static final int FLAG_AUTOMATIC = 4;
    private final BlockPosition pos;
    private final String command;
    private final boolean trackOutput;
    private final boolean conditional;
    private final boolean automatic;
    private final TileEntityCommand.Type mode;

    public PacketPlayInSetCommandBlock(BlockPosition blockposition, String s, TileEntityCommand.Type tileentitycommand_type, boolean flag, boolean flag1, boolean flag2) {
        this.pos = blockposition;
        this.command = s;
        this.trackOutput = flag;
        this.conditional = flag1;
        this.automatic = flag2;
        this.mode = tileentitycommand_type;
    }

    private PacketPlayInSetCommandBlock(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.command = packetdataserializer.readUtf();
        this.mode = (TileEntityCommand.Type) packetdataserializer.readEnum(TileEntityCommand.Type.class);
        int i = packetdataserializer.readByte();

        this.trackOutput = (i & 1) != 0;
        this.conditional = (i & 2) != 0;
        this.automatic = (i & 4) != 0;
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeUtf(this.command);
        packetdataserializer.writeEnum(this.mode);
        int i = 0;

        if (this.trackOutput) {
            i |= 1;
        }

        if (this.conditional) {
            i |= 2;
        }

        if (this.automatic) {
            i |= 4;
        }

        packetdataserializer.writeByte(i);
    }

    @Override
    public PacketType<PacketPlayInSetCommandBlock> type() {
        return GamePacketTypes.SERVERBOUND_SET_COMMAND_BLOCK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetCommandBlock(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public boolean isConditional() {
        return this.conditional;
    }

    public boolean isAutomatic() {
        return this.automatic;
    }

    public TileEntityCommand.Type getMode() {
        return this.mode;
    }
}
