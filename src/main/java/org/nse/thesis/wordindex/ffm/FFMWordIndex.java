package org.nse.thesis.wordindex.ffm;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.analyzers.IndexAnalyzer;

import java.io.FileNotFoundException;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Index that indexes words to their positions in the file. File is indexed, and queried
 * using native library.
 *
 * @author Niklas Seppälä
 */
public class FFMWordIndex implements WordIndex {
    private final MemoryAddress handle;
    private final String filepath;
    private final int queryBufferSize;

    /**
     * Create Java object that acts as a proxy for native WordIndex.
     * It is crucial that this object is closed when no longer used,
     * or Exception is thrown.
     *
     * @param path                 Path to text file to be indexed.
     * @param analyzer             Analyzer used in tokenizing words from text.
     * @param wordCapacityEstimate Estimate how many unique words file might contain.
     * @param indexingBufferSize   Suggested size of buffer that's used when indexing the file.
     * @param queryBufferSize      Suggested size of buffer that's used when querying
     *                             this index.
     * @param shouldCompact        Should index be compacted after indexing is done.
     *                             This will save memory on the long term, with initial
     *                             time cost.
     * @throws FileNotFoundException When file path is invalid.
     */
    public FFMWordIndex(@NotNull final String path, @NotNull IndexAnalyzer analyzer,
                        long wordCapacityEstimate,
                        long indexingBufferSize, int queryBufferSize,
                        final boolean shouldCompact) throws FileNotFoundException {

        if (Files.notExists(Path.of(path))) {
            throw new FileNotFoundException(path);
        }
        this.filepath = path;
        this.queryBufferSize = Math.max(queryBufferSize, MIN_QUERY_BUFFER_SIZE);

        if (wordCapacityEstimate < MIN_WORD_CAPACITY_ESTIMATE) {
            wordCapacityEstimate = MIN_WORD_CAPACITY_ESTIMATE;
        }

        if (indexingBufferSize < MIN_INDEXING_BUFFER_SIZE) {
            indexingBufferSize = MIN_INDEXING_BUFFER_SIZE;
        }

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment nativeFilePath = session.allocateUtf8String(path);
            handle = (MemoryAddress) FFMNativeHandles.get().openIndex().invoke(
                    nativeFilePath,
                    analyzer.asNative(),
                    wordCapacityEstimate,
                    indexingBufferSize, shouldCompact);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Query the index for all occurrences of words from indexed file, with specified
     * amount of context, on both sides of the word.
     * <pre>
     *     [ctx word ctx]
     * </pre>
     *
     * @param word Word to search for
     * @param ctx  The amount of context bytes to surround the word.
     * @return Collection of words with context.
     */
    @Override
    public @NotNull Collection<String> getWords(@NotNull String word, @NotNull ContextBytes ctx) {
        MemoryAddress nativeResultIteratorPointer = MemoryAddress.NULL;

        int wordBytesLength = word.getBytes(StandardCharsets.UTF_8).length;
        byte[] strBytes = new byte[(ctx.size() << 1) + wordBytesLength];

        Collection<String> results = new ArrayList<>();
        try (MemorySession session = MemorySession.openConfined()) {

            // Allocate native memory, tied to session, and scope.
            MemorySegment nativeWord = session.allocateUtf8String(word);
            MemorySegment readBuffer = session.allocateArray(ValueLayout.JAVA_BYTE,
                    this.queryBufferSize);
            ByteBuffer bb = readBuffer.asByteBuffer();
            bb.order(ByteOrder.nativeOrder());

            do {
                nativeResultIteratorPointer = (MemoryAddress) FFMNativeHandles.get().query().invoke(
                        this.handle,
                        readBuffer, readBuffer.byteSize(), nativeWord, wordBytesLength,
                        ctx.size(),
                        nativeResultIteratorPointer);
                while (true) {
                    int offset = bb.getInt();
                    if (offset == TERM_BUFFER_MARK) {
                        break;
                    }
                    int length = 0;
                    for (; length < offset; length++) {
                        strBytes[length] = bb.get();
                    }
                    String s = new String(strBytes, 0, length, StandardCharsets.UTF_8);
                    results.add(s);
                }
                bb.rewind();
            } while (!nativeResultIteratorPointer.equals(MemoryAddress.NULL));


            // Native memory gets freed.
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    /**
     * Queries the index with word with context, results can be accessed trough lazy
     * iterator.
     *
     * @param word Word to search for
     * @param ctx  The amount of context to surround the word.
     * @return Lazy iterator for query results.
     */
    @Override
    public @NotNull WordContextIterator iterateWords(@NotNull String word, @NotNull ContextBytes ctx)
            throws FileNotFoundException {
        if (Files.notExists(Path.of(this.filepath))) {
            throw new FileNotFoundException(filepath);
        }
        return new NativeWordContextIterator(this.handle, word, ctx,
                this.queryBufferSize);
    }

    @Override
    public void close() {
        try {
            FFMNativeHandles.get().closeIndex().invoke(handle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to close native index", e);
        }
    }

    /**
     * Reads query results lazily from off-heap memory and instantiates
     * String objects when required to do so.
     */
    private static class NativeWordContextIterator implements WordContextIterator {
        private final byte[] str;

        private final MemorySession session;
        private final MemorySegment underlyingBuffer;
        private final ByteBuffer buffer;
        private final MemorySegment wordSegment;
        private final int wordLen;
        private final ContextBytes ctx;
        private final MemoryAddress indexHandle;
        private MemoryAddress iteratorHandle;


        private NativeWordContextIterator(MemoryAddress indexHandle, @NotNull String word,
                                          @NotNull WordIndex.ContextBytes ctx, int queryBufferSize) {
            if (indexHandle.equals(MemoryAddress.NULL)) {
                throw new IllegalStateException("Index is closed");
            }
            this.ctx = ctx;
            this.indexHandle = indexHandle;
            this.iteratorHandle = MemoryAddress.NULL;
            this.wordLen = word.getBytes(StandardCharsets.UTF_8).length;
            this.str = new byte[(this.ctx.size() << 1) + wordLen];
            this.session = MemorySession.openConfined();
            this.underlyingBuffer = session.allocateArray(ValueLayout.JAVA_BYTE,
                    queryBufferSize);
            this.wordSegment = session.allocateUtf8String(word);
            this.buffer = underlyingBuffer.asByteBuffer();
            this.buffer.order(ByteOrder.nativeOrder());

            try {
                this.readIntoBuffer();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        /**
         * Closes this iterator, releasing native resources.
         */
        @Override
        public void close() {
            // Free read buffer memory
            this.session.close();
            // If native method returned NULL_PTR, it already freed
            // existing iterator, or it never existed.
            if (!this.iteratorHandle.equals(MemoryAddress.NULL)) {
                try {
                    FFMNativeHandles.get().closeIterator().invoke(this.iteratorHandle);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (this.bufferHasNext()) {
                return true;
            } else {
                if (!this.iteratorHandle.equals(MemoryAddress.NULL)) {
                    try {
                        readIntoBuffer();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                    return this.bufferHasNext();
                } else {
                    return false;
                }
            }
        }

        @Override
        public String next() {
            final int offset = buffer.getInt();
            int length = 0;
            for (; length < offset; length++) {
                this.str[length] = this.buffer.get();
            }
            return new String(this.str, 0, length, StandardCharsets.UTF_8);
        }

        /**
         * @return True if current buffer has a string to read.
         */
        private boolean bufferHasNext() {
            buffer.mark();
            int nextStringLength = buffer.getInt();
            buffer.reset();
            return nextStringLength != TERM_BUFFER_MARK;
        }

        /**
         * Reads words with context into buffer from native code.
         * Resets the buffer position to ZERO.
         */
        private void readIntoBuffer() throws Throwable {
            this.iteratorHandle =
                    (MemoryAddress) FFMNativeHandles.get().query().invoke(this.indexHandle,
                            this.underlyingBuffer, this.buffer.capacity(), wordSegment,
                            wordLen,
                            ctx.size(),
                            this.iteratorHandle);
            buffer.rewind();
        }

        @Override
        public Stream<String> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    this, Spliterator.ORDERED), false);
        }
    }
}
