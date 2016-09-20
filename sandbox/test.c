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


#define CHK(mesg, X_) do { if ((X_) < 0) { perror(#mesg);       \
            exit(EXIT_FAILURE);} } while(0)


static int make_listener_socket(uint32_t addr, uint16_t port, int *sock)
{
    int s;
    struct sockaddr_in si = { 0 };
    int yes[1] = { 1 };

    CHK(socket, s = socket(AF_INET, SOCK_STREAM, 0));
    CHK(setsockopt, setsockopt(
        s, SOL_SOCKET, SO_REUSEADDR, yes, sizeof yes[0]));

    si.sin_family = AF_INET;
    si.sin_port = htons(port);
    si.sin_addr.s_addr = addr;

    CHK(bind, bind(s, (const struct sockaddr*) &si, sizeof si));
    CHK(listen, listen(s, SOMAXCONN));

    *sock = s;
    return 1;
}




int main(int argc, char * const argv[])
{
    int s, s2, ret;
    char buf[10];

    make_listener_socket(INADDR_ANY, 1234, &s);
    struct sockaddr_in si = {0};
    socklen_t addr_len = sizeof si;


    s2 = accept(s, &si, &addr_len);


    ret = read(s2, buf, 10);
    if (ret < 0) {
        int err_save = errno;
        fprintf(stderr, "read: %s\n", strerror(errno));
        setlocale(LC_ALL, "");
        errno = err_save;
        fprintf(stderr, "read: %s\n", strerror(errno));
    }
    printf("ret: %d\n", ret);

    return EXIT_SUCCESS;
}

