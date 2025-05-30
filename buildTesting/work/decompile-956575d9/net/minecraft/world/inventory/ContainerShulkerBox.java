package net.minecraft.world.inventory;

import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;

public class ContainerShulkerBox extends Container {

    private static final int CONTAINER_SIZE = 27;
    private final IInventory container;

    public ContainerShulkerBox(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, new InventorySubcontainer(27));
    }

    public ContainerShulkerBox(int i, PlayerInventory playerinventory, IInventory iinventory) {
        super(Containers.SHULKER_BOX, i);
        checkContainerSize(iinventory, 27);
        this.container = iinventory;
        iinventory.startOpen(playerinventory.player);
        int j = 3;
        int k = 9;

        for (int l = 0; l < 3; ++l) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new SlotShulkerBox(iinventory, i1 + l * 9, 8 + i1 * 18, 18 + l * 18));
            }
        }

        this.addStandardInventorySlots(playerinventory, 8, 84);
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return this.container.stillValid(entityhuman);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.container.stopOpen(entityhuman);
    }
}
