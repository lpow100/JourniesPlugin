package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemSpade extends Item {

    protected static final Map<Block, IBlockData> FLATTENABLES = Maps.newHashMap((new Builder()).put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.DIRT, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.PODZOL, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.MYCELIUM, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.defaultBlockState()).build());

    public ItemSpade(ToolMaterial toolmaterial, float f, float f1, Item.Info item_info) {
        super(item_info.shovel(toolmaterial, f, f1));
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (itemactioncontext.getClickedFace() == EnumDirection.DOWN) {
            return EnumInteractionResult.PASS;
        } else {
            EntityHuman entityhuman = itemactioncontext.getPlayer();
            IBlockData iblockdata1 = (IBlockData) ItemSpade.FLATTENABLES.get(iblockdata.getBlock());
            IBlockData iblockdata2 = null;

            if (iblockdata1 != null && world.getBlockState(blockposition.above()).isAir()) {
                world.playSound(entityhuman, blockposition, SoundEffects.SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                iblockdata2 = iblockdata1;
            } else if (iblockdata.getBlock() instanceof BlockCampfire && (Boolean) iblockdata.getValue(BlockCampfire.LIT)) {
                if (!world.isClientSide()) {
                    world.levelEvent((Entity) null, 1009, blockposition, 0);
                }

                BlockCampfire.dowse(itemactioncontext.getPlayer(), world, blockposition, iblockdata);
                iblockdata2 = (IBlockData) iblockdata.setValue(BlockCampfire.LIT, false);
            }

            if (iblockdata2 != null) {
                if (!world.isClientSide) {
                    world.setBlock(blockposition, iblockdata2, 11);
                    world.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(entityhuman, iblockdata2));
                    if (entityhuman != null) {
                        itemactioncontext.getItemInHand().hurtAndBreak(1, entityhuman, EntityLiving.getSlotForHand(itemactioncontext.getHand()));
                    }
                }

                return EnumInteractionResult.SUCCESS;
            } else {
                return EnumInteractionResult.PASS;
            }
        }
    }
}
