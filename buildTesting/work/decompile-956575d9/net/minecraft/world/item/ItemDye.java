package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.sheep.EntitySheep;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntitySign;

public class ItemDye extends Item implements SignApplicator {

    private static final Map<EnumColor, ItemDye> ITEM_BY_COLOR = Maps.newEnumMap(EnumColor.class);
    private final EnumColor dyeColor;

    public ItemDye(EnumColor enumcolor, Item.Info item_info) {
        super(item_info);
        this.dyeColor = enumcolor;
        ItemDye.ITEM_BY_COLOR.put(enumcolor, this);
    }

    @Override
    public EnumInteractionResult interactLivingEntity(ItemStack itemstack, EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        if (entityliving instanceof EntitySheep entitysheep) {
            if (entitysheep.isAlive() && !entitysheep.isSheared() && entitysheep.getColor() != this.dyeColor) {
                entitysheep.level().playSound(entityhuman, (Entity) entitysheep, SoundEffects.DYE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (!entityhuman.level().isClientSide) {
                    entitysheep.setColor(this.dyeColor);
                    itemstack.shrink(1);
                }

                return EnumInteractionResult.SUCCESS;
            }
        }

        return EnumInteractionResult.PASS;
    }

    public EnumColor getDyeColor() {
        return this.dyeColor;
    }

    public static ItemDye byColor(EnumColor enumcolor) {
        return (ItemDye) ItemDye.ITEM_BY_COLOR.get(enumcolor);
    }

    @Override
    public boolean tryApplyToSign(World world, TileEntitySign tileentitysign, boolean flag, EntityHuman entityhuman) {
        if (tileentitysign.updateText((signtext) -> {
            return signtext.setColor(this.getDyeColor());
        }, flag)) {
            world.playSound((Entity) null, tileentitysign.getBlockPos(), SoundEffects.DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}
