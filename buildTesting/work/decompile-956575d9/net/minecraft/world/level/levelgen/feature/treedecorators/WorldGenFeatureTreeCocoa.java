package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BlockCocoa;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public class WorldGenFeatureTreeCocoa extends WorldGenFeatureTree {

    public static final MapCodec<WorldGenFeatureTreeCocoa> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(WorldGenFeatureTreeCocoa::new, (worldgenfeaturetreecocoa) -> {
        return worldgenfeaturetreecocoa.probability;
    });
    private final float probability;

    public WorldGenFeatureTreeCocoa(float f) {
        this.probability = f;
    }

    @Override
    protected WorldGenFeatureTrees<?> type() {
        return WorldGenFeatureTrees.COCOA;
    }

    @Override
    public void place(WorldGenFeatureTree.a worldgenfeaturetree_a) {
        RandomSource randomsource = worldgenfeaturetree_a.random();

        if (randomsource.nextFloat() < this.probability) {
            List<BlockPosition> list = worldgenfeaturetree_a.logs();

            if (!((List) list).isEmpty()) {
                int i = ((BlockPosition) list.getFirst()).getY();

                list.stream().filter((blockposition) -> {
                    return blockposition.getY() - i <= 2;
                }).forEach((blockposition) -> {
                    for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                        if (randomsource.nextFloat() <= 0.25F) {
                            EnumDirection enumdirection1 = enumdirection.getOpposite();
                            BlockPosition blockposition1 = blockposition.offset(enumdirection1.getStepX(), 0, enumdirection1.getStepZ());

                            if (worldgenfeaturetree_a.isAir(blockposition1)) {
                                worldgenfeaturetree_a.setBlock(blockposition1, (IBlockData) ((IBlockData) Blocks.COCOA.defaultBlockState().setValue(BlockCocoa.AGE, randomsource.nextInt(3))).setValue(BlockCocoa.FACING, enumdirection));
                            }
                        }
                    }

                });
            }
        }
    }
}
