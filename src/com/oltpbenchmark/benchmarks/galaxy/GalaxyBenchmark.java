package com.oltpbenchmark.benchmarks.galaxy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Math;

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

    private final SQLStmt querySolarSystems= "SELECT * FROM " +
        GalaxyConstants.TABLENAME_SOLARSYSTEMS + ";";


    private ArrayList<ActivityRegion> generateActivityRegions(int numWorkers) {
        PreparedStatement ps = getPreparedStatement(conn, querySolarSystems);
        ResultSet rs = ps.executeQuery();
        ArrayList<SolarSystem> solarSystems = new ArrayList<SolarSystem>();
        ArrayList<ActivityRegion> regions = new ArrayList<ActivityRegion>();

        try {
            while (rs.next()) {
                SolarSystem solar = new SolarSystem(rs.getInt(1), rs.getInt(5), rs.getLong(2),
                        rs.getLong(3), rs.getLong(4));
                solarSystems.add(solar);
            }
        } finally {
            rs.close();
        }
        int regionsPerSolarSystems = 2 * numWorkers / solarSystems.size();
        for (solar : solarSystems) {
            regions.addAll(getSolarRegions(regionsPerSolarSystem, solar);
        }

    }

    private getSolarRegions(int numRegions, SolarSystem solar) {
        ArrayList<ActivityRegion> regions = new ArrayList<ActivityRegion>();
        long sizeX = solar.xMax / Math.max(1, numRegions / 2);
        long sizeY = solar.yMax / Math.max(1, numRegions / 2);
        long sizeZ = solar.zMax / Math.max(1, numRegions / 2);
        Random rng = new Random();
        for (int i = 0; i < numRegions, i++) {
            xPos = rng.nextInt(solar.xMax - sizeX);
            yPos = rng.nextInt(solar.yMax - sizeY);
            zPos = rng.nextInt(solar.zMax - sizeZ);

            ImmutableTriple<long, long, long> minPos =
                new ImmutableTriple<long, long, long> (xPos, yPos, zPos);
            ImmutableTriple<long, long, long> maxPos =
                new ImmutableTriple<long, long, long> (xPos + sizeX, yPos + sizeY, zPos + sizeZ);
            ArrayList<int> probabilityVector = getProbabilityVector(minPos, maxPos, solar.securityLevel);

            regions.add(new ActivityRegion(solar.solarSystemId, minPos, maxPos, probabilityVector));
        }

        return regions;
    }

    private getProbabilityVector(ImmutableTriple<long, long, long> minPos,
            ImmutableTriple<long, long, long> maxPos, int securityLevel) {


}
