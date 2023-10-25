package org.nse.thesis.wordindex.jni;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class JNIWordIndexBindings {
    private static boolean loaded = false;
    public static void load(String path) {
        if (!loaded) {
            System.load(Path.of(path).toAbsolutePath().toString());
            loaded = true;
        }
    }

    /**
     * Creates a native word index for specified file.
     * Remember to close it!
     *
     * @param filepath   Path to text file to be indexed.
     * @param analyzer   Analyzer used in tokenizing words from text.
     * @param capacity   Estimate how many unique words file might contain.
     * @param bufferSize Suggested size of buffer that's used when indexing the file.
     * @param compact    Should index be compacted after indexing is done.
     * @return Handle to native WordIndex
     */
    public static native long wordIndexOpen(String filepath, int analyzer, long capacity,
                                             long bufferSize, boolean compact);

    /**
     * Closes native WordIndex, releasing all native resources.
     *
     * @param handle Handle to native WordIndex
     */
    public static native void wordIndexClose(long handle);

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
    public static native long wordIndexReadWithContextBuffered(long handle,
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
    public static native void wordIndexCloseIterator(long iterator);
}
