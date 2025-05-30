package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyDoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class SeagrassBlock extends VegetationBlock implements IBlockFragilePlantElement, IFluidContainer {

    public static final MapCodec<SeagrassBlock> CODEC = simpleCodec(SeagrassBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0D, 0.0D, 12.0D);

    @Override
    public MapCodec<SeagrassBlock> codec() {
        return SeagrassBlock.CODEC;
    }

    protected SeagrassBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return SeagrassBlock.SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.isFaceSturdy(iblockaccess, blockposition, EnumDirection.UP) && !iblockdata.is(Blocks.MAGMA_BLOCK);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        return fluid.is(TagsFluid.WATER) && fluid.getAmount() == 8 ? super.getStateForPlacement(blockactioncontext) : null;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        IBlockData iblockdata2 = super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);

        if (!iblockdata2.isAir()) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return iblockdata2;
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return iworldreader.getBlockState(blockposition.above()).is(Blocks.WATER);
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return FluidTypes.WATER.getSource(false);
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        IBlockData iblockdata1 = Blocks.TALL_SEAGRASS.defaultBlockState();
        IBlockData iblockdata2 = (IBlockData) iblockdata1.setValue(TallSeagrassBlock.HALF, BlockPropertyDoubleBlockHalf.UPPER);
        BlockPosition blockposition1 = blockposition.above();

        worldserver.setBlock(blockposition, iblockdata1, 2);
        worldserver.setBlock(blockposition1, iblockdata2, 2);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable EntityLiving entityliving, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, FluidType fluidtype) {
        return false;
    }

    @Override
    public boolean placeLiquid(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
        return false;
    }
}
