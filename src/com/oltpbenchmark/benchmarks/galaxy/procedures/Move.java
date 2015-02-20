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

public class Move extends Procedure {
    
    private class Tuple {
        public int x;
        public int y;
        
        public Tuple(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        
        public boolean equals(Tuple other) {
            return this.x == other.x && this.y == other.y;
        }
    }

    // potential return codes
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

    public long run(Connection conn, int shipId, int move_x, int move_y) 
            throws SQLException {

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
        ps = getPreparedStatement(conn, checkTileStmt);
        ps.setInt(1, new_x);
        ps.setInt(2, new_x);
        ps.setInt(3, new_y);
        ps.setInt(4, new_y);
        ps.setInt(5, ssid);
        ps.setInt(6, shipId);
        rs = ps.executeQuery();

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

        if (possibles.size() == 0) { // There are no free spaces
            return MOVE_NOT_SUCCESSFUL;
        } else { // Select a random position of the possibles
            Tuple rand = possibles.get(new Random().nextInt(possibles.size()));
            new_x = rand.x;
            new_y = rand.y;
        }
        
        ps = getPreparedStatement(conn, updateShipPosStmt);
        ps.setInt(1, new_x);
        ps.setInt(2, new_y);
        ps.setInt(3, shipId);
        ps.execute();

        // Set the return value to 0: successful move
        return MOVE_SUCCESSFUL;
    }
}
