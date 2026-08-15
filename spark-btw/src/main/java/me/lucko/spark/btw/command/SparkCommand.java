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

package me.lucko.spark.btw.command;

import me.lucko.spark.btw.SparkBtwMod;
import me.lucko.spark.btw.SparkBtwPlugin;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;

import java.util.Collections;
import java.util.List;

public final class SparkCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "spark";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/spark [help|health|profiler|tickmonitor|gcmonitor|heap|activity]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return new SparkBtwCommandSender(sender).hasPermission("spark");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        SparkBtwPlugin plugin = SparkBtwMod.getPlugin();
        if (plugin == null) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("spark is not available until the server has started."));
            return;
        }

        plugin.getPlatform().executeCommand(new SparkBtwCommandSender(sender), args);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        SparkBtwPlugin plugin = SparkBtwMod.getPlugin();
        if (plugin == null) {
            return Collections.emptyList();
        }
        return plugin.getPlatform().tabCompleteCommand(new SparkBtwCommandSender(sender), args);
    }
}
