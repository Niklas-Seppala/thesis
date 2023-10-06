#include "windex.h"

#include <ctype.h>
#include <inttypes.h>
#include <limits.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "windex/utils.h"

#define EQ 0
#define WHITESPACE " \r\n"
#define RESIZE_TRESHOLD 0.75f

/**
 * @brief Implementation of opaque WordIndex type.
 */
struct wordindex {
    FILE *file;
    char *fname;
    struct hash_table table;
    size_t word_count;
};

// ------------------------------------------------------------
// Internal function prototypes
// ------------------------------------------------------------

static void do_indexing(WordIndex *index, size_t word_buffer_size);
static struct hash_entry *alloc_entry_chain(const char *word, size_t len, uint32_t hash,
                                            FilePosition pos);
static inline bool should_resize(size_t size, size_t cap);
static inline size_t file_pos_with_context(size_t word_position, uint32_t ctx);
static void do_compaction(WordIndex *index);
static void redistribute_hash_entries(WordIndex *index);
static void *read_words_with_ctx(WordIndex *index, struct pos_vec_iter *pos_iter,
                                 char *buffer, size_t buffer_size, uint32_t ctx,
                                 size_t word_len);
static inline uint32_t single_read_size(uint32_t context, size_t word_len);

// ------------------------------------------------------------
// Public API
// ------------------------------------------------------------

WordIndex *file_word_index_open(const char *filepath, size_t capacity,
                                size_t word_buffer_size, bool compact) {
    NONNULL(filepath);
    WordIndex *index = malloc(sizeof(WordIndex));

    index->file = fopen(filepath, "r");
    index->table.table = calloc(capacity, sizeof(struct hash_entry));
    index->table.capacity = capacity;

    const size_t fpath_len = strlen(filepath) + 1;
    index->fname = calloc(fpath_len, sizeof(char));
    strncpy(index->fname, filepath, fpath_len);

    index->word_count = 0;
    do_indexing(index, word_buffer_size);

    if (compact) {
        do_compaction(index);
    }
#ifdef DBG
    dbg(index->fname, index->word_count, &index->table);
#endif
    return index;
}

void *file_word_index_read_context_one_by_one(WordIndex *index, char *read_buffer,
                                              size_t read_buffer_size, char *word,
                                              size_t context, void *existing_iter) {
    NONNULL(index);
    NONNULL(read_buffer);
    NONNULL(word);

    normalize_word(word);

    struct hash_entry *chain = index->table.table[hash(word) % index->table.capacity];
    size_t single_read_space = single_read_size(context, chain->word_len);
    if (single_read_space > read_buffer_size) {
        PRINTF_ERROR("Illegal buffer size: %ld\n", read_buffer_size);
        return NULL;
    }

    if (existing_iter != NULL) {
        return read_words_with_ctx(index, existing_iter, read_buffer, single_read_space,
                                   context, chain->word_len);
    } else {
        while (chain != NULL) {
            if (strncmp(chain->word, word, chain->word_len) == EQ) {
                struct pos_vec_iter *iter = malloc(sizeof(struct pos_vec_iter));
                iter->index = 0;
                iter->vec = &chain->pos_vec;

                return read_words_with_ctx(index, iter, read_buffer, single_read_space,
                                           context, chain->word_len);
            }
            chain = chain->next;
        }
    }

    return NULL;
}

