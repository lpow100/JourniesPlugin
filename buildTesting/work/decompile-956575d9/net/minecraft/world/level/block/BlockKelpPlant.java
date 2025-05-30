package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockKelpPlant extends BlockGrowingStem implements IFluidContainer {

    public static final MapCodec<BlockKelpPlant> CODEC = simpleCodec(BlockKelpPlant::new);

    @Override
    public MapCodec<BlockKelpPlant> codec() {
        return BlockKelpPlant.CODEC;
    }

    protected BlockKelpPlant(BlockBase.Info blockbase_info) {
        super(blockbase_info, EnumDirection.UP, VoxelShapes.block(), true);
    }

    @Override
    protected BlockGrowingTop getHeadBlock() {
        return (BlockGrowingTop) Blocks.KELP;
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return FluidTypes.WATER.getSource(false);
    }

    @Override
    protected boolean canAttachTo(IBlockData iblockdata) {
        return this.getHeadBlock().canAttachTo(iblockdata);
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
