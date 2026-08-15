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

import me.lucko.spark.common.command.sender.AbstractCommandSender;
import me.lucko.spark.mite.MiteChatComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatMessageComponent;
import net.minecraft.Minecraft;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class SparkMiteClientCommandSender extends AbstractCommandSender<Minecraft> {

    public SparkMiteClientCommandSender(Minecraft client) {
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
        ChatMessageComponent component = MiteChatComponentSerializer.serialize(message);
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
