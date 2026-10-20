package org.mtr.render;

import java.awt.Color;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.mtr.MTR;
import org.mtr.block.BlockLiftTrackFloor.LiftTrackFloorBlockEntity;
import org.mtr.client.CustomResourceLoader;
import org.mtr.client.IDrawing;
import org.mtr.client.LiftDoorControl;
import org.mtr.client.MinecraftClientData;
import org.mtr.client.VehicleRidingMovement;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.core.data.LiftFloor;
import org.mtr.core.data.Position;
import org.mtr.core.tool.Vector;
import org.mtr.data.IGui;
import org.mtr.font.FontRenderHelper;
import org.mtr.font.FontRenderOptions;
import org.mtr.font.FontRenderOptions.Alignment;
import org.mtr.font.FontRenderOptions.TextOverflow;
import org.mtr.item.ItemLiftRefresher;
import org.mtr.libraries.com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import org.mtr.libraries.com.logisticscraft.occlusionculling.util.Vec3d;
import org.mtr.libraries.gg.essential.universal.UMinecraft;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.registry.Items;
import org.mtr.resource.LiftResource;
import org.mtr.screen.LiftSelectionScreen;
import org.mtr.tool.Drawing;

public class RenderLifts implements IGui {
   private static final ResourceLocation LIFT_TEXTURE = ResourceLocation.fromNamespaceAndPath("mtr", "textures/vehicle/lift_1.png");
   private static final int LIFT_DISPLAY_COLOR = -65536;
   private static final float LIFT_DOOR_VALUE = 0.75F;
   private static final float LIFT_FLOOR_PADDING = 0.25F;

