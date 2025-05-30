package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockGrowingTop extends BlockGrowingAbstract implements IBlockFragilePlantElement {

    public static final BlockStateInteger AGE = BlockProperties.AGE_25;
    public static final int MAX_AGE = 25;
    private final double growPerTickProbability;

    protected BlockGrowingTop(BlockBase.Info blockbase_info, EnumDirection enumdirection, VoxelShape voxelshape, boolean flag, double d0) {
        super(blockbase_info, enumdirection, voxelshape, flag);
        this.growPerTickProbability = d0;
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockGrowingTop.AGE, 0));
    }

    @Override
    protected abstract MapCodec<? extends BlockGrowingTop> codec();

    @Override
    public IBlockData getStateForPlacement(RandomSource randomsource) {
        return (IBlockData) this.defaultBlockState().setValue(BlockGrowingTop.AGE, randomsource.nextInt(25));
    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockGrowingTop.AGE) < 25;
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if ((Integer) iblockdata.getValue(BlockGrowingTop.AGE) < 25 && randomsource.nextDouble() < this.growPerTickProbability) {
            BlockPosition blockposition1 = blockposition.relative(this.growthDirection);

            if (this.canGrowInto(worldserver.getBlockState(blockposition1))) {
                worldserver.setBlockAndUpdate(blockposition1, this.getGrowIntoState(iblockdata, worldserver.random));
            }
        }

    }

    protected IBlockData getGrowIntoState(IBlockData iblockdata, RandomSource randomsource) {
        return (IBlockData) iblockdata.cycle(BlockGrowingTop.AGE);
    }

    public IBlockData getMaxAgeState(IBlockData iblockdata) {
        return (IBlockData) iblockdata.setValue(BlockGrowingTop.AGE, 25);
    }

    public boolean isMaxAge(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockGrowingTop.AGE) == 25;
    }

    protected IBlockData updateBodyAfterConvertedFromHead(IBlockData iblockdata, IBlockData iblockdata1) {
        return iblockdata1;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (enumdirection == this.growthDirection.getOpposite()) {
            if (!iblockdata.canSurvive(iworldreader, blockposition)) {
                scheduledtickaccess.scheduleTick(blockposition, (Block) this, 1);
            } else {
                IBlockData iblockdata2 = iworldreader.getBlockState(blockposition.relative(this.growthDirection));

                if (iblockdata2.is(this) || iblockdata2.is(this.getBodyBlock())) {
                    return this.updateBodyAfterConvertedFromHead(iblockdata, this.getBodyBlock().defaultBlockState());
                }
            }
        }

        if (enumdirection != this.growthDirection || !iblockdata1.is(this) && !iblockdata1.is(this.getBodyBlock())) {
            if (this.scheduleFluidTicks) {
                scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
            }

            return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
        } else {
            return this.updateBodyAfterConvertedFromHead(iblockdata, this.getBodyBlock().defaultBlockState());
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockGrowingTop.AGE);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return this.canGrowInto(iworldreader.getBlockState(blockposition.relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        BlockPosition blockposition1 = blockposition.relative(this.growthDirection);
        int i = Math.min((Integer) iblockdata.getValue(BlockGrowingTop.AGE) + 1, 25);
        int j = this.getBlocksToGrowWhenBonemealed(randomsource);

        for (int k = 0; k < j && this.canGrowInto(worldserver.getBlockState(blockposition1)); ++k) {
            worldserver.setBlockAndUpdate(blockposition1, (IBlockData) iblockdata.setValue(BlockGrowingTop.AGE, i));
            blockposition1 = blockposition1.relative(this.growthDirection);
            i = Math.min(i + 1, 25);
        }

    }

    protected abstract int getBlocksToGrowWhenBonemealed(RandomSource randomsource);

    protected abstract boolean canGrowInto(IBlockData iblockdata);

    @Override
    protected BlockGrowingTop getHeadBlock() {
        return this;
    }
}
