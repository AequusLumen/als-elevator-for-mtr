import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.mtr.neoforge.MTRNeoForge;
import org.mtr.registry.Blocks;
import org.mtr.registry.BlockEntityTypes;
import org.mtr.registry.DataComponentTypes;
import org.mtr.registry.ItemGroups;
import org.mtr.registry.Items;
import org.mtr.registry.SoundEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class RegistrationCheck {

	public static void main(String[] args) {
		try (final java.io.FileWriter verdict = new java.io.FileWriter("reg_check_out.txt", java.nio.charset.StandardCharsets.UTF_8, true)) {
			verdict.write("=== TEST START user.dir=" + System.getProperty("user.dir") + " ===\n");
			verdict.flush();
			System.setOut(new java.io.PrintStream(new java.io.FileOutputStream("reg_check_stdout.txt"), true, java.nio.charset.StandardCharsets.UTF_8));

			// Set the game version (normally done by the launcher) and boot the vanilla registry system
			net.minecraft.SharedConstants.tryDetectVersion();
			net.minecraft.server.Bootstrap.bootStrap();

			// Replicate MTR.init()'s registration-relevant order (avoiding game-dependent paths)
			Blocks.init();
			Items.init();
			BlockEntityTypes.init();
			ItemGroups.init();
			SoundEvents.init();
			DataComponentTypes.init();

			final List<String> blocks = dump("BLOCKS", MTRNeoForge.BLOCKS);
			final List<String> items = dump("ITEMS", MTRNeoForge.ITEMS);
			final List<String> blockEntityTypes = dump("BLOCK_ENTITY_TYPES", MTRNeoForge.BLOCK_ENTITY_TYPES);
			final List<String> itemGroups = dump("ITEM_GROUPS", MTRNeoForge.ITEM_GROUPS);
			dump("SOUND_EVENTS", MTRNeoForge.SOUND_EVENTS);
			dump("DATA_COMPONENT_TYPES", MTRNeoForge.DATA_COMPONENT_TYPES);

			System.out.println("### ASSERTIONS ###");
			final Set<String> expectedBlocks = new HashSet<>(Arrays.asList(
				"escalator_side", "escalator_step",
				"lift_buttons_1", "lift_panel_even_1", "lift_panel_odd_1", "lift_panel_even_2", "lift_panel_odd_2",
				"lift_track_horizontal_1", "lift_track_1", "lift_track_diagonal_1", "lift_track_floor_1",
				"lift_door_1", "lift_door_odd_1"
			));
			final Set<String> expectedItems = new HashSet<>(Arrays.asList(
				"brush", "escalator",
				"lift_buttons_link_connector", "lift_buttons_link_remover", "lift_door_1", "lift_door_odd_1", "lift_refresher",
				// block items
				"lift_buttons_1", "lift_panel_even_1", "lift_panel_odd_1", "lift_panel_even_2", "lift_panel_odd_2",
				"lift_track_horizontal_1", "lift_track_1", "lift_track_diagonal_1", "lift_track_floor_1"
			));
			final Set<String> expectedBlockEntityTypes = new HashSet<>(Arrays.asList(
				"lift_buttons_1", "lift_panel_even_1", "lift_panel_odd_1", "lift_panel_even_2", "lift_panel_odd_2",
				"lift_track_floor_1", "lift_door_1", "lift_door_odd_1"
			));
			final Set<String> forbidden = new HashSet<>(Arrays.asList(
				"apg_door", "psd_door", "psd_top", "rail", "boat_node", "ticket_machine", "signal_connector_white",
				"train_sensor", "railway_sign_2_even", "station_color_stone", "pids_1", "clock", "logo"
			));

			check("BLOCKS", new HashSet<>(blocks), expectedBlocks);
			check("ITEMS", new HashSet<>(items), expectedItems);
			check("BLOCK_ENTITY_TYPES", new HashSet<>(blockEntityTypes), expectedBlockEntityTypes);
			checkSingle("ITEM_GROUPS", itemGroups, "escalators_lifts");
			checkAbsent("BLOCKS", blocks, forbidden);
			checkAbsent("ITEMS", items, forbidden);
			checkAbsent("BLOCK_ENTITY_TYPES", blockEntityTypes, forbidden);

			System.out.println("ALL REGISTRATION CHECKS PASSED");
			verdict.write("ALL REGISTRATION CHECKS PASSED\n");
			verdict.flush();
		} catch (Throwable t) {
			t.printStackTrace(System.out);
			System.out.println("REGISTRATION CHECK FAILED");
			System.exit(1);
		}
	}

	private static List<String> dump(String name, DeferredRegister<?> register) {
		final List<String> names = new ArrayList<>();
		for (final DeferredHolder<?, ?> holder : register.getEntries()) {
			names.add(holder.getKey().location().getPath());
		}
		names.sort(String::compareTo);
		System.out.println("### " + name + " (" + names.size() + ") ###");
		names.forEach(System.out::println);
		return names;
	}

	private static void check(String name, Set<String> actual, Set<String> expected) {
		if (!actual.equals(expected)) {
			final Set<String> missing = new HashSet<>(expected);
			missing.removeAll(actual);
			final Set<String> extra = new HashSet<>(actual);
			extra.removeAll(expected);
			System.out.println("FAIL " + name + " missing=" + missing + " extra=" + extra);
			System.exit(1);
		}
		System.out.println("PASS " + name + " (" + actual.size() + " entries)");
	}

	private static void checkSingle(String name, List<String> actual, String expected) {
		if (actual.size() != 1 || !actual.get(0).equals(expected)) {
			System.out.println("FAIL " + name + " actual=" + actual);
			System.exit(1);
		}
		System.out.println("PASS " + name + " = [" + expected + "]");
	}

	private static void checkAbsent(String name, List<String> actual, Set<String> forbidden) {
		for (final String s : forbidden) {
			if (actual.contains(s)) {
				System.out.println("FAIL " + name + " contains forbidden entry: " + s);
				System.exit(1);
			}
		}
		System.out.println("PASS " + name + " has none of the forbidden entries");
	}
}
