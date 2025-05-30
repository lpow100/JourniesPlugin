package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockObserver extends BlockDirectional {

    public static final MapCodec<BlockObserver> CODEC = simpleCodec(BlockObserver::new);
    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;

    @Override
    public MapCodec<BlockObserver> codec() {
        return BlockObserver.CODEC;
    }

    public BlockObserver(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockObserver.FACING, EnumDirection.SOUTH)).setValue(BlockObserver.POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockObserver.FACING, BlockObserver.POWERED);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockObserver.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockObserver.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockObserver.FACING)));
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockObserver.POWERED)) {
            // CraftBukkit start
            if (CraftEventFactory.callRedstoneChange(worldserver, blockposition, 15, 0).getNewCurrent() != 0) {
                return;
            }
            // CraftBukkit end
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockObserver.POWERED, false), 2);
        } else {
            // CraftBukkit start
            if (CraftEventFactory.callRedstoneChange(worldserver, blockposition, 0, 15).getNewCurrent() != 15) {
                return;
            }
            // CraftBukkit end
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockObserver.POWERED, true), 2);
            worldserver.scheduleTick(blockposition, (Block) this, 2);
        }

        this.updateNeighborsInFront(worldserver, blockposition, iblockdata);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (iblockdata.getValue(BlockObserver.FACING) == enumdirection && !(Boolean) iblockdata.getValue(BlockObserver.POWERED)) {
            this.startSignal(iworldreader, scheduledtickaccess, blockposition);
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    private void startSignal(IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition) {
        if (!iworldreader.isClientSide() && !scheduledtickaccess.getBlockTicks().hasScheduledTick(blockposition, this)) {
            scheduledtickaccess.scheduleTick(blockposition, (Block) this, 2);
        }

    }

    protected void updateNeighborsInFront(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockObserver.FACING);
        BlockPosition blockposition1 = blockposition.relative(enumdirection.getOpposite());
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(world, enumdirection.getOpposite(), (EnumDirection) null);

        world.neighborChanged(blockposition1, this, orientation);
        world.updateNeighborsAtExceptFromFacing(blockposition1, this, enumdirection, orientation);
    }

    @Override
    protected boolean isSignalSource(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getDirectSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return iblockdata.getSignal(iblockaccess, blockposition, enumdirection);
    }

    @Override
    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return (Boolean) iblockdata.getValue(BlockObserver.POWERED) && iblockdata.getValue(BlockObserver.FACING) == enumdirection ? 15 : 0;
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata.is(iblockdata1.getBlock())) {
            if (!world.isClientSide() && (Boolean) iblockdata.getValue(BlockObserver.POWERED) && !world.getBlockTicks().hasScheduledTick(blockposition, this)) {
                IBlockData iblockdata2 = (IBlockData) iblockdata.setValue(BlockObserver.POWERED, false);

                world.setBlock(blockposition, iblockdata2, 18);
                this.updateNeighborsInFront(world, blockposition, iblockdata2);
            }

        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        if ((Boolean) iblockdata.getValue(BlockObserver.POWERED) && worldserver.getBlockTicks().hasScheduledTick(blockposition, this)) {
            this.updateNeighborsInFront(worldserver, blockposition, (IBlockData) iblockdata.setValue(BlockObserver.POWERED, false));
        }

    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockObserver.FACING, blockactioncontext.getNearestLookingDirection().getOpposite().getOpposite());
    }
}
