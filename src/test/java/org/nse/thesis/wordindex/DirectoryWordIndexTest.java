package org.nse.thesis.wordindex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.pojo.BufferedJavaWordIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

class DirectoryWordIndexTest extends WordIndexTestBase {
    private static final Map<String, Integer> wordToCountMap = new HashMap<>();

    static {
        wordToCountMap.put("power", 58);
        wordToCountMap.put("man", 118);
        wordToCountMap.put("physical", 60);
        wordToCountMap.put("bound", 58);
    }

    private static void assertWordCount(DirectoryWordIndex index, File file, String man, int expected) {
        int manWordCount = index.getWordsWithContextInFile(file.getAbsolutePath(),
                man, WordIndex.ContextBytes.SMALL_CONTEXT).size();
        Assertions.assertEquals(expected, manWordCount);
    }

    @Test
    void test() throws FileNotFoundException {
        try (DirectoryWordIndex index = new DirectoryWordIndex("src/test/resources/docs",
                this.getAnalyzer(), BufferedJavaWordIndex::new)) {
            for (File file : index.files()) {
                wordToCountMap.forEach((key, value) -> assertWordCount(index, file, key, value));
            }
        }
    }
}