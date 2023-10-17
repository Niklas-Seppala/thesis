package org.nse.thesis.wordindex.jni;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JNIWordIndexTest {

    public static final String TEST_FILE = "src/test/resources/bible.txt";
    private final String searchWord = "god";

    @BeforeAll
    public static void loadLib() {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
    }

    @Test
    void testGetWords() throws Exception {
        final WordIndex.ContextBytes ctx = WordIndex.ContextBytes.SMALL_CONTEXT;

        try (WordIndex index = new JNIWordIndex(TEST_FILE,
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
        try (WordIndex index = new JNIWordIndex(TEST_FILE,
                1 << 8,
                8192, 4096, true)) {
            try (WordContextIterator iterator =
                         index.iterateWords(searchWord, ctx)) {
                long count = iterator.stream().count();
                assertEquals(4472, count);
            }
        }
    }

    private static void dumpToFile(WordIndex index, WordIndex.ContextBytes ctx)
            throws IOException {
        File f = new File("build/results-native");
        try (var writer = new FileWriter(f)) {
            index.iterateWords("god", ctx).stream()
                    .map(str -> str.replaceAll("\n", " "))
                    .forEach(str -> {
                        try {
                            writer.write(str);
                            writer.write("\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}