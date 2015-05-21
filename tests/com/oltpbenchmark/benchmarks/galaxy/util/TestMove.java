package com.oltpbenchmark.benchmarks.galaxy.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;
import com.oltpbenchmark.benchmarks.galaxy.procedures.Move;

import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * A class that checks the correctness of the Move procedure
 */
public class TestMove extends TestCase {
    
    private Connection conn;
    private Move moveProc;
    private Random rng;
    private int shipID = 0;
    private int moveStepSize = 5;

    private String ships = GalaxyConstants.TABLENAME_SHIPS;
    private String classes = GalaxyConstants.TABLENAME_CLASSES;
    private String solarsystems = GalaxyConstants.TABLENAME_SOLARSYSTEMS;

    public final String createTmpClass = "INSERT INTO " + classes +
            " VALUES (0, ?, 1000, 0, 0);";
    public final String createTmpShip = "INSERT INTO " + ships + 
            " VALUES (?, 0, 0, 0, 0, 0, 0);";
    public final String createTmpSystem = "INSERT INTO " + solarsystems +
            " VALUES (0, 100000, 100000, 100000, 0);";
    public final String deleteTmpClass = "DELETE FROM " + classes +
            " WHERE class_id = 0;";
    public final String deleteTmpShip = "DELETE FROM " + ships +
            " WHERE ship_id = ?;";
    public final String deleteTmpSystem = "DELETE FROM " + solarsystems +
            " WHERE solar_system_id = 0;";
    public final String findShipsInSamePosition = "SELECT position_x, position_y, position_z ssid FROM " +
            ships + " GROUP BY position_x, position_y, position_z, solar_system_id HAVING COUNT(*) > 1;";
    public final String getShipCount = "SELECT COUNT(*) FROM " + ships + ";";
    public final String getShipPosition = "SELECT position_x, position_y, position_z FROM " + ships + 
            " WHERE ship_id = ?;";
    public final String getShipPositionAndReach = 
            "SELECT position_x, position_y, position_z, reachability FROM " + ships + 
            " JOIN " + classes + " ON " + ships + ".class_id = " + 
            classes + ".class_id WHERE ship_id = ?;";
    public final String getSystemMaxAndReachability = 
            "SELECT max_position_x, max_position_y, max_position_z, reachability FROM " + ships + " JOIN " + 
            solarsystems + " ON " + ships + ".solar_system_id = " + solarsystems + 
            ".solar_system_id JOIN " + classes + " ON " + ships + ".class_id = " + classes +
            ".class_id WHERE ship_id = ?;";
    
    public final String getClassReach = 
            "SELECT reachability FROM " + ships + " JOIN " + classes +
            " ON " + ships + ".class_id = " + classes + ".class_id " +
            "WHERE ship_id = ?;";
    public final String getSystemMax = 
            "SELECT max_position_x, max_position_y, max_position_z FROM " +
            ships + " JOIN " + solarsystems + " ON " + ships + ".solar_system_id = " +
            solarsystems + ".solar_system_id WHERE ship_id = ?;";

    /**
     * Tests that a ship will always stay within the borders of the solarsystem
     * @throws SQLException
     */
    public void cannotMoveOutOfSystem() throws SQLException {
        createTestValues();
        ImmutableTriple<Long, Long, Long> max = getSystemMax();
        int reach = getClassReach();

        // Try to move to -1 x and y
        moveDefined(-1, 0, 0);
        ImmutableTriple<Long, Long, Long> position = getPosition();
        assertTrue("Ship x should still be near 0",
                position.left == 0 || position.left == 1);
        moveDefined(0, -1, 0);
        position = getPosition();
        assertTrue("Ship y should still be near 0",
                position.middle == 0 || position.middle == 1);
        moveDefined(0, 0, -1);
        position = getPosition();
        assertTrue("Ship z should still be near 0", 
                position.right == 0 || position.right == 1);

        long iters = (max.left / reach) + 1;
        assertTrue("asdf", iters >= 100);
        for (int i = 0; i < iters; i++) {
            moveDefined(reach, 0, 0);
        }
        position = getPosition();
        assertTrue("Ship x should be near the edge of the system",
                Math.abs(max.left - position.left) <= 1);

        iters = (max.middle / reach) + 1;
        for (int i = 0; i < iters; i++) {
            moveDefined(0, reach, 0);
        }
        position = getPosition();
        assertTrue("Ship y should be near the edge of the system",
                Math.abs(max.middle - position.middle) <= 1);
        
        iters = (max.right / reach) + 1;
        for (int i = 0; i < iters; i++) {
            moveDefined(0, 0, reach);
        }
        position = getPosition();
        assertTrue("Ship z should be near the edge of the system",
                Math.abs(max.right - position.right) <= 1);
        removeTestValues();
    }

