package org.mtr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.mtr.core.data.Lift;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.resource.RenderStage;

import java.util.ArrayList;
import java.util.List;

/**
 * Faithful port of the 1.20.4 (MTR 4.0.5) ModelLift1. Every part, UV, rotation and the render
 * loop is reproduced verbatim; only the removed MTR mapping layer is replaced with vanilla
 * ModelPart, and the pivot(0,24,0) + baseTransform(-1.5) pair (which cancels out) is flattened.
 */
public final class ModelLift1 {

	private static final int TEXTURE_WIDTH = 128;
	private static final int TEXTURE_HEIGHT = 128;
	private static final int DEFAULT_LIGHT_INTERIOR = 15728816;
	private static final ResourceLocation LIFT_1_TEXTURE = ResourceLocation.fromNamespaceAndPath("mtr", "textures/vehicle/lift_1.png");
	private static final Object2ObjectOpenHashMap<String, ModelLift1> CACHE = new Object2ObjectOpenHashMap<>();

	/** Renders the car for the given lift, reusing a cached model per size. */
	public static void renderLiftCar(StoredMatrixTransformations storedMatrixTransformations, Lift lift, int light, float doorValueFront, float doorValueBack) {
		final String key = Math.round(lift.getHeight() * 2.0F) + "," + Math.round(lift.getWidth()) + "," + Math.round(lift.getDepth()) + "," + lift.getIsDoubleSided();
		ModelLift1 model = CACHE.get(key);
		if (model == null) {
			model = new ModelLift1((int) Math.round(lift.getHeight() * 2.0), (int) Math.round(lift.getWidth()), (int) Math.round(lift.getDepth()), lift.getIsDoubleSided());
			CACHE.put(key, model);
		}
		model.render(storedMatrixTransformations, LIFT_1_TEXTURE, light, doorValueFront, doorValueBack);
	}

	private final PartRef main;
	private final PartRef main_ceiling;
	private final PartRef main_edge;
	private final PartRef main_edge_wall;
	private final PartRef main_edge_ceiling;
	private final PartRef main_corner;
	private final PartRef main_corner_wall;
	private final PartRef main_corner_ceiling;
	private final PartRef main_exterior;
	private final PartRef main_exterior_ceiling;
	private final PartRef main_exterior_edge;
	private final PartRef main_exterior_edge_ceiling;
	private final PartRef main_exterior_corner;
	private final PartRef main_exterior_corner_wall;
	private final PartRef main_exterior_corner_ceiling;
	private final PartRef main_light;
	private final PartRef door;
	private final PartRef door_left;
	private final PartRef door_right;
	private final PartRef door_wall;
	private final PartRef door_ceiling;
	private final PartRef door_exterior;
	private final PartRef door_left_exterior;
	private final PartRef door_right_exterior;
	private final PartRef door_wall_exterior;
	private final PartRef door_ceiling_exterior;
	private final PartRef wall_patch;
	private final PartRef wall_patch_wall;
	private final int heightCount;
	private final int heightOffset;
	private final int width;
	private final int depth;
	private final boolean isDoubleSided;

	private final List<PartRef> allParts = new ArrayList<>();
	private final MeshDefinition mesh = new MeshDefinition();
	private final PartDefinition root = mesh.getRoot();
	private int nameCounter;

