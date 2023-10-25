#include <ctype.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/resource.h>
#include <unistd.h>

#include "wordindex.h"

#define BUFFER_SIZE 4096

void query(WordIndex *index) {
    char word[] = "easy";
    char *buffer = malloc(BUFFER_SIZE);
    enum context ctx = SMALL_CONTEXT;
    void *remaining = NULL;
    int count = 0;
    while (true) {
        remaining = file_word_index_read_with_context_buffered(
            index, buffer, BUFFER_SIZE, word, strlen(word), ctx, remaining);
        char *cursor = buffer;
        while (1) {
            if ((cursor + sizeof(uint32_t)) - buffer > BUFFER_SIZE) {
                printf("ERRR\n");
                abort();
            }
            uint32_t offset;
            memcpy(&offset, cursor, sizeof(uint32_t));

            // Terminate reading.
            if (offset == BUFF_TERM_MARK) {
                break;
            }
            count++;
            cursor += offset + sizeof(uint32_t);
        }
        if (remaining == NULL) {
            break;
        }
    }

    printf("COUNT: %d\n", count);
    free(buffer);
}

int main(void) {
    WordIndex *index = file_word_index_open("test/tfile.txt", TEXT, 15, 8192, true);
    if (index != NULL) {
        query(index);
    }
    file_word_index_close(index);
    return 0;
}
