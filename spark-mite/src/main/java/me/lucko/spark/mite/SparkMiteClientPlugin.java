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
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.common.util.SparkScheduledThreadPoolExecutor;
import me.lucko.spark.mite.command.SparkMiteClientCommandSender;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.Minecraft;
import net.xiaoyu233.fml.FishModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class SparkMiteClientPlugin implements SparkPlugin {

    private final Minecraft client;
    private final Thread clientThread;
    private final Logger logger = LogManager.getLogger("spark-mite-client");
    private final ScheduledExecutorService scheduler = new SparkScheduledThreadPoolExecutor(4);
    private final ConcurrentLinkedQueue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();
    private final MiteTickDispatcher tickDispatcher = new MiteTickDispatcher();
    private final ThreadDumper threadDumper;

    private SparkPlatform platform;

    SparkMiteClientPlugin(Minecraft client) {
        this.client = client;
        this.clientThread = Thread.currentThread();
        this.threadDumper = new ThreadDumper.Specific(this.clientThread);
    }

    public void enable() {
        this.platform = new SparkPlatform(this);
        this.platform.enable();
        this.logger.info("spark client profiler {} enabled", getVersion());
    }

    public void disable() {
        if (this.platform != null && this.platform.hasEnabled()) {
            this.platform.disable();
        }
        this.scheduler.shutdown();
        this.mainThreadTasks.clear();
    }

    public Minecraft getClient() {
        return this.client;
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
                this.logger.error("Exception while executing a spark client task", throwable);
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
        return "sparkc";
    }

    @Override
    public Stream<? extends CommandSender> getCommandSenders() {
        return Stream.of(new SparkMiteClientCommandSender(this.client));
    }

    @Override
    public void executeAsync(Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void executeSync(Runnable task) {
        if (Thread.currentThread() == this.clientThread) {
            task.run();
        } else {
            this.mainThreadTasks.add(task);
        }
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.threadDumper;
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
    public WorldInfoProvider createWorldInfoProvider() {
        return new MiteClientWorldInfoProvider(this.client);
    }

    @Override
    public ClassSourceLookup createClassSourceLookup() {
        return new MiteClassSourceLookup();
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new MiteClientPlatformInfo();
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
