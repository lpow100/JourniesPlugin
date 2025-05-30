package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyStructureMode;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockStructure extends BlockTileEntity implements GameMasterBlock {

    public static final MapCodec<BlockStructure> CODEC = simpleCodec(BlockStructure::new);
    public static final BlockStateEnum<BlockPropertyStructureMode> MODE = BlockProperties.STRUCTUREBLOCK_MODE;

    @Override
    public MapCodec<BlockStructure> codec() {
        return BlockStructure.CODEC;
    }

    protected BlockStructure(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockStructure.MODE, BlockPropertyStructureMode.LOAD));
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityStructure(blockposition, iblockdata);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        return (EnumInteractionResult) (tileentity instanceof TileEntityStructure ? (((TileEntityStructure) tileentity).usedBy(entityhuman) ? EnumInteractionResult.SUCCESS : EnumInteractionResult.PASS) : EnumInteractionResult.PASS);
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        if (!world.isClientSide) {
            if (entityliving != null) {
                TileEntity tileentity = world.getBlockEntity(blockposition);

                if (tileentity instanceof TileEntityStructure) {
                    ((TileEntityStructure) tileentity).createdBy(entityliving);
                }
            }

        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockStructure.MODE);
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (world instanceof WorldServer) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityStructure) {
                TileEntityStructure tileentitystructure = (TileEntityStructure) tileentity;
                boolean flag1 = world.hasNeighborSignal(blockposition);
                boolean flag2 = tileentitystructure.isPowered();

                if (flag1 && !flag2) {
                    tileentitystructure.setPowered(true);
                    this.trigger((WorldServer) world, tileentitystructure);
                } else if (!flag1 && flag2) {
                    tileentitystructure.setPowered(false);
                }

            }
        }
    }

    private void trigger(WorldServer worldserver, TileEntityStructure tileentitystructure) {
        switch (tileentitystructure.getMode()) {
            case SAVE:
                tileentitystructure.saveStructure(false);
                break;
            case LOAD:
                tileentitystructure.placeStructure(worldserver);
                break;
            case CORNER:
                tileentitystructure.unloadStructure();
            case DATA:
        }

    }
}
