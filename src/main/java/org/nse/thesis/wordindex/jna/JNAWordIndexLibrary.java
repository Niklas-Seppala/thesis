package org.nse.thesis.wordindex.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * @author Niklas Seppälä
 * Get implementation of this interface with {@link Impl}
 */
public interface JNAWordIndexLibrary extends Library {

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
    Pointer file_word_index_open(String filepath, int analyzer, long capacity, long bufferSize, boolean compact);

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
     * @param wordLength     Length of the word in bytes
     * @param context        Context surrounding the word in indexed file
     * @param wordIterator   Word file position iterator. Initially should be NULL (0),
     *                       after that, it should be the return value of this method.
     * @return Word file position iterator.
     */
    Pointer file_word_index_read_with_context_buffered(Pointer handle, Pointer readBuffer,
                                                       long readBufferSize, String word,
                                                       long wordLength, long context,
                                                       Pointer wordIterator);

    /**
     * Closes native WordIndex, releasing all native resources.
     *
     * @param handle Handle to native WordIndex
     */
    void file_word_index_close(Pointer handle);


    /**
     * Closes native iterator, releasing its resources. Not closing it will cause a
     * memory leak, IF the iterator was not exhausted. So if you give up on reading, when
     * there's still results remaining, call this method!
     *
     * @param iterator WordIndex word file position iterator.
     */
    void file_word_index_close_iterator(Pointer iterator);


    /**
     * Library implementation singleton. Make sure you load library before
     * accessing it by {@link Impl#get()}.
     * <p>
     *
     * Load the library by {@link Impl#get(String)} or {@link Impl#load(String)}
     */
    class Impl {
        private static volatile JNAWordIndexLibrary instance;

        /**
         * Loads native library and generates implementation to {@link JNAWordIndexLibrary}
         * @param libPath Path to shared native library.
         */
        public static void load(String libPath) {
            if (instance == null) {
                synchronized(JNAWordIndexLibrary.class) {
                    if (instance == null) {
                        instance = Native.load(libPath, JNAWordIndexLibrary.class);
                    }
                }
            }
        }

        /**
         * Get the library instance, when you are sure you have loaded it.
         * Skips synchronization.
         *
         * @return Library instance.
         */
        public static JNAWordIndexLibrary get() {
            if (instance == null) {
                throw new IllegalStateException("Library is not loaded");
            }
            return instance;
        }

        /**
         * Loads native library and returns instance.
         * @param libPath Path to shared native library.
         * @return Library instance.
         */
        public static JNAWordIndexLibrary get(String libPath) {
            load(libPath);
            return instance;
        }
    }
}
