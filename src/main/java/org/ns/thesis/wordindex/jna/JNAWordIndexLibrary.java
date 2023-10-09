package org.ns.thesis.wordindex.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * @author Niklas Seppälä
 */
public interface JNAWordIndexLibrary extends Library {

    /**
     *
     */
    JNAWordIndexLibrary INSTANCE = Native.load("build/libs/wordindex.so",
            JNAWordIndexLibrary.class);

    /**
     *
     * @param filepath
     * @param capacity
     * @param indexingBufferSize
     * @param compact
     * @return
     */
    Pointer file_word_index_open(String filepath, long capacity, long indexingBufferSize, boolean compact);

    /**
     *
     * @param handle
     * @param readBuffer
     * @param readBufferSize
     * @param word
     * @param wordLength
     * @param context
     * @param iterator
     * @return
     */
    Pointer file_word_index_read_with_context_buffered(Pointer handle, Pointer readBuffer,
                                                       long readBufferSize, String word,
                                                       long wordLength, long context,
                                                       Pointer iterator);

    int wadd(int a, int b);

    /**
     *
     * @param handle
     */
    void file_word_index_close(Pointer handle);


    /**
     *
     * @param iter
     */
    void file_word_index_close_iterator(Pointer iter);
}
