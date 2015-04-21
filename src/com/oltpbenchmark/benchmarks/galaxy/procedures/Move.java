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
    public final SQLStmt updateShipPos = new SQLStmt(
        "UPDATE " + GalaxyConstants.TABLENAME_SHIPS +
        " SET position_x = ?, position_y = ? WHERE ship_id = ?;"
    );
    
    /**
     * Caps the move offset, to fit the ships reachability.
     * Also ensures that the ships does not move outside the solar system
     * 
     * @param ship The ship that is moving
     * @param moveOffset The offset that the ship is trying to move
     */
    private void capToReachability(Ship ship, Pair<Integer, Integer> moveOffset) {
        int offsetX;
        int offsetY;
        
        // Cap to reachability
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
        
        // Check if the ship is beyond solar system borders
        ship.positionX = Math.max(Math.min(systemMax.first, ship.positionX + offsetX), 0);
        ship.positionY = Math.max(Math.min(systemMax.second, ship.positionY + offsetY), 0);
    }
    
    /**
     * Checks if there is another ship at a given position.
     * 
     * @param ship The ship that is trying to move (ie. will be excluded from 
     *         search
     * @param ships The other ships, in the same region
     * @return True, if there is another ship at the given position
     */
    private boolean isPositionTaken(Ship ship, ArrayList<Ship> ships) {
        for (Ship sh : ships) {
            if (sh.shipId == ship.shipId) continue;
            if (sh.positionX == ship.positionX && 
                    sh.positionY == ship.positionY &&
                    sh.positionZ == ship.positionZ) {
                return true; // Position is taken
            }
        }
        return false; // Position is free
    }
    
    /**
     * Generates a random move for each given ship, using the given random generator
     * @param ships The ships, that will move
     * @param rng The random generator that will be used
     */
    private void generateMoves(ArrayList<Ship> ships, Random rng) {
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            
            // Generate offsets
            int offsetX = rng.nextInt();
            int offsetY = rng.nextInt();
            
            // Generate if they should be negative
            offsetX *= (rng.nextBoolean()) ? -1 : 1;
            offsetY *= (rng.nextBoolean()) ? -1 : 1;
            
            // Cap to reachability and check if position is free. Remove if not
            capToReachability(ship, new Pair<Integer, Integer>(offsetX, offsetY));
            if (isPositionTaken(ship, ships)) ships.remove(i--);
            // TODO Check if can move, if not, offset by 1 in random direction? 
        }
    }
    
    /**
     * Gets all the ships, that are present in between the given positions, 
     * and is in the given solarsystem
     * 
     * @param conn The connection to the database
     * @param solarSystemId The solar system the region is in
     * @param minPos The start position of the region
     * @param maxPos The end position of the region
     * @return An ArrayList, containing all the ships found
     * @throws SQLException
     */
    private ArrayList<Ship> getShipsInformation(Connection conn, int solarSystemId, 
            Pair<Integer, Integer> minPos, 
            Pair<Integer, Integer> maxPos) throws SQLException {
        // Prepare variables and statement
        ArrayList<Ship> ships = new ArrayList<Ship>();
        PreparedStatement ps = getPreparedStatement(conn, getShipsAndInformation);
        ps.setInt(1, minPos.first);
        ps.setInt(2, maxPos.first);
        ps.setInt(3, minPos.second);
        ps.setInt(4, maxPos.second);
        ps.setInt(5, solarSystemId);
        ResultSet rs = ps.executeQuery();
        
         // Gather information about each ship, and save it in ships
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
     * ships reachability, then it checks if the position is occupied, and 
     * finally it updates all the ships positions
     * 
     * @param conn The connection to the database
     * @param solarSystemId The solar system, the region is in
     * @param minPos The start position of the region
     * @param maxPos The end position of the region
     * @param rng The random generator that will be used
     * @return MOVE_SUCCESSFUL if the move was possible and
     * MOVE_NOT_SUCCESSFUL if not
     * @throws SQLException
     */
    public long run(Connection conn, int solarSystemId, Pair<Integer, Integer> minPos, 
            Pair<Integer, Integer> maxPos, Random rng) throws SQLException {
        ArrayList<Ship> ships = getShipsInformation(conn, solarSystemId, minPos, maxPos);
        if (ships.size() == 0) return MOVE_NOT_SUCCESSFUL;
        generateMoves(ships, rng);
        updateShipInformation(conn, ships);
        return MOVE_SUCCESSFUL;
    }
    
    /**
     * Updates all the given ships informations in the database
     * 
     * @param conn The connection to the database
     * @param ships The ships that will be updated
     * @throws SQLException
     */
    public void updateShipInformation(Connection conn, ArrayList<Ship> ships) throws SQLException {
        PreparedStatement ps = getPreparedStatement(conn, updateShipPos);
        for (Ship ship : ships) {
            ps.setInt(1, ship.positionX);
            ps.setInt(2, ship.positionY);
            ps.setInt(3, ship.positionZ);
            ps.setInt(4, ship.shipId);
            ps.addBatch();
        }
        ps.executeBatch();
    }
}
