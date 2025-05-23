package net.minecraft.world.level.block.piston;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTileEntity;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.EnumRenderType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyPistonType;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockPistonMoving extends BlockTileEntity {

    public static final MapCodec<BlockPistonMoving> CODEC = simpleCodec(BlockPistonMoving::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockPistonExtension.FACING;
    public static final BlockStateEnum<BlockPropertyPistonType> TYPE = BlockPistonExtension.TYPE;

    @Override
    public MapCodec<BlockPistonMoving> codec() {
        return BlockPistonMoving.CODEC;
    }

    public BlockPistonMoving(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockPistonMoving.FACING, EnumDirection.NORTH)).setValue(BlockPistonMoving.TYPE, BlockPropertyPistonType.DEFAULT));
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return null;
    }

    public static TileEntity newMovingBlockEntity(BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, EnumDirection enumdirection, boolean flag, boolean flag1) {
        return new TileEntityPiston(blockposition, iblockdata, iblockdata1, enumdirection, flag, flag1);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return createTickerHelper(tileentitytypes, TileEntityTypes.PISTON, TileEntityPiston::tick);
    }

    @Override
    public void destroy(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata) {
        BlockPosition blockposition1 = blockposition.relative(((EnumDirection) iblockdata.getValue(BlockPistonMoving.FACING)).getOpposite());
        IBlockData iblockdata1 = generatoraccess.getBlockState(blockposition1);

        if (iblockdata1.getBlock() instanceof BlockPiston && (Boolean) iblockdata1.getValue(BlockPiston.EXTENDED)) {
            generatoraccess.removeBlock(blockposition1, false);
        }

    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!world.isClientSide && world.getBlockEntity(blockposition) == null) {
            world.removeBlock(blockposition, false);
            return EnumInteractionResult.CONSUME;
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    @Override
    protected List<ItemStack> getDrops(IBlockData iblockdata, LootParams.a lootparams_a) {
        TileEntityPiston tileentitypiston = this.getBlockEntity(lootparams_a.getLevel(), BlockPosition.containing((IPosition) lootparams_a.getParameter(LootContextParameters.ORIGIN)));

        return tileentitypiston == null ? Collections.emptyList() : tileentitypiston.getMovedState().getDrops(lootparams_a);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return VoxelShapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        TileEntityPiston tileentitypiston = this.getBlockEntity(iblockaccess, blockposition);

        return tileentitypiston != null ? tileentitypiston.getCollisionShape(iblockaccess, blockposition) : VoxelShapes.empty();
    }

    @Nullable
    private TileEntityPiston getBlockEntity(IBlockAccess iblockaccess, BlockPosition blockposition) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        return tileentity instanceof TileEntityPiston ? (TileEntityPiston) tileentity : null;
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.INVISIBLE;
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return ItemStack.EMPTY;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockPistonMoving.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockPistonMoving.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockPistonMoving.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPistonMoving.FACING, BlockPistonMoving.TYPE);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
