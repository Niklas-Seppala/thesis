package org.ns.thesis.wordindex;

import org.junit.jupiter.api.Test;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NativeWordIndexTest {

    @Test
    void getWordWithContext() throws Exception {
        final String searchWord = "god";
        final WordIndex.Context ctx = WordIndex.Context.SMALL_CONTEXT;

        try (WordIndex index = new NativeWordIndex("src/test/resources/bible.txt",
                1 << 8,
                8192, 4096, true)) {

            long count = index.wordsWithContext(searchWord, ctx)
                    .size();
            assertEquals(4471, count);
        }
    }

    @Test
    void getWordIteratorWithContext() throws Exception {
        final String searchWord = "god";
        final WordIndex.Context ctx = WordIndex.Context.SMALL_CONTEXT;

        try (WordIndex index = new NativeWordIndex("src/test/resources/bible.txt",
                1 << 8,
                8192, 4096, true)) {

            WordContextIterator iterator = index.wordIteratorWithContext(searchWord, ctx);

            long count = StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iterator, Spliterator.ORDERED), false).count();

            assertEquals(4471, count);
        }
    }
}