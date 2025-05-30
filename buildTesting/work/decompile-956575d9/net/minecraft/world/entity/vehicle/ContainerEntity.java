package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.IInventory;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAI;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public interface ContainerEntity extends IInventory, ITileInventory {

    Vec3D position();

    AxisAlignedBB getBoundingBox();

    @Nullable
    ResourceKey<LootTable> getContainerLootTable();

    void setContainerLootTable(@Nullable ResourceKey<LootTable> resourcekey);

    long getContainerLootTableSeed();

    void setContainerLootTableSeed(long i);

    NonNullList<ItemStack> getItemStacks();

    void clearItemStacks();

    World level();

    boolean isRemoved();

    @Override
    default boolean isEmpty() {
        return this.isChestVehicleEmpty();
    }

    default void addChestVehicleSaveData(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        if (this.getContainerLootTable() != null) {
            nbttagcompound.putString("LootTable", this.getContainerLootTable().location().toString());
            if (this.getContainerLootTableSeed() != 0L) {
                nbttagcompound.putLong("LootTableSeed", this.getContainerLootTableSeed());
            }
        } else {
            ContainerUtil.saveAllItems(nbttagcompound, this.getItemStacks(), holderlookup_a);
        }

    }

    default void readChestVehicleSaveData(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        this.clearItemStacks();
        ResourceKey<LootTable> resourcekey = (ResourceKey) nbttagcompound.read("LootTable", LootTable.KEY_CODEC).orElse((Object) null);

        this.setContainerLootTable(resourcekey);
        this.setContainerLootTableSeed(nbttagcompound.getLongOr("LootTableSeed", 0L));
        if (resourcekey == null) {
            ContainerUtil.loadAllItems(nbttagcompound, this.getItemStacks(), holderlookup_a);
        }

    }

    default void chestVehicleDestroyed(DamageSource damagesource, WorldServer worldserver, Entity entity) {
        if (worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            InventoryUtils.dropContents(worldserver, entity, this);
            Entity entity1 = damagesource.getDirectEntity();

            if (entity1 != null && entity1.getType() == EntityTypes.PLAYER) {
                PiglinAI.angerNearbyPiglins(worldserver, (EntityHuman) entity1, true);
            }

        }
    }

    default EnumInteractionResult interactWithContainerVehicle(EntityHuman entityhuman) {
        entityhuman.openMenu(this);
        return EnumInteractionResult.SUCCESS;
    }

    default void unpackChestVehicleLootTable(@Nullable EntityHuman entityhuman) {
        MinecraftServer minecraftserver = this.level().getServer();

        if (this.getContainerLootTable() != null && minecraftserver != null) {
            LootTable loottable = minecraftserver.reloadableRegistries().getLootTable(this.getContainerLootTable());

            if (entityhuman != null) {
                CriterionTriggers.GENERATE_LOOT.trigger((EntityPlayer) entityhuman, this.getContainerLootTable());
            }

            this.setContainerLootTable((ResourceKey) null);
            LootParams.a lootparams_a = (new LootParams.a((WorldServer) this.level())).withParameter(LootContextParameters.ORIGIN, this.position());

            if (entityhuman != null) {
                lootparams_a.withLuck(entityhuman.getLuck()).withParameter(LootContextParameters.THIS_ENTITY, entityhuman);
            }

            loottable.fill(this, lootparams_a.create(LootContextParameterSets.CHEST), this.getContainerLootTableSeed());
        }

    }

    default void clearChestVehicleContent() {
        this.unpackChestVehicleLootTable((EntityHuman) null);
        this.getItemStacks().clear();
    }

    default boolean isChestVehicleEmpty() {
        for (ItemStack itemstack : this.getItemStacks()) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    default ItemStack removeChestVehicleItemNoUpdate(int i) {
        this.unpackChestVehicleLootTable((EntityHuman) null);
        ItemStack itemstack = (ItemStack) this.getItemStacks().get(i);

        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.getItemStacks().set(i, ItemStack.EMPTY);
            return itemstack;
        }
    }

    default ItemStack getChestVehicleItem(int i) {
        this.unpackChestVehicleLootTable((EntityHuman) null);
        return (ItemStack) this.getItemStacks().get(i);
    }

    default ItemStack removeChestVehicleItem(int i, int j) {
        this.unpackChestVehicleLootTable((EntityHuman) null);
        return ContainerUtil.removeItem(this.getItemStacks(), i, j);
    }

    default void setChestVehicleItem(int i, ItemStack itemstack) {
        this.unpackChestVehicleLootTable((EntityHuman) null);
        this.getItemStacks().set(i, itemstack);
        itemstack.limitSize(this.getMaxStackSize(itemstack));
    }

    default SlotAccess getChestVehicleSlot(final int i) {
        return i >= 0 && i < this.getContainerSize() ? new SlotAccess() {
            @Override
            public ItemStack get() {
                return ContainerEntity.this.getChestVehicleItem(i);
            }

            @Override
            public boolean set(ItemStack itemstack) {
                ContainerEntity.this.setChestVehicleItem(i, itemstack);
                return true;
            }
        } : SlotAccess.NULL;
    }

    default boolean isChestVehicleStillValid(EntityHuman entityhuman) {
        return !this.isRemoved() && entityhuman.canInteractWithEntity(this.getBoundingBox(), 4.0D);
    }
}
