package org.ns.thesis.wordindex;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JavaWordIndex implements WordIndex {
    private static final String READ_MODE = "r";
    private final String path;
    private final Map<@NotNull String, @NotNull WordEntry> index = new HashMap<>();
    private RandomAccessFile file;

    public JavaWordIndex(@NotNull String path) throws FileNotFoundException {
        this.file = new RandomAccessFile(path, READ_MODE);
        this.path = path;
        this.doIndexing();
    }

    private static long withContext(long pos, Context ctx) {
        return Math.max(pos - ctx.asInt(), 0);
    }

    @Override
    public String toString() {
        return index.toString();
    }

    @Override
    public @NotNull Collection<String> getWordWithContext(@NotNull String word,
                                                          @NotNull Context ctx) {
        long start, stop;
        start = System.currentTimeMillis();

        WordEntry entry = this.index.get(WordEntry.normalize(word));
        if (entry == null) {
            return List.of();
        }

        LinkedList<String> results = new LinkedList<>();
        try {
            if (!this.file.getChannel().isOpen()) {
                this.file = new RandomAccessFile(this.path, READ_MODE);
            }
            byte[] buffer = new byte[(ctx.asInt() << 1) + entry.word.length()];
            for (Long pos : entry.filePositions) {
                this.file.seek(withContext(pos, ctx));
                int readBytes = this.file.read(buffer);
                String str = new String(buffer, 0, readBytes, StandardCharsets.UTF_8);
                results.add(str);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stop = System.currentTimeMillis();

        System.err.println("Took " + (stop - start) + "ms to query \"" + word +
                "\"");
        return results;
    }

    @Override
    public @NotNull WordContextIterator getWordIteratorWithContext(@NotNull String word,
                                                                @NotNull Context ctx) {
        WordEntry entry = this.index.get(WordEntry.normalize(word));
        if (entry == null) {
            return (WordContextIterator)(Object)Collections.emptyIterator();
        }
        try {
            return new JavaWordContextIterator(new RandomAccessFile(this.path, READ_MODE),
                    ctx,
                    entry);
        } catch (FileNotFoundException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return (WordContextIterator)(Object)Collections.emptyIterator();
        }
    }

    @Override
    public void close() throws IOException {
        this.file.close();
    }

    protected void doIndexing() {
        long start, stop;
        start = System.currentTimeMillis();
        try {
            long filePosition = 0L;
            String line;
            while ((line = this.file.readLine()) != null) {
                for (int i = 0, j = 0; i < line.length(); i++) {
                    if (Character.isWhitespace(
                            line.charAt(i)) || i + 1 == line.length()) {
                        if (i > j) {
                            if (i + 1 == line.length()) {
                                i++;
                            }
                            String word = WordEntry.normalize(line.substring(j, i));
                            long pos = filePosition + j;
                            WordEntry existingWordEntry = this.index.get(word);
                            if (existingWordEntry != null) {
                                existingWordEntry.addFilePosition(pos);
                            } else {
                                this.index.put(word, new WordEntry(word, pos));
                            }
                            j = i + 1;
                        }
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
                //noinspection CallToPrintStackTrace
                ioe.printStackTrace();
            }
        }
        stop = System.currentTimeMillis();

        System.err.println("Took " + (stop - start) + "ms to index");
    }

    private static class JavaWordContextIterator implements WordContextIterator {

        private final RandomAccessFile file;
        private final Context ctx;
        private final Iterator<Long> positions;
        private final byte[] buffer;


        public JavaWordContextIterator(@NotNull RandomAccessFile file, @NotNull Context ctx,
                                   @NotNull WordEntry entry) {
            this.file = file;
            this.ctx = ctx;
            this.positions = entry.filePositions.iterator();
            this.buffer = new byte[(ctx.asInt() << 1) + entry.word.length()];
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
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    private static class WordEntry {
        private final String word;
        private final List<Long> filePositions;

        public WordEntry(String word, long initial) {
            this.word = word;
            this.filePositions = new ArrayList<>();
            this.filePositions.add(initial);
        }

        public static String normalize(String word) {
            return word.replaceAll("\\W", "").toLowerCase();
        }

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
