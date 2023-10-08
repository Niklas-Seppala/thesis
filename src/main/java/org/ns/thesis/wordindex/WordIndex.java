package org.ns.thesis.wordindex;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.Collection;

/**
 * Index that indexes words to their positions in the file.
 *
 * @author Niklas Seppälä
 */
public interface WordIndex extends AutoCloseable {

    /**
     * Query the index for all occurrences of words from indexed file, with specified
     * amount of context, on both sides of the word.
     *
     * @param word Word to search for.
     * @param ctx  The amount of context bytes to surround the word.
     * @return Collection of words with context.
     */
    @NotNull Collection<String> getWords(@NotNull String word, @NotNull WordIndex.ContextBytes ctx);

    /**
     * Queries the index with word with context, results can be accessed through an
     * iterator.
     *
     * @param word Word to search for.
     * @param ctx  The amount of context bytes to surround the word.
     * @return Iterator that iterates over the results.
     */
    @NotNull WordContextIterator iterateWords(@NotNull String word,
                                              @NotNull WordIndex.ContextBytes ctx)
            throws FileNotFoundException;

    /**
     * Context used in {@link WordIndex} queries, to specify the amount of leading
     * and trailing bytes surrounding the queried word.
     */
    enum ContextBytes {
        /**
         * No context bytes, just the word itself.
         */
        NO_CONTEXT,
        /**
         * 16 bytes of context.
         */
        SMALL_CONTEXT,
        /**
         * 64 bytes of context.
         */
        MEDIUM_CONTEXT,
        /**
         * 128 bytes of context.
         */
        LARGE_CONTEXT;

        public int size() {
            return switch (this) {
                case NO_CONTEXT -> 0;
                case SMALL_CONTEXT -> 16;
                case MEDIUM_CONTEXT -> 64;
                case LARGE_CONTEXT -> 128;
            };
        }
    }
}
