package com.oltpbenchmark.benchmarks.galaxy;

import com.oltpbenchmark.api.AbstractTestWorker;

public class TestGalaxyWorker extends AbstractTestWorker<GalaxyBenchmark> {

    @Override
    protected void setUp() throws Exception {
        super.setUp(GalaxyBenchmark.class, TestGalaxyBenchmark.PROC_CLASSES);
    }
}
