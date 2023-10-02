package org.ns.thesis.wordindex;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

class JavaWordIndexTest {

    @Test
    void getWordWithContext() throws IOException {
        JavaWordIndex index = new JavaWordIndex("src/test/resources/tfile.txt");

        index.getWordWithContext("lucifer", WordIndex.Context.LARGE_CONTEXT)
                .stream().map(it -> it.replace('\n', ' '))
                .forEach(System.out::println);

        System.out.println("\n\n");
        index.close();
    }

    @Test
    void getWordIteratorWithContext()  {
        try (JavaWordIndex index = new JavaWordIndex("src/test/resources/tfile.txt")) {

            final WordContextIterator iterator = index.getWordIteratorWithContext("god",
                    WordIndex.Context.SMALL_CONTEXT);

            try (iterator) {
                iterator.forEachRemaining(it -> {
                        System.out.println(it.replace('\n', ' '));
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void doIndexing() throws IOException {
        JavaWordIndex index = new JavaWordIndex("src/test/resources/small.txt");
        System.out.println(index);
        index.close();
    }
}