package org.ns.thesis.wordindex;

import org.jetbrains.annotations.NotNull;

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
 * @author Niklas Seppälä
 */
public class JavaWordIndex implements WordIndex {
    private static final String READ_MODE = "r";
    private final String path;
    private final Map<@NotNull String, @NotNull WordEntry> index = new HashMap<>();
    private RandomAccessFile file;

    /**
     *
     * @param path
     * @throws FileNotFoundException
     */
    public JavaWordIndex(@NotNull String path) throws FileNotFoundException {
        if (Files.notExists(Path.of(path))) {
            throw new IllegalArgumentException("Invalid filepath: " + path);
        }

        this.file = new RandomAccessFile(path, READ_MODE);
        this.path = path;
        this.doIndexing();
    }

    private static long withContext(long pos, @NotNull WordIndex.ContextBytes ctx) {
        return Math.max(pos - ctx.size(), 0);
    }

    /**
     *
     * @param word
     * @param ctx
     * @return
     */
    @Override
    public @NotNull Collection<String> wordsWithContext(@NotNull String word,
                                                        @NotNull WordIndex.ContextBytes ctx) {
        WordEntry entry = this.index.get(normalize(word));
        if (entry == null) {
            return List.of();
        }

        LinkedList<String> results = new LinkedList<>();
        try {
            if (!this.file.getChannel().isOpen()) {
                this.file = new RandomAccessFile(this.path, READ_MODE);
            }
            byte[] buffer = new byte[(ctx.size() << 1) + entry.word.length()];
            for (Long pos : entry.filePositions) {
                this.file.seek(withContext(pos, ctx));
                int readBytes = this.file.read(buffer);
                String str = new String(buffer, 0, readBytes, StandardCharsets.UTF_8);
                results.add(str);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    /**
     *
     * @param word
     * @param ctx
     * @return
     */
    @Override
    public @NotNull WordContextIterator wordIteratorWithContext(@NotNull String word,
                                                                @NotNull WordIndex.ContextBytes ctx) {
        WordEntry entry = this.index.get(normalize(word));
        if (entry == null) {
            return (WordContextIterator)(Object)Collections.emptyIterator();
        }
        try {
            return new JavaWordContextIterator(new RandomAccessFile(this.path, READ_MODE),
                    ctx,
                    entry);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return (WordContextIterator)(Object)Collections.emptyIterator();
        }
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.file.close();
    }

    @Override
    public String toString() {
        return index.toString();
    }

    /**
     *
     */
    protected void doIndexing() {
        try {
            long filePosition = 0L;
            String line;
            while ((line = this.file.readLine()) != null) {
                for (WordToken token : new LineWordTokenizer(line)) {
                    WordEntry existing = this.index.get(token.word());
                    if (existing != null) {
                        existing.addFilePosition(filePosition + token.position());
                    } else {
                        this.index.put(token.word(), new WordEntry(token.word(),
                                filePosition + token.position()));
                    }
                }
                filePosition += line.length() + 1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                this.file.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * @author Niklas Seppälä
     */
    private static class JavaWordContextIterator implements WordContextIterator {

        private final RandomAccessFile file;
        private final ContextBytes ctx;
        private final Iterator<Long> positions;
        private final byte[] buffer;

        /**
         *
         * @param file
         * @param ctx
         * @param entry
         */
        public JavaWordContextIterator(@NotNull RandomAccessFile file, @NotNull WordIndex.ContextBytes ctx,
                                   @NotNull WordEntry entry) {
            this.file = file;
            this.ctx = ctx;
            this.positions = entry.filePositions.iterator();
            this.buffer = new byte[(ctx.size() << 1) + entry.word.length()];
        }

        @Override
        public boolean hasNext() {
            return positions.hasNext();
        }

        @Override
        public String next() {
            long pos = this.positions.next();
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

    /**
     *
     * @param word
     * @return
     */
    public static String normalize(String word) {
        String token = null;
        for (int i = 0; i < word.length(); i++) {
            if (!Character.isLetterOrDigit(word.charAt(i))) {
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
     * @author Niklas Seppälä
     */
    private static class WordEntry {
        private final String word;
        private final List<Long> filePositions;

        /**
         *
         * @param word
         * @param initial
         */
        public WordEntry(String word, long initial) {
            this.word = word;
            this.filePositions = new ArrayList<>();
            this.filePositions.add(initial);
        }



        /**
         *
         * @param filePosition
         */
        public void addFilePosition(long filePosition) {
            this.filePositions.add(filePosition);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            WordEntry entry = (WordEntry) o;
            return Objects.equals(word, entry.word);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word);
        }

        @Override
        public String toString() {
            return "{\"" + word + "\" " + filePositions + '}';
        }
    }
}
