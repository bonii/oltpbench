package com.oltpbenchmark.benchmarks.galaxy.util;

import java.util.ArrayList;

import com.oltpbenchmark.util.Pair;

public class Ship {
    private final int shipId;
    private final int pos_x;
    private final int pos_y;
    private final int classId;
    private final int solarSystemId;
    private final int healthPoints;

    private final int damage;
    private final int defence;

    public Ship(int shipId, int pos_x, int pos_y, int classId,
        int solarSystemId, int healthPoints) {
      this.shipId = shipId;
      this.pos_x = pos_x;
      this.pos_y = pos_y;
      this.classId = classId;
      this.solarSystemId = solarSystemId;
      this.healthPoints = healthPoints;
    }

    public void statsFromFittings(ArrayList<Pair<int, bool>> fittingValues) {
        int tempDamage = 0;
        int tempDefence = 0;
        for (Pair<int, bool> valueType : fittingValues) {
            if (valueType.second) {
                tempDamage += Pair.first;
            } else {
                tempDefence += Pair.first;
            }
        }
        this.damage = tempDamage;
        this.defence = tempDefence;
    }

    public int getDamage() {
        return this.damage
    }

    public int getDefence() {
        return this.defence
    }

}
