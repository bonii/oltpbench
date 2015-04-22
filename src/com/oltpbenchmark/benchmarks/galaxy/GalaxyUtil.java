package com.oltpbenchmark.benchmarks.galaxy;

import java.util.Random;

public class GalaxyUtil {
    
    public final static long nextLong(final Random rng, final long n) {
        long bits, val;
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % n;
        } while (bits - val + (n-1) < 0L);
        return val;
    }
    
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
    
    public final static long randLong(final long start, final long end, final Random rng) {
        return nextLong(rng, end - start + 1) + start;
    }

}
