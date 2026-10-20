package org.mtr.core.operation;

import org.mtr.core.data.Data;
import org.mtr.core.data.Lift;
import org.mtr.core.serializer.JsonReader;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class GenerateByLift {
   private final Data data;
   private final Lift lift;

   public GenerateByLift(JsonReader jsonReader, Data data) {
      this.data = data;
      this.lift = new Lift(jsonReader, data);
   }

   public void generate() {
      System.out.println("[MEE-DEBUG] core generate_by_lift id=" + this.lift.getId() + " floors=" + this.lift.getFloorCount() + " before=" + this.data.lifts.size());
      ObjectArrayList<Lift> liftsToModify = UpdateDataRequest.getAndRemoveMatchingLifts(this.data, this.lift);
      liftsToModify.add(this.lift);
      ((Lift)liftsToModify.getFirst()).setFloors(this.lift);
      this.data.lifts.add((Lift)liftsToModify.getFirst());
      this.data.sync();
      System.out.println("[MEE-DEBUG] core generate_by_lift after=" + this.data.lifts.size());
   }
}
