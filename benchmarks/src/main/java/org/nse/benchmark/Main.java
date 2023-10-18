package org.nse.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Main {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ColdStartBenchmark.class.getSimpleName())
                .include(TroughPutBenchmark.class.getSimpleName())
                .include(DirectoryWordIndexBenchmark.class.getSimpleName())
                .build();

        new Runner(options).run();
    }
}
