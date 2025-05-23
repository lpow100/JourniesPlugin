package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundTransferPacket(String host, int port) implements Packet<ClientCommonPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundTransferPacket> STREAM_CODEC = Packet.<PacketDataSerializer, ClientboundTransferPacket>codec(ClientboundTransferPacket::write, ClientboundTransferPacket::new);

    private ClientboundTransferPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(), packetdataserializer.readVarInt());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.host);
        packetdataserializer.writeVarInt(this.port);
    }

    @Override
    public PacketType<ClientboundTransferPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_TRANSFER;
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleTransfer(this);
    }
}
