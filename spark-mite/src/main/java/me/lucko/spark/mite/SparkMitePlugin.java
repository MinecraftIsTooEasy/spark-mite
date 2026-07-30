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

package me.lucko.spark.mite;

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
import me.lucko.spark.mite.command.SparkMiteCommandSender;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.xiaoyu233.fml.FishModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class SparkMitePlugin implements SparkPlugin {

    private final MinecraftServer server;
    private final Logger logger = LogManager.getLogger("spark-mite");
    private final ScheduledExecutorService scheduler = new SparkScheduledThreadPoolExecutor(4);
    private final ConcurrentLinkedQueue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();
    private final MiteTickDispatcher tickDispatcher = new MiteTickDispatcher();
    private final ThreadDumper gameThreadDumper;

    private SparkPlatform platform;

    SparkMitePlugin(MinecraftServer server) {
        this.server = server;
        this.gameThreadDumper = new ThreadDumper.Specific(server.thread);
    }

    public void enable() {
        this.platform = new SparkPlatform(this);
        this.platform.enable();
        this.logger.info("spark {} enabled for MITE 1.6.4", getVersion());
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

    public MiteTickDispatcher getTickDispatcher() {
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
        return FishModLoader.getModContainer("spark_mite")
                .map(ModContainer::getMetadata)
                .map(metadata -> metadata.getVersion().getFriendlyString())
                .orElse("1.10.0-mite");
    }

    @Override
    public Path getPluginDirectory() {
        return FishModLoader.CONFIG_DIR.toPath().resolve("spark");
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
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(SparkMiteCommandSender::new);
        return Stream.concat(playerSenders, Stream.of(new SparkMiteCommandSender(this.server)));
    }

    @Override
    public void executeAsync(Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void executeSync(Runnable task) {
        if (Thread.currentThread() == this.server.thread) {
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
        return new MiteTickHook(this.tickDispatcher);
    }

    @Override
    public TickReporter createTickReporter() {
        return new MiteTickReporter(this.tickDispatcher);
    }

    @Override
    public PlayerPingProvider createPlayerPingProvider() {
        return new MitePlayerPingProvider(this.server);
    }

    @Override
    public ServerConfigProvider createServerConfigProvider() {
        return new MiteServerConfigProvider();
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new MiteWorldInfoProvider(this.server);
    }

    @Override
    public ClassSourceLookup createClassSourceLookup() {
        return new MiteClassSourceLookup();
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new MitePlatformInfo();
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
