package org.mtr.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.LevelAccessor;

public abstract class BlockDirectionalDoubleBlockBase extends Block implements IBlock {

	public BlockDirectionalDoubleBlockBase(BlockBehaviour.Properties blockSettings) {
		super(blockSettings);
	}

	@Override
  protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		return DoubleVerticalBlock.updateShape(state, direction, neighborState.is(this), super.updateShape(state, direction, neighborState, world, pos, neighborPos));
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		DoubleVerticalBlock.setPlacedBy(world, pos, state, getAdditionalState(pos, IBlock.getStatePropertySafe(state, BlockStateProperties.HORIZONTAL_FACING)));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return DoubleVerticalBlock.getStateForPlacement(ctx, getAdditionalState(ctx.getClickedPos(), ctx.getHorizontalDirection()));
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		DoubleVerticalBlock.playerWillDestroy(world, pos, state, player);
		return super.playerWillDestroy(world, pos, state, player);
	}

	protected BlockState getAdditionalState(BlockPos pos, Direction facing) {
		return defaultBlockState();
	}
}
