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

import api.AddonHandler;
import api.BTWAddon;
import me.lucko.spark.btw.command.SparkCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Minecraft;

public final class SparkBtwMod extends BTWAddon {

    private static volatile SparkBtwPlugin plugin;
    private static volatile SparkBtwClientPlugin clientPlugin;

    @Override
    public void initialize() {
        registerAddonCommand(new SparkCommand());
        AddonHandler.logMessage("spark " + getVersionString() + " initialized");
    }

    public static synchronized void startClient(Minecraft client) {
        if (clientPlugin != null) {
            return;
        }

        SparkBtwClientPlugin created = new SparkBtwClientPlugin(client);
        created.enable();
        clientPlugin = created;
    }

    public static synchronized void stopClient(Minecraft client) {
        SparkBtwClientPlugin current = clientPlugin;
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

        SparkBtwPlugin created = new SparkBtwPlugin(server);
        created.enable();
        plugin = created;
    }

    public static synchronized void stopServer(MinecraftServer server) {
        SparkBtwPlugin current = plugin;
        if (current == null || current.getServer() != server) {
            return;
        }

        plugin = null;
        current.disable();
    }

    public static SparkBtwPlugin getPlugin() {
        return plugin;
    }

    public static SparkBtwClientPlugin getClientPlugin() {
        return clientPlugin;
    }
}
