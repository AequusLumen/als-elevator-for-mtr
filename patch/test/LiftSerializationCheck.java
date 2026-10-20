import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.mtr.client.MinecraftClientData;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftFloor;
import org.mtr.core.data.Position;
import org.mtr.core.operation.UpdateDataRequest;
import org.mtr.core.serializer.JsonReader;
import org.mtr.core.tool.Utilities;
import org.mtr.libraries.com.google.gson.JsonObject;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Round-trip test: does a Lift survive JSON serialization (the update_data packet path)?
 */
public class LiftSerializationCheck {

	public static void main(String[] args) throws Exception {
		final Lift lift = new Lift(new MinecraftClientData());
		final ObjectArrayList<LiftFloor> floors = new ObjectArrayList<>();
		floors.add(new LiftFloor(new Position(10, 65, 10), "F1", ""));
		floors.add(new LiftFloor(new Position(10, 72, 10), "F2", ""));
		lift.setFloors(floors);
		lift.setDimensions(3, 2, 2, 0, 0, 0);
		lift.setStyle("default_transparent");
		System.out.println("original: id=" + lift.getId() + " floors=" + lift.getFloorCount());

		final JsonObject json = Utilities.getJsonObjectFromData(lift);
		System.out.println("json: " + json.toString().substring(0, Math.min(400, json.toString().length())));
		final Lift parsed = new Lift(new JsonReader(json), new MinecraftClientData());
		System.out.println("parsed: id=" + parsed.getId() + " floors=" + parsed.getFloorCount() + " style=" + parsed.getStyle());

		final UpdateDataRequest request = new UpdateDataRequest(new MinecraftClientData()).addLift(parsed);
		final JsonObject requestJson = Utilities.getJsonObjectFromData(request);
		System.out.println("request json: " + requestJson.toString().substring(0, Math.min(300, requestJson.toString().length())));
		final UpdateDataRequest parsedRequest = new UpdateDataRequest(new JsonReader(requestJson), new MinecraftClientData());
		System.out.println("request re-parsed ok");
		System.out.println("=== ALL DONE ===");
	}
}
