package com.oltpbenchmark.benchmarks.galaxy.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;

public class Tests extends Procedure {

    private Connection conn;
    private int shipID = 0;
    private int moveStepSize = 5;

    private String ships = GalaxyConstants.TABLENAME_SHIPS;
    private String classes = GalaxyConstants.TABLENAME_CLASSES;
    private String solarsystems = GalaxyConstants.TABLENAME_SOLARSYSTEMS;

    public final SQLStmt tmpShip = new SQLStmt(
                "INSERT INTO " + GalaxyConstants.TABLENAME_SHIPS +
                " VALUES (?, 0, 0, 0, 0);"
                );

    public final SQLStmt del = new SQLStmt(
                "DELETE FROM " + GalaxyConstants.TABLENAME_SHIPS +
                " WHERE sid = ?;"
                );

    public final SQLStmt classInsert = new SQLStmt(
                "INSERT INTO " + classes + " VALUES (0, ?, 1000);"
                );
    public final SQLStmt systemInsert = new SQLStmt(
                "INSERT INTO " + solarsystems + " VALUES (0, 100000, 100000);"
                );
    public final SQLStmt shipsInsert = new SQLStmt(
                "INSERT INTO " + ships + " VALUES (0, 0, 0, 0, 0);"
                );

    public final SQLStmt getPos = new SQLStmt(
                "SELECT x, y FROM " + GalaxyConstants.TABLENAME_SHIPS +
                " WHERE sid = ?;"
                );

    public final SQLStmt getPosReach = new SQLStmt(
                "SELECT x, y, reachability FROM " +
                        GalaxyConstants.TABLENAME_SHIPS + " JOIN " +
                        GalaxyConstants.TABLENAME_CLASSES + " ON " +
                        GalaxyConstants.TABLENAME_SHIPS + ".class = " +
                        GalaxyConstants.TABLENAME_CLASSES + ".cid " +
                        "WHERE sid = ?;"
                );

    public final SQLStmt findDuplicates = new SQLStmt(
                "SELECT x, y, ssid FROM ships " +
                        "GROUP BY x, y, ssid HAVING COUNT(*) > 1;"
                );

    public final SQLStmt delShips = new SQLStmt(
                "DELETE FROM " + ships + " WHERE sid = 0;"
                );

    public final SQLStmt delSystem = new SQLStmt(
                "DELETE FROM " + solarsystems + " WHERE ssid = 0;"
                );

    public final SQLStmt delClass = new SQLStmt(
                "DELETE FROM " + classes + " WHERE cid = 0;"
                );

    @Test
    public void cannotMoveOutOfSystem() throws SQLException {
        createTestValues();
        int[] maxAndReach = getSystemMaxAndReach();
        int xMax = maxAndReach[0];
        int yMax = maxAndReach[1];
        int reach = maxAndReach[2];

        // Try to move to -1 x and y
        moveDefined(-1, 0);
        int[] cords = getPosition();
        assertTrue("Ship x should still be near 0",
                cords[0] == 0 || cords[0] == 1);
        moveDefined(0, -1);
        cords = getPosition();
        assertTrue("Ship y should still be near 0",
                cords[1] == 0 || cords[1] == 1);

        int iters = (xMax / reach) + 1;
        assertTrue("asdf", iters >= 100);
        for (int i = 0; i < iters; i++) {
            moveDefined(reach, 0);
        }
        cords = getPosition();
        assertTrue("Ship x should be near the edge of the system",
                Math.abs(xMax - cords[0]) <= 1);

        iters = (yMax / reach) + 1;
        for (int i = 0; i < iters; i++) {
            moveDefined(0, reach);
        }
        cords = getPosition();
        assertTrue("Ship y should be near the edge of the system",
                Math.abs(yMax - cords[1]) <= 1);
        removeTestValues();
    }

    @Test
    public void cannotMoveOnTopOfOther() throws SQLException {
        createTestValues();
        moveDefined(moveStepSize, moveStepSize);
        int tmp = shipID;
        shipID = -1;
        PreparedStatement ps = getPreparedStatement(this.conn, this.tmpShip);
        ps.setInt(1, shipID);
        ps.execute();
        noShipsInSamePos();
        ps = getPreparedStatement(this.conn, this.del);
        ps.setInt(1, shipID);
        ps.execute();
        shipID = tmp;
        removeTestValues();
    }

    private void createTestValues() throws SQLException {

        PreparedStatement ps = getPreparedStatement(this.conn, this.classInsert);
        ps.setString(1, "Test cruiser");
        ps.execute();
        ps = getPreparedStatement(this.conn, this.systemInsert);
        ps.execute();
        ps = getPreparedStatement(this.conn, this.shipsInsert);
        ps.execute();
    }

