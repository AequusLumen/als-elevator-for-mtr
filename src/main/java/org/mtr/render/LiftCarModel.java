package org.mtr.render;

import net.minecraft.resources.ResourceLocation;
import org.mtr.MTR;
import org.mtr.core.data.Lift;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.model.NewOptimizedModel;
import org.mtr.model.ObjModelLoader;
import org.mtr.resource.RenderStage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * MEE patch: loads the lift car 3D model (assets/mtr/models/vehicle/lift_car.obj) through the
 * mod's own OBJ pipeline and renders it with MainRenderer.renderModel. The OBJ uses the original
 * ModelLift1 UV regions from lift_1.png, so the car looks like the genuine MTR lift.
 */
public final class LiftCarModel {

	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mtr", "textures/vehicle/lift_car_model.png");
	private static final String MODEL_PATH = "/assets/mtr/models/vehicle/lift_car.obj";

	private static ObjModelLoader loader;
	private static Object2ObjectOpenHashMap<RenderStage, ObjectArrayList<NewOptimizedModel>> models;
	private static boolean loadAttempted;

	private LiftCarModel() {
	}

	private static void tryLoad() {
		if (loadAttempted) {
			return;
		}
		loadAttempted = true;
		try (InputStream in = LiftCarModel.class.getResourceAsStream(MODEL_PATH)) {
			if (in == null) {
				MTR.LOGGER.error("[MEE] lift car model not found: {}", MODEL_PATH);
				return;
			}
			final String objString = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			loader = new ObjModelLoader(TEXTURE);
			loader.loadModel(objString, name -> "", name -> TEXTURE, false, false);
		} catch (Exception e) {
			MTR.LOGGER.error("[MEE] failed to load the lift car model", e);
		}
	}

	public static void render(StoredMatrixTransformations storedMatrixTransformations, Lift lift, int light) {
		if (!loadAttempted) {
			tryLoad();
		}
		if (models == null && loader != null) {
			models = loader.get();
		}
		if (models == null || models.isEmpty()) {
			return;
		}
		// the OBJ is authored for a 2 x 2 x 3 car; scale it for other dimensions
		final StoredMatrixTransformations scaled = storedMatrixTransformations.copy();
		scaled.add(matrixStack -> matrixStack.scale((float) lift.getWidth() / 2.0F, (float) lift.getHeight() / 3.0F, (float) lift.getDepth() / 2.0F));
		MainRenderer.renderModel(models, scaled, light);
	}
}
