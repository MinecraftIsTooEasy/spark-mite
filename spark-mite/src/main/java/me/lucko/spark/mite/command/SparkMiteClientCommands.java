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

package me.lucko.spark.mite.command;

import me.lucko.spark.mite.SparkMiteClientPlugin;
import me.lucko.spark.mite.SparkMiteMod;

import java.util.Arrays;

public final class SparkMiteClientCommands {

    public static boolean tryExecute(String message) {
        String raw = message.startsWith("/") ? message.substring(1) : message;
        String[] split = raw.split(" ");
        if (split.length == 0 || !(split[0].equalsIgnoreCase("sparkc") || split[0].equalsIgnoreCase("sparkclient"))) {
            return false;
        }

        SparkMiteClientPlugin plugin = SparkMiteMod.getClientPlugin();
        if (plugin == null) {
            return true;
        }

        String[] args = Arrays.copyOfRange(split, 1, split.length);
        plugin.getPlatform().executeCommand(new SparkMiteClientCommandSender(plugin.getClient()), args);
        return true;
    }
}
