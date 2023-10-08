package org.ns.thesis.wordindex;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Niklas Seppälä
 */
public class LineWordTokenizer implements Iterable<WordToken> {
    private final String line;
    private final List<WordToken> tokens;

    public LineWordTokenizer(@NotNull String line) {
        this.line = line;
        this.tokens = new ArrayList<>();
        this.tokenize();
    }

    private void tokenize() {
        for (int wordStart = 0, wordEnd = 0; wordEnd <= this.line.length(); wordEnd++) {
            if (wordEnd == this.line.length()) {
                wordStart = createToken(wordStart, wordEnd);
            } else if (Character.isWhitespace(line.charAt(wordEnd))) {
                wordStart = createToken(wordStart, wordEnd);
            }
        }
    }

    @NotNull
    @Override
    public Iterator<WordToken> iterator() {
        return this.tokens.iterator();
    }

    /**
     * @param wordStart
     * @param wordEnd
     * @return
     */
    private int createToken(int wordStart, int wordEnd) {
        String token = line.substring(wordStart, wordEnd);
        if (!(token.isBlank() || token.isEmpty())) {
            this.tokens.add(new WordToken(JavaWordIndex.normalize(token), wordStart));
        }
        return wordEnd + 1;
    }
}
