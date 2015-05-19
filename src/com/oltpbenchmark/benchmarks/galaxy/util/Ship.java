package com.oltpbenchmark.benchmarks.galaxy.util;

import com.oltpbenchmark.util.Triple;

public class Ship {
    
    public final int shipId;
    
    // Idle
    public int class_id;
    
    // Move
    // TODO triple!
    public Triple<Long, Long, Long> position;
    public int reachability;
    
    // Combat
    public int healthPoints;
    public int damage;
    public int defence;
    
    public Ship(int shipId) {
        this.shipId = shipId;
    }

}
