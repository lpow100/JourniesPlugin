package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.horse.EntityHorseChestedAbstract;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBoneMeal;
import net.minecraft.world.item.ItemMonsterEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockBeehive;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.BlockPumpkinCarved;
import net.minecraft.world.level.block.BlockRespawnAnchor;
import net.minecraft.world.level.block.BlockShulkerBox;
import net.minecraft.world.level.block.BlockSkull;
import net.minecraft.world.level.block.BlockTNT;
import net.minecraft.world.level.block.BlockWitherSkull;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.IFluidSource;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import org.slf4j.Logger;

public interface IDispenseBehavior {

    Logger LOGGER = LogUtils.getLogger();
    IDispenseBehavior NOOP = (sourceblock, itemstack) -> {
        return itemstack;
    };

    ItemStack dispense(SourceBlock sourceblock, ItemStack itemstack);

    static void bootStrap() {
        BlockDispenser.registerProjectileBehavior(Items.ARROW);
        BlockDispenser.registerProjectileBehavior(Items.TIPPED_ARROW);
        BlockDispenser.registerProjectileBehavior(Items.SPECTRAL_ARROW);
        BlockDispenser.registerProjectileBehavior(Items.EGG);
        BlockDispenser.registerProjectileBehavior(Items.BLUE_EGG);
        BlockDispenser.registerProjectileBehavior(Items.BROWN_EGG);
        BlockDispenser.registerProjectileBehavior(Items.SNOWBALL);
        BlockDispenser.registerProjectileBehavior(Items.EXPERIENCE_BOTTLE);
        BlockDispenser.registerProjectileBehavior(Items.SPLASH_POTION);
        BlockDispenser.registerProjectileBehavior(Items.LINGERING_POTION);
        BlockDispenser.registerProjectileBehavior(Items.FIREWORK_ROCKET);
        BlockDispenser.registerProjectileBehavior(Items.FIRE_CHARGE);
        BlockDispenser.registerProjectileBehavior(Items.WIND_CHARGE);
        DispenseBehaviorItem dispensebehavioritem = new DispenseBehaviorItem() {
            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
                EntityTypes<?> entitytypes = ((ItemMonsterEgg) itemstack.getItem()).getType(sourceblock.level().registryAccess(), itemstack);

                try {
                    entitytypes.spawn(sourceblock.level(), itemstack, (EntityLiving) null, sourceblock.pos().relative(enumdirection), EntitySpawnReason.DISPENSER, enumdirection != EnumDirection.UP, false);
                } catch (Exception exception) {
                    null.LOGGER.error("Error while dispensing spawn egg from dispenser at {}", sourceblock.pos(), exception);
                    return ItemStack.EMPTY;
                }

                itemstack.shrink(1);
                sourceblock.level().gameEvent((Entity) null, (Holder) GameEvent.ENTITY_PLACE, sourceblock.pos());
                return itemstack;
            }
        };

        for (ItemMonsterEgg itemmonsteregg : ItemMonsterEgg.eggs()) {
            BlockDispenser.registerBehavior(itemmonsteregg, dispensebehavioritem);
        }

