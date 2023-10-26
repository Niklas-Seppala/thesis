package org.nse.thesis.wordindex.jni;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordIndex;
import org.nse.thesis.wordindex.WordIndexTestBase;

class JNIWordIndexTest extends WordIndexTestBase {
    static {
        JNIWordIndexBindings.load("build/libs/wordindex.so");
    }

    @Test
    void testGetWords() {
        try (WordIndex index = new JNIWordIndex(TEST_FILE, this.getAnalyzer(),
                1 << 8,
                8192, 4096, true)) {
            this.getWordOccurrences().forEach((word, count) -> this.checkResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testIterateWords() {
        try (WordIndex index = new JNIWordIndex(TEST_FILE, this.getAnalyzer(),
                1 << 8,
                8192, 4096, true)) {
            this.getWordOccurrences().forEach((word, count) -> this.checkIteratorResultsBySize(index, word, count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}