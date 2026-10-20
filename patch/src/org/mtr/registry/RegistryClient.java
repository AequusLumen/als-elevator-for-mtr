package org.mtr.registry;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.mtr.neoforge.ModEventBus;
import org.mtr.neoforge.ModEventBusClient;
import org.mtr.packet.CustomPacketC2S;
import org.mtr.packet.PacketBufferReceiver;
import org.mtr.packet.PacketBufferSender;
import org.mtr.packet.PacketHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * MEE (Modern Elevators and Escalators) patched variant of RegistryClient.
 * All registrations are guarded so that unregistered (cut) content never reaches the game.
 */
public final class RegistryClient {

	public static <T extends BlockEntity, U extends T> void registerBlockEntityRenderer(ObjectHolder<BlockEntityType<U>> blockEntityType, BlockEntityRendererProvider<T> factory) {
		ModEventBusClient.BLOCK_ENTITY_RENDERERS.add(event -> {
			final BlockEntityType<U> type = blockEntityType.get();
			if (type != null) {
				event.registerBlockEntityRenderer(type, factory);
			}
		});
	}

	public static void registerBlockRenderType(RenderType renderLayer, ObjectHolder<Block> block) {
		ModEventBusClient.CLIENT_OBJECTS_TO_REGISTER.add(() -> {
			final Block blockObject = block.get();
			if (blockObject != null) {
				ItemBlockRenderTypes.setRenderLayer(blockObject, renderLayer);
			}
		});
	}

	public static void registerKeyBinding(KeyMapping keyBinding) {
		ModEventBusClient.KEY_BINDINGS.add(keyBinding);
	}

	@SafeVarargs
	public static void registerBlockColors(BlockColor blockColorProvider, ObjectHolder<Block>... blocks) {
		ModEventBusClient.BLOCK_COLORS.add(event -> {
			final Block[] blockArray = Arrays.stream(blocks).map(ObjectHolder::get).filter(Objects::nonNull).toArray(Block[]::new);
			if (blockArray.length > 0) {
				event.getBlockColors().register(blockColorProvider, blockArray);
			}
		});
	}

	public static void setupPackets() {
		RegistryServer.s2cClientHandler = (customPacketS2C, context) -> PacketBufferReceiver.receive(customPacketS2C.buffer(), packetBufferReceiver -> {
			final Function<PacketBufferReceiver, ? extends PacketHandler> getInstance = ModEventBus.PACKETS.get(packetBufferReceiver.readString());
			if (getInstance != null) {
				getInstance.apply(packetBufferReceiver).runClient();
			}
		}, Minecraft.getInstance()::execute);
	}

	public static <T extends PacketHandler> void sendPacketToServer(T data) {
		final PacketBufferSender packetBufferSender = new PacketBufferSender();
		packetBufferSender.writeString(data.getClass().getName());
		data.write(packetBufferSender);
		packetBufferSender.send(bytes -> PacketDistributor.sendToServer(new CustomPacketC2S(bytes)), Minecraft.getInstance()::execute);
	}
}
