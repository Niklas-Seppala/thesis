package org.nse.thesis.wordindex.pojo;

public class WhitespaceTextAnalyzer implements IndexAnalyzer {

    @Override
    public boolean breakAt(char c) {
        return (c == ' ' || c == '.' || c == ',' || c == '\n' || c == '\r' || c == '?'|| c == '!' || c == ';' || c == ':');
    }
}
