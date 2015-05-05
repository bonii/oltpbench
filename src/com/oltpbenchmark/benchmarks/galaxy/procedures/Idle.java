package com.oltpbenchmark.benchmarks.galaxy.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;
import com.oltpbenchmark.benchmarks.galaxy.util.Ship;
import com.oltpbenchmark.util.Triple;

public class Idle extends Procedure {
    
    // Possible return codes
    public static final long IDLE_SUCCESSFUL = 0L;
    
    public final SQLStmt getShipsStmt = new SQLStmt(
            "SELECT ship_id, position_x, position_y, position_z, class_id FROM " +
            GalaxyConstants.TABLENAME_SHIPS + " WHERE " +
            "position_x BETWEEN ? AND ? AND " +
            "position_y BETWEEN ? AND ? AND " +
            "position_z BETWEEN ? AND ? AND " +
            "solar_system_id = ?;"
    );
    
    private ArrayList<Ship> getShips(Connection conn, int solarSystemId, 
            Triple<Long, Long, Long> minPos, Triple<Long, Long, Long> maxPos)
            throws SQLException {
        ArrayList<Ship> ships = new ArrayList<Ship>();
        PreparedStatement ps = getPreparedStatement(conn, getShipsStmt);
        ps.setLong(1, minPos.left);
        ps.setLong(2, maxPos.left);
        ps.setLong(3, minPos.middle);
        ps.setLong(4, maxPos.middle);
        ps.setLong(5, minPos.right);
        ps.setLong(6, maxPos.right);
        ps.setLong(7, solarSystemId);
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Ship ship = new Ship(rs.getInt(1));
                ship.position = new Triple<Long, Long, Long>(
                        rs.getLong(2), rs.getLong(3), rs.getLong(4)
                        );
                ship.class_id = rs.getInt(5);
                ships.add(ship);
            }
        } finally {
            rs.close();
        }
        return ships;
    }
    
    private void getVisibleObjects(Connection conn, int solarSystemId, 
            ArrayList<Ship> ships) throws SQLException {
        for (Ship ship : ships) {
            PreparedStatement ps = getPreparedStatement(conn, getShipsStmt);
            ps.setLong(1, ship.position.left - GalaxyConstants.VISIBLE_RANGE);
            ps.setLong(2, ship.position.left + GalaxyConstants.VISIBLE_RANGE);
            ps.setLong(3, ship.position.middle - GalaxyConstants.VISIBLE_RANGE);
            ps.setLong(4, ship.position.middle + GalaxyConstants.VISIBLE_RANGE);
            ps.setLong(5, ship.position.right - GalaxyConstants.VISIBLE_RANGE);
            ps.setLong(6, ship.position.right + GalaxyConstants.VISIBLE_RANGE);
            ps.setInt(7, solarSystemId);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    // Get results, but dont use them
                    rs.getInt(1);  // ship_id
                    rs.getLong(2); // position_x
                    rs.getLong(3); // position_y
                    rs.getLong(4); // position_z
                    rs.getInt(5);  // class_id
                }
            } finally {
                rs.close();
            }
        }
    }
    
    // TODO Get locations!
    
    public long run(Connection conn, int solarSystemId, Triple<Long, Long, Long> minPos,
        Triple<Long, Long, Long> maxPos) throws SQLException {
        ArrayList<Ship> ships = getShips(conn, solarSystemId, minPos, maxPos);
        getVisibleObjects(conn, solarSystemId, ships);
        return IDLE_SUCCESSFUL;
    }

}