	public ModelLift1(int height, int width, int depth, boolean isDoubleSided) {
		this.heightCount = height - 4;
		this.heightOffset = -this.heightCount * 8;
		this.width = width;
		this.depth = depth;
		this.isDoubleSided = isDoubleSided;

		this.main = createPart();
		this.main.setTextureUVOffset(0, 34).addCuboid(-8.0F, 0.0F, -8.0F, 16, 0, 16);
		this.main_ceiling = createPart();
		this.main_ceiling.setTextureUVOffset(79, 44).addCuboid(-8.0F, -32.0F, -8.0F, 16, 0, 16);
		this.main_edge = createPart();
		this.main_edge.setTextureUVOffset(18, 44).addCuboid(-4.0F, 0.0F, -8.0F, 8, 0, 6);
		this.main_edge.setTextureUVOffset(76, 33).addCuboid(-4.0F, -32.0F, -3.0F, 8, 32, 1);
		this.main_edge.setTextureUVOffset(28, 52).addCuboid(-4.0F, -13.0F, -4.0F, 8, 1, 1);
		this.main_edge.setTextureUVOffset(26, 50).addCuboid(-4.0F, -2.0F, -4.0F, 8, 1, 1);
		this.main_edge_wall = createPart();
		this.main_edge_wall.setTextureUVOffset(76, 33).addCuboid(-4.0F, -40.0F, -3.0F, 8, 8, 1);
		this.main_edge_ceiling = createPart();
		this.main_edge_ceiling.setTextureUVOffset(97, 44).addCuboid(-4.0F, -32.0F, -8.0F, 8, 0, 6);
		this.main_corner = createPart();
		this.main_corner.setTextureUVOffset(20, 44).addCuboid(2.0F, 0.0F, -8.0F, 6, 0, 6);
		this.main_corner.setTextureUVOffset(112, 62).addCuboid(3.0F, -32.0F, -3.0F, 5, 32, 1);
		this.main_corner.setTextureUVOffset(29, 52).addCuboid(5.0F, -13.0F, -4.0F, 3, 1, 1);
		this.main_corner.setTextureUVOffset(27, 50).addCuboid(5.0F, -2.0F, -4.0F, 3, 1, 1);
		this.main_corner.setTextureUVOffset(104, 68).addCuboid(2.0F, -32.0F, -3.0F, 1, 32, 1);
		PartRef handrailBottom = this.main_corner.addChild();
		handrailBottom.setRotation(0.0F, -1.5708F, 0.0F);
		handrailBottom.setTextureUVOffset(36, 50).addCuboid(-8.0F, -2.0F, -4.0F, 3, 1, 1);
		handrailBottom.setTextureUVOffset(38, 52).addCuboid(-8.0F, -13.0F, -4.0F, 3, 1, 1);
		handrailBottom.setTextureUVOffset(112, 62).addCuboid(-8.0F, -32.0F, -3.0F, 5, 32, 1);
		this.main_corner_wall = createPart();
		this.main_corner_wall.setTextureUVOffset(112, 62).addCuboid(3.0F, -40.0F, -3.0F, 5, 8, 1);
		this.main_corner_wall.setTextureUVOffset(104, 68).addCuboid(2.0F, -40.0F, -3.0F, 1, 8, 1);
		PartRef wallR1 = this.main_corner_wall.addChild();
		wallR1.setRotation(0.0F, -1.5708F, 0.0F);
		wallR1.setTextureUVOffset(112, 62).addCuboid(-8.0F, -40.0F, -3.0F, 5, 8, 1);
		this.main_corner_ceiling = createPart();
		this.main_corner_ceiling.setTextureUVOffset(99, 44).addCuboid(2.0F, -32.0F, -8.0F, 6, 0, 6);
		this.main_exterior = createPart();
		this.main_exterior.setTextureUVOffset(0, 17).addCuboid(-8.0F, 0.0F, -8.0F, 16, 1, 16);
		this.main_exterior_ceiling = createPart();
		this.main_exterior_ceiling.setTextureUVOffset(0, 0).addCuboid(-8.0F, -33.0F, -8.0F, 16, 1, 16);
		this.main_exterior_edge = createPart();
		this.main_exterior_edge.setTextureUVOffset(18, 27).addCuboid(-4.0F, 0.0F, -8.0F, 8, 1, 6);
		this.main_exterior_edge_ceiling = createPart();
		this.main_exterior_edge_ceiling.setTextureUVOffset(18, 10).addCuboid(-4.0F, -33.0F, -8.0F, 8, 1, 6);
		this.main_exterior_corner = createPart();
		this.main_exterior_corner.setTextureUVOffset(20, 27).addCuboid(2.0F, 0.0F, -8.0F, 6, 1, 6);
		this.main_exterior_corner_wall = createPart();
		this.main_exterior_corner_wall.setTextureUVOffset(108, 68).addCuboid(2.0F, -40.0F, -3.0F, 1, 8, 1);
		this.main_exterior_corner_ceiling = createPart();
		this.main_exterior_corner_ceiling.setTextureUVOffset(20, 10).addCuboid(2.0F, -33.0F, -8.0F, 6, 1, 6);
		this.main_light = createPart();
		this.main_light.setTextureUVOffset(79, 28).addCuboid(-8.0F, -32.5F, -8.0F, 16, 0, 16);
		this.door = createPart();
		this.door.setTextureUVOffset(90, 66).addCuboid(-16.0F, -32.0F, 4.0F, 4, 32, 3);
		this.door.setTextureUVOffset(14, 84).addCuboid(12.0F, -32.0F, 4.0F, 4, 32, 3);
		this.door.setTextureUVOffset(20, 101).addCuboid(-16.0F, 0.0F, 0.0F, 32, 0, 8);
		this.door_left = createPart();
		this.door_left.setTextureUVOffset(52, 68).addCuboid(-12.0F, -32.0F, 6.0F, 12, 32, 0);
		this.door_right = createPart();
		this.door_right.setTextureUVOffset(28, 68).addCuboid(0.0F, -32.0F, 6.0F, 12, 32, 0);
		this.door_wall = createPart();
		this.door_wall.setTextureUVOffset(48, 0).addCuboid(-16.0F, -40.0F, 4.0F, 32, 8, 3);
		this.door_ceiling = createPart();
		this.door_ceiling.setTextureUVOffset(20, 101).addCuboid(-16.0F, -32.0F, 0.0F, 32, 0, 8);
		this.door_exterior = createPart();
		this.door_exterior.setTextureUVOffset(0, 84).addCuboid(-16.0F, -32.0F, 4.0F, 4, 32, 3);
		this.door_exterior.setTextureUVOffset(76, 66).addCuboid(12.0F, -32.0F, 4.0F, 4, 32, 3);
		this.door_exterior.setTextureUVOffset(28, 109).addCuboid(-16.0F, 0.0F, 0.0F, 32, 1, 8);
		this.door_left_exterior = createPart();
		this.door_left_exterior.setTextureUVOffset(0, 50).addCuboid(-12.0F, -32.0F, 6.0F, 12, 32, 2);
		this.door_right_exterior = createPart();
		this.door_right_exterior.setTextureUVOffset(48, 34).addCuboid(0.0F, -32.0F, 6.0F, 12, 32, 2);
		this.door_wall_exterior = createPart();
		this.door_wall_exterior.setTextureUVOffset(48, 17).addCuboid(-16.0F, -40.0F, 5.0F, 32, 8, 3);
		this.door_ceiling_exterior = createPart();
		this.door_ceiling_exterior.setTextureUVOffset(0, 119).addCuboid(-16.0F, -33.0F, 0.0F, 32, 1, 8);
		this.wall_patch = createPart();
		PartRef wallR2 = this.wall_patch.addChild();
		wallR2.setRotation(0.0F, -1.5708F, 0.0F);
		wallR2.setTextureUVOffset(108, 95).addCuboid(0.0F, -32.0F, 13.0F, 4, 32, 1);
		wallR2.setTextureUVOffset(30, 50).addCuboid(0.0F, -2.0F, 12.0F, 4, 1, 1);
		wallR2.setTextureUVOffset(32, 52).addCuboid(0.0F, -13.0F, 12.0F, 4, 1, 1);
		PartRef wallR3 = this.wall_patch.addChild();
		wallR3.setRotation(0.0F, 1.5708F, 0.0F);
		wallR3.setTextureUVOffset(108, 95).addCuboid(-4.0F, -32.0F, 13.0F, 4, 32, 1);
		wallR3.setTextureUVOffset(30, 50).addCuboid(-4.0F, -2.0F, 12.0F, 4, 1, 1);
		wallR3.setTextureUVOffset(32, 52).addCuboid(-4.0F, -13.0F, 12.0F, 4, 1, 1);
		this.wall_patch_wall = createPart();
		PartRef wallR4 = this.wall_patch_wall.addChild();
		wallR4.setRotation(0.0F, -1.5708F, 0.0F);
		wallR4.setTextureUVOffset(108, 95).addCuboid(0.0F, -40.0F, 13.0F, 4, 8, 1);
		PartRef wallR5 = this.wall_patch_wall.addChild();
		wallR5.setRotation(0.0F, 1.5708F, 0.0F);
		wallR5.setTextureUVOffset(108, 95).addCuboid(-4.0F, -40.0F, 13.0F, 4, 8, 1);

		buildModel();
	}

