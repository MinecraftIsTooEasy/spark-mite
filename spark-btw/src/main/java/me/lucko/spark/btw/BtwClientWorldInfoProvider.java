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

import me.lucko.spark.common.platform.world.ChunkInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.btw.mixin.client.ChunkProviderClientAccessor;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkProviderClient;
import net.minecraft.src.GameRules;
import net.minecraft.src.Minecraft;
import net.minecraft.src.WorldClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class BtwClientWorldInfoProvider implements WorldInfoProvider {

    private final Minecraft client;

    BtwClientWorldInfoProvider(Minecraft client) {
        this.client = client;
    }

    @Override
    public CountsResult pollCounts() {
        WorldClient world = this.client.theWorld;
        if (world == null) {
            return new CountsResult(0, 0, 0, 0);
        }
        int players = world.playerEntities.size();
        int chunks = world.getChunkProvider().getLoadedChunkCount();
        return new CountsResult(players, world.loadedEntityList.size(), world.loadedTileEntityList.size(), chunks);
    }

    @Override
    public ChunksResult<? extends ChunkInfo<?>> pollChunks() {
        ChunksResult<BtwWorldInfoProvider.BtwChunkInfo> result = new ChunksResult<>();
        WorldClient world = this.client.theWorld;
        if (world == null || !(world.getChunkProvider() instanceof ChunkProviderClient provider)) {
            return result;
        }

        List<BtwWorldInfoProvider.BtwChunkInfo> chunks = new ArrayList<>();
        for (Chunk chunk : ((ChunkProviderClientAccessor) (Object) provider).spark$getLoadedChunks()) {
            if (!chunk.isEmpty()) {
                chunks.add(new BtwWorldInfoProvider.BtwChunkInfo(chunk));
            }
        }
        result.put(world.provider.getDimensionName(), chunks);
        return result;
    }

    @Override
    public GameRulesResult pollGameRules() {
        GameRulesResult result = new GameRulesResult();
        WorldClient world = this.client.theWorld;
        if (world == null) {
            return result;
        }

        GameRules rules = world.getGameRules();
        for (String rule : rules.getRules()) {
            String value = rules.getGameRuleStringValue(rule);
            result.putDefault(rule, value);
            result.put(rule, world.provider.getDimensionName(), value);
        }
        return result;
    }

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
        return Collections.emptyList();
    }
}
