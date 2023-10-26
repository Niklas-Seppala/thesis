package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.analyzers.IndexAnalyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tokenizer that extract normalized words from a string.
 *
 * @author Niklas Seppälä
 */
public class LineWordTokenizer implements Iterable<WordToken> {
    private final List<WordToken> tokens;
    private final IndexAnalyzer analyzer;

    /**
     *  Constructor
     * @param line Line to tokenize.
     * @param analyzer Analyzer used in tokenization process.
     */
    public LineWordTokenizer(@NotNull String line, @NotNull IndexAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.tokens = new ArrayList<>();
        this.tokenizeLine(line);
    }

    /**
     * @return Iterator that iterates over words tokenized in the line.
     */
    @NotNull
    @Override
    public Iterator<WordToken> iterator() {
        return this.tokens.iterator();
    }

    /**
     * Reads the line, and tokenizes the words. Words are normalized.
     *
     * @param line Line to process
     */
    private void tokenizeLine(@NotNull String line) {
        for (int wordStart = 0, wordEnd = 0; wordEnd <= line.length(); wordEnd++) {
            if (wordEnd == line.length()) {
                wordStart = storeToken(line, wordStart, wordEnd);
            } else if (Character.isWhitespace(line.charAt(wordEnd))) {
                wordStart = storeToken(line, wordStart, wordEnd);
            }
        }
    }

    /**
     * Creates and stores WordToken from line.
     *
     * @param line      Line to create token from
     * @param wordStart Word start position in a line.
     * @param wordEnd   Word end position in the line.
     * @return Next position.
     */
    private int storeToken(@NotNull String line, int wordStart, int wordEnd) {
        String token = line.substring(wordStart, wordEnd);
        if (!(token.isBlank() || token.isEmpty())) {
            this.tokens.add(new WordToken(JavaWordIndex.normalize(token, this.analyzer), wordStart));
        }
        return wordEnd + 1;
    }
}
