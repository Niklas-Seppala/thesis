package org.ns.thesis.wordindex;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface WordIndex extends AutoCloseable {

    @NotNull Collection<String> wordsWithContext(@NotNull String word, @NotNull Context ctx);

    @NotNull WordContextIterator wordIteratorWithContext(@NotNull String word,
                                                         @NotNull Context ctx);

    enum Context {
        NO_CONTEXT,
        SMALL_CONTEXT,
        MEDIUM_CONTEXT,
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
