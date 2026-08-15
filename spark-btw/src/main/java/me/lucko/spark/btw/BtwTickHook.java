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

import me.lucko.spark.common.tick.AbstractTickHook;

public final class BtwTickHook extends AbstractTickHook {

    private final BtwTickDispatcher dispatcher;
    private final Runnable callback = this::onTick;

    BtwTickHook(BtwTickDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void start() {
        this.dispatcher.addTickCallback(this.callback);
    }

    @Override
    public void close() {
        this.dispatcher.removeTickCallback(this.callback);
    }
}
