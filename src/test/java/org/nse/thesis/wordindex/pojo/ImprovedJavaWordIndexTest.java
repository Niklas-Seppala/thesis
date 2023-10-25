package org.nse.thesis.wordindex.pojo;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.WordIndexTestBase;

public class ImprovedJavaWordIndexTest extends WordIndexTestBase {

    @Test
    void testGetWords() {
        try (WordIndex index = new ImprovedJavaWordIndex(TEST_FILE, this.getAnalyzer())) {
            this.getWordOccurrences().forEach((word, count) -> this.checkResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testIterateWords() {
        try (WordIndex index = new ImprovedJavaWordIndex(TEST_FILE, this.getAnalyzer())) {
            this.getWordOccurrences().forEach((word, count) -> this.checkIteratorResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
