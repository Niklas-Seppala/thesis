package org.nse.thesis.wordindex;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.analyzers.IndexAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WordIndex for files located in directory.
 *
 * @author Niklas Seppälä
 */
public class DirectoryWordIndex implements AutoCloseable {
    private final Map<String, WordIndex> indexMap;
    private final String dirPath;
    private final IndexAnalyzer analyzer;

    /**
     * Constructor
     * @param dirPath Path to directory to be indexed.
     * @param analyzer Analyzer used by indexes.
     * @param indexProvider Method to provide implementation of {@link WordIndex}
     * @throws FileNotFoundException When directory doesn't exist
     */
    public DirectoryWordIndex(@NotNull String dirPath, @NotNull IndexAnalyzer analyzer,
                              @NotNull WordIndex.Provider indexProvider)
            throws FileNotFoundException {
        final Path path = Path.of(dirPath);

        if (Files.notExists(path)) {
            throw new FileNotFoundException(dirPath);
        }
        if (!Files.isDirectory(path)) {
            throw new FileNotFoundException(
                    String.format("%s is not a directory", dirPath));
        }
        this.dirPath = path.toAbsolutePath().toString();
        this.analyzer = analyzer;
        File dir = path.toFile();

        this.indexMap = new HashMap<>();
        File[] files = dir.listFiles();
        if (files != null) {
            indexFilesInDirectory(Arrays.asList(files), indexProvider);
        }
    }

    /**
     * Indexes all text files in the directory.
     *
     * @param files List of files located in directory
     * @param indexProvider Method to provide implementation of {@link WordIndex}
     */
    private void indexFilesInDirectory(@NotNull List<File> files,
                                       @NotNull WordIndex.Provider indexProvider) {
        if (files.isEmpty()) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            if (!indexMap.containsKey(fileName)) {
                try {
                    WordIndex index =
                            indexProvider.indexFrom(file.getAbsolutePath(), this.analyzer);
                    this.indexMap.put(fileName, index);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Queries the index for word occurrences.
     *
     * @param path Path to the file to search word from.
     * @param word Word to search for.
     * @param ctx Context size in bytes.
     *
     * @return List of words wrapped in context.
     */
    @NotNull
    public Collection<String> getWordsWithContextInFile(@NotNull String path, @NotNull String word,
                                                        @NotNull WordIndex.ContextBytes ctx) {
        String fileName = Path.of(path).getFileName().toString();
        WordIndex index = this.indexMap.get(fileName);
        if (index != null) {
            return index.getWords(word, ctx);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Queries the index for word occurrences, and iterates the results.
     *
     * @param path Path to the file to search word from.
     * @param word Word to search for.
     * @param ctx Context size in bytes.
     *
     * @return Iterator that iterates over the results.
     *
     * @throws FileNotFoundException If file was not indexed, or was removed.
     */
    @NotNull
    public Iterator<String> iterateWordsWithContextInFile(@NotNull String path, @NotNull String word,
                                                          @NotNull WordIndex.ContextBytes ctx)
            throws FileNotFoundException {
        String fileName = Path.of(path).getFileName().toString();
        WordIndex index = this.indexMap.get(fileName);
        if (index != null) {
            return index.iterateWords(word, ctx);
        } else {
            return Collections.emptyIterator();
        }
    }

    /**
     * Closes all {@link WordIndex}s
     */
    @Override
    public void close() {
        for (WordIndex index : this.indexMap.values()) {
            try {
                index.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the list of files indexed by this object.
     * @return List of indexed files.
     */
    @NotNull
    public List<File> files() {
        return this.indexMap.keySet().stream()
                .map(fileName ->
                        new File(this.dirPath + File.separator + fileName))
                .collect(Collectors.toList());
    }
}
