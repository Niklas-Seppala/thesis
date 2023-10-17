package org.nse.thesis.wordindex.pojo;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImprovedJavaWordIndexTest {
    private final String searchWord = "god";

    @Test
    void testGetWords() {
        try (WordIndex index = new ImprovedJavaWordIndex("src/test/resources/small.txt")) {
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
        IntStream.range(0, 1).parallel().forEach(i -> {
            createIndex();
        });
    }


    private void createIndex() {
        try (WordIndex index = new ImprovedJavaWordIndex("src/test/resources/bible10x.txt")) {
            try (WordContextIterator iterator = index.iterateWords(searchWord,
                    WordIndex.ContextBytes.SMALL_CONTEXT)) {
                long count = iterator.stream().count();
                assertEquals(44720, count);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
