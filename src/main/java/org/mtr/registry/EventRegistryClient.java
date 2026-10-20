package org.mtr.registry;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mtr.MTR;
import org.mtr.MTRClient;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


import org.mtr.neoforge.MainEventBusClient;
import org.mtr.neoforge.ModEventBusClient;

public final class EventRegistryClient {

	public static void registerStartClientTick(Runnable runnable) {

		MainEventBusClient.startClientTickRunnable = runnable;
//
	}

	public static void registerEndClientTick(Runnable runnable) {

		MainEventBusClient.endClientTickRunnable = runnable;
//
	}

	public static void registerStartWorldTick(Consumer<ClientLevel> consumer) {

		MainEventBusClient.startWorldTickRunnable = consumer;
//
	}

	public static void registerEndWorldTick(Consumer<ClientLevel> consumer) {

		MainEventBusClient.endWorldTickRunnable = consumer;
//
	}

	public static void registerClientJoin(Runnable runnable) {

		MainEventBusClient.clientJoinRunnable = runnable;
//
	}

	public static void registerClientDisconnect(Runnable runnable) {

		MainEventBusClient.clientDisconnectRunnable = runnable;
//
	}

	public static void registerChunkLoad(BiConsumer<ClientLevel, ChunkAccess> consumer) {

		ModEventBusClient.chunkLoadConsumer = consumer;
//
	}

	public static void registerChunkUnload(BiConsumer<ClientLevel, ChunkAccess> consumer) {

		ModEventBusClient.chunkUnloadConsumer = consumer;
//
	}

	public static void registerResourceReloadEvent(Runnable runnable) {

		ModEventBusClient.resourceReloadRunnable = runnable;
//
	}

	public static void registerWorldRenderEvent(MTRClient.WorldRenderCallback worldRenderCallback) {

		MainEventBusClient.worldRenderCallback = worldRenderCallback;
//
	}

	public static void registerHudLayerRenderEvent(Consumer<GuiGraphics> hudLayerRenderCallback) {

		MainEventBusClient.hudLayerRenderCallback = hudLayerRenderCallback;
//
	}
}
