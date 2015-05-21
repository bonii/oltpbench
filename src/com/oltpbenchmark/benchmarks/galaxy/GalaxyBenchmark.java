package com.oltpbenchmark.benchmarks.galaxy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.lang.Math;

import com.oltpbenchmark.DBWorkload;
import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.api.TransactionType;
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
        int regionsPerSolarSystem = 10;
        for (SolarSystem solar : solarSystems) {
            regions.addAll(getSolarRegions(regionsPerSolarSystem, solar));
        }
        return regions;
    }

    /**
     * Generates ActivityRegions for a single solarsystem.
     * @param numRegions number of regions for the solar system
     * @param solar the SolarSystem object for the solar system
     */
    private ArrayList<ActivityRegion> getSolarRegions(int numRegions, SolarSystem solar) {
        ArrayList<ActivityRegion> regions = new ArrayList<ActivityRegion>();
        Long solarVolume = solar.xMax * solar.yMax * solar.zMax;
        Long size = (long) Math.cbrt(solarVolume * 30 / 100);
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
            HashMap<String, Integer> probabilityVector = getProbabilityVector(minPos, maxPos, solar.securityLevel);

            regions.add(new ActivityRegion(solar.solarSystemId, minPos, maxPos, probabilityVector));
        }

        return regions;
    }

    private HashMap<String, Integer> getProbabilityVector(ImmutableTriple<Long, Long, Long> minPos,
            ImmutableTriple<Long, Long, Long> maxPos, int securityLevel) {
    	HashMap<String, Integer> probVec = new HashMap<String, Integer>();
    	for (TransactionType transType : workConf.getTransTypes()) {
    	    if (transType.getName().equals("Combat")) probVec.put("combat", getCombatProb(securityLevel));
    	    else if (transType.getName().equals("Move")) probVec.put("move", 25);
    	    else if (transType.getName().equals("Idle")) probVec.put("idle", getIdleProb(securityLevel));
    	    // String switching only allowed in java 1.7 or newer :/
    		/*switch(transType.getName()) {
    		case "Combat":
    			probVec.put("combat", getCombatProb(securityLevel));
                break;
    		case "Move":
                probVec.put("move", 25);
                break;
            case "Idle":
            	probVec.put("idle", getIdleProb(securityLevel));
                break;
            default:
                break;
    		}*/
    	}
        return probVec;
    }

    private Integer getCombatProb(int securityLevel) {
        if (securityLevel <= 0) {
            return 30;
        } else if (securityLevel > 0 && securityLevel <= 4) {
            return 20;
        } else {
            return 10;
        }
    }

    private Integer getIdleProb(int securityLevel) {
        if (securityLevel <= 0) {
            return 1;
        } else if (securityLevel > 0 && securityLevel <= 4) {
            return 5;
        } else {
            return 10;
        }
    }
}
