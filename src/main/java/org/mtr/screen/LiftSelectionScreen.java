package org.mtr.screen;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.mtr.MTR;
import org.mtr.client.MinecraftClientData;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.core.operation.PressLift;
import org.mtr.data.IGui;
import org.mtr.generated.lang.TranslationProvider;
import org.mtr.libraries.gg.essential.elementa.components.UIWrappedText;
import org.mtr.libraries.gg.essential.elementa.constraints.FillConstraint;
import org.mtr.libraries.gg.essential.elementa.constraints.PixelConstraint;
import org.mtr.libraries.gg.essential.elementa.constraints.RelativeConstraint;
import org.mtr.libraries.gg.essential.elementa.constraints.SiblingConstraint;
import org.mtr.libraries.gg.essential.elementa.constraints.SubtractiveConstraint;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.packet.PacketPressLiftButton;
import org.mtr.registry.RegistryClient;
import org.mtr.render.RenderLifts;
import org.mtr.tool.GuiHelper;
import org.mtr.widget.BackgroundComponent;
import org.mtr.widget.ListComponent;
import org.mtr.widget.ListItem;
import org.mtr.widget.SlotBackgroundComponent;
import org.mtr.widget.ListItem.ActionConsumer;

public final class LiftSelectionScreen extends WindowBase {
   private final ObjectArrayList<BlockPos> floorLevels = new ObjectArrayList();
   private final ObjectArrayList<String> floorDescriptions = new ObjectArrayList();
   private final long liftId;
   private final ListComponent<DashboardListItem> listComponent;

   public LiftSelectionScreen(long liftId) {
      this.liftId = liftId;
      Lift lift = MinecraftClientData.getLift(liftId);
      ClientLevel clientWorld = Minecraft.getInstance().level;
      if (lift != null && clientWorld != null) {
         lift.iterateFloors(
            floor -> {
               BlockPos blockPos = MTR.positionToBlockPos(floor.getPosition());
               this.floorLevels.add(blockPos);
               ObjectObjectImmutablePair<LiftDirection, ObjectObjectImmutablePair<String, String>> liftDetails = RenderLifts.getLiftDetails(
                  clientWorld, lift, blockPos
               );
               this.floorDescriptions
                  .add(
                     String.format(
                        "%s %s",
                        ((ObjectObjectImmutablePair)liftDetails.right()).left(),
                        IGui.formatStationName(String.join("|", (CharSequence)((ObjectObjectImmutablePair)liftDetails.right()).right()))
                     )
                  );
            }
         );
      }

      BackgroundComponent backgroundComponent = new BackgroundComponent(this.getWindow(), ObjectImmutableList.of());
      new UIWrappedText(TranslationProvider.GUI_MTR_SELECT_FLOOR.getString(new Object[0]), false)
         .setChildOf(backgroundComponent)
         .setWidth(new RelativeConstraint())
         .setColor(new Color(-12566464));
      SlotBackgroundComponent slotBackgroundComponent = (SlotBackgroundComponent)new SlotBackgroundComponent()
         .setChildOf(backgroundComponent)
         .setY(new SiblingConstraint(4.0F))
         .setWidth(new RelativeConstraint())
         .setHeight(new SubtractiveConstraint(new FillConstraint(), new PixelConstraint(4.0F)));
      this.listComponent = GuiHelper.createListComponent(slotBackgroundComponent);

   }

   public void onTick() {
      super.onTick();
      Lift lift = MinecraftClientData.getLift(this.liftId);
      if (lift == null) {
         Minecraft.getInstance().setScreen(null);
      } else {
         ObjectArrayList<ListItem<DashboardListItem>> listItems = new ObjectArrayList();

         for (int i = this.floorLevels.size() - 1; i >= 0; i--) {
            BlockPos blockPos = (BlockPos)this.floorLevels.get(i);
            Color color = lift.hasInstruction(lift.getFloorIndex(MTR.blockPosToPosition(blockPos))).contains(LiftDirection.NONE) ? Color.RED : Color.WHITE;
            int floorIndexFromTop = i;
            DashboardListItem dashboardListItem = new DashboardListItem(blockPos.asLong(), (String)this.floorDescriptions.get(i), color.getRGB());
            listItems.add(
               ListItem.createChild(
                  (drawing, x, y) -> drawing.setVerticesWH(x + 4.0F, y + 4.0F, 8.0, 8.0).setColor(color).draw(),
                  null,
                  12,
                  dashboardListItem,
                  dashboardListItem.getName(true),
                  ObjectArrayList.of(
                     new ObjectObjectImmutablePair[]{
                        new ObjectObjectImmutablePair(GuiHelper.ADD_TEXTURE_ID, (ActionConsumer)(indexList, data) -> this.onPress(floorIndexFromTop))
                     }
                  )
               )
            );
         }

         this.listComponent.setData(listItems);
      }
   }

   private void onPress(int floorIndexFromTop) {
      PressLift pressLift = new PressLift();
      pressLift.add(MTR.blockPosToPosition((BlockPos)this.floorLevels.get(floorIndexFromTop)), LiftDirection.NONE);
      RegistryClient.sendPacketToServer(new PacketPressLiftButton(pressLift));
      Minecraft.getInstance().setScreen(null);
   }
}
