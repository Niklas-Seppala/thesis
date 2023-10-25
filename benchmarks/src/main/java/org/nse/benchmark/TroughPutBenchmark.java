package org.nse.benchmark;


import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.ffm.FFMNativeHandles;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndexLibrary;
import org.nse.thesis.wordindex.jni.JNIWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndexBindings;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;
import org.nse.thesis.wordindex.pojo.WhitespaceTextAnalyzer;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

public class TroughPutBenchmark {

    static {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
        JNAWordIndexLibrary.Impl.load("build/libs/wordindex.so");
        FFMNativeHandles.load("build/libs/wordindex.so");
    }
    static final String file = "testfiles/small.txt";

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex POJOIndexCreation() throws Exception {
        try (WordIndex index = new JavaWordIndex(file, new WhitespaceTextAnalyzer())) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex POJOImprovedIndexCreation() throws Exception {
        try (WordIndex index = new JavaWordIndex(file, new WhitespaceTextAnalyzer())) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex JNIIndexCreation() throws Exception {
        try (WordIndex index = new JNIWordIndex(file, new WhitespaceTextAnalyzer(),
                16, 64, 10, true)) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex JNAIndexCreation() throws Exception {
        try (WordIndex index = new JNAWordIndex(file, new WhitespaceTextAnalyzer(),
                16, 64, 10, true)) {
            return index;
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex FFMIndexCreation() throws Exception {
        try (WordIndex index = new FFMWordIndex(file, new WhitespaceTextAnalyzer(),
                16, 64, 10, true)) {
            return index;
        }
    }
}
