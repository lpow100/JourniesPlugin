package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

public enum EnumDirection implements INamable {

    DOWN(0, 1, -1, "down", EnumDirection.EnumAxisDirection.NEGATIVE, EnumDirection.EnumAxis.Y, new BaseBlockPosition(0, -1, 0)), UP(1, 0, -1, "up", EnumDirection.EnumAxisDirection.POSITIVE, EnumDirection.EnumAxis.Y, new BaseBlockPosition(0, 1, 0)), NORTH(2, 3, 2, "north", EnumDirection.EnumAxisDirection.NEGATIVE, EnumDirection.EnumAxis.Z, new BaseBlockPosition(0, 0, -1)), SOUTH(3, 2, 0, "south", EnumDirection.EnumAxisDirection.POSITIVE, EnumDirection.EnumAxis.Z, new BaseBlockPosition(0, 0, 1)), WEST(4, 5, 1, "west", EnumDirection.EnumAxisDirection.NEGATIVE, EnumDirection.EnumAxis.X, new BaseBlockPosition(-1, 0, 0)), EAST(5, 4, 3, "east", EnumDirection.EnumAxisDirection.POSITIVE, EnumDirection.EnumAxis.X, new BaseBlockPosition(1, 0, 0));

    public static final INamable.a<EnumDirection> CODEC = INamable.<EnumDirection>fromEnum(EnumDirection::values);
    public static final Codec<EnumDirection> VERTICAL_CODEC = EnumDirection.CODEC.validate(EnumDirection::verifyVertical);
    public static final IntFunction<EnumDirection> BY_ID = ByIdMap.<EnumDirection>continuous(EnumDirection::get3DDataValue, values(), ByIdMap.a.WRAP);
    public static final StreamCodec<ByteBuf, EnumDirection> STREAM_CODEC = ByteBufCodecs.idMapper(EnumDirection.BY_ID, EnumDirection::get3DDataValue);
    /** @deprecated */
    @Deprecated
    public static final Codec<EnumDirection> LEGACY_ID_CODEC = Codec.BYTE.xmap(EnumDirection::from3DDataValue, (enumdirection) -> {
        return (byte) enumdirection.get3DDataValue();
    });
    /** @deprecated */
    @Deprecated
    public static final Codec<EnumDirection> LEGACY_ID_CODEC_2D = Codec.BYTE.xmap(EnumDirection::from2DDataValue, (enumdirection) -> {
        return (byte) enumdirection.get2DDataValue();
    });
    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final EnumDirection.EnumAxis axis;
    private final EnumDirection.EnumAxisDirection axisDirection;
    private final BaseBlockPosition normal;
    private final Vec3D normalVec3;
    private final Vector3fc normalVec3f;
    private static final EnumDirection[] VALUES = values();
    private static final EnumDirection[] BY_3D_DATA = (EnumDirection[]) Arrays.stream(EnumDirection.VALUES).sorted(Comparator.comparingInt((enumdirection) -> {
        return enumdirection.data3d;
    })).toArray((i) -> {
        return new EnumDirection[i];
    });
    private static final EnumDirection[] BY_2D_DATA = (EnumDirection[]) Arrays.stream(EnumDirection.VALUES).filter((enumdirection) -> {
        return enumdirection.getAxis().isHorizontal();
    }).sorted(Comparator.comparingInt((enumdirection) -> {
        return enumdirection.data2d;
    })).toArray((i) -> {
        return new EnumDirection[i];
    });

    private EnumDirection(final int i, final int j, final int k, final String s, final EnumDirection.EnumAxisDirection enumdirection_enumaxisdirection, final EnumDirection.EnumAxis enumdirection_enumaxis, final BaseBlockPosition baseblockposition) {
        this.data3d = i;
        this.data2d = k;
        this.oppositeIndex = j;
        this.name = s;
        this.axis = enumdirection_enumaxis;
        this.axisDirection = enumdirection_enumaxisdirection;
        this.normal = baseblockposition;
        this.normalVec3 = Vec3D.atLowerCornerOf(baseblockposition);
        this.normalVec3f = new org.joml.Vector3f((float) baseblockposition.getX(), (float) baseblockposition.getY(), (float) baseblockposition.getZ());
    }

