package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockBanner extends BlockBannerAbstract {

    public static final MapCodec<BlockBanner> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnumColor.CODEC.fieldOf("color").forGetter(BlockBannerAbstract::getColor), propertiesCodec()).apply(instance, BlockBanner::new);
    });
    public static final BlockStateInteger ROTATION = BlockProperties.ROTATION_16;
    private static final Map<EnumColor, Block> BY_COLOR = Maps.newHashMap();
    private static final VoxelShape SHAPE = Block.column(8.0D, 0.0D, 16.0D);

    @Override
    public MapCodec<BlockBanner> codec() {
        return BlockBanner.CODEC;
    }

    public BlockBanner(EnumColor enumcolor, BlockBase.Info blockbase_info) {
        super(enumcolor, blockbase_info);
        this.registerDefaultState((IBlockData) (this.stateDefinition.any()).setValue(BlockBanner.ROTATION, 0));
        BlockBanner.BY_COLOR.put(enumcolor, this);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return iworldreader.getBlockState(blockposition.below()).isSolid();
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockBanner.SHAPE;
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockBanner.ROTATION, RotationSegment.convertToSegment(blockactioncontext.getRotation() + 180.0F));
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return enumdirection == EnumDirection.DOWN && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockBanner.ROTATION, enumblockrotation.rotate((Integer) iblockdata.getValue(BlockBanner.ROTATION), 16));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return (IBlockData) iblockdata.setValue(BlockBanner.ROTATION, enumblockmirror.mirror((Integer) iblockdata.getValue(BlockBanner.ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBanner.ROTATION);
    }

    public static Block byColor(EnumColor enumcolor) {
        return (Block) BlockBanner.BY_COLOR.getOrDefault(enumcolor, Blocks.WHITE_BANNER);
    }
}
