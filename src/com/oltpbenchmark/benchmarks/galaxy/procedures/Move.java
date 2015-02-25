package com.oltpbenchmark.benchmarks.galaxy.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;

/**
 * A class containing the Move procedure
 */
public class Move extends Procedure {

    /**
     * A class that mimics an integer tuple
     */
    private class Tuple {
        public int x;
        public int y;

        /**
         * Creates a new instance of the Tuple class
         * @param x The first integer value
         * @param y The second integer value
         */
        public Tuple(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Compares the called Tuple object with another Tuple object
         * @param other The Tuple object to compare with
         * @return True if the values of the two Tuple objects are equal 
         */
        public boolean equals(Tuple other) {
            return this.x == other.x && this.y == other.y;
        }
    }

    // Potential return codes
    public static final long MOVE_SUCCESSFUL = 0;
    public static final long MOVE_NOT_SUCCESSFUL = 1;
    public static final long ERR_INVALID_SHIP = 1;

    // Check single tile if free
    public final SQLStmt checkTileStmt = new SQLStmt(
        "SELECT x, y FROM " + GalaxyConstants.TABLENAME_SHIPS +
        " WHERE x BETWEEN ?-1 AND ?+1 AND y BETWEEN ?-1 AND ?+1 AND ssid = ?" +
        " AND sid != ?;"
    );

    // Get ship, class and solarsystem information
    public final SQLStmt getShipInfo = new SQLStmt(
            "SELECT x, y, reachability, " +
            GalaxyConstants.TABLENAME_SHIPS + ".ssid, x_max, y_max FROM " +
            GalaxyConstants.TABLENAME_SHIPS + " JOIN " +
            GalaxyConstants.TABLENAME_CLASSES + " ON " +
            GalaxyConstants.TABLENAME_SHIPS + ".class = " +
            GalaxyConstants.TABLENAME_CLASSES + ".cid JOIN " +
            GalaxyConstants.TABLENAME_SOLARSYSTEMS + " ON " +
            GalaxyConstants.TABLENAME_SHIPS + ".ssid = " +
            GalaxyConstants.TABLENAME_SOLARSYSTEMS + ".ssid WHERE sid = ?;"
            );

    // Update ship position
    public final SQLStmt updateShipPosStmt = new SQLStmt(
        "UPDATE " + GalaxyConstants.TABLENAME_SHIPS +
        " SET x = ?, y = ? WHERE sid = ?;"
    );

    /**
     * Runs the move procedure.
     * <br>
     * Starts by retrieving ship information, then it caps the movement to the
     * ships reachability and finally makes a list of possible positions in
     * the vicinity of the target position, and selects a random position from
     * that list.
     * @param conn The connection to the database
     * @param shipId The id of the ship to be moved
     * @param move_x Relative x position to move
     * @param move_y Relative y position to move
     * @return MOVE_SUCCESSFUL if the move was possible and 
     * MOVE_NOT_SUCCESSFUL if not
     * @throws SQLException
     */
    public long run(Connection conn, int shipId, int move_x, int move_y)
            throws SQLException {
        // Get ship information
        PreparedStatement ps = getPreparedStatement(conn, getShipInfo);
        ps.setInt(1, shipId);
        ResultSet rs = ps.executeQuery();
        int x;
        int y;
        int reachability;
        int ssid;
        int x_max;
        int y_max;
        try {
            if (!rs.next()) {
                System.out.println("Ship: " + Integer.toString(shipId));
                return ERR_INVALID_SHIP;
            } else {
                x = rs.getInt(1);
                y = rs.getInt(2);
                reachability = rs.getInt(3);
                ssid = rs.getInt(4);
                x_max = rs.getInt(5);
                y_max = rs.getInt(6);
            }
        } finally {
            rs.close();
        }

        // Cap the movement to reachability
        if (move_x < 0) {
            move_x = Math.max(move_x, -reachability);
        } else {
            move_x = Math.min(move_x, reachability);
        }
        if (move_y < 0) {
            move_y = Math.max(move_y, -reachability);
        } else {
            move_y = Math.min(move_y, reachability);
        }
        int new_x = Math.max(Math.min(x_max, x + move_x), 0);
        int new_y = Math.max(Math.min(y_max, y + move_y), 0);

        // Prepare the list of possible positions
        ArrayList<Tuple> possibles = new ArrayList<Tuple>();
        Tuple min = new Tuple(Math.max(0, new_x - 1),
                Math.max(0, new_y));
        Tuple max = new Tuple(Math.min(x_max, new_x + 1),
                Math.min(y_max, new_y + 1));
        for (int i = min.x; i <= max.x; i++) {
            for (int j = min.y; j <= max.y; j++) {
                possibles.add(new Tuple(i,j));
            }
        }

        // Get all ships in the window 
        // and remove them from the possible positions
        ps = getPreparedStatement(conn, checkTileStmt);
        ps.setInt(1, new_x);
        ps.setInt(2, new_x);
        ps.setInt(3, new_y);
        ps.setInt(4, new_y);
        ps.setInt(5, ssid);
        ps.setInt(6, shipId);
        rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Tuple taken = new Tuple(rs.getInt(1), rs.getInt(2));
                for (int i = 0; i < possibles.size(); i++) {
                    if (taken.equals(possibles.get(i))) {
                        possibles.remove(i);
                    }
                }
            }
        } finally {
            rs.close();
        }

        // Select a random position among the possibles, if any
        if (possibles.size() == 0) { 
            return MOVE_NOT_SUCCESSFUL;
        } else { 
            Tuple rand = possibles.get(new Random().nextInt(possibles.size()));
            new_x = rand.x;
            new_y = rand.y;
        }

        // Update the ships position
        ps = getPreparedStatement(conn, updateShipPosStmt);
        ps.setInt(1, new_x);
        ps.setInt(2, new_y);
        ps.setInt(3, shipId);
        ps.execute();

        // Set the return value to 0: successful move
        return MOVE_SUCCESSFUL;
    }
}
