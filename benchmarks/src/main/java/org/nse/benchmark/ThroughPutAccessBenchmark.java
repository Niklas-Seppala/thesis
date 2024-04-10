package org.nse.benchmark;

import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.analyzers.EnglishAnalyzer;
import org.nse.thesis.wordindex.analyzers.IndexAnalyzer;
import org.nse.thesis.wordindex.ffm.FFMNativeHandles;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndexLibrary;
import org.nse.thesis.wordindex.jni.JNIWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndexBindings;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;
import org.openjdk.jmh.annotations.*;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class ThroughPutAccessBenchmark {
    static final int FORK = 5;
    static final String file = "testfiles/small.txt";
    static {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
        JNAWordIndexLibrary.Impl.load("build/libs/wordindex.so");
        FFMNativeHandles.load("build/libs/wordindex.so");
    }

    private WordIndex javaIndex;
    private WordIndex jniIndex;
    private WordIndex jnaIndex;
    private WordIndex ffmIndex;


    @Setup
    public void setup() throws FileNotFoundException {
        IndexAnalyzer analyzer = new  EnglishAnalyzer();
        javaIndex = new JavaWordIndex(file, analyzer);
        jniIndex = new JNIWordIndex(file, analyzer, 16, 8192, 256, true);
        jnaIndex = new JNAWordIndex(file, analyzer, 16, 8192, 256,true);
        ffmIndex = new FFMWordIndex(file, analyzer, 16, 8192, 256, true);
    }

    @TearDown
    public void teardown() throws Exception {
        if (javaIndex != null) {
            javaIndex.close();
        }
        if (jniIndex != null) {
            jniIndex.close();
        }
        if (jnaIndex != null) {
            jnaIndex.close();
        }
        if (ffmIndex != null) {
            ffmIndex.close();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = FORK, warmups = FORK)
    public Collection<String> POJO_indexWordAccess() {
        return javaIndex.getWords("Whereupon", WordIndex.ContextBytes.SMALL_CONTEXT);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = FORK, warmups = FORK)
    public Collection<String> JNI_indexWordAccess() {
        return jniIndex.getWords("Whereupon", WordIndex.ContextBytes.SMALL_CONTEXT);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = FORK, warmups = FORK)
    public Collection<String> JNA_indexWordAccess() {
        return jnaIndex.getWords("Whereupon", WordIndex.ContextBytes.SMALL_CONTEXT);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = FORK, warmups = FORK)
    public Collection<String> FFM_indexWordAccess() {
        return ffmIndex.getWords("Whereupon", WordIndex.ContextBytes.SMALL_CONTEXT);
    }
}
