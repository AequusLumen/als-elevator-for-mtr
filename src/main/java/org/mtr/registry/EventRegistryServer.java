package org.mtr.registry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


import org.mtr.neoforge.MainEventBus;
import org.mtr.neoforge.ModEventBus;

public final class EventRegistryServer {

	public static void registerServerStarting(Consumer<MinecraftServer> consumer) {

		MainEventBus.serverStartingConsumer = consumer;
//
	}

	public static void registerServerStarted(Consumer<MinecraftServer> consumer) {

		MainEventBus.serverStartedConsumer = consumer;
//
	}

	public static void registerServerStopping(Consumer<MinecraftServer> consumer) {

		MainEventBus.serverStoppingConsumer = consumer;
//
	}

	public static void registerServerStopped(Consumer<MinecraftServer> consumer) {

		MainEventBus.serverStoppedConsumer = consumer;
//
	}

	public static void registerStartServerTick(Runnable runnable) {

		MainEventBus.startServerTickRunnable = runnable;
//
	}

	public static void registerEndServerTick(Runnable runnable) {

		MainEventBus.endServerTickRunnable = runnable;
//
	}

	public static void registerStartWorldTick(Consumer<ServerLevel> consumer) {

		MainEventBus.startWorldTickRunnable = consumer;
//
	}

	public static void registerEndWorldTick(Consumer<ServerLevel> consumer) {

		MainEventBus.endWorldTickRunnable = consumer;
//
	}

	public static void registerPlayerJoin(BiConsumer<MinecraftServer, ServerPlayer> consumer) {

		MainEventBus.playerJoinRunnable = consumer;
//
	}

	public static void registerPlayerDisconnect(BiConsumer<MinecraftServer, ServerPlayer> consumer) {

		MainEventBus.playerDisconnectRunnable = consumer;
//
	}

	public static void registerChunkLoad(BiConsumer<ServerLevel, ChunkAccess> consumer) {

		ModEventBus.chunkLoadConsumer = consumer;
//
	}

	public static void registerChunkUnload(BiConsumer<ServerLevel, ChunkAccess> consumer) {

		ModEventBus.chunkUnloadConsumer = consumer;
//
	}
}
