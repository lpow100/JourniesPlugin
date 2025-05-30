package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class BlockBubbleColumn extends Block implements IFluidSource {

    public static final MapCodec<BlockBubbleColumn> CODEC = simpleCodec(BlockBubbleColumn::new);
    public static final BlockStateBoolean DRAG_DOWN = BlockProperties.DRAG;
    private static final int CHECK_PERIOD = 5;

    @Override
    public MapCodec<BlockBubbleColumn> codec() {
        return BlockBubbleColumn.CODEC;
    }

    public BlockBubbleColumn(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockBubbleColumn.DRAG_DOWN, true));
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity, InsideBlockEffectApplier insideblockeffectapplier) {
        IBlockData iblockdata1 = world.getBlockState(blockposition.above());
        boolean flag = iblockdata1.getCollisionShape(world, blockposition).isEmpty() && iblockdata1.getFluidState().isEmpty();

        if (flag) {
            entity.onAboveBubbleColumn((Boolean) iblockdata.getValue(BlockBubbleColumn.DRAG_DOWN), blockposition);
        } else {
            entity.onInsideBubbleColumn((Boolean) iblockdata.getValue(BlockBubbleColumn.DRAG_DOWN));
        }

    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        updateColumn(worldserver, blockposition, iblockdata, worldserver.getBlockState(blockposition.below()));
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return FluidTypes.WATER.getSource(false);
    }

    public static void updateColumn(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata) {
        updateColumn(generatoraccess, blockposition, generatoraccess.getBlockState(blockposition), iblockdata);
    }

    public static void updateColumn(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1) {
        if (canExistIn(iblockdata)) {
            IBlockData iblockdata2 = getColumnState(iblockdata1);

            generatoraccess.setBlock(blockposition, iblockdata2, 2);
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable().move(EnumDirection.UP);

            while (canExistIn(generatoraccess.getBlockState(blockposition_mutableblockposition))) {
                if (!generatoraccess.setBlock(blockposition_mutableblockposition, iblockdata2, 2)) {
                    return;
                }

                blockposition_mutableblockposition.move(EnumDirection.UP);
            }

        }
    }

    private static boolean canExistIn(IBlockData iblockdata) {
        return iblockdata.is(Blocks.BUBBLE_COLUMN) || iblockdata.is(Blocks.WATER) && iblockdata.getFluidState().getAmount() >= 8 && iblockdata.getFluidState().isSource();
    }

    private static IBlockData getColumnState(IBlockData iblockdata) {
        return iblockdata.is(Blocks.BUBBLE_COLUMN) ? iblockdata : (iblockdata.is(Blocks.SOUL_SAND) ? (IBlockData) Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BlockBubbleColumn.DRAG_DOWN, false) : (iblockdata.is(Blocks.MAGMA_BLOCK) ? (IBlockData) Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BlockBubbleColumn.DRAG_DOWN, true) : Blocks.WATER.defaultBlockState()));
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        double d0 = (double) blockposition.getX();
        double d1 = (double) blockposition.getY();
        double d2 = (double) blockposition.getZ();

        if ((Boolean) iblockdata.getValue(BlockBubbleColumn.DRAG_DOWN)) {
            world.addAlwaysVisibleParticle(Particles.CURRENT_DOWN, d0 + 0.5D, d1 + 0.8D, d2, 0.0D, 0.0D, 0.0D);
            if (randomsource.nextInt(200) == 0) {
                world.playLocalSound(d0, d1, d2, SoundEffects.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundCategory.BLOCKS, 0.2F + randomsource.nextFloat() * 0.2F, 0.9F + randomsource.nextFloat() * 0.15F, false);
            }
        } else {
            world.addAlwaysVisibleParticle(Particles.BUBBLE_COLUMN_UP, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.04D, 0.0D);
            world.addAlwaysVisibleParticle(Particles.BUBBLE_COLUMN_UP, d0 + (double) randomsource.nextFloat(), d1 + (double) randomsource.nextFloat(), d2 + (double) randomsource.nextFloat(), 0.0D, 0.04D, 0.0D);
            if (randomsource.nextInt(200) == 0) {
                world.playLocalSound(d0, d1, d2, SoundEffects.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2F + randomsource.nextFloat() * 0.2F, 0.9F + randomsource.nextFloat() * 0.15F, false);
            }
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        if (!iblockdata.canSurvive(iworldreader, blockposition) || enumdirection == EnumDirection.DOWN || enumdirection == EnumDirection.UP && !iblockdata1.is(Blocks.BUBBLE_COLUMN) && canExistIn(iblockdata1)) {
            scheduledtickaccess.scheduleTick(blockposition, (Block) this, 5);
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.below());

        return iblockdata1.is(Blocks.BUBBLE_COLUMN) || iblockdata1.is(Blocks.MAGMA_BLOCK) || iblockdata1.is(Blocks.SOUL_SAND);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return VoxelShapes.empty();
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBubbleColumn.DRAG_DOWN);
    }

    @Override
    public ItemStack pickupBlock(@Nullable EntityLiving entityliving, GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata) {
        generatoraccess.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 11);
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public Optional<SoundEffect> getPickupSound() {
        return FluidTypes.WATER.getPickupSound();
    }
}
