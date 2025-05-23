package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityJukeBox;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockJukeBox extends BlockTileEntity {

    public static final MapCodec<BlockJukeBox> CODEC = simpleCodec(BlockJukeBox::new);
    public static final BlockStateBoolean HAS_RECORD = BlockProperties.HAS_RECORD;

    @Override
    public MapCodec<BlockJukeBox> codec() {
        return BlockJukeBox.CODEC;
    }

    protected BlockJukeBox(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockJukeBox.HAS_RECORD, false));
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        super.setPlacedBy(world, blockposition, iblockdata, entityliving, itemstack);
        CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);

        if (customdata.contains("RecordItem")) {
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockJukeBox.HAS_RECORD, true), 2);
        }

    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if ((Boolean) iblockdata.getValue(BlockJukeBox.HAS_RECORD)) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityJukeBox) {
                TileEntityJukeBox tileentityjukebox = (TileEntityJukeBox) tileentity;

                tileentityjukebox.popOutTheItem();
                return EnumInteractionResult.SUCCESS;
            }
        }

        return EnumInteractionResult.PASS;
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if ((Boolean) iblockdata.getValue(BlockJukeBox.HAS_RECORD)) {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
            ItemStack itemstack1 = entityhuman.getItemInHand(enumhand);
            EnumInteractionResult enuminteractionresult = JukeboxPlayable.tryInsertIntoJukebox(world, blockposition, itemstack1, entityhuman);

            return (EnumInteractionResult) (!enuminteractionresult.consumesAction() ? EnumInteractionResult.TRY_WITH_EMPTY_HAND : enuminteractionresult);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        InventoryUtils.updateNeighboursAfterDestroy(iblockdata, worldserver, blockposition);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityJukeBox(blockposition, iblockdata);
    }

    @Override
    public boolean isSignalSource(IBlockData iblockdata) {
        return true;
    }

    @Override
    public int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityJukeBox tileentityjukebox) {
            if (tileentityjukebox.getSongPlayer().isPlaying()) {
                return 15;
            }
        }

        return 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityJukeBox tileentityjukebox) {
            return tileentityjukebox.getComparatorOutput();
        } else {
            return 0;
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockJukeBox.HAS_RECORD);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return (Boolean) iblockdata.getValue(BlockJukeBox.HAS_RECORD) ? createTickerHelper(tileentitytypes, TileEntityTypes.JUKEBOX, TileEntityJukeBox::tick) : null;
    }
}
