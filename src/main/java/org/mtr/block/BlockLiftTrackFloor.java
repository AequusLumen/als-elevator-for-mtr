package org.mtr.block;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.mtr.MTR;
import org.mtr.generated.lang.TranslationProvider;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.packet.PacketDeleteData;
import org.mtr.packet.PacketOpenBlockEntityScreen;
import org.mtr.registry.BlockEntityTypes;
import org.mtr.registry.Items;

/**
 * MEE patch: the official 4.1.0-beta.2 returns InteractionResult.FAIL here when not holding the brush,
 * which swallows the click of the Lift Buttons Link Connector / Remover and makes lift linking impossible.
 * This returns PASS instead so item interactions (linking) can run, while keeping the brush behaviour.
 */
public class BlockLiftTrackFloor extends BlockLiftTrackBase implements EntityBlock {
	public BlockLiftTrackFloor(Properties settings) {
		super(settings);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (!world.isClientSide() && player.isHolding(Items.BRUSH.get())) {
			PacketOpenBlockEntityScreen.sendDirectlyToServer((ServerLevel) world, (ServerPlayer) player, pos);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new LiftTrackFloorBlockEntity(blockPos, blockState);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (world instanceof ServerLevel serverWorld) {
			PacketDeleteData.sendDirectlyToServerLiftFloorPosition(serverWorld, MTR.blockPosToPosition(pos));
		}

		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return IBlock.getVoxelShapeByDirection(
			0.0, 0.0, 0.0, 16.0, 16.0, 1.0, (Direction) IBlock.getStatePropertySafe(state, BlockStateProperties.HORIZONTAL_FACING)
		);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag options) {
		tooltip.add(TranslationProvider.TOOLTIP_MTR_LIFT_TRACK_FLOOR.getMutableText(new Object[0]).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public ObjectArrayList<Direction> getConnectingDirections(BlockState blockState) {
		final Direction facing = (Direction) IBlock.getStatePropertySafe(blockState, BlockStateProperties.HORIZONTAL_FACING);
		return ObjectArrayList.of(Direction.UP, Direction.DOWN, facing.getClockWise(), facing.getCounterClockWise());
	}

	public static class LiftTrackFloorBlockEntity extends BlockEntityExtension {
		private String floorNumber = "1";
		private String floorDescription = "";
		private boolean shouldDing;
		private static final String KEY_FLOOR_NUMBER = "floor_number";
		private static final String KEY_FLOOR_DESCRIPTION = "floor_description";
		private static final String KEY_SHOULD_DING = "should_ding";

		public LiftTrackFloorBlockEntity(BlockPos pos, BlockState state) {
			super((BlockEntityType) BlockEntityTypes.LIFT_TRACK_FLOOR_1.get(), pos, state);
		}

		@Override
		protected void readNbt(CompoundTag nbtCompound) {
			this.floorNumber = nbtCompound.getString("floor_number");
			this.floorDescription = nbtCompound.getString("floor_description");
			this.shouldDing = nbtCompound.getBoolean("should_ding");
		}

		@Override
		protected void writeNbt(CompoundTag nbtCompound) {
			nbtCompound.putString("floor_number", this.floorNumber);
			nbtCompound.putString("floor_description", this.floorDescription);
			nbtCompound.putBoolean("should_ding", this.shouldDing);
		}

		public void setData(String floorNumber, String floorDescription, boolean shouldDing) {
			this.floorNumber = floorNumber;
			this.floorDescription = floorDescription;
			this.shouldDing = shouldDing;
			this.setChanged();
		}

		public boolean getShouldDing() {
			return this.shouldDing;
		}

		public String getFloorNumber() {
			return this.floorNumber;
		}

		public String getFloorDescription() {
			return this.floorDescription;
		}
	}
}
