package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;

public class CreakingHeartBlock extends BlockTileEntity {

    public static final MapCodec<CreakingHeartBlock> CODEC = simpleCodec(CreakingHeartBlock::new);
    public static final BlockStateEnum<EnumDirection.EnumAxis> AXIS = BlockProperties.AXIS;
    public static final BlockStateEnum<CreakingHeartState> STATE = BlockProperties.CREAKING_HEART_STATE;
    public static final BlockStateBoolean NATURAL = BlockProperties.NATURAL;

    @Override
    public MapCodec<CreakingHeartBlock> codec() {
        return CreakingHeartBlock.CODEC;
    }

    protected CreakingHeartBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(CreakingHeartBlock.AXIS, EnumDirection.EnumAxis.Y)).setValue(CreakingHeartBlock.STATE, CreakingHeartState.UPROOTED)).setValue(CreakingHeartBlock.NATURAL, false));
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new CreakingHeartBlockEntity(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return world.isClientSide ? null : (iblockdata.getValue(CreakingHeartBlock.STATE) != CreakingHeartState.UPROOTED ? createTickerHelper(tileentitytypes, TileEntityTypes.CREAKING_HEART, CreakingHeartBlockEntity::serverTick) : null);
    }

    public static boolean isNaturalNight(World world) {
        return world.isMoonVisible();
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (isNaturalNight(world)) {
            if (iblockdata.getValue(CreakingHeartBlock.STATE) != CreakingHeartState.UPROOTED) {
                if (randomsource.nextInt(16) == 0 && isSurroundedByLogs(world, blockposition)) {
                    world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.CREAKING_HEART_IDLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                }

            }
        }
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        scheduledtickaccess.scheduleTick(blockposition, (Block) this, 1);
        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        IBlockData iblockdata1 = updateState(iblockdata, worldserver, blockposition);

        if (iblockdata1 != iblockdata) {
            worldserver.setBlock(blockposition, iblockdata1, 3);
        }

    }

    private static IBlockData updateState(IBlockData iblockdata, World world, BlockPosition blockposition) {
        boolean flag = hasRequiredLogs(iblockdata, world, blockposition);
        boolean flag1 = iblockdata.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.UPROOTED;

        return flag && flag1 ? (IBlockData) iblockdata.setValue(CreakingHeartBlock.STATE, isNaturalNight(world) ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT) : iblockdata;
    }

    public static boolean hasRequiredLogs(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection.EnumAxis enumdirection_enumaxis = (EnumDirection.EnumAxis) iblockdata.getValue(CreakingHeartBlock.AXIS);

        for (EnumDirection enumdirection : enumdirection_enumaxis.getDirections()) {
            IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.relative(enumdirection));

            if (!iblockdata1.is(TagsBlock.PALE_OAK_LOGS) || iblockdata1.getValue(CreakingHeartBlock.AXIS) != enumdirection_enumaxis) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSurroundedByLogs(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        for (EnumDirection enumdirection : EnumDirection.values()) {
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            IBlockData iblockdata = generatoraccess.getBlockState(blockposition1);

            if (!iblockdata.is(TagsBlock.PALE_OAK_LOGS)) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return updateState((IBlockData) this.defaultBlockState().setValue(CreakingHeartBlock.AXIS, blockactioncontext.getClickedFace().getAxis()), blockactioncontext.getLevel(), blockactioncontext.getClickedPos());
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return BlockRotatable.rotatePillar(iblockdata, enumblockrotation);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(CreakingHeartBlock.AXIS, CreakingHeartBlock.STATE, CreakingHeartBlock.NATURAL);
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        InventoryUtils.updateNeighboursAfterDestroy(iblockdata, worldserver, blockposition);
    }

    @Override
    protected void onExplosionHit(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, Explosion explosion, BiConsumer<ItemStack, BlockPosition> biconsumer) {
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);

        if (tileentity instanceof CreakingHeartBlockEntity creakingheartblockentity) {
            if (explosion instanceof ServerExplosion serverexplosion) {
                if (explosion.getBlockInteraction().shouldAffectBlocklikeEntities()) {
                    creakingheartblockentity.removeProtector(serverexplosion.getDamageSource());
                    EntityLiving entityliving = explosion.getIndirectSourceEntity();

                    if (entityliving instanceof EntityHuman) {
                        EntityHuman entityhuman = (EntityHuman) entityliving;

                        if (explosion.getBlockInteraction().shouldAffectBlocklikeEntities()) {
                            this.tryAwardExperience(entityhuman, iblockdata, worldserver, blockposition);
                        }
                    }
                }
            }
        }

        super.onExplosionHit(iblockdata, worldserver, blockposition, explosion, biconsumer);
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof CreakingHeartBlockEntity creakingheartblockentity) {
            creakingheartblockentity.removeProtector(entityhuman.damageSources().playerAttack(entityhuman));
            this.tryAwardExperience(entityhuman, iblockdata, world, blockposition);
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    private void tryAwardExperience(EntityHuman entityhuman, IBlockData iblockdata, World world, BlockPosition blockposition) {
        if (!entityhuman.preventsBlockDrops() && !entityhuman.isSpectator() && (Boolean) iblockdata.getValue(CreakingHeartBlock.NATURAL) && world instanceof WorldServer worldserver) {
            this.popExperience(worldserver, blockposition, world.random.nextIntBetweenInclusive(20, 24));
        }

    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        if (iblockdata.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.UPROOTED) {
            return 0;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof CreakingHeartBlockEntity) {
                CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) tileentity;

                return creakingheartblockentity.getAnalogOutputSignal();
            } else {
                return 0;
            }
        }
    }
}
