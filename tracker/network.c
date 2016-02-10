#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>
#include <errno.h>

#include "protocol.h"
#include "client.h"
#include "cutil/error.h"

#define CHK(X_) do { if ((X_) < 0) { perror(#X_);       \
            exit(EXIT_FAILURE);} } while(0)
#define CHK2(X_) do { if (NULL == (X_)) { perror(#X_);  \
            exit(EXIT_FAILURE);} } while(0)

char *sockaddr_stringify(const struct sockaddr_in *si)
{
    char *socket_str;
    char addr[20] = {0};
    inet_ntop(AF_INET, &si->sin_addr.s_addr, addr, 19);
    asprintf(&socket_str, "%s:%hu", addr, ntohs(si->sin_port));
    return socket_str;
}

int make_listener_socket(uint32_t addr, uint16_t port, int *sock)
{
    int s;
    struct sockaddr_in si = { 0 };
    int yes[1] = { 1 };

    CHK(s = socket(AF_INET, SOCK_STREAM, 0));
    CHK(setsockopt(s, SOL_SOCKET, SO_REUSEADDR, yes, sizeof yes[0]));

    si.sin_family = AF_INET;
    si.sin_port = htons(port);
    si.sin_addr.s_addr = addr;

    CHK(bind(s, (const struct sockaddr*) &si, sizeof si));
    CHK(listen(s, SOMAXCONN));
    rz_debug("server listening on socket %s\n", sockaddr_stringify(&si));

    *sock = s;
    return 1;
}

void server_run(uint32_t addr, uint16_t port)
{
    int s;
    make_listener_socket(addr, port, &s);

    while (1) {
        pthread_t t;
        pthread_attr_t attr;
        int accept_s;
        struct sockaddr_in accept_si = { 0 };
        socklen_t size = sizeof accept_s;
        struct client *c;

        CHK(accept_s = accept(s,(struct sockaddr*)&accept_si, &size));

        c = get_or_create_client(&accept_si);
        set_client_sockaddr(c, accept_s, &accept_si);

        pthread_attr_init(&attr);
        pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
        if (pthread_create(&t, &attr, (void*(*)(void*))handle_request, c) < 0) {
            fprintf(stderr, "cannot create thread\n");
            exit(EXIT_FAILURE);
        }
        pthread_attr_destroy(&attr);
    }
}

void server_run_bind_any(uint16_t port)
{
    server_run(INADDR_ANY, port);
}
