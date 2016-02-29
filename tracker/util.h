#ifndef UTIL_H
#define UTIL_H

#include <sys/types.h>
#include <regex.h>

void start_detached_thread(
    void* (*task)(void*), void *param, const char *name);

void start_attached_thread(
    pthread_t *t, void* (*task)(void*), void *param, const char *name);

int regex_exec(const char *regexp, const char *str,
               size_t nmatch, regmatch_t pmatch[]);

char *int_stringify(int i);

#endif //UTIL_H
