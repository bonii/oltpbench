package com.oltpbenchmark.benchmarks.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.test.procedures.Move;

public class TestBenchmark extends BenchmarkModule {

    public final int numContestants;

    public TestBenchmark(WorkloadConfiguration workConf) {
        super("test", workConf, true);
        numContestants = TestUtil.getScaledNumContestants(workConf.getScaleFactor());
    }

    @Override
    protected List<Worker> makeWorkersImpl(boolean verbose) throws IOException {
        List<Worker> workers = new ArrayList<Worker>();
        for (int i = 0; i < workConf.getTerminals(); ++i) {
            workers.add(new TestWorker(this, i));
        }
        return workers;
    }

    @Override
    protected Loader makeLoaderImpl(Connection conn) throws SQLException {
        return new TestLoader(this, conn);
    }

    @Override
    protected Package getProcedurePackageImpl() {
       return Test.class.getPackage();
    }

}
