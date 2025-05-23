package net.minecraft.world.item.crafting.display;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface DisplayContentsFactory<T> {
    public interface b<T> extends DisplayContentsFactory<T> {

        default T forStack(Holder<Item> holder) {
            return (T) this.forStack(new ItemStack(holder));
        }

        default T forStack(Item item) {
            return (T) this.forStack(new ItemStack(item));
        }

        T forStack(ItemStack itemstack);
    }

    public interface a<T> extends DisplayContentsFactory<T> {

        T addRemainder(T t0, List<T> list);
    }
}
