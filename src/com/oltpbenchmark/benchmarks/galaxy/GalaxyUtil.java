package com.oltpbenchmark.benchmarks.galaxy;

import java.util.Random;

public class GalaxyUtil {
    
    /**
     * Returns a random number in between start and end inclusive
     * @param start
     * @param end
     * @param rng
     * @return
     */
    public final static int randInt(final int start, final int end, final Random rng) {
        return rng.nextInt(end - start + 1) + start;
    }

}
