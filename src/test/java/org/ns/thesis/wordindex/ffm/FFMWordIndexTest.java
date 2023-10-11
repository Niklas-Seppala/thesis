package org.ns.thesis.wordindex.ffm;

import org.junit.jupiter.api.Test;
import org.ns.thesis.wordindex.WordIndex;
import org.ns.thesis.wordindex.jni.JNIWordIndex;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class FFMWordIndexTest {

    @Test
    void getWords() throws Exception {
        final WordIndex.ContextBytes ctx = WordIndex.ContextBytes.SMALL_CONTEXT;

        try (WordIndex index = new JNIWordIndex("src/test/resources/bible.txt",
                1 << 8,
                8192, 4096, true)) {

            long count = index.getWords("god", ctx).size();
            assertEquals(4472, count);
        }
    }

    @Test
    void iterateWords() {
    }
}