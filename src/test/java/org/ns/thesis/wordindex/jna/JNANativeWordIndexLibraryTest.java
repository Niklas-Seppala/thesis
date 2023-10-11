package org.ns.thesis.wordindex.jna;

import com.sun.jna.Pointer;
import org.junit.jupiter.api.Test;

class JNANativeWordIndexLibraryTest {

    @Test
    void file_word_index_open() {
        final Pointer handle = JNAWordIndexLibrary.INSTANCE
                .file_word_index_open("src/test/resources/bible.txt",
                        1 << 16, 8192, true);

        JNAWordIndexLibrary.INSTANCE.file_word_index_close(handle);
    }
}