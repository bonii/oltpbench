package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.SQLException;
import java.util.Random;
import java.util.ArrayList;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.*;
import com.oltpbenchmark.benchmarks.galaxy.util.ActivityRegion;
import com.oltpbenchmark.types.TransactionStatus;

import org.apache.log4j.Logger;

/**
 * A class, which handles the work a worker needs to do
 */
public class GalaxyWorker extends Worker {

    private static final Logger LOG = Logger.getLogger(GalaxyWorker.class);

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
    }

    @Override
    protected TransactionStatus executeWork(TransactionType txnType) throws UserAbortException, SQLException {
        long returncode;
        try {
            GalaxyProcedure proc = (GalaxyProcedure) this.getProcedure(txnType.getProcedureClass());
            assert(proc != null);
            ActivityRegion region = getActivityRegion(txnType.getName());
            returncode = proc.run(conn, region.solarSystemId, region.minPos, region.maxPos, new Random());
        } catch (ClassCastException ex) {
            LOG.error("Given invalid transaction type", ex);
            conn.rollback();
            return TransactionStatus.RETRY_DIFFERENT;
        }
        conn.commit();
        if (returncode != 0)
    	    return TransactionStatus.USER_ABORTED;
        else
            return TransactionStatus.SUCCESS;
    }

    private ActivityRegion getActivityRegion(String transactionName) {
        return regions.get(this.rng.nextInt(regions.size()));
    }

}
