package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;
import com.oltpbenchmark.types.TransactionStatus;

public class GalaxyWorker extends Worker {


    public GalaxyWorker(GalaxyBenchmark benchmarkModule, int id) {
        super(benchmarkModule, id);
    }

    @Override
    protected TransactionStatus executeWork(TransactionType txnType) throws UserAbortException, SQLException {
        assert (txnType.getProcedureClass().equals(Move.class));
        Move proc = getProcedure(Move.class);
        assert (proc != null);
        Random rng = new Random();
        int ship_id = rng.nextInt(GalaxyConstants.NUM_SHIPS) + 1;
        int move_x = rng.nextInt(GalaxyConstants.MAX_MOVE * 2) - GalaxyConstants.MAX_MOVE;
        int move_y = rng.nextInt(GalaxyConstants.MAX_MOVE * 2) - GalaxyConstants.MAX_MOVE;
        proc.run(conn, ship_id, move_x, move_y);
        conn.commit();
        return TransactionStatus.SUCCESS;
    }

}
