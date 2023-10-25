package org.nse.thesis.wordindex.jna;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.WordIndexTestBase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JNAWordIndexTest extends WordIndexTestBase {

    public static final String TEST_FILE = "src/test/resources/bible.txt";
    private final String searchWord = "god";

    @Test
    void testGetWords() throws Exception {
        final WordIndex.ContextBytes ctx = WordIndex.ContextBytes.SMALL_CONTEXT;

        try (WordIndex index = new JNAWordIndex(TEST_FILE,
                1 << 8,
                8192, 4096, true)) {

            long count = index.getWords(searchWord, ctx)
                    .size();
            assertEquals(4472, count);
        }
    }

    @Test
    void testIterateWords() throws Exception {
        final WordIndex.ContextBytes ctx = WordIndex.ContextBytes.SMALL_CONTEXT;
        try (WordIndex index = new JNAWordIndex(TEST_FILE,
                1 << 8,
                8192, 4096, true)) {
            try (WordContextIterator iterator =
                         index.iterateWords(searchWord, ctx)) {
                long count = iterator.stream().count();
                assertEquals(4472, count);
            }
        }
    }
}