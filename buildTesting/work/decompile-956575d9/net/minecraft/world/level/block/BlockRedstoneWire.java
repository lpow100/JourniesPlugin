package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamRedstone;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ARGB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyRedstoneSide;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.redstone.DefaultRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.ExperimentalRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockRedstoneWire extends Block {

    public static final MapCodec<BlockRedstoneWire> CODEC = simpleCodec(BlockRedstoneWire::new);
    public static final BlockStateEnum<BlockPropertyRedstoneSide> NORTH = BlockProperties.NORTH_REDSTONE;
    public static final BlockStateEnum<BlockPropertyRedstoneSide> EAST = BlockProperties.EAST_REDSTONE;
    public static final BlockStateEnum<BlockPropertyRedstoneSide> SOUTH = BlockProperties.SOUTH_REDSTONE;
    public static final BlockStateEnum<BlockPropertyRedstoneSide> WEST = BlockProperties.WEST_REDSTONE;
    public static final BlockStateInteger POWER = BlockProperties.POWER;
    public static final Map<EnumDirection, BlockStateEnum<BlockPropertyRedstoneSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(EnumDirection.NORTH, BlockRedstoneWire.NORTH, EnumDirection.EAST, BlockRedstoneWire.EAST, EnumDirection.SOUTH, BlockRedstoneWire.SOUTH, EnumDirection.WEST, BlockRedstoneWire.WEST)));
    private static final int[] COLORS = (int[]) SystemUtils.make(new int[16], (aint) -> {
        for (int i = 0; i <= 15; ++i) {
            float f = (float) i / 15.0F;
            float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float f3 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);

            aint[i] = ARGB.colorFromFloat(1.0F, f1, f2, f3);
        }

    });
    private static final float PARTICLE_DENSITY = 0.2F;
    private final Function<IBlockData, VoxelShape> shapes;
    private final IBlockData crossState;
    private final RedstoneWireEvaluator evaluator = new DefaultRedstoneWireEvaluator(this);
    private boolean shouldSignal = true;

    @Override
    public MapCodec<BlockRedstoneWire> codec() {
        return BlockRedstoneWire.CODEC;
    }

    public BlockRedstoneWire(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockRedstoneWire.NORTH, BlockPropertyRedstoneSide.NONE)).setValue(BlockRedstoneWire.EAST, BlockPropertyRedstoneSide.NONE)).setValue(BlockRedstoneWire.SOUTH, BlockPropertyRedstoneSide.NONE)).setValue(BlockRedstoneWire.WEST, BlockPropertyRedstoneSide.NONE)).setValue(BlockRedstoneWire.POWER, 0));
        this.shapes = this.makeShapes();
        this.crossState = (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockRedstoneWire.NORTH, BlockPropertyRedstoneSide.SIDE)).setValue(BlockRedstoneWire.EAST, BlockPropertyRedstoneSide.SIDE)).setValue(BlockRedstoneWire.SOUTH, BlockPropertyRedstoneSide.SIDE)).setValue(BlockRedstoneWire.WEST, BlockPropertyRedstoneSide.SIDE);
    }

    private Function<IBlockData, VoxelShape> makeShapes() {
        int i = 1;
        int j = 10;
        VoxelShape voxelshape = Block.column(10.0D, 0.0D, 1.0D);
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateHorizontal(Block.boxZ(10.0D, 0.0D, 1.0D, 0.0D, 8.0D));
        Map<EnumDirection, VoxelShape> map1 = VoxelShapes.rotateHorizontal(Block.boxZ(10.0D, 16.0D, 0.0D, 1.0D));

        return this.getShapeForEachState((iblockdata) -> {
            VoxelShape voxelshape1 = voxelshape;

            for (Map.Entry<EnumDirection, BlockStateEnum<BlockPropertyRedstoneSide>> map_entry : BlockRedstoneWire.PROPERTY_BY_DIRECTION.entrySet()) {
                VoxelShape voxelshape2;

                switch ((BlockPropertyRedstoneSide) iblockdata.getValue((IBlockState) map_entry.getValue())) {
                    case UP:
                        voxelshape2 = VoxelShapes.or(voxelshape1, (VoxelShape) map.get(map_entry.getKey()), (VoxelShape) map1.get(map_entry.getKey()));
                        break;
                    case SIDE:
                        voxelshape2 = VoxelShapes.or(voxelshape1, (VoxelShape) map.get(map_entry.getKey()));
                        break;
                    case NONE:
                        voxelshape2 = voxelshape1;
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }

                voxelshape1 = voxelshape2;
            }

            return voxelshape1;
        }, new IBlockState[]{BlockRedstoneWire.POWER});
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return this.getConnectionState(blockactioncontext.getLevel(), this.crossState, blockactioncontext.getClickedPos());
    }

    private IBlockData getConnectionState(IBlockAccess iblockaccess, IBlockData iblockdata, BlockPosition blockposition) {
        boolean flag = isDot(iblockdata);

        iblockdata = this.getMissingConnections(iblockaccess, (IBlockData) this.defaultBlockState().setValue(BlockRedstoneWire.POWER, (Integer) iblockdata.getValue(BlockRedstoneWire.POWER)), blockposition);
        if (flag && isDot(iblockdata)) {
            return iblockdata;
        } else {
            boolean flag1 = ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.NORTH)).isConnected();
            boolean flag2 = ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.SOUTH)).isConnected();
            boolean flag3 = ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.EAST)).isConnected();
            boolean flag4 = ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.WEST)).isConnected();
            boolean flag5 = !flag1 && !flag2;
            boolean flag6 = !flag3 && !flag4;

            if (!flag4 && flag5) {
                iblockdata = (IBlockData) iblockdata.setValue(BlockRedstoneWire.WEST, BlockPropertyRedstoneSide.SIDE);
            }

            if (!flag3 && flag5) {
                iblockdata = (IBlockData) iblockdata.setValue(BlockRedstoneWire.EAST, BlockPropertyRedstoneSide.SIDE);
            }

            if (!flag1 && flag6) {
                iblockdata = (IBlockData) iblockdata.setValue(BlockRedstoneWire.NORTH, BlockPropertyRedstoneSide.SIDE);
            }

            if (!flag2 && flag6) {
                iblockdata = (IBlockData) iblockdata.setValue(BlockRedstoneWire.SOUTH, BlockPropertyRedstoneSide.SIDE);
            }

            return iblockdata;
        }
    }

    private IBlockData getMissingConnections(IBlockAccess iblockaccess, IBlockData iblockdata, BlockPosition blockposition) {
        boolean flag = !iblockaccess.getBlockState(blockposition.above()).isRedstoneConductor(iblockaccess, blockposition);

        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            if (!((BlockPropertyRedstoneSide) iblockdata.getValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection))).isConnected()) {
                BlockPropertyRedstoneSide blockpropertyredstoneside = this.getConnectingSide(iblockaccess, blockposition, enumdirection, flag);

                iblockdata = (IBlockData) iblockdata.setValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection), blockpropertyredstoneside);
            }
        }

        return iblockdata;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (enumdirection == EnumDirection.DOWN) {
            return !this.canSurviveOn(iworldreader, blockposition1, iblockdata1) ? Blocks.AIR.defaultBlockState() : iblockdata;
        } else if (enumdirection == EnumDirection.UP) {
            return this.getConnectionState(iworldreader, iblockdata, blockposition);
        } else {
            BlockPropertyRedstoneSide blockpropertyredstoneside = this.getConnectingSide(iworldreader, blockposition, enumdirection);

            return blockpropertyredstoneside.isConnected() == ((BlockPropertyRedstoneSide) iblockdata.getValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection))).isConnected() && !isCross(iblockdata) ? (IBlockData) iblockdata.setValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection), blockpropertyredstoneside) : this.getConnectionState(iworldreader, (IBlockData) ((IBlockData) this.crossState.setValue(BlockRedstoneWire.POWER, (Integer) iblockdata.getValue(BlockRedstoneWire.POWER))).setValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection), blockpropertyredstoneside), blockposition);
        }
    }

    private static boolean isCross(IBlockData iblockdata) {
        return ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.NORTH)).isConnected() && ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.SOUTH)).isConnected() && ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.EAST)).isConnected() && ((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.WEST)).isConnected();
    }

    private static boolean isDot(IBlockData iblockdata) {
        return !((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.NORTH)).isConnected() && !((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.SOUTH)).isConnected() && !((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.EAST)).isConnected() && !((BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.WEST)).isConnected();
    }

    @Override
    protected void updateIndirectNeighbourShapes(IBlockData iblockdata, GeneratorAccess generatoraccess, BlockPosition blockposition, int i, int j) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            BlockPropertyRedstoneSide blockpropertyredstoneside = (BlockPropertyRedstoneSide) iblockdata.getValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection));

            if (blockpropertyredstoneside != BlockPropertyRedstoneSide.NONE && !generatoraccess.getBlockState(blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection)).is(this)) {
                blockposition_mutableblockposition.move(EnumDirection.DOWN);
                IBlockData iblockdata1 = generatoraccess.getBlockState(blockposition_mutableblockposition);

                if (iblockdata1.is(this)) {
                    BlockPosition blockposition1 = blockposition_mutableblockposition.relative(enumdirection.getOpposite());

                    generatoraccess.neighborShapeChanged(enumdirection.getOpposite(), blockposition_mutableblockposition, blockposition1, generatoraccess.getBlockState(blockposition1), i, j);
                }

                blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection).move(EnumDirection.UP);
                IBlockData iblockdata2 = generatoraccess.getBlockState(blockposition_mutableblockposition);

                if (iblockdata2.is(this)) {
                    BlockPosition blockposition2 = blockposition_mutableblockposition.relative(enumdirection.getOpposite());

                    generatoraccess.neighborShapeChanged(enumdirection.getOpposite(), blockposition_mutableblockposition, blockposition2, generatoraccess.getBlockState(blockposition2), i, j);
                }
            }
        }

    }

    private BlockPropertyRedstoneSide getConnectingSide(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getConnectingSide(iblockaccess, blockposition, enumdirection, !iblockaccess.getBlockState(blockposition.above()).isRedstoneConductor(iblockaccess, blockposition));
    }

    private BlockPropertyRedstoneSide getConnectingSide(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection, boolean flag) {
        BlockPosition blockposition1 = blockposition.relative(enumdirection);
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition1);

        if (flag) {
            boolean flag1 = iblockdata.getBlock() instanceof BlockTrapdoor || this.canSurviveOn(iblockaccess, blockposition1, iblockdata);

            if (flag1 && shouldConnectTo(iblockaccess.getBlockState(blockposition1.above()))) {
                if (iblockdata.isFaceSturdy(iblockaccess, blockposition1, enumdirection.getOpposite())) {
                    return BlockPropertyRedstoneSide.UP;
                }

                return BlockPropertyRedstoneSide.SIDE;
            }
        }

        return !shouldConnectTo(iblockdata, enumdirection) && (iblockdata.isRedstoneConductor(iblockaccess, blockposition1) || !shouldConnectTo(iblockaccess.getBlockState(blockposition1.below()))) ? BlockPropertyRedstoneSide.NONE : BlockPropertyRedstoneSide.SIDE;
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition1);

        return this.canSurviveOn(iworldreader, blockposition1, iblockdata1);
    }

    private boolean canSurviveOn(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        return iblockdata.isFaceSturdy(iblockaccess, blockposition, EnumDirection.UP) || iblockdata.is(Blocks.HOPPER);
    }

    private void updatePowerStrength(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable Orientation orientation, boolean flag) {
        if (useExperimentalEvaluator(world)) {
            (new ExperimentalRedstoneWireEvaluator(this)).updatePowerStrength(world, blockposition, iblockdata, orientation, flag);
        } else {
            this.evaluator.updatePowerStrength(world, blockposition, iblockdata, orientation, flag);
        }

    }

    public int getBlockSignal(World world, BlockPosition blockposition) {
        this.shouldSignal = false;
        int i = world.getBestNeighborSignal(blockposition);

        this.shouldSignal = true;
        return i;
    }

    private void checkCornerChangeAt(World world, BlockPosition blockposition) {
        if (world.getBlockState(blockposition).is(this)) {
            world.updateNeighborsAt(blockposition, this);

            for (EnumDirection enumdirection : EnumDirection.values()) {
                world.updateNeighborsAt(blockposition.relative(enumdirection), this);
            }

        }
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata1.is(iblockdata.getBlock()) && !world.isClientSide) {
            this.updatePowerStrength(world, blockposition, iblockdata, (Orientation) null, true);

            for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.VERTICAL) {
                world.updateNeighborsAt(blockposition.relative(enumdirection), this);
            }

            this.updateNeighborsOfNeighboringWires(world, blockposition);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        if (!flag) {
            for (EnumDirection enumdirection : EnumDirection.values()) {
                worldserver.updateNeighborsAt(blockposition.relative(enumdirection), this);
            }

            this.updatePowerStrength(worldserver, blockposition, iblockdata, (Orientation) null, false);
            this.updateNeighborsOfNeighboringWires(worldserver, blockposition);
        }
    }

    private void updateNeighborsOfNeighboringWires(World world, BlockPosition blockposition) {
        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            this.checkCornerChangeAt(world, blockposition.relative(enumdirection));
        }

        for (EnumDirection enumdirection1 : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            BlockPosition blockposition1 = blockposition.relative(enumdirection1);

            if (world.getBlockState(blockposition1).isRedstoneConductor(world, blockposition1)) {
                this.checkCornerChangeAt(world, blockposition1.above());
            } else {
                this.checkCornerChangeAt(world, blockposition1.below());
            }
        }

    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (!world.isClientSide) {
            if (block != this || !useExperimentalEvaluator(world)) {
                if (iblockdata.canSurvive(world, blockposition)) {
                    this.updatePowerStrength(world, blockposition, iblockdata, orientation, false);
                } else {
                    dropResources(iblockdata, world, blockposition);
                    world.removeBlock(blockposition, false);
                }

            }
        }
    }

    private static boolean useExperimentalEvaluator(World world) {
        return world.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS);
    }

    @Override
    protected int getDirectSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return !this.shouldSignal ? 0 : iblockdata.getSignal(iblockaccess, blockposition, enumdirection);
    }

    @Override
    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        if (this.shouldSignal && enumdirection != EnumDirection.DOWN) {
            int i = (Integer) iblockdata.getValue(BlockRedstoneWire.POWER);

            return i == 0 ? 0 : (enumdirection != EnumDirection.UP && !((BlockPropertyRedstoneSide) this.getConnectionState(iblockaccess, iblockdata, blockposition).getValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection.getOpposite()))).isConnected() ? 0 : i);
        } else {
            return 0;
        }
    }

    protected static boolean shouldConnectTo(IBlockData iblockdata) {
        return shouldConnectTo(iblockdata, (EnumDirection) null);
    }

    protected static boolean shouldConnectTo(IBlockData iblockdata, @Nullable EnumDirection enumdirection) {
        if (iblockdata.is(Blocks.REDSTONE_WIRE)) {
            return true;
        } else if (iblockdata.is(Blocks.REPEATER)) {
            EnumDirection enumdirection1 = (EnumDirection) iblockdata.getValue(BlockRepeater.FACING);

            return enumdirection1 == enumdirection || enumdirection1.getOpposite() == enumdirection;
        } else {
            return iblockdata.is(Blocks.OBSERVER) ? enumdirection == iblockdata.getValue(BlockObserver.FACING) : iblockdata.isSignalSource() && enumdirection != null;
        }
    }

    @Override
    protected boolean isSignalSource(IBlockData iblockdata) {
        return this.shouldSignal;
    }

    public static int getColorForPower(int i) {
        return BlockRedstoneWire.COLORS[i];
    }

    private static void spawnParticlesAlongLine(World world, RandomSource randomsource, BlockPosition blockposition, int i, EnumDirection enumdirection, EnumDirection enumdirection1, float f, float f1) {
        float f2 = f1 - f;

        if (randomsource.nextFloat() < 0.2F * f2) {
            float f3 = 0.4375F;
            float f4 = f + f2 * randomsource.nextFloat();
            double d0 = 0.5D + (double) (0.4375F * (float) enumdirection.getStepX()) + (double) (f4 * (float) enumdirection1.getStepX());
            double d1 = 0.5D + (double) (0.4375F * (float) enumdirection.getStepY()) + (double) (f4 * (float) enumdirection1.getStepY());
            double d2 = 0.5D + (double) (0.4375F * (float) enumdirection.getStepZ()) + (double) (f4 * (float) enumdirection1.getStepZ());

            world.addParticle(new ParticleParamRedstone(i, 1.0F), (double) blockposition.getX() + d0, (double) blockposition.getY() + d1, (double) blockposition.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        int i = (Integer) iblockdata.getValue(BlockRedstoneWire.POWER);

        if (i != 0) {
            for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                BlockPropertyRedstoneSide blockpropertyredstoneside = (BlockPropertyRedstoneSide) iblockdata.getValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection));

                switch (blockpropertyredstoneside) {
                    case UP:
                        spawnParticlesAlongLine(world, randomsource, blockposition, BlockRedstoneWire.COLORS[i], enumdirection, EnumDirection.UP, -0.5F, 0.5F);
                    case SIDE:
                        spawnParticlesAlongLine(world, randomsource, blockposition, BlockRedstoneWire.COLORS[i], EnumDirection.DOWN, enumdirection, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        spawnParticlesAlongLine(world, randomsource, blockposition, BlockRedstoneWire.COLORS[i], EnumDirection.DOWN, enumdirection, 0.0F, 0.3F);
                }
            }

        }
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (enumblockrotation) {
            case CLOCKWISE_180:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockRedstoneWire.NORTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.SOUTH))).setValue(BlockRedstoneWire.EAST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.WEST))).setValue(BlockRedstoneWire.SOUTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.NORTH))).setValue(BlockRedstoneWire.WEST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.EAST));
            case COUNTERCLOCKWISE_90:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockRedstoneWire.NORTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.EAST))).setValue(BlockRedstoneWire.EAST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.SOUTH))).setValue(BlockRedstoneWire.SOUTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.WEST))).setValue(BlockRedstoneWire.WEST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.NORTH));
            case CLOCKWISE_90:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockRedstoneWire.NORTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.WEST))).setValue(BlockRedstoneWire.EAST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.NORTH))).setValue(BlockRedstoneWire.SOUTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.EAST))).setValue(BlockRedstoneWire.WEST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.SOUTH));
            default:
                return iblockdata;
        }
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        switch (enumblockmirror) {
            case LEFT_RIGHT:
                return (IBlockData) ((IBlockData) iblockdata.setValue(BlockRedstoneWire.NORTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.SOUTH))).setValue(BlockRedstoneWire.SOUTH, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.NORTH));
            case FRONT_BACK:
                return (IBlockData) ((IBlockData) iblockdata.setValue(BlockRedstoneWire.EAST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.WEST))).setValue(BlockRedstoneWire.WEST, (BlockPropertyRedstoneSide) iblockdata.getValue(BlockRedstoneWire.EAST));
            default:
                return super.mirror(iblockdata, enumblockmirror);
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockRedstoneWire.NORTH, BlockRedstoneWire.EAST, BlockRedstoneWire.SOUTH, BlockRedstoneWire.WEST, BlockRedstoneWire.POWER);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!entityhuman.getAbilities().mayBuild) {
            return EnumInteractionResult.PASS;
        } else {
            if (isCross(iblockdata) || isDot(iblockdata)) {
                IBlockData iblockdata1 = isCross(iblockdata) ? this.defaultBlockState() : this.crossState;

                iblockdata1 = (IBlockData) iblockdata1.setValue(BlockRedstoneWire.POWER, (Integer) iblockdata.getValue(BlockRedstoneWire.POWER));
                iblockdata1 = this.getConnectionState(world, iblockdata1, blockposition);
                if (iblockdata1 != iblockdata) {
                    world.setBlock(blockposition, iblockdata1, 3);
                    this.updatesOnShapeChange(world, blockposition, iblockdata, iblockdata1);
                    return EnumInteractionResult.SUCCESS;
                }
            }

            return EnumInteractionResult.PASS;
        }
    }

    private void updatesOnShapeChange(World world, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1) {
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(world, (EnumDirection) null, EnumDirection.UP);

        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (((BlockPropertyRedstoneSide) iblockdata.getValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection))).isConnected() != ((BlockPropertyRedstoneSide) iblockdata1.getValue((IBlockState) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection))).isConnected() && world.getBlockState(blockposition1).isRedstoneConductor(world, blockposition1)) {
                world.updateNeighborsAtExceptFromFacing(blockposition1, iblockdata1.getBlock(), enumdirection.getOpposite(), ExperimentalRedstoneUtils.withFront(orientation, enumdirection));
            }
        }

    }
}
