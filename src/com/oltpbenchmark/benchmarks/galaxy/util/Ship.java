package com.oltpbenchmark.benchmarks.galaxy.util;

import java.util.ArrayList;

import com.oltpbenchmark.util.Pair;

public class Ship {
    public final int shipId;
    public int healthPoints;

    public final int damage;
    public final int defence;

    public Ship(int shipId, int healthPoints, int damage, int defence) {
        this.shipId = shipId;
        this.healthPoints = healthPoints;
        this.damage = damage;
        this.defence = defence;
    }

    /*public void setDamageDefence(int damage, int defence) {
        this.damage = damage;
        this.defence = defence;
    }*/

}
