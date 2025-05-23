package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BigDripleafStemBlock extends BlockFacingHorizontal implements IBlockFragilePlantElement, IBlockWaterlogged {

    public static final MapCodec<BigDripleafStemBlock> CODEC = simpleCodec(BigDripleafStemBlock::new);
    private static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final Map<EnumDirection, VoxelShape> SHAPES = VoxelShapes.rotateHorizontal(Block.column(6.0D, 0.0D, 16.0D).move(0.0D, 0.0D, 0.25D).optimize());

    @Override
    public MapCodec<BigDripleafStemBlock> codec() {
        return BigDripleafStemBlock.CODEC;
    }

    protected BigDripleafStemBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BigDripleafStemBlock.WATERLOGGED, false)).setValue(BigDripleafStemBlock.FACING, EnumDirection.NORTH));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) BigDripleafStemBlock.SHAPES.get(iblockdata.getValue(BigDripleafStemBlock.FACING));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BigDripleafStemBlock.WATERLOGGED, BigDripleafStemBlock.FACING);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BigDripleafStemBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition1);
        IBlockData iblockdata2 = iworldreader.getBlockState(blockposition.above());

        return (iblockdata1.is(this) || iblockdata1.is(TagsBlock.BIG_DRIPLEAF_PLACEABLE)) && (iblockdata2.is(this) || iblockdata2.is(Blocks.BIG_DRIPLEAF));
    }

    protected static boolean place(GeneratorAccess generatoraccess, BlockPosition blockposition, Fluid fluid, EnumDirection enumdirection) {
        IBlockData iblockdata = (IBlockData) ((IBlockData) Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(BigDripleafStemBlock.WATERLOGGED, fluid.isSourceOfType(FluidTypes.WATER))).setValue(BigDripleafStemBlock.FACING, enumdirection);

        return generatoraccess.setBlock(blockposition, iblockdata, 3);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((enumdirection == EnumDirection.DOWN || enumdirection == EnumDirection.UP) && !iblockdata.canSurvive(iworldreader, blockposition)) {
            scheduledtickaccess.scheduleTick(blockposition, (Block) this, 1);
        }

        if ((Boolean) iblockdata.getValue(BigDripleafStemBlock.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!iblockdata.canSurvive(worldserver, blockposition)) {
            worldserver.destroyBlock(blockposition, true);
        }

    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        Optional<BlockPosition> optional = BlockUtil.getTopConnectedBlock(iworldreader, blockposition, iblockdata.getBlock(), EnumDirection.UP, Blocks.BIG_DRIPLEAF);

        if (optional.isEmpty()) {
            return false;
        } else {
            BlockPosition blockposition1 = ((BlockPosition) optional.get()).above();
            IBlockData iblockdata1 = iworldreader.getBlockState(blockposition1);

            return BigDripleafBlock.canPlaceAt(iworldreader, blockposition1, iblockdata1);
        }
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        Optional<BlockPosition> optional = BlockUtil.getTopConnectedBlock(worldserver, blockposition, iblockdata.getBlock(), EnumDirection.UP, Blocks.BIG_DRIPLEAF);

        if (!optional.isEmpty()) {
            BlockPosition blockposition1 = (BlockPosition) optional.get();
            BlockPosition blockposition2 = blockposition1.above();
            EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BigDripleafStemBlock.FACING);

            place(worldserver, blockposition1, worldserver.getFluidState(blockposition1), enumdirection);
            BigDripleafBlock.place(worldserver, blockposition2, worldserver.getFluidState(blockposition2), enumdirection);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return new ItemStack(Blocks.BIG_DRIPLEAF);
    }
}
