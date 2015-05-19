package com.oltpbenchmark.benchmarks.galaxy.util;

import org.apache.commons.lang3.tuple.ImmutableTriple;

public class Ship {
    
    public final int shipId;
    
    // Idle
    public int class_id;
    
    // Move
    // TODO triple!
    public ImmutableTriple<Long, Long, Long> position;
    public int reachability;
    
    // Combat
    public int healthPoints;
    public int damage;
    public int defence;
    
    public Ship(int shipId) {
        this.shipId = shipId;
    }

}
