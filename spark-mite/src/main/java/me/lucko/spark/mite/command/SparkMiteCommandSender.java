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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.ChatMessageComponent;
import net.minecraft.EntityPlayer;
import net.minecraft.ICommandSender;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class SparkMiteCommandSender extends AbstractCommandSender<ICommandSender> {

    public SparkMiteCommandSender(ICommandSender delegate) {
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
        String legacy = LegacyComponentSerializer.legacySection().serialize(message);
        if (!(this.delegate instanceof EntityPlayer)) {
            legacy = legacy.replaceAll("\\u00a7[0-9A-FK-ORa-fk-or]", "");
        }
        this.delegate.sendChatToPlayer(ChatMessageComponent.createFromText(legacy));
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
