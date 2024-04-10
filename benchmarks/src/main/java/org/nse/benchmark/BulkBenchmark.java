package org.nse.benchmark;

import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.analyzers.EnglishAnalyzer;
import org.nse.thesis.wordindex.ffm.FFMNativeHandles;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndexLibrary;
import org.nse.thesis.wordindex.jni.JNIWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndexBindings;
import org.nse.thesis.wordindex.pojo.BufferedJavaWordIndex;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class BulkBenchmark {
    private static final int FORK = 5;
    private static final int BULK = 64;
    static {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
        JNAWordIndexLibrary.Impl.load("build/libs/wordindex.so");
        FFMNativeHandles.load("build/libs/wordindex.so");
    }

    static final String file = "testfiles/bible.txt";

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public void POJO_bulkCreate(Blackhole bh) throws Exception {
        WordIndex[] results = new WordIndex[BULK];
        for (int i = 0; i < results.length; i++) {
            results[i] = new JavaWordIndex(file, new EnglishAnalyzer(), 10000);
        }
        for (int i = 0; i < results.length; i++) {
            WordIndex index = results[i];
            index.close();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public void POJO_BUFFERED_bulkCreate(Blackhole bh) throws Exception {
        WordIndex[] results = new WordIndex[BULK];
        for (int i = 0; i < results.length; i++) {
            results[i] = new BufferedJavaWordIndex(file, new EnglishAnalyzer(), 10000);
        }
        for (int i = 0; i < results.length; i++) {
            WordIndex index = results[i];
            index.close();
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public void JNI_bulkCreate(Blackhole bh) throws Exception {
        WordIndex[] results = new WordIndex[BULK];
        for (int i = 0; i < results.length; i++) {
            results[i] = new JNIWordIndex(file, new EnglishAnalyzer(), 10000, 8192, 256,
                    false);
        }
        for (int i = 0; i < results.length; i++) {
            WordIndex index = results[i];
            index.close();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public void JNA_bulkCreate(Blackhole bh) throws Exception {
        WordIndex[] results = new WordIndex[BULK];
        for (int i = 0; i < results.length; i++) {
            results[i] = new JNAWordIndex(file, new EnglishAnalyzer(), 10000, 8192, 256, false);
        }
        for (int i = 0; i < results.length; i++) {
            WordIndex index = results[i];
            index.close();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = FORK, warmups = FORK)
    public void FFM_bulkCreate(Blackhole bh) throws Exception {
        WordIndex[] results = new WordIndex[BULK];
        for (int i = 0; i < results.length; i++) {
            results[i] = new FFMWordIndex(file, new EnglishAnalyzer(), 10000, 8192, 256, false);
        }
        for (int i = 0; i < results.length; i++) {
            WordIndex index = results[i];
            index.close();
        }
    }
}
