package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockChorusFlower extends Block {

    public static final MapCodec<BlockChorusFlower> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant").forGetter((blockchorusflower) -> {
            return blockchorusflower.plant;
        }), propertiesCodec()).apply(instance, BlockChorusFlower::new);
    });
    public static final int DEAD_AGE = 5;
    public static final BlockStateInteger AGE = BlockProperties.AGE_5;
    private static final VoxelShape SHAPE_BLOCK_SUPPORT = Block.column(14.0D, 0.0D, 15.0D);
    private final Block plant;

    @Override
    public MapCodec<BlockChorusFlower> codec() {
        return BlockChorusFlower.CODEC;
    }

    protected BlockChorusFlower(Block block, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.plant = block;
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockChorusFlower.AGE, 0));
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!iblockdata.canSurvive(worldserver, blockposition)) {
            worldserver.destroyBlock(blockposition, true);
        }

    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockChorusFlower.AGE) < 5;
    }

    @Override
    public VoxelShape getBlockSupportShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockChorusFlower.SHAPE_BLOCK_SUPPORT;
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        BlockPosition blockposition1 = blockposition.above();

        if (worldserver.isEmptyBlock(blockposition1) && blockposition1.getY() <= worldserver.getMaxY()) {
            int i = (Integer) iblockdata.getValue(BlockChorusFlower.AGE);

            if (i < 5) {
                boolean flag = false;
                boolean flag1 = false;
                IBlockData iblockdata1 = worldserver.getBlockState(blockposition.below());

                if (iblockdata1.is(Blocks.END_STONE)) {
                    flag = true;
                } else if (iblockdata1.is(this.plant)) {
                    int j = 1;

                    for (int k = 0; k < 4; ++k) {
                        IBlockData iblockdata2 = worldserver.getBlockState(blockposition.below(j + 1));

                        if (!iblockdata2.is(this.plant)) {
                            if (iblockdata2.is(Blocks.END_STONE)) {
                                flag1 = true;
                            }
                            break;
                        }

                        ++j;
                    }

                    if (j < 2 || j <= randomsource.nextInt(flag1 ? 5 : 4)) {
                        flag = true;
                    }
                } else if (iblockdata1.isAir()) {
                    flag = true;
                }

                if (flag && allNeighborsEmpty(worldserver, blockposition1, (EnumDirection) null) && worldserver.isEmptyBlock(blockposition.above(2))) {
                    // CraftBukkit start - add event
                    if (CraftEventFactory.handleBlockSpreadEvent(worldserver, blockposition, blockposition1, this.defaultBlockState().setValue(BlockChorusFlower.AGE, Integer.valueOf(i)), 2)) {
                        worldserver.setBlock(blockposition, BlockChorusFruit.getStateWithConnections(worldserver, blockposition, this.plant.defaultBlockState()), 2);
                        this.placeGrownFlower(worldserver, blockposition1, i);
                    }
                    // CraftBukkit end
                } else if (i < 4) {
                    int l = randomsource.nextInt(4);

                    if (flag1) {
                        ++l;
                    }

                    boolean flag2 = false;

                    for (int i1 = 0; i1 < l; ++i1) {
                        EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
                        BlockPosition blockposition2 = blockposition.relative(enumdirection);

                        if (worldserver.isEmptyBlock(blockposition2) && worldserver.isEmptyBlock(blockposition2.below()) && allNeighborsEmpty(worldserver, blockposition2, enumdirection.getOpposite())) {
                            // CraftBukkit start - add event
                            if (CraftEventFactory.handleBlockSpreadEvent(worldserver, blockposition, blockposition2, this.defaultBlockState().setValue(BlockChorusFlower.AGE, Integer.valueOf(i + 1)), 2)) {
                                this.placeGrownFlower(worldserver, blockposition2, i + 1);
                                flag2 = true;
                            }
                            // CraftBukkit end
                        }
                    }

                    if (flag2) {
                        worldserver.setBlock(blockposition, BlockChorusFruit.getStateWithConnections(worldserver, blockposition, this.plant.defaultBlockState()), 2);
                    } else {
                        // CraftBukkit start - add event
                        if (CraftEventFactory.handleBlockGrowEvent(worldserver, blockposition, this.defaultBlockState().setValue(BlockChorusFlower.AGE, Integer.valueOf(5)), 2)) {
                            this.placeDeadFlower(worldserver, blockposition);
                        }
                        // CraftBukkit end
                    }
                } else {
                    // CraftBukkit start - add event
                    if (CraftEventFactory.handleBlockGrowEvent(worldserver, blockposition, this.defaultBlockState().setValue(BlockChorusFlower.AGE, Integer.valueOf(5)), 2)) {
                        this.placeDeadFlower(worldserver, blockposition);
                    }
                    // CraftBukkit end
                }

            }
        }
    }

    private void placeGrownFlower(World world, BlockPosition blockposition, int i) {
        world.setBlock(blockposition, (IBlockData) this.defaultBlockState().setValue(BlockChorusFlower.AGE, i), 2);
        world.levelEvent(1033, blockposition, 0);
    }

    private void placeDeadFlower(World world, BlockPosition blockposition) {
        world.setBlock(blockposition, (IBlockData) this.defaultBlockState().setValue(BlockChorusFlower.AGE, 5), 2);
        world.levelEvent(1034, blockposition, 0);
    }

    private static boolean allNeighborsEmpty(IWorldReader iworldreader, BlockPosition blockposition, @Nullable EnumDirection enumdirection) {
        for (EnumDirection enumdirection1 : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            if (enumdirection1 != enumdirection && !iworldreader.isEmptyBlock(blockposition.relative(enumdirection1))) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (enumdirection != EnumDirection.UP && !iblockdata.canSurvive(iworldreader, blockposition)) {
            scheduledtickaccess.scheduleTick(blockposition, (Block) this, 1);
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.below());

        if (!iblockdata1.is(this.plant) && !iblockdata1.is(Blocks.END_STONE)) {
            if (!iblockdata1.isAir()) {
                return false;
            } else {
                boolean flag = false;

                for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                    IBlockData iblockdata2 = iworldreader.getBlockState(blockposition.relative(enumdirection));

                    if (iblockdata2.is(this.plant)) {
                        if (flag) {
                            return false;
                        }

                        flag = true;
                    } else if (!iblockdata2.isAir()) {
                        return false;
                    }
                }

                return flag;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockChorusFlower.AGE);
    }

    public static void generatePlant(GeneratorAccess generatoraccess, BlockPosition blockposition, RandomSource randomsource, int i) {
        generatoraccess.setBlock(blockposition, BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition, Blocks.CHORUS_PLANT.defaultBlockState()), 2);
        growTreeRecursive(generatoraccess, blockposition, randomsource, blockposition, i, 0);
    }

    private static void growTreeRecursive(GeneratorAccess generatoraccess, BlockPosition blockposition, RandomSource randomsource, BlockPosition blockposition1, int i, int j) {
        Block block = Blocks.CHORUS_PLANT;
        int k = randomsource.nextInt(4) + 1;

        if (j == 0) {
            ++k;
        }

        for (int l = 0; l < k; ++l) {
            BlockPosition blockposition2 = blockposition.above(l + 1);

            if (!allNeighborsEmpty(generatoraccess, blockposition2, (EnumDirection) null)) {
                return;
            }

            generatoraccess.setBlock(blockposition2, BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition2, block.defaultBlockState()), 2);
            generatoraccess.setBlock(blockposition2.below(), BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition2.below(), block.defaultBlockState()), 2);
        }

        boolean flag = false;

        if (j < 4) {
            int i1 = randomsource.nextInt(4);

            if (j == 0) {
                ++i1;
            }

            for (int j1 = 0; j1 < i1; ++j1) {
                EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
                BlockPosition blockposition3 = blockposition.above(k).relative(enumdirection);

                if (Math.abs(blockposition3.getX() - blockposition1.getX()) < i && Math.abs(blockposition3.getZ() - blockposition1.getZ()) < i && generatoraccess.isEmptyBlock(blockposition3) && generatoraccess.isEmptyBlock(blockposition3.below()) && allNeighborsEmpty(generatoraccess, blockposition3, enumdirection.getOpposite())) {
                    flag = true;
                    generatoraccess.setBlock(blockposition3, BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition3, block.defaultBlockState()), 2);
                    generatoraccess.setBlock(blockposition3.relative(enumdirection.getOpposite()), BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition3.relative(enumdirection.getOpposite()), block.defaultBlockState()), 2);
                    growTreeRecursive(generatoraccess, blockposition3, randomsource, blockposition1, i, j + 1);
                }
            }
        }

        if (!flag) {
            generatoraccess.setBlock(blockposition.above(k), (IBlockData) Blocks.CHORUS_FLOWER.defaultBlockState().setValue(BlockChorusFlower.AGE, 5), 2);
        }

    }

    @Override
    protected void onProjectileHit(World world, IBlockData iblockdata, MovingObjectPositionBlock movingobjectpositionblock, IProjectile iprojectile) {
        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

        if (world instanceof WorldServer worldserver) {
            if (iprojectile.mayInteract(worldserver, blockposition) && iprojectile.mayBreak(worldserver)) {
                // CraftBukkit
                if (!CraftEventFactory.callEntityChangeBlockEvent(iprojectile, blockposition, Blocks.AIR.defaultBlockState())) {
                    return;
                }
                // CraftBukkit end
                world.destroyBlock(blockposition, true, iprojectile);
            }
        }

    }
}
