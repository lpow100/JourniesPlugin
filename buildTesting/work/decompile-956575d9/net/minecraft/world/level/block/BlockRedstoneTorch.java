package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamRedstone;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

public class BlockRedstoneTorch extends BaseTorchBlock {

    public static final MapCodec<BlockRedstoneTorch> CODEC = simpleCodec(BlockRedstoneTorch::new);
    public static final BlockStateBoolean LIT = BlockProperties.LIT;
    private static final Map<IBlockAccess, List<BlockRedstoneTorch.RedstoneUpdateInfo>> RECENT_TOGGLES = new WeakHashMap();
    public static final int RECENT_TOGGLE_TIMER = 60;
    public static final int MAX_RECENT_TOGGLES = 8;
    public static final int RESTART_DELAY = 160;
    private static final int TOGGLE_DELAY = 2;

    @Override
    public MapCodec<? extends BlockRedstoneTorch> codec() {
        return BlockRedstoneTorch.CODEC;
    }

    protected BlockRedstoneTorch(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockRedstoneTorch.LIT, true));
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        this.notifyNeighbors(world, blockposition, iblockdata);
    }

    private void notifyNeighbors(World world, BlockPosition blockposition, IBlockData iblockdata) {
        Orientation orientation = this.randomOrientation(world, iblockdata);

        for (EnumDirection enumdirection : EnumDirection.values()) {
            world.updateNeighborsAt(blockposition.relative(enumdirection), this, ExperimentalRedstoneUtils.withFront(orientation, enumdirection));
        }

    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        if (!flag) {
            this.notifyNeighbors(worldserver, blockposition, iblockdata);
        }

    }

    @Override
    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return (Boolean) iblockdata.getValue(BlockRedstoneTorch.LIT) && EnumDirection.UP != enumdirection ? 15 : 0;
    }

    protected boolean hasNeighborSignal(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return world.hasSignal(blockposition.below(), EnumDirection.DOWN);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        boolean flag = this.hasNeighborSignal(worldserver, blockposition, iblockdata);
        List<BlockRedstoneTorch.RedstoneUpdateInfo> list = (List) BlockRedstoneTorch.RECENT_TOGGLES.get(worldserver);

        while (list != null && !list.isEmpty() && worldserver.getGameTime() - ((BlockRedstoneTorch.RedstoneUpdateInfo) list.get(0)).when > 60L) {
            list.remove(0);
        }

        if ((Boolean) iblockdata.getValue(BlockRedstoneTorch.LIT)) {
            if (flag) {
                worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockRedstoneTorch.LIT, false), 3);
                if (isToggledTooFrequently(worldserver, blockposition, true)) {
                    worldserver.levelEvent(1502, blockposition, 0);
                    worldserver.scheduleTick(blockposition, worldserver.getBlockState(blockposition).getBlock(), 160);
                }
            }
        } else if (!flag && !isToggledTooFrequently(worldserver, blockposition, false)) {
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockRedstoneTorch.LIT, true), 3);
        }

    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if ((Boolean) iblockdata.getValue(BlockRedstoneTorch.LIT) == this.hasNeighborSignal(world, blockposition, iblockdata) && !world.getBlockTicks().willTickThisTick(blockposition, this)) {
            world.scheduleTick(blockposition, (Block) this, 2);
        }

    }

    @Override
    protected int getDirectSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return enumdirection == EnumDirection.DOWN ? iblockdata.getSignal(iblockaccess, blockposition, enumdirection) : 0;
    }

    @Override
    protected boolean isSignalSource(IBlockData iblockdata) {
        return true;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockRedstoneTorch.LIT)) {
            double d0 = (double) blockposition.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double) blockposition.getY() + 0.7D + (randomsource.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double) blockposition.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;

            world.addParticle(ParticleParamRedstone.REDSTONE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockRedstoneTorch.LIT);
    }

    private static boolean isToggledTooFrequently(World world, BlockPosition blockposition, boolean flag) {
        List<BlockRedstoneTorch.RedstoneUpdateInfo> list = (List) BlockRedstoneTorch.RECENT_TOGGLES.computeIfAbsent(world, (iblockaccess) -> {
            return Lists.newArrayList();
        });

        if (flag) {
            list.add(new BlockRedstoneTorch.RedstoneUpdateInfo(blockposition.immutable(), world.getGameTime()));
        }

        int i = 0;

        for (BlockRedstoneTorch.RedstoneUpdateInfo blockredstonetorch_redstoneupdateinfo : list) {
            if (blockredstonetorch_redstoneupdateinfo.pos.equals(blockposition)) {
                ++i;
                if (i >= 8) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    protected Orientation randomOrientation(World world, IBlockData iblockdata) {
        return ExperimentalRedstoneUtils.initialOrientation(world, (EnumDirection) null, EnumDirection.UP);
    }

    public static class RedstoneUpdateInfo {

        final BlockPosition pos;
        final long when;

        public RedstoneUpdateInfo(BlockPosition blockposition, long i) {
            this.pos = blockposition;
            this.when = i;
        }
    }
}
