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

import me.lucko.spark.common.platform.PlatformInfo;
import net.fabricmc.loader.api.FabricLoader;

public final class BtwClientPlatformInfo implements PlatformInfo {

    @Override
    public Type getType() {
        return Type.CLIENT;
    }

    @Override
    public String getName() {
        return "Fabric Loader";
    }

    @Override
    public String getBrand() {
        return "Better Than Wolves Community Edition";
    }

    @Override
    public String getVersion() {
        return FabricLoader.getInstance().getModContainer("btw")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public String getMinecraftVersion() {
        return "1.6.4";
    }
}
