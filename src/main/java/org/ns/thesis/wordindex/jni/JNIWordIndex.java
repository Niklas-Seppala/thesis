package org.ns.thesis.wordindex.jni;

import org.jetbrains.annotations.NotNull;
import org.ns.thesis.wordindex.WordContextIterator;
import org.ns.thesis.wordindex.WordIndex;

import java.io.FileNotFoundException;
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
public class JNIWordIndex implements WordIndex {

    /**
     * 4 byte mark on query buffer that indicates there is
     * no more bytes to read.
     */
    private final static int TERM_BUFFER_MARK = 0;
    private final static int MIN_QUERY_BUFFER_SIZE = 512;
    private final static int MIN_INDEXING_BUFFER_SIZE = 4096;
    private final static int MIN_WORD_CAPACITY_ESTIMATE = 64;
    private static final long NULL_PTR = 0;

    static {
        // TODO: load native library somewhere else.
        System.load(Path.of("build/libs/wordindex.so").toAbsolutePath().toString());
    }

    private final long nativeHandle;
    private final String filepath;

    private final int queryBufferSize;

    /**
     * Create Java object that acts as a proxy for native WordIndex.
     * It is crucial that this object is closed when no longer used,
     * or Exception is thrown.
     *
     * @param path                 Path to text file to be indexed.
     * @param wordCapacityEstimate Estimate how many unique words file might contain.
     * @param indexingBufferSize   Suggested size of buffer that's used when indexing the file.
     * @param queryBufferSize      Suggested size of buffer that's used when querying
     *                             this index.
     * @param shouldCompact        Should index be compacted after indexing is done.
     *                             This will save memory on the long term, with initial
     *                             time cost.
     * @throws FileNotFoundException When file path is invalid.
     */
    public JNIWordIndex(@NotNull final String path, long wordCapacityEstimate,
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

        this.nativeHandle = JNIWordIndexBindings.wordIndexOpen(path, wordCapacityEstimate,
                indexingBufferSize,
                shouldCompact);
    }

    /**
     * @param preferredSize Size asked for
     * @param minSize       guarantee that buffer has ATLEAST this much room.
     * @return Direct buffer with native byte-order.
     */
    @NotNull
    private static ByteBuffer getNativeBuffer(int preferredSize, int minSize) {
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(
                Math.max(preferredSize, minSize));
        readBuffer.order(ByteOrder.nativeOrder());
        return readBuffer;
    }

    /**
     * Creates a native word index for specified file.
     * Remember to close it!
     *
     * @param filepath   Path to text file to be indexed.
     * @param capacity   Estimate how many unique words file might contain.
     * @param bufferSize Suggested size of buffer that's used when indexing the file.
     * @param compact    Should index be compacted after indexing is done.
     * @return Handle to native WordIndex
     */
    private static native long wordIndexOpen(String filepath, long capacity,
                                             long bufferSize, boolean compact);

    /**
     * Closes native WordIndex, releasing all native resources.
     *
     * @param handle Handle to native WordIndex
     */
    private static native void wordIndexClose(long handle);

    /**
     * Reads words with context from indexed file in buffered manner. If buffer was not
     * big enough to read all results, iterator handle is returned. Next call to this
     * method should use that iterator as a parameter to continue where previous call
     * left.
     * <p>
     * <h3>Buffer byte protocol</h3>
     * Strings are written to read buffer as contiguous span of bytes, lead by 4 byte
     * integer, specifying string byte length. TERM_BUFFER_MARK == no more strings left
     * in the buffer.
     * <p>
     * Example:
     * <pre>
     *     [3ace2of5spades..]
     * </pre>
     *
     * @param handle         Native WordIndex handle
     * @param readBuffer     Native buffer to read result into
     * @param readBufferSize Native buffer size
     * @param word           To search for
     * @param wordLen        Length of the word in bytes
     * @param context        Context surrounding the word in indexed file
     * @param wordIterator   Word file position iterator. Initially should be NULL (0),
     *                       after that, it should be the return value of this method.
     * @return Word file position iterator.
     */
    private static native long wordIndexReadWithContextBuffered(long handle,
                                                                ByteBuffer readBuffer,
                                                                long readBufferSize,
                                                                String word, int wordLen,
                                                                int context,
                                                                long wordIterator);

