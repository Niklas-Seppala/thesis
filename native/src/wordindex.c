/**
 * @file wordindex.c
 * @author Niklas Seppälä
 * @copyright Copyright (c) 2023
 */

#include "wordindex.h"

#include <ctype.h>
#include <inttypes.h>
#include <limits.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "wordindex/utils.h"

#define EQ 0
#define RESIZE_TRESHOLD 0.75f

static int god_count = 0;

/**
 * @brief Implementation of opaque WordIndex type.
 */
struct wordindex {
    // FILE *file;
    char *fname;
    struct hash_table table;
    size_t word_count;
};

// ------------------------------------------------------------
// Internal function prototypes
// ------------------------------------------------------------

static bool index_file(WordIndex *index, FILE *file, size_t word_buffer_size);
static struct hash_entry *alloc_entry(const char *word, size_t len, uint32_t hash,
                                      FilePosition pos);
static inline bool index_should_resize(size_t size, size_t cap);
static inline size_t file_pos_with_context(size_t word_position, uint32_t ctx);
static void do_compaction(WordIndex *index);
static void resize(WordIndex *index);
static void redistribute_hash_entries(size_t old_capacity, struct hash_entry **old_table,
                                      size_t new_capacity, struct hash_entry **new_table);
static struct index_read_iterator *read_words_with_txt_to_buffer(
    struct index_read_iterator *read_iterator, char *buffer, size_t buffer_size,
    uint32_t ctx, size_t word_len);
static inline uint32_t single_read_size(uint32_t context, size_t word_len);

// ------------------------------------------------------------
// Public API
// ------------------------------------------------------------

WordIndex *file_word_index_open(const char *filepath, size_t capacity,
                                size_t word_buffer_size, bool compact) {
    NONNULL(filepath);
    WordIndex *index = malloc(sizeof(WordIndex));
    if (index == NULL) {
        return NULL;
    }

    // TODO what the hell was going on here???
    // TODO index->table.table = calloc(capacity, sizeof(struct hash_entry));
    struct hash_entry **table = calloc(capacity, sizeof(uintptr_t));
    if (table == NULL) {
        PRINTF_ERROR("%s", ALLOC_ERR);
        file_word_index_close(index);
        return NULL;
    }
    index->table.table = table;
    index->table.capacity = capacity;

    const size_t fpath_len = strlen(filepath) + 1;
    char *fname = calloc(fpath_len, sizeof(char));
    if (fname == NULL) {
        PRINTF_ERROR("%s", ALLOC_ERR);
        file_word_index_close(index);
        return NULL;
    }
    strncpy(fname, filepath, fpath_len);
    index->fname = fname;

    index->word_count = 0;
    FILE *file = fopen(index->fname, "r");
    if (file != NULL) {
        bool success = index_file(index, file, word_buffer_size);
        fclose(file);
        if (!success) {
            file_word_index_close(index);
            return NULL;
        }
    } else {
        PRINTF_ERROR_WITH_ERRNO("Could not open a file %s to index", index->fname);
        file_word_index_close(index);
        return NULL;
    }

    if (compact) {
        do_compaction(index);
    }
#ifdef DBG
    dbg(index->fname, index->word_count, &index->table);
#endif
    return index;
}

void *file_word_index_read_with_context_buffered(WordIndex *index, char *buffer,
                                                 size_t buffer_size, const char *word,
                                                 size_t word_len, size_t ctx,
                                                 void *read_iterator) {
    NONNULL(index);
    NONNULL(buffer);

    // Check if caller is continuing from previous position.
    if (read_iterator != NULL) {
        return read_words_with_txt_to_buffer(read_iterator, buffer, buffer_size, ctx,
                                             word_len);
    }

    normalize_word(word);
    struct hash_entry *chain = index->table.table[hash(word) % index->table.capacity];

    // Find word from chain, and start writing words with their context to buffer.
    // Returns NULL, if all words fit to buffer, otherwise returns position to continue
    // from. See above.
    while (chain != NULL) {
        if (strncmp(chain->word, word, chain->word_len) == EQ) {
            struct index_read_iterator *new_iterator =
                malloc(sizeof(struct index_read_iterator));
            if (new_iterator == NULL) {
                PRINTF_ERROR("%s", ALLOC_ERR);
                return NULL;
            }
            new_iterator->index = 0;
            new_iterator->file = fopen(index->fname, "r");
            new_iterator->vec = &chain->pos_vec;
            return read_words_with_txt_to_buffer(new_iterator, buffer, buffer_size, ctx,
                                                 word_len);
        }
        chain = chain->next;
    }

    // Word was not found.
    write_u32(buffer, BUFF_TERM_MARK);
    return NULL;
}

