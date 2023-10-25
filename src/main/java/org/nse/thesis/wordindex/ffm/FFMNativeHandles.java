package org.nse.thesis.wordindex.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

/**
 *
 * @param openIndex
 * @param closeIndex
 * @param closeIterator
 * @param query
 */
public record FFMNativeHandles(MethodHandle openIndex, MethodHandle closeIndex,
                        MethodHandle closeIterator, MethodHandle query) {
    public static final String FF_OPEN_NAME = "file_word_index_open";
    public static final String FF_CLOSE_NAME = "file_word_index_close";
    public static final String FF_CONTEXT_QUERY_NAME = "file_word_index_read_with_context_buffered";
    public static final String FF_ITER_CLOSE = "file_word_index_close_iterator";

    private volatile static FFMNativeHandles INSTANCE;

    public static void load(String libPath) {
        if (INSTANCE == null) {
            synchronized (FFMNativeHandles.class) {
                if (INSTANCE == null) {
                    System.load(Path.of(libPath).toAbsolutePath().toString());

                    SymbolLookup lookup = SymbolLookup.loaderLookup();
                    Linker linker = Linker.nativeLinker();

                    MethodHandle openIndex = getOpenIndexMethodHandle(linker, lookup);
                    MethodHandle closeIndex = getCloseIndexMethodHandle(linker, lookup);
                    MethodHandle closeIterator = getCloseIteratorMethodHandle(linker, lookup);
                    MethodHandle query = getQueryMethodHandle(linker, lookup);

                    INSTANCE = new FFMNativeHandles(openIndex, closeIndex, closeIterator, query);
                }
            }
        }
    }

    private static MethodHandle getQueryMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment readAddress = lookup.lookup(FF_CONTEXT_QUERY_NAME).orElseThrow();
        return linker.downcallHandle(
                readAddress, FunctionDescriptor.of(ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
    }

    private static MethodHandle getCloseIteratorMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment closeIteratorAddress = lookup.lookup(FF_ITER_CLOSE).orElseThrow();
        return linker.downcallHandle(
                closeIteratorAddress, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
    }

    private static MethodHandle getCloseIndexMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment closeAddress = lookup.lookup(FF_CLOSE_NAME).orElseThrow();
        return linker.downcallHandle(
                        closeAddress,
                        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    }

    private static MethodHandle getOpenIndexMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment openAddress = lookup.lookup(FF_OPEN_NAME).orElseThrow();
        return linker.downcallHandle(
                openAddress,
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_BOOLEAN)
        );
    }

    public static FFMNativeHandles get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Library is not loaded");
        }
        return INSTANCE;
    }

    public static FFMNativeHandles get(String libPath) {
        load(libPath);
        return INSTANCE;
    }
}
