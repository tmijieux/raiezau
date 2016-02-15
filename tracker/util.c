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

int regex_exec(const char *regexp, const char *str,
               size_t nmatch, regmatch_t pmatch[])
{
    int ret;
    regex_t reg;
    
    ret = regcomp(&reg, regexp, REG_EXTENDED | REG_ICASE);
    if (ret != 0) {
        char err[500] = {0};
        regerror(ret, &reg, err, 500);
        rz_error(_("regexp compilation: %s\n"), err);
        return -1;
    }

    if (regexec(&reg, str, nmatch, pmatch, 0) != 0) {
        rz_debug(_("regex doesn't match: '%s'\n"), str);
        regfree(&reg);
        return -1;
    }

    regfree(&reg);
    return 0;
}
