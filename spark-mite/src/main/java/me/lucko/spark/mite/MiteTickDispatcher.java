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

import java.util.concurrent.CopyOnWriteArrayList;

public final class MiteTickDispatcher {

    private final CopyOnWriteArrayList<Runnable> tickCallbacks = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> startCallbacks = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> endCallbacks = new CopyOnWriteArrayList<>();

    void addTickCallback(Runnable callback) {
        this.tickCallbacks.add(callback);
    }

    void removeTickCallback(Runnable callback) {
        this.tickCallbacks.remove(callback);
    }

    void addReporter(Runnable start, Runnable end) {
        this.startCallbacks.add(start);
        this.endCallbacks.add(end);
    }

    void removeReporter(Runnable start, Runnable end) {
        this.startCallbacks.remove(start);
        this.endCallbacks.remove(end);
    }

    public void onTickStart() {
        this.tickCallbacks.forEach(Runnable::run);
        this.startCallbacks.forEach(Runnable::run);
    }

    public void onTickEnd() {
        this.endCallbacks.forEach(Runnable::run);
    }
}
