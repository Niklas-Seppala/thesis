/**
 * @file wordindex.h
 * @author Niklas Seppälä
 * @copyright Copyright (c) 2023
 */

#if !defined(WINDEX_H)
#define WINDEX_H

#include <stdarg.h>
#include <stdbool.h>
#include <stdlib.h>

#include "wordindex/analyzers.h"
#include "wordindex/utils.h"

#define BUFF_TERM_MAKR_TYPE uint32_t
#define BUFF_TERM_MARK ((BUFF_TERM_MAKR_TYPE)0)
#define BUFF_TERM_MARK_SIZE (sizeof(BUFF_TERM_MAKR_TYPE))

/**
 * @brief
 *
 */
enum context {
    NO_CONTEXT = 0,
    SMALL_CONTEXT = 16,
    MEDIUM_CONTEXT = 64,
    LARGE_CONTEXT = 128
};

/**
 * @brief
 *
 */
typedef struct wordindex WordIndex;

/**
 * @brief Opens a WordIndex over specified file.
 *
 * @param filepath
 * @param analyzer
 * @param capacity
 * @param word_buffer_size
 * @param compact
 * @return WordIndex*
 */
WordIndex *file_word_index_open(const char *filepath, enum index_analyzer analyzer,
                                size_t capacity, size_t word_buffer_size, bool compact);

/**
 * @brief
 *
 * @param index
 * @param read_buffer
 * @param read_buffer_size
 * @param word
 * @param context
 * @return void*
 */
void *file_word_index_read_with_context_buffered(WordIndex *index, char *read_buffer,
                                                 size_t read_buffer_size,
                                                 const char *word, size_t word_len,
                                                 size_t context, void *read_iterator);

/**
 * @brief
 *
 * @param index
 */
void file_word_index_close(WordIndex *index);

/**
 * @brief
 *
 * @param iter
 */
void file_word_index_close_iterator(struct index_read_iterator *iter);

#endif  // WINDEX_H
