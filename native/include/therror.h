#if !defined(CTL_ERROR_H)
#define CTL_ERROR_H

#include <stdarg.h>
#include <stdlib.h>

#include "stdio.h"

#define NOT_USED(x) (void)(x)

#define UNDEFINED

#ifndef __FUNCTION_NAME__
#ifdef WIN32
#define __FUNCTION_NAME__ __FUNCTION__
#else
#define __FUNCTION_NAME__ __func__
#endif
#endif

#define PRINTF_ERROR(format, ...)                   \
    {                                               \
        printf(format, __VA_ARGS__);                \
        printf(                                     \
            "\tat %s(), %s:%d"                      \
            "\n",                                   \
            __FUNCTION_NAME__, __FILE__, __LINE__); \
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

#endif  // CTL_ERROR_H
