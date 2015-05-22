package com.oltpbenchmark.benchmarks.galaxy.procedures;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import com.oltpbenchmark.api.Procedure;

public abstract class GalaxyProcedure extends Procedure {

    public abstract long run(Connection conn, int solarSystemId, ImmutableTriple<Long, Long, Long> minPos,
                                    ImmutableTriple<Long, Long, Long> maxPos, Random rng) throws SQLException;
}
