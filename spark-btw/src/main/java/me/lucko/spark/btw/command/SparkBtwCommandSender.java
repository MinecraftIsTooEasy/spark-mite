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
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICommandSender;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class SparkBtwCommandSender extends AbstractCommandSender<ICommandSender> {

    public SparkBtwCommandSender(ICommandSender delegate) {
        super(delegate);
    }

    @Override
    public String getName() {
        String name = this.delegate.getCommandSenderName();
        return "Server".equals(name) ? "Console" : name;
    }

    @Override
    public UUID getUniqueId() {
        if (!(this.delegate instanceof EntityPlayer)) {
            return null;
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendMessage(Component message) {
        if (!(this.delegate instanceof EntityPlayer)) {
            this.delegate.sendChatToPlayer(ChatMessageComponent.createFromText(BtwChatComponentSerializer.serializePlain(message)));
            return;
        }
        this.delegate.sendChatToPlayer(BtwChatComponentSerializer.serialize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return !(this.delegate instanceof EntityPlayer)
                || this.delegate.canCommandSenderUseCommand(4, "spark");
    }

    @Override
    protected Object getObjectForComparison() {
        UUID uniqueId = getUniqueId();
        return uniqueId == null ? getName() : uniqueId;
    }
}
