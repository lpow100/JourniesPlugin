package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagType;
import net.minecraft.nbt.StreamTagVisitor;

public class CollectFields extends CollectToTag {

    private int fieldsToGetCount;
    private final Set<NBTTagType<?>> wantedTypes;
    private final Deque<FieldTree> stack = new ArrayDeque();

    public CollectFields(FieldSelector... afieldselector) {
        this.fieldsToGetCount = afieldselector.length;
        ImmutableSet.Builder<NBTTagType<?>> immutableset_builder = ImmutableSet.builder();
        FieldTree fieldtree = FieldTree.createRoot();

        for (FieldSelector fieldselector : afieldselector) {
            fieldtree.addEntry(fieldselector);
            immutableset_builder.add(fieldselector.type());
        }

        this.stack.push(fieldtree);
        immutableset_builder.add(NBTTagCompound.TYPE);
        this.wantedTypes = immutableset_builder.build();
    }

    @Override
    public StreamTagVisitor.b visitRootEntry(NBTTagType<?> nbttagtype) {
        return nbttagtype != NBTTagCompound.TYPE ? StreamTagVisitor.b.HALT : super.visitRootEntry(nbttagtype);
    }

    @Override
    public StreamTagVisitor.a visitEntry(NBTTagType<?> nbttagtype) {
        FieldTree fieldtree = (FieldTree) this.stack.element();

        return this.depth() > fieldtree.depth() ? super.visitEntry(nbttagtype) : (this.fieldsToGetCount <= 0 ? StreamTagVisitor.a.BREAK : (!this.wantedTypes.contains(nbttagtype) ? StreamTagVisitor.a.SKIP : super.visitEntry(nbttagtype)));
    }

    @Override
    public StreamTagVisitor.a visitEntry(NBTTagType<?> nbttagtype, String s) {
        FieldTree fieldtree = (FieldTree) this.stack.element();

        if (this.depth() > fieldtree.depth()) {
            return super.visitEntry(nbttagtype, s);
        } else if (fieldtree.selectedFields().remove(s, nbttagtype)) {
            --this.fieldsToGetCount;
            return super.visitEntry(nbttagtype, s);
        } else {
            if (nbttagtype == NBTTagCompound.TYPE) {
                FieldTree fieldtree1 = (FieldTree) fieldtree.fieldsToRecurse().get(s);

                if (fieldtree1 != null) {
                    this.stack.push(fieldtree1);
                    return super.visitEntry(nbttagtype, s);
                }
            }

            return StreamTagVisitor.a.SKIP;
        }
    }

    @Override
    public StreamTagVisitor.b visitContainerEnd() {
        if (this.depth() == ((FieldTree) this.stack.element()).depth()) {
            this.stack.pop();
        }

        return super.visitContainerEnd();
    }

    public int getMissingFieldCount() {
        return this.fieldsToGetCount;
    }
}
