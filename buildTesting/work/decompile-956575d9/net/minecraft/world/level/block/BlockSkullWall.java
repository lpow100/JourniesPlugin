package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockSkullWall extends BlockSkullAbstract {

    public static final MapCodec<BlockSkullWall> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockSkull.a.CODEC.fieldOf("kind").forGetter(BlockSkullAbstract::getType), propertiesCodec()).apply(instance, BlockSkullWall::new);
    });
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    private static final Map<EnumDirection, VoxelShape> SHAPES = VoxelShapes.rotateHorizontal(Block.boxZ(8.0D, 8.0D, 16.0D));

    @Override
    public MapCodec<? extends BlockSkullWall> codec() {
        return BlockSkullWall.CODEC;
    }

    protected BlockSkullWall(BlockSkull.a blockskull_a, BlockBase.Info blockbase_info) {
        super(blockskull_a, blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(BlockSkullWall.FACING, EnumDirection.NORTH));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) BlockSkullWall.SHAPES.get(iblockdata.getValue(BlockSkullWall.FACING));
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = super.getStateForPlacement(blockactioncontext);
        IBlockAccess iblockaccess = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        EnumDirection[] aenumdirection = blockactioncontext.getNearestLookingDirections();

        for (EnumDirection enumdirection : aenumdirection) {
            if (enumdirection.getAxis().isHorizontal()) {
                EnumDirection enumdirection1 = enumdirection.getOpposite();

                iblockdata = (IBlockData) iblockdata.setValue(BlockSkullWall.FACING, enumdirection1);
                if (!iblockaccess.getBlockState(blockposition.relative(enumdirection)).canBeReplaced(blockactioncontext)) {
                    return iblockdata;
                }
            }
        }

        return null;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockSkullWall.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockSkullWall.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockSkullWall.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        super.createBlockStateDefinition(blockstatelist_a);
        blockstatelist_a.add(BlockSkullWall.FACING);
    }
}
