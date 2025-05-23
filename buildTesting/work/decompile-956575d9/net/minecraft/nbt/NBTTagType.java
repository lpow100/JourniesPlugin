package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface NBTTagType<T extends NBTBase> {

    T load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException;

    StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException;

    default void parseRoot(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
        switch (streamtagvisitor.visitRootEntry(this)) {
            case CONTINUE:
                this.parse(datainput, streamtagvisitor, nbtreadlimiter);
            case HALT:
            default:
                break;
            case BREAK:
                this.skip(datainput, nbtreadlimiter);
        }

    }

    void skip(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException;

    void skip(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException;

    String getName();

    String getPrettyName();

    static NBTTagType<NBTTagEnd> createInvalid(final int i) {
        return new NBTTagType<NBTTagEnd>() {
            private IOException createException() {
                return new IOException("Invalid tag id: " + i);
            }

            @Override
            public NBTTagEnd load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
                throw this.createException();
            }

            @Override
            public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput datainput, int j, NBTReadLimiter nbtreadlimiter) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
                throw this.createException();
            }

            @Override
            public String getName() {
                return "INVALID[" + i + "]";
            }

            @Override
            public String getPrettyName() {
                return "UNKNOWN_" + i;
            }
        };
    }

    public interface a<T extends NBTBase> extends NBTTagType<T> {

        @Override
        default void skip(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            datainput.skipBytes(this.size());
        }

        @Override
        default void skip(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
            datainput.skipBytes(this.size() * i);
        }

        int size();
    }

    public interface b<T extends NBTBase> extends NBTTagType<T> {

        @Override
        default void skip(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
            for (int j = 0; j < i; ++j) {
                this.skip(datainput, nbtreadlimiter);
            }

        }
    }
}
