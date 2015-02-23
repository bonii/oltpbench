package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Tests;
import com.oltpbenchmark.types.TransactionStatus;

public class GalaxyWorker extends Worker {


    public GalaxyWorker(GalaxyBenchmark benchmarkModule, int id) {
        super(benchmarkModule, id);
    }

    @Override
    protected TransactionStatus executeWork(TransactionType txnType) throws UserAbortException, SQLException {
        if (txnType.getProcedureClass().equals(Move.class)) {
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
        if (txnType.getProcedureClass().equals(Tests.class)) {
            Tests.conn = conn;
            JUnitCore junit = new JUnitCore();
            Result result = junit.run(Tests.class);
            System.out.println("-----");
            System.out.println("JUnit failures: " + result.getFailureCount());
            System.out.println("JUnit run count: " + result.getRunCount());
            System.out.println("JUnit run time: " + result.getRunTime());
            System.out.println("-----");
            return TransactionStatus.SUCCESS;
        }
        return TransactionStatus.RETRY_DIFFERENT;
    }

}
