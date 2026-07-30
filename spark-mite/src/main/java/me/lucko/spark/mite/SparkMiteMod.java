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

import me.lucko.spark.mite.command.SparkCommand;
import moddedmite.rustedironcore.api.event.Handlers;
import moddedmite.rustedironcore.api.event.listener.IInitializationListener;
import net.fabricmc.api.ModInitializer;
import net.minecraft.Minecraft;
import net.minecraft.server.MinecraftServer;

public final class SparkMiteMod implements ModInitializer {

    private static volatile SparkMitePlugin plugin;
    private static volatile SparkMiteClientPlugin clientPlugin;

    @Override
    public void onInitialize() {
        Handlers.Command.register(event -> event.register(new SparkCommand()));
        Handlers.Initialization.register(new IInitializationListener() {
            @Override
            public void onClientStarted(Minecraft client) {
                startClient(client);
            }

            @Override
            public void onServerStarted(MinecraftServer server) {
                startServer(server);
            }
        });
    }

    public static synchronized void startClient(Minecraft client) {
        if (clientPlugin != null) {
            return;
        }

        SparkMiteClientPlugin created = new SparkMiteClientPlugin(client);
        created.enable();
        clientPlugin = created;
    }

    public static synchronized void stopClient(Minecraft client) {
        SparkMiteClientPlugin current = clientPlugin;
        if (current == null || current.getClient() != client) {
            return;
        }

        clientPlugin = null;
        current.disable();
    }

    public static synchronized void startServer(MinecraftServer server) {
        if (plugin != null) {
            return;
        }

        SparkMitePlugin created = new SparkMitePlugin(server);
        created.enable();
        plugin = created;
    }

    public static synchronized void stopServer(MinecraftServer server) {
        SparkMitePlugin current = plugin;
        if (current == null || current.getServer() != server) {
            return;
        }

        plugin = null;
        current.disable();
    }

    public static SparkMitePlugin getPlugin() {
        return plugin;
    }

    public static SparkMiteClientPlugin getClientPlugin() {
        return clientPlugin;
    }
}
