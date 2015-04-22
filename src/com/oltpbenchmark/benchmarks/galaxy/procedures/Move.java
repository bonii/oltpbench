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

import com.oltpbenchmark.util.Triple;

/**
 * A class containing the Move procedure
 */
public class Move extends Procedure {

    // Potential return codes
    public static final long MOVE_SUCCESSFUL = 0;
    public static final long MOVE_NO_SHIPS = 1;
    
    // Solar system information
    private Triple<Integer, Integer, Integer> systemMax;

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
        "position_z BETWEEN ? AND ? AND " +
        GalaxyConstants.TABLENAME_SHIPS + ".solar_system_id = ?;"
    );

    // Update ship position
    public final SQLStmt updateShipPos = new SQLStmt(
        "UPDATE " + GalaxyConstants.TABLENAME_SHIPS +
        " SET position_x = ?, position_y = ?, position_z = ? WHERE ship_id = ?;"
    );
    
    /**
     * Caps the move offset, to fit the ships reachability.
     * Also ensures that the ships does not move outside the solar system
     * 
     * @param ship The ship that is moving
     * @param moveOffset The offset that the ship is trying to move
     */
    private void capToReachability(Ship ship, Triple<Integer, Integer, Integer> moveOffset) {
        int offsetX;
        int offsetY;
        int offsetZ;
        
        // Cap to reachability
        if (moveOffset.left < 0) {
            offsetX = Math.max(moveOffset.left, -ship.reachability);
        } else {
            offsetX = Math.min(moveOffset.left, ship.reachability);
        }
        if (moveOffset.middle < 0) {
            offsetY = Math.max(moveOffset.middle, -ship.reachability);
        } else {
            offsetY = Math.min(moveOffset.middle, ship.reachability);
        }
        if (moveOffset.right < 0) {
            offsetZ = Math.max(moveOffset.right, -ship.reachability);
        } else {
            offsetZ = Math.min(moveOffset.right, ship.reachability);
        }
        
        // Check if the ship is beyond solar system borders
        int positionX = Math.max(Math.min(systemMax.left, ship.position.left + offsetX), 0);
        int positionY = Math.max(Math.min(systemMax.middle, ship.position.middle + offsetY), 0);
        int positionZ = Math.max(Math.min(systemMax.right, ship.position.right + offsetZ), 0);
        ship.position = new Triple<Integer, Integer, Integer>(positionX, positionY, positionZ);
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
            if (sh.shipId == ship.shipId) continue; // Do not check itself
            if (sh.position == ship.position) return true; // Position is taken
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
            int offsetZ = rng.nextInt();
            
            // Generate if they should be negative
            offsetX *= (rng.nextBoolean()) ? -1 : 1;
            offsetY *= (rng.nextBoolean()) ? -1 : 1;
            offsetZ *= (rng.nextBoolean()) ? -1 : 1;
            
            // Cap to reachability and check if position is free. Remove if not
            capToReachability(ship, new Triple<Integer, Integer, Integer>(offsetX, offsetY, offsetZ));
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
            Triple<Integer, Integer, Integer> minPos, 
            Triple<Integer, Integer, Integer> maxPos) throws SQLException {
        // Prepare variables and statement
        ArrayList<Ship> ships = new ArrayList<Ship>();
        PreparedStatement ps = getPreparedStatement(conn, getShipsAndInformation);
        ps.setInt(1, minPos.left);
        ps.setInt(2, maxPos.left);
        ps.setInt(3, minPos.middle);
        ps.setInt(4, maxPos.middle);
        ps.setInt(5, minPos.right);
        ps.setInt(6, maxPos.right);
        ps.setInt(7, solarSystemId);
        ResultSet rs = ps.executeQuery();
        
         // Gather information about each ship, and save it in ships
        try {
            while (rs.next()) {
                Ship ship = new Ship(rs.getInt(1)); // shipId
                ship.position = new Triple<Integer, Integer, Integer>(
                        rs.getInt(2), rs.getInt(3), rs.getInt(4));
                ship.reachability = rs.getInt(5);
                ships.add(ship);
                if (systemMax == null) { // Only need to set it once
                    systemMax = new Triple<Integer, Integer, Integer>(
                            rs.getInt(6), rs.getInt(7), rs.getInt(8));
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
    public long run(Connection conn, int solarSystemId, Triple<Integer, Integer, Integer> minPos, 
            Triple<Integer, Integer, Integer> maxPos, Random rng) throws SQLException {
        ArrayList<Ship> ships = getShipsInformation(conn, solarSystemId, minPos, maxPos);
        if (ships.size() == 0) return MOVE_NO_SHIPS;
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
            ps.setInt(1, ship.position.left);
            ps.setInt(2, ship.position.middle);
            ps.setInt(3, ship.position.right);
            ps.setInt(4, ship.shipId);
            ps.addBatch();
        }
        ps.executeBatch();
    }
}
