package org.ns.thesis.wordindex;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaWordIndexTest {

    private final String searchWord = "god";

    @Test
    void testGetWords() {
        try (WordIndex index = new JavaWordIndex("src/test/resources/small.txt")) {
            long count = index.getWords(searchWord,
                            WordIndex.ContextBytes.SMALL_CONTEXT)
                    .size();
            assertEquals(2, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testIterateWords() {
        try (WordIndex index = new JavaWordIndex("src/test/resources/bible.txt")) {
            dumpToFile(index);

            try (WordContextIterator iterator = index.iterateWords(searchWord,
                    WordIndex.ContextBytes.SMALL_CONTEXT)) {
                long count = iterator.stream().count();
                assertEquals(4472, count);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void dumpToFile(WordIndex index) throws IOException {
        File f = new File("build/results-java");
        try (var writer = new FileWriter(f)) {
            index.iterateWords("god",
                            WordIndex.ContextBytes.SMALL_CONTEXT).stream()
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