package com.oltpbenchmark.benchmarks.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.util.SQLUtil;

public class TestLoader extends Loader {

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
            ps.setString(2, x_max[i]); // x max
            ps.setString(3, y_max[i]); // y max
            ps.addBatch();
        }
        ps.executeBatch();

        tbl = getTableCatalog(TestConstants.TABLENAME_SHIPS);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, false));
        for  (int i = 0; i < TestConstants.NUM_SHIPS; i++) {
            ssid = rng.nextInt(TestConstants.NUM_SOLARSYSTEMS) + 1;
            x = rng.nextInt(x_max[ssid - 1]);
            y = rng.nextInt(y_max[ssid - 1]);

            ps.setInt(1, i + 1);  // Ship ID(sid)
            ps.setInt(2, x);      // x
            ps.setInt(3, y);      // y
            ps.setInt(3, rng.nextInt(TestConstants.NUM_CLASSES) + 1); // Class ID(cid)
            ps.setInt(4, ssid);   // Solarsystem ID(ssid)
    }

}
