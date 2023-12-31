package org.nse.thesis.wordindex;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.analyzers.IndexAnalyzer;

import java.io.FileNotFoundException;
import java.util.Collection;

/**
 * Index that indexes words to their positions in the file.
 *
 * @author Niklas Seppälä
 */
public interface WordIndex extends AutoCloseable {
    /**
     * 4 byte mark on query buffer that indicates there is
     * no more bytes to read.
     */
    int TERM_BUFFER_MARK = 0;

    /**
     * When index is queried, this is the minimum buffer size results
     * are read into, IF index supports buffered reading.
     */
    int MIN_QUERY_BUFFER_SIZE = 512;

    /**
     * When indexed file is read, this is the minimum buffer size,
     * IF index supports buffered reading.
     */
    int MIN_INDEXING_BUFFER_SIZE = 4096;

    /**
     * Index hash container is at least this big.
     */
    int MIN_WORD_CAPACITY_ESTIMATE = 64;

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
     *
     * @return Iterator that iterates over the results.
     * @throws FileNotFoundException If indexed file was deleted.
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

        /**
         * Get the size of this Context in bytes.
         * @return Context in bytes.
         */
        public int size() {
            return switch (this) {
                case NO_CONTEXT -> 0;
                case SMALL_CONTEXT -> 16;
                case MEDIUM_CONTEXT -> 64;
                case LARGE_CONTEXT -> 128;
            };
        }
    }

    /**
     * Provides {@link WordIndex} implementation.
     */
    @FunctionalInterface
    interface Provider {
        /**
         * Creates index for specified file.
         *
         * @param path Path to the file to index.
         * @param analyzer Analyzer used in indexing.
         * @return Word index for specified file.
         *
         * @throws FileNotFoundException When file doesn't exist.
         */
        WordIndex indexFrom(String path, IndexAnalyzer analyzer) throws FileNotFoundException;
    }
}
