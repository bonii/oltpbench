package com.oltpbenchmark.benchmarks.galaxy.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Idle;

import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * A class that checks the correctness of the Move procedure
 */
public class TestIdle extends TestCase {
    
    private Connection conn;
    private Idle idleProc; //dummy change to Combat

    private String ships = GalaxyConstants.TABLENAME_SHIPS;
    private String classes = GalaxyConstants.TABLENAME_CLASSES;
    private String solarsystems = GalaxyConstants.TABLENAME_SOLARSYSTEMS;
    private String fitting = GalaxyConstants.TABLENAME_FITTING;
    private String fittings = GalaxyConstants.TABLENAME_FITTINGS;

    public final String createTmpClass = "INSERT INTO " + classes +
            " VALUES (0, 'Test Cruiser', 1000, ?, 2);";
    public final String createTmpShip = "INSERT INTO " + ships + 
            " VALUES (?, ?, ?, ?, 0, 0, ?);";
    public final String createTmpSystem = "INSERT INTO " + solarsystems +
            " VALUES (0, 1000, 1000, 1000, 1);";
    public final String createTmpFitting = "INSERT INTO " + fitting +
            " VALUES (?, ?, ?);";
    public final String createTmpFittings = "INSERT INTO " + fittings +
            " VALUES (?, ?, ?);";
    
    public final String deleteTmpClass = "DELETE FROM " + classes +
            " WHERE class_id = 0;";
    public final String deleteTmpShip = "DELETE FROM " + ships +
            " WHERE ship_id = ?;";
    public final String deleteTmpSystem = "DELETE FROM " + solarsystems +
            " WHERE solar_system_id = 0;";
    public final String deleteTmpFitting = "DELETE FROM " + fitting +
            " WHERE fitting_id = ?;";
    public final String deleteTmpFittings = "DELETE FROM " + fittings +
            " WHERE fittings_id = ?;";
   
    
    public final String getShips = "SELECT ship_id, position_x, position_y, " +
            "position_z, class_id, health_points FROM " + ships +
            " WHERE solar_system_id = 0;"; //ADD join locations once implemented

    
    