        BlockDispenser.registerBehavior(Items.ARMOR_STAND, new DispenseBehaviorItem() {
            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
                BlockPosition blockposition = sourceblock.pos().relative(enumdirection);
                WorldServer worldserver = sourceblock.level();
                Consumer<EntityArmorStand> consumer = EntityTypes.<EntityArmorStand>appendDefaultStackConfig((entityarmorstand) -> {
                    entityarmorstand.setYRot(enumdirection.toYRot());
                }, worldserver, itemstack, (EntityLiving) null);
                EntityArmorStand entityarmorstand = EntityTypes.ARMOR_STAND.spawn(worldserver, consumer, blockposition, EntitySpawnReason.DISPENSER, false, false);

                if (entityarmorstand != null) {
                    itemstack.shrink(1);
                }

                return itemstack;
            }
        });
        BlockDispenser.registerBehavior(Items.CHEST, new DispenseBehaviorMaybe() {
            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));

                for (EntityHorseChestedAbstract entityhorsechestedabstract : sourceblock.level().getEntitiesOfClass(EntityHorseChestedAbstract.class, new AxisAlignedBB(blockposition), (entityhorsechestedabstract1) -> {
                    return entityhorsechestedabstract1.isAlive() && !entityhorsechestedabstract1.hasChest();
                })) {
                    if (entityhorsechestedabstract.isTamed() && entityhorsechestedabstract.getSlot(499).set(itemstack)) {
                        itemstack.shrink(1);
                        this.setSuccess(true);
                        return itemstack;
                    }
                }

                return super.execute(sourceblock, itemstack);
            }
        });
        BlockDispenser.registerBehavior(Items.OAK_BOAT, new DispenseBehaviorBoat(EntityTypes.OAK_BOAT));
        BlockDispenser.registerBehavior(Items.SPRUCE_BOAT, new DispenseBehaviorBoat(EntityTypes.SPRUCE_BOAT));
        BlockDispenser.registerBehavior(Items.BIRCH_BOAT, new DispenseBehaviorBoat(EntityTypes.BIRCH_BOAT));
        BlockDispenser.registerBehavior(Items.JUNGLE_BOAT, new DispenseBehaviorBoat(EntityTypes.JUNGLE_BOAT));
        BlockDispenser.registerBehavior(Items.DARK_OAK_BOAT, new DispenseBehaviorBoat(EntityTypes.DARK_OAK_BOAT));
        BlockDispenser.registerBehavior(Items.ACACIA_BOAT, new DispenseBehaviorBoat(EntityTypes.ACACIA_BOAT));
        BlockDispenser.registerBehavior(Items.CHERRY_BOAT, new DispenseBehaviorBoat(EntityTypes.CHERRY_BOAT));
        BlockDispenser.registerBehavior(Items.MANGROVE_BOAT, new DispenseBehaviorBoat(EntityTypes.MANGROVE_BOAT));
        BlockDispenser.registerBehavior(Items.PALE_OAK_BOAT, new DispenseBehaviorBoat(EntityTypes.PALE_OAK_BOAT));
        BlockDispenser.registerBehavior(Items.BAMBOO_RAFT, new DispenseBehaviorBoat(EntityTypes.BAMBOO_RAFT));
        BlockDispenser.registerBehavior(Items.OAK_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.OAK_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.SPRUCE_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.SPRUCE_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.BIRCH_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.BIRCH_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.JUNGLE_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.JUNGLE_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.DARK_OAK_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.DARK_OAK_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.ACACIA_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.ACACIA_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.CHERRY_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.CHERRY_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.MANGROVE_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.MANGROVE_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.PALE_OAK_CHEST_BOAT, new DispenseBehaviorBoat(EntityTypes.PALE_OAK_CHEST_BOAT));
        BlockDispenser.registerBehavior(Items.BAMBOO_CHEST_RAFT, new DispenseBehaviorBoat(EntityTypes.BAMBOO_CHEST_RAFT));
        IDispenseBehavior idispensebehavior = new DispenseBehaviorItem() {
            private final DispenseBehaviorItem defaultDispenseItemBehavior = new DispenseBehaviorItem();

            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                DispensibleContainerItem dispensiblecontaineritem = (DispensibleContainerItem) itemstack.getItem();
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
                World world = sourceblock.level();

                if (dispensiblecontaineritem.emptyContents((EntityLiving) null, world, blockposition, (MovingObjectPositionBlock) null)) {
                    dispensiblecontaineritem.checkExtraContent((EntityLiving) null, world, itemstack, blockposition);
                    return this.consumeWithRemainder(sourceblock, itemstack, new ItemStack(Items.BUCKET));
                } else {
                    return this.defaultDispenseItemBehavior.dispense(sourceblock, itemstack);
                }
            }
        };

        BlockDispenser.registerBehavior(Items.LAVA_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.WATER_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.POWDER_SNOW_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.SALMON_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.COD_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.PUFFERFISH_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.TROPICAL_FISH_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.AXOLOTL_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.TADPOLE_BUCKET, idispensebehavior);
        BlockDispenser.registerBehavior(Items.BUCKET, new DispenseBehaviorItem() {
            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                GeneratorAccess generatoraccess = sourceblock.level();
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
                IBlockData iblockdata = generatoraccess.getBlockState(blockposition);
                Block block = iblockdata.getBlock();

                if (block instanceof IFluidSource ifluidsource) {
                    ItemStack itemstack1 = ifluidsource.pickupBlock((EntityLiving) null, generatoraccess, blockposition, iblockdata);

                    if (itemstack1.isEmpty()) {
                        return super.execute(sourceblock, itemstack);
                    } else {
                        generatoraccess.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PICKUP, blockposition);
                        Item item = itemstack1.getItem();

                        return this.consumeWithRemainder(sourceblock, itemstack, new ItemStack(item));
                    }
                } else {
                    return super.execute(sourceblock, itemstack);
                }
            }
        });
        BlockDispenser.registerBehavior(Items.FLINT_AND_STEEL, new DispenseBehaviorMaybe() {
            @Override
            protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                WorldServer worldserver = sourceblock.level();

                this.setSuccess(true);
                EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
                BlockPosition blockposition = sourceblock.pos().relative(enumdirection);
                IBlockData iblockdata = worldserver.getBlockState(blockposition);

                if (BlockFireAbstract.canBePlacedAt(worldserver, blockposition, enumdirection)) {
                    worldserver.setBlockAndUpdate(blockposition, BlockFireAbstract.getState(worldserver, blockposition));
                    worldserver.gameEvent((Entity) null, (Holder) GameEvent.BLOCK_PLACE, blockposition);
                } else if (!BlockCampfire.canLight(iblockdata) && !CandleBlock.canLight(iblockdata) && !CandleCakeBlock.canLight(iblockdata)) {
                    if (iblockdata.getBlock() instanceof BlockTNT) {
                        if (BlockTNT.prime(worldserver, blockposition)) {
                            worldserver.removeBlock(blockposition, false);
                        } else {
                            this.setSuccess(false);
                        }
                    } else {
                        this.setSuccess(false);
                    }
                } else {
                    worldserver.setBlockAndUpdate(blockposition, (IBlockData) iblockdata.setValue(BlockProperties.LIT, true));
                    worldserver.gameEvent((Entity) null, (Holder) GameEvent.BLOCK_CHANGE, blockposition);
                }

                if (this.isSuccess()) {
                    itemstack.hurtAndBreak(1, worldserver, (EntityPlayer) null, (item) -> {
                    });
                }

                return itemstack;
            }
        });
        BlockDispenser.registerBehavior(Items.BONE_MEAL, new DispenseBehaviorMaybe() {
            @Override
            protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                this.setSuccess(true);
                World world = sourceblock.level();
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));

                if (!ItemBoneMeal.growCrop(itemstack, world, blockposition) && !ItemBoneMeal.growWaterPlant(itemstack, world, blockposition, (EnumDirection) null)) {
                    this.setSuccess(false);
                } else if (!world.isClientSide) {
                    world.levelEvent(1505, blockposition, 15);
                }

                return itemstack;
            }
        });
        BlockDispenser.registerBehavior(Blocks.TNT, new DispenseBehaviorMaybe() {
            @Override
            protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                WorldServer worldserver = sourceblock.level();

                if (!worldserver.getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
                    this.setSuccess(false);
                    return itemstack;
                } else {
                    BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
                    EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(worldserver, (double) blockposition.getX() + 0.5D, (double) blockposition.getY(), (double) blockposition.getZ() + 0.5D, (EntityLiving) null);

                    worldserver.addFreshEntity(entitytntprimed);
                    worldserver.playSound((Entity) null, entitytntprimed.getX(), entitytntprimed.getY(), entitytntprimed.getZ(), SoundEffects.TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    worldserver.gameEvent((Entity) null, (Holder) GameEvent.ENTITY_PLACE, blockposition);
                    itemstack.shrink(1);
                    this.setSuccess(true);
                    return itemstack;
                }
            }
        });
        BlockDispenser.registerBehavior(Items.WITHER_SKELETON_SKULL, new DispenseBehaviorMaybe() {
            @Override
            protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                World world = sourceblock.level();
                EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
                BlockPosition blockposition = sourceblock.pos().relative(enumdirection);

                if (world.isEmptyBlock(blockposition) && BlockWitherSkull.canSpawnMob(world, blockposition, itemstack)) {
                    world.setBlock(blockposition, (IBlockData) Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(BlockSkull.ROTATION, RotationSegment.convertToSegment(enumdirection)), 3);
                    world.gameEvent((Entity) null, (Holder) GameEvent.BLOCK_PLACE, blockposition);
                    TileEntity tileentity = world.getBlockEntity(blockposition);

                    if (tileentity instanceof TileEntitySkull) {
                        BlockWitherSkull.checkSpawn(world, blockposition, (TileEntitySkull) tileentity);
                    }

                    itemstack.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment(sourceblock, itemstack));
                }

                return itemstack;
            }
        });
        BlockDispenser.registerBehavior(Blocks.CARVED_PUMPKIN, new DispenseBehaviorMaybe() {
            @Override
            protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                World world = sourceblock.level();
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
                BlockPumpkinCarved blockpumpkincarved = (BlockPumpkinCarved) Blocks.CARVED_PUMPKIN;

                if (world.isEmptyBlock(blockposition) && blockpumpkincarved.canSpawnGolem(world, blockposition)) {
                    if (!world.isClientSide) {
                        world.setBlock(blockposition, blockpumpkincarved.defaultBlockState(), 3);
                        world.gameEvent((Entity) null, (Holder) GameEvent.BLOCK_PLACE, blockposition);
                    }

                    itemstack.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment(sourceblock, itemstack));
                }

                return itemstack;
            }
        });
        BlockDispenser.registerBehavior(Blocks.SHULKER_BOX.asItem(), new DispenseBehaviorShulkerBox());

        for (EnumColor enumcolor : EnumColor.values()) {
            BlockDispenser.registerBehavior(BlockShulkerBox.getBlockByColor(enumcolor).asItem(), new DispenseBehaviorShulkerBox());
        }

        BlockDispenser.registerBehavior(Items.GLASS_BOTTLE.asItem(), new DispenseBehaviorMaybe() {
            private ItemStack takeLiquid(SourceBlock sourceblock, ItemStack itemstack, ItemStack itemstack1) {
                sourceblock.level().gameEvent((Entity) null, (Holder) GameEvent.FLUID_PICKUP, sourceblock.pos());
                return this.consumeWithRemainder(sourceblock, itemstack, itemstack1);
            }

            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                this.setSuccess(false);
                WorldServer worldserver = sourceblock.level();
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
                IBlockData iblockdata = worldserver.getBlockState(blockposition);

                if (iblockdata.is(TagsBlock.BEEHIVES, (blockbase_blockdata) -> {
                    return blockbase_blockdata.hasProperty(BlockBeehive.HONEY_LEVEL) && blockbase_blockdata.getBlock() instanceof BlockBeehive;
                }) && (Integer) iblockdata.getValue(BlockBeehive.HONEY_LEVEL) >= 5) {
                    ((BlockBeehive) iblockdata.getBlock()).releaseBeesAndResetHoneyLevel(worldserver, iblockdata, blockposition, (EntityHuman) null, TileEntityBeehive.ReleaseStatus.BEE_RELEASED);
                    this.setSuccess(true);
                    return this.takeLiquid(sourceblock, itemstack, new ItemStack(Items.HONEY_BOTTLE));
                } else if (worldserver.getFluidState(blockposition).is(TagsFluid.WATER)) {
                    this.setSuccess(true);
                    return this.takeLiquid(sourceblock, itemstack, PotionContents.createItemStack(Items.POTION, Potions.WATER));
                } else {
                    return super.execute(sourceblock, itemstack);
                }
            }
        });
        BlockDispenser.registerBehavior(Items.GLOWSTONE, new DispenseBehaviorMaybe() {
            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
                BlockPosition blockposition = sourceblock.pos().relative(enumdirection);
                World world = sourceblock.level();
                IBlockData iblockdata = world.getBlockState(blockposition);

                this.setSuccess(true);
                if (iblockdata.is(Blocks.RESPAWN_ANCHOR)) {
                    if ((Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) != 4) {
                        BlockRespawnAnchor.charge((Entity) null, world, blockposition, iblockdata);
                        itemstack.shrink(1);
                    } else {
                        this.setSuccess(false);
                    }

                    return itemstack;
                } else {
                    return super.execute(sourceblock, itemstack);
                }
            }
        });
        BlockDispenser.registerBehavior(Items.SHEARS.asItem(), new DispenseBehaviorShears());
        BlockDispenser.registerBehavior(Items.BRUSH.asItem(), new DispenseBehaviorMaybe() {
            @Override
            protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                WorldServer worldserver = sourceblock.level();
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
                List<Armadillo> list = worldserver.<Armadillo>getEntitiesOfClass(Armadillo.class, new AxisAlignedBB(blockposition), IEntitySelector.NO_SPECTATORS);

                if (list.isEmpty()) {
                    this.setSuccess(false);
                    return itemstack;
                } else {
                    for (Armadillo armadillo : list) {
                        if (armadillo.brushOffScute()) {
                            itemstack.hurtAndBreak(16, worldserver, (EntityPlayer) null, (item) -> {
                            });
                            return itemstack;
                        }
                    }

                    this.setSuccess(false);
                    return itemstack;
                }
            }
        });
        BlockDispenser.registerBehavior(Items.HONEYCOMB, new DispenseBehaviorMaybe() {
            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
                World world = sourceblock.level();
                IBlockData iblockdata = world.getBlockState(blockposition);
                Optional<IBlockData> optional = HoneycombItem.getWaxed(iblockdata);

                if (optional.isPresent()) {
                    world.setBlockAndUpdate(blockposition, (IBlockData) optional.get());
                    world.levelEvent(3003, blockposition, 0);
                    itemstack.shrink(1);
                    this.setSuccess(true);
                    return itemstack;
                } else {
                    return super.execute(sourceblock, itemstack);
                }
            }
        });
        BlockDispenser.registerBehavior(Items.POTION, new DispenseBehaviorItem() {
            private final DispenseBehaviorItem defaultDispenseItemBehavior = new DispenseBehaviorItem();

            @Override
            public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
                PotionContents potioncontents = (PotionContents) itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

                if (!potioncontents.is(Potions.WATER)) {
                    return this.defaultDispenseItemBehavior.dispense(sourceblock, itemstack);
                } else {
                    WorldServer worldserver = sourceblock.level();
                    BlockPosition blockposition = sourceblock.pos();
                    BlockPosition blockposition1 = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));

                    if (!worldserver.getBlockState(blockposition1).is(TagsBlock.CONVERTABLE_TO_MUD)) {
                        return this.defaultDispenseItemBehavior.dispense(sourceblock, itemstack);
                    } else {
                        if (!worldserver.isClientSide) {
                            for (int i = 0; i < 5; ++i) {
                                worldserver.sendParticles(Particles.SPLASH, (double) blockposition.getX() + worldserver.random.nextDouble(), (double) (blockposition.getY() + 1), (double) blockposition.getZ() + worldserver.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
                            }
                        }

                        worldserver.playSound((Entity) null, blockposition, SoundEffects.BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        worldserver.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PLACE, blockposition);
                        worldserver.setBlockAndUpdate(blockposition1, Blocks.MUD.defaultBlockState());
                        return this.consumeWithRemainder(sourceblock, itemstack, new ItemStack(Items.GLASS_BOTTLE));
                    }
                }
            }
        });
        BlockDispenser.registerBehavior(Items.MINECART, new MinecartDispenseItemBehavior(EntityTypes.MINECART));
        BlockDispenser.registerBehavior(Items.CHEST_MINECART, new MinecartDispenseItemBehavior(EntityTypes.CHEST_MINECART));
        BlockDispenser.registerBehavior(Items.FURNACE_MINECART, new MinecartDispenseItemBehavior(EntityTypes.FURNACE_MINECART));
        BlockDispenser.registerBehavior(Items.TNT_MINECART, new MinecartDispenseItemBehavior(EntityTypes.TNT_MINECART));
        BlockDispenser.registerBehavior(Items.HOPPER_MINECART, new MinecartDispenseItemBehavior(EntityTypes.HOPPER_MINECART));
        BlockDispenser.registerBehavior(Items.COMMAND_BLOCK_MINECART, new MinecartDispenseItemBehavior(EntityTypes.COMMAND_BLOCK_MINECART));
    }
}
