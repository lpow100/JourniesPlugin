package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentManager;

class ArmorSlot extends Slot {

    private final EntityLiving owner;
    private final EnumItemSlot slot;
    @Nullable
    private final MinecraftKey emptyIcon;

    public ArmorSlot(IInventory iinventory, EntityLiving entityliving, EnumItemSlot enumitemslot, int i, int j, int k, @Nullable MinecraftKey minecraftkey) {
        super(iinventory, i, j, k);
        this.owner = entityliving;
        this.slot = enumitemslot;
        this.emptyIcon = minecraftkey;
    }

    @Override
    public void setByPlayer(ItemStack itemstack, ItemStack itemstack1) {
        this.owner.onEquipItem(this.slot, itemstack1, itemstack);
        super.setByPlayer(itemstack, itemstack1);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack itemstack) {
        return this.owner.isEquippableInSlot(itemstack, this.slot);
    }

    @Override
    public boolean isActive() {
        return this.owner.canUseSlot(this.slot);
    }

    @Override
    public boolean mayPickup(EntityHuman entityhuman) {
        ItemStack itemstack = this.getItem();

        return !itemstack.isEmpty() && !entityhuman.isCreative() && EnchantmentManager.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ? false : super.mayPickup(entityhuman);
    }

    @Nullable
    @Override
    public MinecraftKey getNoItemIcon() {
        return this.emptyIcon;
    }
}