void file_word_index_close_iterator(struct index_read_iterator *iter) {
    if (iter != NULL) {
        if (iter->file != NULL) {
            fclose(iter->file);
        }
        free(iter);
    }
}

void file_word_index_close(WordIndex *index) {
    if (index == NULL) {
        return;
    }

    if (index->fname != NULL) {
        free(index->fname);
    }
    if (index->table.table != NULL) {
        for (size_t i = 0; i < index->table.capacity; i++) {
            struct hash_entry *entry = index->table.table[i];
            while (entry != NULL) {
                struct hash_entry *next = entry->next;
                free(entry->pos_vec.array);
                free(entry->word);
                free(entry);
                entry = next;
            }
        }
        free(index->table.table);
    }
    free(index);
}

/**
 * @brief Stores word occurance at specified FilePosition
 * to the index. If word when word already exist in the index,
 * only file position is stored.
 *
 * @param index WordIndex
 * @param word word
 * @param word_len word length
 * @param pos file position
 *
 * @return true  - When word was added to index.
 * @return false - When adding failed.
 */
static bool index_word_at_position(WordIndex *index, char *word, size_t word_len,
                                   FilePosition pos) {
    NONNULL(index);
    NONNULL(word);
    uint32_t hash_value = hash(word);

    if (strcmp(word, "god") == EQ) {
        god_count++;
    }

    const size_t capacity = index->table.capacity;
    struct hash_entry **bucket = index->table.table + (hash_value % capacity);
    if (*bucket == NULL) {
        // First entry for this bucket.
        index->word_count++;
        *bucket = alloc_entry(word, word_len, hash_value, pos);
        if (bucket == NULL) {
            // Failed to allocate bucket.
            return false;
        }
    } else {
        struct hash_entry *entry = *bucket;
        do {
            if (strncmp(entry->word, word, word_len) == EQ) {
                // Found existing entry of word.
                if (!pos_vec_add(&entry->pos_vec, pos)) {
                    // Failed to add position to existing entry.
                    return false;
                } else {
                    break;
                }
            } else if (entry->next == NULL) {
                // Hash collision, add word new entry to the bucket.
                index->word_count++;
                struct hash_entry *new_entry =
                    alloc_entry(word, word_len, hash_value, pos);
                if (new_entry == NULL) {
                    // Failed to allocate new entry.
                    return false;
                } else {
                    entry->next = new_entry;
                    break;
                }
            }
        } while ((entry = entry->next));
    }
    if (index_should_resize(index->word_count, capacity)) {
        resize(index);
    }
    return true;
}

/**
 * @brief Reads the file in buffered manner,
 * and tokenizes the words and stores their file
 * positions to index.
 *
 * @param index Index object.
 * @param file Open file to read from.
 * @param word_buffer_size Size of a buffer used in reading
 *                         words from the file.
 *
 * @return true - When indexing was succesful.
 * @return false When indexing failed.
 */
