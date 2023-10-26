package org.nse.thesis.wordindex;

import org.jetbrains.annotations.NotNull;
import org.nse.thesis.wordindex.analyzers.IndexAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DirectoryWordIndex implements AutoCloseable {
    private final Map<String, WordIndex> indexMap;
    private final String dirPath;
    private final IndexAnalyzer analyzer;

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

    @NotNull
    public List<File> files() {
        return this.indexMap.keySet().stream()
                .map(fileName ->
                        new File(this.dirPath + File.separator + fileName))
                .collect(Collectors.toList());
    }
}
