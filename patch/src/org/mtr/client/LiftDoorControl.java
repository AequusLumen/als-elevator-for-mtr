package org.mtr.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mtr.MTR;
import org.mtr.block.BlockPSDAPGDoorBase;

/**
 * MEE patch: robust lift-door control. The official beta locates doors via a rotated
 * doorway AABB which is fragile; this instead scans a radius around the lift and drives
 * every nearby lift-door block entity directly.
 */
public final class LiftDoorControl {

	private static long overrideLiftId;
	private static boolean overrideOpen;
	private static long debugCounter;

	private LiftDoorControl() {
	}

	public static void openNearbyDoors(Level world, double x, double y, double z, double range, double doorValue) {
		final BlockPos min = BlockPos.containing(x - range, y - 1.0, z - range);
		final BlockPos max = BlockPos.containing(x + range, y + range + 1.0, z + range);
		int count = 0;
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			final BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BlockPSDAPGDoorBase.BlockEntityBase doorBase) {
				doorBase.setDoorValue(doorValue);
				count++;
			}
		}
		if (debugCounter++ % 100 == 0) {
			MTR.LOGGER.info("[MEE-DEBUG] openNearbyDoors at ({},{},{}) value={} doors={}", (int) x, (int) y, (int) z, doorValue, count);
		}
	}

	public static boolean hasNearbyLiftDoor(Level world, double x, double y, double z, double range) {
		final BlockPos min = BlockPos.containing(x - range, y - 1.0, z - range);
		final BlockPos max = BlockPos.containing(x + range, y + range + 1.0, z + range);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (world.getBlockEntity(pos) instanceof BlockPSDAPGDoorBase.BlockEntityBase) {
				return true;
			}
		}
		return false;
	}

	public static void setDoorOverride(long liftId, boolean open) {
		overrideLiftId = liftId;
		overrideOpen = open;
	}

	public static boolean isOverrideOpen(long liftId) {
		return overrideOpen && overrideLiftId == liftId;
	}

	public static void clearOverride() {
		overrideOpen = false;
	}
}
