package org.mtr.registry;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mtr.MTR;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.neoforge.MTRNeoForge;
import org.mtr.neoforge.MainEventBus;
import org.mtr.neoforge.ModEventBus;
import org.mtr.packet.CustomPacketC2S;
import org.mtr.packet.CustomPacketS2C;
import org.mtr.packet.PacketBufferReceiver;
import org.mtr.packet.PacketBufferSender;
import org.mtr.packet.PacketHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * MEE (Modern Elevators and Escalators) patched variant of RegistryServer.
 * Only registers the lift / escalator / PSD / APG feature subset.
 */
public final class RegistryServer {

	public static BiConsumer<CustomPacketS2C, IPayloadContext> s2cClientHandler = (packet, context) -> {
	};

	private static final ObjectArrayList<Runnable> OBJECTS_TO_REGISTER = new ObjectArrayList<>();
	private static final Object2ObjectOpenHashMap<String, ObjectArrayList<Supplier<ItemLike>>> ITEM_GROUP_ENTRIES = new Object2ObjectOpenHashMap<>();
	private static final String ITEM_GROUP = "escalators_lifts";

	// MEE allowlist: faithfully mirrors the original Modern Elevators and Escalators build (Keys.LIFTS_ONLY)
	private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
		// Escalators
		"escalator", "escalator_side", "escalator_step",
		// Lifts
		"lift_buttons_1", "lift_panel_even_1", "lift_panel_odd_1", "lift_panel_even_2", "lift_panel_odd_2",
		"lift_track_horizontal_1", "lift_track_1", "lift_track_diagonal_1", "lift_track_floor_1",
		"lift_door_1", "lift_door_odd_1",
		"lift_buttons_link_connector", "lift_buttons_link_remover", "lift_refresher",
		// Misc: brush was registered in the original MEE build
		"brush"
	));

	private static boolean isAllowed(String registryName) {
		return ALLOWED.contains(registryName);
	}

	public static void init() {
	}

	public static ObjectHolder<Block> registerBlock(String registryName, Function<BlockBehaviour.Properties, Block> factory) {
		if (!isAllowed(registryName)) {
			return new ObjectHolder<>(() -> null);
		}
		return new ObjectHolder<>(MTRNeoForge.BLOCKS.register(registryName, identifier -> factory.apply(BlockBehaviour.Properties.of())));
	}

	public static ObjectHolder<Item> registerItem(String registryName, Function<Item.Properties, Item> factory, String itemGroupRegistryName) {
		if (!isAllowed(registryName)) {
			return new ObjectHolder<>(() -> null);
		}
		final ObjectHolder<Item> objectHolder = new ObjectHolder<>(MTRNeoForge.ITEMS.register(registryName, identifier -> factory.apply(new Item.Properties())));
		if (itemGroupRegistryName != null) {
			ITEM_GROUP_ENTRIES.computeIfAbsent(ITEM_GROUP, key -> new ObjectArrayList<>()).add(objectHolder::get);
		}
		return objectHolder;
	}

	public static <T extends BlockEntity> ObjectHolder<BlockEntityType<T>> registerBlockEntityType(String registryName, BiFunction<BlockPos, BlockState, T> factory, Supplier<Block> blockSupplier) {
		if (!isAllowed(registryName)) {
			return new ObjectHolder<>(() -> null);
		}
		return new ObjectHolder<>(MTRNeoForge.BLOCK_ENTITY_TYPES.register(registryName, () -> BlockEntityType.Builder.of(factory::apply, blockSupplier.get()).build(null)));
	}

	public static String registerItemGroup(String registryName, Supplier<ItemStack> iconSupplier) {
		if (ITEM_GROUP.equals(registryName)) {
			MTRNeoForge.ITEM_GROUPS.register(registryName, () -> CreativeModeTab.builder().icon(iconSupplier).title(Component.translatable(String.format("itemGroup.%s.%s", MTR.MOD_ID, registryName))).displayItems((displayContext, entries) -> ITEM_GROUP_ENTRIES.getOrDefault(registryName, new ObjectArrayList<>()).forEach(itemSupplier -> entries.accept(itemSupplier.get()))).build());
		}
		return registryName;
	}

	public static ObjectHolder<SoundEvent> registerSoundEvent(String registryName, Supplier<SoundEvent> supplier) {
		return new ObjectHolder<>(MTRNeoForge.SOUND_EVENTS.register(registryName, supplier));
	}

	public static void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
		MainEventBus.commandConsumer = consumer;
	}

	public static <T> ObjectHolder<DataComponentType<T>> registerDataComponentType(String registryName, Supplier<DataComponentType<T>> supplier) {
		return new ObjectHolder<>(MTRNeoForge.DATA_COMPONENT_TYPES.register(registryName, supplier));
	}

	public static void setupPackets() {
		ModEventBus.PAYLOAD_HANDLERS.add(payloadRegistrar -> payloadRegistrar.playBidirectional(MTR.PACKET_IDENTIFIER_C2S, StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, CustomPacketC2S::buffer, CustomPacketC2S::new), new DirectionalPayloadHandler<>((customPacketC2S, context) -> {
		}, (customPacketC2S, context) -> {
			final Player player = context.player();
			if (player instanceof ServerPlayer) {
				PacketBufferReceiver.receive(customPacketC2S.buffer(), packetBufferReceiver -> {
					final Function<PacketBufferReceiver, ? extends PacketHandler> getInstance = ModEventBus.PACKETS.get(packetBufferReceiver.readString());
					if (getInstance != null) {
						getInstance.apply(packetBufferReceiver).runServer(((ServerPlayer) player).server, (ServerPlayer) player);
					}
				}, ((ServerPlayer) player).server::execute);
			}
		})));
		ModEventBus.PAYLOAD_HANDLERS.add(payloadRegistrar -> payloadRegistrar.playBidirectional(MTR.PACKET_IDENTIFIER_S2C, StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, CustomPacketS2C::buffer, CustomPacketS2C::new), new DirectionalPayloadHandler<>(s2cClientHandler::accept, (customPacketS2C, context) -> {
		})));
	}

	public static <T extends PacketHandler> void registerPacket(Class<T> classObject, Function<PacketBufferReceiver, T> getInstance) {
		ModEventBus.PACKETS.put(classObject.getName(), getInstance);
	}

	public static <T extends PacketHandler> void sendPacketToClient(ServerPlayer serverPlayerEntity, T data) {
		final PacketBufferSender packetBufferSender = new PacketBufferSender();
		packetBufferSender.writeString(data.getClass().getName());
		data.write(packetBufferSender);
		packetBufferSender.send(bytes -> PacketDistributor.sendToPlayer(serverPlayerEntity, new CustomPacketS2C(bytes)), serverPlayerEntity.server::execute);
	}

	// Kept for bytecode fidelity with the original class (unused on NeoForge)
	private static <T extends U, U> ObjectHolder<T> register(Registry<U> registry, ResourceKey<Registry<U>> registryKey, String registryName, Function<ResourceKey<U>, T> factory) {
		final ResourceKey<U> dataRegistryKey = ResourceKey.create(registryKey, ResourceLocation.fromNamespaceAndPath(MTR.MOD_ID, registryName));
		final T data = Registry.register(registry, dataRegistryKey, factory.apply(dataRegistryKey));
		final ObjectHolder<T> objectHolder = new ObjectHolder<>(() -> data);
		OBJECTS_TO_REGISTER.add(objectHolder::get);
		return objectHolder;
	}
}
