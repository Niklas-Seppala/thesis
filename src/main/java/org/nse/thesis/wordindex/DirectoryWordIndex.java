package org.nse.thesis.wordindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DirectoryWordIndex implements AutoCloseable {

    private final Map<String, WordIndex> indexMap;

    public DirectoryWordIndex(String dirPath, WordIndex.IndexProvider indexProvider) throws FileNotFoundException {

        final Path path = Path.of(dirPath);

        if (Files.notExists(path)) {
            throw new FileNotFoundException(dirPath);
        }
        if (!Files.isDirectory(path)) {
            throw new FileNotFoundException(
                    String.format("%s is not a directory", dirPath));
        }
        File dir = path.toFile();
        assert dir.canRead();

        this.indexMap = new HashMap<>();
        File[] files = dir.listFiles();
        if (files != null) {
            indexFilesInDirectory(Arrays.asList(files), indexProvider);
        }
    }

    private void indexFilesInDirectory(List<File> files,
                                       WordIndex.IndexProvider indexProvider) {
        if (files.isEmpty()) {
            return;
        }
        for (File file : files) {
            String fileName = file.getName();
            if (!indexMap.containsKey(fileName)) {
                try {
                    WordIndex index =
                            indexProvider.indexFrom(file.getAbsolutePath());
                    this.indexMap.put(fileName, index);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Collection<String> getWordsWithContextInFile(String path, String word,
                                                        WordIndex.ContextBytes ctx) {
        String fileName = Path.of(path).getFileName().toString();
        WordIndex index = this.indexMap.get(fileName);
        if (index != null) {
            return index.getWords(word, ctx);
        } else {
            return null;
        }
    }

    public Iterator<String> iterateWordsWithContextInFile(String path, String word,
                                                          WordIndex.ContextBytes ctx)
            throws FileNotFoundException {
        String fileName = Path.of(path).getFileName().toString();
        WordIndex index = this.indexMap.get(fileName);
        if (index != null) {
            return index.iterateWords(word, ctx);
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        this.indexMap.values().forEach(index -> {
            try {
                index.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
