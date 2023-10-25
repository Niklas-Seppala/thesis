package org.nse.benchmark;


import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.ffm.FFMNativeHandles;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndexLibrary;
import org.nse.thesis.wordindex.jni.JNIWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndexBindings;
import org.nse.thesis.wordindex.pojo.ImprovedJavaWordIndex;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;
import org.nse.thesis.wordindex.pojo.WhitespaceTextAnalyzer;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

public class ColdStartBenchmark {

    static final String file = "testfiles/bible10x.txt";

    static {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
        JNAWordIndexLibrary.Impl.load("build/libs/wordindex.so");
        FFMNativeHandles.load("build/libs/wordindex.so");
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex coldStartPOJOIndexCreation() throws Exception {
        try (WordIndex index = new JavaWordIndex(file, new WhitespaceTextAnalyzer())) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex coldStartPOJOImprovedIndexCreation() throws Exception {
        try (WordIndex index = new ImprovedJavaWordIndex(file, new WhitespaceTextAnalyzer())) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex coldStartJNIIndexCreation() throws Exception {
        try (WordIndex index = new JNIWordIndex(file, new WhitespaceTextAnalyzer(),
                16, 64, 10, true)) {
            return index;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex ColdStartJNAIndexCreation() throws Exception {
        try (WordIndex index = new JNAWordIndex(file, new WhitespaceTextAnalyzer(),
                16, 64, 10, true)) {
            return index;
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public WordIndex coldStartFFMIndexCreation() throws Exception {
        try (WordIndex index = new FFMWordIndex(file, new WhitespaceTextAnalyzer(),
                16, 64, 10, true)) {
            return index;
        }
    }
}
