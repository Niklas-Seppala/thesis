#include "wordindex.h"

#include <ctype.h>
#include <inttypes.h>
#include <limits.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

#include "therror.h"

#ifndef VEC_DEF_CAP
#define VEC_DEF_CAP 8
#endif

#define READ_MODE "r"
#define EQ 0
#define WHITESPACE " \r\n"
#define RESIZE_TRESHOLD 0.75f
#define SHOULD_RESIZE(size, cap) (size / (float)cap)
#define POS_WITH_CTX(pos, ctx) ((int)(pos <= ctx ? 0 : ((int)pos - (int)ctx)))

typedef size_t FilePosition;

/**
 * @brief
 *
 * Class:     org_ns_thesis_wordindex_NativeWordIndex
 * Method:    wordIndexOpen
 * Signature: (Ljava/lang/String;JJZ)J
 *
 * @see http://www.cse.yorku.ca/~oz/hash.html
 *
 * @param word
 * @return uint32_t
 */
static uint32_t sdbm_str_hash(const void *word);

/**
 * @brief
 *
 * @param index
 */
static void do_indexing(WordIndex *index, size_t word_buffer_size);

/**
 * @brief
 *
 * @param word
 * @param len
 * @param hash
 * @param pos
 * @return struct entry*
 */
static inline struct entry *alloc_entry_chain(const char *word, size_t len, uint32_t hash,
                                              FilePosition pos);

/**
 * @brief Construct a new compact vectors object
 *
 * @param index
 */
static inline void do_compaction(WordIndex *index);

struct pos_vec {
    size_t capacity;
    size_t length;
    FilePosition *array;
};

struct pos_vec_iter {
    size_t index;
    struct pos_vec *vec;
};

/**
 * @brief
 *
 * @param iter
 * @return true
 * @return false
 */
static inline bool pos_vec_iter_has_next(struct pos_vec_iter *iter);

/**
 * @brief
 *
 * @param iter
 * @return FilePosition
 */
static inline FilePosition pos_vec_iter_next(struct pos_vec_iter *iter);

/**
 * @brief
 *
 * @param vec
 * @param initial
 */
static inline void pos_vec_init(struct pos_vec *vec, FilePosition initial);

/**
 * @brief
 *
 * @param vec
 * @param pos
 */
static inline void pos_vec_add(struct pos_vec *vec, FilePosition pos);

#ifdef DBG

#ifdef DBG_POS
/**
 * @brief
 *
 * @param positions
 */
static void dbg_positions(struct pos_vec *pos_vec);
#endif

/**
 * @brief
 *
 * @param index
 */
static void dbg(WordIndex *index);
#endif

/**
 * @brief
 *
 * @param word
 * @return size_t
 */
static size_t normalize_word(char *word);

/**
 * @brief
 *
 * @param index
 */
static void redistribute_hash_entries(WordIndex *index);

/**
 * @brief
 *
 * @param bytes
 * @param value
 */
static inline void write_u32(char *bytes, uint32_t value);

static void *read_words_with_ctx(WordIndex *index, struct pos_vec_iter *pos_iter,
                                 char *buffer, size_t buffer_size, uint32_t ctx,
                                 size_t word_len);

struct entry {
    char *word;
    size_t word_len;
    uint32_t hash;
    struct pos_vec pos_vec;
    struct entry *next;
};

struct hash_table {
    size_t capacity;
    struct entry **table;
};

/**
 * @brief Implementation of opaque WordIndex type.
 */
struct wordindex {
    FILE *file;
    char *fname;
    struct hash_table table;
    size_t word_count;
};

