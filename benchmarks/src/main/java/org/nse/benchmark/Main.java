package org.nse.benchmark;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Main {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ColdStartBenchmark.class.getSimpleName())
                .include(ThroughPutAccessBenchmark.class.getSimpleName())
                .include(BulkBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .build();

        new Runner(options).run();
    }
}
