package org.nse.thesis.wordindex.ffm;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.WordIndexTestBase;

class FFMWordIndexTest extends WordIndexTestBase {

    static {
        FFMNativeHandles.load("build/libs/wordindex.so");
    }

    @Test
    void testGetWords() {
        try (WordIndex index = new FFMWordIndex(TEST_FILE, this.getAnalyzer(),
                1 << 8,
                8192, 4096, true)) {
            this.getWordOccurrences().forEach((word, count) -> this.checkResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testIterateWords() {
        try (WordIndex index = new FFMWordIndex(TEST_FILE, this.getAnalyzer(),
                1 << 8,
                8192, 4096, true)) {
            this.getWordOccurrences().forEach((word, count) -> this.checkIteratorResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}