void *file_word_index_read_with_context_buffered(WordIndex *index, char *buffer,
                                                 size_t buffer_size, const char *word,
                                                 size_t word_len, size_t ctx,
                                                 void *existing_iter) {
    NONNULL(index);
    NONNULL(buffer);

    // Check if caller is continuing from previous position.
    if (existing_iter != NULL) {
        return read_words_with_ctx(index, existing_iter, buffer, buffer_size, ctx,
                                   word_len);
    }

    normalize_word(word);
    struct hash_entry *chain = index->table.table[hash(word) % index->table.capacity];

    // Find word from chain, and start writing words with their context to buffer.
    // Returns NULL, if all words fit to buffer, otherwise returns position to continue
    // from. See above.
    while (chain != NULL) {
        if (strncmp(chain->word, word, chain->word_len) == EQ) {
            struct pos_vec_iter *iter = malloc(sizeof(struct pos_vec_iter));
            iter->index = 0;
            iter->vec = &chain->pos_vec;
            return read_words_with_ctx(index, iter, buffer, buffer_size, ctx, word_len);
        }
        chain = chain->next;
    }

    // Word was not found.
    write_u32(buffer, BUFF_TERM_MARK);
    return NULL;
}

void file_word_index_close(WordIndex *index) {
    fclose(index->file);
    free(index->fname);
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
    free(index);
}

/**
 * @brief
 *
 * @param index
 * @param word
 * @param len
 * @param pos
 */
static void store_word(WordIndex *index, char *word, size_t len, FilePosition pos) {
    NONNULL(index);
    NONNULL(word);
    uint32_t hash_value = hash(word);

    const size_t capacity = index->table.capacity;
    struct hash_entry **chain = index->table.table + (hash_value % capacity);
    if (*chain == NULL) {
        // First entry for this bucket.
        index->word_count++;
        *chain = alloc_entry_chain(word, len, hash_value, pos);
    } else {
        struct hash_entry *chained_entry = *chain;
        do {
            if (strncmp(chained_entry->word, word, len) == EQ) {
                // Found existing entry of word.
                pos_vec_add(&chained_entry->pos_vec, pos);
                break;
            } else if (chained_entry->next == NULL) {
                // Hash collision, append word entry to chain.
                index->word_count++;
                chained_entry->next = alloc_entry_chain(word, len, hash_value, pos);
                break;
            }
        } while ((chained_entry = chained_entry->next));
    }
    if (should_resize(index->word_count, capacity) > RESIZE_TRESHOLD) {
        redistribute_hash_entries(index);
    }
}

/**
 * @brief
 *
 * @param index
 * @param word_buffer_size
 */
static void do_indexing(WordIndex *index, size_t word_buffer_size) {
    NONNULL(index);
    char word_buffer[word_buffer_size];

    size_t readBytes = 0, position_offset = 0, trunc_offset = 0;
    while ((readBytes = fread_unlocked(word_buffer + trunc_offset, sizeof(char),
                                       (word_buffer_size - 1) - trunc_offset,
                                       index->file)) != 0) {
        word_buffer[trunc_offset + readBytes] = '\0';

        char *str = strtok(word_buffer, WHITESPACE);
        while (str != NULL) {
            const size_t str_len = strlen(str);

            // Check if we need to move remaining word to start
            // of buffer, and continue reading to complete the
            // word.
            if (str + str_len == word_buffer + (word_buffer_size - 1)) {
                memmove(word_buffer, str, str_len);
                trunc_offset = str_len;
                break;
            }

            // Analyze token, and store it.
            FilePosition pos = position_offset + trunc_offset + (str - word_buffer);
            size_t new_len = normalize_word(str);
            store_word(index, str, new_len, pos);

            // Get next token.
            str = strtok(NULL, WHITESPACE);
        }
        trunc_offset = 0;
        position_offset += readBytes;
    }
}

/**
 * @brief
 *
 * @param word
 * @param len
 * @param hash
 * @param pos
 * @return struct hash_entry*
 */
static struct hash_entry *alloc_entry_chain(const char *word, size_t len, uint32_t hash,
                                            FilePosition pos) {
    struct hash_entry *new_entry = malloc(sizeof(struct hash_entry));
    new_entry->hash = hash;
    new_entry->word_len = len;
    pos_vec_init(&new_entry->pos_vec, pos);
    new_entry->next = NULL;
    new_entry->word = calloc(len + 1, sizeof(char));
    memccpy(new_entry->word, word, sizeof(char), len);
    return new_entry;
}

