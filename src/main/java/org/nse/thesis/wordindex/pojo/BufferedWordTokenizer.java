package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.analyzers.IndexAnalyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tokenizer that extract normalized words from a buffer.
 *
 * @author Niklas Seppälä
 */
public class BufferedWordTokenizer implements Iterable<WordToken> {
    private final List<WordToken> wordTokens;
    private final IndexAnalyzer analyzer;
    private int truncate = 0;

    /**
     * @param buffer   Buffer to read tokenize words from.
     * @param nBytes   Size of the buffer.
     * @param analyzer Analyzer used in tokenization.
     */
    public BufferedWordTokenizer(byte[] buffer, int nBytes, IndexAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.wordTokens = new ArrayList<>();
        for (int wordEnd = 0, wordStart = 0; wordEnd < nBytes; wordEnd++) {
            if (wordEnd == nBytes - 1) {
                // Final character, check if we got full word, or do we
                // need to truncate. Also check if buffer is not full.
                // In that case, we are at the end of the whole text read.
                // Don't check for whitespace.
                if (nBytes < buffer.length) {
                    wordStart = storeToken(buffer, wordStart, wordEnd + 1);
                } else if (!isWhitespace(buffer[wordEnd])) {
                    this.truncate = (wordEnd - wordStart) + 1;
                    System.arraycopy(buffer, wordStart, buffer, 0, this.truncate);
                } else {
                    // Got a whole word.
                    wordStart = storeToken(buffer, wordStart, wordEnd);
                }
            } else if (isWhitespace(buffer[wordEnd])) {
                // a Whole word is between offsets: wordStart - wordEnd.
                wordStart = storeToken(buffer, wordStart, wordEnd);
            }
        }
    }

    /**
     * @param b Byte to check
     * @return True if byte is whitespace character.
     */
    private boolean isWhitespace(byte b) {
        return Character.isWhitespace((char) b);
    }

    /**
     * Reads String from buffer and stores it.
     *
     * @param buffer    Buffer to read word from.
     * @param wordStart Word start position in the buffer.
     * @param wordEnd   Word end position in the buffer.
     * @return next position after word.
     */
    private int storeToken(byte[] buffer, int wordStart, int wordEnd) {
        String word = new String(buffer, wordStart, wordEnd - wordStart);
        if (!(word.isBlank() || word.isEmpty())) {
            this.wordTokens.add(new WordToken(JavaWordIndex.normalize(word, this.analyzer), wordStart));
        }
        return wordEnd + 1;
    }

    /**
     * @return Iterator that iterates over words read from buffer.
     */
    @NotNull
    @Override
    public Iterator<WordToken> iterator() {
        return this.wordTokens.iterator();
    }

    /**
     * @return Number of bytes truncated, and moved to the start
     * of the buffer.
     */
    public int getTruncatedBytes() {
        return truncate;
    }

    /**
     * @return True if truncation happened.
     */
    public boolean didTruncate() {
        return this.truncate != 0;
    }
}
