package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockBeetroot extends BlockCrops {

    public static final MapCodec<BlockBeetroot> CODEC = simpleCodec(BlockBeetroot::new);
    public static final int MAX_AGE = 3;
    public static final BlockStateInteger AGE = BlockProperties.AGE_3;
    private static final VoxelShape[] SHAPES = Block.boxes(3, (i) -> {
        return Block.column(16.0D, 0.0D, (double) (2 + i * 2));
    });

    @Override
    public MapCodec<BlockBeetroot> codec() {
        return BlockBeetroot.CODEC;
    }

    public BlockBeetroot(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected BlockStateInteger getAgeProperty() {
        return BlockBeetroot.AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected IMaterial getBaseSeedId() {
        return Items.BEETROOT_SEEDS;
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(3) != 0) {
            super.randomTick(iblockdata, worldserver, blockposition, randomsource);
        }

    }

    @Override
    protected int getBonemealAgeIncrease(World world) {
        return super.getBonemealAgeIncrease(world) / 3;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBeetroot.AGE);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockBeetroot.SHAPES[this.getAge(iblockdata)];
    }
}
