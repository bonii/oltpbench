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

import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * A class containing the Move procedure
 */
public class Move extends Procedure {

    // Potential return codes
    public static final long MOVE_SUCCESSFUL = 0;
    public static final long MOVE_NO_SHIPS = 1;
    
    // Solar system information
    private ImmutableTriple<Long, Long, Long> systemMax;

    // Get ship positions, that are within range
    public final SQLStmt getShips = new SQLStmt(
            "SELECT position_x, position_y, position_z FROM " +
            GalaxyConstants.TABLENAME_SHIPS + " WHERE " +
            "position_x BETWEEN ? AND ? AND " + 
            "position_y BETWEEN ? AND ? AND " +
            "position_z BETWEEN ? AND ? AND " +
            "solar_system_id = ?;"
    );
    
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
     * @return An triple, containing the new position
     */
    private ImmutableTriple<Long, Long, Long> capToReachability(
            Ship ship, ImmutableTriple<Integer, Integer, Integer> moveOffset) {
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
        long positionX = Math.max(Math.min(systemMax.left, ship.position.left + offsetX), 0);
        long positionY = Math.max(Math.min(systemMax.middle, ship.position.middle + offsetY), 0);
        long positionZ = Math.max(Math.min(systemMax.right, ship.position.right + offsetZ), 0);
        return new ImmutableTriple<Long, Long, Long>(positionX, positionY, positionZ);
    }
    
    /**
     * Finds a free position, which at most differ by 1 from the target position
     * 
     * @param conn The connection to the database
     * @param solarSystemId The solar system the procedure is running on
     * @param position The target position
     * @param rng A random generator
     * @return A Triple containing the new position, or null if there were none
     * @throws SQLException
     */
    private ImmutableTriple<Long, Long, Long> getFreePosition(Connection conn, 
            int solarSystemId, ImmutableTriple<Long, Long, Long> position, 
            Random rng) throws SQLException {
        
        ArrayList<ImmutableTriple<Long, Long, Long>> positions =
                new ArrayList<ImmutableTriple<Long, Long, Long>>();
        for (long i = position.left-1; i < position.left+2; i++) {
            for (long j = position.middle-1; j < position.middle+2; j++) {
                for (long k = position.right-1; k < position.right+2; k++) {
                    positions.add(new ImmutableTriple<Long, Long, Long>(i,j,k));
                }
            }
        }
        PreparedStatement ps = getPreparedStatement(conn, getShips);
        ps.setLong(1, position.left-1);
        ps.setLong(2, position.left+1);
        ps.setLong(3, position.middle-1);
        ps.setLong(4, position.middle+1);
        ps.setLong(5, position.right-1);
        ps.setLong(6, position.right+1);
        ps.setInt(7, solarSystemId);
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                positions.remove(new ImmutableTriple<Long, Long, Long>(
                        rs.getLong(1), rs.getLong(2), rs.getLong(3)
                ));
            }
        } finally {
            rs.close();
        }
        if (positions.size() > 0) {
            return positions.get(rng.nextInt(positions.size()));
        } else {
            return null;
        }
    }
    
    /**
     * Generates a random move for each given ship, using the given random generator
     * 
     * @param conn The connection to the database
     * @param solarSystemId The solar system the ships are in
     * @param ships The given ships
     * @param rng The given random generator
     * @throws SQLException
     */
    private void generateMoves(Connection conn, int solarSystemId, 
            ArrayList<Ship> ships, Random rng) throws SQLException {
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
            ImmutableTriple<Long, Long, Long> newPosition = 
                    capToReachability(ship, 
                            new ImmutableTriple<Integer, Integer, Integer>(offsetX, offsetY, offsetZ));
            newPosition = getFreePosition(conn, solarSystemId, newPosition, rng);
            if (newPosition == null) {
                ships.remove(i--);
            } else {
                ships.get(i).position = newPosition;
            }
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
            ImmutableTriple<Long, Long, Long> minPos, 
            ImmutableTriple<Long, Long, Long> maxPos) throws SQLException {
        // Prepare variables and statement
        ArrayList<Ship> ships = new ArrayList<Ship>();
        PreparedStatement ps = getPreparedStatement(conn, getShipsAndInformation);
        ps.setLong(1, minPos.left);
        ps.setLong(2, maxPos.left);
        ps.setLong(3, minPos.middle);
        ps.setLong(4, maxPos.middle);
        ps.setLong(5, minPos.right);
        ps.setLong(6, maxPos.right);
        ps.setInt(7, solarSystemId);
        ResultSet rs = ps.executeQuery();
        
         // Gather information about each ship, and save it in ships
        try {
            while (rs.next()) {
                Ship ship = new Ship(rs.getInt(1)); // shipId
                ship.position = new ImmutableTriple<Long, Long, Long>(
                        rs.getLong(2), rs.getLong(3), rs.getLong(4));
                ship.reachability = rs.getInt(5);
                ships.add(ship);
                if (systemMax == null) { // Only need to set it once
                    systemMax = new ImmutableTriple<Long, Long, Long>(
                            rs.getLong(6), rs.getLong(7), rs.getLong(8));
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
    public long run(Connection conn, int solarSystemId, ImmutableTriple<Long, Long, Long> minPos, 
            ImmutableTriple<Long, Long, Long> maxPos, Random rng) throws SQLException {
        ArrayList<Ship> ships = getShipsInformation(conn, solarSystemId, minPos, maxPos);
        if (ships.size() == 0) return MOVE_NO_SHIPS;
        generateMoves(conn, solarSystemId, ships, rng);
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
            ps.setLong(1, ship.position.left);
            ps.setLong(2, ship.position.middle);
            ps.setLong(3, ship.position.right);
            ps.setInt(4, ship.shipId);
            ps.addBatch();
        }
        ps.executeBatch();
    }
}
