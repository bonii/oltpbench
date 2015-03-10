package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.AbstractTestWorker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;
import com.oltpbenchmark.benchmarks.galaxy.util.TestMove;

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
    
    public void testMove() throws SQLException {
        new TestMove().run(this.conn, new Move(), new Random());
    }
    
}
