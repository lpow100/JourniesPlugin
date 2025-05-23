package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockLantern extends Block implements IBlockWaterlogged {

    public static final MapCodec<BlockLantern> CODEC = simpleCodec(BlockLantern::new);
    public static final BlockStateBoolean HANGING = BlockProperties.HANGING;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_STANDING = VoxelShapes.or(Block.column(4.0D, 7.0D, 9.0D), Block.column(6.0D, 0.0D, 7.0D));
    private static final VoxelShape SHAPE_HANGING = BlockLantern.SHAPE_STANDING.move(0.0D, 0.0625D, 0.0D).optimize();

    @Override
    public MapCodec<BlockLantern> codec() {
        return BlockLantern.CODEC;
    }

    public BlockLantern(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockLantern.HANGING, false)).setValue(BlockLantern.WATERLOGGED, false));
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        for (EnumDirection enumdirection : blockactioncontext.getNearestLookingDirections()) {
            if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
                IBlockData iblockdata = (IBlockData) this.defaultBlockState().setValue(BlockLantern.HANGING, enumdirection == EnumDirection.UP);

                if (iblockdata.canSurvive(blockactioncontext.getLevel(), blockactioncontext.getClickedPos())) {
                    return (IBlockData) iblockdata.setValue(BlockLantern.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
                }
            }
        }

        return null;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (Boolean) iblockdata.getValue(BlockLantern.HANGING) ? BlockLantern.SHAPE_HANGING : BlockLantern.SHAPE_STANDING;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockLantern.HANGING, BlockLantern.WATERLOGGED);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection enumdirection = getConnectedDirection(iblockdata).getOpposite();

        return Block.canSupportCenter(iworldreader, blockposition.relative(enumdirection), enumdirection.getOpposite());
    }

    protected static EnumDirection getConnectedDirection(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockLantern.HANGING) ? EnumDirection.DOWN : EnumDirection.UP;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockLantern.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return getConnectedDirection(iblockdata).getOpposite() == enumdirection && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockLantern.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