    private int[] getPosition() throws SQLException {
        PreparedStatement ps = getPreparedStatement(this.conn, this.getPos);
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
        PreparedStatement ps = getPreparedStatement(this.conn, this.getPosReach);
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

    private int[] getSystemMaxAndReach() throws SQLException {
        SQLStmt getInfo = new SQLStmt(
                "SELECT x_max, y_max, reachability FROM " +
                GalaxyConstants.TABLENAME_SHIPS + " JOIN " +
                GalaxyConstants.TABLENAME_SOLARSYSTEMS + " ON " +
                GalaxyConstants.TABLENAME_SHIPS + ".ssid = " +
                GalaxyConstants.TABLENAME_SOLARSYSTEMS + ".ssid JOIN " +
                GalaxyConstants.TABLENAME_CLASSES + " ON " +
                GalaxyConstants.TABLENAME_SHIPS + ".class = " +
                GalaxyConstants.TABLENAME_CLASSES + ".cid WHERE sid = ?;"
                );
        PreparedStatement ps = getPreparedStatement(this.conn, getInfo);
        ps.setInt(1, shipID);
        ResultSet rs = ps.executeQuery();
        int[] info = new int[3];
        try {
            assertTrue("Query should return something", rs.next());
            info[0] = rs.getInt(1);
            info[1] = rs.getInt(2);
            info[2] = rs.getInt(3);
        } finally {
            rs.close();
        }
        return info;
    }

    @Test // TODO edit this test to handle random tile
    public void manyMoves() throws SQLException {
        createTestValues();
        int numMoves = 100;
        int[] posBefore = getPosition();
        for (int i = 0; i < numMoves; i++) {
            moveDefined(moveStepSize, moveStepSize);
        }
        int[] posAfter = getPosition();
        assertTrue("Should have moved " + numMoves + " positions",
                Math.abs(posBefore[0] + (moveStepSize * numMoves)
                        - posAfter[0]) <= numMoves);
        assertTrue("Should have moved " + numMoves + " positions",
                Math.abs(posBefore[1] + (moveStepSize * numMoves)
                        - posAfter[1]) <= numMoves);
        removeTestValues();
    }

    private void moveDefined(int x, int y) throws SQLException {
        Move proc = new Move();
        assertEquals("Move should be successfull", 0,
                proc.run(this.conn, shipID, x, y));
    }

    @Test
    public void noShipsInSamePos() throws SQLException {
        PreparedStatement ps = getPreparedStatement(this.conn, this.findDuplicates);
        ResultSet rs = ps.executeQuery();
        try {
            assertFalse("Query should not return anything", rs.next());
        } finally {
            rs.close();
        }
    }

    @Test
    public void noShipsDisappeared() throws SQLException {
        createTestValues();
        SQLStmt countShips = new SQLStmt(
                "SELECT COUNT(*) FROM " +
                        GalaxyConstants.TABLENAME_SHIPS + ";"
                );
        PreparedStatement ps = getPreparedStatement(this.conn, countShips);
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

    @Test // TODO Handle solarsystem borders
    public void oneMove() throws SQLException {
        createTestValues();
        Move proc = new Move();
        int[] cords = getPosition();
        int newX = cords[0] + moveStepSize;
        int newY = cords[1] + moveStepSize;
        assertEquals("Move should be successfull", Move.MOVE_SUCCESSFUL,
                proc.run(this.conn, shipID, moveStepSize, moveStepSize));
        cords = getPosition();
        assertTrue("X should be near new position",
                Math.abs(newX - cords[0]) <= 1);
        assertTrue("Y should be near new position",
                Math.abs(newY - cords[1]) <= 1);
        removeTestValues();
    }

    private void removeTestValues() throws SQLException {

        PreparedStatement ps = getPreparedStatement(this.conn, this.delShips);
        ps.execute();
        ps = getPreparedStatement(this.conn, this.delSystem);
        ps.execute();
        ps = getPreparedStatement(this.conn, this.delClass);
        ps.execute();
    }

    public void run(Connection conn) throws SQLException {
        this.conn = conn;
        cannotMoveOutOfSystem();
        cannotMoveOnTopOfOther();
        manyMoves();
        noShipsInSamePos();
        noShipsDisappeared();
        oneMove();
        withinReachability();
    }

    @Test // TODO Handle solarsystem borders
    public void withinReachability() throws SQLException {
        createTestValues();
        int[] cords = getPositionAndReach();
        int x = cords[0];
        int y = cords[1];
        int reach = cords[2];
        moveDefined(reach * 2, reach * 2);
        cords = getPosition();
        assertTrue("X should be within reach",
                Math.abs(x + reach - cords[0]) <= 1);
        assertTrue("Y should be within reach",
                Math.abs(y + reach - cords[1]) <= 1);
        removeTestValues();
    }

}
