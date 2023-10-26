package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;

/**
 * @param word     Word.
 * @param position Position of the word in a file.
 * @author Niklas Seppälä
 */
public record WordToken(@NotNull String word, int position) {
}
