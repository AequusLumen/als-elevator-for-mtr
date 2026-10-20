package org.mtr.render;

import net.minecraft.util.Mth;

/** The CONSTANT door animation used by the 1.20.4 ModelLift1. */
public enum DoorAnimationType {
	CONSTANT;

	public static float getDoorAnimationX(DoorAnimationType type, float value) {
		return 0.0F;
	}

	public static float getDoorAnimationZ(DoorAnimationType type, int doorMax, float duration, float value, boolean opening) {
		return doorMax * Mth.clamp(value, 0.0F, 1.0F) / duration;
	}
}
