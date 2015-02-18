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

import com.oltpbenchmark.DBWorkload;
import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;

public class Tests extends Procedure {
    
    public Connection conn;
    public int shipID;
    
    @Before
    public void setup() throws SQLException {
        shipID = 1;
        conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/galaxy", "carljohnsen", "test");
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
                    GalaxyConstants.NUM_SHIPS, shipCount);
        } finally {
            rs.close();
        }
    }
    
    // TODO split into two functions: one for pos and one for reach
    private int[] getPosition() throws SQLException {
        SQLStmt getPos = new SQLStmt(
                "SELECT x, y, reachability FROM " + 
                        GalaxyConstants.TABLENAME_SHIPS + " JOIN " +
                        GalaxyConstants.TABLENAME_CLASSES + " ON " +
                        GalaxyConstants.TABLENAME_SHIPS + ".cid = " + 
                        GalaxyConstants.TABLENAME_CLASSES + ".cid" +
                        "WHERE " + GalaxyConstants.TABLENAME_SHIPS + ".sid = ?;"
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
    
    @Test // TODO Handle solarsystem borders
    public void oneMove() throws SQLException {
        Move proc = new Move();
        int[] cords = getPosition();
        int new_x = cords[0] + 1;
        int new_y = cords[1] + 1;
        assertEquals("Move should be successfull", 0, proc.run(conn, shipID, new_x, new_y));
        cords = getPosition();
        assertTrue("X should be within window", Math.abs(new_x - cords[0]) <= 1);
        assertTrue("Y should be within window", Math.abs(new_y - cords[1]) <= 1);
    }
    
    @Test // TODO Handle solarsystem borders
    public void withinReachability() throws SQLException {
        Move proc = new Move();
        int[] cords = getPosition();
        int x = cords[0];
        int y = cords[1];
        int reach = cords[2];
        assertEquals("Move should be successfull", 0, proc.run(conn, shipID, cords[0] + (reach * 2), cords[1] + (reach * 2)));
        cords = getPosition();
        assertTrue("X should be within reach", Math.abs(x - cords[0]) <= reach + 1);
        assertTrue("Y should be within reach", Math.abs(y - cords[1]) <= reach + 1);
    }
    
    public long run(Connection conn) {
        // TODO call tests
        return 0L;
    }
    
    @After
    public void tearDown() throws SQLException {
        conn.close();
    }
    
}