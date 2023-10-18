package org.nse.thesis.wordindex;

import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.ffm.FFMWordIndex;
import org.nse.thesis.wordindex.jna.JNAWordIndex;
import org.nse.thesis.wordindex.jni.JNIWordIndex;
import org.nse.thesis.wordindex.pojo.ImprovedJavaWordIndex;
import org.nse.thesis.wordindex.pojo.JavaWordIndex;

import java.io.FileNotFoundException;

class DirectoryWordIndexTest {

    @Test
    void asd() throws FileNotFoundException {
        final String DIR = "src/test/resources/";
        final String WORD = "god";
        final String FILE = "bible.txt";

        // Open indexes

        /*
        DirectoryWordIndex index = new DirectoryWordIndex(DIR, JavaWordIndex::new);
        */

        DirectoryWordIndex indexerImproved = new DirectoryWordIndex(DIR,
                ImprovedJavaWordIndex::new);

        /*
        DirectoryWordIndex indexJNI = new DirectoryWordIndex(DIR,
                (p) -> new JNIWordIndex(p, 1 << 8,
                        8192, 4096, true));
        */

        DirectoryWordIndex indexJNA = new DirectoryWordIndex(DIR,
                (p) -> new JNAWordIndex(p, 1 << 8,
                        8192, 4096, true));


        DirectoryWordIndex indexFFM = new DirectoryWordIndex(DIR,
                (p) -> new FFMWordIndex(p, 1 << 8,
                        8192, 4096, true));

        // Query

        /*
        index.getWordsWithContextInFile(FILE, WORD,
                WordIndex.ContextBytes.SMALL_CONTEXT);
        index.close();
        */

        indexerImproved.getWordsWithContextInFile(FILE, WORD, WordIndex.ContextBytes.SMALL_CONTEXT);
        indexerImproved.close();

        indexJNA.getWordsWithContextInFile(FILE, WORD, WordIndex.ContextBytes.SMALL_CONTEXT);
        indexJNA.close();

        /*
        indexJNI.getWordsWithContextInFile(FILE, WORD, WordIndex.ContextBytes.SMALL_CONTEXT);
        indexJNI.close();
        */
        indexFFM.getWordsWithContextInFile(FILE, WORD, WordIndex.ContextBytes.SMALL_CONTEXT);
        indexFFM.close();
    }
}