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

import me.lucko.spark.common.command.sender.AbstractCommandSender;
import me.lucko.spark.btw.BtwChatComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.Minecraft;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class SparkBtwClientCommandSender extends AbstractCommandSender<Minecraft> {

    public SparkBtwClientCommandSender(Minecraft client) {
        super(client);
    }

    @Override
    public String getName() {
        return this.delegate.thePlayer == null ? "Client" : this.delegate.thePlayer.getCommandSenderName();
    }

    @Override
    public UUID getUniqueId() {
        if (this.delegate.thePlayer == null) {
            return null;
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendMessage(Component message) {
        ChatMessageComponent component = BtwChatComponentSerializer.serialize(message);
        if (this.delegate.thePlayer != null) {
            this.delegate.thePlayer.sendChatToPlayer(component);
            return;
        }
        if (this.delegate.ingameGUI != null) {
            this.delegate.ingameGUI.getChatGUI().printChatMessage(component.toStringWithFormatting(true));
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }
}
