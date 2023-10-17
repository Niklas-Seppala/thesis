package org.nse.thesis.wordindex.pojo;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;

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
    void testIterateWords() throws InterruptedException {
        Thread.sleep(1000);
        try (WordIndex index = new JavaWordIndex("src/test/resources/bible.txt")) {

            try (WordContextIterator iterator = index.iterateWords(searchWord,
                    WordIndex.ContextBytes.SMALL_CONTEXT)) {
                long count = iterator.stream().count();
                assertEquals(4472, count);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}