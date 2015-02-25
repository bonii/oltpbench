package com.oltpbenchmark.benchmarks.galaxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.types.DatabaseType;
import com.oltpbenchmark.util.SQLUtil;

/**
 * A class that prepares the database for the benchmark
 */
public class GalaxyLoader extends Loader {

    /**
     * Creates a new instance of the GalaxyLoader class
     * @param benchmark The benchmark class the loader is called from
     * @param conn The connection to the database to be prepared
     */
    public GalaxyLoader(GalaxyBenchmark benchmark, Connection conn) {
        super(benchmark, conn);
    }

    @Override
    public void load() throws SQLException {
        boolean escapeNames = false;
        if (this.getDatabaseType() == DatabaseType.POSTGRES) {
            escapeNames = false;
        }
        if (this.getDatabaseType() == DatabaseType.SQLITE) {
            escapeNames = true;
        }

        Table tbl = getTableCatalog(GalaxyConstants.TABLENAME_CLASSES);
        PreparedStatement ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, escapeNames));
        for (int i = 0; i < GalaxyConstants.reachability.length; i++) {
            ps.setInt(1, i + 1); // Class ID (cid)
            ps.setString(2, GalaxyConstants.classes[i]); // Class name
            ps.setInt(3, GalaxyConstants.reachability[i]); // Reachability
            ps.addBatch();
        }
        ps.executeBatch();

        Random rng = new Random();
        tbl = getTableCatalog(GalaxyConstants.TABLENAME_SOLARSYSTEMS);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, escapeNames));
        for (int i = 0; i < GalaxyConstants.NUM_SOLARSYSTEMS; i++) {
            ps.setInt(1, i + 1); // Solarsystem ID(ssid)
            ps.setInt(2, GalaxyConstants.x_max[i]); // x max
            ps.setInt(3, GalaxyConstants.y_max[i]); // y max
            ps.addBatch();
        }
        ps.executeBatch();

        tbl = getTableCatalog(GalaxyConstants.TABLENAME_SHIPS);
        ps = this.conn.prepareStatement(SQLUtil.getInsertSQL(tbl, escapeNames));
        for  (int i = 0; i < GalaxyConstants.NUM_SHIPS; i++) {
            int ssid = rng.nextInt(GalaxyConstants.NUM_SOLARSYSTEMS) + 1;
            int x = rng.nextInt(GalaxyConstants.x_max[ssid - 1]);
            int y = rng.nextInt(GalaxyConstants.y_max[ssid - 1]);

            ps.setInt(1, i + 1);  // Ship ID(sid)
            ps.setInt(2, x);      // x
            ps.setInt(3, y);      // y
            ps.setInt(4, rng.nextInt(GalaxyConstants.NUM_CLASSES) + 1); // Class ID(cid)
            ps.setInt(5, ssid);   // Solarsystem ID(ssid)
            ps.addBatch();
        }
        ps.executeBatch();
    }

}