static bool index_file(WordIndex *index, FILE *file, size_t word_buffer_size) {
    NONNULL(index);
    char word_buffer[word_buffer_size];
    char *next_read_position_in_buffer = word_buffer;
    size_t readBytes = 0, position_offset = 0, truncate_offset = 0;

    const char *WHITESPACE = " \r\n";
    while ((readBytes = fread_unlocked(next_read_position_in_buffer, sizeof(char),
                                       (word_buffer_size - 1) - truncate_offset, file)) !=
           0) {
        // We use stdlib string functions, so let's make sure last string is
        // terminated. This is taken into account, we read in buffsize-1 bytes.
        word_buffer[truncate_offset + readBytes] = '\0';

        char *str = strtok(word_buffer, WHITESPACE);
        while (str != NULL) {
            const size_t str_len = strlen(str);

            // Check if we need to move remaining word to start
            // of buffer, and continue reading to complete the
            // word.
            if (str + str_len == word_buffer + (word_buffer_size - 1)) {
                memmove(word_buffer, str, str_len);
                truncate_offset = str_len;
                break;
            }

            // Store the word and it's file position
            FilePosition pos = position_offset - truncate_offset + (str - word_buffer);
            size_t new_len = normalize_word(str);
            bool success = index_word_at_position(index, str, new_len, pos);
            if (!success) {
                PRINTF_ERROR("Failed to add word %s to the index", str);
                return false;
            }

            // Get next token.
            str = strtok(NULL, WHITESPACE);
            if (str == NULL) {
                // Reading buffer is done. Didn't truncate, so reset to zero.
                truncate_offset = 0;
            }
        }
        // Set the next read position. If we had to truncate, next read should
        // 'continue' after truncated bytes.
        next_read_position_in_buffer = word_buffer + truncate_offset;
        position_offset += readBytes;
    }
    return true;
}

/**
 * @brief Allocates entry object in heap.
 *
 * @param word Word string
 * @param len word length
 * @param hash hash value of the word
 * @param pos word's file positon
 *
 * @return struct hash_entry* Pointer to new entry.
 */
static struct hash_entry *alloc_entry(const char *word, size_t len, uint32_t hash,
                                      FilePosition pos) {
    struct hash_entry *new_entry = malloc(sizeof(struct hash_entry));
    if (new_entry == NULL) {
        PRINTF_ERROR("%s", ALLOC_ERR);
        return NULL;
    }
    new_entry->hash = hash;
    new_entry->word_len = len;
    if (!pos_vec_init(&new_entry->pos_vec, pos)) {
        // Failed to initialize entry.
        free(new_entry);
        return NULL;
    }
    new_entry->next = NULL;
    char *word_buffer = calloc(len + 1, sizeof(char));
    if (word_buffer == NULL) {
        PRINTF_ERROR("%s", ALLOC_ERR);
        free(new_entry);
        return NULL;
    }
    new_entry->word = word_buffer;
    memccpy(new_entry->word, word, sizeof(char), len);
    return new_entry;
}

/**
 * @brief
 *
 * @param index
 */
static void resize(WordIndex *index) {
    size_t old_capacity = index->table.capacity;
    struct hash_entry **old_table = index->table.table;

    const size_t new_capacity = old_capacity << 1;
    struct hash_entry **new_table = calloc(new_capacity, sizeof(struct hash_entry **));
    if (new_table == NULL) {
        PRINTF_ERROR("%s", ALLOC_ERR);
        // Nothing to do.
        return;
    }
    redistribute_hash_entries(old_capacity, old_table, new_capacity, new_table);

    index->table.table = new_table;
    index->table.capacity = new_capacity;

    free(old_table);
}

/**
 * @brief When index needs to resize, entries need to be
 * redistributed into new hash container. We can avoid rehashing,
 * as hash value is stored in each entry.
 *
 * @param old_capacity Old hashtable capacity.
 * @param old_table  Old hashtable hash container.
 * @param new_capacity New hashtable capacity.
 * @param new_table New hashtable hash container.
 */
static void redistribute_hash_entries(size_t old_capacity, struct hash_entry **old_table,
                                      size_t new_capacity,
                                      struct hash_entry **new_table) {
    for (size_t i = 0; i < old_capacity; i++) {
        struct hash_entry *entry = old_table[i];
        while (entry != NULL) {
            const uint32_t hash_slot = entry->hash % new_capacity;

            struct hash_entry *next = entry->next;
            entry->next = NULL;

            struct hash_entry *entry_at_new_position = new_table[hash_slot];
            if (entry_at_new_position == NULL) {
                // Free slot, store entry here.
                new_table[hash_slot] = entry;
            } else {
                // Find a place at existing bucket and store there.
                while (entry_at_new_position->next != NULL) {
                    entry_at_new_position = entry_at_new_position->next;
                }
                entry_at_new_position->next = entry;
            }

            entry = next;
        }
    }
}

