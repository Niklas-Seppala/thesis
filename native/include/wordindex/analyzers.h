#if !defined(WORD_INDEX_ANALYZERS_H)
#define WORD_INDEX_ANALYZERS_H

#include <stdlib.h>
#include <ctype.h>
#include <stdbool.h>

/**
 * @brief
 */
enum index_analyzer {
    // Tokenizes text to words.
    //
    // Words break on newlines, whitespace,
    // and '.', ',', '!', '?', ':', ';' characters.
    TEXT = 0
};

/**
 * @brief
 *
 * @param analyzer
 * @param c
 * @return true
 * @return false
 */
bool analyzer_word_should_break_at(enum index_analyzer analyzer, char c);

/**
 * @brief
 *
 * @param analyzer
 * @return const char*
 */
const char *analyzer_get_delim(enum index_analyzer analyzer);

/**
 * @brief
 *
 * @param analyzer
 * @param original
 * @param original_len
 * @param normalized
 * @return size_t
 */
size_t analyzer_normalize(enum index_analyzer analyzer, const char *original,
                          size_t original_len, char *normalized);

/**
 * @brief
 *
 * @param analyzer
 * @param text
 * @param save_ptr
 * @return const char*
 */
const char *analyzer_tokenize_word(enum index_analyzer analyzer, char *text,
                                   char **save_ptr);

#endif  // WORD_INDEX_ANALYZERS_H
