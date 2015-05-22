package com.oltpbenchmark.benchmarks.galaxy.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Combat;

import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * A class that checks the correctness of the Move procedure
 */
public class TestCombat extends TestCase {
    
    private Connection conn;
    private Combat combatProc; //dummy change to Combat
    private Random rng;
    
    private int offensive_fitting = GalaxyConstants.FITTING_TYPE_OFFENSIVE;
    private int defensive_fitting = GalaxyConstants.FITTING_TYPE_DEFENSIVE;

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
    
    public final String getShipCount = "SELECT COUNT(*) FROM " + ships + " WHERE solar_system_id = 0;";
    public final String getShipHealth = "SELECT health_points FROM " + ships + 
            " WHERE ship_id = ?;";
    
    
    

    /*
     * TODO create different tests
     * 1on1 fighting
     * 3on4 fighting
     * not enough ships (0 or 1) in solar system
     * fight with only def fittings
     * fight with only atk fittings
     * test with dying ship (hp -> 0)
     */
    

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
    
    private int combatDefined(int solarsystem, Long shipsize, int expected) throws SQLException {
        Long zerolong = new Long(0);
        ImmutableTriple<Long, Long, Long> startpos = new ImmutableTriple<Long, Long, Long>(zerolong,zerolong,zerolong);
        ImmutableTriple<Long, Long, Long> endpos = new ImmutableTriple<Long, Long, Long>(shipsize,shipsize,shipsize);
        assertEquals("Combat gave unexpected result", expected,
                combatProc.run(this.conn, solarsystem, startpos, endpos, rng)); //TODO change 0 here HERE!
        return expected;
    }
    
    /**
     * Gets the health points from all ships in shipid array
     * @throws SQLException
     */
    private int[] getHealth(int[] shipid, int[] hp) throws SQLException {
        PreparedStatement ps = null;
        ResultSet tmp = null;
        int[] health = new int[shipid.length];
        for (int i = 0; i < shipid.length; i++) {
            if (hp[i] > 0) {
                ps = conn.prepareStatement(getShipHealth);
                ps.setInt(1, shipid[i]);
                tmp = ps.executeQuery();
                try {
                    assertTrue("Query should return something", tmp.next());
                    health[i] = tmp.getInt(1);
                } finally {
                    tmp.close();
                }
            } else {
                health[i] = hp[i]; //only for dying ships
            }
        }
        return health;
    }
    
    private fightvalues getFightValues(int[] shipid, int[] fitsship, int[] fitsfitid,
            int[] fitid, int[] fitval, int[] fittype ) {
        int caldmg1 = 0;
        int caldmg2 = 0;
        int shipsize = shipid.length;
        int[] def = new int[shipsize];

        for (int i = 0; i < def.length; i++) {
            def[i] = 0;
        }
        for (int i = 0; i < shipsize; i++) {
            for (int k = 0; k < fitsship.length; k++) {
                if (fitsship[k] == shipid[i]) {
                    for (int j = 0; j < fitid.length; j++) {
                        if (fitid[j] == fitsfitid[k]) {
                            if (fittype[j] == offensive_fitting) {
                                if (i % 2 == 0) {
                                    caldmg1 += fitval[j];
                                } else {
                                    caldmg2 += fitval[j];
                                }
                            } else {
                                def[i] += fitval[j]; 
                            }
                        }
                    }
                }
            }            
        }
        fightvalues tmp = new fightvalues(def, caldmg1, caldmg2);
        return tmp;
    }
    
    
    /**
     * Tests simple one ship vs one ship combat
     * @throws SQLException
     */
    public void testCombat(int combatants, int[] fit_type, int[] fit_value, int expected)  throws SQLException {
        int shipsize = combatants;
        int fitsize = fit_type.length;
        int fitssize = fit_value.length;
        
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
        }
        fittype = fit_type;
        fitval = fit_value;
        if (fitsize != 0) {
            for (int i = 0; i < fitssize; i++) {
                fitsid[i] = 0 - i;
                fitsship[i] = 0 - (i % shipsize);
                fitsfitid[i] = 0 - (i % fitsize);
            }
        }
        createTestValues(base, shipid, x, y, z, hp, fitid, fittype, fitval, fitsid, fitsship, fitsfitid);
        
