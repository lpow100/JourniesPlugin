package net.minecraft.world.level;

import com.google.common.collect.AbstractIterator;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.CursorPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class VoxelShapeSpliterator<T> extends AbstractIterator<T> {

    private final AxisAlignedBB box;
    private final VoxelShapeCollision context;
    private final CursorPosition cursor;
    private final BlockPosition.MutableBlockPosition pos;
    private final VoxelShape entityShape;
    private final ICollisionAccess collisionGetter;
    private final boolean onlySuffocatingBlocks;
    @Nullable
    private IBlockAccess cachedBlockGetter;
    private long cachedBlockGetterPos;
    private final BiFunction<BlockPosition.MutableBlockPosition, VoxelShape, T> resultProvider;

    public VoxelShapeSpliterator(ICollisionAccess icollisionaccess, @Nullable Entity entity, AxisAlignedBB axisalignedbb, boolean flag, BiFunction<BlockPosition.MutableBlockPosition, VoxelShape, T> bifunction) {
        this(icollisionaccess, entity == null ? VoxelShapeCollision.empty() : VoxelShapeCollision.of(entity), axisalignedbb, flag, bifunction);
    }

    public VoxelShapeSpliterator(ICollisionAccess icollisionaccess, VoxelShapeCollision voxelshapecollision, AxisAlignedBB axisalignedbb, boolean flag, BiFunction<BlockPosition.MutableBlockPosition, VoxelShape, T> bifunction) {
        this.context = voxelshapecollision;
        this.pos = new BlockPosition.MutableBlockPosition();
        this.entityShape = VoxelShapes.create(axisalignedbb);
        this.collisionGetter = icollisionaccess;
        this.box = axisalignedbb;
        this.onlySuffocatingBlocks = flag;
        this.resultProvider = bifunction;
        int i = MathHelper.floor(axisalignedbb.minX - 1.0E-7D) - 1;
        int j = MathHelper.floor(axisalignedbb.maxX + 1.0E-7D) + 1;
        int k = MathHelper.floor(axisalignedbb.minY - 1.0E-7D) - 1;
        int l = MathHelper.floor(axisalignedbb.maxY + 1.0E-7D) + 1;
        int i1 = MathHelper.floor(axisalignedbb.minZ - 1.0E-7D) - 1;
        int j1 = MathHelper.floor(axisalignedbb.maxZ + 1.0E-7D) + 1;

        this.cursor = new CursorPosition(i, k, i1, j, l, j1);
    }

    @Nullable
    private IBlockAccess getChunk(int i, int j) {
        int k = SectionPosition.blockToSectionCoord(i);
        int l = SectionPosition.blockToSectionCoord(j);
        long i1 = ChunkCoordIntPair.asLong(k, l);

        if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == i1) {
            return this.cachedBlockGetter;
        } else {
            IBlockAccess iblockaccess = this.collisionGetter.getChunkForCollisions(k, l);

            this.cachedBlockGetter = iblockaccess;
            this.cachedBlockGetterPos = i1;
            return iblockaccess;
        }
    }

    protected T computeNext() {
        while (true) {
            if (this.cursor.advance()) {
                int i = this.cursor.nextX();
                int j = this.cursor.nextY();
                int k = this.cursor.nextZ();
                int l = this.cursor.getNextType();

                if (l == 3) {
                    continue;
                }

                IBlockAccess iblockaccess = this.getChunk(i, k);

                if (iblockaccess == null) {
                    continue;
                }

                this.pos.set(i, j, k);
                IBlockData iblockdata = iblockaccess.getBlockState(this.pos);

                if (this.onlySuffocatingBlocks && !iblockdata.isSuffocating(iblockaccess, this.pos) || l == 1 && !iblockdata.hasLargeCollisionShape() || l == 2 && !iblockdata.is(Blocks.MOVING_PISTON)) {
                    continue;
                }

                VoxelShape voxelshape = this.context.getCollisionShape(iblockdata, this.collisionGetter, this.pos);

                if (voxelshape == VoxelShapes.block()) {
                    if (!this.box.intersects((double) i, (double) j, (double) k, (double) i + 1.0D, (double) j + 1.0D, (double) k + 1.0D)) {
                        continue;
                    }

                    return (T) this.resultProvider.apply(this.pos, voxelshape.move((BaseBlockPosition) this.pos));
                }

                VoxelShape voxelshape1 = voxelshape.move((BaseBlockPosition) this.pos);

                if (voxelshape1.isEmpty() || !VoxelShapes.joinIsNotEmpty(voxelshape1, this.entityShape, OperatorBoolean.AND)) {
                    continue;
                }

                return (T) this.resultProvider.apply(this.pos, voxelshape1);
            }

            return (T) this.endOfData();
        }
    }
}