	public void render(StoredMatrixTransformations storedMatrixTransformations, ResourceLocation texture, int light, float doorValueFront, float doorValueBack) {
		final float doorLeftZ = DoorAnimationType.getDoorAnimationZ(DoorAnimationType.CONSTANT, getDoorMax(), getDoorDuration(), doorValueFront / 0.75F, false);
		final float doorRightZ = DoorAnimationType.getDoorAnimationZ(DoorAnimationType.CONSTANT, getDoorMax(), getDoorDuration(), doorValueBack / 0.75F, false);
		final int lightOnInteriorLevel = DEFAULT_LIGHT_INTERIOR;
		final int lightOnGlowingLevel = GraphicsHolder.getDefaultLight();

		MainRenderer.scheduleRender(texture, false, QueuedRenderLayer.LIGHT, (matrixStack, vertexConsumer, offset) -> {
			storedMatrixTransformations.transform(matrixStack, offset);
			final GraphicsHolder graphicsHolder = new GraphicsHolder(matrixStack, vertexConsumer);
			render(graphicsHolder, RenderStage.LIGHT, lightOnGlowingLevel, 0.0F, 0.0F, doorLeftZ, doorRightZ, 0, 1, true, true);
			matrixStack.popPose();
		});
		MainRenderer.scheduleRender(texture, false, QueuedRenderLayer.INTERIOR, (matrixStack, vertexConsumer, offset) -> {
			storedMatrixTransformations.transform(matrixStack, offset);
			final GraphicsHolder graphicsHolder = new GraphicsHolder(matrixStack, vertexConsumer);
			render(graphicsHolder, RenderStage.INTERIOR, lightOnInteriorLevel, 0.0F, 0.0F, doorLeftZ, doorRightZ, 0, 1, true, true);
			matrixStack.popPose();
		});
		MainRenderer.scheduleRender(texture, false, QueuedRenderLayer.EXTERIOR, (matrixStack, vertexConsumer, offset) -> {
			storedMatrixTransformations.transform(matrixStack, offset);
			final GraphicsHolder graphicsHolder = new GraphicsHolder(matrixStack, vertexConsumer);
			render(graphicsHolder, RenderStage.EXTERIOR, light, 0.0F, 0.0F, doorLeftZ, doorRightZ, 0, 1, true, true);
			matrixStack.popPose();
		});
		MainRenderer.scheduleRender(texture, false, QueuedRenderLayer.LIGHT_TRANSLUCENT, (matrixStack, vertexConsumer, offset) -> {
			storedMatrixTransformations.transform(matrixStack, offset);
			final GraphicsHolder graphicsHolder = new GraphicsHolder(matrixStack, vertexConsumer);
			render(graphicsHolder, RenderStage.ALWAYS_ON_LIGHT, GraphicsHolder.getDefaultLight(), 0.0F, 0.0F, doorLeftZ, doorRightZ, 0, 1, true, true);
			matrixStack.popPose();
		});
	}

