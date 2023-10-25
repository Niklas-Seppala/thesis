package org.nse.thesis.wordindex.pojo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nse.thesis.wordindex.WordIndexTestBase;

import java.util.Iterator;
import java.util.List;

class LineWordTokenizerTest extends WordIndexTestBase {

    @Test
    void nextToken() {
        LineWordTokenizer tokenizer = new LineWordTokenizer("this is a line", getAnalyzer());
        for (WordToken token : tokenizer) {
            System.out.println(token);
        }
    }

    @Test
    public void hasNext() {
        Iterator<WordToken> tokenizer = new LineWordTokenizer(
                "This is a line", getAnalyzer()).iterator();
        Assertions.assertTrue(tokenizer.hasNext());
        tokenizer.next();
        Assertions.assertTrue(tokenizer.hasNext());
        tokenizer.next();
        Assertions.assertTrue(tokenizer.hasNext());
        tokenizer.next();
        Assertions.assertTrue(tokenizer.hasNext());
        tokenizer.next();
        Assertions.assertFalse(tokenizer.hasNext());
    }

    @Test
    void nextTokenHasPunctuation() {
        Iterator<WordToken> correctTokens = List.of(
                new WordToken("this", 0),
                new WordToken("is", 5),
                new WordToken("a", 9),
                new WordToken("line", 11)
        ).iterator();
        new LineWordTokenizer("This is; a line.", getAnalyzer()).iterator().forEachRemaining(token ->
                Assertions.assertEquals(correctTokens.next(), token)
        );
    }

    @Test
    void nextTokenStartsWithWhitespace() {
        Iterator<WordToken> correctTokens = List.of(
                new WordToken("this", 2),
                new WordToken("is", 7),
                new WordToken("a", 10),
                new WordToken("line", 12)
        ).iterator();
        new LineWordTokenizer("  This is a line", getAnalyzer()).iterator().forEachRemaining(token ->
                Assertions.assertEquals(correctTokens.next(), token)
        );
    }


    @Test
    void nextTokenHasWhitespaceAtEnd() {
        Iterator<WordToken> correctTokens = List.of(
                new WordToken("this", 2),
                new WordToken("is", 7),
                new WordToken("a", 10),
                new WordToken("line", 12)
        ).iterator();
        new LineWordTokenizer("  This is a line  ", getAnalyzer()).iterator().forEachRemaining(token ->
                Assertions.assertEquals(correctTokens.next(), token)
        );
    }

    @Test
    void nextTokenHasStuffAllOver() {
        Iterator<WordToken> correctTokens = List.of(
                new WordToken("this", 2),
                new WordToken("is", 10),
                new WordToken("a", 15),
                new WordToken("line", 20)
        ).iterator();
        new LineWordTokenizer("  This,   is.  a    line!  ", getAnalyzer()).iterator().forEachRemaining(
                token ->
                        Assertions.assertEquals(correctTokens.next(), token)
        );
    }
}