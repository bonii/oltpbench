package com.oltpbenchmark.benchmarks.galaxy.util;

import org.apache.commons.lang3.tuple.ImmutableTriple;

public class ActivityRegion {
    public final int solarSystemId;
    public final ImmutableTriple<Long, Long, Long> minPos;
    public final ImmutableTriple<Long, Long, Long> maxPos;

    public ActivityRegion(int solarSystemId, ImmutableTriple<Long, Long, Long> minPos,
            ImmutableTriple<Long, Long, Long> maxPos) {
        this.solarSystemId  = solarSystemId;
        this.minPos       = minPos;
        this.maxPos         = maxPos;
    }

    public String toString() {
        String result = "" + solarSystemId + ",";
        result += "" + minPos.left + "," + minPos.middle + "," + minPos.right + ",";
        result += "" + maxPos.left + "," + maxPos.middle + "," + maxPos.right + ",";
        return result;
    }
}
