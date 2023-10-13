package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;

/**
 * @author Niklas Seppälä
 *
 * @param word Word.
 * @param position Position of the word in a file.
 */
public record WordToken(@NotNull String word, int position) {
}
