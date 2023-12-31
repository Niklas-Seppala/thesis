/**
 * @file wordindex/utils.h
 * @author Nikals Seppälä
 * @copyright Copyright (c) 2023
 */

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
#define ALLOC_ERR "Failed to allocate"

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
#define NONNULL(ptr) UNDEFINED
#endif

#ifndef VEC_DEF_CAP
#define VEC_DEF_CAP 8
#endif

typedef size_t FilePosition;

// --------------------------------------------------

/**
 * @brief Dynamic vector that can hold word FilePositions.
 *        Automatically resizes when full, by doubling current
 *        capacity.
 */
struct pos_vec {
    size_t capacity;      // Current vector capacity.
    size_t length;        // Current length (number of entries held).
    FilePosition *array;  // Underylying array.
};

/**
 * @brief Iterator used when reading from the index in buffered
 *        manner.
 */
struct index_read_iterator {
    FILE *file;           // Open file to read from using word positions.
    size_t index;         // Next position index.
    struct pos_vec *vec;  // Vector that contains word FilePositions.
};

/**
 * @brief Adds FilePosition to the vector. If vector is full it
 *        will resize by doubling it's capacity.
 *
 * @param vec Vector to add position to.
 * @param position Position to add.
 *
 * @return true  - When action was completed succesfully.
 * @return false - When action failed.
 */
bool pos_vec_add(struct pos_vec *vec, FilePosition position);

/**
 * @brief Allocates vector that holds FilePositions
 *        with specified capacity.
 *
 * @param vec Vector to initialize.
 * @param initial Initial capacity of the vector.
 *
 * @return true  - When action was completed succesfully.
 * @return false - When action failed.
 */
bool pos_vec_init(struct pos_vec *vec, FilePosition initial);

/**
 * @brief Check if iterator has more FilePositions to read.
 *
 * @param iter Iterator object to check.
 *
 * @return true - When iterator has more items to read.
 * @return false - When iterator is exhausted.
 */
bool pos_vec_iter_has_next(struct index_read_iterator *iter);

/**
 * @brief Gets the next word FilePosition from iterator.
 *
 * @param iter Iterator to read from.
 * @return FilePosition word FilePosition read by the iterator.
 */
FilePosition pos_vec_iter_next(struct index_read_iterator *iter);

// --------------------------------------------------

/**
 * @brief Entry stored in hashmap.
 */
struct hash_entry {
    char *word;
    size_t word_len;
    uint32_t hash;
    struct pos_vec pos_vec;
    struct hash_entry *next;
};

/**
 * @brief Hashtable
 */
struct hash_table {
    size_t capacity;
    struct hash_entry **table;
};

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