/**
 * @brief Reads words with context from index to fresh buffer. If
 * results remain to be read after buffer is full, iterator object
 * is returned.
 *
 * This iterator should be passed as parameter to next method call.
 * If no more reads are needed, you must free the iterator object
 * yourself.
 *
 * @param index Word index object.
 * @param pos_iter Iterator, first time NULL
 * @param buffer buffer to read results into
 * @param buffer_size Size of the read buffer
 * @param ctx Context size.
 * @param word_len Length of the word.
 *
 * @return struct pos_vec_iter * Iterator object to continue reading.
 */
static struct index_read_iterator *read_words_with_txt_to_buffer(
    struct index_read_iterator *read_iterator, char *buffer, size_t buffer_size,
    uint32_t ctx, size_t word_len) {
    size_t total_written = 0;
    uint32_t default_read_size = (ctx << 1) + word_len;

    while (pos_vec_iter_has_next(read_iterator)) {
        const FilePosition fpos = pos_vec_iter_next(read_iterator);
        // NOTE: reserve space for complete string with length + enough room for
        // TERM_BUFFER_MARK
        if ((total_written + default_read_size + (sizeof(uint32_t) << 1)) > buffer_size) {
            // No more room. Mark buffer and rollback iterator
            // so we can access same position next time.
            write_u32(buffer + total_written, BUFF_TERM_MARK);
            read_iterator->index--;
            return read_iterator;
        }

        const int truncate_beginning = (int)(fpos - ctx);
        // Define how much we should read from file, starting from file_pos - ctx.
        // Default read size is ctx bytes on both sides of the word.
        uint32_t read_size = default_read_size;
        if (truncate_beginning < 0) {
            // There's not enough ctx at the beginning, take this into account
            // when reading from file.
            read_size = default_read_size + truncate_beginning;
        }

        // Seek and read word with ctx into buffer, after 4 bytes reserved
        // for string size.
        fseek(read_iterator->file, file_pos_with_context(fpos, ctx), SEEK_SET);
        size_t read_bytes = fread_unlocked(buffer + total_written + sizeof(uint32_t),
                                           sizeof(char), read_size, read_iterator->file);
        // Write string length before the read bytes.
        write_u32(buffer + total_written, read_bytes);

        total_written += read_bytes + sizeof(uint32_t);
    }

    // All word occurances in file read.
    write_u32(buffer + total_written, BUFF_TERM_MARK);
    file_word_index_close_iterator(read_iterator);
    return NULL;
}

/**
 * @brief Compacts all saved word entry file position vectors
 * to their current capacity.
 *
 * @param index Word index object.
 */
static void do_compaction(WordIndex *index) {
    for (size_t i = 0; i < index->table.capacity; i++) {
        if (index->table.table[i] != NULL) {
            struct pos_vec *v = &index->table.table[i]->pos_vec;
            v->capacity = v->length;
            FilePosition *arr = realloc(v->array, v->capacity * sizeof(FilePosition));
            if (arr == NULL) {
                PRINTF_ERROR("%s", ALLOC_ERR);
                break;
            } else {
                index->table.table[i]->pos_vec.array = arr;
            }
        }
    }
}

/**
 * @brief Determines if index should do resizing.
 *
 * @param size Current size of the index.
 * @param cap Capacity of the index hashmap.
 *
 * @return true when index should resize
 * @return false when not
 */
static inline bool index_should_resize(size_t size, size_t cap) {
    return (size / (float)cap) > RESIZE_TRESHOLD;
}

/**
 * @brief Calculate where to start reading for word, when context
 * is taken into equation.
 *
 * @param word_position Position of the word in a file
 * @param ctx Size of the context.
 *
 * @return size_t Real file position to start reading.
 */
static inline size_t file_pos_with_context(size_t word_position, uint32_t ctx) {
    return ((int)(word_position <= ctx ? 0 : ((int)word_position - (int)ctx)));
}

/**
 * @brief Calculate size of a word read with context in bytes.
 *
 * @param context Size of the context
 * @param word_len Word lenght.
 *
 * @return uint32_t Single read size in bytes
 */
static inline uint32_t single_read_size(uint32_t context, size_t word_len) {
    return (context * 2) + word_len + (sizeof(uint32_t) * 2);
}
