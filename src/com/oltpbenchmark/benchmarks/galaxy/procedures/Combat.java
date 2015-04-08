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
public class Combat extends Procedure {

    // Potential return codes
    public static final long COMBAT_SUCCESSFUL = 0;
    public static final long COMBAT_NOT_SUCCESSFUL = 1;
    public static final long ERR_INVALID_SHIP = 2;

    // Get all ships within a range in a specified solarsystem
    public final SQLStmt queueRangeShips = new SQLStmt(
        "SELECT * FROM " + GalaxyConstants.TABLENAME_SHIPS +
        " WHERE position_x BETWEEN ? AND ? AND position_y BETWEEN ? AND ? AND " +
        "solar_system_id = ?;"
    );

    /**
     * Runs the combat procedure.
     * <br>
     * @throws SQLException
     */
    public long run(Connection conn, int solarSystemId, Pair<Integer, Integer> minPos,
        Pair<Integer, Integer> maxPos, Random rng) throws SQLException {
        // Get ship information
        PreparedStatement ps = getPreparedStatement(conn, queueRangeShips);
        ps.setInt(1, minPos.first);
        ps.setInt(2, minPos.second);
        ps.setInt(3, maxPos.first);
        ps.setInt(4, maxPos.second);
        ps.setInt(5, solarSystemId);
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
