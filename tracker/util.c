#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#include "cutil/error.h"
#include "util.h"

void start_detached_thread(
    void* (*task)(void*), void *param, const char *name)
{
    pthread_t t;
    pthread_attr_t attr;

    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    if (pthread_create(&t, &attr, task, param) < 0) {
        fprintf(stderr, _("cannot create %s thread\n"), name);
            exit(EXIT_FAILURE);
    }
    pthread_attr_destroy(&attr);
}

void start_attached_thread(
    pthread_t *t, void* (*task)(void*), void *param, const char *name)
{
    if (pthread_create(t, NULL, task, param) < 0) {
        fprintf(stderr, _("cannot create %s thread\n"), name);
            exit(EXIT_FAILURE);
    }
}

char *int_stringify(int i)
{
    char *str;
    asprintf(&str, "%d", i);
    return str;
}

int regex_exec(const char *regexp, const char *str,
               size_t nmatch, regmatch_t pmatch[])
{
    int err = 0;
    regex_t reg;
    
    err = regcomp(&reg, regexp, REG_EXTENDED | REG_ICASE);
    if (err != 0) {
        char err_s[500] = {0};
        regerror(err, &reg, err_s, 500);
        rz_error(_("regexp compilation: %s\n"), err_s);
        err = -1;
    } else {
        if (regexec(&reg, str, nmatch, pmatch, 0) != 0) {
            rz_debug(_("regex doesn't match: '%s'\npattern was '%s'\n"),
                     str, regexp);
            err = -1;
        }
        regfree(&reg);
    }
    return err;
}
