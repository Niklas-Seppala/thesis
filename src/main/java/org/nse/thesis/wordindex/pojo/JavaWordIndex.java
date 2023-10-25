package org.nse.thesis.wordindex.pojo;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.WordContextIterator;
import org.nse.thesis.wordindex.WordIndex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Index that indexes words to their positions in the file.
 *
 * @author Niklas Seppälä
 */
public class JavaWordIndex implements WordIndex {
    private static final String READ_MODE = "r";
    private final String path;
    private final Map<@NotNull String, @NotNull WordEntry> index = new HashMap<>();

    private final IndexAnalyzer analyzer;

    /**
     * Creates Word index over specified text file.
     *
     * @param path Path to text file to be indexed.
     * @param analyzer Analyzer used in tokenizing text to words.
     * @throws FileNotFoundException When file path is invalid.
     */
    public JavaWordIndex(@NotNull String path, @NotNull IndexAnalyzer analyzer)
            throws FileNotFoundException {
        if (Files.notExists(Path.of(path))) {
            throw new FileNotFoundException(path);
        }
        this.path = path;
        this.analyzer = analyzer;
        this.doIndexing();
    }

    /**
     * Finds position in a file, when search context size
     * is calculated.
     *
     * @param pos Word position.
     * @param ctx Context size in bytes.
     * @return File position from word, with context. No value less than zero is returned.
     */
    private static int withContext(int pos, @NotNull WordIndex.ContextBytes ctx) {
        return Math.max(pos - ctx.size(), 0);
    }

    /**
     * Normalizes the word by removing punctuation characters, and
     * changing all characters to lowercase.
     *
     * @param word Word to normalize
     * @return Normalized word
     */
    public static String normalize(String word, IndexAnalyzer analyzer) {
        String token = null;
        for (int i = 0; i < word.length(); i++) {
            if (analyzer.breakAt(word.charAt(i))) {
                token = word.substring(0, i);
                break;
            }
        }
        if (token == null) {
            token = word;
        }
        return token.toLowerCase();

    }


    /**
     * Query the index for all occurrences of words from indexed file, with specified
     * amount of context, on both sides of the word.
     * <pre>
     *     [ctx word ctx]
     * </pre>
     *
     * @param word Word to search for
     * @param ctx  The amount of context bytes to surround the word.
     * @return Collection of words with context.
     */
    @Override
    public @NotNull Collection<String> getWords(@NotNull String word,
                                                @NotNull WordIndex.ContextBytes ctx) {
        WordEntry entry = this.index.get(normalize(word, this.analyzer));
        if (entry == null) {
            return List.of();
        }

        List<String> results = new ArrayList<>();
        try (final RandomAccessFile file = new RandomAccessFile(this.path, READ_MODE)) {
            byte[] buffer = new byte[(ctx.size() << 1) + entry.getWord().length()];
            for (Integer pos : entry.getFilePositions()) {
                int actualReadLength = getActualReadLength(ctx, pos, buffer.length);

                file.seek(withContext(pos, ctx));
                int readBytes = file.read(buffer);

                String str = new String(buffer, 0, Math.min(readBytes, actualReadLength),
                        StandardCharsets.UTF_8);
                results.add(str);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    /**
     * Queries the index with word with context, results can be accessed through an
     * iterator.
     *
     * @param word Word to search for
     * @param ctx  The amount of context to surround the word.
     * @return iterator for query results.
     * @throws FileNotFoundException when indexed file got deleted.
     */
    @Override
    public @NotNull WordContextIterator iterateWords(@NotNull String word,
                                                     @NotNull WordIndex.ContextBytes ctx)
            throws FileNotFoundException {
        WordEntry entry = this.index.get(normalize(word, this.analyzer));
        if (entry == null) {
            return (WordContextIterator) (Object) Collections.emptyIterator();
        }
        return new JavaWordContextIterator(new RandomAccessFile(this.path, READ_MODE),
                ctx,
                entry);

    }

    @Override
    public void close() {
        // NOP
    }

    @Override
    public String toString() {
        return index.toString();
    }

    /**
     * Tokenizes the file to words, and indexes them by mapping file positions
     * to words.
     */
    private void doIndexing() {
        try (final RandomAccessFile file = new RandomAccessFile(this.path, READ_MODE)) {
            int position = 0;
            String line;
            while ((line = file.readLine()) != null) {
                for (WordToken token : new LineWordTokenizer(line, analyzer)) {
                    WordEntry existing = this.index.get(token.word());
                    if (existing != null) {
                        existing.addFilePosition(position + token.position());
                    } else {
                        this.index.put(token.word(), new WordEntry(token.word(),
                                position + token.position()));
                    }
                }
                position += line.length() + 1; // NOTE: +1 because '\n' got trimmed.
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to index file", e);
        }
    }

    /**
     * Basic case for read length is ctx + word + ctx.
     * When there is not enough leading or trailing bytes to satisfy this case,
     * we read what we have.
     *
     * @param ctx        Context size in bytesss
     * @param pos        file position
     * @param bufferSize Size of the buffer (basic case)
     * @return How many bytes we should actually read.
     */
    private int getActualReadLength(@NotNull ContextBytes ctx, Integer pos,
                                    int bufferSize) {
        int truncateBeginning = pos - ctx.size();
        int actualReadLength = bufferSize;
        if (truncateBeginning < 0) {
            actualReadLength += truncateBeginning;
        }
        return actualReadLength;
    }

    /**
     * Iterator that iterates over word's file positions, and returns result
     * strings containing the word + context.
     */
    private static class JavaWordContextIterator implements WordContextIterator {

        private final RandomAccessFile file;
        private final ContextBytes ctx;
        private final Iterator<Integer> positions;
        private final byte[] buffer;

        /**
         * @param file  Indexed file.
         * @param ctx   Search context size in bytes.
         * @param entry Word file Positions to iterate over.
         */
        public JavaWordContextIterator(@NotNull RandomAccessFile file, @NotNull WordIndex.ContextBytes ctx,
                                       @NotNull WordEntry entry) {
            this.file = file;
            this.ctx = ctx;
            this.positions = entry.getFilePositions().iterator();
            this.buffer = new byte[(ctx.size() << 1) + entry.getWord().length()];
        }

        @Override
        public boolean hasNext() {
            return positions.hasNext();
        }

        @Override
        public String next() {
            int pos = this.positions.next();
            try {
                this.file.seek(withContext(pos, ctx));
                int readBytes = this.file.read(buffer);
                return new String(buffer, 0, readBytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            try {
                this.file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Stream<String> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    this, Spliterator.ORDERED), false);
        }
    }
}
