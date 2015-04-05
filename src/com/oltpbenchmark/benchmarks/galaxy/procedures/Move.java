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
import com.oltpbenchmark.util.Pair;

/**
 * A class containing the Move procedure
 */
public class Move extends Procedure {

    // Potential return codes
    public static final long MOVE_SUCCESSFUL = 0;
    public static final long MOVE_NOT_SUCCESSFUL = 1;
    public static final long ERR_INVALID_SHIP = 2;

    // Check single tile if free
    public final SQLStmt checkTileStmt = new SQLStmt(
        "SELECT position_x, poasition_y FROM " + GalaxyConstants.TABLENAME_SHIPS +
        " WHERE position_x BETWEEN ?-1 AND ?+1 AND position_y BETWEEN ?-1 AND ?+1 AND " +
        "solar_system_id = ? AND ship_id != ?;"
    );

    // Get ship, class and solarsystem information
    public final SQLStmt getShipInfo = new SQLStmt(
            "SELECT position_x, position_y, reachability, " +
            GalaxyConstants.TABLENAME_SHIPS + ".solar_system_id, " +
            "max_position_x, max_position_y FROM " +
            GalaxyConstants.TABLENAME_SHIPS + " JOIN " +
            GalaxyConstants.TABLENAME_CLASSES + " ON " +
            GalaxyConstants.TABLENAME_SHIPS + ".class_id = " +
            GalaxyConstants.TABLENAME_CLASSES + ".class_id JOIN " +
            GalaxyConstants.TABLENAME_SOLARSYSTEMS + " ON " +
            GalaxyConstants.TABLENAME_SHIPS + ".solar_system_id = " +
            GalaxyConstants.TABLENAME_SOLARSYSTEMS + ".solar_system_id " +
            "WHERE ship_id = ?;"
            );

    // Update ship position
    public final SQLStmt updateShipPosStmt = new SQLStmt(
        "UPDATE " + GalaxyConstants.TABLENAME_SHIPS +
        " SET position_x = ?, position_y = ? WHERE ship_id = ?;"
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
     * MOVE_NOT_SUCCESSFUL if not and ERR_INVALID_SHIP if the ship didn't exist
     * @throws SQLException
     */
    public long run(Connection conn, int shipId, int move_x, int move_y, Random rng)
            throws SQLException {
        // Get ship information
        PreparedStatement ps = getPreparedStatement(conn, getShipInfo);
        ps.setInt(1, shipId);
        ResultSet rs = ps.executeQuery();
        Pair<Integer, Integer> position;
        int reachability;
        int ssid;
        Pair<Integer, Integer> systemMax;
        try {
            if (!rs.next()) {
                System.out.println("Ship: " + Integer.toString(shipId));
                return ERR_INVALID_SHIP;
            } else {
                position = new Pair<Integer, Integer>
                        (rs.getInt(1), rs.getInt(2));
                reachability = rs.getInt(3);
                ssid = rs.getInt(4);
                systemMax = new Pair<Integer, Integer>
                        (rs.getInt(5), rs.getInt(6));
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
        Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>
                (Math.max(Math.min(systemMax.first, position.first + move_x), 0),
                 Math.max(Math.min(systemMax.second, position.second + move_y), 0));

        // Prepare the list of possible positions
        ArrayList<Pair<Integer, Integer>> possibles = 
                new ArrayList<Pair<Integer, Integer>>();
        Pair<Integer, Integer> min = new Pair<Integer, Integer>
                (Math.max(0, newPosition.first - 1), 
                 Math.max(0, newPosition.second));
        Pair<Integer, Integer> max = new Pair<Integer, Integer>
                (Math.min(systemMax.first, newPosition.first + 1), 
                 Math.min(systemMax.second, newPosition.second + 1));
        for (int i = min.first; i <= max.first; i++) {
            for (int j = min.second; j <= max.second; j++) {
                possibles.add(new Pair<Integer, Integer>(i,j));
            }
        }

        // Get all ships in the window
        // and remove them from the possible positions
        ps = getPreparedStatement(conn, checkTileStmt);
        ps.setInt(1, newPosition.first);
        ps.setInt(2, newPosition.first);
        ps.setInt(3, newPosition.second);
        ps.setInt(4, newPosition.second);
        ps.setInt(5, ssid);
        ps.setInt(6, shipId);
        rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Pair<Integer, Integer> taken = new Pair<Integer, Integer>
                        (rs.getInt(1), rs.getInt(2));
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
            newPosition = possibles.get(rng.nextInt(possibles.size()));
        }

        // Update the ships position
        ps = getPreparedStatement(conn, updateShipPosStmt);
        ps.setInt(1, newPosition.first);
        ps.setInt(2, newPosition.second);
        ps.setInt(3, shipId);
        ps.execute();

        // Set the return value to 0: successful move
        return MOVE_SUCCESSFUL;
    }
}
