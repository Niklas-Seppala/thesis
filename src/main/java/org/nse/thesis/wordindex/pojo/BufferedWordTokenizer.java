package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BufferedWordTokenizer implements Iterable<WordToken> {
    private final List<WordToken> wordTokens;
    private int truncate = 0;

    private final IndexAnalyzer analyzer;

    private boolean isWhitespace (byte b) {
        return Character.isWhitespace((char)b);
    }

    private int storeToken(byte[] buffer, int wordStart, int wordEnd) {
        String word = new String(buffer, wordStart, wordEnd - wordStart);
        if (!(word.isBlank() || word.isEmpty())) {
            this.wordTokens.add(new WordToken(JavaWordIndex.normalize(word, this.analyzer), wordStart));
        }
        return wordEnd + 1;
    }

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

    @NotNull
    @Override
    public Iterator<WordToken> iterator() {
        return this.wordTokens.iterator();
    }

    public int getTruncatedBytes() {
        return truncate;
    }

    public boolean didTruncate() {
        return this.truncate != 0;
    }
}
