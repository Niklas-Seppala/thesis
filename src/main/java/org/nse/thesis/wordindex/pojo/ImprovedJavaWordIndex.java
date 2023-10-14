package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;

import java.io.FileNotFoundException;
import java.util.Collection;

public class ImprovedJavaWordIndex implements WordIndex {

    @Override
    public @NotNull Collection<String> getWords(@NotNull String word, @NotNull ContextBytes ctx) {
        return null;
    }

    @Override
    public @NotNull WordContextIterator iterateWords(@NotNull String word, @NotNull ContextBytes ctx)
            throws FileNotFoundException {
        return null;
    }

    @Override
    public void close() {

    }
}
