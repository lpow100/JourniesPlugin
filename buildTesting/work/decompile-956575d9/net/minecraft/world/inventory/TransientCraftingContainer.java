package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;

public class TransientCraftingContainer implements InventoryCrafting {

    private final NonNullList<ItemStack> items;
    private final int width;
    private final int height;
    private final Container menu;

    public TransientCraftingContainer(Container container, int i, int j) {
        this(container, i, j, NonNullList.withSize(i * j, ItemStack.EMPTY));
    }

    private TransientCraftingContainer(Container container, int i, int j, NonNullList<ItemStack> nonnulllist) {
        this.items = nonnulllist;
        this.menu = container;
        this.width = i;
        this.height = j;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        return i >= this.getContainerSize() ? ItemStack.EMPTY : (ItemStack) this.items.get(i);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerUtil.takeItem(this.items, i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemstack = ContainerUtil.removeItem(this.items, i, j);

        if (!itemstack.isEmpty()) {
            this.menu.slotsChanged(this);
        }

        return itemstack;
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.items.set(i, itemstack);
        this.menu.slotsChanged(this);
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public List<ItemStack> getItems() {
        return List.copyOf(this.items);
    }

    @Override
    public void fillStackedContents(StackedItemContents stackeditemcontents) {
        for (ItemStack itemstack : this.items) {
            stackeditemcontents.accountSimpleStack(itemstack);
        }

    }
}
