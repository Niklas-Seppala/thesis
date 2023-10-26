package org.nse.thesis.wordindex.analyzers;

/**
 * TODO
 */
public class WhitespaceAnalyzer implements IndexAnalyzer {
    @Override
    public int asNative() {
        return 1;
    }

    @Override
    public boolean breakAt(char c) {
        return Character.isWhitespace(c);
    }
}
