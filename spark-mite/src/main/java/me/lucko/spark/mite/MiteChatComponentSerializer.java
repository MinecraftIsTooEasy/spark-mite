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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.ChatMessageComponent;

public final class MiteChatComponentSerializer {

    public static ChatMessageComponent serialize(Component component) {
        return ChatMessageComponent.createFromJson(GsonComponentSerializer.gson().serialize(component));
    }

    public static String serializePlain(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component)
                .replaceAll("\\u00a7[0-9A-FK-ORa-fk-or]", "");
    }

}
