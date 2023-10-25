#include "wordindex/analyzers.h"

#include <string.h>

#include "wordindex/utils.h"

#define MAX_DELIM_CHARS 16

// Delimiter table for different analyzers.
// Analyzer enum points to the index.
static const char delim_table[][MAX_DELIM_CHARS] = {
    " ?!:;,.\n\r"  // TEXT analyzer delimiters
};

bool analyzer_word_should_break_at(enum index_analyzer analyzer, char at_char) {
    const char *analyzer_delims = delim_table[analyzer];
    char delim_char;
    while ((delim_char = *analyzer_delims++)) {
        if (at_char == delim_char) {
            return true;
        }
    }
    return false;
}

size_t analyzer_normalize(enum index_analyzer analyzer, const char *original,
                          size_t original_len, char *normalized) {
    NONNULL(original);
    strncpy(normalized, original, original_len);
    normalized[original_len] = '\0';
    char c;
    char *d = normalized;
    size_t length = 0;
    while ((c = *normalized++)) {
        if (analyzer_word_should_break_at(analyzer, c)) {
            *(normalized - 1) = '\0';
            break;
        }
        length++;
    }
    for (; *d; d++) *d = tolower(*d);
    return length;
}

const char *analyzer_tokenize_word(enum index_analyzer analyzer, char *text,
                                   char **save_ptr) {
    return strtok_r(text, delim_table[analyzer], save_ptr);
}

const char *analyzer_get_delim(enum index_analyzer analyzer) {
    return delim_table[analyzer];
}
