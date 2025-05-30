package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockReed extends Block {

    public static final MapCodec<BlockReed> CODEC = simpleCodec(BlockReed::new);
    public static final BlockStateInteger AGE = BlockProperties.AGE_15;
    private static final VoxelShape SHAPE = Block.column(12.0D, 0.0D, 16.0D);

    @Override
    public MapCodec<BlockReed> codec() {
        return BlockReed.CODEC;
    }

    protected BlockReed(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockReed.AGE, 0));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockReed.SHAPE;
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!iblockdata.canSurvive(worldserver, blockposition)) {
            worldserver.destroyBlock(blockposition, true);
        }

    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (worldserver.isEmptyBlock(blockposition.above())) {
            int i;

            for (i = 1; worldserver.getBlockState(blockposition.below(i)).is(this); ++i) {
                ;
            }

            if (i < 3) {
                int j = (Integer) iblockdata.getValue(BlockReed.AGE);

                if (j == 15) {
                    worldserver.setBlockAndUpdate(blockposition.above(), this.defaultBlockState());
                    worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockReed.AGE, 0), 260);
                } else {
                    worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockReed.AGE, j + 1), 260);
                }
            }
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (!iblockdata.canSurvive(iworldreader, blockposition)) {
            scheduledtickaccess.scheduleTick(blockposition, (Block) this, 1);
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.below());

        if (iblockdata1.is(this)) {
            return true;
        } else {
            if (iblockdata1.is(TagsBlock.DIRT) || iblockdata1.is(TagsBlock.SAND)) {
                BlockPosition blockposition1 = blockposition.below();

                for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                    IBlockData iblockdata2 = iworldreader.getBlockState(blockposition1.relative(enumdirection));
                    Fluid fluid = iworldreader.getFluidState(blockposition1.relative(enumdirection));

                    if (fluid.is(TagsFluid.WATER) || iblockdata2.is(Blocks.FROSTED_ICE)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockReed.AGE);
    }
}
