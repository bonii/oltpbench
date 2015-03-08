package com.oltpbenchmark.benchmarks.galaxy.util;

public class Coordinate {

    /**
     * A class to hold a single coordinate
     */
    public int x;
    public int y;

    /**
     * Creates a new instance of the Coordinate class
     * @param x The value of the x position
     * @param y The value of the y position
     */
    public Coordinate(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Compares the the object with a given other object
     * @param other The object to compare with
     * @return True if both x and y are equal between the two objects
     */
    public boolean equals(final Coordinate other) {
        return this.x == other.x && this.y == other.y;
    }
}
