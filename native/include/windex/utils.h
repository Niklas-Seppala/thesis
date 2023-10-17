#if !defined(WINDEX_UTILS_H)
#define WINDEX_UTILS_H
#include <errno.h>
#include <inttypes.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

#define NOT_USED(x) (void)(x)
#define UNDEFINED
#ifndef __FUNCTION_NAME__
#ifdef WIN32
#define __FUNCTION_NAME__ __FUNCTION__
#else
#define __FUNCTION_NAME__ __func__
#endif
#endif

extern int errno;
#define ALLOC_ERR "Failed to allcoate"

#define PRINTF_ERROR(format, ...)                       \
    {                                                   \
        fprintf(stderr, format, __VA_ARGS__);           \
        fprintf(stderr,                                 \
                "\tat %s(), %s:%d"                      \
                "\n",                                   \
                __FUNCTION_NAME__, __FILE__, __LINE__); \
    }

#define PRINTF_ERROR_WITH_ERRNO(format, ...)                   \
    {                                                          \
        fprintf(stderr, format, __VA_ARGS__);                  \
        fprintf(stderr,                                        \
                "\n\tat %s(), %s:%d"                           \
                "\n\t",                                        \
                __FUNCTION_NAME__, __FILE__, __LINE__);        \
        fprintf(stderr, "\tcaused by: %s\n", strerror(errno)); \
    }

#ifdef NULL_CHECKS
#define NONNULL(ptr)                                                           \
    {                                                                          \
        if (ptr == NULL) {                                                     \
            PRINTF_ERROR("%s\n", "NullPointerError on variable \"" #ptr "\""); \
            abort();                                                           \
        }                                                                      \
    }
#else
#define NONNULL UNDEFINED
#endif

#ifndef VEC_DEF_CAP
#define VEC_DEF_CAP 8
#endif

typedef size_t FilePosition;

// --------------------------------------------------

struct pos_vec {
    size_t capacity;
    size_t length;
    FilePosition *array;
};

struct index_read_iterator {
    FILE *file;
    size_t index;
    struct pos_vec *vec;
};

/**
 * @brief
 *
 * @param vec
 * @param position
 */
void pos_vec_add(struct pos_vec *vec, FilePosition position);

/**
 * @brief
 *
 * @param vec
 * @param initial
 */
void pos_vec_init(struct pos_vec *vec, FilePosition initial);

/**
 * @brief
 *
 * @param iter
 * @return true
 * @return false
 */
bool pos_vec_iter_has_next(struct index_read_iterator *iter);

/**
 * @brief
 *
 * @param iter
 * @return FilePosition
 */
FilePosition pos_vec_iter_next(struct index_read_iterator *iter);

// --------------------------------------------------

struct hash_entry {
    char *word;
    size_t word_len;
    uint32_t hash;
    struct pos_vec pos_vec;
    struct hash_entry *next;
};

struct hash_table {
    size_t capacity;
    struct hash_entry **table;
};

// --------------------------------------------------

/**
 * @brief
 *
 * @param bytes
 * @param value
 */
void write_u32(char *bytes, uint32_t value);

// --------------------------------------------------

/**
 * @brief
 *
 * @param word
 * @return size_t
 */
size_t normalize_word(char *word);

// --------------------------------------------------

/**
 * @brief
 *
 * @param word
 * @return uint32_t
 */
uint32_t hash(const void *word);

// --------------------------------------------------
#ifdef DBG
/**
 * @brief
 *
 * @param fname
 * @param wcount
 * @param table
 */
void dbg(const char *fname, size_t wcount, const struct hash_table *table);
#endif

#endif  // WINDEX_UTILS_H
