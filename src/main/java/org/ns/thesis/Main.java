package org.ns.thesis;

import org.ns.thesis.wordindex.NativeWordIndex;
import org.ns.thesis.wordindex.WordContextIterator;
import org.ns.thesis.wordindex.WordIndex;

public class Main {
    public static void main(String[] args) throws Exception {

        NativeWordIndex index = new NativeWordIndex("src/test/resources/tfile.txt",
                1 << 16,
                8192, 4096, true);

        index.wordsWithContext("god", WordIndex.Context.SMALL_CONTEXT)
                .stream()
                .map(it -> it.replace('\n', ' '))
                .forEach(System.out::println);

//        WordContextIterator iter = index.wordIteratorWithContext("lucifer",
//                WordIndex.Context.SMALL_CONTEXT);

//        while (iter.hasNext()) {
////            System.out.println(iter.next().replace('\n', ' '));
//        }

//        iter.close();

        index.close();

    }
}