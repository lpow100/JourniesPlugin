package net.minecraft.world.level.levelgen;

import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public final class OreVeinifier {

    private static final float VEININESS_THRESHOLD = 0.4F;
    private static final int EDGE_ROUNDOFF_BEGIN = 20;
    private static final double MAX_EDGE_ROUNDOFF = 0.2D;
    private static final float VEIN_SOLIDNESS = 0.7F;
    private static final float MIN_RICHNESS = 0.1F;
    private static final float MAX_RICHNESS = 0.3F;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
    private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;

    private OreVeinifier() {}

    protected static NoiseChunk.c create(DensityFunction densityfunction, DensityFunction densityfunction1, DensityFunction densityfunction2, PositionalRandomFactory positionalrandomfactory) {
        IBlockData iblockdata = null;

        return (densityfunction_b) -> {
            double d0 = densityfunction.compute(densityfunction_b);
            int i = densityfunction_b.blockY();
            OreVeinifier.a oreveinifier_a = d0 > 0.0D ? OreVeinifier.a.COPPER : OreVeinifier.a.IRON;
            double d1 = Math.abs(d0);
            int j = oreveinifier_a.maxY - i;
            int k = i - oreveinifier_a.minY;

            if (k >= 0 && j >= 0) {
                int l = Math.min(j, k);
                double d2 = MathHelper.clampedMap((double) l, 0.0D, 20.0D, -0.2D, 0.0D);

                if (d1 + d2 < (double) 0.4F) {
                    return iblockdata;
                } else {
                    RandomSource randomsource = positionalrandomfactory.at(densityfunction_b.blockX(), i, densityfunction_b.blockZ());

                    if (randomsource.nextFloat() > 0.7F) {
                        return iblockdata;
                    } else if (densityfunction1.compute(densityfunction_b) >= 0.0D) {
                        return iblockdata;
                    } else {
                        double d3 = MathHelper.clampedMap(d1, (double) 0.4F, (double) 0.6F, (double) 0.1F, (double) 0.3F);

                        return (double) randomsource.nextFloat() < d3 && densityfunction2.compute(densityfunction_b) > (double) -0.3F ? (randomsource.nextFloat() < 0.02F ? oreveinifier_a.rawOreBlock : oreveinifier_a.ore) : oreveinifier_a.filler;
                    }
                }
            } else {
                return iblockdata;
            }
        };
    }

    protected static enum a {

        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50), IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        final IBlockData ore;
        final IBlockData rawOreBlock;
        final IBlockData filler;
        protected final int minY;
        protected final int maxY;

        private a(final IBlockData iblockdata, final IBlockData iblockdata1, final IBlockData iblockdata2, final int i, final int j) {
            this.ore = iblockdata;
            this.rawOreBlock = iblockdata1;
            this.filler = iblockdata2;
            this.minY = i;
            this.maxY = j;
        }
    }
}