    /**
     * Closes native iterator, releasing its resources. Not closing it will cause a
     * memory leak, IF the iterator was not exhausted. So if you give up on reading, when
     * there's still results remaining, call this method!
     *
     * @param iterator WordIndex word file position iterator.
     */
    private static native void wordIndexCloseIterator(long iterator);

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
    @NotNull
    public Collection<String> getWords(@NotNull String word,
                                       @NotNull WordIndex.ContextBytes ctx) {
        long nativeIterHandle = NULL_PTR;

        int wordBytesLength = word.getBytes(StandardCharsets.UTF_8).length;
        int maxStrLength = (ctx.size() << 1) + wordBytesLength;
        byte[] str = new byte[maxStrLength];
        ByteBuffer readBuffer = getNativeBuffer(this.queryBufferSize,
                maxStrLength + Integer.BYTES);

        Collection<String> results = new ArrayList<>();
        do {
            nativeIterHandle = JNIWordIndexBindings.wordIndexReadWithContextBuffered(
                    this.nativeHandle,
                    readBuffer, readBuffer.capacity(), word, wordBytesLength, ctx.size(),
                    nativeIterHandle);
            while (true) {
                int offset = readBuffer.getInt();
                if (offset == TERM_BUFFER_MARK) {
                    break;
                }
                int length = 0;
                for (; length < offset; length++) {
                    str[length] = readBuffer.get();
                }
                String s = new String(str, 0, length, StandardCharsets.UTF_8);
                results.add(s);
            }
            readBuffer.rewind();
        } while (nativeIterHandle != NULL_PTR);

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
    @NotNull
    public WordContextIterator iterateWords(@NotNull String word,
                                            @NotNull WordIndex.ContextBytes ctx)
            throws FileNotFoundException {
        if (Files.notExists(Path.of(this.filepath))) {
            throw new FileNotFoundException(filepath);
        }
        return new NativeWordContextIterator(this.nativeHandle, word, ctx,
                this.queryBufferSize);
    }

    /**
     * Closes the index, releases native resources.
     *
     * @throws Exception It won't.
     */
    @Override
    public void close() throws Exception {
        JNIWordIndexBindings.wordIndexClose(this.nativeHandle);
    }

    /**
     * Reads query results lazily from off-heap memory and instantiates
     * String objects when required to do so.
     */
    private static class NativeWordContextIterator implements WordContextIterator {
        private final byte[] str;
        private final ByteBuffer buffer;
        private final String word;
        private final int wordLen;
        private final ContextBytes ctx;
        private final long indexHandle;
        private long iteratorHandle;


        private NativeWordContextIterator(long indexHandle, @NotNull String word,
                                          @NotNull WordIndex.ContextBytes ctx, int queryBufferSize) {
            if (indexHandle == NULL_PTR) {
                throw new IllegalStateException("Index is closed");
            }
            this.word = word;
            this.ctx = ctx;
            this.indexHandle = indexHandle;
            this.iteratorHandle = NULL_PTR;
            this.wordLen = word.getBytes(StandardCharsets.UTF_8).length;
            this.str = new byte[(this.ctx.size() << 1) + wordLen];
            this.buffer = getNativeBuffer(queryBufferSize, str.length + Integer.BYTES);
            this.readIntoBuffer();
        }

        /**
         * Closes this iterator, releasing native resources.
         */
        @Override
        public void close() {
            // If native method returned NULL_PTR, it already freed
            // existing iterator, or it never existed.
            if (this.iteratorHandle != NULL_PTR) {
                JNIWordIndexBindings.wordIndexCloseIterator(this.iteratorHandle);
            }
        }

        @Override
        public boolean hasNext() {
            if (this.bufferHasNext()) {
                return true;
            } else {
                if (this.iteratorHandle != NULL_PTR) {
                    readIntoBuffer();
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
        private void readIntoBuffer() {
            this.iteratorHandle =
                    JNIWordIndexBindings.wordIndexReadWithContextBuffered(this.indexHandle,
                            this.buffer, this.buffer.capacity(), word, wordLen,
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
