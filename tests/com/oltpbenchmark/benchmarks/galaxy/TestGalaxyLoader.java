package com.oltpbenchmark.benchmarks.galaxy;

import com.oltpbenchmark.api.AbstractTestLoader;

/**
 * A class that sets up the loader to test the Galaxy benchmark
 */
public class TestGalaxyLoader extends AbstractTestLoader<GalaxyBenchmark> {

    @Override
    protected void setUp() throws Exception {
        super.setUp(GalaxyBenchmark.class, null, TestGalaxyBenchmark.PROC_CLASSES);
        this.workConf.setScaleFactor(0.0001);
    }

}
