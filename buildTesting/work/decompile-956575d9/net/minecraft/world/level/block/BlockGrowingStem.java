package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockGrowingStem extends BlockGrowingAbstract implements IBlockFragilePlantElement {

    protected BlockGrowingStem(BlockBase.Info blockbase_info, EnumDirection enumdirection, VoxelShape voxelshape, boolean flag) {
        super(blockbase_info, enumdirection, voxelshape, flag);
    }

    @Override
    protected abstract MapCodec<? extends BlockGrowingStem> codec();

    protected IBlockData updateHeadAfterConvertedFromBody(IBlockData iblockdata, IBlockData iblockdata1) {
        return iblockdata1;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (enumdirection == this.growthDirection.getOpposite() && !iblockdata.canSurvive(iworldreader, blockposition)) {
            scheduledtickaccess.scheduleTick(blockposition, (Block) this, 1);
        }

        BlockGrowingTop blockgrowingtop = this.getHeadBlock();

        if (enumdirection == this.growthDirection && !iblockdata1.is(this) && !iblockdata1.is(blockgrowingtop)) {
            return this.updateHeadAfterConvertedFromBody(iblockdata, blockgrowingtop.getStateForPlacement(randomsource));
        } else {
            if (this.scheduleFluidTicks) {
                scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
            }

            return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return new ItemStack(this.getHeadBlock());
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        Optional<BlockPosition> optional = this.getHeadPos(iworldreader, blockposition, iblockdata.getBlock());

        return optional.isPresent() && this.getHeadBlock().canGrowInto(iworldreader.getBlockState(((BlockPosition) optional.get()).relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        Optional<BlockPosition> optional = this.getHeadPos(worldserver, blockposition, iblockdata.getBlock());

        if (optional.isPresent()) {
            IBlockData iblockdata1 = worldserver.getBlockState((BlockPosition) optional.get());

            ((BlockGrowingTop) iblockdata1.getBlock()).performBonemeal(worldserver, randomsource, (BlockPosition) optional.get(), iblockdata1);
        }

    }

    private Optional<BlockPosition> getHeadPos(IBlockAccess iblockaccess, BlockPosition blockposition, Block block) {
        return BlockUtil.getTopConnectedBlock(iblockaccess, blockposition, block, this.growthDirection, this.getHeadBlock());
    }

    @Override
    protected boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        boolean flag = super.canBeReplaced(iblockdata, blockactioncontext);

        return flag && blockactioncontext.getItemInHand().is(this.getHeadBlock().asItem()) ? false : flag;
    }

    @Override
    protected Block getBodyBlock() {
        return this;
    }
}
