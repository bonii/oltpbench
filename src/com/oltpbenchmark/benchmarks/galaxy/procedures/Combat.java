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
import com.oltpbenchmark.benchmarks.galaxy.util.Ship;
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
        "SELECT ship_id, health_points FROM " + GalaxyConstants.TABLENAME_SHIPS +
        " WHERE position_x BETWEEN ? AND ? AND position_y BETWEEN ? AND ? AND " +
        "solar_system_id = ?;"
    );

    public final SQLStmt getShipFittings = new SQLStmt(
        "SELECT SUM(fitting_value) FROM " + GalaxyConstants.TABLENAME_FITTINGS +
        " WHERE ship_id = ? AND fitting_type = ?;"
    );

    /**
     * Runs the combat procedure.
     * <br>
     * @throws SQLException
     */
    public long run(Connection conn, int solarSystemId, Pair<Integer, Integer> minPos,
        Pair<Integer, Integer> maxPos, Random rng) throws SQLException {
        ArrayList<Ship> ships = getShipInformation(conn, solarSystemId, minPos, maxPos);
        ships = getShipDamageDefence(conn, ships);

        for (int i = 0; i < ships.size(); i++) {
            if (i % 2 == 0) {
        }

        return COMBAT_SUCCESSFUL
    }

    private ArrayList<Ship> getShipInformation(conn, solarSystemId, Pair<Integer, Integer> minPos,
        Pair<Integer, Integer> maxPos) throws SQLException {
        // Get ship information
        PreparedStatement ps = getPreparedStatement(conn, queueRangeShips);
        ps.setInt(1, minPos.first);
        ps.setInt(2, minPos.second);
        ps.setInt(3, maxPos.first);
        ps.setInt(4, maxPos.second);
        ps.setInt(5, solarSystemId);
        ResultSet rs = ps.executeQuery();
        ArrayList<Ship> ships = new ArrayList<Ship>;
        int shipId;
        int healthPoints;
        try {
            if (!rs.next()) {
                throw new SQLException();
            } else {
                shipId = rs.getInt(1);
                healthPoints = rs.getInt(2);
                ships.add(new Ship(shipId, healthPoints);
            }
        } finally {
            rs.close();
        }
        return ships;
    }

    private ArrayList<Ship> getShipDamageDefence(conn, ArrayList<Ship>) throws SQLException {
        int tempDamage;
        int tempDefence;
        ResultSet rs;
        for (Ship ship : ships) {
            ps = getPreparedStatement(conn, getShipFittings);
            ps.setInt(1, ship.shipId);
            ps.setInt(2, GalaxyConstants.FITTING_TYPE_OFFENSIVE);
            rs = ps.executeQuery();
            try {
                if (!rs.next()) {
                    throw new SQLException();
                } else {
                    tempDamage = rs.getInt(1);
                }
            } finally {
                rs.close();
            }
            ps.setInt(2, GalaxyConstants.FITTING_TYPE_DEFENSIVE);
            rs = ps.executeQuery();
            try {
                if (!rs.next()) {
                    throw new SQLException();
                } else {
                    tempDefence = rs.getInt(2);
                }
            } finally {
              rs.close();
            }
            ship.setDamageDefence(tempDamage, tempDefence);
        }
        return ships;
    }

}
