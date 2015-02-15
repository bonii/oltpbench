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

public class GalaxyBenchmark extends BenchmarkModule {


    public GalaxyBenchmark(WorkloadConfiguration workConf) {
        super("galaxy", workConf, true);
    }

    @Override
    protected List<Worker> makeWorkersImpl(boolean verbose) throws IOException {
        List<Worker> workers = new ArrayList<Worker>();
        for (int i = 0; i < workConf.getTerminals(); ++i) {
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
