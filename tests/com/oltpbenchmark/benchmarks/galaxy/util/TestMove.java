package com.oltpbenchmark.benchmarks.galaxy.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;

/**
 * A class that checks the correctness of the Move procedure
 */
public class TestMove extends TestCase {
    
    private Connection conn;
    private Move proc;
    private Random rng;

    private String ships = GalaxyConstants.TABLENAME_SHIPS;
    private String classes = GalaxyConstants.TABLENAME_CLASSES;
    private String solarsystems = GalaxyConstants.TABLENAME_SOLARSYSTEMS;
    
    public final String getShips = "SELECT ship_id, position_x, " +
            "position_y, position_z, reachability FROM " + ships +
            " JOIN " + classes + " ON " + ships + ".class_id = " +
            classes + ".class_id WHERE solar_system_id = ? ORDER BY ship_id ASC;";
    public final String getSolarSystem = "SELECT * FROM " + solarsystems + 
            " WHERE solar_system_id = ?;";
    public final String findNumberOfShips = "SELECT COUNT(*) FROM " + ships + ";";
    public final String findShipsInSamePosition = "SELECT position_x, position_y, position_z ssid FROM " +
            ships + " GROUP BY position_x, position_y, position_z, solar_system_id HAVING COUNT(*) > 1;";
    public final String findShipsNotInSolarSystem = "SELECT * FROM " + ships + 
            " WHERE position_x NOT BETWEEN 0 AND ? AND " +
            "position_y NOT BETWEEN 0 AND ? AND " +
            "position_z NOT BETWEEN 0 AND ? AND " +
            "solar_system_id = ?;";
    
    private ArrayList<Ship> getShips(int id) throws SQLException {
        PreparedStatement ps = this.conn.prepareStatement(getShips);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        ArrayList<Ship> ships = new ArrayList<Ship>();
        try {
            while (rs.next()) {
                Ship ship = new Ship(rs.getInt(1));
                ship.position = new ImmutableTriple<Long, Long, Long>(
                        rs.getLong(2), rs.getLong(3), rs.getLong(4)
                        );
                ship.reachability = rs.getLong(5);
                ships.add(ship);
            }
        } finally {
            rs.close();
        }
        assertTrue("There should be at least one ship", ships.size() > 0);
        return ships;
    }
    
    private SolarSystem getSolarSystemInformation(int id) throws SQLException {
        PreparedStatement ps = this.conn.prepareStatement(getSolarSystem);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        SolarSystem solar;
        try {
            assertTrue("There should be a solar system with id " + id, rs.next());
            solar = new SolarSystem(rs.getInt(1), rs.getInt(5),  // id, security 
                    rs.getLong(2), rs.getLong(3), rs.getLong(4)); // x, y, z
        } finally {
            rs.close();
        }
        return solar;
    }
    
    private void noShipsInSamePosition() throws SQLException{
        PreparedStatement ps = this.conn.prepareStatement(findShipsInSamePosition);
        ResultSet rs = ps.executeQuery();
        try {
            assertFalse("There should not be two ships in the same position", rs.next());
        } finally {
            rs.close();
        }
    }
    
    private void noShipsDisappeared() throws SQLException {
        PreparedStatement ps = this.conn.prepareStatement(findNumberOfShips);
        ResultSet rs = ps.executeQuery();
        try {
            assertTrue("Query should return something", rs.next());
            assertTrue("There should be the same number of ships, as before", 
                    rs.getInt(1) == GalaxyConstants.NUM_SHIPS);
        } finally {
            rs.close();
        }
    }
    
    private void noShipsMovedMoreThanReachability(
            ArrayList<Ship> before, ArrayList<Ship> after) {
        for (int i = 0; i < before.size(); i++) {
            Ship bef = before.get(i);
            Ship aft = after.get(i);
            assertTrue("Ship position x should be within reachability", 
                    Math.abs(bef.position.left - aft.position.left) < bef.reachability);
            assertTrue("Ship position y should be within reachability", 
                    Math.abs(bef.position.middle - aft.position.middle) < bef.reachability);
            assertTrue("Ship position z should be within reachability", 
                    Math.abs(bef.position.right - aft.position.right) < bef.reachability);
        }
    }
    
    private void noShipsMovedOutOfSolarSystem(SolarSystem solar) throws SQLException {
        PreparedStatement ps = this.conn.prepareStatement(findShipsNotInSolarSystem);
        ps.setLong(1, solar.xMax);
        ps.setLong(2, solar.yMax);
        ps.setLong(3, solar.zMax);
        ps.setLong(4, solar.solarSystemId);
        ResultSet rs = ps.executeQuery();
        try {
            assertFalse("There should not be any ships outside solar system", rs.next());
        } finally {
            rs.close();
        }
    }
    
    public void run(Connection conn, Move proc, Random rng) throws SQLException {
        this.conn = conn;
        this.proc = proc;
        this.rng = rng;
        SolarSystem solar = getSolarSystemInformation(1);
        ImmutableTriple<Long, Long, Long> minPos = new ImmutableTriple<Long, Long, Long>(0L,0L,0L);
        ImmutableTriple<Long, Long, Long> maxPos = new ImmutableTriple<Long, Long, Long>(
                solar.xMax, solar.yMax, solar.zMax);
        ArrayList<Ship> before = getShips(1);
        for (int i = 0; i < 100; i++) {
            this.proc.run(conn, 1, minPos, maxPos, this.rng);
            noShipsDisappeared();
            noShipsInSamePosition();
            noShipsMovedOutOfSolarSystem(solar);
            ArrayList<Ship> after = getShips(1);
            noShipsMovedMoreThanReachability(before, after);
            before = after;
        }
    }

}