package org.nse.thesis.wordindex.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Word entry data object, that hold file positions.
 * Object is comparable by its word.
 *
 * @author Niklas Seppälä
 */
class WordEntry {
    private final String word;
    private final List<Integer> filePositions;

    /**
     * Creates new WordEntry object, with initial file position.
     *
     * @param word    Word
     * @param initial First position
     */
    public WordEntry(String word, int initial) {
        this.word = word;
        this.filePositions = new ArrayList<>();
        this.filePositions.add(initial);
    }

    /**
     * @return Get the word of the word entry.
     */
    public String getWord() {
        return word;
    }

    /**
     * @return Get pe positions where word appears in the indexed
     * file.
     */
    public List<Integer> getFilePositions() {
        return filePositions;
    }

    /**
     * Add file position to this entry.
     *
     * @param filePosition Position to add.
     */
    public void addFilePosition(int filePosition) {
        this.filePositions.add(filePosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WordEntry entry = (WordEntry) o;
        return Objects.equals(word, entry.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
    }

    @Override
    public String toString() {
        return "{\"" + word + "\" " + filePositions + '}';
    }
}
