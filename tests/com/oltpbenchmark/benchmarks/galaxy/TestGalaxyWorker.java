package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.AbstractTestWorker;
import com.oltpbenchmark.api.Worker;
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
    }
    
    public void testMove() throws SQLException, Exception {
        this.benchmark.createDatabase();
        this.benchmark.loadDatabase();
        Worker w = workers.get(0);
        Move proc = w.getProcedure(Move.class);
        if (proc != null) {
            new TestMove().run(conn, proc, new Random());
        }
    }
    
}
