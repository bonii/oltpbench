/* This file is part of VoltDB.
 * Copyright (C) 2008-2012 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

//
// Accepts a vote, enforcing business logic: make sure the vote is for a valid
// congalaxyant and that the voter (phone number of the caller) is not above the
// number of allowed votes.
//

package com.oltpbenchmark.benchmarks.galaxy.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Math;
import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.galaxy.GalaxyConstants;

public class Move extends Procedure {

    // potential return codes
    public static final long MOVE_SUCCESSFUL = 0;
    public static final long ERR_INVALID_SHIP = 1;
    public static final long ERR_INVALID_CLASS = 2;
    public static final long ERR_INVALID_SOLARSYSTEM = 3;

    // Get ship entry
    public final SQLStmt getShipStmt = new SQLStmt(
	"SELECT * FROM " + GalaxyConstants.TABLENAME_SHIPS  +" WHERE sid = ?;"
    );

    // Get ship class entry
    public final SQLStmt getClassStmt = new SQLStmt(
        "SELECT * FROM " + GalaxyConstants.TABLENAME_CLASSES + " WHERE cid = ?;"
    );

    // Get solarsystem entry
    public final SQLStmt getSolarStmt = new SQLStmt(
        "SELECT x_max, y_max FROM " + GalaxyConstants.TABLENAME_SOLARSYSTEMS + " WHERE ssid = ?;"
    );

    // Check single tile if free
    public final SQLStmt checkTileStmt = new SQLStmt(
        "SELECT x, y FROM " + GalaxyConstants.TABLENAME_SHIPS + " WHERE x = ? AND y = ?;"
    );

    // Update ship position
    public final SQLStmt updateShipPosStmt = new SQLStmt(
        "UPDATE " + GalaxyConstants.TABLENAME_SHIPS + " SET x = ?, y = ? WHERE sid = ?;"
    );

    public long run(Connection conn, int shipId, int move_x, int move_y) throws SQLException {

        PreparedStatement ps = getPreparedStatement(conn, getShipStmt);
        ps.setInt(1, shipId);
        ResultSet rs = ps.executeQuery();
        int x;
        int y;
        int cid;
        int ssid;
        try {
            if (!rs.next()) {
                System.out.println("Ship: " + Integer.toString(shipId) + "\n");
                return ERR_INVALID_SHIP;
            } else {
                x = rs.getInt(2);
                y = rs.getInt(3);
                cid = rs.getInt(4);
                ssid = rs.getInt(5);
            }
        } finally {
            rs.close();
        }

        ps = getPreparedStatement(conn, getClassStmt);
        ps.setInt(1, cid);
        rs = ps.executeQuery();

        int reachability;
        try {
            if (!rs.next()) {
                System.out.println("Class: " + Integer.toString(cid) + "\n");
                return ERR_INVALID_CLASS;
            } else {
                reachability = rs.getInt(3);
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

        ps = getPreparedStatement(conn, getSolarStmt);
        ps.setInt(1, ssid);
        rs = ps.executeQuery();

        int x_max;
        int y_max;
        try {
            if (!rs.next()) {
                System.out.println("Solarsystem: " + Integer.toString(ssid) + "\n");
                return ERR_INVALID_SOLARSYSTEM;
            } else {
                x_max = rs.getInt(1);
                y_max = rs.getInt(2);
            }
        } finally {
            rs.close();
        }

        int new_x = Math.min(x_max, x + move_x);
        int new_y = Math.min(y_max, y + move_y);
        ps = getPreparedStatement(conn, checkTileStmt);
        ps.setInt(1, new_x);
        ps.setInt(2, new_y);
        rs = ps.executeQuery();

        try {
            if (rs.next()) {
                new_x = x;
                new_y = y;
            }
        } finally {
            rs.close();
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
