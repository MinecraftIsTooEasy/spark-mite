/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package me.lucko.spark.btw;

import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.common.util.SparkScheduledThreadPoolExecutor;
import me.lucko.spark.btw.command.SparkBtwCommandSender;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayerMP;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class SparkBtwPlugin implements SparkPlugin {

    private final MinecraftServer server;
    private final Logger logger = LogManager.getLogger("spark-btw");
    private final ScheduledExecutorService scheduler = new SparkScheduledThreadPoolExecutor(4);
    private final ConcurrentLinkedQueue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();
    private final BtwTickDispatcher tickDispatcher = new BtwTickDispatcher();
    private final ThreadDumper gameThreadDumper;
    private final Thread serverThread;

    private SparkPlatform platform;

    SparkBtwPlugin(MinecraftServer server) {
        this.server = server;
        this.serverThread = Thread.currentThread();
        this.gameThreadDumper = new ThreadDumper.Specific(this.serverThread);
    }

    public void enable() {
        this.platform = new SparkPlatform(this);
        this.platform.enable();
        this.logger.info("spark {} enabled for Better Than Wolves CE", getVersion());
    }

    public void disable() {
        if (this.platform != null && this.platform.hasEnabled()) {
            this.platform.disable();
        }
        this.scheduler.shutdown();
        this.mainThreadTasks.clear();
        this.logger.info("spark disabled");
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public SparkPlatform getPlatform() {
        return this.platform;
    }

    public BtwTickDispatcher getTickDispatcher() {
        return this.tickDispatcher;
    }

    public void runMainThreadTasks() {
        Runnable task;
        while ((task = this.mainThreadTasks.poll()) != null) {
            try {
                task.run();
            } catch (Throwable throwable) {
                this.logger.error("Exception while executing a spark main-thread task", throwable);
            }
        }
    }

    @Override
    public String getVersion() {
        return FabricLoader.getInstance().getModContainer("spark")
                .map(ModContainer::getMetadata)
                .map(metadata -> metadata.getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public Path getPluginDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve("spark");
    }

    @Override
    public String getCommandName() {
        return "spark";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<? extends CommandSender> getCommandSenders() {
        List<Object> players = this.server.getConfigurationManager().playerEntityList;
        Stream<CommandSender> playerSenders = players.stream()
                .filter(EntityPlayerMP.class::isInstance)
                .map(EntityPlayerMP.class::cast)
                .map(SparkBtwCommandSender::new);
        return Stream.concat(playerSenders, Stream.of(new SparkBtwCommandSender(this.server)));
    }

    @Override
    public void executeAsync(Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void executeSync(Runnable task) {
        if (Thread.currentThread() == this.serverThread) {
            task.run();
        } else {
            this.mainThreadTasks.add(task);
        }
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper;
    }

    @Override
    public TickHook createTickHook() {
        return new BtwTickHook(this.tickDispatcher);
    }

    @Override
    public TickReporter createTickReporter() {
        return new BtwTickReporter(this.tickDispatcher);
    }

    @Override
    public PlayerPingProvider createPlayerPingProvider() {
        return new BtwPlayerPingProvider(this.server);
    }

    @Override
    public ServerConfigProvider createServerConfigProvider() {
        return new BtwServerConfigProvider();
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new BtwWorldInfoProvider(this.server);
    }

    @Override
    public ClassSourceLookup createClassSourceLookup() {
        return new BtwClassSourceLookup();
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new BtwPlatformInfo();
    }

    @Override
    public void log(Level level, String message) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            this.logger.error(message);
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            this.logger.warn(message);
        } else {
            this.logger.info(message);
        }
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            this.logger.error(message, throwable);
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            this.logger.warn(message, throwable);
        } else {
            this.logger.info(message, throwable);
        }
    }
}
