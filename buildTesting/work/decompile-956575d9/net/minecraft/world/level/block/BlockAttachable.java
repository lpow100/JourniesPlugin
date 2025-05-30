package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyAttachPosition;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;

public abstract class BlockAttachable extends BlockFacingHorizontal {

    public static final BlockStateEnum<BlockPropertyAttachPosition> FACE = BlockProperties.ATTACH_FACE;

    protected BlockAttachable(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected abstract MapCodec<? extends BlockAttachable> codec();

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return canAttach(iworldreader, blockposition, getConnectedDirection(iblockdata).getOpposite());
    }

    public static boolean canAttach(IWorldReader iworldreader, BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition blockposition1 = blockposition.relative(enumdirection);

        return iworldreader.getBlockState(blockposition1).isFaceSturdy(iworldreader, blockposition1, enumdirection.getOpposite());
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        for (EnumDirection enumdirection : blockactioncontext.getNearestLookingDirections()) {
            IBlockData iblockdata;

            if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
                iblockdata = (IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockAttachable.FACE, enumdirection == EnumDirection.UP ? BlockPropertyAttachPosition.CEILING : BlockPropertyAttachPosition.FLOOR)).setValue(BlockAttachable.FACING, blockactioncontext.getHorizontalDirection());
            } else {
                iblockdata = (IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockAttachable.FACE, BlockPropertyAttachPosition.WALL)).setValue(BlockAttachable.FACING, enumdirection.getOpposite());
            }

            if (iblockdata.canSurvive(blockactioncontext.getLevel(), blockactioncontext.getClickedPos())) {
                return iblockdata;
            }
        }

        return null;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return getConnectedDirection(iblockdata).getOpposite() == enumdirection && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    protected static EnumDirection getConnectedDirection(IBlockData iblockdata) {
        switch ((BlockPropertyAttachPosition) iblockdata.getValue(BlockAttachable.FACE)) {
            case CEILING:
                return EnumDirection.DOWN;
            case FLOOR:
                return EnumDirection.UP;
            default:
                return (EnumDirection) iblockdata.getValue(BlockAttachable.FACING);
        }
    }
}
