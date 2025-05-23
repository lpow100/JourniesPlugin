package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public abstract class BlockMinecartTrackAbstract extends Block implements IBlockWaterlogged {

    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_FLAT = Block.column(16.0D, 0.0D, 2.0D);
    private static final VoxelShape SHAPE_SLOPE = Block.column(16.0D, 0.0D, 8.0D);
    private final boolean isStraight;

    public static boolean isRail(World world, BlockPosition blockposition) {
        return isRail(world.getBlockState(blockposition));
    }

    public static boolean isRail(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.RAILS) && iblockdata.getBlock() instanceof BlockMinecartTrackAbstract;
    }

    protected BlockMinecartTrackAbstract(boolean flag, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.isStraight = flag;
    }

    @Override
    protected abstract MapCodec<? extends BlockMinecartTrackAbstract> codec();

    public boolean isStraight() {
        return this.isStraight;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return ((BlockPropertyTrackPosition) iblockdata.getValue(this.getShapeProperty())).isSlope() ? BlockMinecartTrackAbstract.SHAPE_SLOPE : BlockMinecartTrackAbstract.SHAPE_FLAT;
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return canSupportRigidBlock(iworldreader, blockposition.below());
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata1.is(iblockdata.getBlock())) {
            this.updateState(iblockdata, world, blockposition, flag);
        }
    }

    protected IBlockData updateState(IBlockData iblockdata, World world, BlockPosition blockposition, boolean flag) {
        iblockdata = this.updateDir(world, blockposition, iblockdata, true);
        if (this.isStraight) {
            world.neighborChanged(iblockdata, blockposition, this, (Orientation) null, flag);
        }

        return iblockdata;
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (!world.isClientSide && world.getBlockState(blockposition).is(this)) {
            BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(this.getShapeProperty());

            if (shouldBeRemoved(blockposition, world, blockpropertytrackposition)) {
                dropResources(iblockdata, world, blockposition);
                world.removeBlock(blockposition, flag);
            } else {
                this.updateState(iblockdata, world, blockposition, block);
            }

        }
    }

    private static boolean shouldBeRemoved(BlockPosition blockposition, World world, BlockPropertyTrackPosition blockpropertytrackposition) {
        if (!canSupportRigidBlock(world, blockposition.below())) {
            return true;
        } else {
            switch (blockpropertytrackposition) {
                case ASCENDING_EAST:
                    return !canSupportRigidBlock(world, blockposition.east());
                case ASCENDING_WEST:
                    return !canSupportRigidBlock(world, blockposition.west());
                case ASCENDING_NORTH:
                    return !canSupportRigidBlock(world, blockposition.north());
                case ASCENDING_SOUTH:
                    return !canSupportRigidBlock(world, blockposition.south());
                default:
                    return false;
            }
        }
    }

    protected void updateState(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {}

    protected IBlockData updateDir(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        if (world.isClientSide) {
            return iblockdata;
        } else {
            BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(this.getShapeProperty());

            return (new MinecartTrackLogic(world, blockposition, iblockdata)).place(world.hasNeighborSignal(blockposition), flag, blockpropertytrackposition).getState();
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        if (!flag) {
            if (((BlockPropertyTrackPosition) iblockdata.getValue(this.getShapeProperty())).isSlope()) {
                worldserver.updateNeighborsAt(blockposition.above(), this);
            }

            if (this.isStraight) {
                worldserver.updateNeighborsAt(blockposition, this);
                worldserver.updateNeighborsAt(blockposition.below(), this);
            }

        }
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());
        boolean flag = fluid.getType() == FluidTypes.WATER;
        IBlockData iblockdata = super.defaultBlockState();
        EnumDirection enumdirection = blockactioncontext.getHorizontalDirection();
        boolean flag1 = enumdirection == EnumDirection.EAST || enumdirection == EnumDirection.WEST;

        return (IBlockData) ((IBlockData) iblockdata.setValue(this.getShapeProperty(), flag1 ? BlockPropertyTrackPosition.EAST_WEST : BlockPropertyTrackPosition.NORTH_SOUTH)).setValue(BlockMinecartTrackAbstract.WATERLOGGED, flag);
    }

    public abstract IBlockState<BlockPropertyTrackPosition> getShapeProperty();

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockMinecartTrackAbstract.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockMinecartTrackAbstract.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }
}
