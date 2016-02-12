#ifndef CLIENT_H
#define CLIENT_H

#include <stdint.h>
#include "rz.h"
#include "network.h"

struct client {
    int sock;
    struct sockaddr_in addr;

    uint16_t listening_port;
    struct list *files;
};

struct client *get_or_create_client(const struct sockaddr_in *si);
void set_client_sockaddr(
    struct client *c, int sock, const struct sockaddr_in *si);


#endif //CLIENT_H
