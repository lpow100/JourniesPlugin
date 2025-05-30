package net.minecraft.world.inventory;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemBanner;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;

// CraftBukkit start
import org.bukkit.Location;
import org.bukkit.craftbukkit.inventory.CraftInventoryLoom;
import org.bukkit.craftbukkit.inventory.view.CraftLoomView;
import org.bukkit.entity.Player;
// CraftBukkit end

public class ContainerLoom extends Container {

    // CraftBukkit start
    private CraftLoomView bukkitEntity = null;
    private Player player;

    @Override
    public CraftLoomView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryLoom inventory = new CraftInventoryLoom(this.inputContainer, this.outputContainer);
        bukkitEntity = new CraftLoomView(this.player, inventory, this);
        return bukkitEntity;
    }
    // CraftBukkit end
    private static final int PATTERN_NOT_SET = -1;
    private static final int INV_SLOT_START = 4;
    private static final int INV_SLOT_END = 31;
    private static final int USE_ROW_SLOT_START = 31;
    private static final int USE_ROW_SLOT_END = 40;
    private final ContainerAccess access;
    final ContainerProperty selectedBannerPatternIndex;
    private List<Holder<EnumBannerPatternType>> selectablePatterns;
    Runnable slotUpdateListener;
    private final HolderGetter<EnumBannerPatternType> patternGetter;
    final Slot bannerSlot;
    final Slot dyeSlot;
    private final Slot patternSlot;
    private final Slot resultSlot;
    long lastSoundTime;
    private final IInventory inputContainer;
    private final IInventory outputContainer;

    public ContainerLoom(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerLoom(int i, PlayerInventory playerinventory, final ContainerAccess containeraccess) {
        super(Containers.LOOM, i);
        this.selectedBannerPatternIndex = ContainerProperty.standalone();
        this.selectablePatterns = List.of();
        this.slotUpdateListener = () -> {
        };
        this.inputContainer = new InventorySubcontainer(3) {
            @Override
            public void setChanged() {
                super.setChanged();
                ContainerLoom.this.slotsChanged(this);
                ContainerLoom.this.slotUpdateListener.run();
            }

            // CraftBukkit start
            @Override
            public Location getLocation() {
                return containeraccess.getLocation();
            }
            // CraftBukkit end
        };
        this.outputContainer = new InventorySubcontainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                ContainerLoom.this.slotUpdateListener.run();
            }

            // CraftBukkit start
            @Override
            public Location getLocation() {
                return containeraccess.getLocation();
            }
            // CraftBukkit end
        };
        this.access = containeraccess;
        this.bannerSlot = this.addSlot(new Slot(this.inputContainer, 0, 13, 26) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.getItem() instanceof ItemBanner;
            }
        });
        this.dyeSlot = this.addSlot(new Slot(this.inputContainer, 1, 33, 26) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.getItem() instanceof ItemDye;
            }
        });
        this.patternSlot = this.addSlot(new Slot(this.inputContainer, 2, 23, 45) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.has(DataComponents.PROVIDES_BANNER_PATTERNS);
            }
        });
        this.resultSlot = this.addSlot(new Slot(this.outputContainer, 0, 143, 57) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return false;
            }

            @Override
            public void onTake(EntityHuman entityhuman, ItemStack itemstack) {
                ContainerLoom.this.bannerSlot.remove(1);
                ContainerLoom.this.dyeSlot.remove(1);
                if (!ContainerLoom.this.bannerSlot.hasItem() || !ContainerLoom.this.dyeSlot.hasItem()) {
                    ContainerLoom.this.selectedBannerPatternIndex.set(-1);
                }

                containeraccess.execute((world, blockposition) -> {
                    long j = world.getGameTime();

                    if (ContainerLoom.this.lastSoundTime != j) {
                        world.playSound((Entity) null, blockposition, SoundEffects.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        ContainerLoom.this.lastSoundTime = j;
                    }

                });
                super.onTake(entityhuman, itemstack);
            }
        });
        this.addStandardInventorySlots(playerinventory, 8, 84);
        this.addDataSlot(this.selectedBannerPatternIndex);
        this.patternGetter = playerinventory.player.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        player = (Player) playerinventory.player.getBukkitEntity(); // CraftBukkit
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        if (!this.checkReachable) return true; // CraftBukkit
        return stillValid(this.access, entityhuman, Blocks.LOOM);
    }

    @Override
    public boolean clickMenuButton(EntityHuman entityhuman, int i) {
        if (i >= 0 && i < this.selectablePatterns.size()) {
            this.selectedBannerPatternIndex.set(i);
            this.setupResultSlot((Holder) this.selectablePatterns.get(i));
            return true;
        } else {
            return false;
        }
    }

    private List<Holder<EnumBannerPatternType>> getSelectablePatterns(ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            return (List) this.patternGetter.get(BannerPatternTags.NO_ITEM_REQUIRED).map(ImmutableList::copyOf).orElse(ImmutableList.of());
        } else {
            TagKey<EnumBannerPatternType> tagkey = (TagKey) itemstack.get(DataComponents.PROVIDES_BANNER_PATTERNS);

            return tagkey != null ? (List) this.patternGetter.get(tagkey).map(ImmutableList::copyOf).orElse(ImmutableList.of()) : List.of();
        }
    }

    private boolean isValidPatternIndex(int i) {
        return i >= 0 && i < this.selectablePatterns.size();
    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        ItemStack itemstack = this.bannerSlot.getItem();
        ItemStack itemstack1 = this.dyeSlot.getItem();
        ItemStack itemstack2 = this.patternSlot.getItem();

        if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
            int i = this.selectedBannerPatternIndex.get();
            boolean flag = this.isValidPatternIndex(i);
            List<Holder<EnumBannerPatternType>> list = this.selectablePatterns;

            this.selectablePatterns = this.getSelectablePatterns(itemstack2);
            Holder<EnumBannerPatternType> holder;

            if (this.selectablePatterns.size() == 1) {
                this.selectedBannerPatternIndex.set(0);
                holder = (Holder) this.selectablePatterns.get(0);
            } else if (!flag) {
                this.selectedBannerPatternIndex.set(-1);
                holder = null;
            } else {
                Holder<EnumBannerPatternType> holder1 = (Holder) list.get(i);
                int j = this.selectablePatterns.indexOf(holder1);

                if (j != -1) {
                    holder = holder1;
                    this.selectedBannerPatternIndex.set(j);
                } else {
                    holder = null;
                    this.selectedBannerPatternIndex.set(-1);
                }
            }

            if (holder != null) {
                BannerPatternLayers bannerpatternlayers = (BannerPatternLayers) itemstack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
                boolean flag1 = bannerpatternlayers.layers().size() >= 6;

                if (flag1) {
                    this.selectedBannerPatternIndex.set(-1);
                    this.resultSlot.set(ItemStack.EMPTY);
                } else {
                    this.setupResultSlot(holder);
                }
            } else {
                this.resultSlot.set(ItemStack.EMPTY);
            }

            this.broadcastChanges();
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
            this.selectablePatterns = List.of();
            this.selectedBannerPatternIndex.set(-1);
        }
    }

    public List<Holder<EnumBannerPatternType>> getSelectablePatterns() {
        return this.selectablePatterns;
    }

    public int getSelectedBannerPatternIndex() {
        return this.selectedBannerPatternIndex.get();
    }

    public void registerUpdateListener(Runnable runnable) {
        this.slotUpdateListener = runnable;
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i == this.resultSlot.index) {
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (i != this.dyeSlot.index && i != this.bannerSlot.index && i != this.patternSlot.index) {
                if (itemstack1.getItem() instanceof ItemBanner) {
                    if (!this.moveItemStackTo(itemstack1, this.bannerSlot.index, this.bannerSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.getItem() instanceof ItemDye) {
                    if (!this.moveItemStackTo(itemstack1, this.dyeSlot.index, this.dyeSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.has(DataComponents.PROVIDES_BANNER_PATTERNS)) {
                    if (!this.moveItemStackTo(itemstack1, this.patternSlot.index, this.patternSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= 4 && i < 31) {
                    if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= 31 && i < 40 && !this.moveItemStackTo(itemstack1, 4, 31, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(entityhuman, itemstack1);
        }

        return itemstack;
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.access.execute((world, blockposition) -> {
            this.clearContainer(entityhuman, this.inputContainer);
        });
    }

    private void setupResultSlot(Holder<EnumBannerPatternType> holder) {
        ItemStack itemstack = this.bannerSlot.getItem();
        ItemStack itemstack1 = this.dyeSlot.getItem();
        ItemStack itemstack2 = ItemStack.EMPTY;

        if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
            itemstack2 = itemstack.copyWithCount(1);
            EnumColor enumcolor = ((ItemDye) itemstack1.getItem()).getDyeColor();

            itemstack2.update(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY, (bannerpatternlayers) -> {
                // CraftBukkit start
                if (bannerpatternlayers.layers().size() > 20) {
                    bannerpatternlayers = new BannerPatternLayers(List.copyOf(bannerpatternlayers.layers().subList(0, 20)));
                }
                // CraftBukkit end
                return (new BannerPatternLayers.a()).addAll(bannerpatternlayers).add(holder, enumcolor).build();
            });
        }

        if (!ItemStack.matches(itemstack2, this.resultSlot.getItem())) {
            this.resultSlot.set(itemstack2);
        }

    }

    public Slot getBannerSlot() {
        return this.bannerSlot;
    }

    public Slot getDyeSlot() {
        return this.dyeSlot;
    }

    public Slot getPatternSlot() {
        return this.patternSlot;
    }

    public Slot getResultSlot() {
        return this.resultSlot;
    }
}
