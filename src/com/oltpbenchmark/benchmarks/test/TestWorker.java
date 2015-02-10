package com.oltpbenchmark.benchmarks.voter;

import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.test.procedures.Move;
import com.oltpbenchmark.types.TransactionStatus;

public class TestWorker extends Worker {


    public TestWorker(TestBenchmark benchmarkModule, int id) {
        super(benchmarkModule, id);
    }

    @Override
    protected TransactionStatus executeWork(TransactionType txnType) throws UserAbortException, SQLException {
        assert (txnType.getProcedureClass().equals(Move.class));
        Move proc = getProcedure(Move.class);
        assert (proc != null);
        Random rng = new Random();
        int ship_id = rng.nextInt(TestConstants.NUM_SHIPS) + 1;
        int move_x = rng.nextInt(TestConstants.MAX_MOVE * 2) - TestConstants.MAX_MOVE;
        int move_y = rng.nextInt(TestConstants.MAX_MOVE * 2) - TestConstants.MAX_MOVE;
        proc.run(conn, ship_id, move_x, move_y);
        conn.commit();
        return TransactionStatus.SUCCESS;
    }

}
