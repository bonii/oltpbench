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
public class Move extends Procedure {

    // Potential return codes
    public static final long MOVE_SUCCESSFUL = 0;
    public static final long MOVE_NOT_SUCCESSFUL = 1;
    public static final long ERR_INVALID_SHIP = 2;
    
    // Solar system information
    private Pair<Integer, Integer> systemMax;

    // Get ship, class and solarsystem information
    public final SQLStmt getShipsAndInformation = new SQLStmt(
        "SELECT ship_id, position_x, position_y, position_z, reachability, " + 
        "max_position_x, max_position_y, max_position_z FROM " + 
        GalaxyConstants.TABLENAME_SHIPS + " JOIN " +
        GalaxyConstants.TABLENAME_CLASSES + " ON " +
        GalaxyConstants.TABLENAME_SHIPS + ".class_id = " +
        GalaxyConstants.TABLENAME_CLASSES + ".class_id JOIN " +
        GalaxyConstants.TABLENAME_SOLARSYSTEMS + " ON " + 
        GalaxyConstants.TABLENAME_SHIPS + ".solar_system_id = " + 
        GalaxyConstants.TABLENAME_SOLARSYSTEMS + ".solar_system_id " +
        "WHERE position_x BETWEEN ? AND ? AND " + 
        "position_y BETWEEN ? AND ? AND " +
        "position_z BETWEEN ? AND ? AND solar_system_id = ?;"
    );

    // Update ship position
    public final SQLStmt updateShipPosStmt = new SQLStmt(
        "UPDATE " + GalaxyConstants.TABLENAME_SHIPS +
        " SET position_x = ?, position_y = ? WHERE ship_id = ?;"
    );
    
    private void capToReachability(Ship ship, Pair<Integer, Integer> moveOffset) {
        int offsetX;
        int offsetY;
        if (moveOffset.first < 0) {
            offsetX = Math.max(moveOffset.first, -ship.reachability);
        } else {
            offsetX = Math.min(moveOffset.first, ship.reachability);
        }
        if (moveOffset.second < 0) {
            offsetY = Math.max(moveOffset.second, -ship.reachability);
        } else {
            offsetY = Math.min(moveOffset.second, ship.reachability);
        }
        ship.positionX = Math.max(Math.min(systemMax.first, ship.positionX + offsetX), 0);
        ship.positionY = Math.max(Math.min(systemMax.second, ship.positionY + offsetY), 0);
    }
    
    private void generateMoves(ArrayList<Ship> ships, Random rng) {
        //generate and capToReachability
        for (Ship ship : ships) {
            capToReachability(ship, new Pair<Integer, Integer>(rng.nextInt(), rng.nextInt()));
            // TODO Check if can move, if not, offset by 1 in random direction? 
        }
    }
    
    private ArrayList<Ship> getShipsInformation(Connection conn, int solarSystemId, Pair<Integer, Integer> minPos, 
            Pair<Integer, Integer> maxPos) throws SQLException {
        ArrayList<Ship> ships = new ArrayList<Ship>();
        PreparedStatement ps = getPreparedStatement(conn, getShipsAndInformation);
        ps.setInt(1, minPos.first);
        ps.setInt(2, maxPos.first);
        ps.setInt(3, minPos.second);
        ps.setInt(4, maxPos.second);
        ps.setInt(5, solarSystemId);
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Ship ship = new Ship(rs.getInt(1)); // shipId
                ship.positionX = rs.getInt(2);
                ship.positionY = rs.getInt(3);
                ship.positionZ = rs.getInt(4);
                ship.reachability = rs.getInt(5);
                ships.add(ship);
                if (systemMax == null) {
                    systemMax = new Pair<Integer,Integer>(rs.getInt(6), rs.getInt(7));
                }
            }
        } finally {
            rs.close();
        }
        return ships;
    }

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
    public long run(Connection conn, int solarSystemId, Pair<Integer, Integer> minPos, 
            Pair<Integer, Integer> maxPos, Random rng) throws SQLException {
        ArrayList<Ship> ships = getShipsInformation(conn, solarSystemId, minPos, maxPos);
        generateMoves(ships, rng);
        updateShipInformation(ships);
        // Set the return value to 0: successful move
        return MOVE_SUCCESSFUL;
    }
    
    public void updateShipInformation(ArrayList<Ship> ships) throws SQLException {
        // TODO batch!
    }
}
