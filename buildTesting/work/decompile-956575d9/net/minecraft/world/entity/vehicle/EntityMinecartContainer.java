package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3D;

public abstract class EntityMinecartContainer extends EntityMinecartAbstract implements ContainerEntity {

    private NonNullList<ItemStack> itemStacks;
    @Nullable
    public ResourceKey<LootTable> lootTable;
    public long lootTableSeed;

    protected EntityMinecartContainer(EntityTypes<?> entitytypes, World world) {
        super(entitytypes, world);
        this.itemStacks = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
    }

    @Override
    public void destroy(WorldServer worldserver, DamageSource damagesource) {
        super.destroy(worldserver, damagesource);
        this.chestVehicleDestroyed(damagesource, worldserver, this);
    }

    @Override
    public ItemStack getItem(int i) {
        return this.getChestVehicleItem(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return this.removeChestVehicleItem(i, j);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return this.removeChestVehicleItemNoUpdate(i);
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.setChestVehicleItem(i, itemstack);
    }

    @Override
    public SlotAccess getSlot(int i) {
        return this.getChestVehicleSlot(i);
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return this.isChestVehicleStillValid(entityhuman);
    }

    @Override
    public void remove(Entity.RemovalReason entity_removalreason) {
        if (!this.level().isClientSide && entity_removalreason.shouldDestroy()) {
            InventoryUtils.dropContents(this.level(), (Entity) this, this);
        }

        super.remove(entity_removalreason);
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        this.addChestVehicleSaveData(nbttagcompound, this.registryAccess());
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.readChestVehicleSaveData(nbttagcompound, this.registryAccess());
    }

    @Override
    public EnumInteractionResult interact(EntityHuman entityhuman, EnumHand enumhand) {
        return this.interactWithContainerVehicle(entityhuman);
    }

    @Override
    protected Vec3D applyNaturalSlowdown(Vec3D vec3d) {
        float f = 0.98F;

        if (this.lootTable == null) {
            int i = 15 - Container.getRedstoneSignalFromContainer(this);

            f += (float) i * 0.001F;
        }

        if (this.isInWater()) {
            f *= 0.95F;
        }

        return vec3d.multiply((double) f, 0.0D, (double) f);
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    public void setLootTable(ResourceKey<LootTable> resourcekey, long i) {
        this.lootTable = resourcekey;
        this.lootTableSeed = i;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerinventory, EntityHuman entityhuman) {
        if (this.lootTable != null && entityhuman.isSpectator()) {
            return null;
        } else {
            this.unpackChestVehicleLootTable(playerinventory.player);
            return this.createMenu(i, playerinventory);
        }
    }

    protected abstract Container createMenu(int i, PlayerInventory playerinventory);

    @Nullable
    @Override
    public ResourceKey<LootTable> getContainerLootTable() {
        return this.lootTable;
    }

    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> resourcekey) {
        this.lootTable = resourcekey;
    }

    @Override
    public long getContainerLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setContainerLootTableSeed(long i) {
        this.lootTableSeed = i;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.<ItemStack>withSize(this.getContainerSize(), ItemStack.EMPTY);
    }
}
