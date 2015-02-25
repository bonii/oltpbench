package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;
import com.oltpbenchmark.benchmarks.galaxy.procedures.TestMove;
import com.oltpbenchmark.types.TransactionStatus;

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
        if (txnType.getProcedureClass().equals(Move.class)) {
            Move proc = getProcedure(Move.class);
            assert (proc != null);
            Random rng = new Random();

            // Generate a random move vector, within reason(MAX_MOVE)
            int ship_id = rng.nextInt(GalaxyConstants.NUM_SHIPS) + 1;
            int move_x = rng.nextInt(GalaxyConstants.MAX_MOVE * 2) - GalaxyConstants.MAX_MOVE;
            int move_y = rng.nextInt(GalaxyConstants.MAX_MOVE * 2) - GalaxyConstants.MAX_MOVE;
            proc.run(conn, ship_id, move_x, move_y);
            conn.commit();
            return TransactionStatus.SUCCESS;
        }

        // Give the Tests procedure the needed arguments
        if (txnType.getProcedureClass().equals(TestMove.class)) {
            TestMove proc = getProcedure(TestMove.class);
            Move moveProc = getProcedure(Move.class);
            proc.run(conn, moveProc);
            return TransactionStatus.SUCCESS;
        }
        return TransactionStatus.RETRY_DIFFERENT;
    }

}