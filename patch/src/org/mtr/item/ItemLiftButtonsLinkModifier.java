package org.mtr.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mtr.block.BlockLiftButtons;
import org.mtr.block.BlockLiftPanelBase;
import org.mtr.block.BlockLiftTrackFloor;
import org.mtr.block.BlockLiftPanelBase.BlockEntityBase;
import org.mtr.block.BlockLiftTrackFloor.LiftTrackFloorBlockEntity;

public class ItemLiftButtonsLinkModifier extends ItemBlockClickingBase {
   private final boolean isConnector;

   public ItemLiftButtonsLinkModifier(boolean isConnector, Properties settings) {
      super(settings);
      this.isConnector = isConnector;
   }

   protected void onStartClick(UseOnContext context) {
   }

   protected void onEndClick(UseOnContext context, BlockPos posEnd) {
      Level world = context.getLevel();
      BlockPos posStart = context.getClickedPos();
      connect(world, posStart, posEnd, this.isConnector);
      connect(world, posEnd, posStart, this.isConnector);
   }

   protected boolean clickCondition(UseOnContext context) {
      Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
      return block instanceof BlockLiftTrackFloor || block instanceof BlockLiftButtons || block instanceof BlockLiftPanelBase;
   }

   private static void connect(Level world, BlockPos blockPos1, BlockPos blockPos2, boolean isAdd) {
      BlockEntity blockEntity1 = world.getBlockEntity(blockPos1);
      BlockEntity blockEntity2 = world.getBlockEntity(blockPos2);
      if (blockEntity1 != null && blockEntity2 instanceof LiftTrackFloorBlockEntity) {
         if (blockEntity1 instanceof BlockLiftButtons.LiftButtonsBlockEntity) {
            ((BlockLiftButtons.LiftButtonsBlockEntity)blockEntity1).registerFloor(blockPos2, isAdd);
         }

         if (blockEntity1 instanceof BlockEntityBase) {
            ((BlockEntityBase)blockEntity1).registerFloor(world, blockPos2, isAdd);
         }

         // MEE patch: push the block entity update to the client so the buttons/panel refresh immediately
         world.sendBlockUpdated(blockPos1, world.getBlockState(blockPos1), world.getBlockState(blockPos1), 3);
      }
   }
}