    /**
     * Tests that two ships cannot move on top of each other
     * @throws SQLException
     */
    public void cannotMoveOnTopOfOther() throws SQLException {
        createTestValues();
        moveDefined(moveStepSize, moveStepSize, moveStepSize);
        int tmpID = shipID;
        shipID = -1;
        PreparedStatement ps = conn.prepareStatement(createTmpShip);
        ps.setInt(1, shipID);
        ps.execute();
        noShipsInSamePos();
        ps = conn.prepareStatement(deleteTmpShip);
        ps.setInt(1, shipID);
        ps.execute();
        shipID = tmpID;
        removeTestValues();
    }

    /**
     * Fills the database with known test values
     * @throws SQLException
     */
    private void createTestValues() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(createTmpClass);
        ps.setString(1, "Test cruiser");
        ps.execute();
        ps = conn.prepareStatement(createTmpSystem);
        ps.execute();
        ps = conn.prepareStatement(createTmpShip);
        ps.setInt(1, shipID);
        ps.execute();
    }
    
    private int getClassReach() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getClassReach);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        int reach = 0;
        try {
            assertTrue("Query should return something", rs.next());
            reach = rs.getInt(1);
        } finally {
            rs.close();
        }
        return reach;
    }

    /**
     * Gets the position of the ship with id TestMove.shipID
     * @return An integer array holding the position of the ship
     * @throws SQLException
     */
    private ImmutableTriple<Long, Long, Long> getPosition() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getShipPosition);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        ImmutableTriple<Long, Long, Long> position;
        try {
            assertTrue("Query should return something", rs.next());
            position = new ImmutableTriple<Long, Long, Long>(rs.getLong(1), rs.getLong(2), rs.getLong(3));
        } finally {
            rs.close();
        }
        return position;
    }

    /**
     * Gets the position and reachability of the ship with id TestMove.shipID
     * @return An integer array containing the position of the ship and its 
     * reachability
     * @throws SQLException
     */
    private int[] getPositionAndReach() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getShipPositionAndReach);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        int[] positionAndReach = new int[3];
        try {
            assertTrue("Query should return something", rs.next());
            positionAndReach[0] = rs.getInt(1);
            positionAndReach[1] = rs.getInt(2);
            positionAndReach[2] = rs.getInt(3);
        } finally {
            rs.close();
        }
        return positionAndReach;
    }

    /**
     * Returns the reachability of the ship and the max position in the 
     * solarsystem the ship resides in, from the ship with it TestMove.shipID
     * @return An integer array containing the reachability of the ship, and 
     * the maximum position of the solarsystem
     * @throws SQLException
     */
    private int[] getSystemMaxAndReach() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getSystemMaxAndReachability);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        int[] maxAndReach = new int[3];
        try {
            assertTrue("Query should return something", rs.next());
            maxAndReach[0] = rs.getInt(1);
            maxAndReach[1] = rs.getInt(2);
            maxAndReach[2] = rs.getInt(3);
        } finally {
            rs.close();
        }
        return maxAndReach;
    }
    
    // TODO make it return a SolarSystem
    private ImmutableTriple<Long, Long, Long> getSystemMax() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getSystemMax);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        ImmutableTriple<Long, Long, Long> systemMax = null;
        try {
            assertTrue("Query should return something", rs.next());
            systemMax = new ImmutableTriple<Long, Long, Long>(
                    rs.getLong(1),
                    rs.getLong(2),
                    rs.getLong(3)
                    );
        } finally {
            rs.close();
        }
        return systemMax;
    }

    /**
     * Tests the correctness of multiple calls of the Move procedure
     * @throws SQLException
     */
    public void manyMoves() throws SQLException {
        createTestValues();
        int numMoves = 100;
        ImmutableTriple<Long, Long, Long> posBefore = getPosition();
        for (int i = 0; i < numMoves; i++) {
            moveDefined(moveStepSize, moveStepSize, moveStepSize);
        }
        ImmutableTriple<Long, Long, Long> posAfter = getPosition();
        assertTrue("Should have moved " + numMoves + " x positions",
                Math.abs(posBefore.left + (moveStepSize * numMoves)
                        - posAfter.left) <= numMoves);
        assertTrue("Should have moved " + numMoves + " y positions",
                Math.abs(posBefore.middle + (moveStepSize * numMoves)
                        - posAfter.middle) <= numMoves);
        assertTrue("Should have moved " + numMoves + " z positions",
                Math.abs(posBefore.right + (moveStepSize * numMoves)
                        - posAfter.right) <= numMoves);
        removeTestValues();
    }

    /**
     * Calls the Move procedure with given values, and checks that it succeeds
     * @param x The relative x value to be moved
     * @param y The relative y value to be moved
     * @throws SQLException
     */
    private void moveDefined(int x, int y, int z) throws SQLException {
        /* TODO assertEquals("Move should be successfull", 0,
                moveProc.run(this.conn, shipID, x, y, rng));*/
    }

    /**
     * Checks that there are no two ships in the database, that has the same 
     * position and solarsystem id
     * @throws SQLException
     */
    public void noShipsInSamePos() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(findShipsInSamePosition);
        ResultSet rs = ps.executeQuery();
        try {
            assertFalse("Query should not return anything", rs.next());
        } finally {
            rs.close();
        }
    }

    /**
     * Checks that the ship count is correct
     * @throws SQLException
     */
    public void noShipsDisappeared() throws SQLException {
        createTestValues();
        PreparedStatement ps = conn.prepareStatement(getShipCount);
        ResultSet rs = ps.executeQuery();
        try {
            assertTrue("Query should return something", rs.next());
            int shipCount = rs.getInt(1);
            assertEquals("Ship count should not have changed",
                    GalaxyConstants.NUM_SHIPS + 1, shipCount);
        } finally {
            rs.close();
        }
        removeTestValues();
    }

    /**
     * Tests that one call to the Move procedure moves the ship correct
     * @throws SQLException
     */
    public void oneMove() throws SQLException {
        createTestValues();
        ImmutableTriple<Long, Long, Long> position = getPosition();
        long newX = position.left + moveStepSize;
        long newY = position.middle + moveStepSize;
        long newZ = position.right + moveStepSize;
        /* TODO assertEquals("Move should be successfull", Move.MOVE_SUCCESSFUL,
                moveProc.run(this.conn, shipID, moveStepSize, moveStepSize, rng));*/ 
        position = getPosition();
        assertTrue("X should be near new position",
                Math.abs(newX - position.left) <= 1);
        assertTrue("Y should be near new position",
                Math.abs(newY - position.middle) <= 1);
        assertTrue("Z should be near new position",
                Math.abs(newZ - position.right) <= 1);
        removeTestValues();
    }

    /**
     * Removes the known test values from the database
     * @throws SQLException
     */
    private void removeTestValues() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(deleteTmpShip);
        ps.setInt(1, shipID);
        ps.execute();
        ps = conn.prepareStatement(deleteTmpSystem);
        ps.execute();
        ps = conn.prepareStatement(deleteTmpClass);
        ps.execute();
    }

    /**
     * Sets the connection and procedure variables, and runs all the tests
     * @param conn The connection to the database
     * @param moveProc The Move procedure
     * @throws SQLException
     */
    @Test
    public void run(Connection conn, Move moveProc, Random rng) throws SQLException {
        this.conn = conn;
        this.moveProc = moveProc;
        this.rng = rng;
        cannotMoveOutOfSystem();
        cannotMoveOnTopOfOther();
        manyMoves();
        noShipsInSamePos();
        noShipsDisappeared();
        oneMove();
        withinReachability();
    }

    /**
     * Tests that the Move procedure will cap the movement of the ship to 
     * its reachability
     * @throws SQLException
     */
    public void withinReachability() throws SQLException {
        createTestValues();
        ImmutableTriple<Long, Long, Long> positionOld = getPosition();
        int reach = getClassReach();
        moveDefined(reach * 2, reach * 2, reach * 2);
        ImmutableTriple<Long, Long, Long> position = getPosition();
        assertTrue("X should be within reach",
                Math.abs(positionOld.left + reach - position.left) <= 1);
        assertTrue("Y should be within reach",
                Math.abs(positionOld.middle + reach - position.middle) <= 1);
        assertTrue("Z should be within reach",
                Math.abs(positionOld.right + reach - position.right) <= 1);
        removeTestValues();
    }

}