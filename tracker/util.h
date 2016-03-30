#ifndef UTIL_H
#define UTIL_H

#include <sys/types.h>
#include <regex.h>


#define var_switch(tmp, x, y)                   \
    do {                                        \
        tmp = x;                                \
        x = y;                                  \
        y = tmp;                                \
    } while (0)

#define STRING_EQUAL(x, y) (!strcmp((x), (y)))

#define RED_COLOR "31;1"
#define GREEN_COLOR "32"
#define RESET_COLOR "0"
#define COLOR(__c, __s) ESCAPE(__c) __s ESCAPE(RESET)
#define ESCAPE(__s) "\x1B[" __s##_COLOR "m"


void start_detached_thread(
    void* (*task)(void*), void *param, const char *name);

void start_attached_thread(
    pthread_t *t, void* (*task)(void*), void *param, const char *name);

int regex_exec(const char *regexp, const char *str,
               size_t nmatch, regmatch_t pmatch[]);

char *int_stringify(int i);

#endif //UTIL_H
