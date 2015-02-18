package com.oltpbenchmark.benchmarks.galaxy.procedures;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;

public class Tests extends Procedure {
    
    private Connection conn;
    private int shipID;
    private int moveStepSize;
    
    private void createTestValues() throws SQLException {
        SQLStmt create = new SQLStmt(
                "INSERT INTO " + GalaxyConstants.TABLENAME_SOLARSYSTEMS + 
                " VALUES (0, 100000, 100000); " +
                "INSERT INTO " + GalaxyConstants.TABLENAME_CLASSES + 
                " VALUES (0, ?, 100); " +
                "INSERT INTO " + GalaxyConstants.TABLENAME_SHIPS + 
                " VALUES (0, 0, 0, 0, 0);"
                );
        PreparedStatement ps = getPreparedStatement(conn, create);
        ps.setString(1, "Test cruiser");
        ps.execute();
    }
    
    private int[] getPosition() throws SQLException {
        SQLStmt getPos = new SQLStmt(
                "SELECT x, y FROM " + GalaxyConstants.TABLENAME_SHIPS +
                " WHERE sid = ?;"
                );
        PreparedStatement ps = getPreparedStatement(conn, getPos);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        int[] cords = new int[2];
        try {
            assertTrue("Query should return something", rs.next());
            cords[0] = rs.getInt(1);
            cords[1] = rs.getInt(2);
        } finally {
            rs.close();
        }
        return cords;
    }
    
    private int[] getPositionAndReach() throws SQLException {
        SQLStmt getPos = new SQLStmt(
                "SELECT x, y, reachability FROM " + 
                        GalaxyConstants.TABLENAME_SHIPS + " JOIN " +
                        GalaxyConstants.TABLENAME_CLASSES + " ON " +
                        GalaxyConstants.TABLENAME_SHIPS + ".class = " + 
                        GalaxyConstants.TABLENAME_CLASSES + ".cid " +
                        "WHERE sid = ?;"
                );
        PreparedStatement ps = getPreparedStatement(conn, getPos);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        int[] cords = new int[3];
        try {
            assertTrue("Query should return something", rs.next());
            cords[0] = rs.getInt(1);
            cords[1] = rs.getInt(2);
            cords[2] = rs.getInt(3);
        } finally {
            rs.close();
        }
        return cords;
    }
    
    @Test
    public void manyMoves() throws SQLException {
        int[] posBefore = getPosition();
        for (int i = 0; i < 100; i++) {
            oneMove();
        }
        int[] posAfter = getPosition();
        assertEquals("Should have moved 100 positions", 
                posBefore[0] + (moveStepSize * 100), posAfter[0]);
        assertEquals("Should have moved 100 positions", 
                posBefore[1] + (moveStepSize * 100), posAfter[1]);
    }
    
    @Test
    public void noShipsInSamePos() throws SQLException {
        SQLStmt findDuplicates = new SQLStmt(
                "SELECT x, y, ssid FROM ships " +
                        "GROUP BY x, y, ssid HAVING COUNT(*) > 1;"
                );
        PreparedStatement ps = getPreparedStatement(conn, findDuplicates);
        ResultSet rs = ps.executeQuery();
        try {
            assertFalse("Query should not return anything", rs.next());
        } finally {
            rs.close();
        }
    }
    
    @Test
    public void noShipsDisappeared() throws SQLException {
        SQLStmt countShips = new SQLStmt(
                "SELECT COUNT(*) FROM " + 
                        GalaxyConstants.TABLENAME_SHIPS + ";"
                );
        PreparedStatement ps = getPreparedStatement(conn, countShips);
        ResultSet rs = ps.executeQuery();
        try {
            assertTrue("Query should return something", rs.next());
            int shipCount = rs.getInt(1);
            assertEquals("Ship count should not have changed", 
                    GalaxyConstants.NUM_SHIPS + 1, shipCount);
        } finally {
            rs.close();
        }
    }
    
    @Test // TODO Handle solarsystem borders
    public void oneMove() throws SQLException {
        Move proc = new Move();
        int[] cords = getPosition();
        int new_x = cords[0] + moveStepSize;
        int new_y = cords[1] + moveStepSize;
        assertEquals("Move should be successfull", Move.MOVE_SUCCESSFUL, 
                proc.run(conn, shipID, moveStepSize, moveStepSize));
        cords = getPosition();
        assertEquals("X should be new position", new_x, cords[0]);
        assertEquals("Y should be new position", new_y, cords[1]);
    }
    
    private void removeTestValues() throws SQLException {
        SQLStmt del = new SQLStmt(
                "DELETE FROM " + GalaxyConstants.TABLENAME_SHIPS + 
                " WHERE sid = 0; " +
                "DELETE FROM " + GalaxyConstants.TABLENAME_SOLARSYSTEMS +
                " WHERE ssid = 0; " +
                "DELETE FROM " + GalaxyConstants.TABLENAME_CLASSES + 
                " WHERE cid = 0;"
                );
        PreparedStatement ps = getPreparedStatement(conn, del);
        ps.execute();
    }
    
    @Before
    public void setup() throws SQLException {
        shipID = 0;
        conn = DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1:5432/galaxy", 
                "carljohnsen", 
                "test");
        createTestValues();
    }
    
    @After
    public void tearDown() throws SQLException {
        removeTestValues();
        conn.close();
    }
    
    @Test // TODO Handle solarsystem borders
    public void withinReachability() throws SQLException {
        Move proc = new Move();
        int[] cords = getPositionAndReach();
        int x = cords[0];
        int y = cords[1];
        int reach = cords[2];
        assertEquals("Move should be successfull", 0, proc.run(conn, shipID, 
                x + (reach * 2), y + (reach * 2)));
        cords = getPosition();
        assertEquals("X should be within reach",
                x + reach, cords[0]);
        assertEquals("Y should be within reach",
                y + reach, cords[1]);
    }
    
}