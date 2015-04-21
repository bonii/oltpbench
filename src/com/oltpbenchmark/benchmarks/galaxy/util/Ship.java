package com.oltpbenchmark.benchmarks.galaxy.util;

public class Ship {
    
    public final int shipId;
    
    // Move
    // TODO triple!
    public int positionX;
    public int positionY;
    public int positionZ;
    public int reachability;
    
    // Combat
    public int healthPoints;
    public int damage;
    public int defence;
    
    public Ship(int shipId) {
        this.shipId = shipId;
    }

}
