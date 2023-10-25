package org.nse.thesis.wordindex.pojo;

/**
 *
 */
public interface IndexAnalyzer {

    int asNative();

    /**
     *
     * @param c
     * @return
     */
    boolean breakAt(char c);
}
