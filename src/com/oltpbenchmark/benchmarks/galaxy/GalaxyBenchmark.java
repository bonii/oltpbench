package com.oltpbenchmark.benchmarks.galaxy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import com.oltpbenchmark.benchmarks.galaxy.util.ActivityRegion;
import com.oltpbenchmark.benchmarks.galaxy.util.SolarSystem;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.log4j.Logger;

/**
 * A class, which handles the workers, the loader and the config
 */
public class GalaxyBenchmark extends BenchmarkModule {

    private static final Logger LOG = Logger.getLogger(GalaxyBenchmark.class);

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

        ArrayList<ActivityRegion> regions = new ArrayList<ActivityRegion>(); // init because of try....
        try {
            regions = generateActivityRegions(numWorkers);
        } catch (SQLException e) {
            LOG.error("Unexpected error when starting benchmark: ", e);
        }
        for (ActivityRegion region : regions) {
            LOG.info(region.toString());
        }

        for (int i = 0; i < numWorkers; ++i) {
            workers.add(new GalaxyWorker(this, i, regions));
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

    private final String querySolarSystems= "SELECT * FROM " +
        GalaxyConstants.TABLENAME_SOLARSYSTEMS + ";"
        ;
    
    private final String queryShips = "SELECT COUNT(*) FROM " +
        GalaxyConstants.TABLENAME_SHIPS + " WHERE " +
        "position_x BETWEEN ? AND ? AND " +
        "position_y BETWEEN ? AND ? AND " +
        "position_z BETWEEN ? AND ? AND " +
        "solar_system_id = ?;";


    /**
     * Generates ActivityRegions for the benchmark
     * @param numWorkers number of workers in use
     * @return an ArrayList of ActivityRegions.
     * @throws SQLException
     */
    private ArrayList<ActivityRegion> generateActivityRegions(int numWorkers)
            throws SQLException {
        Connection conn = getLastConnection();
        if (conn == null) conn = makeConnection();
        PreparedStatement ps = conn.prepareStatement(querySolarSystems);
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
        int regionsPerSolarSystem = Math.max(10, 20 * numWorkers / solarSystems.size());
        for (SolarSystem solar : solarSystems) {
            regions.addAll(getSolarRegions(regionsPerSolarSystem, conn, solar));
        }
        return regions;
    }

    /**
     * Generates ActivityRegions for a single solarsystem.
     * @param numRegions number of regions for the solar system
     * @param solar the SolarSystem object for the solar system
     */
    private ArrayList<ActivityRegion> getSolarRegions(int numRegions, Connection conn, SolarSystem solar) 
            throws SQLException {
        ArrayList<ActivityRegion> regions = new ArrayList<ActivityRegion>();
        double xMax = solar.xMax.floatValue();
        double yMax = solar.yMax.floatValue();
        double zMax = solar.zMax.floatValue();
        double solarVolume = xMax * yMax * zMax;
        Long size = (long) (Math.cbrt(solarVolume / numRegions)*1.2);
        Long sizeX = size;
        Long sizeY = size;
        Long sizeZ = size;
        Random rng = new Random();
        for (int i = 0; i < numRegions; i++) {
            Long xPos = GalaxyUtil.nextLong(rng, solar.xMax - sizeX);
            Long yPos = GalaxyUtil.nextLong(rng, solar.yMax - sizeY);
            Long zPos = GalaxyUtil.nextLong(rng, solar.zMax - sizeZ);

            ImmutableTriple<Long, Long, Long> minPos =
                new ImmutableTriple<Long, Long, Long> (xPos, yPos, zPos);
            ImmutableTriple<Long, Long, Long> maxPos =
                new ImmutableTriple<Long, Long, Long> (xPos + sizeX, yPos + sizeY, zPos + sizeZ);

            int count = 0;
            PreparedStatement ps = conn.prepareStatement(queryShips);
            ps.setLong(1, minPos.left);
            ps.setLong(2, maxPos.left);
            ps.setLong(3, minPos.middle);
            ps.setLong(4, maxPos.middle);
            ps.setLong(5, minPos.right);
            ps.setLong(6, maxPos.right);
            ps.setInt(7, solar.solarSystemId);
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next())
                    count = rs.getInt(1);
                else
                    throw new SQLException();
            } finally {
                rs.close();
            }
            if (count <= 1)
                i--;
            else
                regions.add(new ActivityRegion(solar.solarSystemId, minPos, maxPos));
        }

        return regions;
    }

}
