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

package me.lucko.spark.mite.mixin.client;

import me.lucko.spark.mite.SparkMiteClientPlugin;
import me.lucko.spark.mite.SparkMiteMod;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "runTick", at = @At("HEAD"))
    private void spark$onTickStart(CallbackInfo callbackInfo) {
        SparkMiteClientPlugin plugin = SparkMiteMod.getClientPlugin();
        if (plugin != null) {
            plugin.runMainThreadTasks();
            plugin.getTickDispatcher().onTickStart();
        }
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void spark$onTickEnd(CallbackInfo callbackInfo) {
        SparkMiteClientPlugin plugin = SparkMiteMod.getClientPlugin();
        if (plugin != null) {
            plugin.getTickDispatcher().onTickEnd();
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void spark$onShutdown(CallbackInfo callbackInfo) {
        SparkMiteMod.stopClient((Minecraft) (Object) this);
    }
}
