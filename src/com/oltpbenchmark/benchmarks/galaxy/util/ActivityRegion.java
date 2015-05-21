package com.oltpbenchmark.benchmarks.galaxy.util;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import java.util.HashMap;

public class ActivityRegion {
    public final int solarSystemId;
    public final ImmutableTriple<Long, Long, Long> minPos;
    public final ImmutableTriple<Long, Long, Long> maxPos;
    public final HashMap<String, Integer> probabilityVector;

    public ActivityRegion(int solarSystemId, ImmutableTriple<Long, Long, Long> minPos,
            ImmutableTriple<Long, Long, Long> maxPos, HashMap<String, Integer> probabilityVector) {
        this.solarSystemId  = solarSystemId;
        this.minPos       = minPos;
        this.maxPos         = maxPos;
        this.probabilityVector = probabilityVector;
    }
    
    public String toString() {
        String result = "" + solarSystemId + ",";
        result += "" + minPos.left + "," + minPos.middle + "," + minPos.right + ",";
        result += "" + maxPos.left + "," + maxPos.middle + "," + maxPos.right + ",";
        return result;
    }
}
