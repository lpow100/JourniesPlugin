package net.minecraft.world.item;

import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class PlaceOnWaterBlockItem extends ItemBlock {

    public PlaceOnWaterBlockItem(Block block, Item.Info item_info) {
        super(block, item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        return EnumInteractionResult.PASS;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        MovingObjectPositionBlock movingobjectpositionblock = getPlayerPOVHitResult(world, entityhuman, RayTrace.FluidCollisionOption.SOURCE_ONLY);
        MovingObjectPositionBlock movingobjectpositionblock1 = movingobjectpositionblock.withPosition(movingobjectpositionblock.getBlockPos().above());

        return super.useOn(new ItemActionContext(entityhuman, enumhand, movingobjectpositionblock1));
    }
}
