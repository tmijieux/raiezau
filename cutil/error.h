#ifndef ERROR_H
#define ERROR_H

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <errno.h>

#define __FILENAME__ (strrchr(__FILE__, '/') ? \
                      strrchr(__FILE__, '/') + 1 : __FILE__)

#define rz_error(format, ...)                                        \
    fprintf(stderr, "\e[31;1mERROR: %s\e[32m:\e[31;1m"               \
            "%d\e[32m|\e[31;1m%s:\e[0m " format, __FILENAME__ ,      \
            __LINE__, __PRETTY_FUNCTION__, ##__VA_ARGS__);


#ifdef DEBUG
#define rz_debug(format, ...)                                        \
    fprintf(stderr, "\e[0;30;43mDEBUG:\e[0;31;1m %s\e[32m:\e[31;1m"    \
            "%d\e[32m|\e[31;1m%s:\e[0m " format, __FILENAME__ ,      \
            __LINE__, __PRETTY_FUNCTION__, ##__VA_ARGS__);
#else
#define rz_debug(f, ...)
#endif


#endif //ERROR_H