WordIndex *file_word_index_open(const char *filepath, size_t capacity,
                                size_t word_buffer_size, bool compact) {
    NONNULL(filepath);
    WordIndex *index = malloc(sizeof(WordIndex));

    index->file = fopen(filepath, READ_MODE);
    index->table.table = calloc(capacity, sizeof(struct entry));
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
    dbg(index);
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

    struct entry *chain = index->table.table[sdbm_str_hash(word) % index->table.capacity];
    size_t limit_to_single_read = (context * 2) + chain->word_len + sizeof(uint32_t);
    if (limit_to_single_read > read_buffer_size) {
        PRINTF_ERROR("Illegal buffer size: %ld\n", read_buffer_size);
        return NULL;
    }

    if (existing_iter != NULL) {
        return read_words_with_ctx(index, existing_iter, read_buffer,
                                   limit_to_single_read, context, chain->word_len);
    } else {
        while (chain != NULL) {
            if (strncmp(chain->word, word, chain->word_len) == EQ) {
                struct pos_vec_iter *iter = malloc(sizeof(struct pos_vec_iter));
                iter->index = 0;
                iter->vec = &chain->pos_vec;

                return read_words_with_ctx(index, iter, read_buffer, limit_to_single_read,
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
    struct entry *chain = index->table.table[sdbm_str_hash(word) % index->table.capacity];

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
    write_u32(buffer, TERM_BUFFER_MARK);
    return NULL;
}

void file_word_index_close(WordIndex *index) {
    fclose(index->file);
    free(index->fname);
    for (size_t i = 0; i < index->table.capacity; i++) {
        struct entry *entry = index->table.table[i];
        while (entry != NULL) {
            struct entry *next = entry->next;
            free(entry->pos_vec.array);
            free(entry->word);
            free(entry);
            entry = next;
        }
    }
    free(index->table.table);
    free(index);
}

static void store(WordIndex *index, char *word, size_t len, FilePosition pos) {
    NONNULL(index);
    NONNULL(word);
    uint32_t hash = sdbm_str_hash(word);

    const size_t capacity = index->table.capacity;
    struct entry **chain = index->table.table + (hash % capacity);
    if (*chain == NULL) {
        // First entry for this bucket.
        index->word_count++;
        *chain = alloc_entry_chain(word, len, hash, pos);
    } else {
        struct entry *chained_entry = *chain;
        do {
            if (strncmp(chained_entry->word, word, len) == EQ) {
                // Found existing entry of word.
                pos_vec_add(&chained_entry->pos_vec, pos);
                break;
            } else if (chained_entry->next == NULL) {
                // Hash collision, append word entry to chain.
                index->word_count++;
                chained_entry->next = alloc_entry_chain(word, len, hash, pos);
                break;
            }
        } while ((chained_entry = chained_entry->next));
    }
    if (SHOULD_RESIZE(index->word_count, capacity) > RESIZE_TRESHOLD) {
        redistribute_hash_entries(index);
    }
}

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
            store(index, str, new_len, pos);

            // Get next token.
            str = strtok(NULL, WHITESPACE);
        }
        trunc_offset = 0;
        position_offset += readBytes;
    }
}

static inline uint32_t sdbm_str_hash(const void *word) {
    if (word == NULL) return 0;

    const uint8_t *str = word;
    uint32_t hash = 0;
    int c;
    while ((c = *str++)) hash = c + (hash << 6) + (hash << 16) - hash;

    return hash;
}

static inline struct entry *alloc_entry_chain(const char *word, size_t len, uint32_t hash,
                                              FilePosition pos) {
    struct entry *new_entry = malloc(sizeof(struct entry));
    new_entry->hash = hash;
    new_entry->word_len = len;
    pos_vec_init(&new_entry->pos_vec, pos);
    new_entry->next = NULL;
    new_entry->word = calloc(len + 1, sizeof(char));
    memccpy(new_entry->word, word, sizeof(char), len);
    return new_entry;
}

static void redistribute_hash_entries(WordIndex *index) {
    struct entry **old_table = index->table.table;
    size_t old_capacity = index->table.capacity;

    const size_t new_capacity = old_capacity << 1;
    index->table.table = calloc(new_capacity, sizeof(struct entry **));
    index->table.capacity = new_capacity;

    for (size_t i = 0; i < old_capacity; i++) {
        struct entry *entry = old_table[i];
        while (entry != NULL) {
            // Save next link and terminate chain.
            struct entry *next_link = entry->next;
            entry->next = NULL;
            // TODO: doc
            struct entry **new_chain = index->table.table + (entry->hash % new_capacity);
            if (*new_chain == NULL) {
                // TODO: doc
                *new_chain = entry;
            } else {
                // TODO: doc
                struct entry *new_chained_entry = *new_chain;
                while (new_chained_entry->next != NULL)
                    new_chained_entry = new_chained_entry->next;
                new_chained_entry->next = entry;
            }
            entry = next_link;
        }
    }
    free(old_table);
}

static size_t normalize_word(char *word) {
    NONNULL(word);
    char c;
    char *d = word;
    size_t length = 0;
    while ((c = *word++)) {
        if (ispunct(c)) {
            *(word - 1) = '\0';
            break;
        }
        length++;
    }
    for (; *d; d++) *d = tolower(*d);
    return length;
}

static inline void write_u32(char *bytes, uint32_t value) {
    *bytes++ = value & 0xff;
    *bytes++ = (value >> 8) & 0xff;
    *bytes++ = (value >> 16) & 0xff;
    *bytes++ = (value >> 24) & 0xff;
}

static void *read_words_with_ctx(WordIndex *index, struct pos_vec_iter *pos_iter,
                                 char *buffer, size_t buffer_size, uint32_t ctx,
                                 size_t word_len) {
    size_t total_written = 0;
    uint32_t default_read_size = (ctx << 1) + word_len;

    while (pos_vec_iter_has_next(pos_iter)) {
        const FilePosition fpos = pos_vec_iter_next(pos_iter);
        // TODO: DOC BUG, fixed by reserving space for TERM_BUFFER_MARK
        /*
==22234== Invalid write of size 1
==22234==    at 0x10A3DB: write_u32 (wordindex.c:433)
==22234==    by 0x10A473: read_words_with_ctx (wordindex.c:448)
==22234==    by 0x109AC5: file_word_index_read_with_context_buffered (wordindex.c:265)
==22234==    by 0x10A807: main (test.c:23)
==22234==  Address 0x67807d1 is 1 bytes after a block of size 4,096 alloc'd
==22234==    by 0x10A7C5: main (test.c:17)
        */
        if ((total_written + default_read_size + (sizeof(uint32_t) << 1)) > buffer_size) {
            // No more room. Mark buffer and rollback iterator
            // so we can access same position next time.
            write_u32(buffer + total_written, TERM_BUFFER_MARK);
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
        fseek(index->file, POS_WITH_CTX(fpos, ctx), SEEK_SET);
        size_t read_bytes = fread_unlocked(buffer + total_written + sizeof(uint32_t),
                                           sizeof(char), read_size, index->file);
        // Write string length before the read bytes.
        write_u32(buffer + total_written, read_bytes);

        total_written += read_bytes + sizeof(uint32_t);
    }

    // All word occurances in file read.
    write_u32(buffer + total_written, TERM_BUFFER_MARK);
    free(pos_iter);
    return NULL;
}

/*
 ------------------------------------------------
 --------------------- DEBUG --------------------
 ------------------------------------------------
*/

#ifdef DBG
#ifdef DBG_POS
static void dbg_positions(struct pos_vec *pos_vec) {
    fprintf(stdout, " %ld/%ld - [", pos_vec->length, pos_vec->capacity);
    for (size_t i = 0; i < pos_vec->length; i++) {
        fprintf(stdout, "%ld", pos_vec->array[i]);
        if (i + 1 < pos_vec->length) {
            putc(',', stdout);
        }
    }
    fprintf(stdout, "]");
}
#endif

static void dbg(WordIndex *index) {
    fprintf(stdout, "[WordIndex: (%s) table-capacity: %ld words: %ld]\n", index->fname,
            index->table.capacity, index->word_count);

#ifdef DBG_HASH
    for (size_t i = 0; i < index->table.capacity; i++) {
        struct entry *chain = index->table.table[i];
        if (chain == NULL) continue;
        fprintf(stdout, "table[%ld] == ", i);
        while (chain != NULL) {
            fprintf(stdout, "(\"%s\"", chain->word);
#ifdef DBG_POS
            dbg_positions(&chain->pos_vec);
#endif
            putc(')', stdout);
            chain = chain->next;
            if (chain != NULL) {
                fprintf(stdout, "->");
            }
        }
        fprintf(stdout, "\n");
    }
#endif
}
#endif

static inline FilePosition pos_vec_iter_next(struct pos_vec_iter *iter) {
    return iter->vec->array[iter->index++];
}

static inline bool pos_vec_iter_has_next(struct pos_vec_iter *iter) {
    return iter->index < iter->vec->length;
}

static inline void pos_vec_add(struct pos_vec *vec, FilePosition position) {
    if (vec->length == vec->capacity) {
        vec->capacity <<= 1;
        vec->array = realloc(vec->array, sizeof(FilePosition) * vec->capacity);
    }
    vec->array[vec->length++] = position;
}

static inline void pos_vec_init(struct pos_vec *vec, FilePosition initial) {
    vec->length = 0;
    vec->capacity = VEC_DEF_CAP;
    vec->array = malloc(sizeof(FilePosition) * VEC_DEF_CAP);
    vec->array[vec->length++] = initial;
}

static inline void do_compaction(WordIndex *index) {
    for (size_t i = 0; i < index->table.capacity; i++) {
        if (index->table.table[i] != NULL) {
            struct pos_vec *v = &index->table.table[i]->pos_vec;
            v->capacity = v->length;
            index->table.table[i]->pos_vec.array =
                realloc(v->array, v->capacity * sizeof(FilePosition));
        }
    }
}