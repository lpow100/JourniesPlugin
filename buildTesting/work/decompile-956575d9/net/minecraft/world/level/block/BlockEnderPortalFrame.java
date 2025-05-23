package net.minecraft.world.level.block;

import com.google.common.base.Predicates;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.pattern.ShapeDetector;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockEnderPortalFrame extends Block {

    public static final MapCodec<BlockEnderPortalFrame> CODEC = simpleCodec(BlockEnderPortalFrame::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateBoolean HAS_EYE = BlockProperties.EYE;
    private static final VoxelShape SHAPE_EMPTY = Block.column(16.0D, 0.0D, 13.0D);
    private static final VoxelShape SHAPE_FULL = VoxelShapes.or(BlockEnderPortalFrame.SHAPE_EMPTY, Block.column(8.0D, 13.0D, 16.0D));
    private static ShapeDetector portalShape;

    @Override
    public MapCodec<BlockEnderPortalFrame> codec() {
        return BlockEnderPortalFrame.CODEC;
    }

    public BlockEnderPortalFrame(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockEnderPortalFrame.FACING, EnumDirection.NORTH)).setValue(BlockEnderPortalFrame.HAS_EYE, false));
    }

    @Override
    protected boolean useShapeForLightOcclusion(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (Boolean) iblockdata.getValue(BlockEnderPortalFrame.HAS_EYE) ? BlockEnderPortalFrame.SHAPE_FULL : BlockEnderPortalFrame.SHAPE_EMPTY;
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockEnderPortalFrame.FACING, blockactioncontext.getHorizontalDirection().getOpposite())).setValue(BlockEnderPortalFrame.HAS_EYE, false);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return (Boolean) iblockdata.getValue(BlockEnderPortalFrame.HAS_EYE) ? 15 : 0;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockEnderPortalFrame.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockEnderPortalFrame.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockEnderPortalFrame.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockEnderPortalFrame.FACING, BlockEnderPortalFrame.HAS_EYE);
    }

    public static ShapeDetector getOrCreatePortalShape() {
        if (BlockEnderPortalFrame.portalShape == null) {
            BlockEnderPortalFrame.portalShape = ShapeDetectorBuilder.start().aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?").where('?', ShapeDetectorBlock.hasState(BlockStatePredicate.ANY)).where('^', ShapeDetectorBlock.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(BlockEnderPortalFrame.HAS_EYE, Predicates.equalTo(true)).where(BlockEnderPortalFrame.FACING, Predicates.equalTo(EnumDirection.SOUTH)))).where('>', ShapeDetectorBlock.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(BlockEnderPortalFrame.HAS_EYE, Predicates.equalTo(true)).where(BlockEnderPortalFrame.FACING, Predicates.equalTo(EnumDirection.WEST)))).where('v', ShapeDetectorBlock.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(BlockEnderPortalFrame.HAS_EYE, Predicates.equalTo(true)).where(BlockEnderPortalFrame.FACING, Predicates.equalTo(EnumDirection.NORTH)))).where('<', ShapeDetectorBlock.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(BlockEnderPortalFrame.HAS_EYE, Predicates.equalTo(true)).where(BlockEnderPortalFrame.FACING, Predicates.equalTo(EnumDirection.EAST)))).build();
        }

        return BlockEnderPortalFrame.portalShape;
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
