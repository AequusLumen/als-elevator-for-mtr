package org.mtr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.mtr.client.IDrawing;
import org.mtr.core.data.Lift;

/**
 * MEE patch: renders the lift car body for MTR 4.1.0-beta.2.
 * The official beta has the ModelLift1 rendering commented out (model system rewrite).
 *
 * The car is drawn as a box using a single custom texture with four quadrants:
 *   top-left     (0.0-0.5, 0.0-0.5)  = side wall
 *   top-right    (0.5-1.0, 0.0-0.5)  = sliding door panel
 *   bottom-left  (0.0-0.5, 0.5-1.0)  = ceiling
 *   bottom-right (0.5-1.0, 0.5-1.0)  = floor
 *
 * Replace assets/mtr/textures/vehicle/lift_car.png (or ship a resource pack that overrides
 * that path) to customise the car; no code changes are needed.
 */
public final class LiftCarRenderer {

	private static final float DOOR_HALF_WIDTH = 0.75F;
	private static final float DOOR_Z_OFFSET = 0.01F;
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mtr", "textures/vehicle/lift_car.png");
	// Use the real world light so the car is not blown out; clamp to a minimum so it stays
	// visible inside dark shafts. 0xA000A0 = block 10 / sky 10.
	private static final int MIN_LIGHT = 0xA000A0;

	private static final float WALL_U1 = 0.0F, WALL_V1 = 0.0F, WALL_U2 = 0.5F, WALL_V2 = 0.5F;
	private static final float DOOR_U1 = 0.5F, DOOR_V1 = 0.0F, DOOR_U2 = 1.0F, DOOR_V2 = 0.5F;
	private static final float CEIL_U1 = 0.0F, CEIL_V1 = 0.5F, CEIL_U2 = 0.5F, CEIL_V2 = 1.0F;
	private static final float FLOOR_U1 = 0.5F, FLOOR_V1 = 0.5F, FLOOR_U2 = 1.0F, FLOOR_V2 = 1.0F;

	private LiftCarRenderer() {
	}

	public static void renderLiftCar(StoredMatrixTransformations storedMatrixTransformations, Lift lift, float doorValue, int light, boolean drawDoors) {
		final float width = (float) lift.getWidth();
		final float depth = (float) lift.getDepth();
		final float height = (float) lift.getHeight();
		final float doorAmount = Math.min(1.0F, doorValue);
		final boolean isDoubleSided = lift.getIsDoubleSided();
		final int packedLight = Math.max(light, MIN_LIGHT);

		// NOTE: QueuedRenderLayer.LIGHT maps to RenderType.beaconBeam(texture, false) which is
		// textured + translucent blending + depth write. LIGHT_TRANSLUCENT maps to
		// beaconBeam(texture, true) which is *no texture*, so it must not be used here.
		MainRenderer.scheduleRender(TEXTURE, false, QueuedRenderLayer.LIGHT, (matrixStack, vertexConsumer, offset) -> {
			storedMatrixTransformations.transform(matrixStack, offset);

			final float halfWidth = width / 2.0F;
			final float halfDepth = depth / 2.0F;
			final float doorSlide = DOOR_HALF_WIDTH * doorAmount;
			// NOTE: the render transform applies rotateX(180°) which flips Y; the car body is
			// therefore drawn with negative local Y so it appears above the lift position.
			final float top = -height;

			// Left and right walls
			drawQuadX(matrixStack, vertexConsumer, -halfWidth, top, 0.0F, -halfDepth, halfDepth, Direction.WEST, WALL_U1, WALL_V1, WALL_U2, WALL_V2, packedLight);
			drawQuadX(matrixStack, vertexConsumer, halfWidth, top, 0.0F, -halfDepth, halfDepth, Direction.EAST, WALL_U1, WALL_V1, WALL_U2, WALL_V2, packedLight);
			// Back wall
			drawQuadZ(matrixStack, vertexConsumer, -halfDepth, top, 0.0F, -halfWidth, halfWidth, Direction.NORTH, WALL_U1, WALL_V1, WALL_U2, WALL_V2, packedLight);
			// Front wall side strips
			drawQuadZ(matrixStack, vertexConsumer, halfDepth, top, 0.0F, -halfWidth, -DOOR_HALF_WIDTH, Direction.SOUTH, WALL_U1, WALL_V1, WALL_U2, WALL_V2, packedLight);
			drawQuadZ(matrixStack, vertexConsumer, halfDepth, top, 0.0F, DOOR_HALF_WIDTH, halfWidth, Direction.SOUTH, WALL_U1, WALL_V1, WALL_U2, WALL_V2, packedLight);

			// Sliding doors (front) -- skipped when real lift-door blocks are present at the car,
			// otherwise the block doors and these panels would overlap (z-fighting / clipping).
			if (drawDoors) {
				drawQuadZ(matrixStack, vertexConsumer, halfDepth + DOOR_Z_OFFSET, top, 0.0F, -DOOR_HALF_WIDTH - doorSlide, -doorSlide, Direction.SOUTH, DOOR_U1, DOOR_V1, DOOR_U2, DOOR_V2, packedLight);
				drawQuadZ(matrixStack, vertexConsumer, halfDepth + DOOR_Z_OFFSET, top, 0.0F, doorSlide, DOOR_HALF_WIDTH + doorSlide, Direction.SOUTH, DOOR_U1, DOOR_V1, DOOR_U2, DOOR_V2, packedLight);
				if (isDoubleSided) {
					drawQuadZ(matrixStack, vertexConsumer, -halfDepth - DOOR_Z_OFFSET, top, 0.0F, -DOOR_HALF_WIDTH - doorSlide, -doorSlide, Direction.NORTH, DOOR_U1, DOOR_V1, DOOR_U2, DOOR_V2, packedLight);
					drawQuadZ(matrixStack, vertexConsumer, -halfDepth - DOOR_Z_OFFSET, top, 0.0F, doorSlide, DOOR_HALF_WIDTH + doorSlide, Direction.NORTH, DOOR_U1, DOOR_V1, DOOR_U2, DOOR_V2, packedLight);
				}
			}

			// Ceiling and floor
			drawQuadHorizontal(matrixStack, vertexConsumer, -halfWidth, -halfDepth, halfWidth, halfDepth, top, Direction.DOWN, CEIL_U1, CEIL_V1, CEIL_U2, CEIL_V2, packedLight);
			drawQuadHorizontal(matrixStack, vertexConsumer, -halfWidth, -halfDepth, halfWidth, halfDepth, 0.0F, Direction.UP, FLOOR_U1, FLOOR_V1, FLOOR_U2, FLOOR_V2, packedLight);

			matrixStack.popPose();
		});
	}