    public static EnumDirection[] orderedByNearest(Entity entity) {
        float f = entity.getViewXRot(1.0F) * ((float) Math.PI / 180F);
        float f1 = -entity.getViewYRot(1.0F) * ((float) Math.PI / 180F);
        float f2 = MathHelper.sin(f);
        float f3 = MathHelper.cos(f);
        float f4 = MathHelper.sin(f1);
        float f5 = MathHelper.cos(f1);
        boolean flag = f4 > 0.0F;
        boolean flag1 = f2 < 0.0F;
        boolean flag2 = f5 > 0.0F;
        float f6 = flag ? f4 : -f4;
        float f7 = flag1 ? -f2 : f2;
        float f8 = flag2 ? f5 : -f5;
        float f9 = f6 * f3;
        float f10 = f8 * f3;
        EnumDirection enumdirection = flag ? EnumDirection.EAST : EnumDirection.WEST;
        EnumDirection enumdirection1 = flag1 ? EnumDirection.UP : EnumDirection.DOWN;
        EnumDirection enumdirection2 = flag2 ? EnumDirection.SOUTH : EnumDirection.NORTH;

        return f6 > f8 ? (f7 > f9 ? makeDirectionArray(enumdirection1, enumdirection, enumdirection2) : (f10 > f7 ? makeDirectionArray(enumdirection, enumdirection2, enumdirection1) : makeDirectionArray(enumdirection, enumdirection1, enumdirection2))) : (f7 > f10 ? makeDirectionArray(enumdirection1, enumdirection2, enumdirection) : (f9 > f7 ? makeDirectionArray(enumdirection2, enumdirection, enumdirection1) : makeDirectionArray(enumdirection2, enumdirection1, enumdirection)));
    }

    private static EnumDirection[] makeDirectionArray(EnumDirection enumdirection, EnumDirection enumdirection1, EnumDirection enumdirection2) {
        return new EnumDirection[]{enumdirection, enumdirection1, enumdirection2, enumdirection2.getOpposite(), enumdirection1.getOpposite(), enumdirection.getOpposite()};
    }

    public static EnumDirection rotate(Matrix4fc matrix4fc, EnumDirection enumdirection) {
        org.joml.Vector3f org_joml_vector3f = matrix4fc.transformDirection(enumdirection.normalVec3f, new org.joml.Vector3f());

        return getApproximateNearest(org_joml_vector3f.x(), org_joml_vector3f.y(), org_joml_vector3f.z());
    }

    public static Collection<EnumDirection> allShuffled(RandomSource randomsource) {
        return SystemUtils.shuffledCopy(values(), randomsource);
    }

    public static Stream<EnumDirection> stream() {
        return Stream.of(EnumDirection.VALUES);
    }

    public static float getYRot(EnumDirection enumdirection) {
        float f;

        switch (enumdirection.ordinal()) {
            case 2:
                f = 180.0F;
                break;
            case 3:
                f = 0.0F;
                break;
            case 4:
                f = 90.0F;
                break;
            case 5:
                f = -90.0F;
                break;
            default:
                throw new IllegalStateException("No y-Rot for vertical axis: " + String.valueOf(enumdirection));
        }

        return f;
    }

