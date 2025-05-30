package net.minecraft.util;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.BlockStainedGlass;
import net.minecraft.world.level.block.BlockStainedGlassPane;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public class SpawnUtil {

    public SpawnUtil() {}

    public static <T extends EntityInsentient> Optional<T> trySpawnMob(EntityTypes<T> entitytypes, EntitySpawnReason entityspawnreason, WorldServer worldserver, BlockPosition blockposition, int i, int j, int k, SpawnUtil.a spawnutil_a, boolean flag) {
        // CraftBukkit start
        return trySpawnMob(entitytypes, entityspawnreason, worldserver, blockposition, i, j, k, spawnutil_a, flag, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DEFAULT);
    }

    public static <T extends EntityInsentient> Optional<T> trySpawnMob(EntityTypes<T> entitytypes, EntitySpawnReason entityspawnreason, WorldServer worldserver, BlockPosition blockposition, int i, int j, int k, SpawnUtil.a spawnutil_a, boolean flag, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        // CraftBukkit end
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();

        for (int l = 0; l < i; ++l) {
            int i1 = MathHelper.randomBetweenInclusive(worldserver.random, -j, j);
            int j1 = MathHelper.randomBetweenInclusive(worldserver.random, -j, j);

            blockposition_mutableblockposition.setWithOffset(blockposition, i1, k, j1);
            if (worldserver.getWorldBorder().isWithinBounds((BlockPosition) blockposition_mutableblockposition) && moveToPossibleSpawnPosition(worldserver, k, blockposition_mutableblockposition, spawnutil_a) && (!flag || worldserver.noCollision(entitytypes.getSpawnAABB((double) blockposition_mutableblockposition.getX() + 0.5D, (double) blockposition_mutableblockposition.getY(), (double) blockposition_mutableblockposition.getZ() + 0.5D)))) {
                T t0 = entitytypes.create(worldserver, (Consumer<T>) null, blockposition_mutableblockposition, entityspawnreason, false, false); // CraftBukkit - decompile error

                if (t0 != null) {
                    if (t0.checkSpawnRules(worldserver, entityspawnreason) && t0.checkSpawnObstruction(worldserver)) {
                        worldserver.addFreshEntityWithPassengers(t0, reason); // CraftBukkit
                        if (t0.isRemoved()) return Optional.empty(); // CraftBukkit
                        t0.playAmbientSound();
                        return Optional.of(t0);
                    }

                    t0.discard(null); // CraftBukkit - add Bukkit remove cause
                }
            }
        }

        return Optional.empty();
    }

    private static boolean moveToPossibleSpawnPosition(WorldServer worldserver, int i, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, SpawnUtil.a spawnutil_a) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = (new BlockPosition.MutableBlockPosition()).set(blockposition_mutableblockposition);
        IBlockData iblockdata = worldserver.getBlockState(blockposition_mutableblockposition1);

        for (int j = i; j >= -i; --j) {
            blockposition_mutableblockposition.move(EnumDirection.DOWN);
            blockposition_mutableblockposition1.setWithOffset(blockposition_mutableblockposition, EnumDirection.UP);
            IBlockData iblockdata1 = worldserver.getBlockState(blockposition_mutableblockposition);

            if (spawnutil_a.canSpawnOn(worldserver, blockposition_mutableblockposition, iblockdata1, blockposition_mutableblockposition1, iblockdata)) {
                blockposition_mutableblockposition.move(EnumDirection.UP);
                return true;
            }

            iblockdata = iblockdata1;
        }

        return false;
    }

    public interface a {

        /** @deprecated */
        @Deprecated
        SpawnUtil.a LEGACY_IRON_GOLEM = (worldserver, blockposition, iblockdata, blockposition1, iblockdata1) -> {
            return !iblockdata.is(Blocks.COBWEB) && !iblockdata.is(Blocks.CACTUS) && !iblockdata.is(Blocks.GLASS_PANE) && !(iblockdata.getBlock() instanceof BlockStainedGlassPane) && !(iblockdata.getBlock() instanceof BlockStainedGlass) && !(iblockdata.getBlock() instanceof BlockLeaves) && !iblockdata.is(Blocks.CONDUIT) && !iblockdata.is(Blocks.ICE) && !iblockdata.is(Blocks.TNT) && !iblockdata.is(Blocks.GLOWSTONE) && !iblockdata.is(Blocks.BEACON) && !iblockdata.is(Blocks.SEA_LANTERN) && !iblockdata.is(Blocks.FROSTED_ICE) && !iblockdata.is(Blocks.TINTED_GLASS) && !iblockdata.is(Blocks.GLASS) ? (iblockdata1.isAir() || iblockdata1.liquid()) && (iblockdata.isSolid() || iblockdata.is(Blocks.POWDER_SNOW)) : false;
        };
        SpawnUtil.a ON_TOP_OF_COLLIDER = (worldserver, blockposition, iblockdata, blockposition1, iblockdata1) -> {
            return iblockdata1.getCollisionShape(worldserver, blockposition1).isEmpty() && Block.isFaceFull(iblockdata.getCollisionShape(worldserver, blockposition), EnumDirection.UP);
        };
        SpawnUtil.a ON_TOP_OF_COLLIDER_NO_LEAVES = (worldserver, blockposition, iblockdata, blockposition1, iblockdata1) -> {
            return iblockdata1.getCollisionShape(worldserver, blockposition1).isEmpty() && !iblockdata.is(TagsBlock.LEAVES) && Block.isFaceFull(iblockdata.getCollisionShape(worldserver, blockposition), EnumDirection.UP);
        };

        boolean canSpawnOn(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, BlockPosition blockposition1, IBlockData iblockdata1);
    }
}
