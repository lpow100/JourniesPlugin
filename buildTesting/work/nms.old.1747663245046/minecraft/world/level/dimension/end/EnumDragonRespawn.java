package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderCrystal;
import net.minecraft.world.level.World;
import net.minecraft.world.level.levelgen.feature.WorldGenEnder;
import net.minecraft.world.level.levelgen.feature.WorldGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEndSpikeConfiguration;

// CraftBukkit start
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public enum EnumDragonRespawn {

    START {
        @Override
        public void tick(WorldServer worldserver, EnderDragonBattle enderdragonbattle, List<EntityEnderCrystal> list, int i, BlockPosition blockposition) {
            BlockPosition blockposition1 = new BlockPosition(0, 128, 0);

            for (EntityEnderCrystal entityendercrystal : list) {
                entityendercrystal.setBeamTarget(blockposition1);
            }

            enderdragonbattle.setRespawnStage(PREPARING_TO_SUMMON_PILLARS); // CraftBukkit - decompile error
        }
    },
    PREPARING_TO_SUMMON_PILLARS {
        @Override
        public void tick(WorldServer worldserver, EnderDragonBattle enderdragonbattle, List<EntityEnderCrystal> list, int i, BlockPosition blockposition) {
            if (i < 100) {
                if (i == 0 || i == 50 || i == 51 || i == 52 || i >= 95) {
                    worldserver.levelEvent(3001, new BlockPosition(0, 128, 0), 0);
                }
            } else {
                enderdragonbattle.setRespawnStage(SUMMONING_PILLARS); // CraftBukkit - decompile error
            }

        }
    },
    SUMMONING_PILLARS {
        @Override
        public void tick(WorldServer worldserver, EnderDragonBattle enderdragonbattle, List<EntityEnderCrystal> list, int i, BlockPosition blockposition) {
            int j = 40;
            boolean flag = i % 40 == 0;
            boolean flag1 = i % 40 == 39;

            if (flag || flag1) {
                List<WorldGenEnder.Spike> list1 = WorldGenEnder.getSpikesForLevel(worldserver);
                int k = i / 40;

                if (k < list1.size()) {
                    WorldGenEnder.Spike worldgenender_spike = (WorldGenEnder.Spike) list1.get(k);

                    if (flag) {
                        for (EntityEnderCrystal entityendercrystal : list) {
                            entityendercrystal.setBeamTarget(new BlockPosition(worldgenender_spike.getCenterX(), worldgenender_spike.getHeight() + 1, worldgenender_spike.getCenterZ()));
                        }
                    } else {
                        int l = 10;

                        for (BlockPosition blockposition1 : BlockPosition.betweenClosed(new BlockPosition(worldgenender_spike.getCenterX() - 10, worldgenender_spike.getHeight() - 10, worldgenender_spike.getCenterZ() - 10), new BlockPosition(worldgenender_spike.getCenterX() + 10, worldgenender_spike.getHeight() + 10, worldgenender_spike.getCenterZ() + 10))) {
                            worldserver.removeBlock(blockposition1, false);
                        }

                        worldserver.explode((Entity) null, (double) ((float) worldgenender_spike.getCenterX() + 0.5F), (double) worldgenender_spike.getHeight(), (double) ((float) worldgenender_spike.getCenterZ() + 0.5F), 5.0F, World.a.BLOCK);
                        WorldGenFeatureEndSpikeConfiguration worldgenfeatureendspikeconfiguration = new WorldGenFeatureEndSpikeConfiguration(true, ImmutableList.of(worldgenender_spike), new BlockPosition(0, 128, 0));

                        WorldGenerator.END_SPIKE.place(worldgenfeatureendspikeconfiguration, worldserver, worldserver.getChunkSource().getGenerator(), RandomSource.create(), new BlockPosition(worldgenender_spike.getCenterX(), 45, worldgenender_spike.getCenterZ()));
                    }
                } else if (flag) {
                    enderdragonbattle.setRespawnStage(SUMMONING_DRAGON); // CraftBukkit - decompile error
                }
            }

        }
    },
    SUMMONING_DRAGON {
        @Override
        public void tick(WorldServer worldserver, EnderDragonBattle enderdragonbattle, List<EntityEnderCrystal> list, int i, BlockPosition blockposition) {
            if (i >= 100) {
                enderdragonbattle.setRespawnStage(END); // CraftBukkit - decompile error
                enderdragonbattle.resetSpikeCrystals();

                for (EntityEnderCrystal entityendercrystal : list) {
                    entityendercrystal.setBeamTarget((BlockPosition) null);
                    worldserver.explode(entityendercrystal, entityendercrystal.getX(), entityendercrystal.getY(), entityendercrystal.getZ(), 6.0F, World.a.NONE);
                    entityendercrystal.discard(EntityRemoveEvent.Cause.EXPLODE); // CraftBukkit - add Bukkit remove cause
                }
            } else if (i >= 80) {
                worldserver.levelEvent(3001, new BlockPosition(0, 128, 0), 0);
            } else if (i == 0) {
                for (EntityEnderCrystal entityendercrystal1 : list) {
                    entityendercrystal1.setBeamTarget(new BlockPosition(0, 128, 0));
                }
            } else if (i < 5) {
                worldserver.levelEvent(3001, new BlockPosition(0, 128, 0), 0);
            }

        }
    },
    END {
        @Override
        public void tick(WorldServer worldserver, EnderDragonBattle enderdragonbattle, List<EntityEnderCrystal> list, int i, BlockPosition blockposition) {}
    };

    EnumDragonRespawn() {}

    public abstract void tick(WorldServer worldserver, EnderDragonBattle enderdragonbattle, List<EntityEnderCrystal> list, int i, BlockPosition blockposition);
}
