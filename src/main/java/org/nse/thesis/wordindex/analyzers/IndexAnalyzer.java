package org.nse.thesis.wordindex.analyzers;

/**
 * @author Niklas Seppälä
 */
public interface IndexAnalyzer {

    /**
     * @return Native id.
     */
    int asNative();

    /**
     * @param c Character
     * @return true if this analyzer deems word should
     * end at specified char
     */
    boolean breakAt(char c);
}
