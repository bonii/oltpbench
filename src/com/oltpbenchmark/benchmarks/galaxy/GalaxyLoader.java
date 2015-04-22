package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.types.DatabaseType;
//import com.oltpbenchmark.util.Pair;
import com.oltpbenchmark.util.SQLUtil;
import com.oltpbenchmark.util.Triple;

/**
 * A class that prepares the database for the benchmark
 */
public class GalaxyLoader extends Loader {

    /**
     * Creates a new instance of the GalaxyLoader class
     * @param benchmark The benchmark class the loader is called from
     * @param conn The connection to the database to be prepared
     */
    public GalaxyLoader(GalaxyBenchmark benchmark, Connection conn) {
        super(benchmark, conn);
    }

    @Override
    public void load() throws SQLException {
        // Fit constants to scale
        int scale = (int) Math.round(
                benchmark.getWorkloadConfiguration().getScaleFactor());
        int numClasses = GalaxyConstants.NUM_CLASSES * scale;
        int numSolarSystems = GalaxyConstants.NUM_SOLAR_SYSTEMS * scale;
        int numFitting = GalaxyConstants.NUM_FITTING * scale;
        int numShips = GalaxyConstants.NUM_SHIPS * scale;
        
        // Random generator
        Random rng = new Random();
        
        // Database specific settings
        boolean escapeNames = false;
        if (this.getDatabaseType() == DatabaseType.POSTGRES) {
            escapeNames = false;
        }
        if (this.getDatabaseType() == DatabaseType.SQLITE) {
            escapeNames = true;
        }

        // Fill classes table
        Table tbl = getTableCatalog(GalaxyConstants.TABLENAME_CLASSES);
        PreparedStatement ps = this.conn.prepareStatement(
                SQLUtil.getInsertSQL(tbl, escapeNames));
        int[] classHealths = new int[numClasses];
        int[] classFittings = new int[numClasses];        
        for (int i = 0; i < numClasses; i++) {
            // Generate values
            classHealths[i] = GalaxyUtil.randInt(
                    GalaxyConstants.MIN_HEALTH, 
                    GalaxyConstants.MAX_HEALTH, rng);
            classFittings[i] = GalaxyUtil.randInt(
                    GalaxyConstants.MIN_FITTINGS, 
                    GalaxyConstants.MAX_FITTINGS, rng);
            int reachability = GalaxyUtil.randInt(
                    GalaxyConstants.MIN_REACHABILITY, 
                    GalaxyConstants.MAX_REACHABILITY, rng);
            
            // Set values
            ps.setInt(1, i + 1); // Class ID (cid)
            ps.setString(2, GalaxyConstants.classes[0]); // Class name
            ps.setInt(3, reachability);
            ps.setInt(4, classHealths[i]);
            ps.setInt(5, classFittings[i]);
            ps.addBatch();
        }
        ps.executeBatch();

        // Fill solar_systems table
        tbl = getTableCatalog(GalaxyConstants.TABLENAME_SOLARSYSTEMS);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, escapeNames));
        ArrayList<Triple<Long, Long, Long>> systemMax = 
                new ArrayList<Triple<Long, Long, Long>>();
        for (int i = 0; i < numSolarSystems; i++) {
            // Generate values
            long xMax = GalaxyUtil.randLong(
                    GalaxyConstants.MIN_SYSTEM_SIZE, 
                    GalaxyConstants.MAX_SYSTEM_SIZE, rng);
            long yMax = GalaxyUtil.randLong(
                    GalaxyConstants.MIN_SYSTEM_SIZE, 
                    GalaxyConstants.MAX_SYSTEM_SIZE, rng);
            long zMax = GalaxyUtil.randLong(
                    GalaxyConstants.MIN_SYSTEM_SIZE, 
                    GalaxyConstants.MAX_SYSTEM_SIZE, rng);
            int security = GalaxyUtil.randInt(
                    GalaxyConstants.MIN_SECURITY, 
                    GalaxyConstants.MAX_SECURITY, rng);
            systemMax.add(new Triple<Long, Long, Long>(xMax, yMax, zMax));
            
            ps.setInt(1, i + 1); // Solarsystem ID(ssid)
            ps.setLong(2, xMax); 
            ps.setLong(3, yMax);
            ps.setLong(4, zMax);
            ps.setInt(5, security); // Security level
            ps.addBatch();
        }
        ps.executeBatch();
        
        // Fill fitting table
        tbl = getTableCatalog(GalaxyConstants.TABLENAME_FITTING);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, escapeNames));
        for (int i = 0; i < numFitting; i++) {
            ps.setInt(1, i+1); // Fitting id
            ps.setInt(2, rng.nextInt(GalaxyConstants.NUM_FITTING_TYPES)); 
            ps.setInt(3, rng.nextInt(GalaxyConstants.MAX_FITTING_VALUE) + 1);
            ps.addBatch();
        }
        ps.executeBatch();
        
        // Fill ships and fittings tables
        tbl = getTableCatalog(GalaxyConstants.TABLENAME_SHIPS);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, escapeNames));
        tbl = getTableCatalog(GalaxyConstants.TABLENAME_FITTINGS);
        PreparedStatement ps2 = this.conn.prepareStatement(
                SQLUtil.getInsertSQL(tbl, escapeNames));
        int fittingsId = 0; // TODO fittings id hack
        for  (int i = 0; i < numShips; i++) {
            int solarSystemId = rng.nextInt(numSolarSystems);
            long positionX = GalaxyUtil.nextLong(rng, systemMax.get(solarSystemId).left);
            long positionY = GalaxyUtil.nextLong(rng, systemMax.get(solarSystemId).middle);
            long positionZ = GalaxyUtil.nextLong(rng, systemMax.get(solarSystemId).right);
            int classId = rng.nextInt(numClasses);

            ps.setInt(1, i + 1);  // Ship ID(sid)
            ps.setLong(2, positionX);
            ps.setLong(3, positionY);
            ps.setLong(4, positionZ);
            ps.setInt(5, classId + 1);
            ps.setInt(6, solarSystemId + 1);
            ps.setInt(7, classHealths[classId]);
            ps.addBatch();
            
            int numFittings = GalaxyUtil.randInt(1, classFittings[classId], rng);
            for (int j = 0; j < numFittings; j++) {
                ps2.setInt(1, fittingsId++);
                ps2.setInt(2, i+1); // Ship id
                ps2.setInt(3, rng.nextInt(numFitting) + 1); // Fitting id
                ps2.addBatch();
            }
        }
        ps.executeBatch();
        ps2.executeBatch();
    }

}