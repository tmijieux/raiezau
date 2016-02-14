#ifndef CLIENT_H
#define CLIENT_H

#include <stdint.h>
#include "rz.h"
#include "network.h"
#include "cutil/list.h"

struct client {
    int sock;
    struct sockaddr_in addr;

    uint16_t listening_port;
    
    struct list *files_seed;
    struct list *files_leech;
};

struct client *get_or_create_client(const struct sockaddr_in *si);
void set_client_sockaddr(
    struct client *c, int sock, const struct sockaddr_in *si);

struct list *client_list(void);

#endif //CLIENT_H