        int result = combatDefined(0, new Long(shipsize), expected);
        if (result == 1) { //got expected failure, ends test and continues other
            removeTestValues(shipid, fitid, fitsid);
            return;
        }
        
        fightvalues calvals = getFightValues(shipid, fitsship, fitsfitid,
             fitid, fitval, fittype);
        int caldmg1 = calvals.getdmg1();
        int caldmg2 = calvals.getdmg2();
        int[] caldef = calvals.getdef();
        int groupsize = shipsize/2;
        caldmg1 = (int) (caldmg1 / (groupsize + (shipsize % 2)));
        caldmg2 = (int) (caldmg2 / groupsize);
        int remainingShips = shipsize;
        for (int i = 0; i < shipsize; i++) {
            if (i % 2 == 0) {
                hp[i] -= (Math.max(0, caldmg2 - caldef[i]) );
                if (hp[i] <= 0) remainingShips--;
            } else {
                hp[i] -= (Math.max(0, caldmg1 - caldef[i]));
                if (hp[i] <= 0) remainingShips--;
            }
        }
        //check for correct number of ships
        assertEquals("Incorrect number of remaining ships a combat with " + shipsize +
                " combatants", 0, shipCount(remainingShips) );

        
        int[] posthp = getHealth(shipid, hp);
        for (int i = 0; i < shipsize; i++) {
            assertTrue("Ship " + shipid[i] + " have incorrect hp after a combat with " + shipsize +
                    " combatants",
                    posthp[i] == hp[i]);
        }
        removeTestValues(shipid, fitid, fitsid);
    }
    
    
    public int shipCount(int remainingShips) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getShipCount);
        ResultSet rs = ps.executeQuery();
        try {
            if (rs.next()) {
                int remains = rs.getInt(1);
                if (remains == remainingShips) {
                    return 0;
                } else {
                    return 1;
                }
            }
        } catch (Exception e) {
            rs.close();
        }
        return 1;
    }

    /**
     * Sets the connection and procedure variables, and runs all the tests
     * @param conn The connection to the database
     * @param combatProc The Combat procedure
     * @throws SQLException
     */
    @Test
    public void run(Connection conn, Combat proc, Random rng) throws SQLException {
        this.conn = conn;
        this.combatProc = proc;
        this.rng = rng;
        int[] fittype = new int[2];
        int[] fitvalue = new int[4];
        fittype[0] = offensive_fitting;
        fittype[1] = defensive_fitting;
        fitvalue[0] = 30;
        fitvalue[1] = 10;
        fitvalue[2] = 40;
        fitvalue[3] = 10;
        // 2 combatants
        testCombat(2, fittype, fitvalue, 0);
        // large and uneven # of combatants
        testCombat(7, fittype, fitvalue, 0);
        // large # of combatants
        testCombat(8, fittype, fitvalue, 0);
        // too small, expect error
        testCombat(1, fittype, fitvalue, 1);

        //attack only
        fittype[0] = offensive_fitting;
        fittype[1] = offensive_fitting;
        testCombat(2, fittype, fitvalue, 0);
        //defense only
        fittype[0] = defensive_fitting;
        fittype[1] = defensive_fitting;
        testCombat(2, fittype, fitvalue, 0);
        
        //dying ships
        fitvalue[0] = 100;
        fitvalue[1] = 0;
        fitvalue[2] = 100;
        fitvalue[3] = 0;
        fittype[0] = offensive_fitting;
        fittype[1] = defensive_fitting;
        testCombat(2, fittype, fitvalue, 0);


    }
    class fightvalues {
        int[]def;
        int dmg1;
        int dmg2;
        
        fightvalues(int[] caldef, int caldmg1, int caldmg2) {
            this.def = caldef;
            this.dmg1 = caldmg1;
            this.dmg2 = caldmg2;
        }
        
        public int getdmg1() {
            return dmg1;
        }
        
        public int getdmg2() {
            return dmg2;
        }
        
        public int[] getdef() {
            return def;
        }
        
    }
    

}