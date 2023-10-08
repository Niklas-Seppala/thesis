package org.ns.thesis;

import org.ns.thesis.wordindex.NativeWordIndex;
import org.ns.thesis.wordindex.WordContextIterator;
import org.ns.thesis.wordindex.WordIndex;

import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {

        NativeWordIndex index = new NativeWordIndex("src/test/resources/bible.txt",
                1 << 16,
                8192, 4096, true);
/*
        index.wordsWithContext("god", WordIndex.Context.SMALL_CONTEXT)
                .stream()
                .map(it -> it.replace('\n', ' '))
                .map(it -> "\"" + it + "\"" )
                .forEach(System.out::println);
*/

        WordContextIterator iter = index.iterateWords("god",
                WordIndex.ContextBytes.SMALL_CONTEXT);
        while (iter.hasNext()) {
            Stream.of(iter.next())
                .map(it -> it.replace('\n', ' '))
                .map(it -> "\"" + it + "\"" )
                .forEach(System.out::println);
        }
        iter.close();

        index.close();

    }
}