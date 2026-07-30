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

import me.lucko.spark.common.sampler.source.ClassSourceLookup;

import java.net.URI;
import java.nio.file.Path;

public final class MiteClassSourceLookup implements ClassSourceLookup {

    @Override
    public String identify(Class<?> clazz) {
        try {
            if (clazz.getProtectionDomain() == null || clazz.getProtectionDomain().getCodeSource() == null) {
                return null;
            }
            URI location = clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path fileName = Path.of(location).getFileName();
            return fileName == null ? null : fileName.toString();
        } catch (Exception ignored) {
            return null;
        }
    }
}
