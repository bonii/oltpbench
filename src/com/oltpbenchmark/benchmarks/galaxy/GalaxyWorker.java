package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.*;
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
        int k = this.rng.nextInt(this.totalScore);
        HashMap<String, Integer> probVec = this.region.probabilityVector;
        if (k < probVec.get("combat")) {
        	Combat proc = getProcedure(Combat.class);
        	assert(proc != null);
        	proc.run(conn, 6, this.region.minPos, this.region.maxPos, new Random());
        	conn.commit();
        	return TransactionStatus.SUCCESS;
        }
        k -= probVec.get("combat");
        if (k < probVec.get("idle")) {
        	Idle proc = getProcedure(Idle.class);
        	assert(proc != null);
        	proc.run(conn, 6, this.region.minPos, this.region.maxPos);
        	conn.commit();
        	return TransactionStatus.SUCCESS;
        }
        k -= probVec.get("idle");
        if (k < probVec.get("move")) {
        	Move proc = getProcedure(Move.class);
        	assert(proc != null);
        	proc.run(conn, 6, this.region.minPos, this.region.maxPos, new Random());
        	conn.commit();
        	return TransactionStatus.SUCCESS;
        }
        i++;
        return TransactionStatus.RETRY;
    }

    private int getTotalScore() {
        int total = 0;
        HashMap<String, Integer> probVec = this.region.probabilityVector;
        total += probVec.get("combat");
        total += probVec.get("move");
        total += probVec.get("idle");
        return total;
    }

    private void chooseRegion() {
        int i = this.rng.nextInt(this.regions.size());
        this.region = regions.get(i);
        this.totalScore = getTotalScore();
    }


}
