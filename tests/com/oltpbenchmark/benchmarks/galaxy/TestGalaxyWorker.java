package com.oltpbenchmark.benchmarks.galaxy;

import com.oltpbenchmark.api.AbstractTestWorker;

/**
 * A class that sets up workers and test the benchmarks procedures
 */
public class TestGalaxyWorker extends AbstractTestWorker<GalaxyBenchmark> {

    @Override
    protected void setUp() throws Exception {
        super.setUp(GalaxyBenchmark.class, TestGalaxyBenchmark.PROC_CLASSES);
        conn.setAutoCommit(false);
        super.testExecuteWork();
    }
    
}
