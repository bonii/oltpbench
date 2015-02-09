package com.oltpbenchmark.benchmarks.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.util.SQLUtil;

public class TestLoader extends Loader {

    // Ship classes data
    private static final String[] classes = new String[] {
      "Missile boat", "Torpedo boat", "Minesweeper", "Medium landing craft", "Landing Ship Tank" };

    private static final int[] reachability = {200, 450, 900, 3000, 1200};

    public TestLoader(TestBenchmark benchmark, Connection conn) {
        super(benchmark, conn);
    }

    @Override
    public void load() throws SQLException {

        Table tbl = getTableCatalog(TestConstants.TABLENAME_CLASSES);
        PreparedStatement ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, false));
        for (int i = 0; i < reachability.size(); i++) {
            ps.setInt(1, i + 1); // Class ID (cid)
            ps.setString(2, classes[i]); // Class name
            ps.setInt(3, reachability[i]); // Reachability
            ps.addBatch();
        }
        ps.executeBatch();

        Random rng = new Random();
        tbl = getTableCatalog(TestConstants.TABLENAME_SOLARSYSTEMS);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, false));
        for (int i = 0; i < TestConstants.NUM_SOLARSYSTEMS; i++) {
            ps.setInt(1, i + 1]); // Solarsystem ID(ssid)
            ps.setString(2, rng.nextInt(TestConstants.SOLARSYSTEM_MAX_SIZE)); // x max
            ps.setString(3, rng.nextInt(TestConstants.SOLARSYSTEM_MAX_SIZE)); // y max
            ps.addBatch();
        }
        ps.executeBatch();

        tbl = getTableCatalog(TestConstants.TABLENAME_SHIPS);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, false));
        for  (int i = 0; i < TestConstants.NUM_SOLARSYSTEMS; i++) {
           ps.setInt(1, i + 1); // Ship ID(sid)
           ps.setInt(2,
    }

}
