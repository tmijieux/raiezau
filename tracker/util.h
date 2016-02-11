#ifndef UTIL_H
#define UTIL_H

void start_detached_thread(
    void* (*task)(void*), void *param, const char *name);

#endif //UTIL_H
