package org.mtr.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.mtr.MTR;
import org.mtr.block.BlockLiftTrackBase;
import org.mtr.block.BlockLiftTrackFloor;
import org.mtr.client.MinecraftClientData;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftFloor;
import org.mtr.core.data.Position;
import org.mtr.core.tool.Utilities;
import org.mtr.core.tool.Vector;
import org.mtr.generated.lang.TranslationProvider;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.mtr.packet.PacketOpenLiftCustomizationScreen;
import org.mtr.registry.RegistryServer;

public class ItemLiftRefresher extends Item {
   public ItemLiftRefresher(Properties settings) {
      super(settings.stacksTo(1));
   }

   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      Player playerEntity = context.getPlayer();
      if (!world.isClientSide() && playerEntity != null) {
         BlockPos blockPos = context.getClickedPos();
         ObjectOpenHashSet<BlockPos> blacklistedBlockPos = new ObjectOpenHashSet();
         ObjectArrayList<LiftFloor> liftFloors1 = new ObjectArrayList();
         if (!findPath(world, blockPos, null, liftFloors1, new ObjectArrayList(), blacklistedBlockPos, true, false)) {
            playerEntity.displayClientMessage(TranslationProvider.GUI_MTR_LIFT_TRACK_REQUIRED.getText(new Object[0]), true);
            return InteractionResult.FAIL;
         }

         blacklistedBlockPos.remove(blockPos);
         ObjectArrayList<LiftFloor> liftFloors2 = new ObjectArrayList();
         findPath(world, blockPos, null, liftFloors2, new ObjectArrayList(), blacklistedBlockPos, false, false);
         if (liftFloors1.isEmpty() && liftFloors2.isEmpty()) {
            return InteractionResult.FAIL;
         }

         ObjectArrayList<LiftFloor> liftFloors = new ObjectArrayList();
         liftFloors.addAll(reverseList(liftFloors1));
         liftFloors.addAll(liftFloors2);
         boolean needsReverse = ((LiftFloor)Utilities.getElement(liftFloors, -1)).getPosition().getY()
            < ((LiftFloor)Utilities.getElement(liftFloors, 0)).getPosition().getY();
         sendUpdate((ServerLevel)world, needsReverse ? reverseList(liftFloors) : liftFloors);
         RegistryServer.sendPacketToClient(
            (ServerPlayer)playerEntity, new PacketOpenLiftCustomizationScreen(MTR.positionToBlockPos(((LiftFloor)liftFloors.getFirst()).getPosition()))
         );
         return InteractionResult.SUCCESS;
      } else {
         return super.useOn(context);
      }
   }

   public static ObjectArrayList<Vector> findPath(Level world, Position startPosition, Position endPosition) {
      ObjectOpenHashSet<BlockPos> blacklistedBlockPos = new ObjectOpenHashSet();
      BlockPos startBlockPos = MTR.positionToBlockPos(startPosition);

      for (int i = 0; i < 2; i++) {
         ObjectArrayList<LiftFloor> liftFloors = new ObjectArrayList();
         ObjectArrayList<Vector> liftTrackPositions = new ObjectArrayList();
         if (findPath(world, startBlockPos, null, liftFloors, liftTrackPositions, blacklistedBlockPos, false, true)) {
            if (liftFloors.size() == 1 && ((LiftFloor)liftFloors.getFirst()).getPosition().equals(endPosition)) {
               return liftTrackPositions;
            }

            blacklistedBlockPos.remove(startBlockPos);
         }
      }

      return ObjectArrayList.of(
         new Vector[]{
            new Vector(startPosition.getX(), startPosition.getY(), startPosition.getZ()),
            new Vector(endPosition.getX(), endPosition.getY(), endPosition.getZ())
         }
      );
   }

   private static boolean findPath(
      Level world,
      BlockPos blockPos,
      @Nullable Direction direction,
      ObjectArrayList<LiftFloor> liftFloors,
      ObjectArrayList<Vector> liftTrackPositions,
      ObjectOpenHashSet<BlockPos> blacklistedBlockPos,
      boolean addFirstFloor,
      boolean findOneFloorOnly
   ) {
      if (blacklistedBlockPos.contains(blockPos)) {
         return false;
      }

      blacklistedBlockPos.add(blockPos);
      BlockState blockState = world.getBlockState(blockPos);
      Block block = blockState.getBlock();
      if (!(block instanceof BlockLiftTrackBase)) {
         return false;
      }

      ObjectArrayList<Direction> connectingDirections = ((BlockLiftTrackBase)block).getConnectingDirections(blockState);
      if (direction != null && !connectingDirections.contains(direction.getOpposite())) {
         return false;
      }

      liftTrackPositions.add(((BlockLiftTrackBase)block).getCenterPoint(blockPos, blockState));
      BlockEntity blockEntity = world.getBlockEntity(blockPos);
      if (addFirstFloor && blockEntity instanceof BlockLiftTrackFloor.LiftTrackFloorBlockEntity) {
         liftFloors.add(
            new LiftFloor(
               MTR.blockPosToPosition(blockPos),
               ((BlockLiftTrackFloor.LiftTrackFloorBlockEntity)blockEntity).getFloorNumber(),
               ((BlockLiftTrackFloor.LiftTrackFloorBlockEntity)blockEntity).getFloorDescription()
            )
         );
         if (findOneFloorOnly) {
            return true;
         }
      }

      ObjectListIterator var12 = connectingDirections.iterator();

      while (var12.hasNext()) {
         Direction connectingDirection = (Direction)var12.next();
         if (findPath(
            world, blockPos.relative(connectingDirection), connectingDirection, liftFloors, liftTrackPositions, blacklistedBlockPos, true, findOneFloorOnly
         )) {
            break;
         }
      }

      return true;
   }

   private static ObjectArrayList<LiftFloor> reverseList(ObjectArrayList<LiftFloor> liftFloors) {
      ObjectArrayList<LiftFloor> liftFloorsReversed = new ObjectArrayList();
      ObjectListIterator var2 = liftFloors.iterator();

      while (var2.hasNext()) {
         LiftFloor liftFloor = (LiftFloor)var2.next();
         liftFloorsReversed.add(0, liftFloor);
      }

      return liftFloorsReversed;
   }

   private static void sendUpdate(ServerLevel serverWorld, ObjectArrayList<LiftFloor> liftFloors) {
      Lift lift = new Lift(new MinecraftClientData());
      lift.setFloors(liftFloors);
      lift.setDimensions(3.0, 2.0, 2.0, 0.0, 0.0, 0.0);
      lift.setStyle("default_transparent");
      MTR.sendMessageC2S("generate_by_lift", serverWorld.getServer(), serverWorld, lift, null, null);
   }
}
