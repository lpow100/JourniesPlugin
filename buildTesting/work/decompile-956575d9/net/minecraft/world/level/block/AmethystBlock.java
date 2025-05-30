package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class AmethystBlock extends Block {

    public static final MapCodec<AmethystBlock> CODEC = simpleCodec(AmethystBlock::new);

    @Override
    public MapCodec<? extends AmethystBlock> codec() {
        return AmethystBlock.CODEC;
    }

    public AmethystBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected void onProjectileHit(World world, IBlockData iblockdata, MovingObjectPositionBlock movingobjectpositionblock, IProjectile iprojectile) {
        if (!world.isClientSide) {
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

            world.playSound((Entity) null, blockposition, SoundEffects.AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
        }

    }
}
