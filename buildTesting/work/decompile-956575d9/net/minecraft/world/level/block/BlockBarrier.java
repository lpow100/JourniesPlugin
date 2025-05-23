package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
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

public class BlockBarrier extends Block implements IBlockWaterlogged {

    public static final MapCodec<BlockBarrier> CODEC = simpleCodec(BlockBarrier::new);
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;

    @Override
    public MapCodec<BlockBarrier> codec() {
        return BlockBarrier.CODEC;
    }

    protected BlockBarrier(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(BlockBarrier.WATERLOGGED, false));
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata) {
        return iblockdata.getFluidState().isEmpty();
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.INVISIBLE;
    }

    @Override
    protected float getShadeBrightness(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return 1.0F;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockBarrier.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockBarrier.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockBarrier.WATERLOGGED, blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos()).getType() == FluidTypes.WATER);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBarrier.WATERLOGGED);
    }

    @Override
    public ItemStack pickupBlock(@Nullable EntityLiving entityliving, GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata) {
        if (entityliving instanceof EntityHuman entityhuman) {
            if (entityhuman.isCreative()) {
                return IBlockWaterlogged.super.pickupBlock(entityliving, generatoraccess, blockposition, iblockdata);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable EntityLiving entityliving, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, FluidType fluidtype) {
        if (entityliving instanceof EntityHuman entityhuman) {
            if (entityhuman.isCreative()) {
                return IBlockWaterlogged.super.canPlaceLiquid(entityliving, iblockaccess, blockposition, iblockdata, fluidtype);
            }
        }

        return false;
    }
}
