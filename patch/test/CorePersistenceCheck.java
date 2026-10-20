import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.mtr.client.MinecraftClientData;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftFloor;
import org.mtr.core.data.Position;
import org.mtr.core.operation.UpdateDataRequest;
import org.mtr.core.servlet.OperationProcessor;
import org.mtr.core.simulation.Simulator;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Headless test v2: replicate the real flow
 * 1) Lift Refresher -> "generate_by_lift" through the operation processor
 * 2) customization screen close -> UpdateDataRequest.update()
 * 3) save (stop) / reload, checking lift persistence each time
 */
public class CorePersistenceCheck {

	public static void main(String[] args) throws Exception {
		final Path dir = Paths.get(args[0]);
		if (Files.exists(dir)) {
			try (Stream<Path> walk = Files.walk(dir)) {
				walk.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(p -> {
					try {
						Files.delete(p);
					} catch (Exception ignored) {
					}
				});
			}
		}
		Files.createDirectories(dir);

		System.out.println("=== stage 1: generate_by_lift (refresher) ===");
		Simulator s1 = new Simulator("minecraft/overworld", new String[]{"minecraft/overworld"}, dir, false);
		Lift lift = makeLift();
		OperationProcessor.process(OperationProcessor.GENERATE_BY_LIFT, lift, s1);
		System.out.println("lifts in memory: " + s1.lifts.size());
		s1.lifts.forEach(l -> System.out.println("  lift id=" + l.getId() + " floors=" + l.getFloorCount()));

		System.out.println("=== stage 2: save (stop) ===");
		s1.stop();
		try (Stream<Path> walk = Files.walk(dir)) {
			walk.filter(Files::isRegularFile).forEach(p -> System.out.println("  file " + dir.relativize(p)));
		}

		System.out.println("=== stage 3: reload ===");
		Simulator s2 = new Simulator("minecraft/overworld", new String[]{"minecraft/overworld"}, dir, false);
		System.out.println("lifts after reload: " + s2.lifts.size());
		s2.lifts.forEach(l -> System.out.println("  lift id=" + l.getId() + " floors=" + l.getFloorCount() + " style=" + l.getStyle()));

		System.out.println("=== stage 4: customization save (UpdateDataRequest) ===");
		Lift clientLift = makeLift();
		new UpdateDataRequest(s2).addLift(clientLift).update();
		System.out.println("lifts after update: " + s2.lifts.size());
		s2.lifts.forEach(l -> System.out.println("  lift id=" + l.getId() + " floors=" + l.getFloorCount()));
		s2.stop();

		System.out.println("=== stage 5: reload again ===");
		Simulator s3 = new Simulator("minecraft/overworld", new String[]{"minecraft/overworld"}, dir, false);
		System.out.println("lifts after second reload: " + s3.lifts.size());
		s3.lifts.forEach(l -> System.out.println("  lift id=" + l.getId() + " floors=" + l.getFloorCount()));
		System.out.println("=== ALL DONE ===");
	}

	private static Lift makeLift() {
		final Lift lift = new Lift(new MinecraftClientData());
		final ObjectArrayList<LiftFloor> floors = new ObjectArrayList<>();
		floors.add(new LiftFloor(new Position(10, 65, 10), "F1", ""));
		floors.add(new LiftFloor(new Position(10, 72, 10), "F2", ""));
		lift.setFloors(floors);
		lift.setDimensions(3, 2, 2, 0, 0, 0);
		lift.setStyle("default_transparent");
		return lift;
	}
}
