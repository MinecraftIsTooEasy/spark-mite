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

import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public final class BtwPlayerPingProvider implements PlayerPingProvider {

    private final MinecraftServer server;

    BtwPlayerPingProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Map<String, Integer> poll() {
        Map<String, Integer> result = new HashMap<>();
        for (Object object : this.server.getConfigurationManager().playerEntityList) {
            if (object instanceof EntityPlayerMP player) {
                result.put(player.getCommandSenderName(), player.ping);
            }
        }
        return result;
    }
}
