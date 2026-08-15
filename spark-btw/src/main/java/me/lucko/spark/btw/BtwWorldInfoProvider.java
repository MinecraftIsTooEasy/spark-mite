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

import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.btw.mixin.ChunkProviderServerAccessor;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkProviderServer;
import net.minecraft.src.Entity;
import net.minecraft.src.GameRules;
import net.minecraft.src.WorldServer;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class BtwWorldInfoProvider implements WorldInfoProvider {

    private final MinecraftServer server;

    BtwWorldInfoProvider(MinecraftServer server) {
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
            chunks += world.theChunkProviderServer.getLoadedChunkCount();
        }

        int players = this.server.getConfigurationManager().playerEntityList.size();
        return new CountsResult(players, entities, tileEntities, chunks);
    }

    @Override
    public ChunksResult<BtwChunkInfo> pollChunks() {
        ChunksResult<BtwChunkInfo> result = new ChunksResult<>();
        for (WorldServer world : this.server.worldServers) {
            if (world == null) {
                continue;
            }

            List<BtwChunkInfo> chunks = new ArrayList<>();
            ChunkProviderServer provider = world.theChunkProviderServer;
            if (provider == null) {
                result.put(world.provider.getDimensionName(), chunks);
                continue;
            }

            List<Chunk> loadedChunks = ((ChunkProviderServerAccessor) (Object) provider).spark$getLoadedChunks();
            if (loadedChunks == null) {
                result.put(world.provider.getDimensionName(), chunks);
                continue;
            }

            for (Chunk chunk : loadedChunks) {
                chunks.add(new BtwChunkInfo(chunk));
            }
            result.put(world.provider.getDimensionName(), chunks);
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
                result.put(rule, world.provider.getDimensionName(), value);
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

    public static final class BtwChunkInfo extends AbstractChunkInfo<String> {
        private final CountMap<String> entityCounts;

        BtwChunkInfo(Chunk chunk) {
            super(chunk.xPosition, chunk.zPosition);
            this.entityCounts = new CountMap.Simple<>(new HashMap<>());
            for (List<?> entityList : chunk.entityLists) {
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