	private void render(GraphicsHolder graphicsHolder, RenderStage renderStage, int light, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, int currentCar, int trainCars, boolean head1IsFront, boolean renderDetails) {
		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= depth; j++) {
				final float x = (i - width / 2.0F) * 16.0F;
				final float z = (j - depth / 2.0F) * 16.0F;
				final boolean edge1X = i == 0;
				final boolean edge2X = i == width;
				final boolean edge1Z = j == 0;
				final boolean edge2Z = j == depth;

				if (renderStage == RenderStage.LIGHT) {
					if (!edge1X && !edge2X && !edge1Z && !edge2Z) {
						renderOnce(main_light, graphicsHolder, light, x, heightOffset, z);
					}
				} else if (renderStage == RenderStage.INTERIOR || renderStage == RenderStage.EXTERIOR) {
					final PartRef mainPiece = renderStage == RenderStage.INTERIOR ? main : main_exterior;
					final PartRef mainCeilingPiece = renderStage == RenderStage.INTERIOR ? main_ceiling : main_exterior_ceiling;
					final PartRef mainEdgePiece = renderStage == RenderStage.INTERIOR ? main_edge : main_exterior_edge;
					final PartRef mainEdgeCeilingPiece = renderStage == RenderStage.INTERIOR ? main_edge_ceiling : main_exterior_edge_ceiling;
					final PartRef mainCornerPiece = renderStage == RenderStage.INTERIOR ? main_corner : main_exterior_corner;
					final PartRef mainCornerWallPiece = renderStage == RenderStage.INTERIOR ? main_corner_wall : main_exterior_corner_wall;
					final PartRef mainCornerCeilingPiece = renderStage == RenderStage.INTERIOR ? main_corner_ceiling : main_exterior_corner_ceiling;

					if (!edge1X && !edge2X && !edge1Z && !edge2Z) {
						renderOnce(mainPiece, graphicsHolder, light, x, z);
						renderOnce(mainCeilingPiece, graphicsHolder, light, x, heightOffset, z);
					}
					if (edge1X && !edge2X && !edge1Z && !edge2Z) {
						mainEdgePiece.render(graphicsHolder, x, z - 4.0F, (float) (-Math.PI / 2), light);
						mainEdgePiece.render(graphicsHolder, x, z + 4.0F, (float) (-Math.PI / 2), light);
						if (renderStage == RenderStage.INTERIOR) {
							renderWall(main_edge_wall, graphicsHolder, x, z - 4.0F, (float) (-Math.PI / 2), light);
							renderWall(main_edge_wall, graphicsHolder, x, z + 4.0F, (float) (-Math.PI / 2), light);
						}
						mainEdgeCeilingPiece.render(graphicsHolder, x, heightOffset, z - 4.0F, (float) (-Math.PI / 2), light);
						mainEdgeCeilingPiece.render(graphicsHolder, x, heightOffset, z + 4.0F, (float) (-Math.PI / 2), light);
					}
					if (!edge1X && edge2X && !edge1Z && !edge2Z) {
						mainEdgePiece.render(graphicsHolder, x, z - 4.0F, (float) (Math.PI / 2), light);
						mainEdgePiece.render(graphicsHolder, x, z + 4.0F, (float) (Math.PI / 2), light);
						if (renderStage == RenderStage.INTERIOR) {
							renderWall(main_edge_wall, graphicsHolder, x, z - 4.0F, (float) (Math.PI / 2), light);
							renderWall(main_edge_wall, graphicsHolder, x, z + 4.0F, (float) (Math.PI / 2), light);
						}
						mainEdgeCeilingPiece.render(graphicsHolder, x, heightOffset, z - 4.0F, (float) (Math.PI / 2), light);
						mainEdgeCeilingPiece.render(graphicsHolder, x, heightOffset, z + 4.0F, (float) (Math.PI / 2), light);
					}
					if (!edge1X && !edge2X && !edge1Z && edge2Z && !isDoubleSided) {
						renderOnce(mainEdgePiece, graphicsHolder, light, x - 4.0F, z);
						renderOnce(mainEdgePiece, graphicsHolder, light, x + 4.0F, z);
						if (renderStage == RenderStage.INTERIOR) {
							renderWallOnce(main_edge_wall, graphicsHolder, x - 4.0F, z, light);
							renderWallOnce(main_edge_wall, graphicsHolder, x + 4.0F, z, light);
						}
						renderOnce(mainEdgeCeilingPiece, graphicsHolder, light, x - 4.0F, heightOffset, z);
						renderOnce(mainEdgeCeilingPiece, graphicsHolder, light, x + 4.0F, heightOffset, z);
					}
					if (edge1X && !edge2X && !edge1Z && edge2Z && (width > 2 || !isDoubleSided)) {
						mainCornerPiece.render(graphicsHolder, x, z, 0.0F, light);
						if (renderStage == RenderStage.INTERIOR) {
							renderWall(mainCornerWallPiece, graphicsHolder, x, z, 0.0F, light);
						}
						mainCornerCeilingPiece.render(graphicsHolder, x, heightOffset, z, 0.0F, light);
					}
					if (!edge1X && edge2X && !edge1Z && edge2Z && (width > 2 || !isDoubleSided)) {
						mainCornerPiece.render(graphicsHolder, x, z, (float) (Math.PI / 2), light);
						if (renderStage == RenderStage.INTERIOR) {
							renderWall(mainCornerWallPiece, graphicsHolder, x, z, (float) (Math.PI / 2), light);
						}
						mainCornerCeilingPiece.render(graphicsHolder, x, heightOffset, z, (float) (Math.PI / 2), light);
					}
					if (edge1X && !edge2X && edge1Z && !edge2Z && width > 2) {
						mainCornerPiece.render(graphicsHolder, x, z, (float) (-Math.PI / 2), light);
						if (renderStage == RenderStage.INTERIOR) {
							renderWall(mainCornerWallPiece, graphicsHolder, x, z, (float) (-Math.PI / 2), light);
						}
						mainCornerCeilingPiece.render(graphicsHolder, x, heightOffset, z, (float) (-Math.PI / 2), light);
					}
					if (!edge1X && edge2X && edge1Z && !edge2Z && width > 2) {
						mainCornerPiece.render(graphicsHolder, x, z, (float) Math.PI, light);
						if (renderStage == RenderStage.INTERIOR) {
							renderWall(mainCornerWallPiece, graphicsHolder, x, z, (float) Math.PI, light);
						}
						mainCornerCeilingPiece.render(graphicsHolder, x, heightOffset, z, (float) Math.PI, light);
					}
				}
			}
		}

		if (renderStage == RenderStage.INTERIOR || renderStage == RenderStage.EXTERIOR) {
			final PartRef doorLeftPiece = renderStage == RenderStage.INTERIOR ? door_left : door_left_exterior;
			final PartRef doorRightPiece = renderStage == RenderStage.INTERIOR ? door_right : door_right_exterior;
			final PartRef doorPiece = renderStage == RenderStage.INTERIOR ? door : door_exterior;
			final PartRef doorWallPiece = renderStage == RenderStage.INTERIOR ? door_wall : door_wall_exterior;
			final PartRef doorCeilingPiece = renderStage == RenderStage.INTERIOR ? door_ceiling : door_ceiling_exterior;
			final PartRef mainEdgePiece = renderStage == RenderStage.INTERIOR ? main_edge : main_exterior_edge;
			final PartRef mainEdgeCeilingPiece = renderStage == RenderStage.INTERIOR ? main_edge_ceiling : main_exterior_edge_ceiling;

			renderOnceFlipped(doorLeftPiece, graphicsHolder, light, -doorLeftZ, 8 - depth * 8);
			renderOnceFlipped(doorRightPiece, graphicsHolder, light, doorLeftZ, 8 - depth * 8);
			renderOnceFlipped(doorPiece, graphicsHolder, light, 8 - depth * 8);
			renderWallOnceFlipped(doorWallPiece, graphicsHolder, 0.0F, 8 - depth * 8, light);
			renderOnceFlipped(doorCeilingPiece, graphicsHolder, light, 0.0F, heightOffset, 8 - depth * 8);
			if (isDoubleSided) {
				renderOnce(doorLeftPiece, graphicsHolder, light, -doorRightZ, -8 + depth * 8);
				renderOnce(doorRightPiece, graphicsHolder, light, doorRightZ, -8 + depth * 8);
				renderOnce(doorPiece, graphicsHolder, light, -8 + depth * 8);
				renderWallOnce(doorWallPiece, graphicsHolder, 0.0F, -8 + depth * 8, light);
				renderOnce(doorCeilingPiece, graphicsHolder, light, 0.0F, heightOffset, -8 + depth * 8);
			}

			if (renderStage == RenderStage.INTERIOR && width == 2) {
				renderOnceFlipped(wall_patch, graphicsHolder, light, 8 - depth * 8);
				renderWallOnceFlipped(wall_patch_wall, graphicsHolder, 0.0F, 8 - depth * 8, light);
				if (isDoubleSided) {
					renderOnce(wall_patch, graphicsHolder, light, -8 + depth * 8);
					renderWallOnce(wall_patch_wall, graphicsHolder, 0.0F, -8 + depth * 8, light);
				}
			}

			for (int i = 1; i < width - 2; i++) {
				renderOnceFlipped(mainEdgePiece, graphicsHolder, light, i * 8 - width * 8 + 4, -depth * 8);
				renderOnceFlipped(mainEdgePiece, graphicsHolder, light, -i * 8 + width * 8 - 4, -depth * 8);
				if (renderStage == RenderStage.INTERIOR) {
					renderWallOnceFlipped(main_edge_wall, graphicsHolder, i * 8 - width * 8 + 4, -depth * 8, light);
					renderWallOnceFlipped(main_edge_wall, graphicsHolder, -i * 8 + width * 8 - 4, -depth * 8, light);
				}
				renderOnceFlipped(mainEdgeCeilingPiece, graphicsHolder, light, i * 8 - width * 8 + 4, heightOffset, -depth * 8);
				renderOnceFlipped(mainEdgeCeilingPiece, graphicsHolder, light, -i * 8 + width * 8 - 4, heightOffset, -depth * 8);
				if (isDoubleSided) {
					renderOnce(mainEdgePiece, graphicsHolder, light, i * 8 - width * 8 + 4, depth * 8);
					renderOnce(mainEdgePiece, graphicsHolder, light, -i * 8 + width * 8 - 4, depth * 8);
					if (renderStage == RenderStage.INTERIOR) {
						renderWallOnce(main_edge_wall, graphicsHolder, i * 8 - width * 8 + 4, depth * 8, light);
						renderWallOnce(main_edge_wall, graphicsHolder, -i * 8 + width * 8 - 4, depth * 8, light);
					}
					renderOnce(mainEdgeCeilingPiece, graphicsHolder, light, i * 8 - width * 8 + 4, heightOffset, depth * 8);
					renderOnce(mainEdgeCeilingPiece, graphicsHolder, light, -i * 8 + width * 8 - 4, heightOffset, depth * 8);
				}
			}
		}
	}

	private void renderWall(PartRef model, GraphicsHolder graphicsHolder, float x, float z, float rotateY, int light) {
		for (int i = 0; i < heightCount; i++) {
			model.render(graphicsHolder, x, -i * 8, z, rotateY, light);
		}
	}

	private void renderWallOnce(PartRef model, GraphicsHolder graphicsHolder, float x, float z, int light) {
		for (int i = 0; i < heightCount; i++) {
			renderOnce(model, graphicsHolder, light, x, -i * 8, z);
		}
	}

	private void renderWallOnceFlipped(PartRef model, GraphicsHolder graphicsHolder, float x, float z, int light) {
		for (int i = 0; i < heightCount; i++) {
			renderOnceFlipped(model, graphicsHolder, light, x, -i * 8, z);
		}
	}

	private static void renderOnce(PartRef bone, GraphicsHolder graphicsHolder, int light, float position) {
		bone.render(graphicsHolder, 0.0F, position, 0.0F, light);
	}

	private static void renderOnce(PartRef bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionZ) {
		bone.render(graphicsHolder, positionX, positionZ, 0.0F, light);
	}

	private static void renderOnce(PartRef bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionY, float positionZ) {
		bone.render(graphicsHolder, positionX, positionY, positionZ, 0.0F, light);
	}

	private static void renderOnceFlipped(PartRef bone, GraphicsHolder graphicsHolder, int light, float position) {
		bone.render(graphicsHolder, 0.0F, position, (float) Math.PI, light);
	}

	private static void renderOnceFlipped(PartRef bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionZ) {
		bone.render(graphicsHolder, -positionX, positionZ, (float) Math.PI, light);
	}

	private static void renderOnceFlipped(PartRef bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionY, float positionZ) {
		bone.render(graphicsHolder, -positionX, positionY, positionZ, (float) Math.PI, light);
	}

	private int getDoorMax() {
		return 6;
	}

	private float getDoorDuration() {
		return 0.5F;
	}

	private PartRef createPart() {
		final String name = "part" + (nameCounter++);
		final PartRef part = new PartRef(root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO), new ArrayList<>(List.of(name)));
		allParts.add(part);
		return part;
	}

	private void buildModel() {
		final ModelPart baked = LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT).bakeRoot();
		for (PartRef part : allParts) {
			ModelPart current = baked;
			for (String name : part.path) {
				current = current.getChild(name);
			}
			part.modelPart = current;
		}
	}

	/** A model part that can be built (via a PartDefinition) and later rendered (via a baked ModelPart). */
	private static final class PartRef {
		private static final java.util.concurrent.atomic.AtomicInteger COUNTER = new java.util.concurrent.atomic.AtomicInteger();
		private final PartDefinition def;
		private final List<String> path;
		private ModelPart modelPart;
		private float pivotX, pivotY, pivotZ, rotationX, rotationY, rotationZ;
		private int textureU, textureV;

		private PartRef(PartDefinition def, List<String> path) {
			this.def = def;
			this.path = path;
		}

		private void setPivot(float x, float y, float z) {
			this.pivotX = x;
			this.pivotY = y;
			this.pivotZ = z;
		}

		private void setRotation(float x, float y, float z) {
			this.rotationX = x;
			this.rotationY = y;
			this.rotationZ = z;
		}

		private PartRef setTextureUVOffset(int u, int v) {
			this.textureU = u;
			this.textureV = v;
			return this;
		}

		private void addCuboid(float x, float y, float z, int sizeX, int sizeY, int sizeZ) {
			def.addOrReplaceChild("cube" + COUNTER.incrementAndGet(), CubeListBuilder.create().texOffs(textureU, textureV).addBox(x, y, z, sizeX, sizeY, sizeZ, CubeDeformation.NONE), PartPose.offsetAndRotation(pivotX, pivotY, pivotZ, rotationX, rotationY, rotationZ));
		}

		private PartRef addChild() {
			final String name = "child" + COUNTER.incrementAndGet();
			final List<String> childPath = new ArrayList<>(path);
			childPath.add(name);
			return new PartRef(def.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO), childPath);
		}

		private void render(GraphicsHolder graphicsHolder, float x, float z, float rotateY, int light) {
			render(graphicsHolder, x, 0.0F, z, rotateY, light);
		}

		private void render(GraphicsHolder graphicsHolder, float x, float y, float z, float rotateY, int light) {
			modelPart.setPos(x, y, z);
			modelPart.yRot = rotateY;
			modelPart.render(graphicsHolder.matrixStack, graphicsHolder.vertexConsumer, light, OverlayTexture.NO_OVERLAY);
		}
	}
}
