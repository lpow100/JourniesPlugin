package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.DismountUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class BlockRespawnAnchor extends Block {

    public static final MapCodec<BlockRespawnAnchor> CODEC = simpleCodec(BlockRespawnAnchor::new);
    public static final int MIN_CHARGES = 0;
    public static final int MAX_CHARGES = 4;
    public static final BlockStateInteger CHARGE = BlockProperties.RESPAWN_ANCHOR_CHARGES;
    private static final ImmutableList<BaseBlockPosition> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new BaseBlockPosition(0, 0, -1), new BaseBlockPosition(-1, 0, 0), new BaseBlockPosition(0, 0, 1), new BaseBlockPosition(1, 0, 0), new BaseBlockPosition(-1, 0, -1), new BaseBlockPosition(1, 0, -1), new BaseBlockPosition(-1, 0, 1), new BaseBlockPosition(1, 0, 1));
    private static final ImmutableList<BaseBlockPosition> RESPAWN_OFFSETS = (new Builder()).addAll(BlockRespawnAnchor.RESPAWN_HORIZONTAL_OFFSETS).addAll(BlockRespawnAnchor.RESPAWN_HORIZONTAL_OFFSETS.stream().map(BaseBlockPosition::below).iterator()).addAll(BlockRespawnAnchor.RESPAWN_HORIZONTAL_OFFSETS.stream().map(BaseBlockPosition::above).iterator()).add(new BaseBlockPosition(0, 1, 0)).build();

    @Override
    public MapCodec<BlockRespawnAnchor> codec() {
        return BlockRespawnAnchor.CODEC;
    }

    public BlockRespawnAnchor(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockRespawnAnchor.CHARGE, 0));
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (isRespawnFuel(itemstack) && canBeCharged(iblockdata)) {
            charge(entityhuman, world, blockposition, iblockdata);
            itemstack.consume(1, entityhuman);
            return EnumInteractionResult.SUCCESS;
        } else {
            return (EnumInteractionResult) (enumhand == EnumHand.MAIN_HAND && isRespawnFuel(entityhuman.getItemInHand(EnumHand.OFF_HAND)) && canBeCharged(iblockdata) ? EnumInteractionResult.PASS : EnumInteractionResult.TRY_WITH_EMPTY_HAND);
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if ((Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) == 0) {
            return EnumInteractionResult.PASS;
        } else if (!canSetSpawn(world)) {
            if (!world.isClientSide) {
                this.explode(iblockdata, world, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            if (entityhuman instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityhuman;
                EntityPlayer.RespawnConfig entityplayer_respawnconfig = entityplayer.getRespawnConfig();
                EntityPlayer.RespawnConfig entityplayer_respawnconfig1 = new EntityPlayer.RespawnConfig(world.dimension(), blockposition, 0.0F, false);

                if (entityplayer_respawnconfig == null || !entityplayer_respawnconfig.isSamePosition(entityplayer_respawnconfig1)) {
                    entityplayer.setRespawnPosition(entityplayer_respawnconfig1, true, org.bukkit.event.player.PlayerSpawnChangeEvent.Cause.RESPAWN_ANCHOR); // CraftBukkit
                    world.playSound((Entity) null, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, SoundEffects.RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return EnumInteractionResult.SUCCESS_SERVER;
                }
            }

            return EnumInteractionResult.CONSUME;
        }
    }

    private static boolean isRespawnFuel(ItemStack itemstack) {
        return itemstack.is(Items.GLOWSTONE);
    }

    private static boolean canBeCharged(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) < 4;
    }

    private static boolean isWaterThatWouldFlow(BlockPosition blockposition, World world) {
        Fluid fluid = world.getFluidState(blockposition);

        if (!fluid.is(TagsFluid.WATER)) {
            return false;
        } else if (fluid.isSource()) {
            return true;
        } else {
            float f = (float) fluid.getAmount();

            if (f < 2.0F) {
                return false;
            } else {
                Fluid fluid1 = world.getFluidState(blockposition.below());

                return !fluid1.is(TagsFluid.WATER);
            }
        }
    }

    private void explode(IBlockData iblockdata, World world, final BlockPosition blockposition) {
        org.bukkit.block.BlockState blockState = org.bukkit.craftbukkit.block.CraftBlock.at(world, blockposition).getState(); // CraftBukkit - capture BlockState before remove block
        world.removeBlock(blockposition, false);
        Stream<EnumDirection> stream = EnumDirection.EnumDirectionLimit.HORIZONTAL.stream(); // CraftBukkit - decompile error

        Objects.requireNonNull(blockposition);
        boolean flag = stream.map(blockposition::relative).anyMatch((blockposition1) -> {
            return isWaterThatWouldFlow(blockposition1, world);
        });
        final boolean flag1 = flag || world.getFluidState(blockposition.above()).is(TagsFluid.WATER);
        ExplosionDamageCalculator explosiondamagecalculator = new ExplosionDamageCalculator() {
            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition1, IBlockData iblockdata1, Fluid fluid) {
                return blockposition1.equals(blockposition) && flag1 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance(explosion, iblockaccess, blockposition1, iblockdata1, fluid);
            }
        };
        Vec3D vec3d = blockposition.getCenter();

        world.explode((Entity) null, world.damageSources().badRespawnPointExplosion(vec3d, blockState), explosiondamagecalculator, vec3d, 5.0F, true, World.a.BLOCK); // CraftBukkit - add state
    }

    public static boolean canSetSpawn(World world) {
        return world.dimensionType().respawnAnchorWorks();
    }

    public static void charge(@Nullable Entity entity, World world, BlockPosition blockposition, IBlockData iblockdata) {
        IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(BlockRespawnAnchor.CHARGE, (Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) + 1);

        world.setBlock(blockposition, iblockdata1, 3);
        world.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(entity, iblockdata1));
        world.playSound((Entity) null, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, SoundEffects.RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if ((Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) != 0) {
            if (randomsource.nextInt(100) == 0) {
                world.playLocalSound(blockposition, SoundEffects.RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            double d0 = (double) blockposition.getX() + 0.5D + (0.5D - randomsource.nextDouble());
            double d1 = (double) blockposition.getY() + 1.0D;
            double d2 = (double) blockposition.getZ() + 0.5D + (0.5D - randomsource.nextDouble());
            double d3 = (double) randomsource.nextFloat() * 0.04D;

            world.addParticle(Particles.REVERSE_PORTAL, d0, d1, d2, 0.0D, d3, 0.0D);
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockRespawnAnchor.CHARGE);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    public static int getScaledChargeLevel(IBlockData iblockdata, int i) {
        return MathHelper.floor((float) ((Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) - 0) / 4.0F * (float) i);
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return getScaledChargeLevel(iblockdata, 15);
    }

    public static Optional<Vec3D> findStandUpPosition(EntityTypes<?> entitytypes, ICollisionAccess icollisionaccess, BlockPosition blockposition) {
        Optional<Vec3D> optional = findStandUpPosition(entitytypes, icollisionaccess, blockposition, true);

        return optional.isPresent() ? optional : findStandUpPosition(entitytypes, icollisionaccess, blockposition, false);
    }

    private static Optional<Vec3D> findStandUpPosition(EntityTypes<?> entitytypes, ICollisionAccess icollisionaccess, BlockPosition blockposition, boolean flag) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        UnmodifiableIterator unmodifiableiterator = BlockRespawnAnchor.RESPAWN_OFFSETS.iterator();

        while (unmodifiableiterator.hasNext()) {
            BaseBlockPosition baseblockposition = (BaseBlockPosition) unmodifiableiterator.next();

            blockposition_mutableblockposition.set(blockposition).move(baseblockposition);
            Vec3D vec3d = DismountUtil.findSafeDismountLocation(entitytypes, icollisionaccess, blockposition_mutableblockposition, flag);

            if (vec3d != null) {
                return Optional.of(vec3d);
            }
        }

        return Optional.empty();
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
