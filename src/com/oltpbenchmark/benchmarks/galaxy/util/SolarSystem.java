package com.oltpbench.benchmarks.galaxy.util;

public class SolarSystem {
    public final int solarSystemId;
    public final int securityLevel;
    public final long xMax;
    public final long yMax;
    public final long zMax;

    public SolarSystem(int solarSystemId, int securityLevel, long xMax, long yMax, long zMax) {
        this.solarSystemId  = solarSystemId;
        this.securityLevel  = securityLevel;
        this.xMax           = xMax;
        this.yMax           = yMax;
        this.zMax           = zMax;
    }
