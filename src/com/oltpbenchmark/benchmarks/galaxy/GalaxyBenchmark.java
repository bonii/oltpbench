package com.oltpbenchmark.benchmarks.galaxy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;

/**
 * A class, which handles the workers, the loader and the config
 */
public class GalaxyBenchmark extends BenchmarkModule {

    /**
     * Creates a new instance of the GalaxyBenchmark class
     * @param workConf The configuration the benchmark will run with
     */
    public GalaxyBenchmark(WorkloadConfiguration workConf) {
        super("galaxy", workConf, true);
    }

    @Override
    protected List<Worker> makeWorkersImpl(boolean verbose) throws IOException {
        List<Worker> workers = new ArrayList<Worker>();
        int numWorkers = workConf.getTerminals();

        for (int i = 0; i < numWorkers; ++i) {
            workers.add(new GalaxyWorker(this, i));
        }
        return workers;
    }

    @Override
    protected Loader makeLoaderImpl(Connection conn) throws SQLException {
        return new GalaxyLoader(this, conn);
    }

    @Override
    protected Package getProcedurePackageImpl() {
       return Move.class.getPackage();
    }

}
