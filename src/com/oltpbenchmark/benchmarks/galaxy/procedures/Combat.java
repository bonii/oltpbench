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

    public final SQLStmt deleteFittings = new SQLStmt(
        "DELETE FROM " + GalaxyConstants.TABLENAME_FITTINGS + " " +
        "WHERE ship_id = ?;"
    );
    
    public final SQLStmt deleteShip = new SQLStmt(
        "DELETE FROM " + GalaxyConstants.TABLENAME_SHIPS + " " +
        "WHERE ship_id = ?;"
    );
    
    // Get all ships within a range in a specified solar system    
    public final SQLStmt queryShipsInRange = new SQLStmt(
        "SELECT ships.ship_id, health_points, " + 
        "SUM(CASE WHEN fitting_type = " + GalaxyConstants.FITTING_TYPE_OFFENSIVE +
        " THEN fitting_value ELSE 0 END) as offensive," + 
        "SUM(CASE WHEN fitting_type = " + GalaxyConstants.FITTING_TYPE_DEFENSIVE + 
        " THEN fitting_value ELSE 0 END) as defensive " +
        "FROM " + GalaxyConstants.TABLENAME_SHIPS + " " +
        "JOIN " + GalaxyConstants.TABLENAME_FITTINGS + " " +
        "ON " + GalaxyConstants.TABLENAME_SHIPS + ".ship_id " +
        "= " + GalaxyConstants.TABLENAME_FITTINGS + ".ship_id " +
        "JOIN " + GalaxyConstants.TABLENAME_FITTING + " " + 
        "ON " + GalaxyConstants.TABLENAME_FITTINGS + ".fitting_id " +
        "= " + GalaxyConstants.TABLENAME_FITTING + ".fitting_id " + 
        "WHERE position_x BETWEEN ? AND ? " + 
        "AND position_y BETWEEN ? AND ? " +
        "AND solar_system_id = ? " +
        "GROUP BY " + GalaxyConstants.TABLENAME_SHIPS + ".ship_id;"
    );
    
    public final SQLStmt updateShip = new SQLStmt(
        "UPDATE " + GalaxyConstants.TABLENAME_SHIPS + " " +
        "SET health_points = ? WHERE ship_id = ?;"
    );

    /**
     * Runs the combat procedure.
     * <br>
     * @throws SQLException
     */
    public long run(Connection conn, int solarSystemId, Pair<Integer, Integer> minPos,
        Pair<Integer, Integer> maxPos, Random rng) throws SQLException {
        ArrayList<Ship> ships = getShipInformation(conn, solarSystemId, minPos, maxPos);
        Pair<Integer, Integer> groupDmgs = getGroupDmgs(ships);
        divideDmgs(ships, groupDmgs);
        updateShips(conn, ships);
        return COMBAT_SUCCESSFUL;
    }
    
    private void divideDmgs(ArrayList<Ship> ships, Pair<Integer, Integer> groupDmgs) {
        int group1Avg = 0;
        int group2Avg = 0;
        int groupSize = ships.size() / 2;
        // Group one will always be larger than, or equal to group two
        group1Avg = groupDmgs.first / (groupSize + (ships.size() % 2));
        group2Avg = groupDmgs.second / groupSize;
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            if (i % 2 == 0) {
                ship.healthPoints -= group2Avg - ship.defence; 
            } else {
                ship.healthPoints -= group1Avg - ship.defence;
            }
        }
    }
    
    // TODO make avgs
    private Pair<Integer, Integer> getGroupDmgs(ArrayList<Ship> ships) {
        int group1Dmg = 0;
        int group2Dmg = 0;
        for (int i = 0; i < ships.size(); i++) {
            if (i % 2 == 0) {
                group1Dmg += ships.get(i).damage;
            } else {
                group2Dmg += ships.get(i).damage;
            }
        }
        return new Pair<Integer, Integer>(group1Dmg, group2Dmg);
    }

    private ArrayList<Ship> getShipInformation(Connection conn, int solarSystemId, Pair<Integer, Integer> minPos,
        Pair<Integer, Integer> maxPos) throws SQLException {
        // Get ship information
        PreparedStatement ps = getPreparedStatement(conn, queryShipsInRange);
        ps.setInt(1, minPos.first);
        ps.setInt(2, maxPos.first);
        ps.setInt(3, minPos.second);
        ps.setInt(4, maxPos.second);
        ps.setInt(5, solarSystemId);
        ResultSet rs = ps.executeQuery();
        ArrayList<Ship> ships = new ArrayList<Ship>();
        int shipId;
        int healthPoints;
        int damage;
        int defence;
        try {
            while (rs.next()) {
                shipId = rs.getInt(1);
                healthPoints = rs.getInt(2);
                damage = rs.getInt(3);
                defence = rs.getInt(4);
                ships.add(new Ship(shipId, healthPoints, damage, defence));
            }
        } finally {
            rs.close();
        }
        return ships;
    }
    
    private void updateShips(Connection conn, ArrayList<Ship> ships) 
            throws SQLException {
        PreparedStatement shipUpdates = getPreparedStatement(conn, updateShip);
        PreparedStatement shipDeletes = getPreparedStatement(conn, deleteShip);
        PreparedStatement fittingsDeletes = getPreparedStatement(conn, deleteFittings);
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            if (ship.healthPoints <= 0) {
                shipDeletes.setInt(1, ship.shipId);
                shipDeletes.addBatch();
                fittingsDeletes.setInt(1, ship.shipId);
                fittingsDeletes.addBatch();
            } else {
                shipUpdates.setInt(1, ship.healthPoints);
                shipUpdates.setInt(2, ship.shipId);
                shipUpdates.addBatch();
            }
        }
        shipUpdates.executeBatch();
        fittingsDeletes.executeBatch();
        shipDeletes.executeBatch();
    }

}
