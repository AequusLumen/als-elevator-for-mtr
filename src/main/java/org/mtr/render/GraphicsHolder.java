package org.mtr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

/** Lean port of MTR's GraphicsHolder, only the transform primitives the model renderer needs. */
public final class GraphicsHolder {

	public final PoseStack matrixStack;
	public final VertexConsumer vertexConsumer;

	public GraphicsHolder(PoseStack matrixStack, VertexConsumer vertexConsumer) {
		this.matrixStack = matrixStack;
		this.vertexConsumer = vertexConsumer;
	}

	public static int getDefaultLight() {
		return 15728880;
	}

	public void push() {
		matrixStack.pushPose();
	}

	public void pop() {
		matrixStack.popPose();
	}

	public void translate(double x, double y, double z) {
		matrixStack.translate(x, y, z);
	}

	public void rotateXRadians(float angle) {
		matrixStack.mulPose(Axis.XP.rotation(angle));
	}

	public void rotateYRadians(float angle) {
		matrixStack.mulPose(Axis.YP.rotation(angle));
	}

	public void rotateZRadians(float angle) {
		matrixStack.mulPose(Axis.ZP.rotation(angle));
	}

	public void rotateXDegrees(float angle) {
		matrixStack.mulPose(Axis.XP.rotationDegrees(angle));
	}

	public void rotateYDegrees(float angle) {
		matrixStack.mulPose(Axis.YP.rotationDegrees(angle));
	}

	public void rotateZDegrees(float angle) {
		matrixStack.mulPose(Axis.ZP.rotationDegrees(angle));
	}
}
