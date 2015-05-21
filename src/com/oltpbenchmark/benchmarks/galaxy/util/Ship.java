package com.oltpbenchmark.benchmarks.galaxy.util;

import org.apache.commons.lang3.tuple.ImmutableTriple;

public class Ship {

    public final int shipId;

    // Move
    public ImmutableTriple<Long, Long, Long> position;
    public long reachability;

    // Combat
    public int healthPoints;
    public int damage;
    public int defence;

    public Ship(int shipId) {
        this.shipId = shipId;
    }

}
