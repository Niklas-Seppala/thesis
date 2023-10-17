#include <ctype.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/resource.h>
#include <sys/time.h>
#include <unistd.h>

#include "windex.h"

#define BUFFER_SIZE 4096

void query(WordIndex *index) {
    char word[] = "god";
    char *buffer = malloc(BUFFER_SIZE);
    enum context ctx = SMALL_CONTEXT;
    void *remaining = NULL;
    int count = 0;
    while (1) {
        remaining = file_word_index_read_with_context_buffered(
            index, buffer, BUFFER_SIZE, word, strlen(word), ctx, remaining);
        char *cursor = buffer;
        while (1) {
            if ((cursor + sizeof(uint32_t)) - buffer > BUFFER_SIZE) {
                printf("ERRR\n");
                abort();
            }
            uint32_t *offset = (uint32_t *)cursor;
            // Terminate reading.
            if (*offset == BUFF_TERM_MARK) {
                break;
            }
            count++;
            printf("len:%3d ", *offset);

            // Print string.
            char *str = cursor + sizeof(uint32_t);
            putc('"', stdout);
            for (uint32_t i = 0; i < *offset; i++) {
                if (str[i] == '\n')
                    putc(' ', stdout);
                else
                    putc(str[i], stdout);
            }
            putc('"', stdout);
            putc('\n', stdout);
            cursor += *offset + sizeof(uint32_t);
        }
        if (remaining == NULL) {
            break;
        }
    }

    printf("COUNT: %d\n", count);
    free(buffer);
}

int main(void) {
    WordIndex *index = file_word_index_open("test/tfile.txt", 1 << 16, 8192, true);
    if (index != NULL) {
        query(index);
        file_word_index_close(index);
    }
    return 0;
}
