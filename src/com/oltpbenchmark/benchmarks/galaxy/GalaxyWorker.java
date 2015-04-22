package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Combat;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;
import com.oltpbenchmark.types.TransactionStatus;
import com.oltpbenchmark.util.Triple;

/**
 * A class, which handles the work a worker needs to do
 */
public class GalaxyWorker extends Worker {

    /**
     * Creates a new instance of the GalaxyWorker class
     * @param benchmarkModule The benchmark the worker is initialized from
     * @param id The id of the worker
     */
    public GalaxyWorker(GalaxyBenchmark benchmarkModule, int id) {
        super(benchmarkModule, id);
    }

    @Override
    protected TransactionStatus executeWork(TransactionType txnType) throws UserAbortException, SQLException {
        Move proc = getProcedure(Move.class);
        assert(proc != null);
        Random rng = new Random();
        proc.run(conn, 6, new Triple<Integer, Integer, Integer>(0, 0, 0), new Triple<Integer, Integer, Integer>(71, 82, 20), rng);
        conn.commit();
        return TransactionStatus.SUCCESS;
    }

}
