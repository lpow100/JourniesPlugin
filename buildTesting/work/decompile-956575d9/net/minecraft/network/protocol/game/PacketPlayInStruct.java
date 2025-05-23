package net.minecraft.network.protocol.game;

import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.block.state.properties.BlockPropertyStructureMode;

public class PacketPlayInStruct implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInStruct> STREAM_CODEC = Packet.<PacketDataSerializer, PacketPlayInStruct>codec(PacketPlayInStruct::write, PacketPlayInStruct::new);
    private static final int FLAG_IGNORE_ENTITIES = 1;
    private static final int FLAG_SHOW_AIR = 2;
    private static final int FLAG_SHOW_BOUNDING_BOX = 4;
    private static final int FLAG_STRICT = 8;
    private final BlockPosition pos;
    private final TileEntityStructure.UpdateType updateType;
    private final BlockPropertyStructureMode mode;
    private final String name;
    private final BlockPosition offset;
    private final BaseBlockPosition size;
    private final EnumBlockMirror mirror;
    private final EnumBlockRotation rotation;
    private final String data;
    private final boolean ignoreEntities;
    private final boolean strict;
    private final boolean showAir;
    private final boolean showBoundingBox;
    private final float integrity;
    private final long seed;

    public PacketPlayInStruct(BlockPosition blockposition, TileEntityStructure.UpdateType tileentitystructure_updatetype, BlockPropertyStructureMode blockpropertystructuremode, String s, BlockPosition blockposition1, BaseBlockPosition baseblockposition, EnumBlockMirror enumblockmirror, EnumBlockRotation enumblockrotation, String s1, boolean flag, boolean flag1, boolean flag2, boolean flag3, float f, long i) {
        this.pos = blockposition;
        this.updateType = tileentitystructure_updatetype;
        this.mode = blockpropertystructuremode;
        this.name = s;
        this.offset = blockposition1;
        this.size = baseblockposition;
        this.mirror = enumblockmirror;
        this.rotation = enumblockrotation;
        this.data = s1;
        this.ignoreEntities = flag;
        this.strict = flag1;
        this.showAir = flag2;
        this.showBoundingBox = flag3;
        this.integrity = f;
        this.seed = i;
    }

    private PacketPlayInStruct(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.updateType = (TileEntityStructure.UpdateType) packetdataserializer.readEnum(TileEntityStructure.UpdateType.class);
        this.mode = (BlockPropertyStructureMode) packetdataserializer.readEnum(BlockPropertyStructureMode.class);
        this.name = packetdataserializer.readUtf();
        int i = 48;

        this.offset = new BlockPosition(MathHelper.clamp(packetdataserializer.readByte(), -48, 48), MathHelper.clamp(packetdataserializer.readByte(), -48, 48), MathHelper.clamp(packetdataserializer.readByte(), -48, 48));
        int j = 48;

        this.size = new BaseBlockPosition(MathHelper.clamp(packetdataserializer.readByte(), 0, 48), MathHelper.clamp(packetdataserializer.readByte(), 0, 48), MathHelper.clamp(packetdataserializer.readByte(), 0, 48));
        this.mirror = (EnumBlockMirror) packetdataserializer.readEnum(EnumBlockMirror.class);
        this.rotation = (EnumBlockRotation) packetdataserializer.readEnum(EnumBlockRotation.class);
        this.data = packetdataserializer.readUtf(128);
        this.integrity = MathHelper.clamp(packetdataserializer.readFloat(), 0.0F, 1.0F);
        this.seed = packetdataserializer.readVarLong();
        int k = packetdataserializer.readByte();

        this.ignoreEntities = (k & 1) != 0;
        this.strict = (k & 8) != 0;
        this.showAir = (k & 2) != 0;
        this.showBoundingBox = (k & 4) != 0;
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeEnum(this.updateType);
        packetdataserializer.writeEnum(this.mode);
        packetdataserializer.writeUtf(this.name);
        packetdataserializer.writeByte(this.offset.getX());
        packetdataserializer.writeByte(this.offset.getY());
        packetdataserializer.writeByte(this.offset.getZ());
        packetdataserializer.writeByte(this.size.getX());
        packetdataserializer.writeByte(this.size.getY());
        packetdataserializer.writeByte(this.size.getZ());
        packetdataserializer.writeEnum(this.mirror);
        packetdataserializer.writeEnum(this.rotation);
        packetdataserializer.writeUtf(this.data);
        packetdataserializer.writeFloat(this.integrity);
        packetdataserializer.writeVarLong(this.seed);
        int i = 0;

        if (this.ignoreEntities) {
            i |= 1;
        }

        if (this.showAir) {
            i |= 2;
        }

        if (this.showBoundingBox) {
            i |= 4;
        }

        if (this.strict) {
            i |= 8;
        }

        packetdataserializer.writeByte(i);
    }

    @Override
    public PacketType<PacketPlayInStruct> type() {
        return GamePacketTypes.SERVERBOUND_SET_STRUCTURE_BLOCK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetStructureBlock(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public TileEntityStructure.UpdateType getUpdateType() {
        return this.updateType;
    }

    public BlockPropertyStructureMode getMode() {
        return this.mode;
    }

    public String getName() {
        return this.name;
    }

    public BlockPosition getOffset() {
        return this.offset;
    }

    public BaseBlockPosition getSize() {
        return this.size;
    }

    public EnumBlockMirror getMirror() {
        return this.mirror;
    }

    public EnumBlockRotation getRotation() {
        return this.rotation;
    }

    public String getData() {
        return this.data;
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public boolean isStrict() {
        return this.strict;
    }

    public boolean isShowAir() {
        return this.showAir;
    }

    public boolean isShowBoundingBox() {
        return this.showBoundingBox;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public long getSeed() {
        return this.seed;
    }
}
