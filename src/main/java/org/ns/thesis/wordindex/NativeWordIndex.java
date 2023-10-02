package org.ns.thesis.wordindex;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;

public class NativeWordIndex implements WordIndex {
    private final static int TERM_BUFFER_MARK  = 0;
    private final static int QUERY_BUFFER_SIZE = 4096;
    private static final long NULL = 0;


    private final ByteBuffer readBuffer;
    private final long handle;

    public NativeWordIndex(String filepath, long capacity, long bufferSize,
                           boolean compact) {
        this.handle = NativeWordIndex.wordIndexOpen(filepath, capacity, bufferSize,
                compact);
        this.readBuffer = ByteBuffer.allocateDirect(QUERY_BUFFER_SIZE);
    }


    @Override
    public @NotNull Collection<String> getWordWithContext(@NotNull String word, @NotNull Context ctx) {
        long iterHandle = NULL;

        LinkedList<String> results = new LinkedList<>();
        byte[] str = new byte[ctx.asInt() << 1 + word.length()];

        do {
            iterHandle = wordIndexReadWithContextBuffered(this.handle,
                    this.readBuffer, QUERY_BUFFER_SIZE, word, ctx.asInt(),
                    iterHandle);
            while (true) {
                int offset = this.readBuffer.getInt();
                if (offset == TERM_BUFFER_MARK) {
                    break;
                }
                System.out.printf("%d -  ", offset);
                int i = 0;
                for (; i < offset; i++) {
                    str[i++] = this.readBuffer.get();
                }
                String s = new String(str, 0, i, StandardCharsets.UTF_8);
                System.out.println(s);
                results.add(s);
            }
        } while (iterHandle != NULL);

        return results;
    }

    @Override
    public @NotNull WordContextIterator getWordIteratorWithContext(@NotNull String word, @NotNull Context ctx) {
        return null;
    }

    @Override
    public void close() throws Exception {
        NativeWordIndex.wordIndexClose(this.handle);
    }


    private static class NativeWordContextIterator implements WordContextIterator {

        @Override
        public void close() throws Exception {
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            return null;
        }
    }

    private static long withContext(int pos, Context ctx) {
        return Math.max(pos - ctx.asInt(), 0);
    }

    /**
     *
     * @param filepath
     * @param capacity
     * @param bufferSize
     * @param compact
     * @return
     */
    private static native long wordIndexOpen(String filepath, long capacity,
                                             long bufferSize, boolean compact);

    /**
     *
     * @param handle
     */
    private static native void wordIndexClose(long handle);

    /**
     *
     * @param handle
     * @param readBuffer
     * @param readBufferSize
     * @param word
     * @param context
     * @param previous
     * @return
     */
    private  static native long wordIndexReadWithContextBuffered(long handle,
                                                                 ByteBuffer readBuffer,
                                                                 long readBufferSize,
                                                                 String word,
                                                                 int context,
                                                                 long previous);
}
