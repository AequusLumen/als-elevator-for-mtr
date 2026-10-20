package org.mtr.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraft.world.level.LevelAccessor;

public class BlockPlatformSlab extends SlabBlock implements PlatformHelper {

	public BlockPlatformSlab(BlockBehaviour.Properties blockSettings) {
		super(blockSettings);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
//
		return PlatformHelper.getActualState(world, pos, state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		final BlockState slabState = super.getStateForPlacement(ctx);
		return (slabState == null ? defaultBlockState() : slabState).setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(DOOR_TYPE);
		builder.add(SIDE);
	}
}
