package org.ns.thesis.wordindex.ffm;

import org.jetbrains.annotations.NotNull;
import org.ns.thesis.wordindex.WordContextIterator;
import org.ns.thesis.wordindex.WordIndex;

import java.io.FileNotFoundException;
import java.util.Collection;

public class FFMWordIndex implements WordIndex {

    @Override
    public @NotNull Collection<String> getWords(@NotNull String word, @NotNull ContextBytes ctx) {
        return null;
    }

    @Override
    public @NotNull WordContextIterator iterateWords(@NotNull String word, @NotNull ContextBytes ctx) throws FileNotFoundException {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
