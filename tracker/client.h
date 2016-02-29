#ifndef CLIENT_H
#define CLIENT_H

#include <stdint.h>
#include <stdbool.h>
#include "rz.h"
#include "network.h"
#include "cutil/list.h"

struct conn_state {
    bool announced;
};
    
struct client {
    int sock;
    int slot;
    struct sockaddr_in addr;

    uint16_t listening_port;
    
    struct list *files_seed;
    struct list *files_leech;

    pthread_t current_thr;
    struct conn_state state;
};

struct client *get_or_create_client(int sock, int slot);
void delete_client(int sock, int slot);
struct list *client_list(void);

#endif //CLIENT_H
