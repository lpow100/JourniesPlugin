package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IFluidContainer;
import net.minecraft.world.level.block.IFluidSource;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypeFlowing;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class ItemBucket extends Item implements DispensibleContainerItem {

    public final FluidType content;

    public ItemBucket(FluidType fluidtype, Item.Info item_info) {
        super(item_info);
        this.content = fluidtype;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        MovingObjectPositionBlock movingobjectpositionblock = getPlayerPOVHitResult(world, entityhuman, this.content == FluidTypes.EMPTY ? RayTrace.FluidCollisionOption.SOURCE_ONLY : RayTrace.FluidCollisionOption.NONE);

        if (movingobjectpositionblock.getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
            return EnumInteractionResult.PASS;
        } else if (movingobjectpositionblock.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return EnumInteractionResult.PASS;
        } else {
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
            EnumDirection enumdirection = movingobjectpositionblock.getDirection();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (world.mayInteract(entityhuman, blockposition) && entityhuman.mayUseItemAt(blockposition1, enumdirection, itemstack)) {
                if (this.content == FluidTypes.EMPTY) {
                    IBlockData iblockdata = world.getBlockState(blockposition);
                    Block block = iblockdata.getBlock();

                    if (block instanceof IFluidSource) {
                        IFluidSource ifluidsource = (IFluidSource) block;
                        ItemStack itemstack1 = ifluidsource.pickupBlock(entityhuman, world, blockposition, iblockdata);

                        if (!itemstack1.isEmpty()) {
                            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                            ifluidsource.getPickupSound().ifPresent((soundeffect) -> {
                                entityhuman.playSound(soundeffect, 1.0F, 1.0F);
                            });
                            world.gameEvent(entityhuman, (Holder) GameEvent.FLUID_PICKUP, blockposition);
                            ItemStack itemstack2 = ItemLiquidUtil.createFilledResult(itemstack, entityhuman, itemstack1);

                            if (!world.isClientSide) {
                                CriterionTriggers.FILLED_BUCKET.trigger((EntityPlayer) entityhuman, itemstack1);
                            }

                            return EnumInteractionResult.SUCCESS.heldItemTransformedTo(itemstack2);
                        }
                    }

                    return EnumInteractionResult.FAIL;
                } else {
                    IBlockData iblockdata1 = world.getBlockState(blockposition);
                    BlockPosition blockposition2 = iblockdata1.getBlock() instanceof IFluidContainer && this.content == FluidTypes.WATER ? blockposition : blockposition1;

                    if (this.emptyContents(entityhuman, world, blockposition2, movingobjectpositionblock)) {
                        this.checkExtraContent(entityhuman, world, itemstack, blockposition2);
                        if (entityhuman instanceof EntityPlayer) {
                            CriterionTriggers.PLACED_BLOCK.trigger((EntityPlayer) entityhuman, blockposition2, itemstack);
                        }

                        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                        ItemStack itemstack3 = ItemLiquidUtil.createFilledResult(itemstack, entityhuman, getEmptySuccessItem(itemstack, entityhuman));

                        return EnumInteractionResult.SUCCESS.heldItemTransformedTo(itemstack3);
                    } else {
                        return EnumInteractionResult.FAIL;
                    }
                }
            } else {
                return EnumInteractionResult.FAIL;
            }
        }
    }

    public static ItemStack getEmptySuccessItem(ItemStack itemstack, EntityHuman entityhuman) {
        return !entityhuman.hasInfiniteMaterials() ? new ItemStack(Items.BUCKET) : itemstack;
    }

    @Override
    public void checkExtraContent(@Nullable EntityLiving entityliving, World world, ItemStack itemstack, BlockPosition blockposition) {}

    @Override
    public boolean emptyContents(@Nullable EntityLiving entityliving, World world, BlockPosition blockposition, @Nullable MovingObjectPositionBlock movingobjectpositionblock) {
        FluidType fluidtype = this.content;

        if (!(fluidtype instanceof FluidTypeFlowing fluidtypeflowing)) {
            return false;
        } else {
            Block block;
            boolean flag;
            boolean flag1;
            label82:
            {
                iblockdata = world.getBlockState(blockposition);
                block = iblockdata.getBlock();
                flag = iblockdata.canBeReplaced(this.content);
                if (!iblockdata.isAir() && !flag) {
                    label80:
                    {
                        if (block instanceof IFluidContainer) {
                            IFluidContainer ifluidcontainer = (IFluidContainer) block;

                            if (ifluidcontainer.canPlaceLiquid(entityliving, world, blockposition, iblockdata, this.content)) {
                                break label80;
                            }
                        }

                        flag1 = false;
                        break label82;
                    }
                }

                flag1 = true;
            }

            boolean flag2 = flag1;

            if (!flag2) {
                return movingobjectpositionblock != null && this.emptyContents(entityliving, world, movingobjectpositionblock.getBlockPos().relative(movingobjectpositionblock.getDirection()), (MovingObjectPositionBlock) null);
            } else if (world.dimensionType().ultraWarm() && this.content.is(TagsFluid.WATER)) {
                int i = blockposition.getX();
                int j = blockposition.getY();
                int k = blockposition.getZ();

                world.playSound(entityliving, blockposition, SoundEffects.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

                for (int l = 0; l < 8; ++l) {
                    world.addParticle(Particles.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0D, 0.0D, 0.0D);
                }

                return true;
            } else {
                if (block instanceof IFluidContainer) {
                    IFluidContainer ifluidcontainer1 = (IFluidContainer) block;

                    if (this.content == FluidTypes.WATER) {
                        ifluidcontainer1.placeLiquid(world, blockposition, iblockdata, fluidtypeflowing.getSource(false));
                        this.playEmptySound(entityliving, world, blockposition);
                        return true;
                    }
                }

                if (!world.isClientSide && flag && !iblockdata.liquid()) {
                    world.destroyBlock(blockposition, true);
                }

                if (!world.setBlock(blockposition, this.content.defaultFluidState().createLegacyBlock(), 11) && !iblockdata.getFluidState().isSource()) {
                    return false;
                } else {
                    this.playEmptySound(entityliving, world, blockposition);
                    return true;
                }
            }
        }
    }

    protected void playEmptySound(@Nullable EntityLiving entityliving, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        SoundEffect soundeffect = this.content.is(TagsFluid.LAVA) ? SoundEffects.BUCKET_EMPTY_LAVA : SoundEffects.BUCKET_EMPTY;

        generatoraccess.playSound(entityliving, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
        generatoraccess.gameEvent(entityliving, (Holder) GameEvent.FLUID_PLACE, blockposition);
    }
}
