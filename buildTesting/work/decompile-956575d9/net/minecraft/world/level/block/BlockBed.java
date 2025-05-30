package net.minecraft.world.level.block;

import com.mojang.math.PointGroupO;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.DismountUtil;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBed;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyBedPart;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;
import org.apache.commons.lang3.ArrayUtils;

public class BlockBed extends BlockFacingHorizontal implements ITileEntity {

    public static final MapCodec<BlockBed> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnumColor.CODEC.fieldOf("color").forGetter(BlockBed::getColor), propertiesCodec()).apply(instance, BlockBed::new);
    });
    public static final BlockStateEnum<BlockPropertyBedPart> PART = BlockProperties.BED_PART;
    public static final BlockStateBoolean OCCUPIED = BlockProperties.OCCUPIED;
    private static final Map<EnumDirection, VoxelShape> SHAPES = (Map) SystemUtils.make(() -> {
        VoxelShape voxelshape = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 3.0D, 3.0D);
        VoxelShape voxelshape1 = VoxelShapes.rotate(voxelshape, PointGroupO.fromXYAngles(Quadrant.R0, Quadrant.R90));

        return VoxelShapes.rotateHorizontal(VoxelShapes.or(Block.column(16.0D, 3.0D, 9.0D), voxelshape, voxelshape1));
    });
    private final EnumColor color;

    @Override
    public MapCodec<BlockBed> codec() {
        return BlockBed.CODEC;
    }

    public BlockBed(EnumColor enumcolor, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.color = enumcolor;
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockBed.PART, BlockPropertyBedPart.FOOT)).setValue(BlockBed.OCCUPIED, false));
    }

    @Nullable
    public static EnumDirection getBedOrientation(IBlockAccess iblockaccess, BlockPosition blockposition) {
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition);

        return iblockdata.getBlock() instanceof BlockBed ? (EnumDirection) iblockdata.getValue(BlockBed.FACING) : null;
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS_SERVER;
        } else {
            if (iblockdata.getValue(BlockBed.PART) != BlockPropertyBedPart.HEAD) {
                blockposition = blockposition.relative((EnumDirection) iblockdata.getValue(BlockBed.FACING));
                iblockdata = world.getBlockState(blockposition);
                if (!iblockdata.is(this)) {
                    return EnumInteractionResult.CONSUME;
                }
            }

            if (!canSetSpawn(world)) {
                world.removeBlock(blockposition, false);
                BlockPosition blockposition1 = blockposition.relative(((EnumDirection) iblockdata.getValue(BlockBed.FACING)).getOpposite());

                if (world.getBlockState(blockposition1).is(this)) {
                    world.removeBlock(blockposition1, false);
                }

                Vec3D vec3d = blockposition.getCenter();

                world.explode((Entity) null, world.damageSources().badRespawnPointExplosion(vec3d), (ExplosionDamageCalculator) null, vec3d, 5.0F, true, World.a.BLOCK);
                return EnumInteractionResult.SUCCESS_SERVER;
            } else if ((Boolean) iblockdata.getValue(BlockBed.OCCUPIED)) {
                if (!this.kickVillagerOutOfBed(world, blockposition)) {
                    entityhuman.displayClientMessage(IChatBaseComponent.translatable("block.minecraft.bed.occupied"), true);
                }

                return EnumInteractionResult.SUCCESS_SERVER;
            } else {
                entityhuman.startSleepInBed(blockposition).ifLeft((entityhuman_enumbedresult) -> {
                    if (entityhuman_enumbedresult.getMessage() != null) {
                        entityhuman.displayClientMessage(entityhuman_enumbedresult.getMessage(), true);
                    }

                });
                return EnumInteractionResult.SUCCESS_SERVER;
            }
        }
    }

    public static boolean canSetSpawn(World world) {
        return world.dimensionType().bedWorks();
    }

    private boolean kickVillagerOutOfBed(World world, BlockPosition blockposition) {
        List<EntityVillager> list = world.<EntityVillager>getEntitiesOfClass(EntityVillager.class, new AxisAlignedBB(blockposition), EntityLiving::isSleeping);

        if (list.isEmpty()) {
            return false;
        } else {
            ((EntityVillager) list.get(0)).stopSleeping();
            return true;
        }
    }

    @Override
    public void fallOn(World world, IBlockData iblockdata, BlockPosition blockposition, Entity entity, double d0) {
        super.fallOn(world, iblockdata, blockposition, entity, d0 * 0.5D);
    }

    @Override
    public void updateEntityMovementAfterFallOn(IBlockAccess iblockaccess, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(iblockaccess, entity);
        } else {
            this.bounceUp(entity);
        }

    }

    private void bounceUp(Entity entity) {
        Vec3D vec3d = entity.getDeltaMovement();

        if (vec3d.y < 0.0D) {
            double d0 = entity instanceof EntityLiving ? 1.0D : 0.8D;

            entity.setDeltaMovement(vec3d.x, -vec3d.y * (double) 0.66F * d0, vec3d.z);
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return enumdirection == getNeighbourDirection((BlockPropertyBedPart) iblockdata.getValue(BlockBed.PART), (EnumDirection) iblockdata.getValue(BlockBed.FACING)) ? (iblockdata1.is(this) && iblockdata1.getValue(BlockBed.PART) != iblockdata.getValue(BlockBed.PART) ? (IBlockData) iblockdata.setValue(BlockBed.OCCUPIED, (Boolean) iblockdata1.getValue(BlockBed.OCCUPIED)) : Blocks.AIR.defaultBlockState()) : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    private static EnumDirection getNeighbourDirection(BlockPropertyBedPart blockpropertybedpart, EnumDirection enumdirection) {
        return blockpropertybedpart == BlockPropertyBedPart.FOOT ? enumdirection : enumdirection.getOpposite();
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (!world.isClientSide && entityhuman.preventsBlockDrops()) {
            BlockPropertyBedPart blockpropertybedpart = (BlockPropertyBedPart) iblockdata.getValue(BlockBed.PART);

            if (blockpropertybedpart == BlockPropertyBedPart.FOOT) {
                BlockPosition blockposition1 = blockposition.relative(getNeighbourDirection(blockpropertybedpart, (EnumDirection) iblockdata.getValue(BlockBed.FACING)));
                IBlockData iblockdata1 = world.getBlockState(blockposition1);

                if (iblockdata1.is(this) && iblockdata1.getValue(BlockBed.PART) == BlockPropertyBedPart.HEAD) {
                    world.setBlock(blockposition1, Blocks.AIR.defaultBlockState(), 35);
                    world.levelEvent(entityhuman, 2001, blockposition1, Block.getId(iblockdata1));
                }
            }
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        EnumDirection enumdirection = blockactioncontext.getHorizontalDirection();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        BlockPosition blockposition1 = blockposition.relative(enumdirection);
        World world = blockactioncontext.getLevel();

        return world.getBlockState(blockposition1).canBeReplaced(blockactioncontext) && world.getWorldBorder().isWithinBounds(blockposition1) ? (IBlockData) this.defaultBlockState().setValue(BlockBed.FACING, enumdirection) : null;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) BlockBed.SHAPES.get(getConnectedDirection(iblockdata).getOpposite());
    }

    public static EnumDirection getConnectedDirection(IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockBed.FACING);

        return iblockdata.getValue(BlockBed.PART) == BlockPropertyBedPart.HEAD ? enumdirection.getOpposite() : enumdirection;
    }

    public static DoubleBlockFinder.BlockType getBlockType(IBlockData iblockdata) {
        BlockPropertyBedPart blockpropertybedpart = (BlockPropertyBedPart) iblockdata.getValue(BlockBed.PART);

        return blockpropertybedpart == BlockPropertyBedPart.HEAD ? DoubleBlockFinder.BlockType.FIRST : DoubleBlockFinder.BlockType.SECOND;
    }

    private static boolean isBunkBed(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockaccess.getBlockState(blockposition.below()).getBlock() instanceof BlockBed;
    }

    public static Optional<Vec3D> findStandUpPosition(EntityTypes<?> entitytypes, ICollisionAccess icollisionaccess, BlockPosition blockposition, EnumDirection enumdirection, float f) {
        EnumDirection enumdirection1 = enumdirection.getClockWise();
        EnumDirection enumdirection2 = enumdirection1.isFacingAngle(f) ? enumdirection1.getOpposite() : enumdirection1;

        if (isBunkBed(icollisionaccess, blockposition)) {
            return findBunkBedStandUpPosition(entitytypes, icollisionaccess, blockposition, enumdirection, enumdirection2);
        } else {
            int[][] aint = bedStandUpOffsets(enumdirection, enumdirection2);
            Optional<Vec3D> optional = findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition, aint, true);

            return optional.isPresent() ? optional : findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition, aint, false);
        }
    }

    private static Optional<Vec3D> findBunkBedStandUpPosition(EntityTypes<?> entitytypes, ICollisionAccess icollisionaccess, BlockPosition blockposition, EnumDirection enumdirection, EnumDirection enumdirection1) {
        int[][] aint = bedSurroundStandUpOffsets(enumdirection, enumdirection1);
        Optional<Vec3D> optional = findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition, aint, true);

        if (optional.isPresent()) {
            return optional;
        } else {
            BlockPosition blockposition1 = blockposition.below();
            Optional<Vec3D> optional1 = findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition1, aint, true);

            if (optional1.isPresent()) {
                return optional1;
            } else {
                int[][] aint1 = bedAboveStandUpOffsets(enumdirection);
                Optional<Vec3D> optional2 = findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition, aint1, true);

                if (optional2.isPresent()) {
                    return optional2;
                } else {
                    Optional<Vec3D> optional3 = findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition, aint, false);

                    if (optional3.isPresent()) {
                        return optional3;
                    } else {
                        Optional<Vec3D> optional4 = findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition1, aint, false);

                        return optional4.isPresent() ? optional4 : findStandUpPositionAtOffset(entitytypes, icollisionaccess, blockposition, aint1, false);
                    }
                }
            }
        }
    }

    private static Optional<Vec3D> findStandUpPositionAtOffset(EntityTypes<?> entitytypes, ICollisionAccess icollisionaccess, BlockPosition blockposition, int[][] aint, boolean flag) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (int[] aint1 : aint) {
            blockposition_mutableblockposition.set(blockposition.getX() + aint1[0], blockposition.getY(), blockposition.getZ() + aint1[1]);
            Vec3D vec3d = DismountUtil.findSafeDismountLocation(entitytypes, icollisionaccess, blockposition_mutableblockposition, flag);

            if (vec3d != null) {
                return Optional.of(vec3d);
            }
        }

        return Optional.empty();
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBed.FACING, BlockBed.PART, BlockBed.OCCUPIED);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityBed(blockposition, iblockdata, this.color);
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        super.setPlacedBy(world, blockposition, iblockdata, entityliving, itemstack);
        if (!world.isClientSide) {
            BlockPosition blockposition1 = blockposition.relative((EnumDirection) iblockdata.getValue(BlockBed.FACING));

            world.setBlock(blockposition1, (IBlockData) iblockdata.setValue(BlockBed.PART, BlockPropertyBedPart.HEAD), 3);
            world.updateNeighborsAt(blockposition, Blocks.AIR);
            iblockdata.updateNeighbourShapes(world, blockposition, 3);
        }

    }

    public EnumColor getColor() {
        return this.color;
    }

    @Override
    protected long getSeed(IBlockData iblockdata, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.relative((EnumDirection) iblockdata.getValue(BlockBed.FACING), iblockdata.getValue(BlockBed.PART) == BlockPropertyBedPart.HEAD ? 0 : 1);

        return MathHelper.getSeed(blockposition1.getX(), blockposition.getY(), blockposition1.getZ());
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    private static int[][] bedStandUpOffsets(EnumDirection enumdirection, EnumDirection enumdirection1) {
        return (int[][]) ArrayUtils.addAll(bedSurroundStandUpOffsets(enumdirection, enumdirection1), bedAboveStandUpOffsets(enumdirection));
    }

    private static int[][] bedSurroundStandUpOffsets(EnumDirection enumdirection, EnumDirection enumdirection1) {
        return new int[][]{{enumdirection1.getStepX(), enumdirection1.getStepZ()}, {enumdirection1.getStepX() - enumdirection.getStepX(), enumdirection1.getStepZ() - enumdirection.getStepZ()}, {enumdirection1.getStepX() - enumdirection.getStepX() * 2, enumdirection1.getStepZ() - enumdirection.getStepZ() * 2}, {-enumdirection.getStepX() * 2, -enumdirection.getStepZ() * 2}, {-enumdirection1.getStepX() - enumdirection.getStepX() * 2, -enumdirection1.getStepZ() - enumdirection.getStepZ() * 2}, {-enumdirection1.getStepX() - enumdirection.getStepX(), -enumdirection1.getStepZ() - enumdirection.getStepZ()}, {-enumdirection1.getStepX(), -enumdirection1.getStepZ()}, {-enumdirection1.getStepX() + enumdirection.getStepX(), -enumdirection1.getStepZ() + enumdirection.getStepZ()}, {enumdirection.getStepX(), enumdirection.getStepZ()}, {enumdirection1.getStepX() + enumdirection.getStepX(), enumdirection1.getStepZ() + enumdirection.getStepZ()}};
    }

    private static int[][] bedAboveStandUpOffsets(EnumDirection enumdirection) {
        return new int[][]{{0, 0}, {-enumdirection.getStepX(), -enumdirection.getStepZ()}};
    }
}
