/* network.c -- most of code that deals with the socket API */

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <pthread.h>
#include <errno.h>

#include "util.h"
#include "protocol.h"
#include "client.h"
#include "cutil/error.h"


#define HEURISTIC_SIZE 3072

#define CHK(mesg, X_) do { if ((X_) < 0) { perror(#mesg);       \
            exit(EXIT_FAILURE);} } while(0)

char *sockaddr_stringify(const struct sockaddr_in *si)
{
    char *socket_str;
    char addr[20] = {0};
    inet_ntop(AF_INET, &si->sin_addr.s_addr, addr, 19);
    asprintf(&socket_str, "%s:%hu", addr, ntohs(si->sin_port));
    return socket_str;
}

char *ip_stringify(uint32_t ip_addr)
{
    char addr[20] = {0};
    inet_ntop(AF_INET, &ip_addr, addr, 19);
    return strdup(addr);
}

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
    rz_debug("server listening on socket %s\n", sockaddr_stringify(&si));

    *sock = s;
    return 1;
}

static void start_server_reqhandler_thread(
    int accept_s, const struct sockaddr_in *accept_si)
{
    struct client *c;
    c = get_or_create_client(accept_si);
    set_client_sockaddr(c, accept_s, accept_si);
    start_detached_thread((void*(*)(void*))handle_request, c, "request");
}

void server_run(uint32_t addr, uint16_t port)
{
    int s;
    make_listener_socket(addr, port, &s);
    const bool __ever = true;

    for (;__ever;) {
        int accept_s;
        socklen_t size = sizeof accept_s;
        struct sockaddr_in accept_si = { 0 };

        accept_s = accept(s, (struct sockaddr*)&accept_si, &size);
        if (accept_s < 0) {
            if (EINTR == errno)
                continue;
            perror("accept");
            continue;
        }
        start_server_reqhandler_thread(accept_s, &accept_si);
    }
}

void server_run_bind_any_addr(uint16_t port)
{
    server_run(INADDR_ANY, port);
}

int socket_read_string(int sock, char **ret_str)
{
    int r, str_size= 0, str_bufsize = HEURISTIC_SIZE;
    char buf[1024] = {0};
    char *str = calloc(HEURISTIC_SIZE, 1);

    // loop that read and allocate sufficient memory
    // This end when a '\0' or '\n' is read at the very end of input
    do {
        r = read(sock, buf, 1000);
        if (r <= 0) {
            *ret_str = NULL;
            return -1;
        }
        
        buf[r] = 0;
        if (str_size + r > str_bufsize) {
            str = realloc(str, 2*str_bufsize);
            str_bufsize *= 2;
        }
        str_size += r;
        strcat(str, buf);

        rz_debug("terminating character is '#%d' : (%s)\n",
                 buf[r-1],
                 buf[r-1] == '\0'
                 ? "nul"
                 : ( buf[r-1] == '\n'
                     ? "newline"
                     : "unknown"));
    } while ( buf[r-1] != '\0' && buf[r-1] != '\n' );

    // remove '\n' at the end of the line
    while (str_size-1 > 0 && str[str_size-1] == '\n') {
        str[str_size-1] = '\0';
        --str_size;
    }

    *ret_str = str;
    return str_size;
}