	// Quad on a plane of constant X, drawn double-sided.
	// The second (reverse) winding must repeat the SAME uv->position mapping, otherwise the
	// texture is rotated 90 degrees on the back face. drawTexture assigns uv by vertex ORDER:
	// v1->vertex1, v2->vertex2, v2->vertex3, v1->vertex4, so the reverse winding swaps u1/u2.
	private static void drawQuadX(PoseStack m, VertexConsumer vc, float x, float y1, float y2, float z1, float z2, Direction facing, float u1, float v1, float u2, float v2, int light) {
		IDrawing.drawTexture(m, vc, x, y1, z1, x, y2, z1, x, y2, z2, x, y1, z2, u1, v1, u2, v2, facing, -1, light);
		IDrawing.drawTexture(m, vc, x, y1, z2, x, y2, z2, x, y2, z1, x, y1, z1, u2, v1, u1, v2, facing, -1, light);
	}

	// Quad on a plane of constant Z, drawn double-sided
	private static void drawQuadZ(PoseStack m, VertexConsumer vc, float z, float y1, float y2, float x1, float x2, Direction facing, float u1, float v1, float u2, float v2, int light) {
		IDrawing.drawTexture(m, vc, x1, y1, z, x1, y2, z, x2, y2, z, x2, y1, z, u1, v1, u2, v2, facing, -1, light);
		IDrawing.drawTexture(m, vc, x2, y1, z, x2, y2, z, x1, y2, z, x1, y1, z, u2, v1, u1, v2, facing, -1, light);
	}

	// Horizontal quad (constant Y), drawn double-sided
	private static void drawQuadHorizontal(PoseStack m, VertexConsumer vc, float x1, float z1, float x2, float z2, float y, Direction facing, float u1, float v1, float u2, float v2, int light) {
		IDrawing.drawTexture(m, vc, x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, u1, v1, u2, v2, facing, -1, light);
		IDrawing.drawTexture(m, vc, x2, y, z1, x2, y, z2, x1, y, z2, x1, y, z1, u2, v1, u1, v2, facing, -1, light);
	}
}