    /**
     * Fills the database with known test values
     * @throws SQLException
     */
    private void createTestValues(int base, int[] shipid, int[] x, int[] y, int[] z, int[] hp, int[] fitid,
            int[] fittype, int[] fitval, int[] fitsid, int[] fitsship, int[] fitsfitid ) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(createTmpClass);
        ps.setInt(1, base);
        ps.execute();
        ps = conn.prepareStatement(createTmpSystem);
        ps.execute();
        ps = conn.prepareStatement(createTmpShip);
        for (int i = 0; i < shipid.length; i++ ) {
            ps.setInt(1, shipid[i]);
            ps.setInt(2, x[i]);
            ps.setInt(3, y[i]);
            ps.setInt(4, z[i]);
            ps.setInt(5, hp[i]);
            ps.addBatch();
        }
        ps.executeBatch();
        ps = conn.prepareStatement(createTmpFitting);
        for (int i = 0; i < fitid.length; i++ ) {
            ps.setInt(1, fitid[i]);
            ps.setInt(2, fittype[i]);
            ps.setInt(3, fitval[i]);
            ps.addBatch();
        }
        ps.executeBatch();
        ps = conn.prepareStatement(createTmpFittings);
        for (int i = 0; i < fitsid.length; i++ ) {
            ps.setInt(1, fitsid[i]);
            ps.setInt(2, fitsship[i]);
            ps.setInt(3, fitsfitid[i]);
            ps.addBatch();
        }
        ps.executeBatch();
    }
    
    private void removeTestValues(int[] shipid, int[] fitid, int[] fitsid) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(deleteTmpFittings);
        for (int i = 0; i < fitsid.length; i++) {
            ps.setInt(1, fitsid[i]);
            ps.addBatch();
        }
        ps.executeBatch();
        ps = conn.prepareStatement(deleteTmpFitting);
        for (int i = 0; i < fitid.length; i++) {
            ps.setInt(1, fitid[i]);
            ps.addBatch();
        }
        ps.executeBatch();
        ps = conn.prepareStatement(deleteTmpShip);
        for (int i = 0; i < shipid.length; i++) {
            ps.setInt(1, shipid[i]);
            ps.addBatch();
        }
        ps.executeBatch();
        ps = conn.prepareStatement(deleteTmpSystem);
        ps.execute();
        ps = conn.prepareStatement(deleteTmpClass);
        ps.execute();
    }
    
    private void idleDefined(int solarsystem, Long shipsize) throws SQLException {
        Long zerolong = new Long(0);
        ImmutableTriple<Long, Long, Long> startpos = new ImmutableTriple<Long, Long, Long>(zerolong,zerolong,zerolong);
        ImmutableTriple<Long, Long, Long> endpos = new ImmutableTriple<Long, Long, Long>(shipsize,shipsize,shipsize);
        assertEquals("Idle gave unexpected result", 0,
                idleProc.run(this.conn, solarsystem, startpos, endpos));
    }
      
    
    /**
     * Tests simple one ship vs one ship combat
     * @throws SQLException
     */
    public void testIdle(int ships)  throws SQLException {
        int shipsize = ships;
        int fitsize = 4;
        int fitssize = shipsize*2;
        
        int base = 100;
        int[] shipid = new int[shipsize];
        int[] x = new int[shipsize];
        int[] y = new int[shipsize];
        int[] z = new int[shipsize];
        int[] hp = new int[shipsize];
        int[] fitid = new int[fitsize];       
        int[] fittype = new int[fitsize];
        int[] fitval = new int[fitsize];
        int[] fitsid = new int[fitssize];
        int[] fitsship = new int[fitssize];
        int[] fitsfitid = new int[fitssize];  
        for (int i = 0; i < shipsize; i++) {
            shipid[i] = 0 - i;
            x[i] = i;
            y[i] = i;
            z[i] = i;
            hp[i] = 100;
        }
        for (int i = 0; i < fitsize; i++) {
            fitid[i] = 0 - i;
            fittype[i] = i % 2; //0 or 1
            fitval[i] = 50;
        }
        if (fitsize != 0) {
            for (int i = 0; i < fitssize; i++) {
                fitsid[i] = 0 - i;
                fitsship[i] = 0 - (i % shipsize);
                fitsfitid[i] = 0 - (i % fitsize);
            }
        }
        createTestValues(base, shipid, x, y, z, hp, fitid, fittype, fitval, fitsid, fitsship, fitsfitid);
        
        idleDefined(0, new Long(shipsize));
        
        int registeredShips = 0;

        PreparedStatement ps = conn.prepareStatement(getShips);
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()) {
                int ship_id = rs.getInt(1);
                for (int i = 0; i < shipsize; i++) {
                    if (shipid[i] == ship_id) {
                        registeredShips++;
                        assertEquals("Incorrect position x after idle with " + shipsize +
                                " ships",x[i] , rs.getLong(2) );
                        assertEquals("Incorrect position x after idle with " + shipsize +
                                " ships",y[i] , rs.getLong(3) );
                        assertEquals("Incorrect position x after idle with " + shipsize +
                                " ships",z[i] , rs.getLong(4) );
                        assertEquals("Incorrect class after idle with " + shipsize +
                                " ships",0 , rs.getInt(5) );
                        assertEquals("Incorrect health points after idle with " + shipsize +
                                " ships",hp[i] , rs.getInt(6) );
                        break;
                    }
                }

            }
        } catch (Exception e) {
            removeTestValues(shipid, fitid, fitsid);
            assertEquals("No data from databse after idle with " + shipsize +
                    " ships",1 , 0 );
            return;
        }
        assertEquals("Incorrect number of ships after idle with " + shipsize +
                " ships",shipsize , registeredShips );
        
        removeTestValues(shipid, fitid, fitsid);
    }
    

    /**
     * Sets the connection and procedure variables, and runs all the tests
     * @param conn The connection to the database
     * @param combatProc The Combat procedure
     * @throws SQLException
     */
    @Test
    public void run(Connection conn, Idle proc, Random rng) throws SQLException {
        this.conn = conn;
        this.idleProc = proc;

        // 1 ship
        testIdle(1);
        // 2 ships
        testIdle(2);
        // large and uneven # of ships
        testIdle(7);
        // large # of ships
        testIdle(8);

    }

}