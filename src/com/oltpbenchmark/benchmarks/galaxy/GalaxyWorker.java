package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;
import java.util.ArrayList;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Combat;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;
import com.oltpbenchmark.benchmarks.galaxy.util.ActivityRegion;
import com.oltpbenchmark.types.TransactionStatus;


import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * A class, which handles the work a worker needs to do
 */
public class GalaxyWorker extends Worker {
    private int currentRegion;
    private int i;
    private int totalScore;
    private ActivityRegion region;
    private final ArrayList<ActivityRegion> regions;
    private final Random rng;

    /**
     * Creates a new instance of the GalaxyWorker class
     * @param benchmarkModule The benchmark the worker is initialized from
     * @param id The id of the worker
     */
    public GalaxyWorker(GalaxyBenchmark benchmarkModule, int id, ArrayList<ActivityRegion> regions) {
        super(benchmarkModule, id);
        this.rng = new Random();
        this.regions = regions;
        this.i = 0;
        chooseRegion();
    }

    @Override
    protected TransactionStatus executeWork(TransactionType txnType) throws UserAbortException, SQLException {
        if (this.i > 10) {
            chooseRegion();
            this.i = 0;
        }

        Combat proc = getProcedure(Combat.class);
        assert(proc != null);
        proc.run(conn, 6, new ImmutableTriple<Long, Long, Long>(0L, 0L, 0L),
                new ImmutableTriple<Long, Long, Long>(100L * GalaxyConstants.AU, 100L * GalaxyConstants.AU, 100L * GalaxyConstants.AU), rng);
        conn.commit();
        i++;
        return TransactionStatus.SUCCESS;
    }

    private int getTotalScore(ArrayList<Integer> probabilityVector) {
        int total = 0;
        for (Integer p : this.region.probabilityVector) {
            total += p;
        }
        return total;
    }

    private void chooseRegion() {
        int i = this.rng.nextInt(this.regions.size());
        this.region = regions.get(i);
        this.totalScore = getTotalScore();
    }

    private int getProc() {
        int k = rng.nextInt(workConf.getTransTypes().size());
    }
}
