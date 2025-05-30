package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyDoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.material.FluidTypes;

public class BlockTallPlant extends VegetationBlock {

    public static final MapCodec<BlockTallPlant> CODEC = simpleCodec(BlockTallPlant::new);
    public static final BlockStateEnum<BlockPropertyDoubleBlockHalf> HALF = BlockProperties.DOUBLE_BLOCK_HALF;

    @Override
    public MapCodec<? extends BlockTallPlant> codec() {
        return BlockTallPlant.CODEC;
    }

    public BlockTallPlant(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        BlockPropertyDoubleBlockHalf blockpropertydoubleblockhalf = (BlockPropertyDoubleBlockHalf) iblockdata.getValue(BlockTallPlant.HALF);

        return enumdirection.getAxis() != EnumDirection.EnumAxis.Y || blockpropertydoubleblockhalf == BlockPropertyDoubleBlockHalf.LOWER != (enumdirection == EnumDirection.UP) || iblockdata1.is(this) && iblockdata1.getValue(BlockTallPlant.HALF) != blockpropertydoubleblockhalf ? (blockpropertydoubleblockhalf == BlockPropertyDoubleBlockHalf.LOWER && enumdirection == EnumDirection.DOWN && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource)) : Blocks.AIR.defaultBlockState();
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        World world = blockactioncontext.getLevel();

        return blockposition.getY() < world.getMaxY() && world.getBlockState(blockposition.above()).canBeReplaced(blockactioncontext) ? super.getStateForPlacement(blockactioncontext) : null;
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        BlockPosition blockposition1 = blockposition.above();

        world.setBlock(blockposition1, copyWaterloggedFrom(world, blockposition1, (IBlockData) this.defaultBlockState().setValue(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER)), 3);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        if (iblockdata.getValue(BlockTallPlant.HALF) != BlockPropertyDoubleBlockHalf.UPPER) {
            return super.canSurvive(iblockdata, iworldreader, blockposition);
        } else {
            IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.below());

            return iblockdata1.is(this) && iblockdata1.getValue(BlockTallPlant.HALF) == BlockPropertyDoubleBlockHalf.LOWER;
        }
    }

    public static void placeAt(GeneratorAccess generatoraccess, IBlockData iblockdata, BlockPosition blockposition, int i) {
        BlockPosition blockposition1 = blockposition.above();

        generatoraccess.setBlock(blockposition, copyWaterloggedFrom(generatoraccess, blockposition, (IBlockData) iblockdata.setValue(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER)), i);
        generatoraccess.setBlock(blockposition1, copyWaterloggedFrom(generatoraccess, blockposition1, (IBlockData) iblockdata.setValue(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER)), i);
    }

    public static IBlockData copyWaterloggedFrom(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return iblockdata.hasProperty(BlockProperties.WATERLOGGED) ? (IBlockData) iblockdata.setValue(BlockProperties.WATERLOGGED, iworldreader.isWaterAt(blockposition)) : iblockdata;
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (!world.isClientSide) {
            if (entityhuman.preventsBlockDrops()) {
                preventDropFromBottomPart(world, blockposition, iblockdata, entityhuman);
            } else {
                dropResources(iblockdata, world, blockposition, (TileEntity) null, entityhuman, entityhuman.getMainHandItem());
            }
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    @Override
    public void playerDestroy(World world, EntityHuman entityhuman, BlockPosition blockposition, IBlockData iblockdata, @Nullable TileEntity tileentity, ItemStack itemstack) {
        super.playerDestroy(world, entityhuman, blockposition, Blocks.AIR.defaultBlockState(), tileentity, itemstack);
    }

    protected static void preventDropFromBottomPart(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        BlockPropertyDoubleBlockHalf blockpropertydoubleblockhalf = (BlockPropertyDoubleBlockHalf) iblockdata.getValue(BlockTallPlant.HALF);

        if (blockpropertydoubleblockhalf == BlockPropertyDoubleBlockHalf.UPPER) {
            BlockPosition blockposition1 = blockposition.below();
            IBlockData iblockdata1 = world.getBlockState(blockposition1);

            if (iblockdata1.is(iblockdata.getBlock()) && iblockdata1.getValue(BlockTallPlant.HALF) == BlockPropertyDoubleBlockHalf.LOWER) {
                IBlockData iblockdata2 = iblockdata1.getFluidState().is(FluidTypes.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();

                world.setBlock(blockposition1, iblockdata2, 35);
                world.levelEvent(entityhuman, 2001, blockposition1, Block.getId(iblockdata1));
            }
        }

    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockTallPlant.HALF);
    }

    @Override
    protected long getSeed(IBlockData iblockdata, BlockPosition blockposition) {
        return MathHelper.getSeed(blockposition.getX(), blockposition.below(iblockdata.getValue(BlockTallPlant.HALF) == BlockPropertyDoubleBlockHalf.LOWER ? 0 : 1).getY(), blockposition.getZ());
    }
}
