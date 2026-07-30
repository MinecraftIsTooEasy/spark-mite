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

import me.lucko.spark.common.platform.PlatformInfo;
import net.xiaoyu233.fml.FishModLoader;

public final class MitePlatformInfo implements PlatformInfo {
    @Override
    public Type getType() {
        return Type.SERVER;
    }

    @Override
    public String getName() {
        return "FishModLoader";
    }

    @Override
    public String getBrand() {
        return "MITE";
    }

    @Override
    public String getVersion() {
        return FishModLoader.VERSION;
    }

    @Override
    public String getMinecraftVersion() {
        return "1.6.4-MITE";
    }
}
