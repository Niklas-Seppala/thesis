package org.nse.benchmark;


import java.util.concurrent.TimeUnit;

import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndexBindings;
import org.nse.thesis.wordindex.pojo.ImprovedJavaWordIndex;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

public class ColdStartBenchmark {
    static final int FORK = 1;

    static {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
    }

    static final String file = "testfiles/bible.txt";

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public WordIndex POJO_coldStartIndexing() throws Exception {
        try (WordIndex index = new JavaWordIndex(file)) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public WordIndex POJO_OPTIMIZED_coldStartIndexing() throws Exception {
        try (WordIndex index = new ImprovedJavaWordIndex(file)) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public WordIndex JNI_coldStartIndexing() throws Exception {
        try (WordIndex index = new JNIWordIndex(file, 16, 64, 10, true)) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public WordIndex JNA_coldStartIndexing() throws Exception {
        try (WordIndex index = new JNAWordIndex(file, 16, 64, 10, true)) {
            return index;
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public WordIndex FFM_coldStartIndexing() throws Exception {
        try (WordIndex index = new FFMWordIndex(file, 16, 64, 10, true)) {
            return index;
        }
    }
}
