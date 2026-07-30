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

import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.mite.mixin.ChunkProviderServerAccessor;
import net.minecraft.Chunk;
import net.minecraft.ChunkProviderServer;
import net.minecraft.Entity;
import net.minecraft.GameRules;
import net.minecraft.WorldServer;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class MiteWorldInfoProvider implements WorldInfoProvider {

    private final MinecraftServer server;

    MiteWorldInfoProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public CountsResult pollCounts() {
        int entities = 0;
        int tileEntities = 0;
        int chunks = 0;

        for (WorldServer world : this.server.worldServers) {
            if (world == null) {
                continue;
            }
            entities += world.loadedEntityList.size();
            tileEntities += world.loadedTileEntityList.size();
            chunks += world.theChunkProviderServer.loadedChunkHashMap.getNumHashElements();
        }

        int players = this.server.getConfigurationManager().playerEntityList.size();
        return new CountsResult(players, entities, tileEntities, chunks);
    }

    @Override
    public ChunksResult<MiteChunkInfo> pollChunks() {
        ChunksResult<MiteChunkInfo> result = new ChunksResult<>();
        for (WorldServer world : this.server.worldServers) {
            if (world == null) {
                continue;
            }

            List<MiteChunkInfo> chunks = new ArrayList<>();
            ChunkProviderServer provider = world.theChunkProviderServer;
            if (provider == null) {
                result.put(world.getDimensionName(), chunks);
                continue;
            }

            List<Chunk> loadedChunks = ((ChunkProviderServerAccessor) (Object) provider).spark$getLoadedChunks();
            if (loadedChunks == null) {
                result.put(world.getDimensionName(), chunks);
                continue;
            }

            for (Chunk chunk : loadedChunks) {
                chunks.add(new MiteChunkInfo(chunk));
            }
            result.put(world.getDimensionName(), chunks);
        }
        return result;
    }

    @Override
    public GameRulesResult pollGameRules() {
        GameRulesResult result = new GameRulesResult();
        boolean first = true;
        for (WorldServer world : this.server.worldServers) {
            if (world == null) {
                continue;
            }

            GameRules rules = world.getGameRules();
            for (String rule : rules.getRules()) {
                String value = rules.getGameRuleStringValue(rule);
                result.put(rule, world.getDimensionName(), value);
                if (first) {
                    result.putDefault(rule, value);
                }
            }
            first = false;
        }
        return result;
    }

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
        return Collections.emptyList();
    }

    public static final class MiteChunkInfo extends AbstractChunkInfo<String> {
        private final CountMap<String> entityCounts;

        MiteChunkInfo(Chunk chunk) {
            super(chunk.xPosition, chunk.zPosition);
            this.entityCounts = new CountMap.Simple<>(new HashMap<>());
            for (List<?> entityList : chunk.getEntityListsForReadingOnly()) {
                for (Object object : entityList) {
                    if (object instanceof Entity entity) {
                        this.entityCounts.increment(entity.getEntityName());
                    }
                }
            }
        }

        @Override
        public CountMap<String> getEntityCounts() {
            return this.entityCounts;
        }

        @Override
        public String entityTypeName(String type) {
            return type;
        }
    }
}
