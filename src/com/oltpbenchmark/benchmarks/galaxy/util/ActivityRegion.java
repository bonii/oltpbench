package com.oltpbenchmark.benchmarks.galaxy.util;

import org.commons.lang3.tuple.ImmutableTriple;

public class ActivityRegion {
    public final int solarSystemId;
    public final ImmutableTriple startPos;
    public final ImmutabelTriple endPos;
    public final ArrayList<int> probabilityVector;

    public ActivityRegion(int solarSystemId, ImmutableTriple startPos,
            ImmutableTriple endPos, ArrayList<int> probabilityVector) {
        this.solarSystemId  = solarSystemId;
        this.startPos       = startPos;
        this.endPos         = endPos;
    }
}
