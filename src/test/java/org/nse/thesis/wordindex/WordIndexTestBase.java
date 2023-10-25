package org.nse.thesis.wordindex;
import org.junit.jupiter.api.BeforeEach;
import org.nse.thesis.wordindex.pojo.IndexAnalyzer;
import org.nse.thesis.wordindex.pojo.WhitespaceTextAnalyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WordIndexTestBase {

    public static final String TEST_FILE = "src/test/resources/bible.txt";
    private final Map<String, Integer> wordOccurrences = new HashMap<>();
    private final IndexAnalyzer analyzer = new WhitespaceTextAnalyzer();

    protected Map<String, Integer> getWordOccurrences() {
        return this.wordOccurrences;
    }

    @BeforeEach
    public void asd() {
        wordOccurrences.put("god", 4443);
        wordOccurrences.put("wrath", 198);
        wordOccurrences.put("him", 6659);
        wordOccurrences.put("easy", 5);
    }

    public void checkIteratorResultsBySize(WordIndex index, String wrath, int expected) {
        try (WordContextIterator iterator = index.iterateWords(wrath,
                WordIndex.ContextBytes.SMALL_CONTEXT)) {
            long count = iterator.stream().count();
            assertEquals(expected, count);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

    public void checkResultsBySize(WordIndex index, String word, int expected) {
        Collection<String> found1 = index.getWords(word, WordIndex.ContextBytes.SMALL_CONTEXT);
        assertEquals(expected, found1.size());
    }

    public IndexAnalyzer getAnalyzer() {
        return analyzer;
    }
}