/**
 * @brief
 *
 * @param index
 */
static void redistribute_hash_entries(WordIndex *index) {
    struct hash_entry **old_table = index->table.table;
    size_t old_capacity = index->table.capacity;

    const size_t new_capacity = old_capacity << 1;
    index->table.table = calloc(new_capacity, sizeof(struct hash_entry **));
    index->table.capacity = new_capacity;

    for (size_t i = 0; i < old_capacity; i++) {
        struct hash_entry *entry = old_table[i];
        while (entry != NULL) {
            // Save next link and terminate chain.
            struct hash_entry *next_link = entry->next;
            entry->next = NULL;
            // TODO: doc
            struct hash_entry **new_chain =
                index->table.table + (entry->hash % new_capacity);
            if (*new_chain == NULL) {
                // TODO: doc
                *new_chain = entry;
            } else {
                // TODO: doc
                struct hash_entry *new_chained_entry = *new_chain;
                while (new_chained_entry->next != NULL)
                    new_chained_entry = new_chained_entry->next;
                new_chained_entry->next = entry;
            }
            entry = next_link;
        }
    }
    free(old_table);
}

/**
 * @brief
 *
 * @param index
 * @param pos_iter
 * @param buffer
 * @param buffer_size
 * @param ctx
 * @param word_len
 * @return void*
 */
static void *read_words_with_ctx(WordIndex *index, struct pos_vec_iter *pos_iter,
                                 char *buffer, size_t buffer_size, uint32_t ctx,
                                 size_t word_len) {
    size_t total_written = 0;
    uint32_t default_read_size = (ctx << 1) + word_len;

    while (pos_vec_iter_has_next(pos_iter)) {
        const FilePosition fpos = pos_vec_iter_next(pos_iter);
        // NOTE: reserve space for complete string with length + enough room for
        // TERM_BUFFER_MARK
        if ((total_written + default_read_size + (sizeof(uint32_t) << 1)) > buffer_size) {
            // No more room. Mark buffer and rollback iterator
            // so we can access same position next time.
            write_u32(buffer + total_written, BUFF_TERM_MARK);
            pos_iter->index--;
            return pos_iter;
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
        fseek(index->file, file_pos_with_context(fpos, ctx), SEEK_SET);
        size_t read_bytes = fread_unlocked(buffer + total_written + sizeof(uint32_t),
                                           sizeof(char), read_size, index->file);
        // Write string length before the read bytes.
        write_u32(buffer + total_written, read_bytes);

        total_written += read_bytes + sizeof(uint32_t);
    }

    // All word occurances in file read.
    write_u32(buffer + total_written, BUFF_TERM_MARK);
    free(pos_iter);
    return NULL;
}

/**
 * @brief
 *
 * @param index
 */
static void do_compaction(WordIndex *index) {
    for (size_t i = 0; i < index->table.capacity; i++) {
        if (index->table.table[i] != NULL) {
            struct pos_vec *v = &index->table.table[i]->pos_vec;
            v->capacity = v->length;
            index->table.table[i]->pos_vec.array =
                realloc(v->array, v->capacity * sizeof(FilePosition));
        }
    }
}

/**
 * @brief
 *
 * @param size
 * @param cap
 * @return true
 * @return false
 */
static inline bool should_resize(size_t size, size_t cap) {
    return (size / (float)cap) > RESIZE_TRESHOLD;
}

/**
 * @brief
 *
 * @param word_position
 * @param ctx
 * @return size_t
 */
static inline size_t file_pos_with_context(size_t word_position, uint32_t ctx) {
    return ((int)(word_position <= ctx ? 0 : ((int)word_position - (int)ctx)));
}

/**
 * @brief
 *
 * @param context
 * @param word_len
 * @return uint32_t
 */
static inline uint32_t single_read_size(uint32_t context, size_t word_len) {
    return (context * 2) + word_len + (sizeof(uint32_t) * 2);
}