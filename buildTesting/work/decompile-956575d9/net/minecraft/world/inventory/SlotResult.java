package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeCrafting;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.level.World;

public class SlotResult extends Slot {

    private final InventoryCrafting craftSlots;
    private final EntityHuman player;
    private int removeCount;

    public SlotResult(EntityHuman entityhuman, InventoryCrafting inventorycrafting, IInventory iinventory, int i, int j, int k) {
        super(iinventory, i, j, k);
        this.player = entityhuman;
        this.craftSlots = inventorycrafting;
    }

    @Override
    public boolean mayPlace(ItemStack itemstack) {
        return false;
    }

    @Override
    public ItemStack remove(int i) {
        if (this.hasItem()) {
            this.removeCount += Math.min(i, this.getItem().getCount());
        }

        return super.remove(i);
    }

    @Override
    protected void onQuickCraft(ItemStack itemstack, int i) {
        this.removeCount += i;
        this.checkTakeAchievements(itemstack);
    }

    @Override
    protected void onSwapCraft(int i) {
        this.removeCount += i;
    }

    @Override
    protected void checkTakeAchievements(ItemStack itemstack) {
        if (this.removeCount > 0) {
            itemstack.onCraftedBy(this.player, this.removeCount);
        }

        IInventory iinventory = this.container;

        if (iinventory instanceof RecipeCraftingHolder recipecraftingholder) {
            recipecraftingholder.awardUsedRecipes(this.player, this.craftSlots.getItems());
        }

        this.removeCount = 0;
    }

    private static NonNullList<ItemStack> copyAllInputItems(CraftingInput craftinginput) {
        NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(craftinginput.size(), ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            nonnulllist.set(i, craftinginput.getItem(i));
        }

        return nonnulllist;
    }

    private NonNullList<ItemStack> getRemainingItems(CraftingInput craftinginput, World world) {
        if (world instanceof WorldServer worldserver) {
            return (NonNullList) worldserver.recipeAccess().getRecipeFor(Recipes.CRAFTING, craftinginput, worldserver).map((recipeholder) -> {
                return ((RecipeCrafting) recipeholder.value()).getRemainingItems(craftinginput);
            }).orElseGet(() -> {
                return copyAllInputItems(craftinginput);
            });
        } else {
            return RecipeCrafting.defaultCraftingReminder(craftinginput);
        }
    }

    @Override
    public void onTake(EntityHuman entityhuman, ItemStack itemstack) {
        this.checkTakeAchievements(itemstack);
        CraftingInput.a craftinginput_a = this.craftSlots.asPositionedCraftInput();
        CraftingInput craftinginput = craftinginput_a.input();
        int i = craftinginput_a.left();
        int j = craftinginput_a.top();
        NonNullList<ItemStack> nonnulllist = this.getRemainingItems(craftinginput, entityhuman.level());

        for (int k = 0; k < craftinginput.height(); ++k) {
            for (int l = 0; l < craftinginput.width(); ++l) {
                int i1 = l + i + (k + j) * this.craftSlots.getWidth();
                ItemStack itemstack1 = this.craftSlots.getItem(i1);
                ItemStack itemstack2 = nonnulllist.get(l + k * craftinginput.width());

                if (!itemstack1.isEmpty()) {
                    this.craftSlots.removeItem(i1, 1);
                    itemstack1 = this.craftSlots.getItem(i1);
                }

                if (!itemstack2.isEmpty()) {
                    if (itemstack1.isEmpty()) {
                        this.craftSlots.setItem(i1, itemstack2);
                    } else if (ItemStack.isSameItemSameComponents(itemstack1, itemstack2)) {
                        itemstack2.grow(itemstack1.getCount());
                        this.craftSlots.setItem(i1, itemstack2);
                    } else if (!this.player.getInventory().add(itemstack2)) {
                        this.player.drop(itemstack2, false);
                    }
                }
            }
        }

    }

    @Override
    public boolean isFake() {
        return true;
    }
}
