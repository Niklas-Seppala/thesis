package org.nse.thesis.wordindex.jna;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.WordIndexTestBase;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JNAWordIndexTest extends WordIndexTestBase {

    @Test
    void testGetWords() {
        try (WordIndex index = new JNAWordIndex(TEST_FILE, this.getAnalyzer(),
                1 << 8,
                8192, 4096, true)) {
            this.getWordOccurrences().forEach((word, count) -> this.checkResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testIterateWords() {
        try (WordIndex index = new JNAWordIndex(TEST_FILE, this.getAnalyzer(),
                1 << 8,
                8192, 4096, true)) {
            this.getWordOccurrences().forEach((word, count) -> this.checkIteratorResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}