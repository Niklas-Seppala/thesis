package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;

/**
 * Word token wraps the word (String representation) and
 * it's position in the file it was read from.
 *
 * @param word     Word.
 * @param position Position of the word in a file.
 * @author Niklas Seppälä
 */
public record WordToken(@NotNull String word, int position) {
}
