package com.oltpbenchmark.benchmarks.galaxy.util;

public class SolarSystem {
    public final int solarSystemId;
    public final int securityLevel;
    public final Long xMax;
    public final Long yMax;
    public final Long zMax;

    public SolarSystem(int solarSystemId, int securityLevel, Long xMax, Long yMax, Long zMax) {
        this.solarSystemId  = solarSystemId;
        this.securityLevel  = securityLevel;
        this.xMax           = xMax;
        this.yMax           = yMax;
        this.zMax           = zMax;
    }
}
