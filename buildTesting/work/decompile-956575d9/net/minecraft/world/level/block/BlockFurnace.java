package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityFurnace;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public abstract class BlockFurnace extends BlockTileEntity {

    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateBoolean LIT = BlockProperties.LIT;

    protected BlockFurnace(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockFurnace.FACING, EnumDirection.NORTH)).setValue(BlockFurnace.LIT, false));
    }

    @Override
    protected abstract MapCodec<? extends BlockFurnace> codec();

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!world.isClientSide) {
            this.openContainer(world, blockposition, entityhuman);
        }

        return EnumInteractionResult.SUCCESS;
    }

    protected abstract void openContainer(World world, BlockPosition blockposition, EntityHuman entityhuman);

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockFurnace.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        InventoryUtils.updateNeighboursAfterDestroy(iblockdata, worldserver, blockposition);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(blockposition));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockFurnace.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockFurnace.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockFurnace.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockFurnace.FACING, BlockFurnace.LIT);
    }

    @Nullable
    protected static <T extends TileEntity> BlockEntityTicker<T> createFurnaceTicker(World world, TileEntityTypes<T> tileentitytypes, TileEntityTypes<? extends TileEntityFurnace> tileentitytypes1) {
        BlockEntityTicker blockentityticker;

        if (world instanceof WorldServer worldserver) {
            blockentityticker = createTickerHelper(tileentitytypes, tileentitytypes1, (world1, blockposition, iblockdata, tileentityfurnace) -> {
                TileEntityFurnace.serverTick(worldserver, blockposition, iblockdata, tileentityfurnace);
            });
        } else {
            blockentityticker = null;
        }

        return blockentityticker;
    }
}
