package com.oltpbenchmark.benchmarks.galaxy.util;

import com.oltpbenchmark.util.Triple;

public class Ship {
    
    public final int shipId;
    
    // Move
    // TODO triple!
    public Triple<Integer, Integer, Integer> position;
    public int reachability;
    
    // Combat
    public int healthPoints;
    public int damage;
    public int defence;
    
    public Ship(int shipId) {
        this.shipId = shipId;
    }

}
