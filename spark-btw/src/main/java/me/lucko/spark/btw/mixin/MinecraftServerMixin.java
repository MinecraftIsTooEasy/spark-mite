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

package me.lucko.spark.btw.mixin;

import me.lucko.spark.btw.SparkBtwMod;
import me.lucko.spark.btw.SparkBtwPlugin;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void spark$onTickStart(CallbackInfo callbackInfo) {
        SparkBtwMod.startServer((MinecraftServer) (Object) this);
        SparkBtwPlugin plugin = SparkBtwMod.getPlugin();
        if (plugin != null) {
            plugin.runMainThreadTasks();
            plugin.getTickDispatcher().onTickStart();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void spark$onTickEnd(CallbackInfo callbackInfo) {
        SparkBtwPlugin plugin = SparkBtwMod.getPlugin();
        if (plugin != null) {
            plugin.getTickDispatcher().onTickEnd();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void spark$onServerStopping(CallbackInfo callbackInfo) {
        SparkBtwMod.stopServer((MinecraftServer) (Object) this);
    }
}
