#if !defined(TH_WORD_INDEX_H)
#define TH_WORD_INDEX_H
#include <stdbool.h>
#include <stdio.h>

#define TERM_BUFFER_MARK 0

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
                                                 const char *word, size_t context,
                                                 void *previous_entry);

/**
 * @brief
 *
 * @param index
 * @param read_buffer
 * @param read_buffer_size
 * @param word
 * @param context
 * @param previous_entry
 * @return void*
 */
void *file_word_index_read_context_one_by_one(WordIndex *index, char *read_buffer,
                                              size_t read_buffer_size, char *word,
                                              size_t context, void *previous_entry);

/**
 * @brief
 *
 * @param index
 */
void file_word_index_close(WordIndex *index);

#endif  // TH_WORD_INDEX_H