   public static void render(long millisElapsed, Vec3 cameraShakeOffset) {
      Minecraft minecraftClient = Minecraft.getInstance();
      ClientLevel clientWorld = minecraftClient.level;
      LocalPlayer clientPlayerEntity = minecraftClient.player;
      if (clientWorld != null && clientPlayerEntity != null) {
         ObjectArrayList<Function<OcclusionCullingInstance, Runnable>> cullingTasks = new ObjectArrayList();
         Vec3 cameraPosition = minecraftClient.gameRenderer.getMainCamera().getPosition();
         Vec3d camera = new Vec3d(cameraPosition.x, cameraPosition.y, cameraPosition.z);
         boolean canRide = !clientPlayerEntity.isSpectator();
         boolean isHoldingRefresher = clientPlayerEntity.isHolding((Item)Items.LIFT_REFRESHER.get());
         MinecraftClientData.getInstance()
            .liftWrapperList
            .values()
            .forEach(
               liftWrapper -> {
                  Lift lift = liftWrapper.getLift();
                  if (isHoldingRefresher) {
                     LiftFloor[] previousLiftFloor = new LiftFloor[]{null};
                     lift.iterateFloors(
                        liftFloor -> {
                           Position position = liftFloor.getPosition();
                           StoredMatrixTransformations storedMatrixTransformationsx = new StoredMatrixTransformations(
                              position.getX(), position.getY(), position.getZ()
                           );
                           MainRenderer.scheduleRender(
                              ResourceLocation.parse("textures/block/redstone_block.png"),
                              false,
                              QueuedRenderLayer.LIGHT,
                              (matrixStack, vertexConsumer, offset) -> {
                                 storedMatrixTransformationsx.transform(matrixStack, offset);
                                 RenderPSDAPGDoor.MODEL_SMALL_CUBE.render(matrixStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY);
                                 matrixStack.popPose();
                              }
                           );
                           if (previousLiftFloor[0] != null) {
                              Position position1 = liftFloor.getPosition();
                              Position position2 = previousLiftFloor[0].getPosition();
                              MainRenderer.scheduleRender(
                                 QueuedRenderLayer.LINES,
                                 (matrixStack, vertexConsumer, offset) -> {
                                    ObjectArrayList<Vector> trackPositions = ItemLiftRefresher.findPath(clientWorld, position1, position2);

                                    for (int ix = 1; ix < trackPositions.size(); ix++) {
                                       IDrawing.drawLineInWorld(
                                          matrixStack,
                                          vertexConsumer,
                                          (float)(((Vector)trackPositions.get(ix - 1)).x() - offset.x + 0.5),
                                          (float)(((Vector)trackPositions.get(ix - 1)).y() - offset.y + 0.5),
                                          (float)(((Vector)trackPositions.get(ix - 1)).z() - offset.z + 0.5),
                                          (float)(((Vector)trackPositions.get(ix)).x() - offset.x + 0.5),
                                          (float)(((Vector)trackPositions.get(ix)).y() - offset.y + 0.5),
                                          (float)(((Vector)trackPositions.get(ix)).z() - offset.z + 0.5),
                                          -1
                                       );
                                    }
                                 }
                              );
                           }

                           previousLiftFloor[0] = liftFloor;
                        }
                     );
                  }

                  PositionAndRotation absolutePositionAndRotation = getLiftPositionAndRotation(clientWorld, lift);
                  cullingTasks.add(
                     (Function<OcclusionCullingInstance, Runnable>)occlusionCullingInstance -> {
                        double longestDimension = Math.max(lift.getHeight(), Math.max(lift.getWidth(), lift.getDepth()));
                        boolean shouldRender = occlusionCullingInstance.isAABBVisible(
                           new Vec3d(
                              absolutePositionAndRotation.position.x() - longestDimension,
                              absolutePositionAndRotation.position.y() - longestDimension,
                              absolutePositionAndRotation.position.z() - longestDimension
                           ),
                           new Vec3d(
                              absolutePositionAndRotation.position.x() + longestDimension,
                              absolutePositionAndRotation.position.y() + longestDimension,
                              absolutePositionAndRotation.position.z() + longestDimension
                           ),
                           camera
                        );
                        return () -> liftWrapper.shouldRender = shouldRender;
                     }
                  );
                  if (liftWrapper.shouldRender) {
                     IntObjectImmutablePair<ObjectObjectImmutablePair<Vec3, Double>> ridingVehicleCarNumberAndOffset = VehicleRidingMovement.getRidingVehicleCarNumberAndOffset(
                        lift.getId()
                     );
                     PositionAndRotation ridingCarPositionAndRotation;
                     Vec3 offsetVector;
                     Double offsetRotation;
                     if (ridingVehicleCarNumberAndOffset == null) {
                        ridingCarPositionAndRotation = null;
                        offsetVector = null;
                        offsetRotation = null;
                     } else {
                        ridingCarPositionAndRotation = absolutePositionAndRotation;
                        offsetVector = (Vec3)((ObjectObjectImmutablePair)ridingVehicleCarNumberAndOffset.right()).left();
                        offsetRotation = (Double)((ObjectObjectImmutablePair)ridingVehicleCarNumberAndOffset.right()).right();
                     }

                     PositionAndRotation renderingPositionAndRotation = RenderVehicles.getRenderPositionAndRotation(
                        offsetVector, offsetRotation, ridingCarPositionAndRotation, absolutePositionAndRotation, cameraShakeOffset
                     );
                     ObjectArrayList<ObjectBooleanImmutablePair<AABB>> floorsAndDoorways = new ObjectArrayList();
                     final float doorValue = lift.getDoorValue();
                     // MEE patch: arrival "ding" chime the moment the door starts opening.
                     if (LiftDoorControl.isDoorJustOpened(lift.getId(), doorValue)) {
                        clientWorld.playLocalSound(absolutePositionAndRotation.position.x(), absolutePositionAndRotation.position.y(), absolutePositionAndRotation.position.z(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.6F, 1.0F, false);
                     }
                     final double doorRange = Math.max(lift.getWidth(), lift.getDepth()) + 1.5;
                     // MEE patch: the door is a single object. When a real lift-door block sits at the car's
                     // front (or back) face, the block door is authoritative and the car's own panel is
                     // skipped — otherwise the two coplanar translucent surfaces z-fight (穿模).
                     // Detect doors directionally at the car's door faces, not as a ball around the car.
                     final double liftYaw = absolutePositionAndRotation.yaw;
                     final double liftX = absolutePositionAndRotation.position.x();
                     final double liftY = absolutePositionAndRotation.position.y();
                     final double liftZ = absolutePositionAndRotation.position.z();
                     final boolean frontHasDoor = LiftDoorControl.hasNearbyLiftDoorOnSide(clientWorld, liftX, liftY, liftZ, liftYaw, doorRange, true);
                     final boolean backHasDoor = lift.getIsDoubleSided() && LiftDoorControl.hasNearbyLiftDoorOnSide(clientWorld, liftX, liftY, liftZ, liftYaw, doorRange, false);
                     // MEE: for a double-sided car, only the side(s) with a real lift-door block open.
                     final float doorValueFront = lift.getIsDoubleSided() ? (frontHasDoor ? doorValue : 0.0F) : doorValue;
                     final float doorValueBack = backHasDoor ? doorValue : 0.0F;
                     // MEE: drive each door block with the value of its own side (single-sided cars keep the back closed).
                     LiftDoorControl.openNearbyDoors(clientWorld, liftX, liftY, liftZ, liftYaw, doorRange, doorValueFront, doorValueBack);

                     if (canRide) {
                        Vec3 playerPosition = (Vec3)absolutePositionAndRotation.transformBackwards(
                           clientPlayerEntity.position(), Vec3::xRot, Vec3::yRot, Vec3::zRot, Vec3::add
                        );
                        // MEE patch: the car interior is hard-clamped on all four sides (air walls);
                        // the front doorway strip is only passable while the doors are open. The four
                        // boxes tile the whole car so there is no gap the player could fall through.
                        final double halfW = lift.getWidth() / 2.0;
                        final double halfD = lift.getDepth() / 2.0;
                        final double doorDepth = 0.6;
                        // interior excludes both the front and back doorway strips; each side is passable only while its door is open
                        final AABB interior = new AABB(-halfW, 0.0, -halfD + doorDepth, halfW, 0.0, halfD - doorDepth);
                        final AABB frontDoorway = new AABB(-0.75, 0.0, halfD - doorDepth, 0.75, 0.0, halfD + 0.05);
                        final AABB frontCornerL = new AABB(-halfW, 0.0, halfD - doorDepth, -0.75, 0.0, halfD + 0.05);
                        final AABB frontCornerR = new AABB(0.75, 0.0, halfD - doorDepth, halfW, 0.0, halfD + 0.05);
                        final AABB backDoorway = new AABB(-0.75, 0.0, -halfD - 0.05, 0.75, 0.0, -halfD + doorDepth);
                        final AABB backCornerL = new AABB(-halfW, 0.0, -halfD - 0.05, -0.75, 0.0, -halfD + doorDepth);
                        final AABB backCornerR = new AABB(0.75, 0.0, -halfD - 0.05, halfW, 0.0, -halfD + doorDepth);
                        final boolean frontOpen = doorValueFront > 0.01F;
                        final boolean backOpen = doorValueBack > 0.01F;

                        final boolean wasRiding = VehicleRidingMovement.isRiding(lift.getId());
                        final ObjectArrayList<AABB> rideBoxes = ObjectArrayList.of(new AABB[]{interior, frontDoorway, frontCornerL, frontCornerR});
                        if (lift.getIsDoubleSided()) {
                           rideBoxes.add(backDoorway);
                           rideBoxes.add(backCornerL);
                           rideBoxes.add(backCornerR);
                        }
                        VehicleRidingMovement.startRiding(
                           rideBoxes, 0L, 0L, lift.getId(), 0, playerPosition.x, playerPosition.y, playerPosition.z, absolutePositionAndRotation.yaw
                        );
                        if (!wasRiding && VehicleRidingMovement.isRiding(lift.getId())) {
                           clientPlayerEntity.displayClientMessage(Component.literal("按 Z 打开面板 | 开门后走出轿厢脱离"), true);
                        }

                        // true = hard clamp (wall), false = passable (doorway)
                        floorsAndDoorways.add(new ObjectBooleanImmutablePair(interior, true));
                        floorsAndDoorways.add(new ObjectBooleanImmutablePair(frontDoorway, !frontOpen));
                        floorsAndDoorways.add(new ObjectBooleanImmutablePair(frontCornerL, true));
                        floorsAndDoorways.add(new ObjectBooleanImmutablePair(frontCornerR, true));
                        if (lift.getIsDoubleSided()) {
                           floorsAndDoorways.add(new ObjectBooleanImmutablePair(backDoorway, !backOpen));
                           floorsAndDoorways.add(new ObjectBooleanImmutablePair(backCornerL, true));
                           floorsAndDoorways.add(new ObjectBooleanImmutablePair(backCornerR, true));
                        }
                        RenderVehicleHelper.renderFloorOrDoorway(interior, -1, playerPosition, renderingPositionAndRotation, offsetVector == null);
                     }

                     StoredMatrixTransformations storedMatrixTransformations = RenderVehicles.getStoredMatrixTransformations(
                        offsetVector == null, renderingPositionAndRotation, 0.0
                     );
                     // MEE: the 4.0.5 car faces -Z; this beta puts the doorway/door block at +Z, so rotate
                     // the shared transform 180deg (the car body and the floor display both use it).
                     storedMatrixTransformations.add(matrixStack -> matrixStack.mulPose(com.mojang.math.Axis.YP.rotation((float) Math.PI)));

                      // MEE patch: render the lift car body (official beta has this commented out)
                      ModelLift1.renderLiftCar(storedMatrixTransformations, lift, absolutePositionAndRotation.light, doorValueFront, doorValueBack);

                     for (int i = 0; i < (lift.getIsDoubleSided() ? 2 : 1); i++) {
                        boolean shouldRotate = i == 0;
                        StoredMatrixTransformations storedMatrixTransformationsNew = storedMatrixTransformations.copy();
                        storedMatrixTransformationsNew.add(matrixStack -> {
                           if (shouldRotate) {
                              Drawing.rotateYDegrees(matrixStack, 180.0F);
                           }

                           matrixStack.translate(0.875, -1.5, lift.getDepth() / 2.0 - 0.25 - 0.003125F);
                        });
                        renderLiftDisplay(storedMatrixTransformationsNew, clientWorld, lift, 0.1875F, 0.3125F);
                     }

                     if (canRide) {
                        VehicleRidingMovement.movePlayer(millisElapsed, lift.getId(), 0, floorsAndDoorways, null, null, null, absolutePositionAndRotation);
                     }
                  }
               }
            );
         MainRenderer.WORKER_THREAD
            .scheduleLifts(
               occlusionCullingInstance -> {
                  ObjectArrayList<Runnable> tasks = new ObjectArrayList();
                  cullingTasks.forEach(
                     occlusionCullingInstanceRunnableFunction -> tasks.add((Runnable)occlusionCullingInstanceRunnableFunction.apply(occlusionCullingInstance))
                  );
                  minecraftClient.execute(() -> tasks.forEach(Runnable::run));
               }
            );
      }
   }

   public static void renderLiftDisplay(StoredMatrixTransformations storedMatrixTransformations, ClientLevel world, Lift lift, float width, float height) {
      ObjectObjectImmutablePair<LiftDirection, ObjectObjectImmutablePair<String, String>> liftDetails = getLiftDetails(
         world, lift, MTR.positionToBlockPos(lift.getCurrentFloor().getPosition())
      );
      LiftDirection liftDirection = (LiftDirection)liftDetails.left();
      MainRenderer.scheduleTextRender(
         (matrixStack, offset) -> {
            storedMatrixTransformations.transform(matrixStack, offset);
            FontRenderHelper.render(
               matrixStack,
               (String)((ObjectObjectImmutablePair)liftDetails.right()).left(),
               FontRenderOptions.builder()
                  .horizontalTextAlignment(Alignment.CENTER)
                  .verticalTextAlignment(Alignment.END)
                  .horizontalPositioning(Alignment.CENTER)
                  .verticalPositioning(Alignment.END)
                  .offsetY(height)
                  .horizontalSpace(width * 0.8F)
                  .verticalSpace(width * 0.6F)
                  .color(new Color(-65536))
                  .textOverflow(TextOverflow.COMPRESS)
                  .build()
            );
            matrixStack.popPose();
         }
      );
      if (liftDirection != LiftDirection.NONE) {
         MainRenderer.scheduleRender(
            ResourceLocation.fromNamespaceAndPath("mtr", "textures/block/sign/lift_arrow.png"),
            false,
            QueuedRenderLayer.LIGHT_TRANSLUCENT,
            (matrixStack, vertexConsumer, offset) -> {
               storedMatrixTransformations.transform(matrixStack, offset);
               IDrawing.drawTexture(
                  matrixStack,
                  vertexConsumer,
                  -width / 6.0F,
                  0.0F,
                  width / 3.0F,
                  width / 3.0F,
                  0.0F,
                  liftDirection == LiftDirection.UP ? 0.0F : 1.0F,
                  1.0F,
                  liftDirection == LiftDirection.UP ? 1.0F : 0.0F,
                  Direction.UP,
                  -65536,
                  15728880
               );
               matrixStack.popPose();
            }
         );
      }
   }

   public static ObjectObjectImmutablePair<LiftDirection, ObjectObjectImmutablePair<String, String>> getLiftDetails(
      ClientLevel world, Lift lift, BlockPos blockPos
   ) {
      LiftFloor liftFloor = lift.getCurrentFloor();
      String floorNumber;
      String floorDescription;
      if (world.getBlockEntity(blockPos) instanceof LiftTrackFloorBlockEntity liftTrackFloorBlockEntity) {
         floorNumber = liftTrackFloorBlockEntity.getFloorNumber();
         floorDescription = liftTrackFloorBlockEntity.getFloorDescription();
      } else {
         floorNumber = liftFloor.getNumber();
         floorDescription = liftFloor.getDescription();
      }

      return new ObjectObjectImmutablePair(lift.getDirection(), new ObjectObjectImmutablePair(floorNumber, floorDescription));
   }

   public static LiftResource getLiftResource(String liftId) {
      LiftResource liftResource;
      if (liftId == null) {
         liftResource = null;
      } else {
         LiftResource[] tempLiftResource = new LiftResource[]{null};
         CustomResourceLoader.getLiftById(liftId, newLiftResource -> tempLiftResource[0] = newLiftResource);
         liftResource = tempLiftResource[0];
      }

      return liftResource == null ? (LiftResource)CustomResourceLoader.getLifts().getFirst() : liftResource;
   }

   private static PositionAndRotation getLiftPositionAndRotation(ClientLevel clientWorld, Lift lift) {
      Vector position = lift.getPosition((floorPosition1, floorPosition2) -> ItemLiftRefresher.findPath(clientWorld, floorPosition1, floorPosition2));
      return new PositionAndRotation(
         new Vector(position.x() + lift.getOffsetX(), position.y() + lift.getOffsetY(), position.z() + lift.getOffsetZ()),
         (-Math.PI / 2) - lift.getAngle().angleRadians,
         0.0,
         0.0
      );
   }
}
