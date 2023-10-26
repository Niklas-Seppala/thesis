package org.nse.thesis.wordindex.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

/**
 * Container record that holds function handles to native library.
 * <h3>Usage</h3>
 * <ol>
 *      <li> Load the library with {@link FFMNativeHandles#load(String)} to load the library before
 *           accessing the singleton </li>
 *      <li>Use the {@link FFMNativeHandles#get()} to get the singleton instance.</li>
 * </ol>
 * <p>
 *     <b>OR</b>
 *     <br><br>
 *     Do both with {@link FFMNativeHandles#get(String)} and pass the path to the library first time.
 * </p>
 *
 * @param openIndex     Open index function-handle
 * @param closeIndex    Close index function-handle
 * @param closeIterator Close iterator function-handle
 * @param query         query function-handle
 * @author Niklas Seppälä
 */
public record FFMNativeHandles(MethodHandle openIndex, MethodHandle closeIndex,
                               MethodHandle closeIterator, MethodHandle query) {
    public static final String NATIVE_FUNCTION_OPEN_NAME = "file_word_index_open";
    public static final String NATIVE_FUNCTION_CLOSE_NAME = "file_word_index_close";
    public static final String NATIVE_FUNCTION_CONTEXT_QUERY_NAME = "file_word_index_read_with_context_buffered";
    public static final String NATIVE_FUNCTION_ITERATOR_CLOSE = "file_word_index_close_iterator";

    private volatile static FFMNativeHandles INSTANCE;

    /**
     * @return The singleton instance of native function handles
     * container.
     */
    public static FFMNativeHandles get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Library is not loaded");
        }
        return INSTANCE;
    }

    /**
     * Gets the singleton, but makes sure it's loaded it first.
     *
     * @param libPath path to shared native library.
     * @return The singleton instance.
     */
    public static FFMNativeHandles get(String libPath) {
        load(libPath);
        return INSTANCE;
    }

    /**
     * Loads the native shared library.
     *
     * @param libPath Path to the library
     */
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

    /**
     * @param linker Linker used in getting a handle to function
     * @param lookup Lookup
     * @return Handle to the query function,
     */
    private static MethodHandle getQueryMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment readAddress = lookup.lookup(NATIVE_FUNCTION_CONTEXT_QUERY_NAME).orElseThrow();
        return linker.downcallHandle(
                readAddress, FunctionDescriptor.of(ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
    }

    /**
     * @param linker Linker used in getting a handle to the function
     * @param lookup Lookup
     * @return Handle to the close_iterator function
     */
    private static MethodHandle getCloseIteratorMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment closeIteratorAddress = lookup.lookup(NATIVE_FUNCTION_ITERATOR_CLOSE).orElseThrow();
        return linker.downcallHandle(
                closeIteratorAddress, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
    }

    /**
     * @param linker Linker used in getting a handle to function.
     * @param lookup Lookup
     * @return Handle to the close_index function.
     */
    private static MethodHandle getCloseIndexMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment closeAddress = lookup.lookup(NATIVE_FUNCTION_CLOSE_NAME).orElseThrow();
        return linker.downcallHandle(
                closeAddress,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    }

    /**
     * @param linker Linker used in getting a handle to the function.
     * @param lookup Lookup
     * @return Handle to the open_index method.
     */
    private static MethodHandle getOpenIndexMethodHandle(Linker linker, SymbolLookup lookup) {
        MemorySegment openAddress = lookup.lookup(NATIVE_FUNCTION_OPEN_NAME).orElseThrow();
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
}
