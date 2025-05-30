package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemLeash;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public class EntityLeash extends BlockAttachedEntity {

    public static final double OFFSET_Y = 0.375D;

    public EntityLeash(EntityTypes<? extends EntityLeash> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityLeash(World world, BlockPosition blockposition) {
        super(EntityTypes.LEASH_KNOT, world, blockposition);
        this.setPos((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {}

    @Override
    protected void recalculateBoundingBox() {
        this.setPosRaw((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.375D, (double) this.pos.getZ() + 0.5D);
        double d0 = (double) this.getType().getWidth() / 2.0D;
        double d1 = (double) this.getType().getHeight();

        this.setBoundingBox(new AxisAlignedBB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0, this.getY() + d1, this.getZ() + d0));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        return d0 < 1024.0D;
    }

    @Override
    public void dropItem(WorldServer worldserver, @Nullable Entity entity) {
        this.playSound(SoundEffects.LEASH_KNOT_BREAK, 1.0F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {}

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {}

    @Override
    public EnumInteractionResult interact(EntityHuman entityhuman, EnumHand enumhand) {
        if (this.level().isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            boolean flag = false;
            List<Leashable> list = ItemLeash.leashableInArea(this.level(), this.getPos(), (leashable) -> {
                Entity entity = leashable.getLeashHolder();

                return entity == entityhuman || entity == this;
            });

            for (Leashable leashable : list) {
                if (leashable.getLeashHolder() == entityhuman) {
                    // CraftBukkit start
                    if (leashable instanceof Entity leashed) {
                        if (CraftEventFactory.callPlayerLeashEntityEvent(leashed, this, entityhuman, enumhand).isCancelled()) {
                            ((EntityPlayer) entityhuman).connection.send(new PacketPlayOutAttachEntity(leashed, leashable.getLeashHolder()));
                            flag = true; // Also set true when the event is cancelled otherwise it tries to unleash the entities
                            continue;
                        }
                    }
                    // CraftBukkit end
                    leashable.setLeashedTo(this, true);
                    flag = true;
                }
            }

            boolean flag1 = false;

            if (!flag) {
                // CraftBukkit start - Move below
                // this.discard();
                boolean die = true;
                // CraftBukkit end
                if (true || entityhuman.getAbilities().instabuild) { // CraftBukkit - Process for non-creative as well
                    for (Leashable leashable1 : list) {
                        if (leashable1.isLeashed() && leashable1.getLeashHolder() == this) {
                            // CraftBukkit start
                            if (leashable1 instanceof Entity leashed) {
                                if (CraftEventFactory.callPlayerUnleashEntityEvent(leashed, entityhuman, enumhand).isCancelled()) {
                                    die = false;
                                    continue;
                                }
                            }
                            if (entityhuman.getAbilities().instabuild){
                                leashable1.removeLeash();
                            } else {
                                leashable1.dropLeash();
                            }
                            // CraftBukkit end
                            flag1 = true;
                        }
                    }
                    // CraftBukkit start
                    if (die) {
                        this.discard(EntityRemoveEvent.Cause.DROP); // CraftBukkit - add Bukkit remove cause
                    }
                    // CraftBukkit end
                }
            }

            if (flag || flag1) {
                this.gameEvent(GameEvent.BLOCK_ATTACH, entityhuman);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    @Override
    public boolean survives() {
        return this.level().getBlockState(this.pos).is(TagsBlock.FENCES);
    }

    public static EntityLeash getOrCreateKnot(World world, BlockPosition blockposition) {
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();

        for (EntityLeash entityleash : world.getEntitiesOfClass(EntityLeash.class, new AxisAlignedBB((double) i - 1.0D, (double) j - 1.0D, (double) k - 1.0D, (double) i + 1.0D, (double) j + 1.0D, (double) k + 1.0D))) {
            if (entityleash.getPos().equals(blockposition)) {
                return entityleash;
            }
        }

        EntityLeash entityleash1 = new EntityLeash(world, blockposition);

        world.addFreshEntity(entityleash1);
        return entityleash1;
    }

    public void playPlacementSound() {
        this.playSound(SoundEffects.LEASH_KNOT_PLACE, 1.0F, 1.0F);
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket(EntityTrackerEntry entitytrackerentry) {
        return new PacketPlayOutSpawnEntity(this, 0, this.getPos());
    }

    @Override
    public Vec3D getRopeHoldPosition(float f) {
        return this.getPosition(f).add(0.0D, 0.2D, 0.0D);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.LEAD);
    }
}
