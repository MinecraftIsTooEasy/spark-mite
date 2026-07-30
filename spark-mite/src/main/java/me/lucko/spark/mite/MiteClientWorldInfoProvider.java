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

import me.lucko.spark.common.platform.world.ChunkInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import net.minecraft.Chunk;
import net.minecraft.ChunkProviderClient;
import net.minecraft.GameRules;
import net.minecraft.LongHashMapEntry;
import net.minecraft.Minecraft;
import net.minecraft.WorldClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MiteClientWorldInfoProvider implements WorldInfoProvider {

    private final Minecraft client;

    MiteClientWorldInfoProvider(Minecraft client) {
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
        ChunksResult<MiteWorldInfoProvider.MiteChunkInfo> result = new ChunksResult<>();
        WorldClient world = this.client.theWorld;
        if (world == null || !(world.getChunkProvider() instanceof ChunkProviderClient provider)) {
            return result;
        }

        List<MiteWorldInfoProvider.MiteChunkInfo> chunks = new ArrayList<>();
        for (LongHashMapEntry bucket : provider.chunkMapping.hashArray) {
            LongHashMapEntry entry = bucket;
            while (entry != null) {
                Object value = entry.value;
                if (value instanceof Chunk chunk && !chunk.isEmpty()) {
                    chunks.add(new MiteWorldInfoProvider.MiteChunkInfo(chunk));
                }
                entry = entry.nextEntry;
            }
        }
        result.put(world.getDimensionName(), chunks);
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
            result.put(rule, world.getDimensionName(), value);
        }
        return result;
    }

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
        return Collections.emptyList();
    }
}
