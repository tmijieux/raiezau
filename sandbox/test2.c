#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <signal.h>
#include <pthread.h>
#include <errno.h>
#include <poll.h>
#include <locale.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#include "../cutil/string2.h"

#define CHK(mesg, X_) do { if ((X_) < 0) { perror(#mesg);       \
            exit(EXIT_FAILURE);} } while(0)




int main(int argc, char * const argv[])
{

    char *str = "abc!=def!=ghi!=jkl";
    char **tab;

    int n = string_split2(str, "!=", &tab);

    for (int i = 0; i < n; ++i)
        puts(tab[i]);

    return EXIT_SUCCESS;
}

