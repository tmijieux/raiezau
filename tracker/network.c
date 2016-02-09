#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

#include "protocol.h"

#define MAX_FORK_TRY 10

#define CHK(X_) do { if ((X_) < 0) { perror(#X_);       \
            exit(EXIT_FAILURE);} } while(0)
#define CHK2(X_) do { if (NULL == (X_)) { perror(#X_);  \
            exit(EXIT_FAILURE);} } while(0)

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

    *sock = s;
    return 1;
}

void server_run(uint32_t addr, uint16_t port)
{
    int s;
    make_listener_socket(addr, port, &s);
    
    while (1) {
        int accept_s;
        struct sockaddr_in accept_si = { 0 };
        socklen_t size = sizeof accept_s;
        struct client *c;
        
        CHK(accept_s = accept(s,(struct sockaddr*)&accept_si, &size));
        
        c = get_client(accept_si);
        set_client_sockaddr(c, accept_s, accept_si);
        
        if (pthread_create(&t, attr, handle_request, c) < 0) {
            fprintf(stderr, "cannot create thread\n");
            exit(EXIT_FAILURE);
        }
    }
}

void server_run_bind_any(uint16_t port)
{
    server_run(INADDR_ANY, port);
}
