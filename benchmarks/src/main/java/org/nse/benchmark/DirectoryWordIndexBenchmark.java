package org.nse.benchmark;

import org.nse.thesis.wordindex.DirectoryWordIndex;
import org.nse.thesis.wordindex.analyzers.EnglishAnalyzer;
import org.nse.thesis.wordindex.ffm.FFMNativeHandles;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndexLibrary;
import org.nse.thesis.wordindex.jni.JNIWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndexBindings;
import org.nse.thesis.wordindex.pojo.ImprovedJavaWordIndex;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class DirectoryWordIndexBenchmark {

    static {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
        JNAWordIndexLibrary.Impl.load("build/libs/wordindex.so");
        FFMNativeHandles.load("build/libs/wordindex.so");
    }


    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void coldStartIndexDirectoryPOJOImproved(Blackhole bh) throws Exception {
        DirectoryWordIndex index = new DirectoryWordIndex("testfiles", new EnglishAnalyzer(),
                ImprovedJavaWordIndex::new);
        index.close();
        bh.consume(index);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void coldStartIndexDirectoryPOJO(Blackhole bh) throws Exception {
        DirectoryWordIndex index = new DirectoryWordIndex("testfiles", new EnglishAnalyzer(),
                JavaWordIndex::new);
        index.close();
        bh.consume(index);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void coldStartIndexDirectoryJNA(Blackhole bh) throws Exception {
        DirectoryWordIndex index = new DirectoryWordIndex("testfiles",
                new EnglishAnalyzer(),
                (path, analyzer) -> new JNAWordIndex(path, analyzer, 1 << 8,
                        8192, 4096, true));
        index.close();
        bh.consume(index);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void coldStartIndexDirectoryFFM(Blackhole bh) throws Exception {
        DirectoryWordIndex index = new DirectoryWordIndex("testfiles", new EnglishAnalyzer(),
                (path, analyzer) -> new FFMWordIndex(path, analyzer, 1 << 8,
                        8192, 4096, true));
        index.close();
        bh.consume(index);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void coldStartIndexDirectoryJNI(Blackhole bh) throws Exception {
        DirectoryWordIndex index = new DirectoryWordIndex("testfiles", new EnglishAnalyzer(),
                (path, analyzer) -> new JNIWordIndex(path, analyzer, 1 << 8,
                        8192, 4096, true));
        index.close();
        bh.consume(index);
    }
}
