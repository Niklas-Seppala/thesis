package org.nse.thesis.wordindex.analyzers;

/**
 * Analyzer used in indexing the words. Analyzers determine "what makes a word".
 * @author Niklas Seppälä
 */
public interface IndexAnalyzer {

    /**
     * Get the native id for this analyzer.
     * @return Native id.
     */
    int asNative();

    /**
     * Break the char sequence to word.
     *
     * @param c Character
     * @return true if this analyzer deems word should
     * end at specified char
     */
    boolean breakAt(char c);
}
