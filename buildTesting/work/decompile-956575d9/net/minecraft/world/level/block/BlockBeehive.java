package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntityBee;
import net.minecraft.world.entity.boss.wither.EntityWither;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityWitherSkull;
import net.minecraft.world.entity.vehicle.EntityMinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockBeehive extends BlockTileEntity {

    public static final MapCodec<BlockBeehive> CODEC = simpleCodec(BlockBeehive::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateInteger HONEY_LEVEL = BlockProperties.LEVEL_HONEY;
    public static final int MAX_HONEY_LEVELS = 5;
    private static final int SHEARED_HONEYCOMB_COUNT = 3;

    @Override
    public MapCodec<BlockBeehive> codec() {
        return BlockBeehive.CODEC;
    }

    public BlockBeehive(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockBeehive.HONEY_LEVEL, 0)).setValue(BlockBeehive.FACING, EnumDirection.NORTH));
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return (Integer) iblockdata.getValue(BlockBeehive.HONEY_LEVEL);
    }

    @Override
    public void playerDestroy(World world, EntityHuman entityhuman, BlockPosition blockposition, IBlockData iblockdata, @Nullable TileEntity tileentity, ItemStack itemstack) {
        super.playerDestroy(world, entityhuman, blockposition, iblockdata, tileentity, itemstack);
        if (!world.isClientSide && tileentity instanceof TileEntityBeehive tileentitybeehive) {
            if (!EnchantmentManager.hasTag(itemstack, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING)) {
                tileentitybeehive.emptyAllLivingFromHive(entityhuman, iblockdata, TileEntityBeehive.ReleaseStatus.EMERGENCY);
                InventoryUtils.updateNeighboursAfterDestroy(iblockdata, world, blockposition);
                this.angerNearbyBees(world, blockposition);
            }

            CriterionTriggers.BEE_NEST_DESTROYED.trigger((EntityPlayer) entityhuman, iblockdata, itemstack, tileentitybeehive.getOccupantCount());
        }

    }

    @Override
    protected void onExplosionHit(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, Explosion explosion, BiConsumer<ItemStack, BlockPosition> biconsumer) {
        super.onExplosionHit(iblockdata, worldserver, blockposition, explosion, biconsumer);
        this.angerNearbyBees(worldserver, blockposition);
    }

    private void angerNearbyBees(World world, BlockPosition blockposition) {
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition)).inflate(8.0D, 6.0D, 8.0D);
        List<EntityBee> list = world.<EntityBee>getEntitiesOfClass(EntityBee.class, axisalignedbb);

        if (!list.isEmpty()) {
            List<EntityHuman> list1 = world.<EntityHuman>getEntitiesOfClass(EntityHuman.class, axisalignedbb);

            if (list1.isEmpty()) {
                return;
            }

            for (EntityBee entitybee : list) {
                if (entitybee.getTarget() == null) {
                    EntityHuman entityhuman = (EntityHuman) SystemUtils.getRandom(list1, world.random);

                    entitybee.setTarget(entityhuman);
                }
            }
        }

    }

    public static void dropHoneycomb(World world, BlockPosition blockposition) {
        popResource(world, blockposition, new ItemStack(Items.HONEYCOMB, 3));
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        int i = (Integer) iblockdata.getValue(BlockBeehive.HONEY_LEVEL);
        boolean flag = false;

        if (i >= 5) {
            Item item = itemstack.getItem();

            if (itemstack.is(Items.SHEARS)) {
                world.playSound(entityhuman, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                dropHoneycomb(world, blockposition);
                itemstack.hurtAndBreak(1, entityhuman, EntityLiving.getSlotForHand(enumhand));
                flag = true;
                world.gameEvent(entityhuman, (Holder) GameEvent.SHEAR, blockposition);
            } else if (itemstack.is(Items.GLASS_BOTTLE)) {
                itemstack.shrink(1);
                world.playSound(entityhuman, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (itemstack.isEmpty()) {
                    entityhuman.setItemInHand(enumhand, new ItemStack(Items.HONEY_BOTTLE));
                } else if (!entityhuman.getInventory().add(new ItemStack(Items.HONEY_BOTTLE))) {
                    entityhuman.drop(new ItemStack(Items.HONEY_BOTTLE), false);
                }

                flag = true;
                world.gameEvent(entityhuman, (Holder) GameEvent.FLUID_PICKUP, blockposition);
            }

            if (!world.isClientSide() && flag) {
                entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
            }
        }

        if (flag) {
            if (!BlockCampfire.isSmokeyPos(world, blockposition)) {
                if (this.hiveContainsBees(world, blockposition)) {
                    this.angerNearbyBees(world, blockposition);
                }

                this.releaseBeesAndResetHoneyLevel(world, iblockdata, blockposition, entityhuman, TileEntityBeehive.ReleaseStatus.EMERGENCY);
            } else {
                this.resetHoneyLevel(world, iblockdata, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            return super.useItemOn(itemstack, iblockdata, world, blockposition, entityhuman, enumhand, movingobjectpositionblock);
        }
    }

    private boolean hiveContainsBees(World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityBeehive tileentitybeehive) {
            return !tileentitybeehive.isEmpty();
        } else {
            return false;
        }
    }

    public void releaseBeesAndResetHoneyLevel(World world, IBlockData iblockdata, BlockPosition blockposition, @Nullable EntityHuman entityhuman, TileEntityBeehive.ReleaseStatus tileentitybeehive_releasestatus) {
        this.resetHoneyLevel(world, iblockdata, blockposition);
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityBeehive tileentitybeehive) {
            tileentitybeehive.emptyAllLivingFromHive(entityhuman, iblockdata, tileentitybeehive_releasestatus);
        }

    }

    public void resetHoneyLevel(World world, IBlockData iblockdata, BlockPosition blockposition) {
        world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockBeehive.HONEY_LEVEL, 0), 3);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if ((Integer) iblockdata.getValue(BlockBeehive.HONEY_LEVEL) >= 5) {
            for (int i = 0; i < randomsource.nextInt(1) + 1; ++i) {
                this.trySpawnDripParticles(world, blockposition, iblockdata);
            }
        }

    }

    private void trySpawnDripParticles(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (iblockdata.getFluidState().isEmpty() && world.random.nextFloat() >= 0.3F) {
            VoxelShape voxelshape = iblockdata.getCollisionShape(world, blockposition);
            double d0 = voxelshape.max(EnumDirection.EnumAxis.Y);

            if (d0 >= 1.0D && !iblockdata.is(TagsBlock.IMPERMEABLE)) {
                double d1 = voxelshape.min(EnumDirection.EnumAxis.Y);

                if (d1 > 0.0D) {
                    this.spawnParticle(world, blockposition, voxelshape, (double) blockposition.getY() + d1 - 0.05D);
                } else {
                    BlockPosition blockposition1 = blockposition.below();
                    IBlockData iblockdata1 = world.getBlockState(blockposition1);
                    VoxelShape voxelshape1 = iblockdata1.getCollisionShape(world, blockposition1);
                    double d2 = voxelshape1.max(EnumDirection.EnumAxis.Y);

                    if ((d2 < 1.0D || !iblockdata1.isCollisionShapeFullBlock(world, blockposition1)) && iblockdata1.getFluidState().isEmpty()) {
                        this.spawnParticle(world, blockposition, voxelshape, (double) blockposition.getY() - 0.05D);
                    }
                }
            }

        }
    }

    private void spawnParticle(World world, BlockPosition blockposition, VoxelShape voxelshape, double d0) {
        this.spawnFluidParticle(world, (double) blockposition.getX() + voxelshape.min(EnumDirection.EnumAxis.X), (double) blockposition.getX() + voxelshape.max(EnumDirection.EnumAxis.X), (double) blockposition.getZ() + voxelshape.min(EnumDirection.EnumAxis.Z), (double) blockposition.getZ() + voxelshape.max(EnumDirection.EnumAxis.Z), d0);
    }

    private void spawnFluidParticle(World world, double d0, double d1, double d2, double d3, double d4) {
        world.addParticle(Particles.DRIPPING_HONEY, MathHelper.lerp(world.random.nextDouble(), d0, d1), d4, MathHelper.lerp(world.random.nextDouble(), d2, d3), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockBeehive.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBeehive.HONEY_LEVEL, BlockBeehive.FACING);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityBeehive(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return world.isClientSide ? null : createTickerHelper(tileentitytypes, TileEntityTypes.BEEHIVE, TileEntityBeehive::serverTick);
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (world instanceof WorldServer worldserver) {
            if (entityhuman.preventsBlockDrops() && worldserver.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
                TileEntity tileentity = world.getBlockEntity(blockposition);

                if (tileentity instanceof TileEntityBeehive) {
                    TileEntityBeehive tileentitybeehive = (TileEntityBeehive) tileentity;
                    int i = (Integer) iblockdata.getValue(BlockBeehive.HONEY_LEVEL);
                    boolean flag = !tileentitybeehive.isEmpty();

                    if (flag || i > 0) {
                        ItemStack itemstack = new ItemStack(this);

                        itemstack.applyComponents(tileentitybeehive.collectComponents());
                        itemstack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(BlockBeehive.HONEY_LEVEL, i));
                        EntityItem entityitem = new EntityItem(world, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), itemstack);

                        entityitem.setDefaultPickUpDelay();
                        world.addFreshEntity(entityitem);
                    }
                }
            }
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    @Override
    protected List<ItemStack> getDrops(IBlockData iblockdata, LootParams.a lootparams_a) {
        Entity entity = (Entity) lootparams_a.getOptionalParameter(LootContextParameters.THIS_ENTITY);

        if (entity instanceof EntityTNTPrimed || entity instanceof EntityCreeper || entity instanceof EntityWitherSkull || entity instanceof EntityWither || entity instanceof EntityMinecartTNT) {
            TileEntity tileentity = (TileEntity) lootparams_a.getOptionalParameter(LootContextParameters.BLOCK_ENTITY);

            if (tileentity instanceof TileEntityBeehive) {
                TileEntityBeehive tileentitybeehive = (TileEntityBeehive) tileentity;

                tileentitybeehive.emptyAllLivingFromHive((EntityHuman) null, iblockdata, TileEntityBeehive.ReleaseStatus.EMERGENCY);
            }
        }

        return super.getDrops(iblockdata, lootparams_a);
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        ItemStack itemstack = super.getCloneItemStack(iworldreader, blockposition, iblockdata, flag);

        if (flag) {
            itemstack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(BlockBeehive.HONEY_LEVEL, (Integer) iblockdata.getValue(BlockBeehive.HONEY_LEVEL)));
        }

        return itemstack;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (iworldreader.getBlockState(blockposition1).getBlock() instanceof BlockFire) {
            TileEntity tileentity = iworldreader.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityBeehive) {
                TileEntityBeehive tileentitybeehive = (TileEntityBeehive) tileentity;

                tileentitybeehive.emptyAllLivingFromHive((EntityHuman) null, iblockdata, TileEntityBeehive.ReleaseStatus.EMERGENCY);
            }
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockBeehive.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockBeehive.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockBeehive.FACING)));
    }
}
