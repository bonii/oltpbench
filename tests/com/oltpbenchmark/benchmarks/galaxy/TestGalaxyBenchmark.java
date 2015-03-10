package com.oltpbenchmark.benchmarks.galaxy;

import com.oltpbenchmark.api.AbstractTestBenchmarkModule;
import com.oltpbenchmark.benchmarks.galaxy.procedures.*;

/**
 * A class containing the benchmark and the list of procedures to test
 */
public class TestGalaxyBenchmark extends AbstractTestBenchmarkModule<GalaxyBenchmark> {

    public static final Class<?> PROC_CLASSES[] = {
        Move.class,
    };

	@Override
	protected void setUp() throws Exception {
		super.setUp(GalaxyBenchmark.class, PROC_CLASSES);
	}

}