    public Quaternionf getRotation() {
        Quaternionf quaternionf;

        switch (this.ordinal()) {
            case 0:
                quaternionf = (new Quaternionf()).rotationX((float) Math.PI);
                break;
            case 1:
                quaternionf = new Quaternionf();
                break;
            case 2:
                quaternionf = (new Quaternionf()).rotationXYZ(((float) Math.PI / 2F), 0.0F, (float) Math.PI);
                break;
            case 3:
                quaternionf = (new Quaternionf()).rotationX(((float) Math.PI / 2F));
                break;
            case 4:
                quaternionf = (new Quaternionf()).rotationXYZ(((float) Math.PI / 2F), 0.0F, ((float) Math.PI / 2F));
                break;
            case 5:
                quaternionf = (new Quaternionf()).rotationXYZ(((float) Math.PI / 2F), 0.0F, (-(float) Math.PI / 2F));
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return quaternionf;
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public EnumDirection.EnumAxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public static EnumDirection getFacingAxis(Entity entity, EnumDirection.EnumAxis enumdirection_enumaxis) {
        EnumDirection enumdirection;

        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                enumdirection = EnumDirection.EAST.isFacingAngle(entity.getViewYRot(1.0F)) ? EnumDirection.EAST : EnumDirection.WEST;
                break;
            case 1:
                enumdirection = entity.getViewXRot(1.0F) < 0.0F ? EnumDirection.UP : EnumDirection.DOWN;
                break;
            case 2:
                enumdirection = EnumDirection.SOUTH.isFacingAngle(entity.getViewYRot(1.0F)) ? EnumDirection.SOUTH : EnumDirection.NORTH;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return enumdirection;
    }

    public EnumDirection getOpposite() {
        return from3DDataValue(this.oppositeIndex);
    }

    public EnumDirection getClockWise(EnumDirection.EnumAxis enumdirection_enumaxis) {
        EnumDirection enumdirection;

        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                enumdirection = this != EnumDirection.WEST && this != EnumDirection.EAST ? this.getClockWiseX() : this;
                break;
            case 1:
                enumdirection = this != EnumDirection.UP && this != EnumDirection.DOWN ? this.getClockWise() : this;
                break;
            case 2:
                enumdirection = this != EnumDirection.NORTH && this != EnumDirection.SOUTH ? this.getClockWiseZ() : this;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return enumdirection;
    }

    public EnumDirection getCounterClockWise(EnumDirection.EnumAxis enumdirection_enumaxis) {
        EnumDirection enumdirection;

        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                enumdirection = this != EnumDirection.WEST && this != EnumDirection.EAST ? this.getCounterClockWiseX() : this;
                break;
            case 1:
                enumdirection = this != EnumDirection.UP && this != EnumDirection.DOWN ? this.getCounterClockWise() : this;
                break;
            case 2:
                enumdirection = this != EnumDirection.NORTH && this != EnumDirection.SOUTH ? this.getCounterClockWiseZ() : this;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return enumdirection;
    }

    public EnumDirection getClockWise() {
        EnumDirection enumdirection;

        switch (this.ordinal()) {
            case 2:
                enumdirection = EnumDirection.EAST;
                break;
            case 3:
                enumdirection = EnumDirection.WEST;
                break;
            case 4:
                enumdirection = EnumDirection.NORTH;
                break;
            case 5:
                enumdirection = EnumDirection.SOUTH;
                break;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + String.valueOf(this));
        }

        return enumdirection;
    }

    private EnumDirection getClockWiseX() {
        EnumDirection enumdirection;

        switch (this.ordinal()) {
            case 0:
                enumdirection = EnumDirection.SOUTH;
                break;
            case 1:
                enumdirection = EnumDirection.NORTH;
                break;
            case 2:
                enumdirection = EnumDirection.DOWN;
                break;
            case 3:
                enumdirection = EnumDirection.UP;
                break;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        }

        return enumdirection;
    }

    private EnumDirection getCounterClockWiseX() {
        EnumDirection enumdirection;

        switch (this.ordinal()) {
            case 0:
                enumdirection = EnumDirection.NORTH;
                break;
            case 1:
                enumdirection = EnumDirection.SOUTH;
                break;
            case 2:
                enumdirection = EnumDirection.UP;
                break;
            case 3:
                enumdirection = EnumDirection.DOWN;
                break;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        }

        return enumdirection;
    }

    private EnumDirection getClockWiseZ() {
        EnumDirection enumdirection;

        switch (this.ordinal()) {
            case 0:
                enumdirection = EnumDirection.WEST;
                break;
            case 1:
                enumdirection = EnumDirection.EAST;
                break;
            case 2:
            case 3:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
            case 4:
                enumdirection = EnumDirection.UP;
                break;
            case 5:
                enumdirection = EnumDirection.DOWN;
        }

        return enumdirection;
    }

    private EnumDirection getCounterClockWiseZ() {
        EnumDirection enumdirection;

        switch (this.ordinal()) {
            case 0:
                enumdirection = EnumDirection.EAST;
                break;
            case 1:
                enumdirection = EnumDirection.WEST;
                break;
            case 2:
            case 3:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
            case 4:
                enumdirection = EnumDirection.DOWN;
                break;
            case 5:
                enumdirection = EnumDirection.UP;
        }

        return enumdirection;
    }

    public EnumDirection getCounterClockWise() {
        EnumDirection enumdirection;

        switch (this.ordinal()) {
            case 2:
                enumdirection = EnumDirection.WEST;
                break;
            case 3:
                enumdirection = EnumDirection.EAST;
                break;
            case 4:
                enumdirection = EnumDirection.SOUTH;
                break;
            case 5:
                enumdirection = EnumDirection.NORTH;
                break;
            default:
                throw new IllegalStateException("Unable to get CCW facing of " + String.valueOf(this));
        }

        return enumdirection;
    }

    public int getStepX() {
        return this.normal.getX();
    }

    public int getStepY() {
        return this.normal.getY();
    }

    public int getStepZ() {
        return this.normal.getZ();
    }

    public org.joml.Vector3f step() {
        return new org.joml.Vector3f(this.normalVec3f);
    }

    public String getName() {
        return this.name;
    }

    public EnumDirection.EnumAxis getAxis() {
        return this.axis;
    }

    @Nullable
    public static EnumDirection byName(@Nullable String s) {
        return EnumDirection.CODEC.byName(s);
    }

    public static EnumDirection from3DDataValue(int i) {
        return EnumDirection.BY_3D_DATA[MathHelper.abs(i % EnumDirection.BY_3D_DATA.length)];
    }

    public static EnumDirection from2DDataValue(int i) {
        return EnumDirection.BY_2D_DATA[MathHelper.abs(i % EnumDirection.BY_2D_DATA.length)];
    }

    public static EnumDirection fromYRot(double d0) {
        return from2DDataValue(MathHelper.floor(d0 / 90.0D + 0.5D) & 3);
    }

    public static EnumDirection fromAxisAndDirection(EnumDirection.EnumAxis enumdirection_enumaxis, EnumDirection.EnumAxisDirection enumdirection_enumaxisdirection) {
        EnumDirection enumdirection;

        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                enumdirection = enumdirection_enumaxisdirection == EnumDirection.EnumAxisDirection.POSITIVE ? EnumDirection.EAST : EnumDirection.WEST;
                break;
            case 1:
                enumdirection = enumdirection_enumaxisdirection == EnumDirection.EnumAxisDirection.POSITIVE ? EnumDirection.UP : EnumDirection.DOWN;
                break;
            case 2:
                enumdirection = enumdirection_enumaxisdirection == EnumDirection.EnumAxisDirection.POSITIVE ? EnumDirection.SOUTH : EnumDirection.NORTH;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return enumdirection;
    }

    public float toYRot() {
        return (float) ((this.data2d & 3) * 90);
    }

    public static EnumDirection getRandom(RandomSource randomsource) {
        return (EnumDirection) SystemUtils.getRandom(EnumDirection.VALUES, randomsource);
    }

    public static EnumDirection getApproximateNearest(double d0, double d1, double d2) {
        return getApproximateNearest((float) d0, (float) d1, (float) d2);
    }

    public static EnumDirection getApproximateNearest(float f, float f1, float f2) {
        EnumDirection enumdirection = EnumDirection.NORTH;
        float f3 = Float.MIN_VALUE;

        for (EnumDirection enumdirection1 : EnumDirection.VALUES) {
            float f4 = f * (float) enumdirection1.normal.getX() + f1 * (float) enumdirection1.normal.getY() + f2 * (float) enumdirection1.normal.getZ();

            if (f4 > f3) {
                f3 = f4;
                enumdirection = enumdirection1;
            }
        }

        return enumdirection;
    }

    public static EnumDirection getApproximateNearest(Vec3D vec3d) {
        return getApproximateNearest(vec3d.x, vec3d.y, vec3d.z);
    }

    @Nullable
    @Contract("_,_,_,!null->!null;_,_,_,_->_")
    public static EnumDirection getNearest(int i, int j, int k, @Nullable EnumDirection enumdirection) {
        int l = Math.abs(i);
        int i1 = Math.abs(j);
        int j1 = Math.abs(k);

        return l > j1 && l > i1 ? (i < 0 ? EnumDirection.WEST : EnumDirection.EAST) : (j1 > l && j1 > i1 ? (k < 0 ? EnumDirection.NORTH : EnumDirection.SOUTH) : (i1 > l && i1 > j1 ? (j < 0 ? EnumDirection.DOWN : EnumDirection.UP) : enumdirection));
    }

    @Nullable
    @Contract("_,!null->!null;_,_->_")
    public static EnumDirection getNearest(BaseBlockPosition baseblockposition, @Nullable EnumDirection enumdirection) {
        return getNearest(baseblockposition.getX(), baseblockposition.getY(), baseblockposition.getZ(), enumdirection);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private static DataResult<EnumDirection> verifyVertical(EnumDirection enumdirection) {
        return enumdirection.getAxis().isVertical() ? DataResult.success(enumdirection) : DataResult.error(() -> {
            return "Expected a vertical direction";
        });
    }

    public static EnumDirection get(EnumDirection.EnumAxisDirection enumdirection_enumaxisdirection, EnumDirection.EnumAxis enumdirection_enumaxis) {
        for (EnumDirection enumdirection : EnumDirection.VALUES) {
            if (enumdirection.getAxisDirection() == enumdirection_enumaxisdirection && enumdirection.getAxis() == enumdirection_enumaxis) {
                return enumdirection;
            }
        }

        String s = String.valueOf(enumdirection_enumaxisdirection);

        throw new IllegalArgumentException("No such direction: " + s + " " + String.valueOf(enumdirection_enumaxis));
    }

    public BaseBlockPosition getUnitVec3i() {
        return this.normal;
    }

    public Vec3D getUnitVec3() {
        return this.normalVec3;
    }

    public Vector3fc getUnitVec3f() {
        return this.normalVec3f;
    }

    public boolean isFacingAngle(float f) {
        float f1 = f * ((float) Math.PI / 180F);
        float f2 = -MathHelper.sin(f1);
        float f3 = MathHelper.cos(f1);

        return (float) this.normal.getX() * f2 + (float) this.normal.getZ() * f3 > 0.0F;
    }

    public static enum EnumAxis implements INamable, Predicate<EnumDirection> {

        X("x") {
            @Override
            public int choose(int i, int j, int k) {
                return i;
            }

            @Override
            public boolean choose(boolean flag, boolean flag1, boolean flag2) {
                return flag;
            }

            @Override
            public double choose(double d0, double d1, double d2) {
                return d0;
            }

            @Override
            public EnumDirection getPositive() {
                return EnumDirection.EAST;
            }

            @Override
            public EnumDirection getNegative() {
                return EnumDirection.WEST;
            }
        },
        Y("y") {
            @Override
            public int choose(int i, int j, int k) {
                return j;
            }

            @Override
            public double choose(double d0, double d1, double d2) {
                return d1;
            }

            @Override
            public boolean choose(boolean flag, boolean flag1, boolean flag2) {
                return flag1;
            }

            @Override
            public EnumDirection getPositive() {
                return EnumDirection.UP;
            }

            @Override
            public EnumDirection getNegative() {
                return EnumDirection.DOWN;
            }
        },
        Z("z") {
            @Override
            public int choose(int i, int j, int k) {
                return k;
            }

            @Override
            public double choose(double d0, double d1, double d2) {
                return d2;
            }

            @Override
            public boolean choose(boolean flag, boolean flag1, boolean flag2) {
                return flag2;
            }

            @Override
            public EnumDirection getPositive() {
                return EnumDirection.SOUTH;
            }

            @Override
            public EnumDirection getNegative() {
                return EnumDirection.NORTH;
            }
        };

        public static final EnumDirection.EnumAxis[] VALUES = values();
        public static final INamable.a<EnumDirection.EnumAxis> CODEC = INamable.<EnumDirection.EnumAxis>fromEnum(EnumDirection.EnumAxis::values);
        private final String name;

        EnumAxis(final String s) {
            this.name = s;
        }

        @Nullable
        public static EnumDirection.EnumAxis byName(String s) {
            return EnumDirection.EnumAxis.CODEC.byName(s);
        }

        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == EnumDirection.EnumAxis.Y;
        }

        public boolean isHorizontal() {
            return this == EnumDirection.EnumAxis.X || this == EnumDirection.EnumAxis.Z;
        }

        public abstract EnumDirection getPositive();

        public abstract EnumDirection getNegative();

        public EnumDirection[] getDirections() {
            return new EnumDirection[]{this.getPositive(), this.getNegative()};
        }

        public String toString() {
            return this.name;
        }

        public static EnumDirection.EnumAxis getRandom(RandomSource randomsource) {
            return (EnumDirection.EnumAxis) SystemUtils.getRandom(EnumDirection.EnumAxis.VALUES, randomsource);
        }

        public boolean test(@Nullable EnumDirection enumdirection) {
            return enumdirection != null && enumdirection.getAxis() == this;
        }

        public EnumDirection.EnumDirectionLimit getPlane() {
            EnumDirection.EnumDirectionLimit enumdirection_enumdirectionlimit;

            switch (this.ordinal()) {
                case 0:
                case 2:
                    enumdirection_enumdirectionlimit = EnumDirection.EnumDirectionLimit.HORIZONTAL;
                    break;
                case 1:
                    enumdirection_enumdirectionlimit = EnumDirection.EnumDirectionLimit.VERTICAL;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return enumdirection_enumdirectionlimit;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract int choose(int i, int j, int k);

        public abstract double choose(double d0, double d1, double d2);

        public abstract boolean choose(boolean flag, boolean flag1, boolean flag2);
    }

    public static enum EnumAxisDirection {

        POSITIVE(1, "Towards positive"), NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private EnumAxisDirection(final int i, final String s) {
            this.step = i;
            this.name = s;
        }

        public int getStep() {
            return this.step;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        public EnumDirection.EnumAxisDirection opposite() {
            return this == EnumDirection.EnumAxisDirection.POSITIVE ? EnumDirection.EnumAxisDirection.NEGATIVE : EnumDirection.EnumAxisDirection.POSITIVE;
        }
    }

    public static enum EnumDirectionLimit implements Iterable<EnumDirection>, Predicate<EnumDirection> {

        HORIZONTAL(new EnumDirection[]{EnumDirection.NORTH, EnumDirection.EAST, EnumDirection.SOUTH, EnumDirection.WEST}, new EnumDirection.EnumAxis[]{EnumDirection.EnumAxis.X, EnumDirection.EnumAxis.Z}), VERTICAL(new EnumDirection[]{EnumDirection.UP, EnumDirection.DOWN}, new EnumDirection.EnumAxis[]{EnumDirection.EnumAxis.Y});

        private final EnumDirection[] faces;
        private final EnumDirection.EnumAxis[] axis;

        private EnumDirectionLimit(final EnumDirection[] aenumdirection, final EnumDirection.EnumAxis[] aenumdirection_enumaxis) {
            this.faces = aenumdirection;
            this.axis = aenumdirection_enumaxis;
        }

        public EnumDirection getRandomDirection(RandomSource randomsource) {
            return (EnumDirection) SystemUtils.getRandom(this.faces, randomsource);
        }

        public EnumDirection.EnumAxis getRandomAxis(RandomSource randomsource) {
            return (EnumDirection.EnumAxis) SystemUtils.getRandom(this.axis, randomsource);
        }

        public boolean test(@Nullable EnumDirection enumdirection) {
            return enumdirection != null && enumdirection.getAxis().getPlane() == this;
        }

        public Iterator<EnumDirection> iterator() {
            return Iterators.forArray(this.faces);
        }

        public Stream<EnumDirection> stream() {
            return Arrays.stream(this.faces);
        }

        public List<EnumDirection> shuffledCopy(RandomSource randomsource) {
            return SystemUtils.shuffledCopy(this.faces, randomsource);
        }

        public int length() {
            return this.faces.length;
        }
    }
}
