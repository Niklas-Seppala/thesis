#include "windex/utils.h"

#include <ctype.h>

void write_u32(char *bytes, uint32_t value) {
    *bytes++ = value & 0xff;
    *bytes++ = (value >> 8) & 0xff;
    *bytes++ = (value >> 16) & 0xff;
    *bytes++ = (value >> 24) & 0xff;
}

void pos_vec_add(struct pos_vec *vec, FilePosition position) {
    if (vec->length == vec->capacity) {
        vec->capacity <<= 1;
        vec->array = realloc(vec->array, sizeof(FilePosition) * vec->capacity);
    }
    vec->array[vec->length++] = position;
}

void pos_vec_init(struct pos_vec *vec, FilePosition initial) {
    vec->length = 0;
    vec->capacity = VEC_DEF_CAP;
    vec->array = malloc(sizeof(FilePosition) * VEC_DEF_CAP);
    vec->array[vec->length++] = initial;
}

bool pos_vec_iter_has_next(struct pos_vec_iter *iter) {
    return iter->index < iter->vec->length;
}

FilePosition pos_vec_iter_next(struct pos_vec_iter *iter) {
    return iter->vec->array[iter->index++];
}

size_t normalize_word(char *word) {
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

uint32_t hash(const void *word) {
    if (word == NULL) return 0;

    const uint8_t *str = word;
    uint32_t hash = 0;
    int c;
    while ((c = *str++)) hash = c + (hash << 6) + (hash << 16) - hash;

    return hash;
}

#ifdef DBG
static void dbg(const char *fname, size_t wcount, const struct hash_table *table) {
    fprintf(stdout, "[WordIndex: (%s) table-capacity: %ld words: %ld]\n", fname,
            table->capacity, wcount);

#ifdef DBG_HASH
    for (size_t i = 0; i < table->capacity; i++) {
        struct hash_entry *chain = table->table[i];
        if (chain == NULL) continue;
        fprintf(stdout, "table[%ld] == ", i);
        while (chain != NULL) {
            fprintf(stdout, "(\"%s\"", chain->word);
#ifdef DBG_POS
            fprintf(stdout, " %ld/%ld - [", chain->pos_vec.length,
                    chain->pos_vec.capacity);
            for (size_t i = 0; i < chain->pos_vec.length; i++) {
                fprintf(stdout, "%ld", chain->pos_vec.array[i]);
                if (i + 1 < chain->pos_vec.length) {
                    putc(',', stdout);
                }
            }
            fprintf(stdout, "]");
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