package org.ns.thesis.wordindex.ffm;

import org.jetbrains.annotations.NotNull;
import org.ns.thesis.wordindex.WordContextIterator;
import org.ns.thesis.wordindex.WordIndex;
import org.ns.thesis.wordindex.jni.JNIWordIndexBindings;

import java.io.FileNotFoundException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class FFMWordIndex implements WordIndex {

    public static final String FF_OPEN_NAME = "file_word_index_open";
    public static final String FF_CLOSE_NAME = "file_word_index_close";
    public static final String FF_CONTEXT_QUERY_NAME = "file_word_index_read_with_context_buffered";
    public static final String FF_ITER_CLOSE =
            "file_word_index_close_iterator";

    private final static int TERM_BUFFER_MARK = 0;
    private final static int MIN_QUERY_BUFFER_SIZE = 512;
    private final static int MIN_INDEXING_BUFFER_SIZE = 4096;
    private final static int MIN_WORD_CAPACITY_ESTIMATE = 64;

    private static final MethodHandle openIndex;
    private static final MethodHandle closeIndex;
    private static final MethodHandle closeIterator;
    private static final MethodHandle read;

    static  {
        System.load(Path.of("build/libs/wordindex.so").toAbsolutePath().toString());
        var lookup = SymbolLookup.loaderLookup();

        MemorySegment openAddr = lookup.lookup(FF_OPEN_NAME).orElseThrow();
        MemorySegment closeAddr = lookup.lookup(FF_CLOSE_NAME).orElseThrow();
        MemorySegment closeIter = lookup.lookup(FF_ITER_CLOSE).orElseThrow();
        MemorySegment readAddr = lookup.lookup(FF_CONTEXT_QUERY_NAME).orElseThrow();

        Linker linker = Linker.nativeLinker();

        openIndex = linker.downcallHandle(
                openAddr,
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_BOOLEAN)
        );

        closeIndex =
                linker.downcallHandle(
                        closeAddr,
                        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

        closeIterator = linker.downcallHandle(
                closeIter, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        read = linker.downcallHandle(
                readAddr, FunctionDescriptor.of(ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );

    }

    private final MemoryAddress handle;
    private final String filepath;

    private final int queryBufferSize;

    public FFMWordIndex(@NotNull final String path, long wordCapacityEstimate,
                        long indexingBufferSize, int queryBufferSize,
                        final boolean shouldCompact) throws FileNotFoundException {

        if (Files.notExists(Path.of(path))) {
            throw new FileNotFoundException(path);
        }
        this.filepath = path;
        this.queryBufferSize = queryBufferSize;

        if (wordCapacityEstimate < MIN_WORD_CAPACITY_ESTIMATE) {
            wordCapacityEstimate = MIN_WORD_CAPACITY_ESTIMATE;
        }

        if (indexingBufferSize < MIN_INDEXING_BUFFER_SIZE) {
            indexingBufferSize = MIN_INDEXING_BUFFER_SIZE;
        }

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment nativeText = session.allocateUtf8String(path);
            handle = (MemoryAddress) openIndex.invoke(
                    nativeText.address(),
                    wordCapacityEstimate,
                    indexingBufferSize, shouldCompact);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Collection<String> getWords(@NotNull String word, @NotNull ContextBytes ctx) {
        MemoryAddress nativeIterHandle = MemoryAddress.NULL;
        int wordBytesLength = word.getBytes(StandardCharsets.UTF_8).length;

        Collection<String> results = new ArrayList<>();
        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment nativeWord = session.allocateUtf8String(word);
            MemorySegment readBuffer = session.allocateArray(ValueLayout.JAVA_BYTE, this.queryBufferSize);
            do {
                nativeIterHandle = (MemoryAddress) read.invoke(
                        this.handle,
                        readBuffer, readBuffer.byteSize(), nativeWord, wordBytesLength,
                        ctx.size(),
                        nativeIterHandle);
                long offset = 0;
                while (true) {
                    int strOffset = readBuffer.get(ValueLayout.JAVA_INT, offset);
                    if (strOffset == TERM_BUFFER_MARK) {
                        break;
                    }
                    results.add(readBuffer.getUtf8String(offset));
                    offset += strOffset;
                }
            } while (!nativeIterHandle.equals(MemoryAddress.NULL));

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public @NotNull WordContextIterator iterateWords(@NotNull String word, @NotNull ContextBytes ctx) throws FileNotFoundException {
        return null;
    }

    @Override
    public void close() throws Exception {
        try {
            closeIndex.invoke(handle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to close native index", e);
        }
    }
}
