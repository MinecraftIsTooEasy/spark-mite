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

package me.lucko.spark.mite.mixin;

import me.lucko.spark.mite.SparkMiteMod;
import me.lucko.spark.mite.SparkMitePlugin;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void spark$onTickStart(CallbackInfo callbackInfo) {
        SparkMitePlugin plugin = SparkMiteMod.getPlugin();
        if (plugin != null) {
            plugin.runMainThreadTasks();
            plugin.getTickDispatcher().onTickStart();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void spark$onTickEnd(CallbackInfo callbackInfo) {
        SparkMitePlugin plugin = SparkMiteMod.getPlugin();
        if (plugin != null) {
            plugin.getTickDispatcher().onTickEnd();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void spark$onServerStopping(CallbackInfo callbackInfo) {
        SparkMiteMod.stopServer((MinecraftServer) (Object) this);
    }
}
