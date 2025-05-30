package net.minecraft.world.level.levelgen.feature;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTorchWall;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;

public class WorldGenEndTrophy extends WorldGenerator<WorldGenFeatureEmptyConfiguration> {

    public static final int PODIUM_RADIUS = 4;
    public static final int PODIUM_PILLAR_HEIGHT = 4;
    public static final int RIM_RADIUS = 1;
    public static final float CORNER_ROUNDING = 0.5F;
    private static final BlockPosition END_PODIUM_LOCATION = BlockPosition.ZERO;
    private final boolean active;

    public static BlockPosition getLocation(BlockPosition blockposition) {
        return WorldGenEndTrophy.END_PODIUM_LOCATION.offset(blockposition);
    }

    public WorldGenEndTrophy(boolean flag) {
        super(WorldGenFeatureEmptyConfiguration.CODEC);
        this.active = flag;
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureEmptyConfiguration> featureplacecontext) {
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();

        for (BlockPosition blockposition1 : BlockPosition.betweenClosed(new BlockPosition(blockposition.getX() - 4, blockposition.getY() - 1, blockposition.getZ() - 4), new BlockPosition(blockposition.getX() + 4, blockposition.getY() + 32, blockposition.getZ() + 4))) {
            boolean flag = blockposition1.closerThan(blockposition, 2.5D);

            if (flag || blockposition1.closerThan(blockposition, 3.5D)) {
                if (blockposition1.getY() < blockposition.getY()) {
                    if (flag) {
                        this.setBlock(generatoraccessseed, blockposition1, Blocks.BEDROCK.defaultBlockState());
                    } else if (blockposition1.getY() < blockposition.getY()) {
                        if (this.active) {
                            this.dropPreviousAndSetBlock(generatoraccessseed, blockposition1, Blocks.END_STONE);
                        } else {
                            this.setBlock(generatoraccessseed, blockposition1, Blocks.END_STONE.defaultBlockState());
                        }
                    }
                } else if (blockposition1.getY() > blockposition.getY()) {
                    if (this.active) {
                        this.dropPreviousAndSetBlock(generatoraccessseed, blockposition1, Blocks.AIR);
                    } else {
                        this.setBlock(generatoraccessseed, blockposition1, Blocks.AIR.defaultBlockState());
                    }
                } else if (!flag) {
                    this.setBlock(generatoraccessseed, blockposition1, Blocks.BEDROCK.defaultBlockState());
                } else if (this.active) {
                    this.dropPreviousAndSetBlock(generatoraccessseed, new BlockPosition(blockposition1), Blocks.END_PORTAL);
                } else {
                    this.setBlock(generatoraccessseed, new BlockPosition(blockposition1), Blocks.AIR.defaultBlockState());
                }
            }
        }

        for (int i = 0; i < 4; ++i) {
            this.setBlock(generatoraccessseed, blockposition.above(i), Blocks.BEDROCK.defaultBlockState());
        }

        BlockPosition blockposition2 = blockposition.above(2);

        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            this.setBlock(generatoraccessseed, blockposition2.relative(enumdirection), (IBlockData) Blocks.WALL_TORCH.defaultBlockState().setValue(BlockTorchWall.FACING, enumdirection));
        }

        return true;
    }

    private void dropPreviousAndSetBlock(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition, Block block) {
        if (!generatoraccessseed.getBlockState(blockposition).is(block)) {
            generatoraccessseed.destroyBlock(blockposition, true, (Entity) null);
            this.setBlock(generatoraccessseed, blockposition, block.defaultBlockState());
        }

    }
}
