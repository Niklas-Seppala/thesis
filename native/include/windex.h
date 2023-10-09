/**
 * @file windex.h
 * @author Niklas Seppälä
 * @brief Public api of word index
 * @date 2023-10-06
 * 
 * @copyright Copyright (c) 2023
 * 
 */

#if !defined(WINDEX_H)
#define WINDEX_H

#include <stdarg.h>
#include <stdbool.h>
#include <stdlib.h>

#define BUFF_TERM_MARK 0

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
 * @brief
 *
 * @param filepath
 * @param capacity
 * @param word_buffer_size
 * @param compact
 * @return WordIndex*
 */
WordIndex *file_word_index_open(const char *filepath, size_t capacity,
                                size_t word_buffer_size, bool compact);

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
                                                 size_t context, void *previous_entry);

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
void file_word_index_close_iterator(void *iter);

#endif  // WINDEX_H
