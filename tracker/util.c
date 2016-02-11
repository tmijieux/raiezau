#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#include "util.h"

void start_detached_thread(
    void* (*task)(void*), void *param, const char *name)
{
    pthread_t t;
    pthread_attr_t attr;

    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    if (pthread_create(&t, &attr, task, param) < 0) {
        fprintf(stderr, "cannot create %s thread\n", name);
            exit(EXIT_FAILURE);
    }
    pthread_attr_destroy(&attr);
}
