package org.nse.thesis.wordindex.pojo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BufferedWordTokenizerTest {

    @Test
    public void testReading() {
        String text =
"""
One two three four five six seven.
Eight nine ten.
""";
        byte[] readBuffer = new byte[16];
        try (InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
            int nBytes;
            int totalReadBytes = 0;
            int truncateOffset = 0;
            while ((nBytes = stream.read(readBuffer, truncateOffset, readBuffer.length - truncateOffset)) > 0) {
                final int bufferContentLength = nBytes + truncateOffset;
                final BufferedWordTokenizer tokenizer = new BufferedWordTokenizer(readBuffer, bufferContentLength);
                for (WordToken token: tokenizer) {
                    System.out.println(token);
                }
                truncateOffset = tokenizer.getTruncate();
            }
            System.out.println("Read in total " + totalReadBytes  + " bytes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void truncatesWhenWordIsNotFinishedFullBufferRe2ad() {
        byte[] readBuffer = new byte[256];
        BufferedWordTokenizer tokenizer = new BufferedWordTokenizer(readBuffer, 0);
        assertFalse(tokenizer.iterator().hasNext());
    }

    @Test
    public void truncatesWhenWordIsNotFinishedFullBufferRead() {
        String str = "One two three fo";
        byte[] readBuffer = new byte[str.length()];
        readIntoBuffer(str, readBuffer);

        Iterator<WordToken> correctTokens = List.of(
                new WordToken("one", 0),
                new WordToken("two", 4),
                new WordToken("three", 8)
        ).iterator();

        BufferedWordTokenizer tokenizer = new BufferedWordTokenizer(readBuffer, str.length());
        tokenizer.iterator().forEachRemaining(it -> {
            assertEquals(correctTokens.next(), it);
        });

        assertTrue(tokenizer.didTruncate());
        assertEquals(2, tokenizer.getTruncate());
        String truncated = new String(readBuffer, 0, tokenizer.getTruncate());
        assertEquals(truncated, "fo");
    }

    @Test
    public void bufferIsNotFullShouldNotTruncateFinalWord() {
        byte[] readBuffer = new byte[256];
        String str = "One two three four five";
        readIntoBuffer(str, readBuffer);

        Iterator<WordToken> correctTokens = List.of(
                new WordToken("one", 0),
                new WordToken("two", 4),
                new WordToken("three", 8),
                new WordToken("four", 14),
                new WordToken("five", 19)
        ).iterator();

        BufferedWordTokenizer tokenizer = new BufferedWordTokenizer(readBuffer, str.length());
        tokenizer.iterator().forEachRemaining(it -> {
            assertEquals(correctTokens.next(), it);
        });
        assertFalse(tokenizer.didTruncate());
    }

    @Test
    public void whiteSpaceAtTheEnd() {
        String str = "One two three four five\n";
        byte[] readBuffer = new byte[str.length()];
        readIntoBuffer(str, readBuffer);

        Iterator<WordToken> correctTokens = List.of(
                new WordToken("one", 0),
                new WordToken("two", 4),
                new WordToken("three", 8),
                new WordToken("four", 14),
                new WordToken("five", 19)
        ).iterator();

        BufferedWordTokenizer tokenizer = new BufferedWordTokenizer(readBuffer, str.length());
        tokenizer.iterator().forEachRemaining(it -> {
            assertEquals(correctTokens.next(), it);
        });

        assertFalse(tokenizer.didTruncate());
        assertEquals(0, tokenizer.getTruncate());
    }

    private static void readIntoBuffer(String str, byte[] readBuffer) {
        System.arraycopy(str.getBytes(StandardCharsets.UTF_8), 0, readBuffer, 0, str.length());
    }
}