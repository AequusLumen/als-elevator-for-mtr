package org.mtr.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mtr.block.BlockPSDAPGDoorBase;

/**
 * MEE patch: robust lift-door opening. The official beta locates doors via a rotated doorway
 * AABB which is fragile; this instead scans a radius around the lift and drives every nearby
 * lift-door block entity directly. Used for the automatic door cycle when the lift stops.
 */
public final class LiftDoorControl {

	private static final java.util.Set<Long> DOORS_OPEN = new java.util.HashSet<>();

	private LiftDoorControl() {
	}

	/**
	 * Returns true exactly once per door-open cycle: on the tick the door value first exceeds the
	 * threshold after being closed. Used to play the arrival "ding" chime.
	 */
	public static boolean isDoorJustOpened(long liftId, double doorValue) {
		final boolean open = doorValue > 0.01;
		final boolean wasOpen = DOORS_OPEN.contains(liftId);
		if (open) {
			DOORS_OPEN.add(liftId);
		} else {
			DOORS_OPEN.remove(liftId);
		}
		return open && !wasOpen;
	}

	/**
	 * Drives each nearby lift-door block with the value for ITS side: door blocks in front of the
	 * car follow {@code frontValue}, blocks behind it follow {@code backValue}. This keeps a
	 * single-sided car from opening door blocks on its back side.
	 */
	public static void openNearbyDoors(Level world, double x, double y, double z, double yaw, double range, double frontValue, double backValue) {
		final double forwardX = Math.sin(yaw);
		final double forwardZ = Math.cos(yaw);
		final BlockPos min = BlockPos.containing(x - range, y - 1.0, z - range);
		final BlockPos max = BlockPos.containing(x + range, y + range + 1.0, z + range);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			final BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BlockPSDAPGDoorBase.BlockEntityBase doorBase) {
				final double dot = (pos.getX() + 0.5 - x) * forwardX + (pos.getZ() + 0.5 - z) * forwardZ;
				doorBase.setDoorValue(dot >= 0.0 ? frontValue : backValue);
			}
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

	/**
	 * Scans the same area as {@link #openNearbyDoors} and reports whether a lift-door block sits on
	 * the requested side (front = the direction the car's own door faces, back = opposite).
	 */
	public static boolean hasNearbyLiftDoorOnSide(Level world, double x, double y, double z, double yaw, double range, boolean front) {
		// The car now faces +Z (aligned with the doorway/door block), so "front" is +forward.
		final double forwardX = Math.sin(yaw);
		final double forwardZ = Math.cos(yaw);
		final BlockPos min = BlockPos.containing(x - range, y - 1.0, z - range);
		final BlockPos max = BlockPos.containing(x + range, y + range + 1.0, z + range);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (world.getBlockEntity(pos) instanceof BlockPSDAPGDoorBase.BlockEntityBase) {
				final double dot = (pos.getX() + 0.5 - x) * forwardX + (pos.getZ() + 0.5 - z) * forwardZ;
				if (front ? dot > 0.0 : dot < 0.0) {
					return true;
				}
			}
		}
		return false;
	}
}
