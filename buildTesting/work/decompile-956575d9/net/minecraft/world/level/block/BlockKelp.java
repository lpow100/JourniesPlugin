package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockKelp extends BlockGrowingTop implements IFluidContainer {

    public static final MapCodec<BlockKelp> CODEC = simpleCodec(BlockKelp::new);
    private static final double GROW_PER_TICK_PROBABILITY = 0.14D;
    private static final VoxelShape SHAPE = Block.column(16.0D, 0.0D, 9.0D);

    @Override
    public MapCodec<BlockKelp> codec() {
        return BlockKelp.CODEC;
    }

    protected BlockKelp(BlockBase.Info blockbase_info) {
        super(blockbase_info, EnumDirection.UP, BlockKelp.SHAPE, true, 0.14D);
    }

    @Override
    protected boolean canGrowInto(IBlockData iblockdata) {
        return iblockdata.is(Blocks.WATER);
    }

    @Override
    protected Block getBodyBlock() {
        return Blocks.KELP_PLANT;
    }

    @Override
    protected boolean canAttachTo(IBlockData iblockdata) {
        return !iblockdata.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable EntityLiving entityliving, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, FluidType fluidtype) {
        return false;
    }

    @Override
    public boolean placeLiquid(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
        return false;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource randomsource) {
        return 1;
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        return fluid.is(TagsFluid.WATER) && fluid.getAmount() == 8 ? super.getStateForPlacement(blockactioncontext) : null;
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return FluidTypes.WATER.getSource(false);
    }
}
