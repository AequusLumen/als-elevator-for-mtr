package org.mtr.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.minecraft.world.level.LevelAccessor;

public abstract class BlockWaterloggable extends Block implements SimpleWaterloggedBlock {

	public BlockWaterloggable(BlockBehaviour.Properties blockSettings) {
		super(blockSettings);
		registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext itemPlacementContext) {
		return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, itemPlacementContext.getLevel().getFluidState(itemPlacementContext.getClickedPos()).getType() == Fluids.WATER);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
//
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			world.scheduleTick(pos, Fluids.WATER, 5);
//
		}

		return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
//
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
	}
}
